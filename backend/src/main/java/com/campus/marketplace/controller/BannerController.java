package com.campus.marketplace.controller;

import com.campus.marketplace.common.entity.Banner;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图控制器
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Slf4j
@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
@Tag(name = "轮播图管理", description = "首页轮播图相关接口")
public class BannerController {

    private final BannerService bannerService;

    /**
     * 获取启用的轮播图列表（前台使用）
     * 
     * GET /api/banners/active
     * 
     * @return 轮播图列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取启用的轮播图", description = "获取当前启用且在有效期内的轮播图列表，按排序顺序返回")
    public ApiResponse<List<Banner>> getActiveBanners() {
        log.info("获取启用的轮播图列表");
        List<Banner> banners = bannerService.getActiveBanners();
        return ApiResponse.success(banners);
    }

    /**
     * 记录轮播图点击
     * 
     * POST /api/banners/{id}/click
     * 
     * @param id 轮播图 ID
     * @return 成功响应
     */
    @PostMapping("/{id}/click")
    @Operation(summary = "记录轮播图点击", description = "用户点击轮播图时调用，用于统计点击次数")
    public ApiResponse<Void> recordClick(@PathVariable Long id) {
        log.info("记录轮播图点击: id={}", id);
        bannerService.recordClick(id);
        return ApiResponse.success(null);
    }

    /**
     * 记录轮播图展示
     * 
     * POST /api/banners/{id}/view
     * 
     * @param id 轮播图 ID
     * @return 成功响应
     */
    @PostMapping("/{id}/view")
    @Operation(summary = "记录轮播图展示", description = "轮播图展示时调用，用于统计展示次数")
    public ApiResponse<Void> recordView(@PathVariable Long id) {
        log.debug("记录轮播图展示: id={}", id);
        bannerService.recordView(id);
        return ApiResponse.success(null);
    }
}
