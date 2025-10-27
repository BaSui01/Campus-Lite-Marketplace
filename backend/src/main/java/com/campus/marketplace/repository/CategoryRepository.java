package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分类 Repository
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 根据名称查询分类
     */
    Optional<Category> findByName(String name);

    /**
     * 查询所有顶级分类（父分类为空）
     */
    List<Category> findByParentIdIsNullOrderBySortOrder();

    /**
     * 根据父分类 ID 查询子分类
     */
    List<Category> findByParentIdOrderBySortOrder(Long parentId);

    /**
     * 检查分类名称是否存在
     */
    boolean existsByName(String name);
}
