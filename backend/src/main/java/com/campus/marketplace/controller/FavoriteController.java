package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 * 
 * 处理物品收藏相关的 HTTP 请求
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
@Tag(name = "收藏管理", description = "物品收藏、取消收藏、查询收藏列表等接口")
public class FavoriteController {

    private final FavoriteService favoriteService;

        @PostMapping("/{goodsId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "添加收藏", description = "用户收藏物品")
    public ApiResponse<Void> addFavorite(
            @Parameter(description = "物品 ID", example = "12345") @PathVariable Long goodsId
    ) {
        favoriteService.addFavorite(goodsId);
        return ApiResponse.success(null);
    }

        @DeleteMapping("/{goodsId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "取消收藏", description = "用户取消收藏物品")
    public ApiResponse<Void> removeFavorite(
            @Parameter(description = "物品 ID", example = "12345") @PathVariable Long goodsId
    ) {
        favoriteService.removeFavorite(goodsId);
        return ApiResponse.success(null);
    }

        @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询收藏列表", description = "查询当前用户的收藏列表")
    public ApiResponse<Page<GoodsResponse>> listFavorites(
            @Parameter(description = "页码（从 0 开始）", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        Page<GoodsResponse> result = favoriteService.listFavorites(page, size);
        return ApiResponse.success(result);
    }

        @GetMapping("/{goodsId}/check")
    @Operation(summary = "检查是否已收藏", description = "检查当前用户是否已收藏指定物品")
    public ApiResponse<Boolean> isFavorited(
            @Parameter(description = "物品 ID", example = "12345") @PathVariable Long goodsId
    ) {
        boolean favorited = favoriteService.isFavorited(goodsId);
        return ApiResponse.success(favorited);
    }
}
