package com.campus.marketplace.common.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 关注响应
 */
@Builder
public record FollowResponse(
        Long sellerId,
        String sellerName,
        String sellerAvatar,
        LocalDateTime followedAt
) {
}
