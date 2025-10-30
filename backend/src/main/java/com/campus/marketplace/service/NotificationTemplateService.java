package com.campus.marketplace.service;

import com.campus.marketplace.common.enums.NotificationChannel;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
/**
 * Notification Template Service
 *
 * @author BaSui
 * @date 2025-10-29
 */


public interface NotificationTemplateService {

    record Rendered(String title, String content, Set<NotificationChannel> channels) {}

    Rendered render(String code, Locale locale, Map<String, Object> params);
}
