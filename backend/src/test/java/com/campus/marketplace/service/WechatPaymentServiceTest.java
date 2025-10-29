package com.campus.marketplace.service;

import com.campus.marketplace.common.config.WechatPayConfig;
import com.campus.marketplace.common.dto.response.PaymentResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.impl.WechatPaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Wechat Payment Service Test
 *
 * @author BaSui
 * @date 2025-10-29
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("微信支付V3服务实现测试")
class WechatPaymentServiceTest {

    @Mock private NativePayService nativePayService;
    @Mock private Config wechatPayV3Config;
    @Mock private WechatPayConfig wechatPayConfig;

    private ObjectMapper objectMapper;
    private WechatPaymentService wechatPaymentService;

    private static class DummyJsonProcessingException extends JsonProcessingException {
        DummyJsonProcessingException() { super("err"); }
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        wechatPaymentService = new WechatPaymentService(nativePayService, wechatPayV3Config, wechatPayConfig, objectMapper);
    }

    @Test
    @DisplayName("创建支付订单成功返回二维码")
    void createPayment_success() {
        when(wechatPayConfig.getAppId()).thenReturn("wx-app");
        when(wechatPayConfig.getMchId()).thenReturn("1900000");
        when(wechatPayConfig.getNotifyUrl()).thenReturn("https://notify");

        PrepayResponse prepayResponse = new PrepayResponse();
        prepayResponse.setCodeUrl("weixin://qr/abc");
        when(nativePayService.prepay(any())).thenReturn(prepayResponse);

        Order order = Order.builder()
                .orderNo("ORD-001")
                .actualAmount(new BigDecimal("10.50"))
                .build();

        PaymentResponse response = wechatPaymentService.createPayment(order);

        assertThat(response.orderNo()).isEqualTo("ORD-001");
        assertThat(response.paymentUrl()).isEqualTo("weixin://qr/abc");
        assertThat(response.expireSeconds()).isEqualTo(30 * 60);

        ArgumentCaptor<PrepayRequest> captor = ArgumentCaptor.forClass(PrepayRequest.class);
        verify(nativePayService).prepay(captor.capture());
        PrepayRequest request = captor.getValue();
        assertThat(request.getAppid()).isEqualTo("wx-app");
        assertThat(request.getMchid()).isEqualTo("1900000");
        assertThat(request.getAmount().getTotal()).isEqualTo(1050);
        assertThat(request.getNotifyUrl()).isEqualTo("https://notify");
        assertThat(request.getTimeExpire()).isNotBlank();
    }

