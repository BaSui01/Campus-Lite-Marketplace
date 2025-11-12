package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * 帖子与标签关联实体
 *
 * 🎯 功能：管理帖子和标签的多对多关系
 * 📋 参考：GoodsTag（商品-标签关联）
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "t_post_tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_post_tag", columnNames = {"post_id", "tag_id"}),
        indexes = {
                @Index(name = "idx_post_tag_post", columnList = "post_id"),
                @Index(name = "idx_post_tag_tag", columnList = "tag_id")
        })
@SQLRestriction("deleted = false")
public class PostTag extends BaseEntity {

    /**
     * 帖子 ID
     */
    @Column(name = "post_id", nullable = false)
    private Long postId;

    /**
     * 标签 ID
     */
    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    /**
     * 帖子（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    /**
     * 标签（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private Tag tag;
}
