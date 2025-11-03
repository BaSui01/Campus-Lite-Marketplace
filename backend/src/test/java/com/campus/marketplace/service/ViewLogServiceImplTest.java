package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.entity.ViewLog;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.repository.ViewLogRepository;
import com.campus.marketplace.service.impl.ViewLogServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("浏览日志服务实现测试")
class ViewLogServiceImplTest {

    @Mock
    private ViewLogRepository viewLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ViewLogServiceImpl viewLogService;

    @Test
    @DisplayName("保存浏览日志成功")
    void saveAsync_persistsViewLog() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ViewLog> saved = new AtomicReference<>();

        User mockUser = new User();
        mockUser.setId(100L);
        mockUser.setUsername("alice");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(mockUser));

        doAnswer(invocation -> {
            saved.set(invocation.getArgument(0));
            latch.countDown();
            return null;
        }).when(viewLogRepository).save(any(ViewLog.class));

        viewLogService.saveAsync("alice", 42L, System.currentTimeMillis());

        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
        ViewLog log = saved.get();
        assertThat(log).isNotNull();
        assertThat(log.getUserId()).isEqualTo(100L);
        assertThat(log.getGoodsId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("允许匿名浏览以及空商品 ID")
    void saveAsync_allowsNullUsernameAndGoodsId() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ViewLog> saved = new AtomicReference<>();

        doAnswer(invocation -> {
            saved.set(invocation.getArgument(0));
            latch.countDown();
            return null;
        }).when(viewLogRepository).save(any(ViewLog.class));

        viewLogService.saveAsync(null, null, 0L);

        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
        ViewLog log = saved.get();
        assertThat(log).isNotNull();
        assertThat(log.getUserId()).isNull();
        assertThat(log.getGoodsId()).isNull();
    }

    @Test
    @DisplayName("用户不存在时保存 userId=null")
    void saveAsync_savesNullUserIdWhenUserNotFound() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ViewLog> saved = new AtomicReference<>();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        doAnswer(invocation -> {
            saved.set(invocation.getArgument(0));
            latch.countDown();
            return null;
        }).when(viewLogRepository).save(any(ViewLog.class));

        viewLogService.saveAsync("nonexistent", 99L, System.currentTimeMillis());

        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
        ViewLog log = saved.get();
        assertThat(log).isNotNull();
        assertThat(log.getUserId()).isNull();
        assertThat(log.getGoodsId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("保存浏览日志出现异常时不会抛出")
    void saveAsync_handlesRepositoryException() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(new User()));

        doAnswer(invocation -> {
            latch.countDown();
            throw new RuntimeException("boom");
        }).when(viewLogRepository).save(any(ViewLog.class));

        viewLogService.saveAsync("bob", 99L, System.currentTimeMillis());

        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
    }
}
