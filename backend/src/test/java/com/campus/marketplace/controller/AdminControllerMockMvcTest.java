package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.config.TestSecurityConfig;
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
import java.util.List;
import java.util.Map;
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

        mockMvc.perform(post("/api/admin/users/ban")
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

        mockMvc.perform(post("/api/admin/users/auto-unban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(3));

        verify(userService).autoUnbanExpiredUsers();
    }

    @Test
    @DisplayName("GET /api/admin/statistics/overview -> 返回统计概览数据")
    @WithMockUser(authorities = PermissionCodes.SYSTEM_STATISTICS_VIEW)
    void getSystemOverview_returnsOverviewData() throws Exception {
        Map<String, Object> overview = Map.of(
                "totalUsers", 1200,
                "todayOrders", 45
        );
        when(statisticsService.getSystemOverview()).thenReturn(overview);

        mockMvc.perform(get("/api/admin/statistics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalUsers").value(1200))
                .andExpect(jsonPath("$.data.todayOrders").value(45));

        verify(statisticsService).getSystemOverview();
    }

    @Test
    @DisplayName("GET /api/admin/statistics/trend -> 带自定义 days 参数")
    @WithMockUser(authorities = PermissionCodes.SYSTEM_STATISTICS_VIEW)
    void getTrendData_withCustomDays() throws Exception {
        Map<String, Object> trend = Map.of(
                "dates", List.of("2025-10-27", "2025-10-28"),
                "newUsers", List.of(12, 18)
        );
        when(statisticsService.getTrendData(7)).thenReturn(trend);

        mockMvc.perform(get("/api/admin/statistics/trend").param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.dates[0]").value("2025-10-27"))
                .andExpect(jsonPath("$.data.newUsers[1]").value(18));

        verify(statisticsService).getTrendData(7);
    }

    @Test
    @DisplayName("GET /api/admin/statistics/top-goods -> 默认参数")
    @WithMockUser(authorities = PermissionCodes.SYSTEM_STATISTICS_VIEW)
    void getTopGoods_returnsRanking() throws Exception {
        List<Map<String, Object>> ranking = List.of(
                Map.of("goodsId", 1L, "goodsName", "iPad", "viewCount", 320),
                Map.of("goodsId", 2L, "goodsName", "Magic Keyboard", "viewCount", 280)
        );
        when(statisticsService.getTopGoods(10)).thenReturn(ranking);

        mockMvc.perform(get("/api/admin/statistics/top-goods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].goodsName").value("iPad"))
                .andExpect(jsonPath("$.data[1].viewCount").value(280));

        verify(statisticsService).getTopGoods(10);
    }
}
