package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.LoginRequest;
import com.campus.marketplace.common.dto.request.RegisterRequest;
import com.campus.marketplace.common.dto.request.ResetPasswordByEmailRequest;
import com.campus.marketplace.common.dto.request.ResetPasswordBySmsRequest;
import com.campus.marketplace.common.dto.response.LoginResponse;
import com.campus.marketplace.common.entity.Role;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.JwtUtil;
import com.campus.marketplace.repository.RoleRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final com.campus.marketplace.service.VerificationCodeService verificationCodeService;
    private final com.campus.marketplace.common.utils.CryptoUtil cryptoUtil;
    private final com.campus.marketplace.service.CaptchaService captchaService; // 新增 - BaSui 2025-11-09
    private final com.campus.marketplace.service.TwoFactorAuthService twoFactorAuthService; // 新增 - BaSui 2025-11-09
    private final com.campus.marketplace.service.LoginNotificationService loginNotificationService; // 新增 - BaSui 2025-11-09
    private final com.campus.marketplace.service.UserService userService; // 新增 - BaSui 2025-11-10

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * 用户注册（支持密码加密传输）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request) {
        log.info("用户注册: username={}, email={}", request.username(), request.email());

        // 1. 🔐 解密密码（如果是加密密码）
        String plainPassword;
        try {
            if (cryptoUtil.isEncrypted(request.password())) {
                plainPassword = cryptoUtil.decryptPassword(request.password());
                log.debug("✅ 注册密码解密成功");
            } else {
                // 兼容明文密码
                plainPassword = request.password();
                log.warn("⚠️ 注册接收到明文密码");
            }
        } catch (com.campus.marketplace.common.exception.CryptoException e) {
            log.error("❌ 注册密码解密失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码格式错误，请重试");
        }

        // 2. 检查用户名是否已存在
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 3. 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }

        // 4. 创建用户（使用解密后的明文密码）
        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(plainPassword))
                .email(request.email())
                .status(UserStatus.ACTIVE)
                .points(100) // 注册赠送 100 积分
                .build();

        // 4. 分配默认角色 ROLE_STUDENT
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND, "学生角色不存在"));
        user.addRole(studentRole);

        // 5. 保存用户
        userRepository.save(user);

        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());

        // 返回用户ID
        return user.getId();
    }

    // ========== 邮箱验证码注册/重置密码 ==========

    @Override
    public void sendRegisterEmailCode(String email) {
        verificationCodeService.sendEmailCode(email, "REGISTER");
    }

    @Override
    @Transactional
    public void registerByEmailCode(com.campus.marketplace.common.dto.request.ConfirmRegisterByEmailRequest request) {
        if (!verificationCodeService.validateEmailCode(request.email(), "REGISTER", request.code())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "验证码错误或已过期");
        }
        register(new RegisterRequest(request.username(), request.password(), request.email()));
        // 成功后可删除验证码（VerificationCodeService内部有TTL，这里不强依赖删除）
    }

    @Override
    public void sendResetEmailCode(String email) {
        try {
            if (userRepository.existsByEmail(email)) {
                verificationCodeService.sendEmailCode(email, "RESET");
            }
        } finally {
            log.info("请求发送重置密码邮箱验证码 email=***{}", email.substring(Math.max(0, email.length()-4)));
        }
    }

    @Override
    @Transactional
    public void resetPasswordByEmailCode(ResetPasswordByEmailRequest request) {
        if (!verificationCodeService.validateEmailCode(request.email(), "RESET", request.code())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "验证码错误或已过期");
        }
        
        // 🔐 解密新密码（如果是加密密码）
        String plainNewPassword;
        try {
            if (cryptoUtil.isEncrypted(request.newPassword())) {
                plainNewPassword = cryptoUtil.decryptPassword(request.newPassword());
                log.debug("✅ 重置密码解密成功");
            } else {
                plainNewPassword = request.newPassword();
                log.warn("⚠️ 重置密码接收到明文");
            }
        } catch (com.campus.marketplace.common.exception.CryptoException e) {
            log.error("❌ 重置密码解密失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码格式错误，请重试");
        }
        
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(plainNewPassword));
        userRepository.save(user);
    }

    // ========== 短信验证码重置密码 ==========

    @Override
    public void sendResetSmsCode(String phone) {
        verificationCodeService.sendSmsCode(phone, "RESET");
    }

    @Override
    @Transactional
    public void resetPasswordBySmsCode(ResetPasswordBySmsRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getPhone() == null || !user.getPhone().equals(request.phone())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "手机号与账号不匹配");
        }
        if (!verificationCodeService.validateSmsCode(request.phone(), "RESET", request.code())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "验证码错误或已过期");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }


    /**
     * 用户登录（支持邮箱/手机号/用户名三种方式 🎯）
     *
     * BaSui 新增：多方式登录支持！
     * - 邮箱登录：包含 @ 符号 → findByEmail
     * - 手机号登录：11位纯数字 → findByPhone
     * - 用户名登录：其他格式 → findByUsernameWithRoles
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        String credential = request.username();
        log.info("用户登录: credential={}", credential);

        // 0. 🔐 验证验证码（新增 - BaSui 2025-11-09）
        // ⚠️ 如果是2FA验证阶段，跳过验证码检查（修复 - BaSui 2025-11-10）
        // 🎯 优先使用验证码通行证（captchaToken），兼容旧方式（更新 - BaSui 2025-11-11）
        if (request.twoFactorCode() == null || request.twoFactorCode().isEmpty()) {
            // 🎯 方案B（推荐）：使用验证码通行证（新增 - BaSui 2025-11-11）
            if (request.captchaToken() != null && !request.captchaToken().isEmpty()) {
                boolean isValid = captchaService.verifyCaptchaToken(request.captchaToken());
                if (!isValid) {
                    log.warn("❌ 验证码通行证验证失败: captchaToken={}", request.captchaToken());
                    throw new BusinessException(ErrorCode.CAPTCHA_ERROR, "验证码已过期或无效，请重新验证");
                }
                log.info("✅ 验证码通行证验证通过");
            }
            // 🎯 方案A（兼容旧方式）：直接验证验证码（保留兼容性）
            else if (request.captchaId() != null && request.captchaCode() != null) {
                // 1️⃣ 图形验证码验证
                boolean isValid = captchaService.verifyImageCaptcha(request.captchaId(), request.captchaCode());
                if (!isValid) {
                    log.warn("❌ 图形验证码验证失败: captchaId={}, code={}", request.captchaId(), request.captchaCode());
                    throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
                }
                log.info("✅ 图形验证码验证通过");
            } else if (request.captchaId() != null && request.slidePosition() != null) {
                // 2️⃣ 滑块验证码验证
                boolean isValid = captchaService.verifySlideCaptcha(request.captchaId(), request.slidePosition());
                if (!isValid) {
                    log.warn("❌ 滑块验证码验证失败: slideId={}, position={}", request.captchaId(), request.slidePosition());
                    throw new BusinessException(ErrorCode.SLIDE_VERIFY_FAILED);
                }
                log.info("✅ 滑块验证码验证通过");
            } else if (request.captchaId() != null && request.rotateAngle() != null) {
                // 3️⃣ 旋转验证码验证
                com.campus.marketplace.common.dto.request.RotateVerifyRequest rotateRequest =
                        new com.campus.marketplace.common.dto.request.RotateVerifyRequest(
                                request.captchaId(),
                                request.rotateAngle()
                        );
                boolean isValid = captchaService.verifyRotateCaptcha(rotateRequest);
                if (!isValid) {
                    log.warn("❌ 旋转验证码验证失败: rotateId={}, angle={}", request.captchaId(), request.rotateAngle());
                    throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
                }
                log.info("✅ 旋转验证码验证通过");
            } else if (request.captchaId() != null && request.clickPoints() != null && !request.clickPoints().isEmpty()) {
                // 4️⃣ 点击验证码验证
                java.util.List<com.campus.marketplace.common.dto.request.ClickVerifyRequest.ClickPoint> clickPoints =
                        request.clickPoints().stream()
                                .map(p -> new com.campus.marketplace.common.dto.request.ClickVerifyRequest.ClickPoint(p.x(), p.y()))
                                .toList();
                com.campus.marketplace.common.dto.request.ClickVerifyRequest clickRequest =
                        new com.campus.marketplace.common.dto.request.ClickVerifyRequest(
                                request.captchaId(),
                                clickPoints
                        );
                boolean isValid = captchaService.verifyClickCaptcha(clickRequest);
                if (!isValid) {
                    log.warn("❌ 点击验证码验证失败: clickId={}, points={}", request.captchaId(), request.clickPoints().size());
                    throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
                }
                log.info("✅ 点击验证码验证通过");
            }
        } else {
            log.info("🔐 2FA验证阶段，跳过验证码检查");
        }

        // 1. 🔐 解密密码（如果是加密密码）
        String plainPassword;
        try {
            if (cryptoUtil.isEncrypted(request.password())) {
                plainPassword = cryptoUtil.decryptPassword(request.password());
                log.debug("✅ 密码解密成功，用户名: {}", credential);
            } else {
                // 兼容旧客户端明文密码（过渡期）
                plainPassword = request.password();
                log.warn("⚠️ 接收到明文密码，用户名: {}", credential);
            }
        } catch (com.campus.marketplace.common.exception.CryptoException e) {
            log.error("❌ 密码解密失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码格式错误，请重试");
        }

        // 2. 🔍 自动识别凭证类型并查询用户（包含角色和权限）
        User user = findUserByCredential(credential)
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSWORD_ERROR));

        // 3. 验证密码（使用解密后的明文密码）
        if (!passwordEncoder.matches(plainPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 3. 检查用户状态
        if (user.isBanned()) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }

        // 4. 🔐 检查是否启用了 2FA（新增 - BaSui 2025-11-09）
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            // 如果请求中没有提供 2FA 代码，返回 requires2FA=true
            if (request.twoFactorCode() == null || request.twoFactorCode().isEmpty()) {
                log.info("🔐 用户启用了 2FA，需要验证: userId={}", user.getId());

                // 生成临时 Token（有效期 5 分钟）
                String tempToken = jwtUtil.generateTempToken(user.getId());
                redisTemplate.opsForValue().set("temp_token:" + tempToken, user.getId(), 5, TimeUnit.MINUTES);

                return LoginResponse.builder()
                        .requires2FA(true)
                        .tempToken(tempToken)
                        .build();
            }

            // 如果提供了 2FA 代码，验证它
            log.info("🔐 验证 2FA 代码: userId={}", user.getId());
            boolean isValid = twoFactorAuthService.verify2FACode(user.getId(), request.twoFactorCode());

            if (!isValid) {
                // 尝试使用恢复码验证
                log.info("🔐 2FA 代码验证失败，尝试恢复码: userId={}", user.getId());
                isValid = twoFactorAuthService.verifyRecoveryCode(user.getId(), request.twoFactorCode());

                if (!isValid) {
                    log.warn("❌ 2FA 验证失败: userId={}", user.getId());
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "2FA 验证码错误");
                } else {
                    log.info("✅ 恢复码验证成功: userId={}", user.getId());
                }
            } else {
                log.info("✅ 2FA 代码验证成功: userId={}", user.getId());
            }
        }

        // 5. 获取角色和权限
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.toList());

        // 5. 生成双 Token（Access Token + Refresh Token）
        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), roles, permissions);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 6. 将 Access Token 存入 Redis（用于登出验证）
        String accessTokenKey = "token:" + accessToken;
        redisTemplate.opsForValue().set(accessTokenKey, user.getId(), jwtExpiration, TimeUnit.MILLISECONDS);

        // 7. 将 Refresh Token 存入 Redis（用于刷新验证和撤销）
        String refreshTokenKey = "refresh_token:" + refreshToken;
        redisTemplate.opsForValue().set(refreshTokenKey, user.getId(), 604800000L, TimeUnit.MILLISECONDS); // 7天

        // 8. 记录登录日志
        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        // 9. 📧 发送登录通知 - 先检测新设备再保存（异步，新增 - BaSui 2025-11-09）
        // ⚠️ 重要：必须先调用 detectAndNotifyNewDevice（检查），再调用 recordLoginDevice（保存）
        // 否则每次登录都会被判定为新设备！
        try {
            loginNotificationService.detectAndNotifyNewDevice(user.getId(), httpRequest);
        } catch (Exception e) {
            log.error("❌ 发送登录通知失败: userId={}, error={}", user.getId(), e.getMessage());
            // 不影响登录流程
        }

        // 10. 💾 记录登录设备 - 在通知之后保存（新增 - BaSui 2025-11-10）
        try {
            userService.recordLoginDevice(user.getId(), httpRequest);
        } catch (Exception e) {
            log.error("❌ 记录登录设备失败: userId={}, error={}", user.getId(), e.getMessage());
            // 不影响登录流程
        }

        // 10. 构建响应
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .refreshExpiresIn(604800000L) // 7天
                .userInfo(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .avatar(user.getAvatar())
                        .points(user.getPoints())
                        .roles(roles)
                        .permissions(permissions)
                        .build())
                .build();
    }

    /**
     * 🔍 根据凭证自动识别查询方式
     *
     * 识别规则：
     * - 包含 @ → 邮箱登录
     * - 11位纯数字 → 手机号登录
     * - 其他 → 用户名登录
     *
     * @param credential 登录凭证（邮箱/手机号/用户名）
     * @return 用户（可能为空）
     */
    private Optional<User> findUserByCredential(String credential) {
        if (credential == null || credential.isEmpty()) {
            return Optional.empty();
        }

        // 邮箱登录：包含 @
        if (credential.contains("@")) {
            log.debug("识别为邮箱登录: {}", credential);
            return userRepository.findByEmail(credential);
        }

        // 手机号登录：11位纯数字
        if (credential.matches("^\\d{11}$")) {
            log.debug("识别为手机号登录: {}", credential);
            return userRepository.findByPhone(credential);
        }

        // 用户名登录：其他格式（包括中文用户名）
        log.debug("识别为用户名登录: {}", credential);
        return userRepository.findByUsernameWithRoles(credential);
    }

    /**
     * 用户登出
     */
    @Override
    public void logout(String token) {
        log.info("用户登出");

        // 从 Redis 中删除 Token
        String redisKey = "token:" + token;
        redisTemplate.delete(redisKey);

        log.info("用户登出成功");
    }

    /**
     * 刷新 Token（使用 Refresh Token）
     */
    @Override
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(String oldRefreshToken) {
        log.info("刷新 Token");

        // 1. 验证 Refresh Token
        if (!jwtUtil.validateRefreshToken(oldRefreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "Refresh Token 无效或已过期");
        }

        // 2. 检查 Refresh Token 是否在 Redis 中（是否已被撤销）
        String refreshTokenKey = "refresh_token:" + oldRefreshToken;
        Long userId = (Long) redisTemplate.opsForValue().get(refreshTokenKey);
        if (userId == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "Refresh Token 已被撤销");
        }

        // 3. 从 Refresh Token 中获取用户名
        String username = jwtUtil.getUsernameFromToken(oldRefreshToken);

        // 4. 查询用户
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 5. 检查用户状态
        if (user.isBanned()) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }

        // 6. 获取角色和权限
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.toList());

        // 7. 生成新的双 Token
        String newAccessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), roles, permissions);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 8. 存储新的 Access Token 到 Redis
        String accessTokenKey = "token:" + newAccessToken;
        redisTemplate.opsForValue().set(accessTokenKey, user.getId(), jwtExpiration, TimeUnit.MILLISECONDS);

        // 9. 删除旧的 Refresh Token，存储新的 Refresh Token
        redisTemplate.delete(refreshTokenKey);
        String newRefreshTokenKey = "refresh_token:" + newRefreshToken;
        redisTemplate.opsForValue().set(newRefreshTokenKey, user.getId(), 604800000L, TimeUnit.MILLISECONDS); // 7天

        log.info("Token 刷新成功: userId={}", user.getId());

        // 10. 构建响应
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .refreshExpiresIn(604800000L) // 7天
                .userInfo(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .avatar(user.getAvatar())
                        .points(user.getPoints())
                        .roles(roles)
                        .permissions(permissions)
                        .build())
                .build();
    }

    // ========== BaSui 新增：实时校验方法实现 🎯 ==========

    /**
     * 检查用户名是否已存在
     */
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否已存在
     */
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
