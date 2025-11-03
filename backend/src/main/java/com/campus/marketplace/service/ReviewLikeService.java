package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ReviewLike;

import java.util.List;

/**
 * 评价点赞服务接口
 *
 * Spec #7：点赞功能，支持点赞/取消点赞
 *
 * @author BaSui 😎 - 点赞互动，好评更有说服力！
 * @since 2025-11-03
 */
public interface ReviewLikeService {

    /**
     * 点赞评价
     * 如果已点赞过（软删除状态），则恢复点赞；否则创建新点赞
     *
     * @param reviewId 评价ID
     * @param userId 用户ID
     * @return 点赞实体
     */
    ReviewLike likeReview(Long reviewId, Long userId);

    /**
     * 取消点赞（软删除，isActive = false）
     *
     * @param reviewId 评价ID
     * @param userId 用户ID
     */
    void unlikeReview(Long reviewId, Long userId);

    /**
     * 切换点赞状态（已点赞则取消，未点赞则点赞）
     *
     * @param reviewId 评价ID
     * @param userId 用户ID
     * @return 点赞后的状态（true=已点赞，false=已取消）
     */
    boolean toggleLike(Long reviewId, Long userId);

    /**
     * 检查用户是否点赞过评价（有效点赞）
     *
     * @param reviewId 评价ID
     * @param userId 用户ID
     * @return 是否点赞
     */
    boolean hasLiked(Long reviewId, Long userId);

    /**
     * 获取评价的所有有效点赞
     *
     * @param reviewId 评价ID
     * @return 点赞列表
     */
    List<ReviewLike> getReviewLikes(Long reviewId);

    /**
     * 获取用户点赞过的评价列表
     *
     * @param userId 用户ID
     * @return 点赞列表
     */
    List<ReviewLike> getUserLikes(Long userId);

    /**
     * 统计评价的有效点赞数量
     *
     * @param reviewId 评价ID
     * @return 点赞数量
     */
    long countReviewLikes(Long reviewId);

    /**
     * 删除评价的所有点赞（物理删除）
     *
     * @param reviewId 评价ID
     */
    void deleteAllLikesByReviewId(Long reviewId);
}
