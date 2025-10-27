package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.CouponResponse;
import com.campus.marketplace.common.entity.Coupon;
import com.campus.marketplace.common.entity.CouponUserRelation;
import com.campus.marketplace.common.enums.CouponType;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券服务接口
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public interface CouponService {

    /**
     * 创建优惠券
     */
    Coupon createCoupon(String code, String name, CouponType type,
                        BigDecimal discountAmount, BigDecimal discountRate, BigDecimal minAmount,
                        Integer totalCount, Integer limitPerUser,
                        LocalDateTime startTime, LocalDateTime endTime, String description);

    /**
     * 领取优惠券
     */
    CouponUserRelation receiveCoupon(Long userId, Long couponId);

    /**
     * 使用优惠券
     */
    void useCoupon(Long userId, Long relationId, Long orderId);

    /**
     * 退还优惠券（订单取消时）
     */
    void refundCoupon(Long relationId);

    /**
     * 查询可用优惠券列表
     */
    Page<CouponResponse> listAvailableCoupons(int page, int size);

    /**
     * 查询用户的优惠券列表
     */
    Page<CouponUserRelation> listUserCoupons(Long userId, int page, int size);

    /**
     * 计算优惠金额
     */
    BigDecimal calculateDiscount(Long couponId, BigDecimal originalAmount);
}
