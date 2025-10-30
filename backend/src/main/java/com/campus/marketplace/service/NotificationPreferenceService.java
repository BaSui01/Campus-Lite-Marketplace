package com.campus.marketplace.service;

import com.campus.marketplace.common.enums.NotificationChannel;

import java.time.LocalTime;
/**
 * Notification Preference Service
 *
 * @author BaSui
 * @date 2025-10-29
 */


public interface NotificationPreferenceService {
    void setChannelEnabled(Long userId, NotificationChannel channel, boolean enabled);
    void setQuietHours(Long userId, NotificationChannel channel, LocalTime start, LocalTime end);
    boolean isChannelEnabled(Long userId, NotificationChannel channel);
    boolean isInQuietHours(Long userId, NotificationChannel channel, LocalTime now);
    void unsubscribe(Long userId, String templateCode, NotificationChannel channel);
    void resubscribe(Long userId, String templateCode, NotificationChannel channel);
    boolean isUnsubscribed(Long userId, String templateCode, NotificationChannel channel);
}
