package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Recommend Controller
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
@Tag(name = "推荐", description = "热门榜与个性化推荐接口")
public class RecommendController {

    private final RecommendService recommendService;

        @GetMapping("/hot")
    @Operation(summary = "热门榜单", description = "按校区获取热门物品榜单")
    public ApiResponse<List<GoodsResponse>> hot(
            @Parameter(description = "校区ID", example = "1") @RequestParam(required = false) Long campusId,
            @Parameter(description = "返回数量", example = "20") @RequestParam(defaultValue = "20") int size) {
        log.info("获取热门榜单: campusId={}, size={}", campusId, size);
        return ApiResponse.success(recommendService.getHotList(campusId, size));
    }

        @GetMapping("/personal")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN','SUPER_ADMIN')")
    @Operation(summary = "个性化推荐", description = "根据用户行为推荐物品，需登录")
    public ApiResponse<List<GoodsResponse>> personal(
            @Parameter(description = "返回数量", example = "20") @RequestParam(defaultValue = "20") int size) {
        log.info("获取个性化推荐: size={}", size);
        return ApiResponse.success(recommendService.getPersonalRecommendations(size));
    }

        @PostMapping("/admin/hot/refresh")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "刷新热门榜单", description = "管理员手动刷新热门榜单缓存")
    public ApiResponse<Void> refreshHot(
            @Parameter(description = "校区ID", example = "1") @RequestParam(required = false) Long campusId,
            @Parameter(description = "取前N条", example = "50") @RequestParam(defaultValue = "50") int topN) {
        log.info("手动刷新热门榜单: campusId={}, topN={}", campusId, topN);
        recommendService.refreshHotRanking(campusId, topN);
        return ApiResponse.success(null);
    }
}
