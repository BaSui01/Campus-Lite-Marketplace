package com.campus.marketplace.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 订单统计DTO
 * 
 * 管理员查看订单统计数据
 * 
 * @author BaSui 😎
 * @date 2025-11-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单统计数据")
public class OrderStatisticsDTO {

    // ==================== 总体统计 ====================
    
    @Schema(description = "总订单数", example = "5420")
    private Long totalOrders;
    
    @Schema(description = "待支付订单数", example = "128")
    private Long pendingPaymentOrders;
    
    @Schema(description = "已支付订单数", example = "3890")
    private Long paidOrders;
    
    @Schema(description = "已完成订单数", example = "3200")
    private Long completedOrders;
    
    @Schema(description = "已取消订单数", example = "450")
    private Long cancelledOrders;
    
    @Schema(description = "退款中订单数", example = "35")
    private Long refundingOrders;
    
    @Schema(description = "已退款订单数", example = "180")
    private Long refundedOrders;
    
    // ==================== 金额统计 ====================
    
    @Schema(description = "总订单金额（元）", example = "542000.00")
    private BigDecimal totalAmount;
    
    @Schema(description = "已完成订单金额（元）", example = "480000.00")
    private BigDecimal completedAmount;
    
    @Schema(description = "已退款金额（元）", example = "18000.00")
    private BigDecimal refundedAmount;
    
    @Schema(description = "平均订单金额（元）", example = "100.00")
    private BigDecimal averageAmount;
    
    // ==================== 比率统计 ====================
    
    @Schema(description = "订单完成率（%）", example = "85.5")
    private Double completionRate;
    
    @Schema(description = "订单取消率（%）", example = "8.3")
    private Double cancellationRate;
    
    @Schema(description = "订单退款率（%）", example = "3.3")
    private Double refundRate;
    
    // ==================== 按状态统计 ====================
    
    @Schema(description = "按订单状态统计（状态 -> 数量）")
    private Map<String, Long> ordersByStatus;
    
    // ==================== 按支付方式统计 ====================
    
    @Schema(description = "按支付方式统计金额（支付方式 -> 金额）")
    private Map<String, BigDecimal> amountByPaymentMethod;
    
    @Schema(description = "按支付方式统计数量（支付方式 -> 数量）")
    private Map<String, Long> countByPaymentMethod;
    
    // ==================== 今日统计 ====================
    
    @Schema(description = "今日新增订单数", example = "32")
    private Long todayNewOrders;
    
    @Schema(description = "今日订单金额（元）", example = "3200.00")
    private BigDecimal todayAmount;
    
    @Schema(description = "今日完成订单数", example = "28")
    private Long todayCompletedOrders;
}
