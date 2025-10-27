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
        LocalDateTime createdAt
) {
}
