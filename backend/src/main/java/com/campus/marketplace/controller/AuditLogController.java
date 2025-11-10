package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.AuditLogFilterRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.AuditLogResponse;
import com.campus.marketplace.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Audit Log Controller
 *
 * @author BaSui
 * @date 2025-10-29
 */

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Tag(name = "审计日志", description = "系统操作审计日志相关接口")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_AUDIT_VIEW)")
    @Operation(summary = "查询审计日志列表", description = "管理员查询系统审计日志，支持分页、筛选、排序")
    public ApiResponse<Page<AuditLogResponse>> listAuditLogs(AuditLogFilterRequest filterRequest) {
        Page<AuditLogResponse> result = auditLogService.listAuditLogs(filterRequest);
        return ApiResponse.success(result);
    }
}
