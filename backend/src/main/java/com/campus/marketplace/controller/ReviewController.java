package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateReviewRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.Review;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 评价管理Controller
 *
 * @author BaSui 😎
 * @since 2025-11-08
 */
@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "评价管理", description = "评价创建、查询、删除接口")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "创建评价", description = "买家对订单进行评价")
    public ApiResponse<Long> createReview(@Valid @RequestBody CreateReviewRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long reviewId = reviewService.createReview(request, userId);
        return ApiResponse.success(reviewId);
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的评价列表", description = "查询当前用户发布的所有评价")
    public ApiResponse<Page<Review>> getMyReviews(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<Review> reviews = reviewService.getMyReviews(userId, page, size);
        return ApiResponse.success(reviews);
    }

    @GetMapping("/goods/{goodsId}")
    @Operation(summary = "获取商品评价列表（旧路由）", description = "查询指定商品的所有评价，支持评分筛选和排序。推荐使用 GET /goods/{goodsId}/reviews")
    public ApiResponse<Page<Review>> getGoodsReviewsOld(
            @Parameter(description = "商品ID") @PathVariable Long goodsId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "评分筛选（1-5星）") @RequestParam(required = false) Integer rating,
            @Parameter(description = "排序方式（time/like/image_first）") @RequestParam(defaultValue = "time") String sortBy,
            @Parameter(description = "只看有图") @RequestParam(required = false) Boolean hasImages,
            @Parameter(description = "评分分组（positive=4-5，neutral=3，negative=1-2）") @RequestParam(required = false) String group
    ) {
        Page<Review> reviews = reviewService.getGoodsReviews(goodsId, page, size, rating, sortBy, hasImages, group);
        return ApiResponse.success(reviews);
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "删除评价", description = "删除自己发布的评价")
    public ApiResponse<Void> deleteReview(@Parameter(description = "评价ID") @PathVariable Long reviewId) {
        Long userId = SecurityUtil.getCurrentUserId();
        reviewService.deleteReview(reviewId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/admin/pending")
    @Operation(summary = "获取待审核评价列表", description = "管理员查询待审核评价")
    public ApiResponse<Page<Review>> getPendingReviews(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size
    ) {
        Page<Review> reviews = reviewService.getPendingReviews(page, size);
        return ApiResponse.success(reviews);
    }

    @PostMapping("/{reviewId}/audit")
    @Operation(summary = "审核评价", description = "管理员审核评价")
    public ApiResponse<Void> auditReview(
            @Parameter(description = "评价ID") @PathVariable Long reviewId,
            @Parameter(description = "是否通过") @RequestParam boolean approved,
            @Parameter(description = "审核意见") @RequestParam(required = false) String reason
    ) {
        reviewService.auditReview(reviewId, approved, reason);
        return ApiResponse.success();
    }
}
