package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.dto.response.NotificationResponse;
import com.campus.marketplace.common.enums.NotificationStatus;
import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = NotificationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("NotificationController MockMvc 测试")
class NotificationControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("未登录访问通知列表返回 403")
    void listNotifications_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isForbidden());

        verify(notificationService, never()).listNotifications(any(), any());
    }

    @Test
    @DisplayName("查询通知列表返回分页结果")
    @WithMockUser
    void listNotifications_success() throws Exception {
        NotificationResponse notification = NotificationResponse.builder()
                .id(1L)
                .type(NotificationType.SYSTEM_ANNOUNCEMENT)
                .title("订单支付成功")
                .content("订单 O202510270001 支付成功")
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.parse("2025-10-29T13:00:00"))
                .build();
        when(notificationService.listNotifications(eq(NotificationStatus.UNREAD), any()))
                .thenReturn(new PageImpl<>(List.of(notification)));

        mockMvc.perform(get("/notifications")
                        .param("status", "UNREAD")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].title").value("订单支付成功"));
    }

    @Test
    @DisplayName("获取未读数量返回整型数据")
    @WithMockUser
    void getUnreadCount_success() throws Exception {
        when(notificationService.getUnreadCount()).thenReturn(5L);

        mockMvc.perform(get("/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @DisplayName("标记通知为已读调用服务层")
    @WithMockUser
    void markAsRead_success() throws Exception {
        String body = "[101,102]";

        mockMvc.perform(put("/notifications/mark-read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"));

        verify(notificationService).markAsRead(eq(List.of(101L, 102L)));
    }

    @Test
    @DisplayName("删除通知时服务抛出未找到返回业务 404")
    @WithMockUser
    void deleteNotifications_notFound() throws Exception {
        BusinessException ex = new BusinessException(ErrorCode.NOT_FOUND, "通知不存在");
        org.mockito.Mockito.doThrow(ex)
                .when(notificationService).deleteNotifications(any());

        mockMvc.perform(delete("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[201]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("通知不存在"));
    }

    @Test
    @DisplayName("全部标记为已读调用服务层")
    @WithMockUser
    void markAllAsRead_success() throws Exception {
        mockMvc.perform(put("/notifications/mark-all-read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(notificationService).markAllAsRead();
    }
}
