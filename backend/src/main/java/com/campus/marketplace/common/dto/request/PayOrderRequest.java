package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 支付订单请求 DTO
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public record PayOrderRequest(
        
        /**
         * 订单号（必填）
         */
        @NotBlank(message = "订单号不能为空")
        String orderNo,
        
        /**
         * 支付方式（必填）
         */
        @NotNull(message = "支付方式不能为空")
        PaymentMethod paymentMethod
) {
}
