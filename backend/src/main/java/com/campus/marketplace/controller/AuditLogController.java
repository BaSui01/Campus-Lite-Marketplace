package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.AuditLogResponse;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Audit Log Controller
 *
 * @author BaSui
 * @date 2025-10-29
 */

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "审计日志", description = "系统操作审计日志相关接口")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_AUDIT_VIEW)")
    @Operation(summary = "查询审计日志列表", description = "管理员查询系统审计日志")
    public ApiResponse<Page<AuditLogResponse>> listAuditLogs(
            @Parameter(description = "操作人ID", example = "10001") @RequestParam(required = false) Long operatorId,
            @Parameter(description = "操作类型", example = "DELETE") @RequestParam(required = false) AuditActionType actionType,
            @Parameter(description = "开始时间", example = "2025-01-01T00:00:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间", example = "2025-01-31T23:59:59") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        Page<AuditLogResponse> result = auditLogService.listAuditLogs(
                operatorId, actionType, startTime, endTime, page, size
        );
        return ApiResponse.success(result);
    }
}
