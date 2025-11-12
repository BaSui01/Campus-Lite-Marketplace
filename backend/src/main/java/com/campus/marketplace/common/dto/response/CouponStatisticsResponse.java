package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券统计响应 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponStatisticsResponse {

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券代码
     */
    private String code;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 总发行数量
     */
    private Integer totalCount;

    /**
     * 已领取数量
     */
    private Integer receivedCount;

    /**
     * 已使用数量
     */
    private Integer usedCount;

    /**
     * 领取率（已领取/总发行）
     */
    private Double receiveRate;

    /**
     * 使用率（已使用/已领取）
     */
    private Double useRate;

    /**
     * 总优惠金额
     */
    private BigDecimal totalDiscountAmount;

    /**
     * 平均优惠金额
     */
    private BigDecimal avgDiscountAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 是否激活
     */
    private Boolean isActive;
}
