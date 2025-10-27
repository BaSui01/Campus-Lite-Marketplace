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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 认证服务单元测试
 * 
 * 测试用户注册功能
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务测试")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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
    }

    @Test
    @DisplayName("注册成功 - 用户名和邮箱都不存在")
    void register_Success_WhenUsernameAndEmailNotExist() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encrypted_password");
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        authService.register(validRequest);

        // Assert
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("testuser@campus.edu");
        verify(passwordEncoder).encode("Password123");
        verify(roleRepository).findByName("ROLE_STUDENT");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void register_Fail_WhenUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(validRequest);
        });

        assertEquals(ErrorCode.USERNAME_EXISTS.getCode(), exception.getCode());
        assertEquals(ErrorCode.USERNAME_EXISTS.getMessage(), exception.getMessage());
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("注册失败 - 邮箱已存在")
    void register_Fail_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("testuser@campus.edu")).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(validRequest);
        });

        assertEquals(ErrorCode.EMAIL_EXISTS.getCode(), exception.getCode());
        assertEquals(ErrorCode.EMAIL_EXISTS.getMessage(), exception.getMessage());
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("testuser@campus.edu");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("注册失败 - 学生角色不存在")
    void register_Fail_WhenStudentRoleNotFound() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encrypted_password");
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(validRequest);
        });

        assertEquals(ErrorCode.ROLE_NOT_FOUND.getCode(), exception.getCode());
        
        verify(roleRepository).findByName("ROLE_STUDENT");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("注册成功 - 验证用户初始状态")
    void register_Success_VerifyUserInitialState() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encrypted_password");
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
        
        // 捕获保存的用户对象
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            
            // 验证用户初始状态
            assertEquals("testuser", user.getUsername());
            assertEquals("encrypted_password", user.getPassword());
            assertEquals("testuser@campus.edu", user.getEmail());
            assertEquals(UserStatus.ACTIVE, user.getStatus());
            assertEquals(100, user.getPoints()); // 注册赠送 100 积分
            assertFalse(user.getRoles().isEmpty());
            
            user.setId(1L);
            return user;
        });

        // Act
        authService.register(validRequest);

        // Assert
        verify(userRepository).save(any(User.class));
    }
}
