package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.MerchantDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商家数据看板控制器
 *
 * 提供商家数据统计与分析接口：
 * - 📊 今日数据概览
 * - 📈 销售趋势分析
 * - 🏆 商品排行榜
 * - 👥 访客分析
 *
 * @author BaSui 😎
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/merchant/dashboard")
@RequiredArgsConstructor
@Tag(name = "商家数据看板", description = "商家数据统计与分析接口")
public class MerchantDashboardController {

    private final MerchantDashboardService merchantDashboardService;

    @GetMapping("/today")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取今日数据概览", description = "获取商家今日销售、订单、访客等核心数据")
    public ApiResponse<Map<String, Object>> getTodayOverview() {
        Long merchantId = SecurityUtil.getCurrentUserId();
        log.debug("商家 {} 查询今日数据概览", merchantId);
        
        Map<String, Object> overview = merchantDashboardService.getTodayOverview(merchantId);
        return ApiResponse.success(overview);
    }

    @GetMapping("/sales-trend")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取销售趋势", description = "获取商家近N天的销售趋势数据")
    public ApiResponse<Map<String, Object>> getSalesTrend(
            @Parameter(description = "统计天数", example = "7")
            @RequestParam(defaultValue = "7") int days
    ) {
        if (days <= 0 || days > 365) {
            return ApiResponse.error(400, "天数必须在 1-365 之间");
        }
        
        Long merchantId = SecurityUtil.getCurrentUserId();
        log.debug("商家 {} 查询近 {} 天销售趋势", merchantId, days);
        
        Map<String, Object> trend = merchantDashboardService.getSalesTrend(merchantId, days);
        return ApiResponse.success(trend);
    }

    @GetMapping("/goods-ranking")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取商品排行榜", description = "获取商家热销商品排行榜（Top 10）")
    public ApiResponse<Map<String, Object>> getGoodsRanking(
            @Parameter(description = "排行榜数量", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (limit <= 0 || limit > 100) {
            return ApiResponse.error(400, "数量必须在 1-100 之间");
        }
        
        Long merchantId = SecurityUtil.getCurrentUserId();
        log.debug("商家 {} 查询商品排行榜 Top {}", merchantId, limit);
        
        Map<String, Object> ranking = merchantDashboardService.getGoodsRanking(merchantId);
        return ApiResponse.success(ranking);
    }

    @GetMapping("/visitor-analysis")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取访客分析", description = "获取商家访客统计与来源分析数据")
    public ApiResponse<Map<String, Object>> getVisitorAnalysis(
            @Parameter(description = "统计天数", example = "7")
            @RequestParam(defaultValue = "7") int days
    ) {
        if (days <= 0 || days > 365) {
            return ApiResponse.error(400, "天数必须在 1-365 之间");
        }
        
        Long merchantId = SecurityUtil.getCurrentUserId();
        log.debug("商家 {} 查询近 {} 天访客分析", merchantId, days);
        
        Map<String, Object> analysis = merchantDashboardService.getVisitorAnalysis(merchantId);
        return ApiResponse.success(analysis);
    }
}
