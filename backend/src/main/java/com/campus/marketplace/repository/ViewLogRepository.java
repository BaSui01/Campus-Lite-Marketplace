package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ViewLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 浏览足迹数据访问接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface ViewLogRepository extends JpaRepository<ViewLog, Long> {

    /**
     * 根据用户ID查询浏览历史（按时间倒序）
     */
    List<ViewLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 根据用户ID和商品ID查询最近的浏览记录
     */
    ViewLog findFirstByUserIdAndGoodsIdOrderByCreatedAtDesc(Long userId, Long goodsId);

    /**
     * 删除用户的所有浏览历史
     */
    void deleteByUserId(Long userId);

    /**
     * 删除指定时间之前的浏览记录
     */
    void deleteByCreatedAtBefore(LocalDateTime dateTime);

    /**
     * 统计商品的浏览次数
     */
    long countByGoodsId(Long goodsId);

    /**
     * 获取用户最近浏览的商品ID列表（去重）
     */
    @Query("SELECT DISTINCT vl.goodsId FROM ViewLog vl WHERE vl.userId = :userId ORDER BY vl.createdAt DESC")
    List<Long> findRecentViewedGoodsIds(@Param("userId") Long userId, Pageable pageable);
}
