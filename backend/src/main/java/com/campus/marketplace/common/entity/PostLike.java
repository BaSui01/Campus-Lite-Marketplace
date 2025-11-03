package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 帖子点赞实体
 * 
 * 用户对帖子的点赞记录
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_post_like", indexes = {
    @Index(name = "idx_post_like_post", columnList = "post_id"),
    @Index(name = "idx_post_like_user", columnList = "user_id"),
    @Index(name = "idx_post_like_unique", columnList = "post_id, user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLike extends BaseEntity {

    /**
     * 帖子 ID
     */
    @Column(name = "post_id", nullable = false)
    private Long postId;

    /**
     * 用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 帖子（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    /**
     * 用户（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
