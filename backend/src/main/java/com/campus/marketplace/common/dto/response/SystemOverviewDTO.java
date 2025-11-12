package com.campus.marketplace.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 📊 BaSui 的系统概览统计响应 - 强类型 DTO！😎
 *
 * 功能：
 * - 系统核心数据统计
 * - 今日新增数据
 * - 活跃用户统计
 * - 待审核商品统计
 *
 * @author BaSui 😎
 * @date 2025-11-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统概览统计数据")
public class SystemOverviewDTO {

    // ==================== 总体统计 ====================

    @Schema(description = "总用户数", example = "1250")
    private Long totalUsers;

    @Schema(description = "总商品数", example = "3680")
    private Long totalGoods;

    @Schema(description = "总订单数", example = "5420")
    private Long totalOrders;

    @Schema(description = "总收入（元）", example = "125000.50")
    private BigDecimal totalRevenue;

    // ==================== 今日统计 ====================

    @Schema(description = "今日新增用户", example = "25")
    private Long todayNewUsers;

    @Schema(description = "今日新增商品", example = "48")
    private Long todayNewGoods;

    @Schema(description = "今日新增订单", example = "32")
    private Long todayNewOrders;

    // ==================== 活跃统计 ====================

    @Schema(description = "活跃用户数（30天内）", example = "850")
    private Long activeUsers;

    @Schema(description = "待审核商品数", example = "15")
    private Long pendingGoods;
}
