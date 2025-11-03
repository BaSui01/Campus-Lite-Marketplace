package com.campus.marketplace.scheduler;

import com.campus.marketplace.common.component.DisputeTimeoutScheduler;
import com.campus.marketplace.common.lock.DistributedLockManager;
import com.campus.marketplace.service.DisputeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 纠纷超时定时任务测试
 *
 * 测试协商超时和仲裁超时的定时检查任务
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("纠纷超时定时任务测试")
class DisputeTimeoutSchedulerTest {

    @Mock
    private DisputeService disputeService;

    @Mock
    private DistributedLockManager lockManager;

    @Mock
    private DistributedLockManager.LockHandle lockHandle;

    @InjectMocks
    private DisputeTimeoutScheduler scheduler;

    @BeforeEach
    void setUp() {
        // 默认锁获取成功
        when(lockHandle.acquired()).thenReturn(true);
        when(lockManager.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(lockHandle);
    }

    /**
     * 测试1：检查过期协商 - 应该成功执行
     */
    @Test
    @DisplayName("检查过期协商 - 应该成功标记并释放锁")
    void checkExpiredNegotiations_ShouldMarkExpiredAndReleaseLock() throws Exception {
        // 模拟标记了2个过期协商
        when(disputeService.markExpiredNegotiations()).thenReturn(2);

        // 执行定时任务
        scheduler.checkExpiredNegotiations();

        // 验证：获取了分布式锁
        verify(lockManager).tryLock(
                eq("lock:dispute:check-expired-negotiations"),
                eq(1L),
                eq(30L),
                eq(TimeUnit.SECONDS)
        );

        // 验证：调用了标记过期协商方法
        verify(disputeService).markExpiredNegotiations();

        // 验证：释放了锁
        verify(lockHandle).close();
    }

    /**
     * 测试2：检查过期仲裁 - 应该成功执行
     */
    @Test
    @DisplayName("检查过期仲裁 - 应该成功标记并释放锁")
    void checkExpiredArbitrations_ShouldMarkExpiredAndReleaseLock() throws Exception {
        // 模拟标记了3个过期仲裁
        when(disputeService.markExpiredArbitrations()).thenReturn(3);

        // 执行定时任务
        scheduler.checkExpiredArbitrations();

        // 验证：获取了分布式锁
        verify(lockManager).tryLock(
                eq("lock:dispute:check-expired-arbitrations"),
                eq(1L),
                eq(30L),
                eq(TimeUnit.SECONDS)
        );

        // 验证：调用了标记过期仲裁方法
        verify(disputeService).markExpiredArbitrations();

        // 验证：释放了锁
        verify(lockHandle).close();
    }

    /**
     * 测试3：锁获取失败 - 应该跳过执行
     */
    @Test
    @DisplayName("协商检查锁获取失败 - 应该跳过本轮任务")
    void checkExpiredNegotiations_WhenLockFailed_ShouldSkip() throws Exception {
        // 模拟锁获取失败
        when(lockHandle.acquired()).thenReturn(false);

        // 执行定时任务
        scheduler.checkExpiredNegotiations();

        // 验证：获取了锁
        verify(lockManager).tryLock(
                eq("lock:dispute:check-expired-negotiations"),
                eq(1L),
                eq(30L),
                eq(TimeUnit.SECONDS)
        );

        // 验证：没有调用标记方法（因为锁获取失败）
        verify(disputeService, never()).markExpiredNegotiations();

        // 验证：仍然释放了锁（try-with-resources）
        verify(lockHandle).close();
    }

    /**
     * 测试4：仲裁检查锁获取失败 - 应该跳过执行
     */
    @Test
    @DisplayName("仲裁检查锁获取失败 - 应该跳过本轮任务")
    void checkExpiredArbitrations_WhenLockFailed_ShouldSkip() throws Exception {
        // 模拟锁获取失败
        when(lockHandle.acquired()).thenReturn(false);

        // 执行定时任务
        scheduler.checkExpiredArbitrations();

        // 验证：获取了锁
        verify(lockManager).tryLock(
                eq("lock:dispute:check-expired-arbitrations"),
                eq(1L),
                eq(30L),
                eq(TimeUnit.SECONDS)
        );

        // 验证：没有调用标记方法
        verify(disputeService, never()).markExpiredArbitrations();

        // 验证：释放了锁
        verify(lockHandle).close();
    }

    /**
     * 测试5：执行过程异常 - 应该捕获异常并记录日志
     */
    @Test
    @DisplayName("协商检查执行异常 - 应该捕获异常不影响下次执行")
    void checkExpiredNegotiations_WhenServiceThrows_ShouldCatchException() throws Exception {
        // 模拟服务层抛出异常
        when(disputeService.markExpiredNegotiations())
                .thenThrow(new RuntimeException("数据库连接失败"));

        // 执行定时任务（不应该抛出异常）
        scheduler.checkExpiredNegotiations();

        // 验证：调用了标记方法
        verify(disputeService).markExpiredNegotiations();

        // 验证：释放了锁
        verify(lockHandle).close();
    }

    /**
     * 测试6：仲裁检查执行异常 - 应该捕获异常
     */
    @Test
    @DisplayName("仲裁检查执行异常 - 应该捕获异常不影响下次执行")
    void checkExpiredArbitrations_WhenServiceThrows_ShouldCatchException() throws Exception {
        // 模拟服务层抛出异常
        when(disputeService.markExpiredArbitrations())
                .thenThrow(new RuntimeException("Redis连接超时"));

        // 执行定时任务
        scheduler.checkExpiredArbitrations();

        // 验证：调用了标记方法
        verify(disputeService).markExpiredArbitrations();

        // 验证：释放了锁
        verify(lockHandle).close();
    }

    /**
     * 测试7：协商检查标记为0 - 应该正常记录日志
     */
    @Test
    @DisplayName("协商检查无过期数据 - 应该正常完成")
    void checkExpiredNegotiations_WhenNoExpired_ShouldComplete() throws Exception {
        // 模拟没有过期数据
        when(disputeService.markExpiredNegotiations()).thenReturn(0);

        // 执行定时任务
        scheduler.checkExpiredNegotiations();

        // 验证：调用了标记方法
        verify(disputeService).markExpiredNegotiations();

        // 验证：释放了锁
        verify(lockHandle).close();
    }

    /**
     * 测试8：仲裁检查标记为0 - 应该正常记录日志
     */
    @Test
    @DisplayName("仲裁检查无过期数据 - 应该正常完成")
    void checkExpiredArbitrations_WhenNoExpired_ShouldComplete() throws Exception {
        // 模拟没有过期数据
        when(disputeService.markExpiredArbitrations()).thenReturn(0);

        // 执行定时任务
        scheduler.checkExpiredArbitrations();

        // 验证：调用了标记方法
        verify(disputeService).markExpiredArbitrations();

        // 验证：释放了锁
        verify(lockHandle).close();
    }
}
