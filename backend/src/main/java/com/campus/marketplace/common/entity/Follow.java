package com.campus.marketplace.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * 用户关注关系。
 *
 * @author BaSui
 * @date 2025-10-28
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "t_seller_follow",
        uniqueConstraints = @UniqueConstraint(name = "uk_seller_follow_follower_seller", columnNames = {"follower_id", "seller_id"}),
        indexes = {
                @Index(name = "idx_seller_follow_follower", columnList = "follower_id"),
                @Index(name = "idx_seller_follow_seller", columnList = "seller_id")
        })
@SQLRestriction("deleted = false")
public class Follow extends BaseEntity {

    /**
     * 关注者用户 ID
     */
    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    /**
     * 被关注用户 ID
     */
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    /**
     * 关注者用户
     */
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "follower_id", insertable = false, updatable = false)
    private User follower;

    /**
     * 被关注用户
     */
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
    private User seller;
}
