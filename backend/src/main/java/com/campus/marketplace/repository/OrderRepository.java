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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 订单 Repository
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 根据订单号查询订单
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 根据订单号查询订单（包含物品、买家、卖家信息）
     */
    @EntityGraph(attributePaths = {"goods", "buyer", "seller"})
    @Query("SELECT o FROM Order o WHERE o.orderNo = :orderNo")
    Optional<Order> findByOrderNoWithDetails(@Param("orderNo") String orderNo);

    /**
     * 查询买家的订单
     */
    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

    /**
     * 查询买家的订单（按状态筛选）
     */
    Page<Order> findByBuyerIdAndStatus(Long buyerId, OrderStatus status, Pageable pageable);

    /**
     * 查询卖家的订单
     */
    Page<Order> findBySellerId(Long sellerId, Pageable pageable);

    /**
     * 查询卖家的订单（按状态筛选）
     */
    Page<Order> findBySellerIdAndStatus(Long sellerId, OrderStatus status, Pageable pageable);

    /**
     * 查询超时未支付的订单
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :timeout")
    List<Order> findTimeoutOrders(@Param("status") OrderStatus status, @Param("timeout") LocalDateTime timeout);

    /**
     * 统计用户的订单数量
     */
    long countByBuyerId(Long buyerId);

    /**
     * 统计卖家的订单数量
     */
    long countBySellerId(Long sellerId);

    /**
     * 检查物品是否已有订单
     */
    boolean existsByGoodsId(Long goodsId);
}
