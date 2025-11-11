package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.MarketingCampaign;
import com.campus.marketplace.service.MarketingCampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 营销活动控制器
 *
 * 处理营销活动相关的 HTTP 请求（限时折扣、满减、秒杀等）
 *
 * @author BaSui 😎
 * @since 2025-11-04
 */
@Slf4j
@RestController
@RequestMapping("/marketing/campaigns")
@RequiredArgsConstructor
@Tag(name = "营销活动", description = "营销活动管理接口（限时折扣、满减、秒杀）")
public class MarketingCampaignController {

    private final MarketingCampaignService marketingCampaignService;

    /**
     * 创建营销活动
     */
    @PostMapping
    @PreAuthorize("hasRole('MERCHANT')")
    @Operation(summary = "创建营销活动", description = "商家创建营销活动（限时折扣/满减/秒杀）")
    public ApiResponse<MarketingCampaign> createCampaign(@RequestBody MarketingCampaign campaign) {
        log.info("创建营销活动: campaignName={}, campaignType={}", campaign.getCampaignName(), campaign.getCampaignType());
        MarketingCampaign created = marketingCampaignService.createCampaign(campaign);
        return ApiResponse.success(created);
    }

    /**
     * 获取我的活动列表（商家）
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('MERCHANT')")
    @Operation(summary = "获取我的活动列表", description = "商家查询自己创建的所有营销活动")
    public ApiResponse<List<MarketingCampaign>> getMyCompaigns() {
        log.info("查询商家的营销活动列表");
        List<MarketingCampaign> campaigns = marketingCampaignService.getMerchantCampaigns(null);
        return ApiResponse.success(campaigns);
    }

    /**
     * 获取指定商家的活动列表
     */
    @GetMapping("/merchant/{merchantId}")
    @Operation(summary = "获取商家活动列表", description = "查询指定商家的所有活动")
    public ApiResponse<List<MarketingCampaign>> getMerchantCampaigns(
            @Parameter(description = "商家ID", example = "1")
            @PathVariable Long merchantId) {
        log.info("查询商家{}的活动列表", merchantId);
        List<MarketingCampaign> campaigns = marketingCampaignService.getMerchantCampaigns(merchantId);
        return ApiResponse.success(campaigns);
    }

