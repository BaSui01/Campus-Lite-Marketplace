package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 帖子回复实体
 * 
 * 存储帖子回复信息，支持楼中楼（parent_id）
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Entity
@Table(name = "t_reply", indexes = {
        @Index(name = "idx_reply_post", columnList = "post_id"),
        @Index(name = "idx_reply_author", columnList = "author_id"),
        @Index(name = "idx_reply_parent", columnList = "parent_id"),
        @Index(name = "idx_reply_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reply extends BaseEntity {

    /**
     * 帖子 ID
     */
    @Column(name = "post_id", nullable = false)
    private Long postId;

    /**
     * 帖子（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    /**
     * 回复内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 作者 ID
     */
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    /**
     * 作者（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private User author;

    /**
     * 父回复 ID（楼中楼）
     * null 表示直接回复帖子
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 父回复（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Reply parent;

    /**
     * 回复目标用户 ID（楼中楼时有值）
     */
    @Column(name = "to_user_id")
    private Long toUserId;

    /**
     * 回复目标用户（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", insertable = false, updatable = false)
    private User toUser;

    /**
     * 点赞数
     */
    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    /**
     * 增加点赞数
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /**
     * 减少点赞数
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * 检查是否为楼中楼回复
     */
    public boolean isSubReply() {
        return this.parentId != null;
    }
}