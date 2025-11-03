package com.campus.marketplace.scheduler;

import com.campus.marketplace.common.component.OrderAutomationScheduler;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.lock.DistributedLockManager;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 订单自动化调度器测试
 * 
 * 测试自动确认收货、异常订单检测等定时任务
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单自动化调度器测试")
class OrderAutomationSchedulerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DistributedLockManager lockManager;

    @Mock
    private DistributedLockManager.LockHandle lockHandle;

    private OrderAutomationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new OrderAutomationScheduler(
            orderRepository,
            notificationService,
            lockManager
        );
    }

    @Test
    @DisplayName("自动确认收货 - 应该将超过7天的已送达订单标记为已完成")
    void autoConfirmReceipt_ShouldCompleteDeliveredOrdersAfter7Days() {
        // Arrange
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7).minusHours(1);
        
        Order order1 = new Order();
        order1.setId(1L);
        order1.setOrderNo("ORD001");
        order1.setBuyerId(100L);
        order1.setSellerId(200L);
        order1.setStatus(OrderStatus.DELIVERED);
        order1.setAmount(BigDecimal.valueOf(100));
        order1.setActualAmount(BigDecimal.valueOf(100));
        order1.setUpdatedAt(sevenDaysAgo);
        
        Order order2 = new Order();
        order2.setId(2L);
        order2.setOrderNo("ORD002");
        order2.setBuyerId(101L);
        order2.setSellerId(201L);
        order2.setStatus(OrderStatus.DELIVERED);
        order2.setAmount(BigDecimal.valueOf(200));
        order2.setActualAmount(BigDecimal.valueOf(200));
        order2.setUpdatedAt(sevenDaysAgo);

        when(lockManager.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
            .thenReturn(lockHandle);
        when(lockHandle.acquired()).thenReturn(true);
        when(orderRepository.findByStatusAndUpdatedAtBefore(
            eq(OrderStatus.DELIVERED),
            any(LocalDateTime.class)
        )).thenReturn(List.of(order1, order2));

        // Act
        scheduler.autoConfirmReceiptJob();

        // Assert
        verify(orderRepository, times(1)).findByStatusAndUpdatedAtBefore(
            eq(OrderStatus.DELIVERED),
            any(LocalDateTime.class)
        );
        verify(orderRepository, times(1)).saveAll(anyList());
        verify(notificationService, times(4)).sendNotification(
            anyLong(),
            any(),
            anyString(),
            anyString(),
            anyLong(),
            anyString(),
            anyString()
        );
    }

    @Test
    @DisplayName("自动确认收货 - 无超时订单时不应执行任何操作")
    void autoConfirmReceipt_ShouldDoNothingWhenNoTimeoutOrders() {
        // Arrange
        when(lockManager.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
            .thenReturn(lockHandle);
        when(lockHandle.acquired()).thenReturn(true);
        when(orderRepository.findByStatusAndUpdatedAtBefore(
            eq(OrderStatus.DELIVERED),
            any(LocalDateTime.class)
        )).thenReturn(List.of());

        // Act
        scheduler.autoConfirmReceiptJob();

        // Assert
        verify(orderRepository, times(1)).findByStatusAndUpdatedAtBefore(
            eq(OrderStatus.DELIVERED),
            any(LocalDateTime.class)
        );
        verify(orderRepository, never()).saveAll(anyList());
        verify(notificationService, never()).sendNotification(
            anyLong(),
            any(),
            anyString(),
            anyString(),
            anyLong(),
            anyString(),
            anyString()
        );
    }

    @Test
    @DisplayName("异常订单检测 - 应该检测已支付但未发货的订单")
    void detectAbnormalOrders_ShouldDetectPaidButNotShipped() {
        // Arrange
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3).minusHours(1);
        
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD001");
        order.setBuyerId(100L);
        order.setSellerId(200L);
        order.setStatus(OrderStatus.PAID);
        order.setAmount(BigDecimal.valueOf(100));
        order.setActualAmount(BigDecimal.valueOf(100));
        order.setUpdatedAt(threeDaysAgo);

        when(lockManager.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
            .thenReturn(lockHandle);
        when(lockHandle.acquired()).thenReturn(true);
        // Mock 两次调用：先 PAID，再 SHIPPED
        when(orderRepository.findByStatusAndUpdatedAtBefore(
            eq(OrderStatus.PAID),
            any(LocalDateTime.class)
        )).thenReturn(List.of(order));
        when(orderRepository.findByStatusAndUpdatedAtBefore(
            eq(OrderStatus.SHIPPED),
            any(LocalDateTime.class)
        )).thenReturn(List.of());  // SHIPPED 状态无订单

        // Act
        scheduler.detectAbnormalOrdersJob();

        // Assert
        verify(orderRepository, times(1)).findByStatusAndUpdatedAtBefore(
            eq(OrderStatus.PAID),
            any(LocalDateTime.class)
        );
        verify(orderRepository, times(1)).findByStatusAndUpdatedAtBefore(
            eq(OrderStatus.SHIPPED),
            any(LocalDateTime.class)
        );
        verify(notificationService, times(1)).sendNotification(
            eq(200L),  // 卖家
            any(),
            eq("订单发货超时提醒"),
            anyString(),
            eq(1L),
            eq("ORDER"),
            anyString()
        );
    }

    @Test
    @DisplayName("异常订单检测 - 应该检测已发货但未送达的订单")
    void detectAbnormalOrders_ShouldDetectShippedButNotDelivered() {
        // Arrange
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7).minusHours(1);
        
        Order order = new Order();
        order.setId(2L);
        order.setOrderNo("ORD002");
        order.setBuyerId(101L);
        order.setSellerId(201L);
        order.setStatus(OrderStatus.SHIPPED);
        order.setAmount(BigDecimal.valueOf(200));
        order.setActualAmount(BigDecimal.valueOf(200));
        order.setUpdatedAt(sevenDaysAgo);

        when(lockManager.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
            .thenReturn(lockHandle);
        when(lockHandle.acquired()).thenReturn(true);
        // Mock 两次调用：先 PAID，再 SHIPPED
        when(orderRepository.findByStatusAndUpdatedAtBefore(
            eq(OrderStatus.PAID),
            any(LocalDateTime.class)
        )).thenReturn(List.of());  // PAID 状态无订单
        when(orderRepository.findByStatusAndUpdatedAtBefore(
            eq(OrderStatus.SHIPPED),
            any(LocalDateTime.class)
        )).thenReturn(List.of(order));

        // Act
        scheduler.detectAbnormalOrdersJob();

        // Assert
        verify(orderRepository, times(1)).findByStatusAndUpdatedAtBefore(
            eq(OrderStatus.PAID),
            any(LocalDateTime.class)
        );
        verify(orderRepository, times(1)).findByStatusAndUpdatedAtBefore(
            eq(OrderStatus.SHIPPED),
            any(LocalDateTime.class)
        );
        verify(notificationService, times(2)).sendNotification(
            anyLong(),
            any(),
            anyString(),
            anyString(),
            eq(2L),
            eq("ORDER"),
            anyString()
        );
    }

    @Test
    @DisplayName("获取锁失败时 - 应该跳过任务执行")
    void autoConfirmReceipt_ShouldSkipWhenLockFailed() {
        // Arrange
        when(lockManager.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
            .thenReturn(lockHandle);
        when(lockHandle.acquired()).thenReturn(false);

        // Act
        scheduler.autoConfirmReceiptJob();

        // Assert
        verify(orderRepository, never()).findByStatusAndUpdatedAtBefore(
            any(OrderStatus.class),
            any(LocalDateTime.class)
        );
        verify(notificationService, never()).sendNotification(
            anyLong(),
            any(),
            anyString(),
            anyString(),
            anyLong(),
            anyString(),
            anyString()
        );
    }
}
