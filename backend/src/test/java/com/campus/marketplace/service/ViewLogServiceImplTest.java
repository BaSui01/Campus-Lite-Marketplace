package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ViewLog;
import com.campus.marketplace.repository.ViewLogRepository;
import com.campus.marketplace.service.impl.ViewLogServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("浏览日志服务实现测试")
class ViewLogServiceImplTest {

    @Mock
    private ViewLogRepository viewLogRepository;

    @InjectMocks
    private ViewLogServiceImpl viewLogService;

    @Test
    @DisplayName("保存浏览日志成功")
    void saveAsync_persistsViewLog() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ViewLog> saved = new AtomicReference<>();

        doAnswer(invocation -> {
            saved.set(invocation.getArgument(0));
            latch.countDown();
            return null;
        }).when(viewLogRepository).save(any(ViewLog.class));

        long timestamp = Instant.parse("2025-01-01T08:30:00Z").toEpochMilli();
        viewLogService.saveAsync("alice", 42L, timestamp);

        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
        ViewLog log = saved.get();
        assertThat(log).isNotNull();
        assertThat(log.getUsername()).isEqualTo("alice");
        assertThat(log.getGoodsId()).isEqualTo(42L);
        assertThat(log.getViewedAt()).isEqualTo(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    @Test
    @DisplayName("保存浏览日志出现异常时不会抛出")
    void saveAsync_handlesRepositoryException() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        doAnswer(invocation -> {
            latch.countDown();
            throw new RuntimeException("boom");
        }).when(viewLogRepository).save(any(ViewLog.class));

        viewLogService.saveAsync("bob", 99L, System.currentTimeMillis());

        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
    }
}
