package com.campus.marketplace.common.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 标签响应
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Builder
public record TagResponse(
        Long id,
        String name,
        String description,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
