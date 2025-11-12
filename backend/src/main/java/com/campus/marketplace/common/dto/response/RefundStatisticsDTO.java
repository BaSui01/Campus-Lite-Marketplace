package com.campus.marketplace.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 退款统计DTO
 * 
 * 管理员查看退款统计数据
 * 
 * @author BaSui 😎
 * @date 2025-11-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "退款统计数据")
public class RefundStatisticsDTO {

    // ==================== 总体统计 ====================
    
    @Schema(description = "总退款申请数", example = "215")
    private Long totalRefunds;
    
    @Schema(description = "待审核退款数", example = "12")
    private Long appliedRefunds;
    
    @Schema(description = "已批准退款数", example = "180")
    private Long approvedRefunds;
    
    @Schema(description = "已拒绝退款数", example = "23")
    private Long rejectedRefunds;
    
    @Schema(description = "处理中退款数", example = "5")
    private Long processingRefunds;
    
    @Schema(description = "已完成退款数", example = "175")
    private Long completedRefunds;
    
    @Schema(description = "失败退款数", example = "5")
    private Long failedRefunds;
    
    // ==================== 金额统计 ====================
    
    @Schema(description = "总退款金额（元）", example = "18000.00")
    private BigDecimal totalAmount;
    
    @Schema(description = "已完成退款金额（元）", example = "17500.00")
    private BigDecimal completedAmount;
    
    @Schema(description = "处理中退款金额（元）", example = "500.00")
    private BigDecimal processingAmount;
    
    @Schema(description = "平均退款金额（元）", example = "83.72")
    private BigDecimal averageAmount;
    
    // ==================== 比率统计 ====================
    
    @Schema(description = "退款批准率（%）", example = "88.4")
    private Double approvalRate;
    
    @Schema(description = "退款成功率（%）", example = "97.2")
    private Double successRate;
    
    @Schema(description = "退款失败率（%）", example = "2.8")
    private Double failureRate;
    
    // ==================== 按状态统计 ====================
    
    @Schema(description = "按退款状态统计（状态 -> 数量）")
    private Map<String, Long> refundsByStatus;
    
    // ==================== 按退款渠道统计 ====================
    
    @Schema(description = "按退款渠道统计金额（渠道 -> 金额）")
    private Map<String, BigDecimal> amountByChannel;
    
    @Schema(description = "按退款渠道统计数量（渠道 -> 数量）")
    private Map<String, Long> countByChannel;
    
    // ==================== 今日统计 ====================
    
    @Schema(description = "今日新增退款数", example = "8")
    private Long todayNewRefunds;
    
    @Schema(description = "今日退款金额（元）", example = "650.00")
    private BigDecimal todayAmount;
    
    @Schema(description = "今日完成退款数", example = "6")
    private Long todayCompletedRefunds;
    
    // ==================== 平均处理时间 ====================
    
    @Schema(description = "平均审核时间（小时）", example = "2.5")
    private Double avgReviewTime;
    
    @Schema(description = "平均完成时间（小时）", example = "24.5")
    private Double avgCompletionTime;
}
