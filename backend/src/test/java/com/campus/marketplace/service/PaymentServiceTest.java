package com.campus.marketplace.service;

import com.campus.marketplace.common.config.AlipayConfig;
import com.campus.marketplace.common.dto.response.PaymentResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.enums.PaymentMethod;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.service.impl.AlipayPaymentService;
import com.campus.marketplace.service.impl.PaymentServiceImpl;
import com.campus.marketplace.service.impl.WechatPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PaymentServiceImpl 路由与兜底行为（无 Mock 版）")
class PaymentServiceTest {

    private RecordingAlipayService alipayService;
    private TestObjectProvider<WechatPaymentService> wechatProvider;
    private PaymentService paymentService;
    private Order order;

    @BeforeEach
    void setUp() {
        alipayService = new RecordingAlipayService();
        wechatProvider = new TestObjectProvider<>();
        paymentService = new PaymentServiceImpl(alipayService, wechatProvider);

        order = Order.builder()
                .orderNo("ORD-10086")
                .buyerId(1L)
                .sellerId(2L)
                .amount(new BigDecimal("99.00"))
                .discountAmount(BigDecimal.ZERO)
                .actualAmount(new BigDecimal("99.00"))
                .status(OrderStatus.PENDING_PAYMENT)
                .build();
    }

    @Test
    @DisplayName("ALIPAY 分支应返回支付宝结果且不触发微信服务")
    void createPayment_alipay_branch() {
        PaymentResponse response = paymentService.createPayment(order, PaymentMethod.ALIPAY);

        assertThat(response.paymentUrl()).isEqualTo("ALIPAY:ORD-10086");
        assertThat(alipayService.createCalled).isTrue();
        assertThat(wechatProvider.getInvocationCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("WECHAT 服务缺失时抛出业务异常")
    void createPayment_wechat_missing() {
        assertThatThrownBy(() -> paymentService.createPayment(order, PaymentMethod.WECHAT))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("微信支付未启用");
    }

    @Nested
    @DisplayName("退款路由")
    class RefundRouting {

        @Test
        @DisplayName("ALIPAY 退款命中支付宝实现")
        void refund_alipay_branch() {
            boolean ok = paymentService.refund(order, BigDecimal.TEN, PaymentMethod.ALIPAY);

            assertThat(ok).isTrue();
            assertThat(alipayService.refundCalled).isTrue();
        }

        @Test
        @DisplayName("WECHAT 退款在服务缺失时返回 false")
        void refund_wechat_missing() {
            boolean ok = paymentService.refund(order, BigDecimal.ONE, PaymentMethod.WECHAT);

            assertThat(ok).isFalse();
        }

        @Test
        @DisplayName("WECHAT 退款命中微信实现")
        void refund_wechat_branch() {
            RecordingWechatService wechat = new RecordingWechatService(true);
            wechatProvider.setInstance(wechat);

            boolean ok = paymentService.refund(order, BigDecimal.ONE, PaymentMethod.WECHAT);

            assertThat(ok).isTrue();
            assertThat(wechat.refundCalled).isTrue();
        }
    }

    private static class RecordingAlipayService extends AlipayPaymentService {
        boolean createCalled;
        boolean refundCalled;

        RecordingAlipayService() {
            super(null, new AlipayConfig());
        }

        @Override
        public PaymentResponse createPayment(Order order) {
            createCalled = true;
            return PaymentResponse.builder()
                    .orderNo(order.getOrderNo())
                    .paymentUrl("ALIPAY:" + order.getOrderNo())
                    .expireSeconds(1800)
                    .build();
        }

        @Override
        public boolean refund(Order order, BigDecimal amount) {
            refundCalled = true;
            return true;
        }
    }

    private static class RecordingWechatService extends WechatPaymentService {
        boolean refundCalled;
        private final boolean refundResult;

        RecordingWechatService(boolean refundResult) {
            super(null, null, null, new ObjectMapper());
            this.refundResult = refundResult;
        }

        @Override
        public PaymentResponse createPayment(Order order) {
            return PaymentResponse.builder()
                    .orderNo(order.getOrderNo())
                    .paymentUrl("WECHAT:" + order.getOrderNo())
                    .expireSeconds(1800)
                    .build();
        }

        @Override
        public boolean refund(Order order, BigDecimal amount) {
            refundCalled = true;
            return refundResult;
        }
    }

    private static class TestObjectProvider<T> implements ObjectProvider<T> {
        private T instance;
        private int invocationCount;

        void setInstance(T instance) {
            this.instance = instance;
        }

        int getInvocationCount() {
            return invocationCount;
        }

        @Override
        public T getObject(Object... args) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T getIfAvailable() {
            invocationCount++;
            return instance;
        }

        @Override
        public T getIfUnique() {
            invocationCount++;
            return instance;
        }

        @Override
        public T getObject() {
            invocationCount++;
            if (instance == null) {
                throw new IllegalStateException("No bean available");
            }
            return instance;
        }

        @Override
        public Iterator<T> iterator() {
            return instance == null
                    ? Map.<T, T>of().values().iterator()
                    : java.util.Collections.singleton(instance).iterator();
        }
    }
}
