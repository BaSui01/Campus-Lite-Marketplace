package com.campus.marketplace.service;
/**
 * Feature Flag Service
 *
 * @author BaSui
 * @date 2025-10-29
 */


public interface FeatureFlagService {

    boolean isEnabled(String key, Long userId, Long campusId, String env);

    void refresh(String key);

    void refreshAll();
}
