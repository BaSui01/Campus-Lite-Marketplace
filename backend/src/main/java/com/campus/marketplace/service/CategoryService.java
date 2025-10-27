package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateCategoryRequest;
import com.campus.marketplace.common.dto.request.UpdateCategoryRequest;
import com.campus.marketplace.common.dto.response.CategoryNodeResponse;

import java.util.List;

public interface CategoryService {

    Long createCategory(CreateCategoryRequest request);

    void updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);

    List<CategoryNodeResponse> getCategoryTree();
}
