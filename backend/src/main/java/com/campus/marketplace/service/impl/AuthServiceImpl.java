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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 * 
 * @author BaSui
 * @date 2025-10-25
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

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    private static final String REG_EMAIL_CODE_KEY = "reg:email:code:";
    private static final String RESET_EMAIL_CODE_KEY = "reset:email:code:";
    private static final String RESET_SMS_CODE_KEY = "reset:sms:code:";

    /**
     * 用户注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        log.info("用户注册: username={}, email={}", request.username(), request.email());

        // 1. 检查用户名是否已存在
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 2. 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }

        // 3. 创建用户
        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
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
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
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
     * 用户登录
     */
    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录: username={}", request.username());

        // 1. 查询用户（包含角色和权限）
        User user = userRepository.findByUsernameWithRoles(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSWORD_ERROR));

        // 2. 验证密码
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
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
}
