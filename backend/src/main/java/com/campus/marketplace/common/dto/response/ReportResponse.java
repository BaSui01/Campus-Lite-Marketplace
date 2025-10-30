package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.entity.Report;
import com.campus.marketplace.common.enums.ReportStatus;
import com.campus.marketplace.common.enums.ReportType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 举报响应 DTO
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Builder
public record ReportResponse(
        Long id,
        Long reporterId,
        String reporterName,
        ReportType targetType,
        Long targetId,
        String reason,
        ReportStatus status,
        Long handlerId,
        String handlerName,
        String handleResult,
        LocalDateTime createdAt
) {
    
    /**
     * 从实体转换为 DTO
     */
    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .reporterName(report.getReporter() != null ? report.getReporter().getUsername() : null)
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .status(report.getStatus())
                .handlerId(report.getHandlerId())
                .handlerName(report.getHandler() != null ? report.getHandler().getUsername() : null)
                .handleResult(report.getHandleResult())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
