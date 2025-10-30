package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.PayOrderRequest;
import com.campus.marketplace.common.dto.request.PaymentCallbackRequest;
import com.campus.marketplace.common.dto.response.PaymentResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.enums.PaymentMethod;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Order Service Pay And Callback Test
 *
 * @author BaSui
 * @date 2025-10-29
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务：支付与回调分支测试")
class OrderServicePayAndCallbackTest {

    @Mock private OrderRepository orderRepository;
    @Mock private com.campus.marketplace.repository.GoodsRepository goodsRepository;
    @Mock private UserRepository userRepository;
    @Mock private PaymentService paymentService;
    @Mock private com.campus.marketplace.repository.ReviewRepository reviewRepository;
    @Mock private com.campus.marketplace.common.utils.SensitiveWordFilter sensitiveWordFilter;
    @Mock private NotificationService notificationService;
    @Mock private com.campus.marketplace.common.component.NotificationDispatcher notificationDispatcher;
    @Mock private AuditLogService auditLogService;
    @Mock private com.campus.marketplace.repository.CouponUserRelationRepository couponUserRelationRepository;
    @Mock private com.campus.marketplace.service.CouponService couponService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private MockedStatic<com.campus.marketplace.common.utils.SecurityUtil> sec;

    private com.campus.marketplace.common.entity.User buyer;

    @BeforeEach
    void init() {
        sec = mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class);
        sec.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername).thenReturn("buyer");

        buyer = com.campus.marketplace.common.entity.User.builder()
                .username("buyer").email("b@e.com").build();
        buyer.setId(1L);
        lenient().when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
    }

    @AfterEach
    void cleanup() {
        if (sec != null) sec.close();
    }

    private Order newPendingOrder() {
        Order o = Order.builder()
                .orderNo("O1")
                .buyerId(1L)
                .sellerId(2L)
                .amount(new BigDecimal("100"))
                .discountAmount(BigDecimal.ZERO)
                .actualAmount(new BigDecimal("100"))
                .status(OrderStatus.PENDING_PAYMENT)
                .build();
        o.setId(10L);
        return o;
    }

    @Test
    @DisplayName("payOrder 成功：调用支付门面并写入支付方式")
    void payOrder_success() {
        Order order = newPendingOrder();
        when(orderRepository.findByOrderNo("O1")).thenReturn(Optional.of(order));
        when(paymentService.createPayment(eq(order), eq(PaymentMethod.ALIPAY)))
                .thenReturn(PaymentResponse.builder().orderNo("O1").paymentUrl("/pay").build());

        PaymentResponse resp = orderService.payOrder(new PayOrderRequest("O1", PaymentMethod.ALIPAY));

        assertThat(resp.paymentUrl()).isEqualTo("/pay");
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getPaymentMethod()).isEqualTo("ALIPAY");
    }

    @Test
    @DisplayName("payOrder 失败：非买家")
    void payOrder_notBuyer() {
        Order order = newPendingOrder();
        order.setBuyerId(999L);
        when(orderRepository.findByOrderNo("O1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.payOrder(new PayOrderRequest("O1", PaymentMethod.ALIPAY)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PERMISSION_DENIED.getCode());
    }

    @Test
    @DisplayName("payOrder 失败：状态为 PAID")
    void payOrder_paid() {
        Order order = newPendingOrder();
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findByOrderNo("O1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.payOrder(new PayOrderRequest("O1", PaymentMethod.ALIPAY)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ORDER_PAID.getCode());
    }

    @Test
    @DisplayName("handlePaymentCallback 失败：签名无效")
    void callback_invalid_signature() {
        Order order = newPendingOrder();
        when(orderRepository.findByOrderNo("O1")).thenReturn(Optional.of(order));

        PaymentCallbackRequest req = new PaymentCallbackRequest("O1", "T1", new BigDecimal("100"), "SUCCESS", "sig");
        assertThatThrownBy(() -> orderService.handlePaymentCallback(req, false))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PAYMENT_FAILED.getCode());
    }

    @Test
    @DisplayName("handlePaymentCallback 失败：金额不匹配")
    void callback_amount_mismatch() {
        Order order = newPendingOrder();
        when(orderRepository.findByOrderNo("O1")).thenReturn(Optional.of(order));

        PaymentCallbackRequest req = new PaymentCallbackRequest("O1", "T1", new BigDecimal("101"), "SUCCESS", "sig");
        assertThatThrownBy(() -> orderService.handlePaymentCallback(req, true))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PAYMENT_FAILED.getCode());
    }

    @Test
    @DisplayName("handlePaymentCallback 返回 false：订单非待支付")
    void callback_not_pending() {
        Order order = newPendingOrder();
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findByOrderNo("O1")).thenReturn(Optional.of(order));

        PaymentCallbackRequest req = new PaymentCallbackRequest("O1", "T1", new BigDecimal("100"), "SUCCESS", "sig");
        assertThat(orderService.handlePaymentCallback(req, true)).isFalse();
    }

    @Test
    @DisplayName("handlePaymentCallback 返回 false：支付状态不是 SUCCESS")
    void callback_status_fail() {
        Order order = newPendingOrder();
        when(orderRepository.findByOrderNo("O1")).thenReturn(Optional.of(order));

        PaymentCallbackRequest req = new PaymentCallbackRequest("O1", "T1", new BigDecimal("100"), "FAIL", "sig");
        assertThat(orderService.handlePaymentCallback(req, true)).isFalse();
    }

    @Test
    @DisplayName("handlePaymentCallback 成功：状态更新为 PAID")
    void callback_success() {
        Order order = newPendingOrder();
        when(orderRepository.findByOrderNo("O1")).thenReturn(Optional.of(order));

        PaymentCallbackRequest req = new PaymentCallbackRequest("O1", "T1", new BigDecimal("100"), "SUCCESS", "sig");
        boolean ok = orderService.handlePaymentCallback(req, true);
        assertThat(ok).isTrue();
        verify(orderRepository, atLeastOnce()).save(argThat(o -> o.getStatus() == OrderStatus.PAID));
    }
}
