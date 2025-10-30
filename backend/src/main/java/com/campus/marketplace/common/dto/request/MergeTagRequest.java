package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 标签合并请求
 *
 * @author BaSui
 * @date 2025-10-29
 */

public record MergeTagRequest(
        @NotNull(message = "来源标签不能为空")
        Long sourceTagId,
        @NotNull(message = "目标标签不能为空")
        Long targetTagId
) {
}
