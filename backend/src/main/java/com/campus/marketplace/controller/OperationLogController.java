package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.AuditLogResponse;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志控制器
 *
 * 基于 AuditLog 实现，提供操作日志查询和统计功能
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Slf4j
@RestController
@RequestMapping("/admin/logs/operations")
@RequiredArgsConstructor
@Tag(name = "操作日志管理", description = "管理员查询操作日志和统计数据")
public class OperationLogController {

    private final AuditLogService auditLogService;

    /**
     * 查询操作日志列表（分页）
     *
     * @param operatorId 操作人ID（可选）
     * @param actionType 操作类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 操作日志分页结果（包含统计数据）
     */
    @GetMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_AUDIT_VIEW)")
    @Operation(summary = "查询操作日志列表", description = "管理员查询系统操作日志（支持分页和筛选）")
    public ApiResponse<Map<String, Object>> listOperationLogs(
            @Parameter(description = "操作人ID", example = "10001") @RequestParam(required = false) Long operatorId,
            @Parameter(description = "操作类型", example = "DELETE") @RequestParam(required = false) AuditActionType actionType,
            @Parameter(description = "开始时间", example = "2025-01-01T00:00:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间", example = "2025-12-31T23:59:59") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        log.info("查询操作日志列表: operatorId={}, actionType={}, startTime={}, endTime={}, page={}, size={}",
                operatorId, actionType, startTime, endTime, page, size);

        // 查询操作日志列表
        Page<AuditLogResponse> logPage = auditLogService.listAuditLogs(operatorId, actionType, startTime, endTime, page, size);

        // 查询统计数据
        Map<String, Object> statistics = auditLogService.getStatistics(operatorId, actionType, startTime, endTime);

        // 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("content", logPage.getContent());
        result.put("totalElements", logPage.getTotalElements());
        result.put("totalPages", logPage.getTotalPages());
        result.put("statistics", statistics);

        log.info("操作日志查询成功: total={}, statistics={}", logPage.getTotalElements(), statistics);
        return ApiResponse.success(result);
    }
}
