package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 收藏 Repository
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /**
     * 查询用户是否已收藏物品
     */
    boolean existsByUserIdAndGoodsId(Long userId, Long goodsId);

    /**
     * 查询用户对物品的收藏记录
     */
    Optional<Favorite> findByUserIdAndGoodsId(Long userId, Long goodsId);

    /**
     * 查询用户的收藏列表（包含物品信息）
     */
    @Query("SELECT f FROM Favorite f JOIN FETCH f.goods WHERE f.userId = :userId")
    Page<Favorite> findByUserIdWithGoods(@Param("userId") Long userId, Pageable pageable);

    /**
     * 统计用户收藏数量
     */
    long countByUserId(Long userId);

    /**
     * 统计物品被收藏数量
     */
    long countByGoodsId(Long goodsId);
}
