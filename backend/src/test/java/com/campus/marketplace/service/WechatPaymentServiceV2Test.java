package com.campus.marketplace.service;

import com.campus.marketplace.common.config.WechatPayV2Config;
import com.campus.marketplace.common.dto.response.PaymentResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.impl.WechatPaymentServiceV2;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderResult;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("微信支付V2服务实现测试")
class WechatPaymentServiceV2Test {

    @Mock private WxPayService wxPayService;
    @Mock private WechatPayV2Config wechatPayV2Config;

    @InjectMocks
    private WechatPaymentServiceV2 wechatPaymentServiceV2;

    @Test
    @DisplayName("创建支付订单成功返回二维码")
    void createPayment_success() throws Exception {
        when(wechatPayV2Config.getNotifyUrl()).thenReturn("https://notify");
        WxPayUnifiedOrderResult result = new WxPayUnifiedOrderResult();
        result.setPrepayId("prepay123");
        result.setCodeURL("weixin://qr/123");
        when(wxPayService.unifiedOrder(any())).thenReturn(result);

        Order order = Order.builder()
                .orderNo("ORD123")
                .actualAmount(new BigDecimal("99.99"))
                .build();

        PaymentResponse response = wechatPaymentServiceV2.createPayment(order);

        assertThat(response.paymentUrl()).isEqualTo("weixin://qr/123");
        verify(wxPayService).unifiedOrder(argThat(req -> req.getTotalFee() == 9999));
    }

    @Test
    @DisplayName("创建支付订单异常抛业务错误")
    void createPayment_failure() throws Exception {
        when(wechatPayV2Config.getNotifyUrl()).thenReturn("https://notify");
        WxPayException exception = new WxPayException("error");
        when(wxPayService.unifiedOrder(any())).thenThrow(exception);
        Order order = Order.builder()
                .orderNo("ORD999")
                .actualAmount(new BigDecimal("10"))
                .build();

        assertThatThrownBy(() -> wechatPaymentServiceV2.createPayment(order))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PAYMENT_CREATE_FAILED.getCode());
    }

    @Test
    @DisplayName("处理支付通知成功返回订单号")
    void handleNotify_success() throws Exception {
        WxPayOrderNotifyResult notifyResult = new WxPayOrderNotifyResult();
        notifyResult.setOutTradeNo("ORD1");
        notifyResult.setTransactionId("TX1");
        notifyResult.setResultCode(WxPayConstants.ResultCode.SUCCESS);
        notifyResult.setTotalFee(100);
        when(wxPayService.parseOrderNotifyResult("xml"))
                .thenReturn(notifyResult);

        String[] result = wechatPaymentServiceV2.handleNotify("xml");

        assertThat(result).containsExactly("ORD1", "TX1");
    }

    @Test
    @DisplayName("处理支付通知异常返回空")
    void handleNotify_error() throws Exception {
        when(wxPayService.parseOrderNotifyResult("bad"))
                .thenThrow(new WxPayException("err"));

        assertThat(wechatPaymentServiceV2.handleNotify("bad")).isNull();
    }

    @Test
    @DisplayName("RequestParam 适配直接返回空")
    void handleNotify_requestParam() {
        assertThat(wechatPaymentServiceV2.handleNotify(new com.wechat.pay.java.core.notification.RequestParam.Builder().build()))
                .isNull();
    }

    @Test
    @DisplayName("查询订单状态返回 UNKNOWN")
    void queryOrderStatus() {
        assertThat(wechatPaymentServiceV2.queryOrderStatus("ORD"))
                .isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("构建成功响应返回固定字符串")
    void buildSuccessResponse() {
        assertThat(wechatPaymentServiceV2.buildSuccessResponse()).contains("SUCCESS");
    }

    @Test
    @DisplayName("构建失败响应返回原因")
    void buildFailResponse() {
        assertThat(wechatPaymentServiceV2.buildFailResponse("error")).contains("error");
    }

}
