package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.ReviewLikeService;
import com.campus.marketplace.common.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 评价点赞管理Controller
 *
 * Spec #7：点赞功能API（点赞/取消点赞/切换）
 *
 * @author BaSui 😎 - 点赞一键搞定，互动更有爱！
 * @since 2025-11-03
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "评价点赞管理", description = "评价点赞、取消点赞、状态查询接口")
public class ReviewLikeController {

    private final ReviewLikeService reviewLikeService;

    @PostMapping("/{reviewId}/like")
    @Operation(summary = "点赞评价", description = "为评价点赞，重复点赞无效")
    public ApiResponse<Void> likeReview(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        log.info("点赞评价：reviewId={}, userId={}", reviewId, currentUserId);
        reviewLikeService.likeReview(reviewId, currentUserId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{reviewId}/like")
    @Operation(summary = "取消点赞", description = "取消对评价的点赞")
    public ApiResponse<Void> unlikeReview(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        log.info("取消点赞：reviewId={}, userId={}", reviewId, currentUserId);
        reviewLikeService.unlikeReview(reviewId, currentUserId);
        return ApiResponse.success();
    }

    @PostMapping("/{reviewId}/like/toggle")
    @Operation(summary = "切换点赞状态", description = "已点赞则取消，未点赞则点赞")
    public ApiResponse<Boolean> toggleLike(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        log.info("切换点赞状态：reviewId={}, userId={}", reviewId, currentUserId);
        boolean liked = reviewLikeService.toggleLike(reviewId, currentUserId);
        return ApiResponse.success(liked);
    }

    @GetMapping("/{reviewId}/like/status")
    @Operation(summary = "查询点赞状态", description = "检查当前用户是否已点赞此评价")
    public ApiResponse<Boolean> checkLikeStatus(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        boolean liked = reviewLikeService.hasLiked(reviewId, currentUserId);
        return ApiResponse.success(liked);
    }

    @GetMapping("/{reviewId}/likes/count")
    @Operation(summary = "获取点赞数量", description = "返回评价的总点赞数量")
    public ApiResponse<Long> countReviewLikes(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId
    ) {
        long count = reviewLikeService.countReviewLikes(reviewId);
        return ApiResponse.success(count);
    }
}
