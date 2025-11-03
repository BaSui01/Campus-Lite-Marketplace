package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.FeedType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 用户动态实体
 * 
 * 用户关注的人的动态流（发帖、评价、收藏等）
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_user_feed", indexes = {
    @Index(name = "idx_user_feed_user", columnList = "user_id"),
    @Index(name = "idx_user_feed_type", columnList = "feed_type"),
    @Index(name = "idx_user_feed_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFeed extends BaseEntity {

    /**
     * 用户 ID（关注者）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 动态类型（发帖、评价、收藏、点赞）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "feed_type", nullable = false, length = 20)
    private FeedType feedType;

    /**
     * 目标 ID（帖子ID、评价ID、收藏ID等）
     */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /**
     * 动态发起人 ID（被关注的用户）
     */
    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    /**
     * 用户（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 动态发起人（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", insertable = false, updatable = false)
    private User actor;
}
