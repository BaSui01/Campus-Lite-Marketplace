package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.enums.OrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单响应 DTO
 * 
 * 返回给前端的订单信息
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Builder
public record OrderResponse(
        Long id,
        String orderNo,
        Long goodsId,
        String goodsTitle,
        String goodsImage,
        Long buyerId,
        String buyerUsername,
        Long sellerId,
        String sellerUsername,
        BigDecimal amount,
        BigDecimal discountAmount,
        BigDecimal actualAmount,
        OrderStatus status,
        String paymentMethod,
        LocalDateTime paymentTime,
        LocalDateTime createdAt,
        /**
         * 支付截止时间（由后端根据配置计算），用于前端倒计时显示
         */
        LocalDateTime paymentExpireAt,
        /**
         * 待支付超时阈值（分钟），用于前端说明文案
         */
        Integer timeoutMinutes
) {
}
