package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.enums.GoodsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 物品 Repository
 * 
 * 提供物品数据访问方法，包含全文搜索和复杂查询
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Repository
public interface GoodsRepository extends JpaRepository<Goods, Long> {

    /**
     * 根据 ID 查询物品（包含卖家和分类信息）
     * 使用 EntityGraph 避免 N+1 查询
     */
    @EntityGraph(attributePaths = {"seller", "category"})
    @Query("SELECT g FROM Goods g WHERE g.id = :id")
    Optional<Goods> findByIdWithDetails(@Param("id") Long id);

    /**
     * 根据状态查询物品（包含卖家信息）
     */
    @Query("SELECT g FROM Goods g JOIN FETCH g.seller WHERE g.status = :status")
    List<Goods> findByStatusWithSeller(@Param("status") GoodsStatus status);

    /**
     * 分页查询物品（支持多条件筛选）
     */
    @Query("SELECT g FROM Goods g WHERE " +
           "(:status IS NULL OR g.status = :status) AND " +
           "(:categoryId IS NULL OR g.categoryId = :categoryId) AND " +
           "(:minPrice IS NULL OR g.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR g.price <= :maxPrice) AND " +
           "(:keyword IS NULL OR g.title LIKE %:keyword% OR g.description LIKE %:keyword%)")
    Page<Goods> findByConditions(
            @Param("status") GoodsStatus status,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 全文搜索（使用 PostgreSQL tsvector）
     */
    @Query(value = "SELECT * FROM t_goods " +
                   "WHERE search_vector @@ to_tsquery('chinese', :query) " +
                   "AND status = 'APPROVED' " +
                   "ORDER BY ts_rank(search_vector, to_tsquery('chinese', :query)) DESC",
           nativeQuery = true)
    List<Goods> fullTextSearch(@Param("query") String query);

    /**
     * 根据卖家 ID 查询物品
     */
    Page<Goods> findBySellerId(Long sellerId, Pageable pageable);

    /**
     * 根据分类 ID 查询物品
     */
    Page<Goods> findByCategoryIdAndStatus(Long categoryId, GoodsStatus status, Pageable pageable);

    /**
     * 查询热门物品（按浏览量和收藏量排序）
     */
    @Query("SELECT g FROM Goods g WHERE g.status = :status " +
           "ORDER BY (g.viewCount * 0.7 + g.favoriteCount * 0.3) DESC")
    List<Goods> findHotGoods(@Param("status") GoodsStatus status, Pageable pageable);

    /**
     * 统计指定状态的物品数量
     */
    long countByStatus(GoodsStatus status);

    /**
     * 统计卖家的物品数量
     */
    long countBySellerId(Long sellerId);
}
