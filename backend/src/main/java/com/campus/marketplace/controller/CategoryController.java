package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateCategoryRequest;
import com.campus.marketplace.common.dto.request.UpdateCategoryRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.CategoryNodeResponse;
import com.campus.marketplace.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类标签控制器
 *
 * 提供分类树查询与分类管理接口
 *
 * @author BaSui
 * @date 2025-10-29
 */

@RestController
@RequiredArgsConstructor
@Tag(name = "分类标签", description = "分类树查询及分类管理接口")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories/tree")
    @Operation(summary = "获取分类树", description = "按层级返回完整的分类树结构")
    public ApiResponse<List<CategoryNodeResponse>> getCategoryTree() {
        return ApiResponse.success(categoryService.getCategoryTree());
    }

    @PostMapping("/admin/categories")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_MANAGE)")
    @Operation(summary = "创建分类", description = "管理员创建新的分类节点")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateCategoryRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"name\": \"数码配件\",
                                      \"description\": \"耳机/键盘/鼠标等\",
                                      \"parentId\": 100,
                                      \"sortOrder\": 10
                                    }
                                    """
                    )
            )
    )
    public ApiResponse<Long> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ApiResponse.success(categoryService.createCategory(request));
    }

    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_MANAGE)")
    @Operation(summary = "更新分类", description = "管理员更新分类信息")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateCategoryRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"name\": \"电脑外设\",
                                      \"description\": \"包含键鼠/音箱\",
                                      \"parentId\": 100,
                                      \"sortOrder\": 20
                                    }
                                    """
                    )
            )
    )
    public ApiResponse<Void> updateCategory(@Parameter(description = "分类ID", example = "201") @PathVariable Long id,
                                            @Valid @RequestBody UpdateCategoryRequest request) {
        categoryService.updateCategory(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CATEGORY_MANAGE)")
    @Operation(summary = "删除分类", description = "管理员删除分类节点，删除前需确保无子节点及关联商品")
    public ApiResponse<Void> deleteCategory(@Parameter(description = "分类ID", example = "201") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success(null);
    }
}
