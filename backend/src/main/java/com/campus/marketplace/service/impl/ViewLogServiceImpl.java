package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.ViewLog;
import com.campus.marketplace.repository.ViewLogRepository;
import com.campus.marketplace.service.ViewLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

/**
 * View Log Service Impl
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewLogServiceImpl implements ViewLogService {

    private final ViewLogRepository viewLogRepository;

    @Override
    public void saveAsync(String username, Long goodsId, long timestampMillis) {
        CompletableFuture
                .runAsync(() -> {
                    LocalDateTime viewedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
                    ViewLog log = ViewLog.builder()
                            .username(username)
                            .goodsId(goodsId)
                            .viewedAt(viewedAt)
                            .build();
                    viewLogRepository.save(log);
                })
                .exceptionally(ex -> {
                    log.warn("保存浏览日志失败: username={}, goodsId={}", username, goodsId, ex);
                    return null;
                });
    }
}
