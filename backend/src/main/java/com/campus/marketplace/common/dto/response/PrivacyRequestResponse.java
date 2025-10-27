package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.enums.PrivacyRequestStatus;
import com.campus.marketplace.common.enums.PrivacyRequestType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 隐私请求响应
 */
@Builder
public record PrivacyRequestResponse(
        Long id,
        PrivacyRequestType type,
        PrivacyRequestStatus status,
        String reason,
        String resultPath,
        LocalDateTime scheduledAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