    /**
     * 获取进行中的活动列表
     */
    @GetMapping("/running")
    @Operation(summary = "获取进行中的活动", description = "查询所有正在进行中的营销活动")
    public ApiResponse<List<MarketingCampaign>> getRunningCampaigns() {
        log.info("查询进行中的活动列表");
        List<MarketingCampaign> campaigns = marketingCampaignService.getRunningCampaigns();
        return ApiResponse.success(campaigns);
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{campaignId}")
    @Operation(summary = "获取活动详情", description = "查询指定营销活动的详细信息")
    public ApiResponse<MarketingCampaign> getCampaignDetail(
            @Parameter(description = "活动ID", example = "1")
            @PathVariable Long campaignId) {
        log.info("查询活动详情: campaignId={}", campaignId);
        MarketingCampaign campaign = marketingCampaignService.getCampaignById(campaignId);
        return ApiResponse.success(campaign);
    }

    /**
     * 修改活动
     */
    @PutMapping("/{campaignId}")
    @PreAuthorize("hasRole('MERCHANT')")
    @Operation(summary = "修改活动", description = "商家修改营销活动信息")
    public ApiResponse<MarketingCampaign> updateCampaign(
            @Parameter(description = "活动ID", example = "1")
            @PathVariable Long campaignId,
            @RequestBody MarketingCampaign campaign) {
        log.info("修改营销活动: campaignId={}", campaignId);
        MarketingCampaign updated = marketingCampaignService.updateCampaign(campaignId, campaign);
        return ApiResponse.success(updated);
    }

    /**
     * 暂停活动
     */
    @PostMapping("/{campaignId}/pause")
    @PreAuthorize("hasRole('MERCHANT')")
    @Operation(summary = "暂停活动", description = "商家暂停正在进行的活动")
    public ApiResponse<Void> pauseCampaign(
            @Parameter(description = "活动ID", example = "1")
            @PathVariable Long campaignId) {
        log.info("暂停活动: campaignId={}", campaignId);
        marketingCampaignService.pauseCampaign(campaignId);
        return ApiResponse.success();
    }

    /**
     * 恢复活动
     */
    @PostMapping("/{campaignId}/resume")
    @PreAuthorize("hasRole('MERCHANT')")
    @Operation(summary = "恢复活动", description = "商家恢复已暂停的活动")
    public ApiResponse<Void> resumeCampaign(
            @Parameter(description = "活动ID", example = "1")
            @PathVariable Long campaignId) {
        log.info("恢复活动: campaignId={}", campaignId);
        marketingCampaignService.resumeCampaign(campaignId);
        return ApiResponse.success();
    }

    /**
     * 结束活动
     */
    @PostMapping("/{campaignId}/end")
    @PreAuthorize("hasRole('MERCHANT')")
    @Operation(summary = "结束活动", description = "商家手动结束活动")
    public ApiResponse<Void> endCampaign(
            @Parameter(description = "活动ID", example = "1")
            @PathVariable Long campaignId) {
        log.info("结束活动: campaignId={}", campaignId);
        marketingCampaignService.endCampaign(campaignId);
        return ApiResponse.success();
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{campaignId}")
    @PreAuthorize("hasRole('MERCHANT')")
    @Operation(summary = "删除活动", description = "商家删除营销活动（软删除）")
    public ApiResponse<Void> deleteCampaign(
            @Parameter(description = "活动ID", example = "1")
            @PathVariable Long campaignId) {
        log.info("删除活动: campaignId={}", campaignId);
        marketingCampaignService.deleteCampaign(campaignId);
        return ApiResponse.success();
    }

    /**
     * 审核活动（管理员）
     */
    @PostMapping("/{campaignId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "审核通过活动", description = "管理员审核通过营销活动")
    public ApiResponse<Void> approveCampaign(
            @Parameter(description = "活动ID", example = "1")
            @PathVariable Long campaignId) {
        log.info("审核通过活动: campaignId={}", campaignId);
        marketingCampaignService.approveCampaign(campaignId);
        return ApiResponse.success();
    }

    /**
     * 拒绝活动（管理员）
     */
    @PostMapping("/{campaignId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "拒绝活动", description = "管理员拒绝营销活动")
    public ApiResponse<Void> rejectCampaign(
            @Parameter(description = "活动ID", example = "1")
            @PathVariable Long campaignId,
            @Parameter(description = "拒绝原因", example = "活动内容不符合规范")
            @RequestParam String reason) {
        log.info("拒绝活动: campaignId={}, reason={}", campaignId, reason);
        marketingCampaignService.rejectCampaign(campaignId, reason);
        return ApiResponse.success();
    }

    /**
     * 扣减活动库存（秒杀）
     */
    @PostMapping("/{campaignId}/deduct-stock")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "扣减库存", description = "秒杀活动扣减库存（原子操作）")
    public ApiResponse<Boolean> deductStock(
            @Parameter(description = "活动ID", example = "1")
            @PathVariable Long campaignId,
            @Parameter(description = "扣减数量", example = "1")
            @RequestParam(defaultValue = "1") int quantity) {
        log.info("扣减活动库存: campaignId={}, quantity={}", campaignId, quantity);
        boolean success = marketingCampaignService.deductStock(campaignId, quantity);
        return ApiResponse.success(success);
    }

    /**
     * 检查商品是否参与活动
     */
    @GetMapping("/goods/{goodsId}")
    @Operation(summary = "检查商品活动", description = "检查指定商品是否参与营销活动")
    public ApiResponse<MarketingCampaign> checkGoodsInCampaign(
            @Parameter(description = "商品ID", example = "1")
            @PathVariable Long goodsId) {
        log.info("检查商品{}是否参与活动", goodsId);
        MarketingCampaign campaign = marketingCampaignService.checkGoodsInCampaign(goodsId);
        return ApiResponse.success(campaign);
    }

    /**
     * 获取活动统计数据
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('MERCHANT')")
    @Operation(summary = "获取活动统计", description = "商家查询营销活动统计数据")
    public ApiResponse<Object> getCampaignStatistics(
            @Parameter(description = "商家ID（可选，不传则查询当前商家）", example = "1")
            @RequestParam(required = false) Long merchantId) {
        log.info("查询活动统计数据: merchantId={}", merchantId);
        java.util.Map<String, Object> statistics = marketingCampaignService.getCampaignStatistics(merchantId);
        return ApiResponse.success(statistics);
    }
}
