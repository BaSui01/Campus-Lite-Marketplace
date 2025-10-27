package com.campus.marketplace.service;

public interface ViewLogService {
    void saveAsync(String username, Long goodsId, long timestampMillis);
}
