package com.campus.marketplace.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 关键词订阅
 *
 * 用户可按关键词+校区订阅新品推送
 *
 * @author Codex
 * @since 2025-10-28
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "t_subscription",
        uniqueConstraints = @UniqueConstraint(name = "uk_subscription_user_keyword_campus", columnNames = {"user_id", "keyword", "campus_id"}),
        indexes = {
                @Index(name = "idx_subscription_user", columnList = "user_id"),
                @Index(name = "idx_subscription_keyword", columnList = "keyword")
        })
public class Subscription extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "keyword", nullable = false, length = 100)
    private String keyword;

    @Column(name = "campus_id")
    private Long campusId;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = Boolean.TRUE;
}
