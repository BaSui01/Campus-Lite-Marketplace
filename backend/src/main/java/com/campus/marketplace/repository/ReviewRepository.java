package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 评价数据访问接口
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 根据订单ID查询评价
     */
    Optional<Review> findByOrderId(Long orderId);

    /**
     * 检查订单是否已评价
     */
    boolean existsByOrderId(Long orderId);

    /**
     * 根据卖家ID查询所有评价
     */
    java.util.List<Review> findBySellerId(Long sellerId);

    /**
     * 根据订单ID列表查询评价
     */
    java.util.List<Review> findByOrderIdIn(java.util.List<Long> orderIds);
}
