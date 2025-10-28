package com.campus.marketplace.integration;

import com.campus.marketplace.service.OrderService;
import com.campus.marketplace.service.PaymentService;
import com.campus.marketplace.service.impl.WechatPaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 支付回调幂等与签名校验（V3 模式）集成测试：
 * - 构造同一回调请求调用两次，均返回 200
 * - 验签被调用
 * - 订单回调处理被调用两次（由服务实现保证幂等）
 */
class PaymentWebhookIdempotencyIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WechatPaymentService wechatPaymentService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private OrderService orderService;

    private void stubHappyPath(String orderNo, String txnId) {
        // 模拟 V3 解析请求体
        when(wechatPaymentService.buildRequestParam(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(org.mockito.Mockito.mock(com.wechat.pay.java.core.notification.RequestParam.class));
        when(wechatPaymentService.handleNotify(any())).thenReturn(new String[]{orderNo, txnId});
        when(wechatPaymentService.buildSuccessResponse()).thenReturn("{\"code\":\"SUCCESS\"}");
        when(wechatPaymentService.buildFailResponse(anyString())).thenReturn("{\"code\":\"FAIL\"}");

        // 验签通过
        when(paymentService.verifySignature(eq(orderNo), eq(txnId), any())).thenReturn(true);

        // 订单金额与回调处理
        com.campus.marketplace.common.entity.Order order = new com.campus.marketplace.common.entity.Order();
        order.setOrderNo(orderNo);
        order.setAmount(new BigDecimal("99.99"));
        when(orderService.getOrderDetail(orderNo)).thenReturn(order);
        when(orderService.handlePaymentCallback(any())).thenReturn(true);
    }

    @Test
    @DisplayName("微信支付回调重复请求幂等返回 200，且验签与回调被调用")
    void wechat_notify_idempotent_200() throws Exception {
        String orderNo = "O202510280001";
        String txnId = "T1234567890";
        stubHappyPath(orderNo, txnId);

        String body = "{\"id\":\"EVENT\",\"resource\":{}}";

        // 第一次回调
        mockMvc.perform(post("/api/payment/wechat/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Wechatpay-Signature", "sig")
                        .header("Wechatpay-Serial", "serial")
                        .header("Wechatpay-Nonce", "nonce")
                        .header("Wechatpay-Timestamp", "ts")
                        .content(body))
                .andExpect(status().isOk());

        // 第二次回调（重放）
        mockMvc.perform(post("/api/payment/wechat/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Wechatpay-Signature", "sig")
                        .header("Wechatpay-Serial", "serial")
                        .header("Wechatpay-Nonce", "nonce")
                        .header("Wechatpay-Timestamp", "ts")
                        .content(body))
                .andExpect(status().isOk());

        Mockito.verify(paymentService, times(2)).verifySignature(eq(orderNo), eq(txnId), any());
        Mockito.verify(orderService, times(2)).handlePaymentCallback(any());
    }
}
