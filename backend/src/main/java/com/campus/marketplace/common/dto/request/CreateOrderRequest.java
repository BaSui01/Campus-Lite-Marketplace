package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 创建订单请求 DTO
 * 
 * 用户下单时提交的数据
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public record CreateOrderRequest(
        
        /**
         * 物品 ID（必填）
         */
        @NotNull(message = "物品 ID 不能为空")
        Long goodsId,
        
        /**
         * 优惠券 ID（可选）
         */
        Long couponId
) {
}
