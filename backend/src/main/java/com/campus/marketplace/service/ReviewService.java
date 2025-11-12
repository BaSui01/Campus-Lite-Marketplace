package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateReviewRequest;
import com.campus.marketplace.common.entity.Review;
import org.springframework.data.domain.Page;

/**
 * 评价服务接口
 *
 * @author BaSui 😎
 * @since 2025-11-08
 */
public interface ReviewService {

    /**
     * 创建评价
     */
    Long createReview(CreateReviewRequest request, Long buyerId);

    /**
     * 获取我的评价列表
     */
    Page<Review> getMyReviews(Long userId, int page, int size);

    /**
     * 获取商品评价列表（支持评分筛选和排序）
     * 
     * @param goodsId 商品ID
     * @param page 页码
     * @param size 每页数量
     * @param rating 评分筛选（可选，1-5星）
     * @param sortBy 排序方式（time=时间倒序, helpful=点赞数倒序）
     * @return 评价分页列表
     */
    Page<Review> getGoodsReviews(Long goodsId, int page, int size, Integer rating, String sortBy, Boolean hasImages, String group);

    /**
     * 删除评价
     */
    void deleteReview(Long reviewId, Long userId);

    /**
     * 获取待审核评价列表（管理员）
     */
    Page<Review> getPendingReviews(int page, int size);

    /**
     * 审核评价（管理员）
     */
    void auditReview(Long reviewId, boolean approved, String reason);
}
