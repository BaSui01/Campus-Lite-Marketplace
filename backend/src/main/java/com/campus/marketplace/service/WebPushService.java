package com.campus.marketplace.service;

public interface WebPushService {
    void send(Long userId, String title, String body, String url);
}
