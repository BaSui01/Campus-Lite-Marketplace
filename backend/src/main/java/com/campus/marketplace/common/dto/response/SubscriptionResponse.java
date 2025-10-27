package com.campus.marketplace.common.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 关键词订阅响应
 */
@Builder
public record SubscriptionResponse(
        Long id,
        String keyword,
        Long campusId,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
