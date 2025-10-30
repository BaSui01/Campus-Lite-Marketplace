package com.campus.marketplace.service;
/**
 * View Log Service
 *
 * @author BaSui
 * @date 2025-10-29
 */


public interface ViewLogService {
    void saveAsync(String username, Long goodsId, long timestampMillis);
}
