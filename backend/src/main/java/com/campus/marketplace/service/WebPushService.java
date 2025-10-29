package com.campus.marketplace.service;
/**
 * Web Push Service
 *
 * @author BaSui
 * @date 2025-10-29
 */


public interface WebPushService {
    void send(Long userId, String title, String body, String url);
}
