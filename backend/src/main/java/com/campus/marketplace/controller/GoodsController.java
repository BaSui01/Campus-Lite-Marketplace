package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.ApproveGoodsRequest;
import com.campus.marketplace.common.dto.request.CreateGoodsRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.GoodsDetailResponse;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.service.GoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * @date 2025-10-29
 */

@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
@Tag(name = "物品管理", description = "物品发布、查询、审核等接口")
public class GoodsController {

    private final GoodsService goodsService;

        @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "发布物品", description = "用户发布二手物品信息")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "创建物品请求体",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateGoodsRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"title\": \"Apple MacBook Pro 13\",
                                      \"description\": \"M1 16G/512G，盒说全，支持自提\",
                                      \"price\": 6999.00,
                                      \"categoryId\": 101,
                                      \"images\": [
                                        \"https://cdn.campus.com/goods/g1.png\"
                                      ],
                                      \"tagIds\": [1,3]
                                    }
                                    """
                    )
            )
    )
    public ApiResponse<Long> createGoods(@Valid @RequestBody CreateGoodsRequest request) {
        Long goodsId = goodsService.createGoods(request);
        return ApiResponse.success(goodsId);
    }

        @GetMapping
    @Operation(summary = "查询物品列表", description = "分页查询物品列表，支持关键词搜索、分类筛选、价格区间筛选、状态筛选和排序")
    public ApiResponse<Page<GoodsResponse>> listGoods(
            @Parameter(description = "搜索关键词", example = "苹果笔记本") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类 ID", example = "101") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "最低价格", example = "1000") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "最高价格", example = "5000") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "物品状态（PENDING/APPROVED/REJECTED/SOLD/OFFLINE）", example = "APPROVED") @RequestParam(required = false) GoodsStatus status,
            @Parameter(description = "页码（从 0 开始）", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段（createdAt/price/viewCount）", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向（ASC/DESC）", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "标签 ID 列表（全部匹配）", example = "1,3,5") @RequestParam(name = "tags", required = false) java.util.List<Long> tagIds
    ) {
        Page<GoodsResponse> result = goodsService.listGoods(
                keyword, categoryId, minPrice, maxPrice, status, page, size, sortBy, sortDirection, tagIds
        );
        return ApiResponse.success(result);
    }

        @GetMapping("/{id}")
    @Operation(summary = "查询物品详情", description = "根据物品 ID 查询详细信息")
    public ApiResponse<GoodsDetailResponse> getGoodsDetail(
            @Parameter(description = "物品 ID", example = "12345") @PathVariable Long id
    ) {
        GoodsDetailResponse response = goodsService.getGoodsDetail(id);
        return ApiResponse.success(response);
    }

        @GetMapping("/pending")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_GOODS_APPROVE)")
    @Operation(summary = "查询待审核物品列表", description = "管理员查询所有待审核的物品，支持关键词搜索")
    public ApiResponse<Page<GoodsResponse>> listPendingGoods(
            @Parameter(description = "搜索关键词", example = "苹果笔记本") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码（从 0 开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size
    ) {
        Page<GoodsResponse> result = goodsService.listPendingGoods(keyword, page, size);
        return ApiResponse.success(result);
    }

        @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_GOODS_APPROVE)")
    @Operation(summary = "审核物品", description = "管理员审核物品，通过或拒绝")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApproveGoodsRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"approved\": true,
                                      \"rejectReason\": null
                                    }
                                    """
                    )
            )
    )
    public ApiResponse<Void> approveGoods(
            @Parameter(description = "物品 ID") @PathVariable Long id,
            @Valid @RequestBody ApproveGoodsRequest request
    ) {
        goodsService.approveGoods(id, request.approved(), request.rejectReason());
        return ApiResponse.success(null);
    }
}
