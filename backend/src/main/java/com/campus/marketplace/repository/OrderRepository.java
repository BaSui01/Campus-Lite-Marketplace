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
    @EntityGraph(attributePaths = {"goods", "goods.category", "goods.seller", "buyer", "seller"})
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

    /**
     * 查找超时订单
     *
     * 查找指定状态且创建时间早于指定时间的订单
     *
     * @param status 订单状态
     * @param createdBefore 创建时间阈值
     * @return 超时订单列表
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :createdBefore")
    List<Order> findTimeoutOrders(
            @Param("status") OrderStatus status,
            @Param("createdBefore") LocalDateTime createdBefore
    );

    /**
     * 查找指定状态且更新时间早于指定时间的订单
     *
     * 用于自动确认收货和异常订单检测
     *
     * @param status 订单状态
     * @param updatedBefore 更新时间阈值
     * @return 订单列表
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.updatedAt < :updatedBefore")
    List<Order> findByStatusAndUpdatedAtBefore(
            @Param("status") OrderStatus status,
            @Param("updatedBefore") LocalDateTime updatedBefore
    );

    /**
     * 按校区统计订单数量
     */
    long countByCampusId(Long campusId);

    /**
     * 统计卖家指定状态的订单数量
     * 
     * @param sellerId 卖家ID
     * @param status 订单状态
     * @return 订单数量
     */
    long countBySellerIdAndStatus(Long sellerId, OrderStatus status);

    /**
     * 统计卖家的总订单数量
     * 
     * @param sellerId 卖家ID
     * @return 订单数量
     */
    long countBySellerId(Long sellerId);

    /**
     * 根据商品ID查询所有订单
     * 
     * @param goodsId 商品ID
     * @return 订单列表
     */
    List<Order> findByGoodsId(Long goodsId);

    /**
     * 查询某个商品的指定状态的第一条订单（用于 LOCKED 可见性与幂等校验）
     */
    Optional<Order> findFirstByGoodsIdAndStatus(Long goodsId, OrderStatus status);

    /**
     * 查找即将超时的待支付订单（用于提醒）
     * createdAt < :createdBefore AND createdAt >= :createdAfter
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :createdBefore AND o.createdAt >= :createdAfter")
    List<Order> findPendingPaymentBetweenCreatedAt(
            @Param("status") OrderStatus status,
            @Param("createdBefore") java.time.LocalDateTime createdBefore,
            @Param("createdAfter") java.time.LocalDateTime createdAfter
    );
}
