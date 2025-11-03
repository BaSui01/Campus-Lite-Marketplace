package com.campus.marketplace.common.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

/**
 * 纠纷统计DTO
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总纠纷数
     */
    private Long totalDisputes;

    /**
     * 协商中数量
     */
    private Long negotiatingCount;

    /**
     * 待仲裁数量
     */
    private Long pendingArbitrationCount;

    /**
     * 仲裁中数量
     */
    private Long arbitratingCount;

    /**
     * 已完成数量
     */
    private Long completedCount;

    /**
     * 已关闭数量
     */
    private Long closedCount;

    /**
     * 按纠纷类型统计
     * Key: DisputeType.name(), Value: count
     */
    private Map<String, Long> disputeTypeDistribution;

    /**
     * 按仲裁结果统计
     * Key: ArbitrationResult.name(), Value: count
     */
    private Map<String, Long> arbitrationResultDistribution;

    /**
     * 平均协商时长（小时）
     */
    private Double avgNegotiationDuration;

    /**
     * 平均仲裁处理时长（小时）
     */
    private Double avgArbitrationDuration;

    /**
     * 协商成功率（%）
     */
    private Double negotiationSuccessRate;

    /**
     * 本月新增纠纷数
     */
    private Long thisMonthNewCount;

    /**
     * 本月解决纠纷数
     */
    private Long thisMonthResolvedCount;
}
