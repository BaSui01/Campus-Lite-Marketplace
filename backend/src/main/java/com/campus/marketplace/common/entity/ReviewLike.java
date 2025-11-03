package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 评价点赞实体
 *
 * Spec #7：用户可以对有帮助的评价点赞
 *
 * @author BaSui 😎 - 觉得有用就点个赞吧！
 * @since 2025-11-03
 */
@Entity
@Table(name = "t_review_like",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_review_user", columnNames = {"review_id", "user_id"})
       },
       indexes = {
           @Index(name = "idx_review_like_review", columnList = "review_id"),
           @Index(name = "idx_review_like_user", columnList = "user_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewLike extends BaseEntity {

    /**
     * 评价ID（外键）
     */
    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    /**
     * 点赞用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 是否有效（用于取消点赞）
     * true=有效点赞，false=已取消
     * 采用软删除策略，保留点赞历史
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 关联到Review实体（可选，用于ORM查询）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", insertable = false, updatable = false)
    private Review review;

    /**
     * 关联到点赞用户（可选，用于ORM查询）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
