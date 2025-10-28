package com.campus.marketplace.service;

import com.campus.marketplace.common.enums.NotificationChannel;
import com.campus.marketplace.service.impl.WebPushServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebPush 服务实现测试")
class WebPushServiceImplTest {

    @Mock
    private NotificationPreferenceService preferenceService;

    @InjectMocks
    private WebPushServiceImpl webPushService;

    private void setEnabled(boolean enabled) {
        try {
            Field field = WebPushServiceImpl.class.getDeclaredField("enabled");
            field.setAccessible(true);
            field.setBoolean(webPushService, enabled);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("未启用时直接返回")
    void send_disabled_skip() {
        setEnabled(false);

        webPushService.send(1L, "title", "body", "/url");

        verifyNoInteractions(preferenceService);
    }

    @Test
    @DisplayName("通道关闭时不发送")
    void send_channelDisabled_skip() {
        setEnabled(true);
        when(preferenceService.isChannelEnabled(1L, NotificationChannel.WEB_PUSH)).thenReturn(false);

        webPushService.send(1L, "title", "body", "/url");

        verify(preferenceService).isChannelEnabled(1L, NotificationChannel.WEB_PUSH);
        verify(preferenceService, never()).isInQuietHours(any(), any(), any());
    }

    @Test
    @DisplayName("静默时段跳过发送")
    void send_quietHours_skip() {
        setEnabled(true);
        when(preferenceService.isChannelEnabled(1L, NotificationChannel.WEB_PUSH)).thenReturn(true);
        when(preferenceService.isInQuietHours(eq(1L), eq(NotificationChannel.WEB_PUSH), any())).thenReturn(true);

        webPushService.send(1L, "title", "body", "/url");

        verify(preferenceService).isInQuietHours(eq(1L), eq(NotificationChannel.WEB_PUSH), any());
    }

    @Test
    @DisplayName("满足条件时记录发送日志")
    void send_success_logs() {
        setEnabled(true);
        when(preferenceService.isChannelEnabled(1L, NotificationChannel.WEB_PUSH)).thenReturn(true);
        when(preferenceService.isInQuietHours(eq(1L), eq(NotificationChannel.WEB_PUSH), any())).thenReturn(false);

        webPushService.send(1L, "title", "body", "/url");

        verify(preferenceService).isChannelEnabled(1L, NotificationChannel.WEB_PUSH);
        verify(preferenceService).isInQuietHours(eq(1L), eq(NotificationChannel.WEB_PUSH), any());
    }
}
