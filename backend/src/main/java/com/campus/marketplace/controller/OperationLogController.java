package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.AuditLogFilterRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.AuditLogResponse;
import com.campus.marketplace.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_AUDIT_VIEW)")
    @Operation(summary = "查询操作日志列表", description = "管理员查询系统操作日志（支持分页、筛选、排序）")
    public ApiResponse<Map<String, Object>> listOperationLogs(AuditLogFilterRequest filterRequest) {
        log.info("查询操作日志列表: operatorId={}, actionType={}, startTime={}, endTime={}, page={}, size={}",
                filterRequest.getOperatorId(), filterRequest.getActionType(), 
                filterRequest.getStartTime(), filterRequest.getEndTime(), 
                filterRequest.getPage(), filterRequest.getSize());

        // 查询操作日志列表
        Page<AuditLogResponse> logPage = auditLogService.listAuditLogs(filterRequest);

        // 查询统计数据
        Map<String, Object> statistics = auditLogService.getStatistics(
                filterRequest.getOperatorId(), filterRequest.getActionType(), 
                filterRequest.getStartTime(), filterRequest.getEndTime()
        );

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
