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
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String credential = request.username();
        log.info("用户登录: credential={}", credential);

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

        // 4. 获取角色和权限
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.toList());

        // 5. 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles, permissions);

        // 6. 将 Token 存入 Redis（用于登出验证）
        String redisKey = "token:" + token;
        redisTemplate.opsForValue().set(redisKey, user.getId(), jwtExpiration, TimeUnit.MILLISECONDS);

        // 7. 记录登录日志
        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        // 8. 构建响应
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
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
     * 刷新 Token
     */
    @Override
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(String oldToken) {
        log.info("刷新 Token");

        // 1. 验证旧 Token
        String username = jwtUtil.getUsernameFromToken(oldToken);
        if (!jwtUtil.validateToken(oldToken, username)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        // 2. 查询用户
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3. 检查用户状态
        if (user.isBanned()) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }

        // 4. 获取角色和权限
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.toList());

        // 5. 生成新 Token
        String newToken = jwtUtil.generateToken(user.getId(), user.getUsername(), roles, permissions);

        // 6. 删除旧 Token，存入新 Token
        String oldRedisKey = "token:" + oldToken;
        String newRedisKey = "token:" + newToken;
        redisTemplate.delete(oldRedisKey);
        redisTemplate.opsForValue().set(newRedisKey, user.getId(), jwtExpiration, TimeUnit.MILLISECONDS);

        log.info("Token 刷新成功: userId={}", user.getId());

        // 7. 构建响应
        return LoginResponse.builder()
                .token(newToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
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
