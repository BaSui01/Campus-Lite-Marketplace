package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新分类请求
 */
public record UpdateCategoryRequest(
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 50, message = "分类名称长度不能超过 50 个字符")
        String name,
        @Size(max = 200, message = "分类描述长度不能超过 200 个字符")
        String description,
        Long parentId,
        Integer sortOrder
) {
}
