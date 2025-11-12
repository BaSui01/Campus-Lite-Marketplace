package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.RegisterRequest;
import com.campus.marketplace.common.entity.Role;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.RoleRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AuthServiceImpl 单元测试
 * 
 * 测试用户注册功能的各种场景
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户注册服务测试")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.campus.marketplace.common.utils.CryptoUtil cryptoUtil;

    @Mock
    private com.campus.marketplace.common.utils.JwtUtil jwtUtil;

    @Mock
    private org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @Mock
    private VerificationCodeService verificationCodeService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest validRequest;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        validRequest = new RegisterRequest(
                "testuser",
                "Password123",
                "testuser@campus.edu"
        );

        studentRole = Role.builder()
                .id(1L)
                .name("ROLE_STUDENT")
                .description("学生角色")
                .build();

        // 🎯 Mock CryptoUtil 行为：默认返回明文密码（兼容模式）
        when(cryptoUtil.isEncrypted(anyString())).thenReturn(false);
    }

    @Test
    @DisplayName("注册成功 - 所有条件满足")
    void register_Success_WhenAllConditionsMet() {
        // Given: 准备测试数据
        when(userRepository.existsByUsername(validRequest.username())).thenReturn(false);
        when(userRepository.existsByEmail(validRequest.email())).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(validRequest.password())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When: 执行注册
        authService.register(validRequest);

        // Then: 验证结果
        // 1. 验证用户名和邮箱唯一性检查
        verify(userRepository).existsByUsername(validRequest.username());
        verify(userRepository).existsByEmail(validRequest.email());

        // 2. 验证角色查询
        verify(roleRepository).findByName("ROLE_STUDENT");

        // 3. 验证密码加密
        verify(passwordEncoder).encode(validRequest.password());

        // 4. 验证用户保存
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo(validRequest.username());
        assertThat(savedUser.getEmail()).isEqualTo(validRequest.email());
        assertThat(savedUser.getPassword()).isEqualTo("$2a$10$encodedPassword");
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUser.getPoints()).isEqualTo(100); // 注册赠送 100 积分
        assertThat(savedUser.getRoles()).contains(studentRole);
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void register_ThrowsException_WhenUsernameExists() {
        // Given: 用户名已存在
        when(userRepository.existsByUsername(validRequest.username())).thenReturn(true);

        // When & Then: 执行注册并验证异常
        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USERNAME_EXISTS.getCode())
                .hasFieldOrPropertyWithValue("message", ErrorCode.USERNAME_EXISTS.getMessage());

        // 验证没有继续执行后续操作
        verify(userRepository).existsByUsername(validRequest.username());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("注册失败 - 邮箱已存在")
    void register_ThrowsException_WhenEmailExists() {
        // Given: 邮箱已存在
        when(userRepository.existsByUsername(validRequest.username())).thenReturn(false);
        when(userRepository.existsByEmail(validRequest.email())).thenReturn(true);

        // When & Then: 执行注册并验证异常
        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.EMAIL_EXISTS.getCode())
                .hasFieldOrPropertyWithValue("message", ErrorCode.EMAIL_EXISTS.getMessage());

        // 验证没有继续执行后续操作
        verify(userRepository).existsByUsername(validRequest.username());
        verify(userRepository).existsByEmail(validRequest.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("注册失败 - 学生角色不存在")
    void register_ThrowsException_WhenStudentRoleNotFound() {
        // Given: 学生角色不存在
        when(userRepository.existsByUsername(validRequest.username())).thenReturn(false);
        when(userRepository.existsByEmail(validRequest.email())).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.empty());

        // When & Then: 执行注册并验证异常
        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ROLE_NOT_FOUND.getCode());

        // 验证没有保存用户
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("注册成功 - 验证密码加密")
    void register_Success_PasswordIsEncrypted() {
        // Given: 准备测试数据
        String rawPassword = validRequest.password();
        String encodedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH";

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: 执行注册
        authService.register(validRequest);

        // Then: 验证密码已加密
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isNotEqualTo(rawPassword);
        assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    @DisplayName("注册成功 - 验证默认角色分配")
    void register_Success_StudentRoleIsAssigned() {
        // Given: 准备测试数据
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: 执行注册
        authService.register(validRequest);

        // Then: 验证角色分配
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles()).contains(studentRole);
    }

    @Test
    @DisplayName("注册成功 - 验证初始积分")
    void register_Success_InitialPointsAreSet() {
        // Given: 准备测试数据
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: 执行注册
        authService.register(validRequest);

        // Then: 验证初始积分
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPoints()).isEqualTo(100);
    }

    @Test
    @DisplayName("注册成功 - 验证用户状态为 ACTIVE")
    void register_Success_UserStatusIsActive() {
        // Given: 准备测试数据
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: 执行注册
        authService.register(validRequest);

        // Then: 验证用户状态
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUser.isActive()).isTrue();
        assertThat(savedUser.isBanned()).isFalse();
    }
}
