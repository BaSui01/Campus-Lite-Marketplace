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
import org.junit.jupiter.api.Nested;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 多方式登录功能单元测试（TDD 红灯阶段）
 *
 * 测试场景：
 * 1. 邮箱登录：basui@campus.edu + Password123 → 成功
 * 2. 手机号登录：13800138000 + Password123 → 成功
 * 3. 用户名登录：basui + Password123 → 成功
 * 4. 混合场景：自动识别凭证类型
 *
 * @author BaSui 😎
 * @date 2025-11-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("🎯 多方式登录测试（邮箱/手机号/用户名）")
class AuthServiceMultiLoginTest {

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

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role studentRole;
    private Permission viewPermission;

    @BeforeEach
    void setUp() {
        // 设置 JWT 过期时间
        ReflectionTestUtils.setField(authService, "jwtExpiration", 7200000L);

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

        // 创建测试用户（包含完整信息）
        testUser = User.builder()
                .username("basui")
                .password("$2a$10$encodedPassword")
                .email("basui@campus.edu")
                .phone("13800138000")
                .status(UserStatus.ACTIVE)
                .points(100)
                .roles(new HashSet<>(Set.of(studentRole)))
                .build();
        testUser.setId(1L);

        // Mock Redis operations
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ========== 邮箱登录测试 ==========

    @Nested
    @DisplayName("📧 邮箱登录")
    class EmailLoginTests {

        @Test
        @DisplayName("成功 - 使用邮箱 basui@campus.edu 登录")
        void loginByEmail_Success() {
            // 🔴 红灯：这个测试会失败，因为 AuthServiceImpl.login() 还不支持邮箱登录

            // Arrange
            LoginRequest request = new LoginRequest("basui@campus.edu", "Password123");

            when(userRepository.findByEmail("basui@campus.edu"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("Password123", "$2a$10$encodedPassword"))
                    .thenReturn(true);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList(), anyList()))
                    .thenReturn("mock-jwt-token");

            // Act
            LoginResponse response = authService.login(request);

            // Assert
            assertNotNull(response);
            assertEquals("mock-jwt-token", response.getToken());
            assertEquals("basui", response.getUserInfo().getUsername());
            assertEquals("basui@campus.edu", response.getUserInfo().getEmail());

            // Verify
            verify(userRepository).findByEmail("basui@campus.edu");
            verify(userRepository, never()).findByUsernameWithRoles(anyString());
            verify(passwordEncoder).matches("Password123", "$2a$10$encodedPassword");
        }

        @Test
        @DisplayName("失败 - 邮箱不存在")
        void loginByEmail_Fail_EmailNotFound() {
            // Arrange
            LoginRequest request = new LoginRequest("notexist@campus.edu", "Password123");

            when(userRepository.findByEmail("notexist@campus.edu"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.login(request));

            assertEquals(ErrorCode.PASSWORD_ERROR.getCode(), exception.getCode());
            verify(userRepository).findByEmail("notexist@campus.edu");
        }

        @Test
        @DisplayName("失败 - 邮箱正确但密码错误")
        void loginByEmail_Fail_WrongPassword() {
            // Arrange
            LoginRequest request = new LoginRequest("basui@campus.edu", "WrongPassword");

            when(userRepository.findByEmail("basui@campus.edu"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("WrongPassword", "$2a$10$encodedPassword"))
                    .thenReturn(false);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.login(request));

            assertEquals(ErrorCode.PASSWORD_ERROR.getCode(), exception.getCode());
        }
    }

    // ========== 手机号登录测试 ==========

    @Nested
    @DisplayName("📱 手机号登录")
    class PhoneLoginTests {

        @Test
        @DisplayName("成功 - 使用手机号 13800138000 登录")
        void loginByPhone_Success() {
            // 🔴 红灯：这个测试会失败，因为 AuthServiceImpl.login() 还不支持手机号登录

            // Arrange
            LoginRequest request = new LoginRequest("13800138000", "Password123");

            when(userRepository.findByPhone("13800138000"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("Password123", "$2a$10$encodedPassword"))
                    .thenReturn(true);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList(), anyList()))
                    .thenReturn("mock-jwt-token");

            // Act
            LoginResponse response = authService.login(request);

            // Assert
            assertNotNull(response);
            assertEquals("mock-jwt-token", response.getToken());
            assertEquals("13800138000", testUser.getPhone());

            // Verify
            verify(userRepository).findByPhone("13800138000");
            verify(userRepository, never()).findByEmail(anyString());
            verify(userRepository, never()).findByUsernameWithRoles(anyString());
        }

        @Test
        @DisplayName("失败 - 手机号不存在")
        void loginByPhone_Fail_PhoneNotFound() {
            // Arrange
            LoginRequest request = new LoginRequest("13900139000", "Password123");

            when(userRepository.findByPhone("13900139000"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.login(request));

            assertEquals(ErrorCode.PASSWORD_ERROR.getCode(), exception.getCode());
        }
    }

    // ========== 用户名登录测试 ==========

    @Nested
    @DisplayName("👤 用户名登录")
    class UsernameLoginTests {

        @Test
        @DisplayName("成功 - 使用用户名 basui 登录")
        void loginByUsername_Success() {
            // 🟢 绿灯：这个测试会通过，因为现有代码已支持用户名登录

            // Arrange
            LoginRequest request = new LoginRequest("basui", "Password123");

            when(userRepository.findByUsernameWithRoles("basui"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("Password123", "$2a$10$encodedPassword"))
                    .thenReturn(true);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList(), anyList()))
                    .thenReturn("mock-jwt-token");

            // Act
            LoginResponse response = authService.login(request);

            // Assert
            assertNotNull(response);
            assertEquals("mock-jwt-token", response.getToken());
            assertEquals("basui", response.getUserInfo().getUsername());

            // Verify
            verify(userRepository).findByUsernameWithRoles("basui");
        }
    }

