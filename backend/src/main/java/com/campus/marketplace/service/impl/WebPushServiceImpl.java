package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.enums.NotificationChannel;
import com.campus.marketplace.service.NotificationPreferenceService;
import com.campus.marketplace.service.WebPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

/**
 * Web Push Service Impl
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushServiceImpl implements WebPushService {

    private final NotificationPreferenceService preferenceService;

    @Value("${notifications.webpush.enabled:false}")
    private boolean enabled;

    @Override
    public void send(Long userId, String title, String body, String url) {
        if (!enabled) {
            log.warn("WebPush 未启用，跳过发送: userId={}, title={}", userId, title);
            return;
        }
        if (!preferenceService.isChannelEnabled(userId, NotificationChannel.WEB_PUSH)) {
            log.info("用户关闭了 WebPush: userId={}", userId);
            return;
        }
        if (preferenceService.isInQuietHours(userId, NotificationChannel.WEB_PUSH, LocalTime.now())) {
            log.info("当前处于静默时段，跳过 WebPush: userId={}", userId);
            return;
        }
        // 这里可集成真实的 WebPush 发送逻辑（如 VAPID），当前仅记录日志作为占位实现
        log.info("WebPush 发送: userId={}, title={}, url={}", userId, title, url);
    }
}