    @Test
    @DisplayName("创建支付订单遇到 ServiceException 抛业务异常")
    void createPayment_serviceException() {
        when(wechatPayConfig.getAppId()).thenReturn("wx-app");
        when(wechatPayConfig.getMchId()).thenReturn("1900000");
        when(wechatPayConfig.getNotifyUrl()).thenReturn("https://notify");

        ServiceException serviceException = mock(ServiceException.class);
        when(serviceException.getHttpStatusCode()).thenReturn(400);
        when(serviceException.getErrorCode()).thenReturn("INVALID_REQUEST");
        when(serviceException.getErrorMessage()).thenReturn("invalid");
        when(nativePayService.prepay(any())).thenThrow(serviceException);

        Order order = Order.builder()
                .orderNo("ORD-FAIL")
                .actualAmount(BigDecimal.ONE)
                .build();

        assertThatThrownBy(() -> wechatPaymentService.createPayment(order))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PAYMENT_CREATE_FAILED.getCode())
                .hasMessageContaining("invalid");
    }

    @Test
    @DisplayName("创建支付订单遇到未知异常走通用兜底")
    void createPayment_unexpectedException() {
        when(wechatPayConfig.getAppId()).thenReturn("wx-app");
        when(wechatPayConfig.getMchId()).thenReturn("1900000");
        when(wechatPayConfig.getNotifyUrl()).thenReturn("https://notify");
        when(nativePayService.prepay(any())).thenThrow(new IllegalStateException("boom"));

        Order order = Order.builder()
                .orderNo("ORD-FAIL2")
                .actualAmount(BigDecimal.ONE)
                .build();

        assertThatThrownBy(() -> wechatPaymentService.createPayment(order))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PAYMENT_CREATE_FAILED.getCode())
                .hasMessageContaining("微信支付创建失败");
    }

    @Test
    @DisplayName("订单金额为负抛业务异常")
    void createPayment_negativeAmount() {
        Order order = Order.builder()
                .orderNo("ORD-NEG")
                .actualAmount(new BigDecimal("-1.00"))
                .build();

        assertThatThrownBy(() -> wechatPaymentService.createPayment(order))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PAYMENT_CREATE_FAILED.getCode())
                .hasMessage("微信支付创建失败");

        verifyNoInteractions(nativePayService);
    }

    @Test
    @DisplayName("订单金额过大抛范围异常")
    void createPayment_amountOverflow() {
        Order order = Order.builder()
                .orderNo("ORD-BIG")
                .actualAmount(new BigDecimal("1000000000"))
                .build();

        assertThatThrownBy(() -> wechatPaymentService.createPayment(order))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PAYMENT_CREATE_FAILED.getCode())
                .hasMessage("微信支付创建失败");

        verifyNoInteractions(nativePayService);
    }

    @Test
    @DisplayName("查询订单状态成功返回交易枚举名称")
    void queryOrderStatus_success() {
        when(wechatPayConfig.getMchId()).thenReturn("1900000");
        Transaction transaction = new Transaction();
        transaction.setTradeState(Transaction.TradeStateEnum.SUCCESS);
        when(nativePayService.queryOrderByOutTradeNo(any())).thenReturn(transaction);

        String status = wechatPaymentService.queryOrderStatus("ORD-OK");

        assertThat(status).isEqualTo("SUCCESS");
        verify(nativePayService).queryOrderByOutTradeNo(any());
    }

    @Test
    @DisplayName("查询订单状态 ServiceException 转换为业务异常")
    void queryOrderStatus_serviceException() {
        when(wechatPayConfig.getMchId()).thenReturn("1900000");
        ServiceException serviceException = mock(ServiceException.class);
        when(serviceException.getErrorCode()).thenReturn("NOT_FOUND");
        when(serviceException.getErrorMessage()).thenReturn("order missing");
        when(nativePayService.queryOrderByOutTradeNo(any())).thenThrow(serviceException);

        assertThatThrownBy(() -> wechatPaymentService.queryOrderStatus("ORD-NO"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PAYMENT_QUERY_FAILED.getCode())
                .hasMessageContaining("查询支付状态失败");
    }

    @Test
    @DisplayName("构建回调请求参数包含签名字段")
    void buildRequestParam() {
        RequestParam param = wechatPaymentService.buildRequestParam("sig", "serial", "nonce", "ts", "body");

        assertThat(param.getSignature()).isEqualTo("sig");
        assertThat(param.getBody()).isEqualTo("body");
        assertThat(param.getSerialNumber()).isEqualTo("serial");
    }

    @Test
    @DisplayName("成功响应序列化为 JSON")
    void buildSuccessResponse() throws Exception {
        String json = wechatPaymentService.buildSuccessResponse();
        var tree = objectMapper.readTree(json);
        assertThat(tree.get("code").asText()).isEqualTo("SUCCESS");
        assertThat(tree.get("message").asText()).isEqualTo("成功");
    }

    @Test
    @DisplayName("成功响应序列化失败时返回兜底 JSON")
    void buildSuccessResponse_fallback() throws JsonProcessingException {
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any())).thenThrow(new DummyJsonProcessingException());
        WechatPaymentService service = new WechatPaymentService(nativePayService, wechatPayV3Config, wechatPayConfig, mapper);

        assertThat(service.buildSuccessResponse()).isEqualTo("{\"code\":\"SUCCESS\",\"message\":\"成功\"}");
    }

    @Test
    @DisplayName("失败响应序列化失败时返回兜底 JSON")
    void buildFailResponse_fallback() throws JsonProcessingException {
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any())).thenThrow(new DummyJsonProcessingException());
        WechatPaymentService service = new WechatPaymentService(nativePayService, wechatPayV3Config, wechatPayConfig, mapper);

        assertThat(service.buildFailResponse("oops")).isEqualTo("{\"code\":\"FAIL\",\"message\":\"系统异常\"}");
    }

    @Test
    @DisplayName("失败响应正常序列化")
    void buildFailResponse() throws Exception {
        String json = wechatPaymentService.buildFailResponse("出错");
        var tree = objectMapper.readTree(json);
        assertThat(tree.get("code").asText()).isEqualTo("FAIL");
        assertThat(tree.get("message").asText()).isEqualTo("出错");
    }

    @Test
    @DisplayName("退款默认返回 false 并记录日志")
    void refund_returnsFalse() {
        Order order = Order.builder().orderNo("ORD-REFUND").build();
        boolean result = wechatPaymentService.refund(order, BigDecimal.ONE);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("退款遇到异常仍返回 false")
    void refund_exception() {
        Order order = mock(Order.class);
        when(order.getOrderNo()).thenThrow(new RuntimeException("boom"))
                .thenReturn("ORD-ERR");

        boolean result = wechatPaymentService.refund(order, BigDecimal.ONE);

        assertThat(result).isFalse();
        verify(order, times(2)).getOrderNo();
    }
}
