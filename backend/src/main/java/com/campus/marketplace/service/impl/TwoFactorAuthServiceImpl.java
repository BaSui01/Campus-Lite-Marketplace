package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.TwoFactorSetupResponse;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.TwoFactorAuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 双因素认证服务实现类 - 真实实现不使用模拟数据！
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorAuthServiceImpl implements TwoFactorAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleAuthenticator googleAuthenticator;
    private final ObjectMapper objectMapper;

    private static final String ISSUER = "CampusMarketplace";
    private static final int RECOVERY_CODE_COUNT = 8;
    private static final int RECOVERY_CODE_LENGTH = 8;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 启用 2FA（生成密钥和 QR 码）
     */
    @Override
    @Transactional
    public TwoFactorSetupResponse enable2FA(Long userId) {
        log.info("🔐 启用 2FA: userId={}", userId);

        // 1. 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 检查是否已启用 2FA
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "2FA 已启用，请先禁用后再重新启用");
        }

        // 3. 生成 TOTP 密钥
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secret = key.getKey();

        // 4. 生成 QR 码 URL
        String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(
                ISSUER,
                user.getEmail() != null ? user.getEmail() : user.getUsername(),
                key
        );

        // 5. 生成恢复码
        List<String> recoveryCodes = generateRecoveryCodes();

        // 6. 加密并存储恢复码（使用 BCrypt）
        List<String> hashedRecoveryCodes = recoveryCodes.stream()
                .map(passwordEncoder::encode)
                .collect(Collectors.toList());

        try {
            String recoveryCodesJson = objectMapper.writeValueAsString(hashedRecoveryCodes);
            user.setTwoFactorRecoveryCodes(recoveryCodesJson);
        } catch (JsonProcessingException e) {
            log.error("❌ 恢复码序列化失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "恢复码生成失败");
        }

        // 7. 保存密钥（暂时不启用，等待用户验证）
        user.setTwoFactorSecret(secret);
        userRepository.save(user);

        log.info("✅ 2FA 密钥生成成功: userId={}", userId);

        // 8. 返回响应（包含明文恢复码，仅此一次显示）
        return new TwoFactorSetupResponse(
                secret,
                qrCodeUrl,
                recoveryCodes,
                "请使用 Google Authenticator 扫描二维码，并妥善保存恢复码（仅显示一次）"
        );
    }

    /**
     * 验证 2FA 代码并完成启用
     */
    @Override
    @Transactional
    public boolean verify2FAAndEnable(Long userId, String code) {
        log.info("🔐 验证 2FA 代码并启用: userId={}", userId);

        // 1. 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 检查是否已生成密钥
        if (user.getTwoFactorSecret() == null || user.getTwoFactorSecret().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "请先生成 2FA 密钥");
        }

        // 3. 验证代码
        boolean isValid = googleAuthenticator.authorize(user.getTwoFactorSecret(), Integer.parseInt(code));

        if (!isValid) {
            log.warn("⚠️ 2FA 代码验证失败: userId={}, code={}", userId, code);
            return false;
        }

        // 4. 启用 2FA
        user.setTwoFactorEnabled(true);
        user.setTwoFactorEnabledAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("✅ 2FA 启用成功: userId={}", userId);
        return true;
    }

    /**
     * 禁用 2FA
     */
    @Override
    @Transactional
    public void disable2FA(Long userId, String password) {
        log.info("🔐 禁用 2FA: userId={}", userId);

        // 1. 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR, "密码错误");
        }

        // 3. 禁用 2FA
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setTwoFactorRecoveryCodes(null);
        user.setTwoFactorEnabledAt(null);
        userRepository.save(user);

        log.info("✅ 2FA 禁用成功: userId={}", userId);
    }

    /**
     * 验证 2FA 代码（登录时使用）
     */
    @Override
    public boolean verify2FACode(Long userId, String code) {
        log.info("🔐 验证 2FA 代码: userId={}", userId);

        // 1. 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 检查是否启用 2FA
        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "2FA 未启用");
        }

        // 3. 验证代码
        boolean isValid = googleAuthenticator.authorize(user.getTwoFactorSecret(), Integer.parseInt(code));

        if (isValid) {
            log.info("✅ 2FA 代码验证成功: userId={}", userId);
        } else {
            log.warn("⚠️ 2FA 代码验证失败: userId={}", userId);
        }

        return isValid;
    }

    /**
     * 使用恢复码验证（当用户丢失 2FA 设备时）
     */
    @Override
    @Transactional
    public boolean verifyRecoveryCode(Long userId, String recoveryCode) {
        log.info("🔐 验证恢复码: userId={}", userId);

        // 1. 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 检查是否启用 2FA
        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "2FA 未启用");
        }

        // 3. 获取恢复码列表
        List<String> hashedRecoveryCodes;
        try {
            hashedRecoveryCodes = objectMapper.readValue(
                    user.getTwoFactorRecoveryCodes(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        } catch (JsonProcessingException e) {
            log.error("❌ 恢复码反序列化失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "恢复码验证失败");
        }

        // 4. 验证恢复码
        for (int i = 0; i < hashedRecoveryCodes.size(); i++) {
            if (passwordEncoder.matches(recoveryCode, hashedRecoveryCodes.get(i))) {
                log.info("✅ 恢复码验证成功: userId={}", userId);

                // 5. 删除已使用的恢复码
                hashedRecoveryCodes.remove(i);
                try {
                    user.setTwoFactorRecoveryCodes(objectMapper.writeValueAsString(hashedRecoveryCodes));
                    userRepository.save(user);
                } catch (JsonProcessingException e) {
                    log.error("❌ 恢复码序列化失败: {}", e.getMessage());
                }

                return true;
            }
        }

        log.warn("⚠️ 恢复码验证失败: userId={}", userId);
        return false;
    }

    /**
     * 重新生成恢复码
     */
    @Override
    @Transactional
    public List<String> regenerateRecoveryCodes(Long userId, String password) {
        log.info("🔐 重新生成恢复码: userId={}", userId);

        // 1. 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR, "密码错误");
        }

        // 3. 检查是否启用 2FA
        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "2FA 未启用");
        }

        // 4. 生成新的恢复码
        List<String> recoveryCodes = generateRecoveryCodes();

        // 5. 加密并存储恢复码
        List<String> hashedRecoveryCodes = recoveryCodes.stream()
                .map(passwordEncoder::encode)
                .collect(Collectors.toList());

        try {
            String recoveryCodesJson = objectMapper.writeValueAsString(hashedRecoveryCodes);
            user.setTwoFactorRecoveryCodes(recoveryCodesJson);
            userRepository.save(user);
        } catch (JsonProcessingException e) {
            log.error("❌ 恢复码序列化失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "恢复码生成失败");
        }

        log.info("✅ 恢复码重新生成成功: userId={}", userId);
        return recoveryCodes;
    }

    /**
     * 检查用户是否启用了 2FA
     */
    @Override
    public boolean is2FAEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return Boolean.TRUE.equals(user.getTwoFactorEnabled());
    }

    // ==================== 私有方法 ====================

    /**
     * 生成恢复码列表
     *
     * @return 恢复码列表
     */
    private List<String> generateRecoveryCodes() {
        List<String> recoveryCodes = new ArrayList<>();
        for (int i = 0; i < RECOVERY_CODE_COUNT; i++) {
            recoveryCodes.add(generateRecoveryCode());
        }
        return recoveryCodes;
    }

    /**
     * 生成单个恢复码（8位数字）
     *
     * @return 恢复码
     */
    private String generateRecoveryCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < RECOVERY_CODE_LENGTH; i++) {
            code.append(SECURE_RANDOM.nextInt(10));
        }
        return code.toString();
    }
}
