package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.CouponType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Entity
@Table(name = "t_coupon", indexes = {
        @Index(name = "idx_coupon_code", columnList = "code", unique = true),
        @Index(name = "idx_coupon_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseEntity {

    /**
     * 优惠券代码（唯一）
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 优惠券名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 优惠券类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private CouponType type;

    /**
     * 优惠金额（满减券）
     */
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    /**
     * 折扣比例（折扣券，如0.9表示9折）
     */
    @Column(name = "discount_rate", precision = 3, scale = 2)
    private BigDecimal discountRate;

    /**
     * 最低使用金额
     */
    @Column(name = "min_amount", precision = 10, scale = 2)
    private BigDecimal minAmount;

    /**
     * 发行数量
     */
    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    /**
     * 已领取数量
     */
    @Column(name = "received_count", nullable = false)
    @Builder.Default
    private Integer receivedCount = 0;

    /**
     * 已使用数量
     */
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    /**
     * 每人限领数量
     */
    @Column(name = "limit_per_user")
    private Integer limitPerUser;

    /**
     * 开始时间
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * 优惠券描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 是否有效
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
