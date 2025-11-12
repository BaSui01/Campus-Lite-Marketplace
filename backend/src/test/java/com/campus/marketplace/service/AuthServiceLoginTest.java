package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.LoginRequest;
import com.campus.marketplace.common.dto.response.LoginResponse;
import com.campus.marketplace.common.entity.Permission;
import com.campus.marketplace.common.entity.Role;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.JwtUtil;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户登录功能单元测试
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户登录测试")
class AuthServiceLoginTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private com.campus.marketplace.common.utils.CryptoUtil cryptoUtil;

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private jakarta.servlet.http.HttpServletRequest httpRequest;

    @Mock
    private com.campus.marketplace.service.LoginNotificationService loginNotificationService;

    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequest validLoginRequest;
    private User testUser;
    private Role studentRole;
    private Permission viewPermission;

    @BeforeEach
    void setUp() {
        // 设置 JWT 过期时间
        ReflectionTestUtils.setField(authService, "jwtExpiration", 7200000L);

        // 🎯 Mock CryptoUtil 行为：默认返回明文密码（兼容模式）
        when(cryptoUtil.isEncrypted(anyString())).thenReturn(false);

        // 🔐 Mock CaptchaService 行为：验证码通行证验证通过
        when(captchaService.verifyCaptchaToken(anyString())).thenReturn(true);

        // 准备测试数据（新增验证码字段和2FA字段，测试中传 "mock-captcha-token"）
        validLoginRequest = new LoginRequest("testuser", "Password123", "mock-captcha-token", null, null, null, null, null, null);

        // 创建权限
        viewPermission = Permission.builder()
                .id(1L)
                .name("system:user:view")
                .description("查看用户")
                .build();

        // 创建角色
        studentRole = Role.builder()
                .id(1L)
                .name("ROLE_STUDENT")
                .description("学生角色")
                .permissions(new HashSet<>(Set.of(viewPermission)))
                .build();

        // 创建用户
        testUser = User.builder()
                .username("testuser")
                .password("$2a$10$encodedPassword")
                .email("testuser@campus.edu")
                .status(UserStatus.ACTIVE)
                .points(100)
                .roles(new HashSet<>(Set.of(studentRole)))
                .build();
        testUser.setId(1L); // 设置 ID（继承自 BaseEntity）

        // Mock Redis operations (使用 lenient 避免不必要的 stubbing 警告)
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("登录成功 - 正确的用户名和密码")
    void login_Success_WithCorrectCredentials() {
        // Arrange
        when(userRepository.findByUsernameWithRoles("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123", "$2a$10$encodedPassword"))
                .thenReturn(true);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyList(), anyList()))
                .thenReturn("mock-jwt-token");

        // Act
        LoginResponse response = authService.login(validLoginRequest, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(7200000L, response.getExpiresIn());
        
        // 验证用户信息
        assertNotNull(response.getUserInfo());
        assertEquals(1L, response.getUserInfo().getId());
        assertEquals("testuser", response.getUserInfo().getUsername());
        assertEquals("testuser@campus.edu", response.getUserInfo().getEmail());
        assertEquals(100, response.getUserInfo().getPoints());
        
        // 验证角色和权限
        assertTrue(response.getUserInfo().getRoles().contains("ROLE_STUDENT"));
        assertTrue(response.getUserInfo().getPermissions().contains("system:user:view"));

        // 验证方法调用
        verify(userRepository).findByUsernameWithRoles("testuser");
        verify(passwordEncoder).matches("Password123", "$2a$10$encodedPassword");
        verify(jwtUtil).generateToken(eq(1L), eq("testuser"), anyList(), anyList());
        verify(valueOperations).set(eq("token:mock-jwt-token"), eq(1L), eq(7200000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_Fail_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsernameWithRoles("nonexistent"))
                .thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("nonexistent", "Password123", "mock-captcha-token", null, null, null, null, null, null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(request, httpRequest);
        });

        assertEquals(ErrorCode.PASSWORD_ERROR.getCode(), exception.getCode());
        verify(userRepository).findByUsernameWithRoles("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_Fail_WhenPasswordIncorrect() {
        // Arrange
        when(userRepository.findByUsernameWithRoles("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", "$2a$10$encodedPassword"))
                .thenReturn(false);

        LoginRequest request = new LoginRequest("testuser", "WrongPassword", "mock-captcha-token", null, null, null, null, null, null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(request, httpRequest);
        });

        assertEquals(ErrorCode.PASSWORD_ERROR.getCode(), exception.getCode());
        verify(userRepository).findByUsernameWithRoles("testuser");
        verify(passwordEncoder).matches("WrongPassword", "$2a$10$encodedPassword");
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), anyList(), anyList());
    }

    @Test
    @DisplayName("登录失败 - 用户已被封禁")
    void login_Fail_WhenUserBanned() {
        // Arrange
        testUser.setStatus(UserStatus.BANNED);
        
        when(userRepository.findByUsernameWithRoles("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123", "$2a$10$encodedPassword"))
                .thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(validLoginRequest, httpRequest);
        });

        assertEquals(ErrorCode.USER_BANNED.getCode(), exception.getCode());
        verify(userRepository).findByUsernameWithRoles("testuser");
        verify(passwordEncoder).matches("Password123", "$2a$10$encodedPassword");
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), anyList(), anyList());
    }

    @Test
    @DisplayName("登录成功 - 验证 JWT Token 生成参数")
    void login_Success_VerifyJwtTokenGeneration() {
        // Arrange
        when(userRepository.findByUsernameWithRoles("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123", "$2a$10$encodedPassword"))
                .thenReturn(true);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyList(), anyList()))
                .thenReturn("mock-jwt-token");

        // Act
        authService.login(validLoginRequest, httpRequest);

        // Assert - 验证 JWT Token 生成时传入的参数
        verify(jwtUtil).generateToken(
                eq(1L),
                eq("testuser"),
                argThat(roles -> roles.contains("ROLE_STUDENT")),
                argThat(permissions -> permissions.contains("system:user:view"))
        );
    }

    @Test
    @DisplayName("登录成功 - 验证 Token 存入 Redis")
    void login_Success_VerifyTokenStoredInRedis() {
        // Arrange
        when(userRepository.findByUsernameWithRoles("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123", "$2a$10$encodedPassword"))
                .thenReturn(true);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyList(), anyList()))
                .thenReturn("generated-token-12345");

        // Act
        authService.login(validLoginRequest, httpRequest);

        // Assert - 验证 Token 存入 Redis
        verify(valueOperations).set(
                eq("token:generated-token-12345"),
                eq(1L),
                eq(7200000L),
                eq(TimeUnit.MILLISECONDS)
        );
    }
}
