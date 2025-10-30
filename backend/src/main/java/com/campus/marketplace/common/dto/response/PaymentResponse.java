package com.campus.marketplace.common.dto.response;

import lombok.Builder;

/**
 * 支付响应 DTO
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Builder
public record PaymentResponse(
        /**
         * 订单号
         */
        String orderNo,
        
        /**
         * 支付链接（沙箱模式返回模拟链接）
         */
        String paymentUrl,
        
        /**
         * 支付二维码（可选）
         */
        String qrCode,
        
        /**
         * 过期时间（秒）
         */
        Integer expireSeconds
) {
}
