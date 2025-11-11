package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.enums.GoodsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.campus.marketplace.repository.projection.GoodsSearchProjection;

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
     * 根据状态分页查询物品（包含卖家、分类、校区信息）
     * 🔧 使用 EntityGraph 避免懒加载异常
     */
    @EntityGraph(attributePaths = {"seller", "category", "campus"})
    Page<Goods> findByStatus(GoodsStatus status, Pageable pageable);

    /**
     * 分页查询物品（支持多条件筛选）
     * 🔧 使用 COALESCE 保证 LIKE 参数为字符串，避免 PostgreSQL 将 NULL 判成 bytea 导致 "~~ bytea" 报错
     */
    @Query("SELECT g FROM Goods g WHERE " +
           "(:status IS NULL OR g.status = :status) AND " +
           "(:categoryId IS NULL OR g.categoryId = :categoryId) AND " +
           "(:minPrice IS NULL OR g.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR g.price <= :maxPrice) AND " +
           "(COALESCE(:keyword, '') = '' OR g.title LIKE CONCAT('%', COALESCE(:keyword, ''), '%') OR g.description LIKE CONCAT('%', COALESCE(:keyword, ''), '%'))")
    Page<Goods> findByConditions(
            @Param("status") GoodsStatus status,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 分页查询物品（包含校区过滤）
     * 🔧 使用 COALESCE 保证 LIKE 参数为字符串，避免 PostgreSQL 将 NULL 判成 bytea 导致 "~~ bytea" 报错
     * 🔧 使用 EntityGraph 预加载 seller 和 category 避免 N+1 查询和懒加载异常
     */
    @EntityGraph(attributePaths = {"seller", "category"})
    @Query("SELECT g FROM Goods g WHERE " +
           "(:status IS NULL OR g.status = :status) AND " +
           "(:categoryId IS NULL OR g.categoryId = :categoryId) AND " +
           "(:minPrice IS NULL OR g.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR g.price <= :maxPrice) AND " +
           "(COALESCE(:keyword, '') = '' OR g.title LIKE CONCAT('%', COALESCE(:keyword, ''), '%') OR g.description LIKE CONCAT('%', COALESCE(:keyword, ''), '%')) AND " +
           "(:campusId IS NULL OR g.campusId = :campusId)")
    Page<Goods> findByConditionsWithCampus(
            @Param("status") GoodsStatus status,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword,
            @Param("campusId") Long campusId,
            Pageable pageable
    );

    /**
     * 分页查询物品（包含校区过滤和 ID 过滤）
     * 🔧 使用 COALESCE 保证 LIKE 参数为字符串，避免 PostgreSQL 将 NULL 判成 bytea 导致 "~~ bytea" 报错
     * 🔧 使用 EntityGraph 预加载 seller 和 category 避免 N+1 查询和懒加载异常
     */
    @EntityGraph(attributePaths = {"seller", "category"})
    @Query("SELECT g FROM Goods g WHERE " +
           "(:status IS NULL OR g.status = :status) AND " +
           "(:categoryId IS NULL OR g.categoryId = :categoryId) AND " +
           "(:minPrice IS NULL OR g.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR g.price <= :maxPrice) AND " +
           "(COALESCE(:keyword, '') = '' OR g.title LIKE CONCAT('%', COALESCE(:keyword, ''), '%') OR g.description LIKE CONCAT('%', COALESCE(:keyword, ''), '%')) AND " +
           "(:campusId IS NULL OR g.campusId = :campusId) AND g.id IN (:goodsIds)")
    Page<Goods> findByConditionsWithCampusAndIds(
            @Param("status") GoodsStatus status,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword,
            @Param("campusId") Long campusId,
            @Param("goodsIds") List<Long> goodsIds,
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
     * 全文检索（高亮 + 排序 + 分页 + 校区过滤）
     */
    @Query(value = "SELECT g.id as id, g.title as title, " +
            "ts_headline('chinese', coalesce(g.description, ''), plainto_tsquery('chinese', :q), 'MaxFragments=2, MaxWords=12, MinWords=4, StartSel=<em>, StopSel=</em>') as snippet, " +
            "ts_rank(g.search_vector, plainto_tsquery('chinese', :q)) as rank, " +
            "g.campus_id as campusId, g.price as price " +
            "FROM t_goods g " +
            "WHERE g.status = 'APPROVED' " +
            "AND g.search_vector @@ plainto_tsquery('chinese', :q) " +
            "AND (:campusId IS NULL OR g.campus_id = :campusId) " +
            "ORDER BY rank DESC, g.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM t_goods g WHERE g.status='APPROVED' AND g.search_vector @@ plainto_tsquery('chinese', :q) AND (:campusId IS NULL OR g.campus_id = :campusId)",
            nativeQuery = true)
    Page<GoodsSearchProjection> searchGoodsFts(@Param("q") String q,
                                               @Param("campusId") Long campusId,
                                               Pageable pageable);

    @Query(value = "SELECT g.id as id, g.title as title, " +
            "ts_headline('chinese', coalesce(g.description, ''), plainto_tsquery('chinese', :q), 'MaxFragments=2, MaxWords=12, MinWords=4, StartSel=<em>, StopSel=</em>') as snippet, " +
            "ts_rank(g.search_vector, plainto_tsquery('chinese', :q)) as rank, " +
            "g.campus_id as campusId, g.price as price " +
            "FROM t_goods g " +
            "WHERE g.status = 'APPROVED' " +
            "AND g.search_vector @@ plainto_tsquery('chinese', :q) " +
            "AND (:campusId IS NULL OR g.campus_id = :campusId) " +
            "AND g.id = ANY(:goodsIds) " +
            "ORDER BY rank DESC, g.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM t_goods g WHERE g.status='APPROVED' AND g.search_vector @@ plainto_tsquery('chinese', :q) AND (:campusId IS NULL OR g.campus_id = :campusId) AND g.id = ANY(:goodsIds)",
            nativeQuery = true)
    Page<GoodsSearchProjection> searchGoodsFtsWithIds(@Param("q") String q,
                                                      @Param("campusId") Long campusId,
                                                      @Param("goodsIds") Long[] goodsIds,
                                                      Pageable pageable);

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
     * 查询热门物品（按浏览量和收藏量排序，支持按校区过滤）
     */
    @Query("SELECT g FROM Goods g WHERE g.status = :status " +
           "AND (:campusId IS NULL OR g.campusId = :campusId) " +
           "ORDER BY (g.viewCount * 0.7 + g.favoriteCount * 0.3) DESC")
    List<Goods> findHotGoodsByCampus(@Param("status") GoodsStatus status,
                                     @Param("campusId") Long campusId,
                                     Pageable pageable);

    /**
     * 查询热门物品（按分类集合与校区过滤）
     */
    @Query("SELECT g FROM Goods g WHERE g.status = :status " +
           "AND (:campusId IS NULL OR g.campusId = :campusId) " +
           "AND g.categoryId IN :categoryIds " +
           "ORDER BY (g.viewCount * 0.7 + g.favoriteCount * 0.3) DESC, g.createdAt DESC")
    List<Goods> findHotGoodsByCampusAndCategories(@Param("status") GoodsStatus status,
                                                  @Param("campusId") Long campusId,
                                                  @Param("categoryIds") java.util.List<Long> categoryIds,
                                                  Pageable pageable);

    /**
     * 统计指定状态的物品数量
     */
    long countByStatus(GoodsStatus status);

    /**
     * 统计卖家的物品数量
     */
    long countBySellerId(Long sellerId);

    /**
     * 按校区统计数量
     */
    long countByCampusId(Long campusId);

    /**
     * 根据分类统计数量
     */
    long countByCategoryId(Long categoryId);

    /**
     * 🎯 BaSui 新增：按分类和状态统计商品数量
     */
    long countByCategoryIdAndStatus(Long categoryId, com.campus.marketplace.common.enums.GoodsStatus status);

    /**
     * 统计卖家的在售商品数量（前端需要）
     * @param sellerId 卖家ID
     * @param status 商品状态
     * @return 商品数量
     */
    long countBySellerIdAndStatus(Long sellerId, GoodsStatus status);

    /**
     * 加行级写锁查询（用于下单场景防并发超卖/重复下单）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Goods g WHERE g.id = :id")
    Optional<Goods> findByIdForUpdate(@Param("id") Long id);
}
