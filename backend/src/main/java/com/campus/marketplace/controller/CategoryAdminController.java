package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CategoryBatchSortRequest;
import com.campus.marketplace.common.dto.request.CreateCategoryRequest;
import com.campus.marketplace.common.dto.request.UpdateCategoryRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.CategoryNodeResponse;
import com.campus.marketplace.common.dto.response.CategoryStatisticsResponse;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 📂 BaSui 的分类管理控制器 - 管理商品分类！😎
 *
 * 功能范围：
 * - 📂 分类管理：查询、创建、编辑、删除、树形结构
 * - 🎯 排序管理：批量更新分类排序
 * - 📊 统计分析：分类商品统计
 *
 * ⚠️ 注意：
 * - 所有接口仅管理员可访问
 * - 删除分类前需检查子分类和关联商品
 * - 支持树形结构和平铺列表两种查询方式
 *
 * @author BaSui
 * @date 2025-11-07
 */
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Tag(name = "分类管理", description = "管理员后台分类管理相关接口")
public class CategoryAdminController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_VIEW)")
    @Operation(summary = "获取分类树", description = "获取完整的分类树形结构（含子分类）")
    public ApiResponse<List<CategoryNodeResponse>> getCategoryTree() {
        List<CategoryNodeResponse> tree = categoryService.getCategoryTree();
        return ApiResponse.success(tree);
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_VIEW)")
    @Operation(summary = "查询分类列表", description = "获取所有分类列表（平铺）")
    public ApiResponse<List<Category>> listCategories() {
        List<Category> categories = categoryService.listAll();
        return ApiResponse.success(categories);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_CREATE)")
    @Operation(summary = "创建分类", description = "创建新的商品分类")
    public ApiResponse<Long> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        Long categoryId = categoryService.createCategory(request);
        return ApiResponse.success(categoryId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_VIEW)")
    @Operation(summary = "获取分类详情", description = "根据ID获取分类详细信息")
    public ApiResponse<Category> getCategoryById(
            @Parameter(description = "分类ID", example = "1") @PathVariable Long id
    ) {
        Category category = categoryService.getById(id);
        return ApiResponse.success(category);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_EDIT)")
    @Operation(summary = "编辑分类", description = "更新分类信息")
    public ApiResponse<Void> updateCategory(
            @Parameter(description = "分类ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        categoryService.updateCategory(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_DELETE)")
    @Operation(summary = "删除分类", description = "删除指定分类（需确保无子分类和关联商品）")
    public ApiResponse<Void> deleteCategory(
            @Parameter(description = "分类ID", example = "1") @PathVariable Long id
    ) {
        categoryService.deleteCategory(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/sort")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_EDIT)")
    @Operation(summary = "批量排序", description = "批量更新分类排序")
    public ApiResponse<Void> batchUpdateSort(
            @Valid @RequestBody CategoryBatchSortRequest request
    ) {
        Map<Long, Integer> sortMap = request.getItems().stream()
                .collect(Collectors.toMap(
                        CategoryBatchSortRequest.SortItem::getCategoryId,
                        CategoryBatchSortRequest.SortItem::getSortOrder
                ));
        categoryService.batchUpdateSort(sortMap);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_VIEW)")
    @Operation(summary = "分类统计", description = "获取分类商品统计数据（在售、已售、子分类数）")
    public ApiResponse<CategoryStatisticsResponse> getCategoryStatistics(
            @Parameter(description = "分类ID", example = "1") @PathVariable Long id
    ) {
        CategoryStatisticsResponse statistics = categoryService.getStatistics(id);
        return ApiResponse.success(statistics);
    }
}
