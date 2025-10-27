package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.CouponStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 用户-优惠券关联实体
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Entity
@Table(name = "t_coupon_user_relation", indexes = {
        @Index(name = "idx_relation_user", columnList = "user_id"),
        @Index(name = "idx_relation_coupon", columnList = "coupon_id"),
        @Index(name = "idx_relation_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUserRelation extends BaseEntity {

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 优惠券ID
     */
    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    /**
     * 优惠券状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CouponStatus status = CouponStatus.AVAILABLE;

    /**
     * 领取时间
     */
    @Column(name = "receive_time", nullable = false)
    private LocalDateTime receiveTime;

    /**
     * 使用时间
     */
    @Column(name = "use_time")
    private LocalDateTime useTime;

    /**
     * 使用的订单ID
     */
    @Column(name = "order_id")
    private Long orderId;

    /**
     * 过期时间
     */
    @Column(name = "expire_time", nullable = false)
    private LocalDateTime expireTime;
}
