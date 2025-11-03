package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 话题标签关联实体
 * 
 * 帖子与话题的多对多关联表
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_topic_tag", indexes = {
    @Index(name = "idx_topic_tag_post", columnList = "post_id"),
    @Index(name = "idx_topic_tag_topic", columnList = "topic_id"),
    @Index(name = "idx_topic_tag_unique", columnList = "post_id, topic_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicTag extends BaseEntity {

    /**
     * 帖子 ID
     */
    @Column(name = "post_id", nullable = false)
    private Long postId;

    /**
     * 话题 ID
     */
    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    /**
     * 帖子（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    /**
     * 话题（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", insertable = false, updatable = false)
    private Topic topic;
}
