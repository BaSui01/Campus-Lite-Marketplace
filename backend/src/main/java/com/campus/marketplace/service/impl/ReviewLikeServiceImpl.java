package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.Review;
import com.campus.marketplace.common.entity.ReviewLike;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.ReviewLikeRepository;
import com.campus.marketplace.repository.ReviewRepository;
import com.campus.marketplace.service.ReviewLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 评价点赞服务实现
 *
 * Spec #7：点赞功能，支持点赞/取消点赞（软删除模式）
 *
 * @author BaSui 😎 - 点赞一键搞定，取消也不留痕迹（软删除大法好）！
 * @since 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewLikeServiceImpl implements ReviewLikeService {

    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public ReviewLike likeReview(Long reviewId, Long userId) {
        // 验证参数
        validateLikeParams(reviewId, userId);

        // 检查评价是否存在
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "评价不存在"));

        // 检查是否已点赞过
        Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId);

        ReviewLike like;
        if (existingLike.isPresent()) {
            like = existingLike.get();
            if (Boolean.TRUE.equals(like.getIsActive())) {
                log.debug("用户{}已点赞过评价{}，无需重复点赞", userId, reviewId);
                return like;
            }

            // 恢复点赞（软删除恢复）
            like.setIsActive(true);
            reviewLikeRepository.save(like);
            log.info("恢复点赞：评价ID={}，用户ID={}", reviewId, userId);
        } else {
            // 创建新点赞
            like = ReviewLike.builder()
                    .reviewId(reviewId)
                    .userId(userId)
                    .isActive(true)
                    .build();
            reviewLikeRepository.save(like);
            log.info("创建点赞：评价ID={}，用户ID={}", reviewId, userId);
        }

        // 更新评价的点赞数量
        updateReviewLikeCount(reviewId);

        return like;
    }

    @Override
    @Transactional
    public void unlikeReview(Long reviewId, Long userId) {
        // 验证参数
        validateLikeParams(reviewId, userId);

        // 检查是否已点赞
        ReviewLike like = reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "未找到点赞记录"));

        if (Boolean.FALSE.equals(like.getIsActive())) {
            log.debug("用户{}已取消点赞评价{}，无需重复操作", userId, reviewId);
            return;
        }

        // 软删除（设置 isActive = false）
        like.setIsActive(false);
        reviewLikeRepository.save(like);

        // 更新评价的点赞数量
        updateReviewLikeCount(reviewId);

        log.info("取消点赞：评价ID={}，用户ID={}", reviewId, userId);
    }

    @Override
    @Transactional
    public boolean toggleLike(Long reviewId, Long userId) {
        // 验证参数
        validateLikeParams(reviewId, userId);

        // 检查当前点赞状态
        boolean currentlyLiked = hasLiked(reviewId, userId);

        if (currentlyLiked) {
            unlikeReview(reviewId, userId);
            log.info("切换点赞状态：取消点赞 - 评价ID={}，用户ID={}", reviewId, userId);
            return false;
        } else {
            likeReview(reviewId, userId);
            log.info("切换点赞状态：点赞 - 评价ID={}，用户ID={}", reviewId, userId);
            return true;
        }
    }

    @Override
    public boolean hasLiked(Long reviewId, Long userId) {
        return reviewLikeRepository.existsByReviewIdAndUserIdAndIsActive(reviewId, userId, true);
    }

    @Override
    public List<ReviewLike> getReviewLikes(Long reviewId) {
        return reviewLikeRepository.findByReviewIdAndIsActive(reviewId, true);
    }

    @Override
    public List<ReviewLike> getUserLikes(Long userId) {
        return reviewLikeRepository.findByUserIdAndIsActive(userId, true);
    }

    @Override
    public long countReviewLikes(Long reviewId) {
        return reviewLikeRepository.countByReviewIdAndIsActive(reviewId, true);
    }

    @Override
    @Transactional
    public void deleteAllLikesByReviewId(Long reviewId) {
        long count = reviewLikeRepository.countByReviewId(reviewId);

        if (count == 0) {
            log.debug("评价{}没有点赞记录，无需删除", reviewId);
            return;
        }

        reviewLikeRepository.deleteByReviewId(reviewId);

        // 更新评价的点赞数量
        updateReviewLikeCount(reviewId);

        log.info("删除评价{}的所有点赞成功，共{}条", reviewId, count);
    }

    /**
     * 验证点赞参数
     */
    private void validateLikeParams(Long reviewId, Long userId) {
        if (reviewId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "评价ID不能为空");
        }

        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }
    }

    /**
     * 更新评价的点赞数量
     */
    private void updateReviewLikeCount(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "评价不存在"));

        long newCount = countReviewLikes(reviewId);
        review.setLikeCount((int) newCount);
        reviewRepository.save(review);

        log.debug("更新评价{}的点赞数量：{}", reviewId, newCount);
    }
}
