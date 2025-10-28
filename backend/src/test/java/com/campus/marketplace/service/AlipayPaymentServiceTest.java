package com.campus.marketplace.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.campus.marketplace.common.config.AlipayConfig;
import com.campus.marketplace.common.dto.response.PaymentResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.impl.AlipayPaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("支付宝支付服务实现测试")
class AlipayPaymentServiceTest {

    @Mock private AlipayClient alipayClient;
    @Mock private AlipayConfig alipayConfig;

    private AlipayPaymentService alipayPaymentService;

    private MockedStatic<AlipaySignature> signatureMock;

    @BeforeEach
    void setUp() {
        alipayPaymentService = new AlipayPaymentService(alipayClient, alipayConfig);
    }

    @AfterEach
    void tearDown() {
        if (signatureMock != null) {
            signatureMock.close();
            signatureMock = null;
        }
    }

    @Test
    @DisplayName("创建支付订单成功返回表单 HTML")
    void createPayment_success() throws Exception {
        when(alipayConfig.getNotifyUrl()).thenReturn("https://notify");
        when(alipayConfig.getReturnUrl()).thenReturn("https://return");

        AlipayTradePagePayResponse response = mock(AlipayTradePagePayResponse.class);
        when(response.isSuccess()).thenReturn(true);
        when(response.getBody()).thenReturn("<form id='pay'></form>");
        when(alipayClient.pageExecute(any(AlipayTradePagePayRequest.class))).thenReturn(response);

        Order order = Order.builder()
                .orderNo("ORD-ALI-1")
                .actualAmount(new BigDecimal("88.23"))
                .build();

        PaymentResponse paymentResponse = alipayPaymentService.createPayment(order);

        assertThat(paymentResponse.orderNo()).isEqualTo("ORD-ALI-1");
        assertThat(paymentResponse.paymentUrl()).contains("form");
        assertThat(paymentResponse.expireSeconds()).isEqualTo(30 * 60);

        ArgumentCaptor<AlipayTradePagePayRequest> captor = ArgumentCaptor.forClass(AlipayTradePagePayRequest.class);
        verify(alipayClient).pageExecute(captor.capture());
        AlipayTradePagePayRequest request = captor.getValue();
        AlipayTradePagePayModel model = (AlipayTradePagePayModel) request.getBizModel();
        assertThat(model.getOutTradeNo()).isEqualTo("ORD-ALI-1");
        assertThat(model.getTotalAmount()).isEqualTo("88.23");
        assertThat(model.getProductCode()).isEqualTo("FAST_INSTANT_TRADE_PAY");
        assertThat(model.getTimeoutExpress()).isEqualTo("30m");
        assertThat(request.getNotifyUrl()).isEqualTo("https://notify");
        assertThat(request.getReturnUrl()).isEqualTo("https://return");
    }

