package com.campus.marketplace.service;

public interface FeatureFlagService {

    boolean isEnabled(String key, Long userId, Long campusId, String env);

    void refresh(String key);

    void refreshAll();
}
