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

    /**
     * 封禁用户
     * 
     * 管理员专用接口，用于封禁违规用户
     * 
     * @param request 封禁请求（包含用户ID、原因、封禁截止时间）
     * @return 操作结果
     */
    @PostMapping("/users/ban")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)")
    @Operation(summary = "封禁用户", description = "管理员封禁违规用户")
    public ApiResponse<Void> banUser(@Valid @RequestBody BanUserRequest request) {
        userService.banUser(request);
        return ApiResponse.success(null);
    }

    /**
     * 解封用户
     * 
     * 管理员专用接口，用于解封被误封或申诉成功的用户
     * 
     * @param userId 用户 ID
     * @return 操作结果
     */
    @PostMapping("/users/{userId}/unban")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)")
    @Operation(summary = "解封用户", description = "管理员解封用户")
    public ApiResponse<Void> unbanUser(
            @Parameter(description = "用户 ID", example = "10002") @PathVariable Long userId
    ) {
        userService.unbanUser(userId);
        return ApiResponse.success(null);
    }

    /**
     * 自动解封过期用户
     * 
     * 定时任务调用，自动解封封禁时间已过期的用户
     * 
     * @return 解封的用户数量
     */
    @PostMapping("/users/auto-unban")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)")
    @Operation(summary = "自动解封过期用户", description = "定时任务：自动解封封禁已过期的用户")
    public ApiResponse<Integer> autoUnbanExpiredUsers() {
        int count = userService.autoUnbanExpiredUsers();
        return ApiResponse.success(count);
    }

    // ========== 数据统计模块 ==========

    /**
     * 获取系统概览统计
     * 
     * 统计内容：
     * - 总用户数、总物品数、总订单数
     * - 今日新增用户、今日新增物品、今日新增订单
     * - 活跃用户数、待审核物品数
     * 
     * @return 系统概览统计数据
     */
    @GetMapping("/statistics/overview")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取系统概览统计", description = "系统整体数据概览")
    public ApiResponse<Map<String, Object>> getSystemOverview() {
        Map<String, Object> data = statisticsService.getSystemOverview();
        return ApiResponse.success(data);
    }

    /**
     * 获取用户统计数据
     * 
     * 统计内容：
     * - 今日新增用户、活跃用户
     * - 封禁用户数、用户增长趋势
     * 
     * @return 用户统计数据
     */
    @GetMapping("/statistics/users")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取用户统计数据", description = "用户相关统计")
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        Map<String, Object> data = statisticsService.getUserStatistics();
        return ApiResponse.success(data);
    }

    /**
     * 获取物品统计数据
     * 
     * 统计内容：
     * - 总物品数、待审核、已上架、已售出
     * - 各分类物品数量分布
     * 
     * @return 物品统计数据
     */
    @GetMapping("/statistics/goods")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取物品统计数据", description = "物品相关统计")
    public ApiResponse<Map<String, Object>> getGoodsStatistics() {
        Map<String, Object> data = statisticsService.getGoodsStatistics();
        return ApiResponse.success(data);
    }

    /**
     * 获取订单统计数据
     * 
     * 统计内容：
     * - 总订单数、已完成、已取消
     * - 交易金额统计
     * 
     * @return 订单统计数据
     */
    @GetMapping("/statistics/orders")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取订单统计数据", description = "订单相关统计")
    public ApiResponse<Map<String, Object>> getOrderStatistics() {
        Map<String, Object> data = statisticsService.getOrderStatistics();
        return ApiResponse.success(data);
    }

    /**
     * 获取今日统计数据
     * 
     * 统计内容：
     * - 今日新增用户、物品、订单
     * - 今日活跃用户、交易金额
     * 
     * @return 今日统计数据
     */
    @GetMapping("/statistics/today")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取今日统计数据", description = "今日数据概览")
    public ApiResponse<Map<String, Object>> getTodayStatistics() {
        Map<String, Object> data = statisticsService.getTodayStatistics();
        return ApiResponse.success(data);
    }

    /**
     * 获取分类统计数据
     * 
     * 统计内容：
     * - 各分类的物品数量
     * 
     * @return 分类统计数据（分类名 -> 物品数量）
     */
    @GetMapping("/statistics/categories")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取分类统计数据", description = "物品分类分布统计")
    public ApiResponse<Map<String, Long>> getCategoryStatistics() {
        Map<String, Long> data = statisticsService.getCategoryStatistics();
        return ApiResponse.success(data);
    }

    /**
     * 获取趋势数据
     * 
     * 统计内容：
     * - 最近 N 天的用户增长、订单增长、交易额趋势
     * 
     * @param days 天数（默认 30 天）
     * @return 趋势数据
     */
    @GetMapping("/statistics/trend")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取趋势数据", description = "最近 N 天的数据趋势分析")
    public ApiResponse<Map<String, Object>> getTrendData(
            @Parameter(description = "天数", example = "30") @RequestParam(defaultValue = "30") int days
    ) {
        Map<String, Object> data = statisticsService.getTrendData(days);
        return ApiResponse.success(data);
    }

    /**
     * 获取热门物品排行榜
     * 
     * 统计内容：
     * - 浏览量最高的物品 Top N
     * 
     * @param limit 数量限制（默认 10）
     * @return 热门物品列表
     */
    @GetMapping("/statistics/top-goods")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取热门物品排行榜", description = "浏览量最高的物品 Top N")
    public ApiResponse<List<Map<String, Object>>> getTopGoods(
            @Parameter(description = "数量限制", example = "10") @RequestParam(defaultValue = "10") int limit
    ) {
        List<Map<String, Object>> data = statisticsService.getTopGoods(limit);
        return ApiResponse.success(data);
    }

    /**
     * 获取活跃用户排行榜
     * 
     * 统计内容：
     * - 积分最高的用户 Top N
     * 
     * @param limit 数量限制（默认 20）
     * @return 活跃用户列表
     */
    @GetMapping("/statistics/top-users")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取活跃用户排行榜", description = "积分最高的用户 Top N")
    public ApiResponse<List<Map<String, Object>>> getTopUsers(
            @Parameter(description = "数量限制", example = "20") @RequestParam(defaultValue = "20") int limit
    ) {
        List<Map<String, Object>> data = statisticsService.getTopUsers(limit);
        return ApiResponse.success(data);
    }

    /**
     * 获取收入统计（按月）
     * 
     * 统计内容：
     * - 最近 N 个月的平台收入（交易手续费）
     * 
     * @param months 月数（默认 12）
     * @return 收入统计数据
     */
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
