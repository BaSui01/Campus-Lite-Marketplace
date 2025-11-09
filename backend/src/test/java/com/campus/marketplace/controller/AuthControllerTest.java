package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.ConfirmRegisterByEmailRequest;
import com.campus.marketplace.common.dto.request.ResetPasswordByEmailRequest;
import com.campus.marketplace.common.dto.request.ResetPasswordBySmsRequest;
import com.campus.marketplace.common.dto.response.LoginResponse;
import com.campus.marketplace.common.exception.GlobalExceptionHandler;
import com.campus.marketplace.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController MockMvc 邮箱流程测试")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("发送注册邮箱验证码")
    void sendRegisterEmailCode_success() throws Exception {
        mockMvc.perform(post("/auth/register/code")
                        .param("email", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(authService).sendRegisterEmailCode("user@example.com");
    }

    @Test
    @DisplayName("邮箱验证码注册成功")
    void registerByEmail_success() throws Exception {
        ConfirmRegisterByEmailRequest request = new ConfirmRegisterByEmailRequest(
                "user@example.com",
                "654321",
                "new-user",
                "Passw0rd!"
        );

        mockMvc.perform(post("/auth/register/by-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<ConfirmRegisterByEmailRequest> captor = ArgumentCaptor.forClass(ConfirmRegisterByEmailRequest.class);
        verify(authService).registerByEmailCode(captor.capture());
        assertThat(captor.getValue().email()).isEqualTo("user@example.com");
        assertThat(captor.getValue().code()).isEqualTo("654321");
    }

    @Test
    @DisplayName("发送重置密码邮箱验证码")
    void sendResetEmailCode_success() throws Exception {
        mockMvc.perform(post("/auth/password/reset/code/email")
                        .param("email", "reset@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(authService).sendResetEmailCode("reset@example.com");
    }

    @Test
    @DisplayName("邮箱验证码重置密码成功")
    void resetPasswordByEmail_success() throws Exception {
        ResetPasswordByEmailRequest request = new ResetPasswordByEmailRequest(
                "reset@example.com",
                "112233",
                "NewPassw0rd!"
        );

        mockMvc.perform(post("/auth/password/reset/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<ResetPasswordByEmailRequest> captor = ArgumentCaptor.forClass(ResetPasswordByEmailRequest.class);
        verify(authService).resetPasswordByEmailCode(captor.capture());
        assertThat(captor.getValue().newPassword()).isEqualTo("NewPassw0rd!");
    }

    @Test
    @DisplayName("发送重置密码短信验证码")
    void sendResetSmsCode_success() throws Exception {
        mockMvc.perform(post("/auth/password/reset/code/sms")
                        .param("phone", "+8613012345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(authService).sendResetSmsCode("+8613012345678");
    }

    @Test
    @DisplayName("短信验证码重置密码成功")
    void resetPasswordBySms_success() throws Exception {
        ResetPasswordBySmsRequest request = new ResetPasswordBySmsRequest(
                "tester",
                "+8613012345678",
                "778899",
                "SmsPassw0rd!"
        );

        mockMvc.perform(post("/auth/password/reset/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<ResetPasswordBySmsRequest> captor = ArgumentCaptor.forClass(ResetPasswordBySmsRequest.class);
        verify(authService).resetPasswordBySmsCode(captor.capture());
        assertThat(captor.getValue().phone()).isEqualTo("+8613012345678");
    }

    @Test
    @DisplayName("用户登出时剥离Bearer前缀")
    void logout_success() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(authService).logout("token-123");
    }

    @Test
    @DisplayName("刷新Token返回新凭证")
    void refreshToken_success() throws Exception {
        LoginResponse response = LoginResponse.builder()
                .accessToken("new-token")
                .expiresIn(Instant.now().plusSeconds(3600).toEpochMilli())
                .userInfo(LoginResponse.UserInfo.builder()
                        .id(1L)
                        .username("alice")
                        .email("alice@example.com")
                        .roles(List.of("USER"))
                        .permissions(List.of("read"))
                        .build())
                .build();
        when(authService.refreshToken("old-token")).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", "Bearer old-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("new-token"))
                .andExpect(jsonPath("$.data.userInfo.username").value("alice"));

        verify(authService).refreshToken("old-token");
    }
}
