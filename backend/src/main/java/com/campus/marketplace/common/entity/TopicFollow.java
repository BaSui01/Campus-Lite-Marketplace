package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 话题关注实体
 * 
 * 用户对话题的关注记录
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_topic_follow", indexes = {
    @Index(name = "idx_topic_follow_topic", columnList = "topic_id"),
    @Index(name = "idx_topic_follow_user", columnList = "user_id"),
    @Index(name = "idx_topic_follow_unique", columnList = "topic_id, user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicFollow extends BaseEntity {

    /**
     * 话题 ID
     */
    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    /**
     * 用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 话题（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", insertable = false, updatable = false)
    private Topic topic;

    /**
     * 用户（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
