package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreateReviewRequest;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.Review;
import com.campus.marketplace.common.enums.ReviewStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.ReviewRepository;
import com.campus.marketplace.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评价服务实现
 *
 * @author BaSui 😎
 * @since 2025-11-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Long createReview(CreateReviewRequest request, Long buyerId) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));

        if (reviewRepository.existsByOrderId(request.orderId())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "该订单已评价");
        }

        Review review = Review.builder()
                .orderId(request.orderId())
                .buyerId(buyerId)
                .sellerId(order.getSellerId())
                .rating(request.rating())
                .content(request.content())
                .qualityScore(request.qualityScore() != null ? request.qualityScore() : request.rating())
                .serviceScore(request.serviceScore() != null ? request.serviceScore() : request.rating())
                .deliveryScore(request.deliveryScore() != null ? request.deliveryScore() : request.rating())
                .isAnonymous(request.isAnonymous() != null ? request.isAnonymous() : false)
                .status(ReviewStatus.NORMAL)
                .build();

        review = reviewRepository.save(review);
        log.info("创建评价成功：reviewId={}, orderId={}", review.getId(), request.orderId());
        return review.getId();
    }

    @Override
    public Page<Review> getMyReviews(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findAll(pageRequest);
    }

    @Override
    public Page<Review> getGoodsReviews(Long goodsId, int page, int size, Integer rating, String sortBy, Boolean hasImages, String group) {
        // 确定排序字段（支持前端传来的 time/like/helpful）
        String sortField;
        if ("like".equalsIgnoreCase(sortBy) || "helpful".equalsIgnoreCase(sortBy)) {
            sortField = "likeCount"; // 按点赞数排序
        } else {
            // image_first 仍按时间排序，前端进行页内重排（有图优先）
            sortField = "createdAt"; // 默认按时间排序（time/image_first/其他值）
        }
        Sort.Direction sortDirection = Sort.Direction.DESC;

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        // 只看有图
        if (Boolean.TRUE.equals(hasImages)) {
            return reviewRepository.findHasImageByOrderGoodsIdAndStatus(goodsId, ReviewStatus.NORMAL, pageRequest);
        }

        // 评分分组（好评/中评/差评）
        if (group != null) {
            String g = group.toLowerCase();
            if ("positive".equals(g)) {
                return reviewRepository.findByOrderGoodsIdAndRatingBetweenAndStatus(goodsId, 4, 5, ReviewStatus.NORMAL, pageRequest);
            } else if ("neutral".equals(g)) {
                return reviewRepository.findByOrderGoodsIdAndRatingBetweenAndStatus(goodsId, 3, 3, ReviewStatus.NORMAL, pageRequest);
            } else if ("negative".equals(g)) {
                return reviewRepository.findByOrderGoodsIdAndRatingBetweenAndStatus(goodsId, 1, 2, ReviewStatus.NORMAL, pageRequest);
            }
        }

        // 如果有精确评分筛选，使用自定义查询
        if (rating != null) {
            return reviewRepository.findByOrderGoodsIdAndRatingAndStatus(goodsId, rating, ReviewStatus.NORMAL, pageRequest);
        }

        // 否则查询所有正常状态的评价
        return reviewRepository.findByOrderGoodsIdAndStatus(goodsId, ReviewStatus.NORMAL, pageRequest);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "评价不存在"));

        if (!review.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "无权删除该评价");
        }

        reviewRepository.delete(review);
        log.info("删除评价成功：reviewId={}", reviewId);
    }

    @Override
    public Page<Review> getPendingReviews(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByStatus(ReviewStatus.REPORTED, pageRequest);
    }

    @Override
    @Transactional
    public void auditReview(Long reviewId, boolean approved, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "评价不存在"));

        review.setStatus(approved ? ReviewStatus.NORMAL : ReviewStatus.HIDDEN);
        reviewRepository.save(review);
        log.info("审核评价成功：reviewId={}, approved={}, reason={}", reviewId, approved, reason);
    }
}
