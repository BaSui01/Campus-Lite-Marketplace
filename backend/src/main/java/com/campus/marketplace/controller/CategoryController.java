package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.CategoryNodeResponse;
import com.campus.marketplace.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    /**
     * 🌲 获取分类树（公开接口）
     *
     * 所有用户（包括未登录用户）都可以查询分类树结构
     *
     * @return 分类树列表
     */
    @GetMapping("/categories/tree")
    @Operation(summary = "获取分类树", description = "按层级返回完整的分类树结构（公开接口）")
    public ApiResponse<List<CategoryNodeResponse>> getCategoryTree() {
        return ApiResponse.success(categoryService.getCategoryTree());
    }
}
