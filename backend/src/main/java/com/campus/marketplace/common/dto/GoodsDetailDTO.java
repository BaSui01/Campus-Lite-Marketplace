package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品详情 DTO
 * 
 * 聚合商品详情页所需的所有数据
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsDetailDTO {

    /**
     * 商品基本信息
     */
    private GoodsResponse goods;

    /**
     * 卖家信息
     */
    private SellerInfoDTO seller;

    /**
     * 相似商品推荐
     */
    private List<GoodsResponse> similarGoods;

    /**
     * 评价统计
     */
    private ReviewStatisticsDTO reviewStatistics;

    /**
     * 最新评价（前3条）
     */
    private List<Review> recentReviews;

    /**
     * 是否已收藏
     */
    private Boolean isFavorited;

    /**
     * 浏览次数
     */
    private Long viewCount;
}
