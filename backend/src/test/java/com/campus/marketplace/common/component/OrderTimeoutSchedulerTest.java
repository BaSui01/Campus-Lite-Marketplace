package com.campus.marketplace.common.component;

import com.campus.marketplace.common.lock.DistributedLockManager;
import com.campus.marketplace.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("OrderTimeoutScheduler 测试")
class OrderTimeoutSchedulerTest {

    private final OrderService orderService = mock(OrderService.class);
    private final DistributedLockManager lockManager = mock(DistributedLockManager.class);
    private final DistributedLockManager.LockHandle lockHandle = mock(DistributedLockManager.LockHandle.class);
    private final OrderTimeoutScheduler scheduler = new OrderTimeoutScheduler(orderService, lockManager);

    @Test
    @DisplayName("获取到分布式锁时执行取消并解锁")
    void cancelTimeoutOrdersJob_lockAcquired() {
        when(lockManager.tryLock(eq("lock:order:cancel-timeout"), anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(lockHandle);
        when(lockHandle.acquired()).thenReturn(true);
        when(orderService.cancelTimeoutOrders()).thenReturn(3);

        scheduler.cancelTimeoutOrdersJob();

        verify(orderService).cancelTimeoutOrders();
        InOrder inOrder = inOrder(lockManager, lockHandle);
        inOrder.verify(lockManager).tryLock(eq("lock:order:cancel-timeout"), anyLong(), anyLong(), eq(TimeUnit.SECONDS));
        inOrder.verify(lockHandle).close();
    }

    @Test
    @DisplayName("未获取锁时跳过任务")
    void cancelTimeoutOrdersJob_lockNotAcquired() {
        when(lockManager.tryLock(eq("lock:order:cancel-timeout"), anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(lockHandle);
        when(lockHandle.acquired()).thenReturn(false);

        scheduler.cancelTimeoutOrdersJob();

        verify(orderService, never()).cancelTimeoutOrders();
        verify(lockHandle).close();
    }

    @Test
    @DisplayName("执行过程中异常也要确保释放锁")
    void cancelTimeoutOrdersJob_exceptionReleasesLock() {
        when(lockManager.tryLock(eq("lock:order:cancel-timeout"), anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(lockHandle);
        when(lockHandle.acquired()).thenReturn(true);
        when(orderService.cancelTimeoutOrders()).thenThrow(new RuntimeException("db error"));

        scheduler.cancelTimeoutOrdersJob();

        verify(lockHandle).close();
    }
}
