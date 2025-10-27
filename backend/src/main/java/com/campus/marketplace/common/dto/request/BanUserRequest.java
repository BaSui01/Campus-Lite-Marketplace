package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 封禁用户请求 DTO
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public record BanUserRequest(
        
        /**
         * 用户ID（必填）
         */
        @NotNull(message = "用户ID不能为空")
        Long userId,
        
        /**
         * 封禁原因（必填）
         */
        @NotBlank(message = "封禁原因不能为空")
        String reason,
        
        /**
         * 封禁天数（0表示永久封禁）
         */
        @NotNull(message = "封禁天数不能为空")
        @Min(value = 0, message = "封禁天数不能为负数")
        Integer days
) {
}
