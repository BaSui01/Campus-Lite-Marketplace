package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateCategoryRequest;
import com.campus.marketplace.common.dto.request.UpdateCategoryRequest;
import com.campus.marketplace.common.dto.response.CategoryNodeResponse;

import java.util.List;

/**
 * 分类服务接口
 *
 * 提供分类创建、更新、删除与分类树查询
 *
 * @author BaSui
 * @date 2025-10-27
 */
public interface CategoryService {

    Long createCategory(CreateCategoryRequest request);

    void updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);

    List<CategoryNodeResponse> getCategoryTree();
}