    @Test
    @DisplayName("创建支付订单响应失败抛业务异常")
    void createPayment_failedResponse() throws Exception {
        AlipayTradePagePayResponse response = mock(AlipayTradePagePayResponse.class);
        when(response.isSuccess()).thenReturn(false);
        when(response.getCode()).thenReturn("40004");
        when(response.getMsg()).thenReturn("Business Failed");
        when(response.getSubCode()).thenReturn("ACQ.SYSTEM_ERROR");
        when(response.getSubMsg()).thenReturn("系统错误");
        when(alipayClient.pageExecute(any(AlipayTradePagePayRequest.class))).thenReturn(response);

        Order order = Order.builder().orderNo("ORD-FAIL").actualAmount(BigDecimal.ONE).build();

        assertThatThrownBy(() -> alipayPaymentService.createPayment(order))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PAYMENT_CREATE_FAILED.getCode())
                .hasMessageContaining("系统错误");
    }

    @Test
    @DisplayName("创建支付订单遭遇 API 异常抛业务异常")
    void createPayment_apiException() throws Exception {
        when(alipayClient.pageExecute(any(AlipayTradePagePayRequest.class)))
                .thenThrow(new AlipayApiException("boom"));

        Order order = Order.builder().orderNo("ORD-ERR").actualAmount(BigDecimal.TEN).build();

        assertThatThrownBy(() -> alipayPaymentService.createPayment(order))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PAYMENT_CREATE_FAILED.getCode())
                .hasMessageContaining("支付宝API调用异常");
    }

    @Test
    @DisplayName("签名验证通过返回 true")
    void verifySignature_true() throws Exception {
        signatureMock = mockStatic(AlipaySignature.class);
        signatureMock.when(() -> AlipaySignature.rsaCheckV1(anyMap(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        when(alipayConfig.getAlipayPublicKey()).thenReturn("pub");
        when(alipayConfig.getCharset()).thenReturn("UTF-8");
        when(alipayConfig.getSignType()).thenReturn("RSA2");

        boolean result = alipayPaymentService.verifySignature(Map.of("key", "val"));

        assertThat(result).isTrue();
        signatureMock.verify(() -> AlipaySignature.rsaCheckV1(anyMap(), eq("pub"), eq("UTF-8"), eq("RSA2")));
    }

    @Test
    @DisplayName("签名验证失败返回 false")
    void verifySignature_false() throws Exception {
        signatureMock = mockStatic(AlipaySignature.class);
        signatureMock.when(() -> AlipaySignature.rsaCheckV1(anyMap(), anyString(), anyString(), anyString()))
                .thenReturn(false);

        when(alipayConfig.getAlipayPublicKey()).thenReturn("pub");
        when(alipayConfig.getCharset()).thenReturn("UTF-8");
        when(alipayConfig.getSignType()).thenReturn("RSA2");

        assertThat(alipayPaymentService.verifySignature(Map.of())).isFalse();
    }

    @Test
    @DisplayName("签名验证异常返回 false")
    void verifySignature_exception() throws Exception {
        signatureMock = mockStatic(AlipaySignature.class);
        signatureMock.when(() -> AlipaySignature.rsaCheckV1(anyMap(), anyString(), anyString(), anyString()))
                .thenThrow(new AlipayApiException("sign error"));

        when(alipayConfig.getAlipayPublicKey()).thenReturn("pub");
        when(alipayConfig.getCharset()).thenReturn("UTF-8");
        when(alipayConfig.getSignType()).thenReturn("RSA2");

        assertThat(alipayPaymentService.verifySignature(Map.of())).isFalse();
    }

    @Test
    @DisplayName("处理回调支付成功返回订单数组")
    void handleNotify_success() {
        Map<String, String> params = Map.of(
                "trade_status", "TRADE_SUCCESS",
                "out_trade_no", "ORD-1",
                "trade_no", "ALI-123",
                "total_amount", "100.00"
        );

        String[] result = alipayPaymentService.handleNotify(params);

        assertThat(result).containsExactly("ORD-1", "ALI-123");
    }

    @Test
    @DisplayName("处理回调状态异常返回 null")
    void handleNotify_notSuccess() {
        Map<String, String> params = Map.of(
                "trade_status", "WAIT_BUYER_PAY",
                "out_trade_no", "ORD-2",
                "trade_no", "ALI-456"
        );

        assertThat(alipayPaymentService.handleNotify(params)).isNull();
    }

    @Test
    @DisplayName("退款成功返回 true")
    void refund_success() throws Exception {
        AlipayTradeRefundResponse response = mock(AlipayTradeRefundResponse.class);
        when(response.isSuccess()).thenReturn(true);
        when(alipayClient.execute(any(AlipayTradeRefundRequest.class))).thenReturn(response);

        Order order = Order.builder().orderNo("ORD-RF-1").build();

        boolean result = alipayPaymentService.refund(order, new BigDecimal("20.50"));

        assertThat(result).isTrue();

        ArgumentCaptor<AlipayTradeRefundRequest> captor = ArgumentCaptor.forClass(AlipayTradeRefundRequest.class);
        verify(alipayClient).execute(captor.capture());
        AlipayTradeRefundModel model = (AlipayTradeRefundModel) captor.getValue().getBizModel();
        assertThat(model.getOutTradeNo()).isEqualTo("ORD-RF-1");
        assertThat(model.getRefundAmount()).isEqualTo("20.50");
        assertThat(model.getRefundReason()).contains("Campus");
    }

    @Test
    @DisplayName("退款失败返回 false")
    void refund_failedResponse() throws Exception {
        AlipayTradeRefundResponse response = mock(AlipayTradeRefundResponse.class);
        when(response.isSuccess()).thenReturn(false);
        when(response.getCode()).thenReturn("40004");
        when(response.getSubMsg()).thenReturn("biz error");
        when(alipayClient.execute(any(AlipayTradeRefundRequest.class))).thenReturn(response);

        Order order = Order.builder().orderNo("ORD-RF-2").build();

        assertThat(alipayPaymentService.refund(order, BigDecimal.ONE)).isFalse();
    }

    @Test
    @DisplayName("退款异常返回 false")
    void refund_exception() throws Exception {
        when(alipayClient.execute(any(AlipayTradeRefundRequest.class)))
                .thenThrow(new AlipayApiException("refund error"));

        Order order = Order.builder().orderNo("ORD-RF-3").build();

        assertThat(alipayPaymentService.refund(order, BigDecimal.TEN)).isFalse();
    }
}
