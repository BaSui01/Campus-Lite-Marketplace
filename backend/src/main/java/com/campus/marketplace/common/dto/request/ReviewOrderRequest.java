package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 订单评价请求 DTO
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public record ReviewOrderRequest(
        
        /**
         * 订单号（必填）
         */
        @NotBlank(message = "订单号不能为空")
        String orderNo,
        
        /**
         * 评分（1-5星，必填）
         */
        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分最低1星")
        @Max(value = 5, message = "评分最高5星")
        Integer rating,
        
        /**
         * 评价内容（必填）
         */
        @NotBlank(message = "评价内容不能为空")
        String content
) {
}
