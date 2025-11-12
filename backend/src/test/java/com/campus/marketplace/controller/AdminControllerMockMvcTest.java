package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.dto.request.BanUserRequest;
import com.campus.marketplace.common.security.PermissionCodes;
import com.campus.marketplace.service.StatisticsService;
import com.campus.marketplace.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("AdminController MockMvc 集成测试")
class AdminControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private StatisticsService statisticsService;

    @Test
    @DisplayName("POST /api/admin/users/ban -> 具备封禁权限的管理员可以封禁用户")
    @WithMockUser(authorities = PermissionCodes.SYSTEM_USER_BAN)
    void banUser_withAuthority_returnsSuccess() throws Exception {
        BanUserRequest request = new BanUserRequest(100L, "spam", 7);

        mockMvc.perform(post("/admin/users/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data").doesNotExist());

        ArgumentCaptor<BanUserRequest> captor = ArgumentCaptor.forClass(BanUserRequest.class);
        verify(userService).banUser(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(100L);
        assertThat(captor.getValue().reason()).isEqualTo("spam");
        assertThat(captor.getValue().days()).isEqualTo(7);
    }

    @Test
    @DisplayName("POST /api/admin/users/auto-unban -> 返回自动解封数量")
    @WithMockUser(authorities = PermissionCodes.SYSTEM_USER_BAN)
    void autoUnbanExpiredUsers_returnsCount() throws Exception {
        when(userService.autoUnbanExpiredUsers()).thenReturn(3);

        mockMvc.perform(post("/admin/users/auto-unban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(3));

        verify(userService).autoUnbanExpiredUsers();
    }

}
