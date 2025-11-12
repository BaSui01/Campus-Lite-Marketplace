package com.campus.marketplace.controller;

import com.campus.marketplace.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 实时校验接口测试（TDD 红灯阶段）🔴
 *
 * 测试场景：
 * 1. 校验用户名是否存在
 * 2. 校验邮箱是否存在
 * 3. 边界情况：空字符串、null、特殊字符
 *
 * @author BaSui 😎
 * @date 2025-11-01
 */
@Disabled("TDD 红灯阶段 - 接口尚未实现")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({com.campus.marketplace.config.TestRedisConfig.class, TestSecurityConfig.class})
@DisplayName("🎯 注册实时校验接口测试")
class AuthControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    // ========== 用户名校验测试 ==========

    @Nested
    @DisplayName("👤 用户名存在性校验")
    class UsernameValidationTests {

        @Test
        @DisplayName("成功 - 用户名已存在")
        void checkUsername_Exists() throws Exception {
            // 🔴 红灯：这个测试会失败，因为接口还不存在

            // Arrange
            when(userRepository.existsByUsername("basui")).thenReturn(true);

            // Act & Assert
            mockMvc.perform(get("/auth/check-username")
                            .param("username", "basui")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true))  // 用户名已存在
                    .andExpect(jsonPath("$.message").value("用户名已存在"));

            // Verify
            verify(userRepository).existsByUsername("basui");
        }

        @Test
        @DisplayName("成功 - 用户名不存在（可注册）")
        void checkUsername_NotExists() throws Exception {
            // Arrange
            when(userRepository.existsByUsername("newuser")).thenReturn(false);

            // Act & Assert
            mockMvc.perform(get("/auth/check-username")
                            .param("username", "newuser")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false))  // 用户名可用
                    .andExpect(jsonPath("$.message").value("用户名可用"));

            verify(userRepository).existsByUsername("newuser");
        }

        @Test
        @DisplayName("失败 - 缺少 username 参数")
        void checkUsername_MissingParameter() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/auth/check-username")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            // 不应该查询数据库
            verify(userRepository, never()).existsByUsername(anyString());
        }

        @Test
        @DisplayName("失败 - username 为空字符串")
        void checkUsername_EmptyString() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/auth/check-username")
                            .param("username", "")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userRepository, never()).existsByUsername(anyString());
        }

        @Test
        @DisplayName("成功 - 中文用户名")
        void checkUsername_ChineseCharacters() throws Exception {
            // Arrange
            when(userRepository.existsByUsername("八岁啊")).thenReturn(false);

            // Act & Assert
            mockMvc.perform(get("/auth/check-username")
                            .param("username", "八岁啊")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false));

            verify(userRepository).existsByUsername("八岁啊");
        }
    }

    // ========== 邮箱校验测试 ==========

    @Nested
    @DisplayName("📧 邮箱存在性校验")
    class EmailValidationTests {

        @Test
        @DisplayName("成功 - 邮箱已存在")
        void checkEmail_Exists() throws Exception {
            // 🔴 红灯：这个测试会失败，因为接口还不存在

            // Arrange
            when(userRepository.existsByEmail("basui@campus.edu")).thenReturn(true);

            // Act & Assert
            mockMvc.perform(get("/auth/check-email")
                            .param("email", "basui@campus.edu")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true))  // 邮箱已被注册
                    .andExpect(jsonPath("$.message").value("邮箱已被注册"));

            verify(userRepository).existsByEmail("basui@campus.edu");
        }

        @Test
        @DisplayName("成功 - 邮箱不存在（可注册）")
        void checkEmail_NotExists() throws Exception {
            // Arrange
            when(userRepository.existsByEmail("newuser@campus.edu")).thenReturn(false);

            // Act & Assert
            mockMvc.perform(get("/auth/check-email")
                            .param("email", "newuser@campus.edu")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false))  // 邮箱可用
                    .andExpect(jsonPath("$.message").value("邮箱可用"));

            verify(userRepository).existsByEmail("newuser@campus.edu");
        }

        @Test
        @DisplayName("失败 - 缺少 email 参数")
        void checkEmail_MissingParameter() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/auth/check-email")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("失败 - email 为空字符串")
        void checkEmail_EmptyString() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/auth/check-email")
                            .param("email", "")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("失败 - email 格式错误（无 @ 符号）")
        void checkEmail_InvalidFormat_NoAtSymbol() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/auth/check-email")
                            .param("email", "invalidemail")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("边界 - email 格式错误（无域名）")
        void checkEmail_InvalidFormat_NoDomain() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/auth/check-email")
                            .param("email", "test@")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(userRepository, never()).existsByEmail(anyString());
        }
    }

    // ========== 性能测试 ==========

    @Nested
    @DisplayName("⚡ 性能与并发测试")
    class PerformanceTests {

        @Test
        @DisplayName("性能 - 校验接口响应时间 < 100ms")
        void checkUsername_ResponseTime() throws Exception {
            // Arrange
            when(userRepository.existsByUsername("basui")).thenReturn(true);

            // Act
            long startTime = System.currentTimeMillis();
            mockMvc.perform(get("/auth/check-username")
                            .param("username", "basui")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            long endTime = System.currentTimeMillis();

            // Assert - 响应时间应该 < 100ms
            long duration = endTime - startTime;
            assertTrue(duration < 100, "响应时间应该 < 100ms，实际: " + duration + "ms");
        }
    }
}
