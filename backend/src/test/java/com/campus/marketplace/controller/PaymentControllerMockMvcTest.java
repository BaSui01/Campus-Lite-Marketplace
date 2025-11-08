package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.dto.request.PaymentCallbackRequest;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.service.OrderService;
import com.campus.marketplace.service.RefundService;
import com.campus.marketplace.service.impl.AlipayPaymentService;
import com.campus.marketplace.service.impl.WechatPaymentService;
import com.campus.marketplace.service.impl.WechatPaymentServiceV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("PaymentController MockMvc 集成测试")
class PaymentControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentController paymentController;

    @MockBean
    private OrderService orderService;

    @MockBean
    private RefundService refundService;

    @MockBean
    private WechatPaymentService wechatPaymentService;

    @MockBean
    private WechatPaymentServiceV2 wechatPaymentServiceV2;

    @MockBean
    private AlipayPaymentService alipayPaymentService;

    @BeforeEach
    void resetVersion() {
        // 默认使用 v3，部分测试会覆盖
        ReflectionTestUtils.setField(paymentController, "wechatPayVersion", "v3");
    }

    @Nested
    @DisplayName("微信支付回调")
    class WechatNotify {

        @Test
        @DisplayName("v2 回调成功 -> 更新订单并返回成功响应")
        void v2Notify_success() throws Exception {
            ReflectionTestUtils.setField(paymentController, "wechatPayVersion", "v2");
            String xmlBody = "<xml>mock</xml>";

            when(wechatPaymentServiceV2.handleNotify(xmlBody))
                    .thenReturn(new String[]{"ORDER123", "TXN456"});
            when(wechatPaymentServiceV2.buildSuccessResponse()).thenReturn("<xml>SUCCESS</xml>");
            when(wechatPaymentServiceV2.buildFailResponse("订单信息为空")).thenReturn("<xml>FAIL</xml>");
            when(orderService.getOrderDetail("ORDER123")).thenReturn(
                    Order.builder().orderNo("ORDER123").amount(new BigDecimal("99.90")).build()
            );
            when(orderService.handlePaymentCallback(any(PaymentCallbackRequest.class), eq(true))).thenReturn(true);

            MockHttpServletRequestBuilder request = post("/payment/wechat/notify")
                    .contentType(MediaType.APPLICATION_XML)
                    .content(xmlBody);

            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string("<xml>SUCCESS</xml>"));

            verify(wechatPaymentServiceV2).handleNotify(xmlBody);
            ArgumentCaptor<PaymentCallbackRequest> captor = ArgumentCaptor.forClass(PaymentCallbackRequest.class);
            verify(orderService).handlePaymentCallback(captor.capture(), eq(true));
            assertThat(captor.getValue().orderNo()).isEqualTo("ORDER123");
            assertThat(captor.getValue().transactionId()).isEqualTo("TXN456");
            assertThat(captor.getValue().amount()).isEqualByComparingTo("99.90");
        }

        @Test
        @DisplayName("v3 回调返回订单信息为空 -> 生成失败响应")
        void v3Notify_missingOrder() throws Exception {
            String jsonBody = "{\"resource\":{}}";
            when(wechatPaymentService.buildRequestParam(any(), any(), any(), any(), eq(jsonBody)))
                    .thenReturn(null);
            when(wechatPaymentService.handleNotify(null)).thenReturn(null);
            when(wechatPaymentService.buildSuccessResponse()).thenReturn("{\"code\":\"SUCCESS\"}");
            when(wechatPaymentService.buildFailResponse("订单信息为空")).thenReturn("{\"code\":\"FAIL\"}");

            MockHttpServletRequestBuilder request = post("/payment/wechat/notify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
                    .header("Wechatpay-Signature", "sig")
                    .header("Wechatpay-Serial", "serial")
                    .header("Wechatpay-Nonce", "nonce")
                    .header("Wechatpay-Timestamp", "ts");

            mockMvc.perform(request)
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("{\"code\":\"FAIL\"}"));

            verify(wechatPaymentService).handleNotify(null);
            verify(orderService, never()).handlePaymentCallback(any(), anyBoolean());
        }
    }

    @Test
    @DisplayName("GET /api/payment/status/{orderNo} -> v3 查询支付状态")
    @WithMockUser(roles = "STUDENT")
    void queryPaymentStatus_v3() throws Exception {
        when(wechatPaymentService.queryOrderStatus("ORDER999")).thenReturn("SUCCESS");

        mockMvc.perform(get("/payment/status/ORDER999"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"code\":200,\"message\":\"操作成功\",\"data\":\"SUCCESS\"}"));

        verify(wechatPaymentService).queryOrderStatus("ORDER999");
    }

    @Test
    @DisplayName("POST /api/payment/alipay/refund/notify -> 验签成功且业务处理成功")
    void alipayRefundNotify_success() throws Exception {
        when(alipayPaymentService.verifySignature(anyMap())).thenReturn(true);
        when(refundService.handleRefundCallback(eq("ORDER789"), eq("ALIPAY"), eq(true), anyMap()))
                .thenReturn(true);

        mockMvc.perform(post("/payment/alipay/refund/notify")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("out_trade_no", "ORDER789")
                        .param("refund_status", "REFUND_SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        verify(refundService).handleRefundCallback(eq("ORDER789"), eq("ALIPAY"), eq(true), anyMap());
    }

    @Test
    @DisplayName("POST /api/payment/alipay/refund/notify -> 验签失败返回400")
    void alipayRefundNotify_verifyFailed() throws Exception {
        when(alipayPaymentService.verifySignature(anyMap())).thenReturn(false);

        mockMvc.perform(post("/payment/alipay/refund/notify")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("out_trade_no", "ORDER789"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("fail"));

        verify(refundService, never()).handleRefundCallback(any(), any(), anyBoolean(), anyMap());
    }
}
