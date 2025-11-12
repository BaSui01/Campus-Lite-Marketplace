package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.ReviewStatisticsDTO;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.ReviewStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品评价公开查询 Controller（无 /reviews 前缀）
 *
 * 对应前端：GET /goods/{goodsId}/reviews 与 /goods/{goodsId}/reviews/stats
 *
 * 注意：项目中已有 ReviewController（前缀 /reviews），为兼容前端期望路径，
 * 这里单独暴露以 /goods 开头的查询路由。
 *
 * @author BaSui
 * @since 2025-11-12
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "商品评价公开查询", description = "按商品ID查询评价统计信息")
public class GoodsReviewController {

    private final ReviewStatisticsService reviewStatisticsService;

    @GetMapping("/goods/{goodsId}/reviews/stats")
    @Operation(summary = "获取商品评价统计", description = "返回总评数、好评率、平均分以及三维评分")
    public ApiResponse<ReviewStatisticsDTO> getGoodsReviewStatistics(
            @Parameter(description = "商品ID") @PathVariable Long goodsId
    ) {
        ReviewStatisticsDTO stats = reviewStatisticsService.getGoodsReviewStatistics(goodsId);
        return ApiResponse.success(stats);
    }
}
