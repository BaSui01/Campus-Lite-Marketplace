package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    /**
     * 热门榜单（按校区）
     */
    @GetMapping("/hot")
    public ApiResponse<List<GoodsResponse>> hot(@RequestParam(required = false) Long campusId,
                                                @RequestParam(defaultValue = "20") int size) {
        log.info("获取热门榜单: campusId={}, size={}", campusId, size);
        return ApiResponse.success(recommendService.getHotList(campusId, size));
    }

    /**
     * 个性化推荐（需登录）
     */
    @GetMapping("/personal")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN','SUPER_ADMIN')")
    public ApiResponse<List<GoodsResponse>> personal(@RequestParam(defaultValue = "20") int size) {
        log.info("获取个性化推荐: size={}", size);
        return ApiResponse.success(recommendService.getPersonalRecommendations(size));
    }

    /**
     * 手动刷新热门榜单（管理员）
     */
    @PostMapping("/admin/hot/refresh")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<Void> refreshHot(@RequestParam(required = false) Long campusId,
                                        @RequestParam(defaultValue = "50") int topN) {
        log.info("手动刷新热门榜单: campusId={}, topN={}", campusId, topN);
        recommendService.refreshHotRanking(campusId, topN);
        return ApiResponse.success(null);
    }
}
