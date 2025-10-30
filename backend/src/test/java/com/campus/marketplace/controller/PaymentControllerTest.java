package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.PaymentCallbackRequest;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.service.OrderService;
import com.campus.marketplace.service.RefundService;
import com.campus.marketplace.service.impl.AlipayPaymentService;
import com.campus.marketplace.service.impl.WechatPaymentService;
import com.campus.marketplace.service.impl.WechatPaymentServiceV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Payment Controller Test
 *
 * @author BaSui
 * @date 2025-10-29
 */

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock private OrderService orderService;
    @Mock private RefundService refundService;
    @Mock private WechatPaymentService wechatPaymentService;
    @Mock private WechatPaymentServiceV2 wechatPaymentServiceV2;
    @Mock private AlipayPaymentService alipayPaymentService;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // 注入 wechatPayVersion 字段为 v3
        paymentController = new PaymentController(orderService, refundService);
        try {
            var field = PaymentController.class.getDeclaredField("wechatPayVersion");
            field.setAccessible(true);
            field.set(paymentController, "v3");
            var v3Field = PaymentController.class.getDeclaredField("wechatPaymentService");
            v3Field.setAccessible(true);
            v3Field.set(paymentController, wechatPaymentService);

            var v2Field = PaymentController.class.getDeclaredField("wechatPaymentServiceV2");
            v2Field.setAccessible(true);
            v2Field.set(paymentController, wechatPaymentServiceV2);

            var alipayField = PaymentController.class.getDeclaredField("alipayPaymentService");
            alipayField.setAccessible(true);
            alipayField.set(paymentController, alipayPaymentService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(paymentController)
                .setControllerAdvice(new com.campus.marketplace.common.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("微信V3回调成功 -> 返回200且触发订单更新")
    void wechatNotify_v3_success() throws Exception {
        when(wechatPaymentService.buildRequestParam(any(), any(), any(), any(), any())).thenReturn(null);
        when(wechatPaymentService.handleNotify(any())).thenReturn(new String[]{"O1","T1"});
        when(wechatPaymentService.buildSuccessResponse()).thenReturn("{\"code\":\"SUCCESS\"}");
        when(wechatPaymentService.buildFailResponse(anyString())).thenReturn("{\"code\":\"FAIL\"}");

        Order order = Order.builder().orderNo("O1").amount(new BigDecimal("1"))
                .actualAmount(new BigDecimal("1")).build();
        when(orderService.getOrderDetail("O1")).thenReturn(order);
        when(orderService.handlePaymentCallback(any(PaymentCallbackRequest.class), anyBoolean())).thenReturn(true);

        mockMvc.perform(post("/api/payment/wechat/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Wechatpay-Signature","sig")
                        .header("Wechatpay-Serial","ser")
                        .header("Wechatpay-Nonce","non")
                        .header("Wechatpay-Timestamp","ts")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("SUCCESS")));

        ArgumentCaptor<PaymentCallbackRequest> captor = ArgumentCaptor.forClass(PaymentCallbackRequest.class);
        ArgumentCaptor<Boolean> signatureCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(orderService).handlePaymentCallback(captor.capture(), signatureCaptor.capture());
        assertThat(captor.getValue().orderNo()).isEqualTo("O1");
        assertThat(signatureCaptor.getValue()).isTrue();
    }

    @Test
    @DisplayName("微信V3回调失败 -> 返回400")
    void wechatNotify_v3_fail() throws Exception {
        when(wechatPaymentService.buildRequestParam(any(), any(), any(), any(), any())).thenReturn(null);
        when(wechatPaymentService.handleNotify(any())).thenReturn(null);
        when(wechatPaymentService.buildFailResponse(anyString())).thenReturn("{\"code\":\"FAIL\"}");

        mockMvc.perform(post("/api/payment/wechat/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("微信V2回调成功 -> 返回200且触发订单更新")
    void wechatNotify_v2_success() throws Exception {
        var versionField = PaymentController.class.getDeclaredField("wechatPayVersion");
        versionField.setAccessible(true);
        versionField.set(paymentController, "v2");

        when(wechatPaymentServiceV2.handleNotify(anyString())).thenReturn(new String[]{"O2", "TXN"});
        when(wechatPaymentServiceV2.buildSuccessResponse()).thenReturn("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
        when(wechatPaymentServiceV2.buildFailResponse(anyString())).thenReturn("<xml><return_code><![CDATA[FAIL]]></return_code></xml>");

        Order order = Order.builder().orderNo("O2").amount(new BigDecimal("2")).actualAmount(new BigDecimal("2")).build();
        when(orderService.getOrderDetail("O2")).thenReturn(order);
        when(orderService.handlePaymentCallback(any(PaymentCallbackRequest.class), anyBoolean())).thenReturn(true);

        mockMvc.perform(post("/api/payment/wechat/notify")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("<xml></xml>"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("SUCCESS")));

        ArgumentCaptor<PaymentCallbackRequest> captor = ArgumentCaptor.forClass(PaymentCallbackRequest.class);
        verify(orderService).handlePaymentCallback(captor.capture(), eq(true));
        assertThat(captor.getValue().orderNo()).isEqualTo("O2");
    }

    @Test
    @DisplayName("微信V2回调失败 -> 返回400")
    void wechatNotify_v2_fail() throws Exception {
        var versionField = PaymentController.class.getDeclaredField("wechatPayVersion");
        versionField.setAccessible(true);
        versionField.set(paymentController, "v2");

        when(wechatPaymentServiceV2.handleNotify(anyString())).thenReturn(null);
        when(wechatPaymentServiceV2.buildFailResponse(anyString())).thenReturn("<xml><return_code><![CDATA[FAIL]]></return_code></xml>");

        mockMvc.perform(post("/api/payment/wechat/notify")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("<xml></xml>"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("支付宝退款回调成功 -> 返回success")
    void alipayRefundNotify_success() throws Exception {
        when(alipayPaymentService.verifySignature(anyMap())).thenReturn(true);
        when(refundService.handleRefundCallback(eq("ORD123"), eq("ALIPAY"), eq(true), anyMap())).thenReturn(true);

        mockMvc.perform(post("/api/payment/alipay/refund/notify")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("out_trade_no", "ORD123")
                        .param("refund_status", "SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }

    @Test
    @DisplayName("支付宝退款验签失败 -> 返回400")
    void alipayRefundNotify_verifyFail() throws Exception {
        when(alipayPaymentService.verifySignature(anyMap())).thenReturn(false);

        mockMvc.perform(post("/api/payment/alipay/refund/notify")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("out_trade_no", "ORD123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("fail"));
    }

    @Test
    @DisplayName("查询支付状态：缺少支付服务 -> 500")
    void queryStatus_noService_throws() throws Exception {
        // 注入 null 的 wechatPaymentService
        var v3Field = PaymentController.class.getDeclaredField("wechatPaymentService");
        v3Field.setAccessible(true);
        v3Field.set(paymentController, null);

        mockMvc.perform(get("/api/payment/status/{orderNo}", "O1"))
                .andExpect(status().is5xxServerError());
    }
}
