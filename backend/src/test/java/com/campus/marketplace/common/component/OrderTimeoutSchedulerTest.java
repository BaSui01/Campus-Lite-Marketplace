package com.campus.marketplace.common.component;

import com.campus.marketplace.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("OrderTimeoutScheduler 测试")
class OrderTimeoutSchedulerTest {

    private final OrderService orderService = mock(OrderService.class);
    private final RedissonClient redissonClient = mock(RedissonClient.class);
    private final RLock lock = mock(RLock.class);
    private final OrderTimeoutScheduler scheduler = new OrderTimeoutScheduler(orderService, redissonClient);

    @Test
    @DisplayName("获取到分布式锁时执行取消并解锁")
    void cancelTimeoutOrdersJob_lockAcquired() throws Exception {
        when(redissonClient.getLock("lock:order:cancel-timeout")).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(orderService.cancelTimeoutOrders()).thenReturn(3);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        scheduler.cancelTimeoutOrdersJob();

        verify(orderService).cancelTimeoutOrders();
        InOrder inOrder = inOrder(lock);
        inOrder.verify(lock).tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS));
        inOrder.verify(lock).unlock();
    }

    @Test
    @DisplayName("未获取锁时跳过任务")
    void cancelTimeoutOrdersJob_lockNotAcquired() throws Exception {
        when(redissonClient.getLock("lock:order:cancel-timeout")).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(false);
        when(lock.isHeldByCurrentThread()).thenReturn(false);

        scheduler.cancelTimeoutOrdersJob();

        verify(orderService, never()).cancelTimeoutOrders();
        verify(lock, never()).unlock();
    }

    @Test
    @DisplayName("执行过程中异常也要确保释放锁")
    void cancelTimeoutOrdersJob_exceptionReleasesLock() throws Exception {
        when(redissonClient.getLock("lock:order:cancel-timeout")).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(orderService.cancelTimeoutOrders()).thenThrow(new RuntimeException("db error"));
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        scheduler.cancelTimeoutOrdersJob();

        verify(lock).unlock();
    }
}
