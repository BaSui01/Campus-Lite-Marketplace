package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.ViewLog;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.repository.ViewLogRepository;
import com.campus.marketplace.service.ViewLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final UserRepository userRepository;

    @Override
    public void saveAsync(String username, Long goodsId, long timestampMillis) {
        CompletableFuture
                .runAsync(() -> {
                    try {
                        Long userId = null;
                        if (username != null) {
                            userId = userRepository.findByUsername(username)
                                    .map(user -> user.getId())
                                    .orElse(null);
                        }

                        ViewLog log = ViewLog.builder()
                                .userId(userId)
                                .goodsId(goodsId)
                                .build();
                        viewLogRepository.save(log);
                        log.debug("浏览日志保存成功: username={}, userId={}, goodsId={}", username, userId, goodsId);
                    } catch (Exception e) {
                        log.warn("保存浏览日志失败: username={}, goodsId={}", username, goodsId, e);
                    }
                })
                .exceptionally(ex -> {
                    log.warn("保存浏览日志异步任务失败: username={}, goodsId={}", username, goodsId, ex);
                    return null;
                });
    }
}
