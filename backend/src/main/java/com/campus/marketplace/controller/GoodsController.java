package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.ApproveGoodsRequest;
import com.campus.marketplace.common.dto.request.CreateGoodsRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.GoodsDetailResponse;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.service.GoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 物品控制器
 * 
 * 处理物品相关的 HTTP 请求
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
@Tag(name = "物品管理", description = "物品发布、查询、审核等接口")
public class GoodsController {

    private final GoodsService goodsService;

    /**
     * 发布物品
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "发布物品", description = "用户发布二手物品信息")
    public ApiResponse<Long> createGoods(@Valid @RequestBody CreateGoodsRequest request) {
        Long goodsId = goodsService.createGoods(request);
        return ApiResponse.success(goodsId);
    }

    /**
     * 查询物品列表
     */
    @GetMapping
    @Operation(summary = "查询物品列表", description = "分页查询物品列表，支持关键词搜索、分类筛选、价格区间筛选和排序")
    public ApiResponse<Page<GoodsResponse>> listGoods(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类 ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "最低价格") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "最高价格") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "页码（从 0 开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段（createdAt/price/viewCount）") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向（ASC/DESC）") @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Page<GoodsResponse> result = goodsService.listGoods(
                keyword, categoryId, minPrice, maxPrice, page, size, sortBy, sortDirection
        );
        return ApiResponse.success(result);
    }

    /**
     * 查询物品详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询物品详情", description = "根据物品 ID 查询详细信息")
    public ApiResponse<GoodsDetailResponse> getGoodsDetail(
            @Parameter(description = "物品 ID") @PathVariable Long id
    ) {
        GoodsDetailResponse response = goodsService.getGoodsDetail(id);
        return ApiResponse.success(response);
    }

    /**
     * 查询待审核物品列表（管理员）
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('system:goods:approve')")
    @Operation(summary = "查询待审核物品列表", description = "管理员查询所有待审核的物品")
    public ApiResponse<Page<GoodsResponse>> listPendingGoods(
            @Parameter(description = "页码（从 0 开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size
    ) {
        Page<GoodsResponse> result = goodsService.listPendingGoods(page, size);
        return ApiResponse.success(result);
    }

    /**
     * 审核物品（管理员）
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('system:goods:approve')")
    @Operation(summary = "审核物品", description = "管理员审核物品，通过或拒绝")
    public ApiResponse<Void> approveGoods(
            @Parameter(description = "物品 ID") @PathVariable Long id,
            @Valid @RequestBody ApproveGoodsRequest request
    ) {
        goodsService.approveGoods(id, request.approved(), request.rejectReason());
        return ApiResponse.success(null);
    }
}
