package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 评价点赞数据访问接口
 *
 * @author BaSui 😎 - 点赞互动，评价更有趣！
 * @since 2025-11-03
 */
@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    /**
     * 根据评价ID和用户ID查询点赞记录
     *
     * @param reviewId 评价ID
     * @param userId 用户ID
     * @return 点赞记录（Optional）
     */
    Optional<ReviewLike> findByReviewIdAndUserId(Long reviewId, Long userId);

    /**
     * 检查用户是否点赞过评价
     *
     * @param reviewId 评价ID
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);

    /**
     * 检查用户是否有效点赞（未取消）
     *
     * @param reviewId 评价ID
     * @param userId 用户ID
     * @param isActive 是否有效
     * @return 是否存在
     */
    boolean existsByReviewIdAndUserIdAndIsActive(Long reviewId, Long userId, Boolean isActive);

    /**
     * 统计评价的所有点赞数量（包括软删除）
     *
     * @param reviewId 评价ID
     * @return 点赞数量
     */
    long countByReviewId(Long reviewId);

    /**
     * 统计评价的有效点赞数量
     *
     * @param reviewId 评价ID
     * @param isActive 是否有效
     * @return 点赞数量
     */
    long countByReviewIdAndIsActive(Long reviewId, Boolean isActive);

    /**
     * 查询评价的所有有效点赞
     *
     * @param reviewId 评价ID
     * @param isActive 是否有效
     * @return 点赞列表
     */
    List<ReviewLike> findByReviewIdAndIsActive(Long reviewId, Boolean isActive);

    /**
     * 查询用户点赞过的评价列表
     *
     * @param userId 用户ID
     * @param isActive 是否有效
     * @return 点赞列表
     */
    List<ReviewLike> findByUserIdAndIsActive(Long userId, Boolean isActive);

    /**
     * 删除评价的所有点赞
     *
     * @param reviewId 评价ID
     */
    void deleteByReviewId(Long reviewId);

    /**
     * 批量更新点赞状态（点赞/取消点赞）
     *
     * @param reviewId 评价ID
     * @param userId 用户ID
     * @param isActive 是否有效
     */
    @Modifying
    @Query("UPDATE ReviewLike rl SET rl.isActive = :isActive WHERE rl.reviewId = :reviewId AND rl.userId = :userId")
    void updateLikeStatus(@Param("reviewId") Long reviewId, @Param("userId") Long userId, @Param("isActive") Boolean isActive);
}
