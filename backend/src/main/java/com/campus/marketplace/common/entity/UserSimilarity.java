package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * 用户相似度实体
 *
 * 存储用户之间的相似度，用于协同过滤推荐。
 * 相似度基于用户行为（浏览、收藏、购买）计算得出。
 *
 * @author BaSui 😎
 * @since 2025-11-04
 */
@Entity
@Table(name = "t_user_similarity", indexes = {
        @Index(name = "idx_similarity_user", columnList = "user_id,similarity_score"),
        @Index(name = "idx_similarity_target", columnList = "similar_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class UserSimilarity extends BaseEntity {

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 相似用户ID
     */
    @Column(name = "similar_user_id", nullable = false)
    private Long similarUserId;

    /**
     * 相似度分数（0.0-1.0）
     */
    @Column(name = "similarity_score", nullable = false)
    private Double similarityScore;

    /**
     * 最后计算时间
     */
    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt;

    /**
     * 共同行为数量（用于解释相似度）
     */
    @Column(name = "common_behavior_count")
    private Integer commonBehaviorCount;
}
