package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.enums.DeliveryMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 * 
 * 存储订单信息
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Entity
@Table(name = "t_order", indexes = {
        @Index(name = "idx_order_buyer", columnList = "buyer_id"),
        @Index(name = "idx_order_seller", columnList = "seller_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_created_at", columnList = "created_at"),
        @Index(name = "idx_order_campus", columnList = "campus_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    /**
     * 订单号（唯一）
     */
    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;

    /**
     * 物品 ID
     */
    @Column(name = "goods_id", nullable = false)
    private Long goodsId;

    /**
     * 物品（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", insertable = false, updatable = false)
    private Goods goods;

    /**
     * 买家 ID
     */
    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    /**
     * 买家（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", insertable = false, updatable = false)
    private User buyer;

    /**
     * 卖家 ID
     */
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    /**
     * 卖家（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
    private User seller;

    /**
     * 校区 ID
     */
    @Column(name = "campus_id")
    private Long campusId;

    /**
     * 校区（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", insertable = false, updatable = false)
    private Campus campus;

    /**
     * 订单金额
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * 优惠金额
     */
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * 实付金额
     */
    @Column(name = "actual_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualAmount;

    /**
     * 订单状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    /**
     * 支付方式
     */
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    /**
     * 支付时间
     */
    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    /**
     * 优惠券 ID
     */
    @Column(name = "coupon_id")
    private Long couponId;

    /**
     * 配送方式（面交/快递）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", length = 20)
    @Builder.Default
    private DeliveryMethod deliveryMethod = DeliveryMethod.FACE_TO_FACE;

    /** 收货人姓名（快递时必填） */
    @Column(name = "receiver_name", length = 50)
    private String receiverName;

    /** 收货人手机号（快递时必填） */
    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    /** 收货地址（快递时必填） */
    @Column(name = "receiver_address", length = 255)
    private String receiverAddress;

    /** 买家备注（可选） */
    @Column(name = "buyer_note", length = 500)
    private String buyerNote;

    /**
     * 检查是否待支付
     */
    public boolean isPendingPayment() {
        return this.status == OrderStatus.PENDING_PAYMENT;
    }

    /**
     * 检查是否已支付
     */
    public boolean isPaid() {
        return this.status == OrderStatus.PAID;
    }

    /**
     * 检查是否已完成
     */
    public boolean isCompleted() {
        return this.status == OrderStatus.COMPLETED;
    }

    /**
     * 检查是否已取消
     */
    public boolean isCancelled() {
        return this.status == OrderStatus.CANCELLED;
    }
}
