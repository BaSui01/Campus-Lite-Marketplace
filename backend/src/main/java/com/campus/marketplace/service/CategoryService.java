package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateCategoryRequest;
import com.campus.marketplace.common.dto.request.UpdateCategoryRequest;
import com.campus.marketplace.common.dto.response.CategoryNodeResponse;
import com.campus.marketplace.common.entity.Category;

import java.util.List;
import java.util.Map;

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

    // 🎯 BaSui 新增方法（分类管理扩展）
    /**
     * 查询所有分类列表（平铺）
     */
    List<Category> listAll();

    /**
     * 根据ID获取分类详情
     */
    Category getById(Long id);

    /**
     * 批量更新分类排序
     * @param sortMap 分类ID -> 新排序值
     */
    void batchUpdateSort(Map<Long, Integer> sortMap);

    /**
     * 获取分类统计信息
     */
    com.campus.marketplace.common.dto.response.CategoryStatisticsResponse getStatistics(Long id);
}
