package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.GoodsStatus;
import org.hibernate.annotations.Where;
import jakarta.persistence.*;
import lombok.*;

/**
 * 帖子实体
 * 
 * 存储论坛帖子信息
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Entity
@Table(name = "t_post", indexes = {
        @Index(name = "idx_post_author", columnList = "author_id"),
        @Index(name = "idx_post_status", columnList = "status"),
        @Index(name = "idx_post_created_at", columnList = "created_at"),
        @Index(name = "idx_post_campus", columnList = "campus_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted = false")
public class Post extends BaseEntity {

    /**
     * 帖子标题
     */
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * 帖子内容
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
     * 校区 ID
     */
    @Column(name = "campus_id")
    private Long campusId;

    /**
     * 校区（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", insertable = false, updatable = false)
    private Campus campus;

    /**
     * 帖子状态（复用物品状态枚举）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private GoodsStatus status = GoodsStatus.PENDING;

    /**
     * 浏览量
     */
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    /**
     * 回复数
     */
    @Column(name = "reply_count", nullable = false)
    @Builder.Default
    private Integer replyCount = 0;

    /**
     * 图片 URL 数组
     */
    @Column(name = "images", columnDefinition = "TEXT[]")
    private String[] images;

    /**
     * 增加浏览量
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 增加回复数
     */
    public void incrementReplyCount() {
        this.replyCount++;
    }

    /**
     * 检查是否已审核通过
     */
    public boolean isApproved() {
        return this.status == GoodsStatus.APPROVED;
    }
}
