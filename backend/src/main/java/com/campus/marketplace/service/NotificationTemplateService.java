package com.campus.marketplace.service;

import com.campus.marketplace.common.enums.NotificationChannel;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface NotificationTemplateService {

    record Rendered(String title, String content, Set<NotificationChannel> channels) {}

    Rendered render(String code, Locale locale, Map<String, Object> params);
}
