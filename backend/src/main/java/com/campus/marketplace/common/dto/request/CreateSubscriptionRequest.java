package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建关键词订阅请求
 */
public record CreateSubscriptionRequest(
        @NotBlank(message = "订阅关键词不能为空")
        @Size(max = 100, message = "订阅关键词长度不能超过 100 个字符")
        String keyword,
        Long campusId
) {
}
