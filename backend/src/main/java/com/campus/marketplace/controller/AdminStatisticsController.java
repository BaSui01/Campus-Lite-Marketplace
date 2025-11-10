package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.OrderStatisticsDTO;
import com.campus.marketplace.common.dto.response.RefundStatisticsDTO;
import com.campus.marketplace.common.dto.response.SystemOverviewDTO;
import com.campus.marketplace.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 🎯 BaSui 的管理端统计接口 - 专业又搞笑的数据分析！😎
 *
 * 功能范围：
 * - 📊 系统概览统计（用户、物品、订单、收入）
 * - 📈 趋势数据分析（用户增长、物品发布、订单趋势）
 * - 🏆 排行榜数据（热门商品、活跃用户）
 * - 💰 收入统计（按月统计）
 * - 📂 分类统计
 *
 * ⚠️ 权限要求：所有接口仅管理员可访问（@PreAuthorize("hasRole('ADMIN')")）
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */
@Slf4j
@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
@Tag(name = "管理端统计", description = "管理后台数据统计分析接口")
public class AdminStatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 📊 获取系统概览统计
     *
     * GET /api/admin/statistics/overview
     *
     * 返回数据：
     * - totalUsers: 总用户数
     * - totalGoods: 总物品数
     * - totalOrders: 总订单数
     * - totalRevenue: 总收入
     * - todayNewUsers: 今日新增用户
     * - todayNewGoods: 今日新增物品
     * - todayNewOrders: 今日新增订单
     * - activeUsers: 活跃用户数
     * - pendingGoods: 待审核物品数
     *
     * @return 系统概览统计数据（强类型 DTO）
     * @updated 2025-11-10 - 使用强类型 DTO 替代 Map<String, Object> 😎
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取系统概览统计", description = "获取系统核心数据概览（仅管理员）")
    public ApiResponse<SystemOverviewDTO> getSystemOverview() {
        log.info("📊 [管理端统计] 获取系统概览统计");

        // 获取系统概览统计（强类型 DTO）
        SystemOverviewDTO overview = statisticsService.getSystemOverview();

        log.info("✅ [管理端统计] 系统概览统计成功");
        return ApiResponse.success(overview);
    }

    /**
     * 📈 获取趋势统计数据
     *
     * GET /api/admin/statistics/trend?days=30
     *
     * 返回数据：
     * - dates: 日期数组
     * - userCounts: 每日新增用户数
     * - goodsCounts: 每日新增物品数
     * - orderCounts: 每日新增订单数
     *
     * @param days 统计天数（默认7天）
     * @return 趋势统计数据
     */
    @GetMapping("/trend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取趋势统计", description = "获取用户、物品、订单的趋势数据（仅管理员）")
    public ApiResponse<Map<String, Object>> getTrend(
            @Parameter(description = "统计天数", example = "30")
            @RequestParam(defaultValue = "7") int days) {
        log.info("📈 [管理端统计] 获取趋势数据, days={}", days);

        Map<String, Object> trendData = statisticsService.getTrendData(days);

        log.info("✅ [管理端统计] 趋势数据获取成功");
        return ApiResponse.success(trendData);
    }

    /**
     * 💰 获取收入趋势（按月统计）
     *
     * GET /api/admin/statistics/revenue?months=12
     *
     * 返回数据：
     * - months: 月份数组
     * - revenues: 每月收入
     *
     * @param months 统计月数（默认12个月）
     * @return 收入趋势数据
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取收入趋势", description = "获取按月统计的收入趋势（仅管理员）")
    public ApiResponse<Map<String, Object>> getRevenueTrend(
            @Parameter(description = "统计月数", example = "12")
            @RequestParam(defaultValue = "12") int months) {
        log.info("💰 [管理端统计] 获取收入趋势, months={}", months);

        Map<String, Object> revenueData = statisticsService.getRevenueByMonth(months);

        log.info("✅ [管理端统计] 收入趋势获取成功");
        return ApiResponse.success(revenueData);
    }

    /**
     * 🏆 获取热门商品排行榜
     *
     * GET /api/admin/statistics/top-goods?limit=10
     *
     * 返回数据：
     * - id: 商品ID
     * - title: 商品标题
     * - viewCount: 浏览次数
     * - favoriteCount: 收藏次数
     *
     * @param limit 返回数量（默认10个）
     * @return 热门商品列表
     */
    @GetMapping("/top-goods")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取热门商品排行", description = "获取浏览量最高的商品排行榜（仅管理员）")
    public ApiResponse<List<Map<String, Object>>> getTopGoods(
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        log.info("🏆 [管理端统计] 获取热门商品, limit={}", limit);

        List<Map<String, Object>> topGoods = statisticsService.getTopGoods(limit);

        log.info("✅ [管理端统计] 热门商品获取成功, count={}", topGoods.size());
        return ApiResponse.success(topGoods);
    }

    /**
     * 👥 获取活跃用户排行榜
     *
     * GET /api/admin/statistics/top-users?limit=10
     *
     * 返回数据：
     * - userId: 用户ID
     * - username: 用户名
     * - goodsCount: 发布物品数
     *
     * @param limit 返回数量（默认10个）
     * @return 活跃用户列表
     */
    @GetMapping("/top-users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取活跃用户排行", description = "获取发布物品最多的用户排行榜（仅管理员）")
    public ApiResponse<List<Map<String, Object>>> getTopUsers(
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        log.info("👥 [管理端统计] 获取活跃用户, limit={}", limit);

        List<Map<String, Object>> topUsers = statisticsService.getTopUsers(limit);

        log.info("✅ [管理端统计] 活跃用户获取成功, count={}", topUsers.size());
        return ApiResponse.success(topUsers);
    }

    /**
     * 📂 获取分类统计
     *
     * GET /api/admin/statistics/categories
     *
     * 返回数据：
     * - Key: 分类名称
     * - Value: 该分类下的物品数量
     *
     * @return 分类统计数据
     */
    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取分类统计", description = "获取各分类下的物品数量统计（仅管理员）")
    public ApiResponse<Map<String, Long>> getCategoryStatistics() {
        log.info("📂 [管理端统计] 获取分类统计");

        Map<String, Long> categoryStats = statisticsService.getCategoryStatistics();

        log.info("✅ [管理端统计] 分类统计获取成功, categories={}", categoryStats.size());
        return ApiResponse.success(categoryStats);
    }

    /**
     * 📅 获取今日统计
     *
     * GET /api/admin/statistics/today
     *
     * 返回数据：
     * - newUsers: 今日新增用户
     * - newGoods: 今日新增物品
     * - newOrders: 今日新增订单
     * - todayRevenue: 今日收入
     *
     * @return 今日统计数据
     */
    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取今日统计", description = "获取今日核心数据统计（仅管理员）")
    public ApiResponse<Map<String, Object>> getTodayStatistics() {
        log.info("📅 [管理端统计] 获取今日统计");

        Map<String, Object> todayStats = statisticsService.getTodayStatistics();

        log.info("✅ [管理端统计] 今日统计获取成功");
        return ApiResponse.success(todayStats);
    }

    /**
     * 📊 获取订单统计（增强版）
     *
     * GET /api/admin/statistics/orders
     *
     * 返回数据：
     * - 总体统计（总订单数、各状态订单数）
     * - 金额统计（总金额、已完成金额、平均金额）
     * - 比率统计（完成率、取消率、退款率）
     * - 按状态统计、按支付方式统计
     * - 今日统计
     *
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @return 订单统计数据
     */
    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取订单统计", description = "获取订单详细统计数据（总数、金额、比率、今日统计等）")
    public ApiResponse<OrderStatisticsDTO> getOrderStatistics(
            @Parameter(description = "开始日期（格式：yyyy-MM-dd）", example = "2025-01-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（格式：yyyy-MM-dd）", example = "2025-12-31")
            @RequestParam(required = false) String endDate) {
        log.info("📊 [管理端统计] 获取订单统计, startDate={}, endDate={}", startDate, endDate);

        OrderStatisticsDTO statistics = statisticsService.getOrderStatisticsEnhanced(startDate, endDate);

        log.info("✅ [管理端统计] 订单统计获取成功");
        return ApiResponse.success(statistics);
    }

    /**
     * 💰 获取退款统计
     *
     * GET /api/admin/statistics/refunds
     *
     * 返回数据：
     * - 总体统计（总退款数、各状态退款数）
     * - 金额统计（总金额、已完成金额、处理中金额）
     * - 比率统计（批准率、成功率、失败率）
     * - 按状态统计、按渠道统计
     * - 今日统计
     * - 平均处理时间
     *
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @return 退款统计数据
     */
    @GetMapping("/refunds")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取退款统计", description = "获取退款详细统计数据（总数、金额、比率、平均处理时间等）")
    public ApiResponse<RefundStatisticsDTO> getRefundStatistics(
            @Parameter(description = "开始日期（格式：yyyy-MM-dd）", example = "2025-01-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（格式：yyyy-MM-dd）", example = "2025-12-31")
            @RequestParam(required = false) String endDate) {
        log.info("💰 [管理端统计] 获取退款统计, startDate={}, endDate={}", startDate, endDate);

        RefundStatisticsDTO statistics = statisticsService.getRefundStatistics(startDate, endDate);

        log.info("✅ [管理端统计] 退款统计获取成功");
        return ApiResponse.success(statistics);
    }
}
