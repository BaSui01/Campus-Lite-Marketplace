package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建标签请求
 */
public record CreateTagRequest(
        @NotBlank(message = "标签名称不能为空")
        @Size(max = 50, message = "标签名称长度不能超过 50 个字符")
        String name,
        @Size(max = 200, message = "标签描述长度不能超过 200 个字符")
        String description,
        Boolean enabled
) {
}
