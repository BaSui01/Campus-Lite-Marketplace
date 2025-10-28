package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.PaymentCallbackRequest;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.service.OrderService;
import com.campus.marketplace.service.PaymentService;
import com.campus.marketplace.service.RefundService;
import com.campus.marketplace.service.impl.WechatPaymentService;
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

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock private PaymentService paymentService;
    @Mock private OrderService orderService;
    @Mock private RefundService refundService;
    @Mock private WechatPaymentService wechatPaymentService;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // 注入 wechatPayVersion 字段为 v3
        paymentController = new PaymentController(paymentService, orderService, refundService);
        try {
            var field = PaymentController.class.getDeclaredField("wechatPayVersion");
            field.setAccessible(true);
            field.set(paymentController, "v3");
            var v3Field = PaymentController.class.getDeclaredField("wechatPaymentService");
            v3Field.setAccessible(true);
            v3Field.set(paymentController, wechatPaymentService);
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
        when(orderService.handlePaymentCallback(any(PaymentCallbackRequest.class))).thenReturn(true);
        when(paymentService.verifySignature(anyString(), anyString(), any())).thenReturn(true);

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
        verify(orderService).handlePaymentCallback(captor.capture());
        assertThat(captor.getValue().orderNo()).isEqualTo("O1");
        verify(paymentService).verifySignature(eq("O1"), eq("T1"), any());
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
