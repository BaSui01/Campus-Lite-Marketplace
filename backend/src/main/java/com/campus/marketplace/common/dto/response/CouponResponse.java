package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.entity.Coupon;
import com.campus.marketplace.common.enums.CouponType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券响应 DTO
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Builder
public record CouponResponse(
        Long id,
        String code,
        String name,
        CouponType type,
        BigDecimal discountAmount,
        BigDecimal discountRate,
        BigDecimal minAmount,
        Integer totalCount,
        Integer receivedCount,
        Integer usedCount,
        Integer limitPerUser,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String description,
        Boolean isActive
) {
    
    public static CouponResponse from(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .type(coupon.getType())
                .discountAmount(coupon.getDiscountAmount())
                .discountRate(coupon.getDiscountRate())
                .minAmount(coupon.getMinAmount())
                .totalCount(coupon.getTotalCount())
                .receivedCount(coupon.getReceivedCount())
                .usedCount(coupon.getUsedCount())
                .limitPerUser(coupon.getLimitPerUser())
                .startTime(coupon.getStartTime())
                .endTime(coupon.getEndTime())
                .description(coupon.getDescription())
                .isActive(coupon.getIsActive())
                .build();
    }
}
