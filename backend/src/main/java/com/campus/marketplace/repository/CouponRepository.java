package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 优惠券 Repository
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /**
     * 根据优惠券代码查询
     */
    Optional<Coupon> findByCode(String code);

    /**
     * 检查优惠券代码是否存在
     */
    boolean existsByCode(String code);
}
