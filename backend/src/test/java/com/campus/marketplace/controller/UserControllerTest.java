package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.dto.request.UpdatePasswordRequest;
import com.campus.marketplace.common.dto.request.UpdateProfileRequest;
import com.campus.marketplace.common.dto.response.UserProfileResponse;
import com.campus.marketplace.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("UserController MockMvc 测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("登录用户获取个人资料成功")
    @WithMockUser(username = "alice")
    void getCurrentUserProfile_success() throws Exception {
        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .roles(List.of("ROLE_USER"))
                .createdAt(LocalDateTime.parse("2025-10-29T12:00:00"))
                .build();
        when(userService.getCurrentUserProfile()).thenReturn(profile);

        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("alice"));

        verify(userService).getCurrentUserProfile();
    }

    @Test
    @DisplayName("未登录访问个人资料被拒绝")
    void getCurrentUserProfile_forbidden() throws Exception {
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getCurrentUserProfile();
    }

    @Test
    @DisplayName("公开获取指定用户资料成功")
    void getUserProfile_success() throws Exception {
        UserProfileResponse profile = UserProfileResponse.builder()
                .id(2L)
                .username("bob")
                .status("ACTIVE")
                .build();
        when(userService.getUserProfile(2L)).thenReturn(profile);

        mockMvc.perform(get("/users/{userId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("bob"));

        verify(userService).getUserProfile(2L);
    }

    @Test
    @DisplayName("登录用户更新资料成功")
    @WithMockUser(username = "alice")
    void updateProfile_success() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "小明",
                "热爱生活，喜欢交友",
                "alice@example.com",
                "13812345678",
                "20240001",
                "https://cdn.example.com/avatar.png"
        );

        mockMvc.perform(put("/users/profile")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("资料更新成功"));

        verify(userService).updateProfile(request);
    }

    @Test
    @DisplayName("未登录更新资料被拒绝")
    void updateProfile_forbidden() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "小明",
                "热爱生活，喜欢交友",
                "alice@example.com",
                "13812345678",
                "20240001",
                "https://cdn.example.com/avatar.png"
        );

        mockMvc.perform(put("/users/profile")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isForbidden());

        verify(userService, never()).updateProfile(any(UpdateProfileRequest.class));
    }

    @Test
    @DisplayName("登录用户修改密码成功")
    @WithMockUser(username = "alice")
    void updatePassword_success() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest("OldPass#123", "NewPass#456");

        mockMvc.perform(put("/users/password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("密码修改成功"));

        verify(userService).updatePassword(request);
    }

    @Test
    @DisplayName("未登录修改密码被拒绝")
    void updatePassword_forbidden() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest("OldPass#123", "NewPass#456");

        mockMvc.perform(put("/users/password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isForbidden());

        verify(userService, never()).updatePassword(any(UpdatePasswordRequest.class));
    }
}