    // ========== 自动识别凭证类型测试 ==========

    @Nested
    @DisplayName("🔍 自动识别凭证类型")
    class CredentialAutoDetectionTests {

        @Test
        @DisplayName("识别邮箱格式 - 包含 @ 符号")
        void detectEmail_WithAtSymbol() {
            // Arrange
            LoginRequest request = new LoginRequest("test@campus.edu", "Password123");

            when(userRepository.findByEmail("test@campus.edu"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList(), anyList()))
                    .thenReturn("token");

            // Act
            authService.login(request);

            // Assert - 应该调用 findByEmail 而不是 findByUsernameWithRoles
            verify(userRepository).findByEmail("test@campus.edu");
            verify(userRepository, never()).findByUsernameWithRoles(anyString());
        }

        @Test
        @DisplayName("识别手机号格式 - 11位纯数字")
        void detectPhone_ElevenDigits() {
            // Arrange
            LoginRequest request = new LoginRequest("13800138000", "Password123");

            when(userRepository.findByPhone("13800138000"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList(), anyList()))
                    .thenReturn("token");

            // Act
            authService.login(request);

            // Assert - 应该调用 findByPhone
            verify(userRepository).findByPhone("13800138000");
            verify(userRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("识别用户名 - 不符合邮箱和手机号规则")
        void detectUsername_NotEmailOrPhone() {
            // Arrange
            LoginRequest request = new LoginRequest("basui", "Password123");

            when(userRepository.findByUsernameWithRoles("basui"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList(), anyList()))
                    .thenReturn("token");

            // Act
            authService.login(request);

            // Assert - 应该调用 findByUsernameWithRoles
            verify(userRepository).findByUsernameWithRoles("basui");
            verify(userRepository, never()).findByEmail(anyString());
            verify(userRepository, never()).findByPhone(anyString());
        }

        @Test
        @DisplayName("边界 - 中文用户名应识别为用户名")
        void detectUsername_ChineseCharacters() {
            // Arrange
            User chineseUser = User.builder()
                    .username("八岁啊")
                    .password("$2a$10$encodedPassword")
                    .email("basui@campus.edu")
                    .status(UserStatus.ACTIVE)
                    .points(100)
                    .roles(new HashSet<>(Set.of(studentRole)))
                    .build();
            chineseUser.setId(2L);

            LoginRequest request = new LoginRequest("八岁啊", "Password123");

            when(userRepository.findByUsernameWithRoles("八岁啊"))
                    .thenReturn(Optional.of(chineseUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList(), anyList()))
                    .thenReturn("token");

            // Act
            authService.login(request);

            // Assert - 中文用户名应该识别为用户名
            verify(userRepository).findByUsernameWithRoles("八岁啊");
        }
    }

    // ========== 边界情况测试 ==========

    @Nested
    @DisplayName("🛡️ 边界情况与异常处理")
    class EdgeCasesTests {

        @Test
        @DisplayName("空字符串凭证")
        void emptyCredential() {
            // Arrange
            LoginRequest request = new LoginRequest("", "Password123");

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.login(request));

            assertEquals(ErrorCode.PASSWORD_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("null 凭证 - 应该抛出 BusinessException")
        void nullCredential() {
            // Arrange
            LoginRequest request = new LoginRequest(null, "Password123");

            // Act & Assert - 预期抛出 BusinessException 而不是 NullPointerException
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.login(request));

            assertEquals(ErrorCode.PASSWORD_ERROR.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("手机号格式错误 - 10位数字应识别为用户名")
        void invalidPhoneFormat_TenDigits() {
            // Arrange
            LoginRequest request = new LoginRequest("1380013800", "Password123");

            when(userRepository.findByUsernameWithRoles("1380013800"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(BusinessException.class, () -> authService.login(request));

            // Verify - 10位数字不符合手机号规则,应识别为用户名
            verify(userRepository).findByUsernameWithRoles("1380013800");
            verify(userRepository, never()).findByPhone(anyString());
        }

        @Test
        @DisplayName("邮箱格式边界 - 无域名部分（test@ 也识别为邮箱）")
        void invalidEmailFormat_NoDomain() {
            // Arrange
            LoginRequest request = new LoginRequest("test@", "Password123");

            when(userRepository.findByEmail("test@"))
                    .thenReturn(Optional.empty());

            // Act & Assert - 因为包含 @ 所以识别为邮箱（虽然格式不完整）
            assertThrows(BusinessException.class, () -> authService.login(request));

            // Verify - 应该调用 findByEmail 而不是 findByUsernameWithRoles
            verify(userRepository).findByEmail("test@");
            verify(userRepository, never()).findByUsernameWithRoles(anyString());
        }
    }
}
