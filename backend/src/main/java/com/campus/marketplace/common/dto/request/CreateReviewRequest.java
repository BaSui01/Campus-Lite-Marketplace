package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建评价请求 DTO
 *
 * @author BaSui 😎
 * @since 2025-11-08
 */
public record CreateReviewRequest(

        @NotNull(message = "订单ID不能为空")
        Long orderId,

        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分最低1星")
        @Max(value = 5, message = "评分最高5星")
        Integer rating,

        @NotBlank(message = "评价内容不能为空")
        String content,

        Integer qualityScore,

        Integer serviceScore,

        Integer deliveryScore,

        Boolean isAnonymous
) {
}
