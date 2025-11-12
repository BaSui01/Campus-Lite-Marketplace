package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.CouponUserRelation;
import com.campus.marketplace.common.enums.CouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户-优惠券关联 Repository
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface CouponUserRelationRepository extends JpaRepository<CouponUserRelation, Long> {

    /**
     * 查询用户的优惠券列表
     */
    Page<CouponUserRelation> findByUserId(Long userId, Pageable pageable);

    /**
     * 查询用户可用的优惠券列表
     */
    List<CouponUserRelation> findByUserIdAndStatus(Long userId, CouponStatus status);

    /**
     * 查询用户领取某优惠券的数量
     */
    long countByUserIdAndCouponId(Long userId, Long couponId);

    /**
     * 查询用户特定状态的优惠券
     */
    Optional<CouponUserRelation> findByIdAndUserIdAndStatus(Long id, Long userId, CouponStatus status);

    /**
     * 根据订单ID查找已使用的优惠券记录
     */
    Optional<CouponUserRelation> findFirstByOrderIdAndStatus(Long orderId, CouponStatus status);

    /**
     * 根据优惠券ID和状态查询关联记录
     */
    List<CouponUserRelation> findByCouponIdAndStatus(Long couponId, CouponStatus status);
}
