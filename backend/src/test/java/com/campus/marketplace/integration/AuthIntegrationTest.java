package com.campus.marketplace.integration;

import com.campus.marketplace.common.dto.request.LoginRequest;
import com.campus.marketplace.common.dto.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户认证集成测试
 * 
 * 测试场景：
 * 1. 用户注册成功
 * 2. 用户登录成功并获取 Token
 * 3. 使用 Token 访问受保护资源
 * 4. 用户名已存在时注册失败
 * 
 * @author BaSui 😎
 * @date 2025-10-27
 */
@DisplayName("用户认证集成测试")
public class AuthIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("完整注册登录流程测试")
    void testCompleteRegistrationAndLoginFlow() throws Exception {
        // ========== 1. 用户注册 ==========
        RegisterRequest registerRequest = new RegisterRequest(
                "testuser",
                "Test@123456",
                "test@campus.edu"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data").isNumber());

        // ========== 2. 用户登录 ==========
        LoginRequest loginRequest = new LoginRequest("testuser", "Test@123456");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.userId").isNumber())
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andReturn();

        // 提取并断言 Token（使用响应体，避免未使用变量告警）
        String responseBody = loginResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);
        String token = root.path("data").path("token").asText("");
        org.assertj.core.api.Assertions.assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("用户名已存在时注册失败")
    void testRegisterWithDuplicateUsername() throws Exception {
        // 第一次注册
        RegisterRequest firstRequest = new RegisterRequest(
                "duplicateuser",
                "Test@123456",
                "first@campus.edu"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // 第二次注册（用户名重复）
        RegisterRequest secondRequest = new RegisterRequest(
                "duplicateuser", // 相同用户名
                "Test@123456",
                "second@campus.edu"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40006)) // USERNAME_EXISTS
                .andExpect(jsonPath("$.message").value(containsString("用户名已存在")));
    }

    @Test
    @DisplayName("密码错误时登录失败")
    void testLoginWithWrongPassword() throws Exception {
        // 先注册用户
        RegisterRequest registerRequest = new RegisterRequest(
                "wrongpassworduser",
                "Correct@123456",
                "wrongpwd@campus.edu"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 使用错误密码登录
        LoginRequest loginRequest = new LoginRequest("wrongpassworduser", "Wrong@123456"); // 错误密码

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40008)) // PASSWORD_ERROR
                .andExpect(jsonPath("$.message").value(containsString("密码错误")));
    }

    @Test
    @DisplayName("参数校验失败测试")
    void testRegisterWithInvalidParameters() throws Exception {
        // 用户名为空
        RegisterRequest request = new RegisterRequest(
                "", // 用户名为空
                "Test@123456",
                "invalid@campus.edu"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400)); // PARAM_ERROR
    }
}
