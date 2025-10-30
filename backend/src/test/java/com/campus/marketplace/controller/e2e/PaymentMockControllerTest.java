package com.campus.marketplace.controller.e2e;

import com.campus.marketplace.common.dto.request.PaymentCallbackRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentMockController 测试")
class PaymentMockControllerTest {

    @Mock
    private OrderService orderService;

    private PaymentMockController controller;

    @BeforeEach
    void setUp() {
        controller = new PaymentMockController(orderService);
    }

    @Test
    @DisplayName("金额合法且订单回调成功时返回成功响应")
    void mockWechatPayment_shouldReturnSuccessResponse() {
        PaymentCallbackRequest request = new PaymentCallbackRequest(
                "ORDER-1",
                "TX-001",
                BigDecimal.TEN,
                "SUCCESS",
                "sig"
        );
        when(orderService.handlePaymentCallback(request, true)).thenReturn(true);

        ApiResponse<Void> response = controller.mockWechatPayment(request);

        assertThat(response.getCode()).isEqualTo(ErrorCode.SUCCESS.getCode());
        assertThat(response.getMessage()).isEqualTo("模拟支付成功");
        assertThat(response.getData()).isNull();
        verify(orderService).handlePaymentCallback(request, true);
    }

    @Test
    @DisplayName("金额非正时抛出业务异常并拒绝调用服务")
    void mockWechatPayment_shouldRejectNonPositiveAmount() {
        PaymentCallbackRequest request = new PaymentCallbackRequest(
                "ORDER-2",
                "TX-002",
                BigDecimal.ZERO,
                "FAIL",
                "sig"
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> controller.mockWechatPayment(request));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAMETER);
        verify(orderService, never()).handlePaymentCallback(any(), eq(true));
    }

    @Test
    @DisplayName("订单服务返回失败时抛出业务异常")
    void mockWechatPayment_shouldThrowWhenOrderServiceFails() {
        PaymentCallbackRequest request = new PaymentCallbackRequest(
                "ORDER-3",
                "TX-003",
                BigDecimal.ONE,
                "SUCCESS",
                "sig"
        );
        when(orderService.handlePaymentCallback(request, true)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> controller.mockWechatPayment(request));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.OPERATION_FAILED);
        verify(orderService).handlePaymentCallback(request, true);
    }
}
