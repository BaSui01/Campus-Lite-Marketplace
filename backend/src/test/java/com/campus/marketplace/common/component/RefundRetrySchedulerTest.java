package com.campus.marketplace.common.component;

import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.RefundRequest;
import com.campus.marketplace.common.enums.PaymentMethod;
import com.campus.marketplace.common.enums.RefundStatus;
import com.campus.marketplace.common.lock.DistributedLockManager;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.RefundRequestRepository;
import com.campus.marketplace.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundRetryScheduler 测试")
class RefundRetrySchedulerTest {

    @Mock
    private RefundRequestRepository refundRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private DistributedLockManager lockManager;
    @Mock
    private DistributedLockManager.LockHandle lockHandle;

    private RefundRetryScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new RefundRetryScheduler(refundRepository, orderRepository, paymentService, lockManager);
        ReflectionTestUtils.setField(scheduler, "maxRetry", 5);
        ReflectionTestUtils.setField(scheduler, "backoffMinutes", 10);
        when(lockManager.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(lockHandle);
    }

    private RefundRequest refundRequest(String orderNo) {
        RefundRequest request = RefundRequest.builder()
                .refundNo("R-" + orderNo)
                .orderNo(orderNo)
                .status(RefundStatus.FAILED)
                .amount(new BigDecimal("12.34"))
                .retryCount(0)
                .build();
        request.setId(1L);
        return request;
    }

    private Order order(String orderNo, PaymentMethod method) {
        Order order = Order.builder()
                .orderNo(orderNo)
                .paymentMethod(method.name())
                .actualAmount(new BigDecimal("12.34"))
                .build();
        order.setId(100L);
        return order;
    }

    @Test
    @DisplayName("获取到锁且退款成功时更新状态为已退款")
    void retryFailedRefunds_successful() throws Exception {
        RefundRequest request = refundRequest("ORD-1");
        when(refundRepository.findRetryCandidates(eq(5), any(LocalDateTime.class)))
                .thenReturn(List.of(request));
        when(orderRepository.findByOrderNo("ORD-1")).thenReturn(Optional.of(order("ORD-1", PaymentMethod.ALIPAY)));
        when(paymentService.refund(any(Order.class), any(), any())).thenReturn(true);
        when(lockHandle.acquired()).thenReturn(true);

        scheduler.retryFailedRefunds();

        assertThat(request.getStatus()).isEqualTo(RefundStatus.REFUNDED);
        assertThat(request.getLastError()).isNull();
        verify(refundRepository, times(1)).save(request);
        verify(lockHandle).close();
    }

    @Test
    @DisplayName("渠道返回失败时累计重试次数并保持失败状态")
    void retryFailedRefunds_channelFailure() throws Exception {
        RefundRequest request = refundRequest("ORD-2");
        when(refundRepository.findRetryCandidates(eq(5), any(LocalDateTime.class)))
                .thenReturn(List.of(request));
        when(orderRepository.findByOrderNo("ORD-2")).thenReturn(Optional.of(order("ORD-2", PaymentMethod.WECHAT)));
        when(paymentService.refund(any(Order.class), any(), any())).thenReturn(false);
        when(lockHandle.acquired()).thenReturn(true);

        scheduler.retryFailedRefunds();

        assertThat(request.getStatus()).isEqualTo(RefundStatus.FAILED);
        assertThat(request.getRetryCount()).isEqualTo(1);
        assertThat(request.getLastError()).isEqualTo("channel refund failed");
        verify(refundRepository, times(1)).save(request);
    }

    @Test
    @DisplayName("退款过程异常时记录错误并继续")
    void retryFailedRefunds_exception() throws Exception {
        RefundRequest request = refundRequest("ORD-3");
        when(refundRepository.findRetryCandidates(eq(5), any(LocalDateTime.class)))
                .thenReturn(List.of(request));
        when(orderRepository.findByOrderNo("ORD-3")).thenReturn(Optional.of(order("ORD-3", PaymentMethod.ALIPAY)));
        when(paymentService.refund(any(Order.class), any(), any())).thenThrow(new IllegalStateException("gateway down"));
        when(lockHandle.acquired()).thenReturn(true);

        scheduler.retryFailedRefunds();

        assertThat(request.getStatus()).isEqualTo(RefundStatus.FAILED);
        assertThat(request.getRetryCount()).isEqualTo(1);
        assertThat(request.getLastError()).isEqualTo("gateway down");
        verify(refundRepository, times(1)).save(request);
    }

    @Test
    @DisplayName("未获取锁时直接返回")
    void retryFailedRefunds_lockUnavailable() throws Exception {
        when(lockHandle.acquired()).thenReturn(false);

        scheduler.retryFailedRefunds();

        verify(refundRepository, never()).findRetryCandidates(anyInt(), any(LocalDateTime.class));
    }
}
