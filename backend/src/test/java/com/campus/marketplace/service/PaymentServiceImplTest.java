package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.PaymentResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.enums.PaymentMethod;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.service.impl.AlipayPaymentService;
import com.campus.marketplace.service.impl.PaymentServiceImpl;
import com.campus.marketplace.service.impl.WechatPaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Payment Service Impl Test
 *
 * @author BaSui
 * @date 2025-10-29
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl 路由与分支测试")
class PaymentServiceImplTest {

    @Mock AlipayPaymentService alipayPaymentService;
    @Mock ObjectProvider<WechatPaymentService> wechatProvider;

    @InjectMocks
    PaymentServiceImpl paymentService;

    private Order newOrder() {
        Order o = Order.builder()
                .orderNo("O1")
                .amount(new BigDecimal("100"))
                .discountAmount(BigDecimal.ZERO)
                .actualAmount(new BigDecimal("100"))
                .status(OrderStatus.PENDING_PAYMENT)
                .build();
        o.setId(1L);
        return o;
    }

    @Test
    @DisplayName("ALIPAY 路由到 AlipayPaymentService.createPayment")
    void createPayment_alipay_routed() {
        Order o = newOrder();
        when(alipayPaymentService.createPayment(o)).thenReturn(PaymentResponse.builder().orderNo("O1").paymentUrl("html").build());

        PaymentResponse resp = paymentService.createPayment(o, PaymentMethod.ALIPAY);

        assertThat(resp.paymentUrl()).isEqualTo("html");
        verify(alipayPaymentService).createPayment(o);
        verifyNoInteractions(wechatProvider);
    }

    @Test
    @DisplayName("WECHAT 路由到 WechatPaymentService（存在时）")
    void createPayment_wechat_present() {
        Order o = newOrder();
        WechatPaymentService wechat = mock(WechatPaymentService.class);
        when(wechatProvider.getIfAvailable()).thenReturn(wechat);
        when(wechat.createPayment(o)).thenReturn(PaymentResponse.builder().orderNo("O1").paymentUrl("wx").build());

        PaymentResponse resp = paymentService.createPayment(o, PaymentMethod.WECHAT);

        assertThat(resp.paymentUrl()).isEqualTo("wx");
        verify(wechat).createPayment(o);
    }

    @Test
    @DisplayName("WECHAT 服务未启用时抛出 BusinessException")
    void createPayment_wechat_absent_throws() {
        Order o = newOrder();
        when(wechatProvider.getIfAvailable()).thenReturn(null);

        assertThatThrownBy(() -> paymentService.createPayment(o, PaymentMethod.WECHAT))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("微信支付未启用");
    }

    @Test
    @DisplayName("退款：ALIPAY 分支返回底层结果")
    void refund_alipay() {
        Order o = newOrder();
        when(alipayPaymentService.refund(o, new BigDecimal("1"))).thenReturn(true);

        boolean ok = paymentService.refund(o, new BigDecimal("1"), PaymentMethod.ALIPAY);
        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("退款：WECHAT 未启用返回 false；启用则返回底层结果")
    void refund_wechat() {
        Order o = newOrder();
        when(wechatProvider.getIfAvailable()).thenReturn(null);
        assertThat(paymentService.refund(o, BigDecimal.ONE, PaymentMethod.WECHAT)).isFalse();

        WechatPaymentService wechat = mock(WechatPaymentService.class);
        when(wechatProvider.getIfAvailable()).thenReturn(wechat);
        when(wechat.refund(o, BigDecimal.TEN)).thenReturn(true);
        assertThat(paymentService.refund(o, BigDecimal.TEN, PaymentMethod.WECHAT)).isTrue();
    }
}
