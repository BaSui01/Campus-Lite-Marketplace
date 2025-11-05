package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.BanUserRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.StatisticsService;
import com.campus.marketplace.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 *
 * 功能模块：
 * 1. 用户管理 - 封禁/解封用户
 * 2. 数据统计 - 系统概览、趋势分析、排行榜
 * 3. 系统管理 - 自动解封过期用户
 *
 * @author BaSui
 * @date 2025-10-29
 */

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "管理员", description = "管理员后台管理相关接口")
public class AdminController {

    private final UserService userService;
    private final StatisticsService statisticsService;

    // ========== 用户管理模块 ==========

        @PostMapping("/users/ban")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)")
    @Operation(summary = "封禁用户", description = "管理员封禁违规用户")
    public ApiResponse<Void> banUser(@Valid @RequestBody BanUserRequest request) {
        userService.banUser(request);
        return ApiResponse.success(null);
    }

        @PostMapping("/users/{userId}/unban")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)")
    @Operation(summary = "解封用户", description = "管理员解封用户")
    public ApiResponse<Void> unbanUser(
            @Parameter(description = "用户 ID", example = "10002") @PathVariable Long userId
    ) {
        userService.unbanUser(userId);
        return ApiResponse.success(null);
    }

        @PostMapping("/users/auto-unban")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)")
    @Operation(summary = "自动解封过期用户", description = "定时任务：自动解封封禁已过期的用户")
    public ApiResponse<Integer> autoUnbanExpiredUsers() {
        int count = userService.autoUnbanExpiredUsers();
        return ApiResponse.success(count);
    }

    // ========== 数据统计模块 ==========

        @GetMapping("/statistics/overview")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取系统概览统计", description = "系统整体数据概览")
    public ApiResponse<Map<String, Object>> getSystemOverview() {
        Map<String, Object> data = statisticsService.getSystemOverview();
        return ApiResponse.success(data);
    }

        @GetMapping("/statistics/users")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取用户统计数据", description = "用户相关统计")
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        Map<String, Object> data = statisticsService.getUserStatistics();
        return ApiResponse.success(data);
    }

        @GetMapping("/statistics/goods")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取物品统计数据", description = "物品相关统计")
    public ApiResponse<Map<String, Object>> getGoodsStatistics() {
        Map<String, Object> data = statisticsService.getGoodsStatistics();
        return ApiResponse.success(data);
    }

        @GetMapping("/statistics/orders")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取订单统计数据", description = "订单相关统计")
    public ApiResponse<Map<String, Object>> getOrderStatistics() {
        Map<String, Object> data = statisticsService.getOrderStatistics();
        return ApiResponse.success(data);
    }

        @GetMapping("/statistics/today")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取今日统计数据", description = "今日数据概览")
    public ApiResponse<Map<String, Object>> getTodayStatistics() {
        Map<String, Object> data = statisticsService.getTodayStatistics();
        return ApiResponse.success(data);
    }

        @GetMapping("/statistics/categories")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取分类统计数据", description = "物品分类分布统计")
    public ApiResponse<Map<String, Long>> getCategoryStatistics() {
        Map<String, Long> data = statisticsService.getCategoryStatistics();
        return ApiResponse.success(data);
    }

        @GetMapping("/statistics/trend")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取趋势数据", description = "最近 N 天的数据趋势分析")
    public ApiResponse<Map<String, Object>> getTrendData(
            @Parameter(description = "天数", example = "30") @RequestParam(defaultValue = "30") int days
    ) {
        Map<String, Object> data = statisticsService.getTrendData(days);
        return ApiResponse.success(data);
    }

        @GetMapping("/statistics/top-goods")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取热门物品排行榜", description = "浏览量最高的物品 Top N")
    public ApiResponse<List<Map<String, Object>>> getTopGoods(
            @Parameter(description = "数量限制", example = "10") @RequestParam(defaultValue = "10") int limit
    ) {
        List<Map<String, Object>> data = statisticsService.getTopGoods(limit);
        return ApiResponse.success(data);
    }

        @GetMapping("/statistics/top-users")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取活跃用户排行榜", description = "积分最高的用户 Top N")
    public ApiResponse<List<Map<String, Object>>> getTopUsers(
            @Parameter(description = "数量限制", example = "20") @RequestParam(defaultValue = "20") int limit
    ) {
        List<Map<String, Object>> data = statisticsService.getTopUsers(limit);
        return ApiResponse.success(data);
    }

        @GetMapping("/statistics/revenue")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取收入统计（按月）", description = "最近 N 个月的平台收入统计")
    public ApiResponse<Map<String, Object>> getRevenueByMonth(
            @Parameter(description = "月数", example = "12") @RequestParam(defaultValue = "12") int months
    ) {
        Map<String, Object> data = statisticsService.getRevenueByMonth(months);
        return ApiResponse.success(data);
    }
}
