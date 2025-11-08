package com.campus.marketplace.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推荐统计DTO
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendStatisticsDTO {

    /**
     * 推荐总次数
     */
    private Long totalRecommendations;

    /**
     * 点击率
     */
    private Double clickRate;

    /**
     * 转化率
     */
    private Double conversionRate;

    /**
     * 平均评分
     */
    private Double avgScore;
}
