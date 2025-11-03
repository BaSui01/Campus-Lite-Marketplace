package com.campus.marketplace.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评价统计 DTO
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatisticsDTO {

    /**
     * 总评价数
     */
    private Long totalCount;

    /**
     * 好评数
     */
    private Long positiveCount;

    /**
     * 中评数
     */
    private Long neutralCount;

    /**
     * 差评数
     */
    private Long negativeCount;

    /**
     * 好评率
     */
    private Double positiveRate;

    /**
     * 平均评分
     */
    private Double avgRating;

    /**
     * 商品质量评分
     */
    private Double qualityScore;

    /**
     * 服务态度评分
     */
    private Double serviceScore;

    /**
     * 物流速度评分
     */
    private Double logisticsScore;
}
