package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.ReviewStatisticsDTO;

/**
 * 评价统计服务接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface ReviewStatisticsService {

    /**
     * 获取商品的评价统计
     * 
     * @param goodsId 商品ID（通过订单关联）
     * @return 评价统计
     */
    ReviewStatisticsDTO getGoodsReviewStatistics(Long goodsId);

    /**
     * 获取卖家的评价统计
     * 
     * @param sellerId 卖家ID
     * @return 评价统计
     */
    ReviewStatisticsDTO getSellerReviewStatistics(Long sellerId);

    /**
     * 计算好评率
     * 
     * @param sellerId 卖家ID
     * @return 好评率（0.0-1.0）
     */
    Double calculatePositiveRate(Long sellerId);

    /**
     * 计算三维评分平均值
     * 
     * @param sellerId 卖家ID
     * @return [质量评分, 服务评分, 物流评分]
     */
    Double[] calculateAverageScores(Long sellerId);
}
