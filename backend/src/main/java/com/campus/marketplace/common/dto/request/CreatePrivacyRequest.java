package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.PrivacyRequestType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建隐私请求 DTO
 */
public record CreatePrivacyRequest(
        @NotNull(message = "请求类型不能为空")
        PrivacyRequestType type,
        @Size(max = 200, message = "备注长度不能超过 200 个字符")
        String reason
) {
}
