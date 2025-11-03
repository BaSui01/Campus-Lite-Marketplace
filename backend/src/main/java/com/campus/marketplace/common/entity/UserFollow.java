package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 用户关注实体
 * 
 * 用户对用户的关注记录（关注关系）
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_user_follow", indexes = {
    @Index(name = "idx_user_follow_follower", columnList = "follower_id"),
    @Index(name = "idx_user_follow_following", columnList = "following_id"),
    @Index(name = "idx_user_follow_unique", columnList = "follower_id, following_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFollow extends BaseEntity {

    /**
     * 关注者 ID（粉丝）
     */
    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    /**
     * 被关注者 ID（关注的人）
     */
    @Column(name = "following_id", nullable = false)
    private Long followingId;

    /**
     * 关注者（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", insertable = false, updatable = false)
    private User follower;

    /**
     * 被关注者（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", insertable = false, updatable = false)
    private User following;
}
