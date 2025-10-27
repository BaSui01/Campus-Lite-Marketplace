package com.campus.marketplace.common.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类树节点
 */
@Builder
public record CategoryNodeResponse(
        Long id,
        String name,
        String description,
        Long parentId,
        Integer sortOrder,
        LocalDateTime createdAt,
        List<CategoryNodeResponse> children
) {
}
