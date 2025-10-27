package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateCategoryRequest;
import com.campus.marketplace.common.dto.request.UpdateCategoryRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.CategoryNodeResponse;
import com.campus.marketplace.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "分类标签", description = "分类树查询及分类管理接口")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/api/categories/tree")
    @Operation(summary = "获取分类树", description = "按层级返回完整的分类树结构")
    public ApiResponse<List<CategoryNodeResponse>> getCategoryTree() {
        return ApiResponse.success(categoryService.getCategoryTree());
    }

    @PostMapping("/api/admin/categories")
    @PreAuthorize("hasAuthority('system:category:manage')")
    @Operation(summary = "创建分类", description = "管理员创建新的分类节点")
    public ApiResponse<Long> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ApiResponse.success(categoryService.createCategory(request));
    }

    @PutMapping("/api/admin/categories/{id}")
    @PreAuthorize("hasAuthority('system:category:manage')")
    @Operation(summary = "更新分类", description = "管理员更新分类信息")
    public ApiResponse<Void> updateCategory(@PathVariable Long id,
                                            @Valid @RequestBody UpdateCategoryRequest request) {
        categoryService.updateCategory(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/api/admin/categories/{id}")
    @PreAuthorize("hasAuthority('system:category:manage')")
    @Operation(summary = "删除分类", description = "管理员删除分类节点，删除前需确保无子节点及关联商品")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success(null);
    }
}
