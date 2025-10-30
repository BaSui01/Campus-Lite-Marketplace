package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.enums.AuditActionType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 审计日志响应 DTO
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Builder
public record AuditLogResponse(
        Long id,
        Long operatorId,
        String operatorName,
        AuditActionType actionType,
        String targetType,
        Long targetId,
        String details,
        String result,
        String ipAddress,
        LocalDateTime createdAt
) {
    
    public static AuditLogResponse from(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .operatorId(log.getOperatorId())
                .operatorName(log.getOperatorName())
                .actionType(log.getActionType())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .details(log.getDetails())
                .result(log.getResult())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
