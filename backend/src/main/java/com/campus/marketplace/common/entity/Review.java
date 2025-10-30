package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 评价实体
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Entity
@Table(name = "t_review", indexes = {
        @Index(name = "idx_review_order", columnList = "order_id"),
        @Index(name = "idx_review_seller", columnList = "seller_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    /**
     * 订单 ID
     */
    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    /**
     * 买家 ID
     */
    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    /**
     * 卖家 ID
     */
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    /**
     * 评分（1-5星）
     */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /**
     * 评价内容
     */
    @Column(name = "content", nullable = false, length = 500)
    private String content;
}
