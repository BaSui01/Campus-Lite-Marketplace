package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 支付回调请求 DTO
 * 
 * 模拟支付平台的回调数据
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public record PaymentCallbackRequest(
        
        /**
         * 订单号
         */
        @NotBlank(message = "订单号不能为空")
        String orderNo,
        
        /**
         * 支付流水号
         */
        @NotBlank(message = "支付流水号不能为空")
        String transactionId,
        
        /**
         * 支付金额
         */
        @NotNull(message = "支付金额不能为空")
        BigDecimal amount,
        
        /**
         * 支付状态（SUCCESS/FAIL）
         */
        @NotBlank(message = "支付状态不能为空")
        String status,
        
        /**
         * 签名（沙箱模式简化验证）
         */
        String signature
) {
}
