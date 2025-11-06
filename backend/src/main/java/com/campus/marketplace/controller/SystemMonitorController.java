package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.*;
import com.campus.marketplace.common.entity.ApiPerformanceLog;
import com.campus.marketplace.common.entity.ErrorLog;
import com.campus.marketplace.common.entity.HealthCheckRecord;
import com.campus.marketplace.common.enums.ErrorSeverity;
import com.campus.marketplace.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统监控Controller
 * 
 * 整合了健康检查、API性能监控、错误日志和性能报表
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
@Tag(name = "系统监控", description = "系统健康检查与性能监控")
public class SystemMonitorController {

    private final SystemMonitorService systemMonitorService;
    private final ApiPerformanceService apiPerformanceService;
    private final ErrorLogService errorLogService;
    private final PerformanceReportService performanceReportService;

    // ========== 健康检查 ==========

    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public ApiResponse<HealthCheckResponse> healthCheck() {
        return ApiResponse.success(systemMonitorService.performHealthCheck());
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_MONITOR_VIEW)")
    @Operation(summary = "获取系统指标")
    public ApiResponse<SystemMetricsResponse> getMetrics() {
        return ApiResponse.success(systemMonitorService.getSystemMetrics());
    }

    @GetMapping("/health/history")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_MONITOR_VIEW)")
    @Operation(summary = "获取健康检查历史")
    public ApiResponse<List<HealthCheckRecord>> getHealthHistory(
        @RequestParam(defaultValue = "24") int hours
    ) {
        return ApiResponse.success(systemMonitorService.getHealthCheckHistory(hours));
    }

    // ========== API性能监控 ==========

    @GetMapping("/api/slow-queries")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_MONITOR_VIEW)")
    @Operation(summary = "获取慢查询日志")
    public ApiResponse<List<ApiPerformanceLog>> getSlowQueries(
        @RequestParam(defaultValue = "24") int hours
    ) {
        return ApiResponse.success(apiPerformanceService.getSlowQueries(hours));
    }

    @GetMapping("/api/statistics")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_MONITOR_VIEW)")
    @Operation(summary = "获取端点性能统计")
    public ApiResponse<List<ApiPerformanceStatistics.EndpointStats>> getEndpointStatistics(
        @RequestParam(defaultValue = "24") int hours
    ) {
        return ApiResponse.success(apiPerformanceService.getEndpointStatistics(hours));
    }

    @GetMapping("/api/errors")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_MONITOR_VIEW)")
    @Operation(summary = "获取错误请求日志")
    public ApiResponse<List<ApiPerformanceLog>> getErrorRequests(
        @RequestParam(defaultValue = "24") int hours
    ) {
        return ApiResponse.success(apiPerformanceService.getErrorRequests(hours));
    }

    @GetMapping("/api/qps")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_MONITOR_VIEW)")
    @Operation(summary = "获取QPS统计")
    public ApiResponse<List<ApiPerformanceStatistics.QpsData>> getQpsStatistics(
        @RequestParam(defaultValue = "1") int hours
    ) {
        return ApiResponse.success(apiPerformanceService.getQpsStatistics(hours));
    }

    // ========== 错误日志监控 ==========

    @GetMapping("/errors/unresolved")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_MONITOR_VIEW)")
    @Operation(summary = "获取未解决的错误")
    public ApiResponse<List<ErrorLog>> getUnresolvedErrors() {
        return ApiResponse.success(errorLogService.getUnresolvedErrors());
    }

    @GetMapping("/errors/by-severity")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_MONITOR_VIEW)")
    @Operation(summary = "获取指定严重程度的错误")
    public ApiResponse<List<ErrorLog>> getErrorsBySeverity(
        @RequestParam ErrorSeverity severity,
        @RequestParam(defaultValue = "24") int hours
    ) {
        return ApiResponse.success(errorLogService.getErrorsBySeverity(severity, hours));
    }

    @GetMapping("/errors/statistics")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_MONITOR_VIEW)")
    @Operation(summary = "获取错误统计")
    public ApiResponse<Map<ErrorSeverity, Long>> getErrorStatistics(
        @RequestParam(defaultValue = "24") int hours
    ) {
        return ApiResponse.success(errorLogService.getErrorStatistics(hours));
    }

    @PostMapping("/errors/{errorId}/resolve")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_MONITOR_MANAGE)")
    @Operation(summary = "标记错误为已解决")
    public ApiResponse<Void> markErrorAsResolved(@PathVariable Long errorId) {
        errorLogService.markAsResolved(errorId);
        return ApiResponse.success();
    }

    // ========== 性能报表 ==========

    @GetMapping("/report")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_MONITOR_VIEW)")
    @Operation(summary = "生成性能报表")
    public ApiResponse<PerformanceReportResponse> generatePerformanceReport(
        @RequestParam(defaultValue = "24") int hours
    ) {
        return ApiResponse.success(performanceReportService.generateReport(hours));
    }

    @GetMapping("/health-score")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_MONITOR_VIEW)")
    @Operation(summary = "获取系统健康度评分")
    public ApiResponse<Double> getHealthScore(
        @RequestParam(defaultValue = "24") int hours
    ) {
        return ApiResponse.success(performanceReportService.calculateHealthScore(hours));
    }

    // ========== 数据清理 ==========

    @DeleteMapping("/cleanup")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_MONITOR_MANAGE)")
    @Operation(summary = "清理所有历史数据")
    public ApiResponse<Void> cleanupAllHistory(
        @RequestParam(defaultValue = "30") int daysToKeep
    ) {
        systemMonitorService.cleanupOldRecords(daysToKeep);
        apiPerformanceService.cleanupOldLogs(daysToKeep);
        errorLogService.cleanupOldLogs(daysToKeep);
        return ApiResponse.success();
    }
}
