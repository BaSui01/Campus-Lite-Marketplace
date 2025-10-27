package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 订单数据访问接口
 * 
 * 提供订单的 CRUD 操作和自定义查询
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 根据订单号查询订单
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 根据订单号查询订单（包含关联数据）
     */
    @EntityGraph(attributePaths = {"goods", "buyer", "seller"})
    @Query("SELECT o FROM Order o WHERE o.orderNo = :orderNo")
    Optional<Order> findByOrderNoWithDetails(@Param("orderNo") String orderNo);

    /**
     * 查询买家订单列表（包含关联数据）
     */
    @EntityGraph(attributePaths = {"goods", "seller"})
    @Query("SELECT o FROM Order o WHERE o.buyerId = :buyerId " +
           "AND (:status IS NULL OR o.status = :status) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findByBuyerIdWithDetails(
            @Param("buyerId") Long buyerId,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    /**
     * 查询卖家订单列表（包含关联数据）
     */
    @EntityGraph(attributePaths = {"goods", "buyer"})
    @Query("SELECT o FROM Order o WHERE o.sellerId = :sellerId " +
           "AND (:status IS NULL OR o.status = :status) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findBySellerIdWithDetails(
            @Param("sellerId") Long sellerId,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    /**
     * 检查物品是否已有订单
     */
    boolean existsByGoodsIdAndStatusNot(Long goodsId, OrderStatus status);
}
