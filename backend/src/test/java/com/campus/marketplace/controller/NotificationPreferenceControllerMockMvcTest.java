package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.enums.NotificationChannel;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.NotificationPreferenceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = NotificationPreferenceController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("NotificationPreferenceController MockMvc 测试")
class NotificationPreferenceControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationPreferenceService preferenceService;

    @Test
    @DisplayName("设置渠道开关成功调用服务层")
    @WithMockUser
    void setChannelEnabled_success() throws Exception {
        try (MockedStatic<SecurityUtil> securityUtil = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(42L);

            mockMvc.perform(post("/api/notifications/preferences/channel/{channel}/enabled/{enabled}", "EMAIL", true))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(preferenceService).setChannelEnabled(42L, NotificationChannel.EMAIL, true);
        }
    }

    @Test
    @DisplayName("设置静默时段成功调用服务层")
    @WithMockUser
    void setQuietHours_success() throws Exception {
        try (MockedStatic<SecurityUtil> securityUtil = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(55L);

            mockMvc.perform(post("/api/notifications/preferences/channel/{channel}/quiet-hours", "WEB_PUSH")
                            .param("start", "22:00")
                            .param("end", "07:30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(preferenceService).setQuietHours(eq(55L), eq(NotificationChannel.WEB_PUSH),
                    eq(LocalTime.of(22, 0)), eq(LocalTime.of(7, 30)));
        }
    }

    @Test
    @DisplayName("退订与重新订阅模板成功执行")
    @WithMockUser
    void unsubscribeAndResubscribe_success() throws Exception {
        try (MockedStatic<SecurityUtil> securityUtil = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(77L);

            mockMvc.perform(post("/api/notifications/preferences/unsubscribe/{channel}/{template}", "EMAIL", "ORDER_PAID"))
                    .andExpect(status().isOk());
            verify(preferenceService).unsubscribe(77L, "ORDER_PAID", NotificationChannel.EMAIL);

            mockMvc.perform(delete("/api/notifications/preferences/unsubscribe/{channel}/{template}", "EMAIL", "ORDER_PAID"))
                    .andExpect(status().isOk());
            verify(preferenceService).resubscribe(77L, "ORDER_PAID", NotificationChannel.EMAIL);
        }
    }

    @Test
    @DisplayName("查看通知偏好状态返回各渠道开关与静默信息")
    @WithMockUser
    void status_success() throws Exception {
        try (MockedStatic<SecurityUtil> securityUtil = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(99L);

            when(preferenceService.isChannelEnabled(99L, NotificationChannel.EMAIL)).thenReturn(true);
            when(preferenceService.isChannelEnabled(99L, NotificationChannel.WEB_PUSH)).thenReturn(false);
            when(preferenceService.isInQuietHours(eq(99L), eq(NotificationChannel.EMAIL), any(LocalTime.class))).thenReturn(true);
            when(preferenceService.isInQuietHours(eq(99L), eq(NotificationChannel.WEB_PUSH), any(LocalTime.class))).thenReturn(false);

            mockMvc.perform(get("/api/notifications/preferences/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.emailEnabled").value(true))
                    .andExpect(jsonPath("$.data.webpushEnabled").value(false))
                    .andExpect(jsonPath("$.data.emailQuietNow").value(true))
                    .andExpect(jsonPath("$.data.webpushQuietNow").value(false));
        }
    }
}
