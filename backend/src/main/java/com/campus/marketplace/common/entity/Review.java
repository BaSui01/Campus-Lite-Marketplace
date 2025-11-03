package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 评价实体
 *
 * Spec #7扩展：三维评分 + 追评功能 + 互动统计 + 隐私状态
 *
 * @author BaSui 😎 - 从单一评分升级到三维评分，还能追评和匿名！
 * @since 2025-10-27 (初始版本)
 * @since 2025-11-03 (Spec #7扩展)
 */
@Entity
@Table(name = "t_review", indexes = {
        @Index(name = "idx_review_order", columnList = "order_id"),
        @Index(name = "idx_review_seller", columnList = "seller_id"),
        @Index(name = "idx_review_status", columnList = "status")
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
     * 综合评分（1-5星）
     * 兼容旧版本，新版本使用三维评分
     */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /**
     * 评价内容
     */
    @Column(name = "content", nullable = false, length = 500)
    private String content;

    // ==================== Spec #7扩展：三维评分系统 ====================

    /**
     * 物品质量评分（1-5星）
     * 用于更细粒度的评价维度
     */
    @Column(name = "quality_score", nullable = false)
    @Builder.Default
    private Integer qualityScore = 5;

    /**
     * 服务态度评分（1-5星）
     * 评价卖家的服务质量
     */
    @Column(name = "service_score", nullable = false)
    @Builder.Default
    private Integer serviceScore = 5;

    /**
     * 物流速度评分（1-5星）
     * 评价发货和配送的速度
     */
    @Column(name = "delivery_score", nullable = false)
    @Builder.Default
    private Integer deliveryScore = 5;

    // ==================== Spec #7扩展：追评功能 ====================

    /**
     * 是否有追评
     * 用户可在收货后7-30天内追加评价
     */
    @Column(name = "has_append_review", nullable = false)
    @Builder.Default
    private Boolean hasAppendReview = false;

    /**
     * 追评内容
     * 最长500字，仅在hasAppendReview=true时有效
     */
    @Column(name = "append_content", length = 500)
    private String appendContent;

    /**
     * 追评时间
     * 记录用户追加评价的时间
     */
    @Column(name = "append_at")
    private LocalDateTime appendAt;

    // ==================== Spec #7扩展：互动统计 ====================

    /**
     * 点赞数
     * 其他用户觉得该评价有帮助的次数
     */
    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    /**
     * 回复数
     * 卖家或管理员回复的次数
     */
    @Column(name = "reply_count", nullable = false)
    @Builder.Default
    private Integer replyCount = 0;

    // ==================== Spec #7扩展：隐私与状态 ====================

    /**
     * 是否匿名评价
     * true=匿名（显示"匿名用户"），false=实名（显示真实昵称）
     */
    @Column(name = "is_anonymous", nullable = false)
    @Builder.Default
    private Boolean isAnonymous = false;

    /**
     * 评价状态
     * NORMAL=正常显示，HIDDEN=管理员隐藏，REPORTED=被举报待审核
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.NORMAL;
}
