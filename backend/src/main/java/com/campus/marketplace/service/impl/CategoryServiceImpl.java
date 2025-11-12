package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreateCategoryRequest;
import com.campus.marketplace.common.dto.request.UpdateCategoryRequest;
import com.campus.marketplace.common.dto.response.CategoryNodeResponse;
import com.campus.marketplace.common.dto.response.CategoryStatisticsResponse;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 分类服务实现
 *
 * 实现分类的创建/更新/删除与分类树构建
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final GoodsRepository goodsRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(CreateCategoryRequest request) {
        String name = request.name().trim();
        categoryRepository.findByName(name).ifPresent(category -> {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "分类名称已存在");
        });

        if (request.parentId() != null && !categoryRepository.existsById(request.parentId())) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, "父级分类不存在");
        }

        Category category = Category.builder()
                .name(name)
                .description(request.description())
                .parentId(request.parentId())
                .sortOrder(Optional.ofNullable(request.sortOrder()).orElse(0))
                .build();
        categoryRepository.save(category);

        log.info("创建分类成功 categoryId={}, name={}", category.getId(), category.getName());
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        String name = request.name().trim();
        categoryRepository.findByName(name)
                .filter(existing -> !Objects.equals(existing.getId(), id))
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "分类名称已存在");
                });

        if (request.parentId() != null) {
            if (Objects.equals(id, request.parentId())) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "分类不能设置为自己的父级");
            }
            categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, "父级分类不存在"));
            // 校验无环
            ensureNoCircularReference(id, request.parentId());
        }

        category.setName(name);
        category.setDescription(request.description());
        category.setParentId(request.parentId());
        category.setSortOrder(Optional.ofNullable(request.sortOrder()).orElse(0));
        categoryRepository.save(category);

        log.info("更新分类成功 categoryId={}, name={}", id, name);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        List<Category> children = categoryRepository.findByParentIdOrderBySortOrder(id);
        if (!children.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "仍存在子分类，无法删除");
        }

        long goodsCount = goodsRepository.countByCategoryId(id);
        if (goodsCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "分类仍有关联商品，无法删除");
        }

        categoryRepository.delete(category);
        log.info("删除分类成功 categoryId={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryNodeResponse> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAll(SortBySortOrder());
        Map<Long, List<Category>> groupedByParent = allCategories.stream()
                .collect(Collectors.groupingBy(cat -> Optional.ofNullable(cat.getParentId()).orElse(0L)));

        return buildTree(groupedByParent, 0L);
    }

    // 🎯 BaSui 新增方法实现（分类管理扩展）

    @Override
    @Transactional(readOnly = true)
    public List<Category> listAll() {
        return categoryRepository.findAll(SortBySortOrder());
    }

    @Override
    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, "分类不存在"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSort(Map<Long, Integer> sortMap) {
        if (sortMap == null || sortMap.isEmpty()) {
            return;
        }

        List<Category> categories = categoryRepository.findAllById(sortMap.keySet());
        if (categories.size() != sortMap.size()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, "部分分类不存在");
        }

        categories.forEach(category -> {
            Integer newSortOrder = sortMap.get(category.getId());
            if (newSortOrder != null) {
                category.setSortOrder(newSortOrder);
            }
        });

        categoryRepository.saveAll(categories);
        log.info("批量更新分类排序成功，共更新 {} 个分类", categories.size());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryStatisticsResponse getStatistics(Long id) {
        Category category = getById(id);

        // 统计商品数量
        long goodsCount = goodsRepository.countByCategoryId(id);
        long onSaleCount = goodsRepository.countByCategoryIdAndStatus(id, GoodsStatus.APPROVED);
        long soldCount = goodsRepository.countByCategoryIdAndStatus(id, GoodsStatus.SOLD);

        // 统计子分类数量
        long childrenCount = categoryRepository.findByParentIdOrderBySortOrder(id).size();

        return CategoryStatisticsResponse.builder()
                .categoryId(id)
                .categoryName(category.getName())
                .goodsCount(goodsCount)
                .childrenCount(childrenCount)
                .onSaleGoodsCount(onSaleCount)
                .soldGoodsCount(soldCount)
                .build();
    }

    // 🔧 私有辅助方法

    private List<CategoryNodeResponse> buildTree(Map<Long, List<Category>> grouped, Long parentId) {
        List<Category> categories = grouped.getOrDefault(parentId, Collections.emptyList());
        return categories.stream()
                .sorted(Comparator.comparing(Category::getSortOrder))
                .map(cat -> CategoryNodeResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .description(cat.getDescription())
                        .parentId(cat.getParentId())
                        .sortOrder(cat.getSortOrder())
                        .createdAt(cat.getCreatedAt())
                        .children(buildTree(grouped, cat.getId()))
                        .build())
                .toList();
    }

    private void ensureNoCircularReference(Long categoryId, Long newParentId) {
        Long currentParent = newParentId;
        while (currentParent != null) {
            if (Objects.equals(currentParent, categoryId)) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "不能将分类移动到其子级下");
            }
            currentParent = categoryRepository.findById(currentParent)
                    .map(Category::getParentId)
                    .orElse(null);
        }
    }

    private org.springframework.data.domain.Sort SortBySortOrder() {
        return org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.asc("parentId"),
                org.springframework.data.domain.Sort.Order.asc("sortOrder"),
                org.springframework.data.domain.Sort.Order.asc("id")
        );
    }
}
