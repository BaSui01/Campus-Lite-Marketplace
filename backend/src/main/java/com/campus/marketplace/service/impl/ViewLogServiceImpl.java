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

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewLogServiceImpl implements ViewLogService {

    private final ViewLogRepository viewLogRepository;

    @Override
    public void saveAsync(String username, Long goodsId, long timestampMillis) {
        Thread.ofVirtual().start(() -> {
            try {
                LocalDateTime viewedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
                ViewLog log = ViewLog.builder()
                        .username(username)
                        .goodsId(goodsId)
                        .viewedAt(viewedAt)
                        .build();
                viewLogRepository.save(log);
            } catch (Exception e) {
                log.warn("保存浏览日志失败: username={}, goodsId={}, err={}", username, goodsId, e.getMessage());
            }
        });
    }
}
