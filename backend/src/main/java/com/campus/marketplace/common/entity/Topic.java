package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 话题实体
 * 
 * 社区广场的话题标签（#数码评测、#好物分享等）
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_topic", indexes = {
    @Index(name = "idx_topic_name", columnList = "name", unique = true),
    @Index(name = "idx_topic_hotness", columnList = "hotness")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic extends BaseEntity {

    /**
     * 话题名称（唯一）
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * 话题描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 话题热度（根据参与人数和讨论量计算）
     */
    @Column(name = "hotness", nullable = false)
    @Builder.Default
    private Integer hotness = 0;

    /**
     * 关联帖子数
     */
    @Column(name = "post_count", nullable = false)
    @Builder.Default
    private Integer postCount = 0;

    /**
     * 关注人数
     */
    @Column(name = "follower_count", nullable = false)
    @Builder.Default
    private Integer followerCount = 0;

    /**
     * 增加帖子数
     */
    public void incrementPostCount() {
        this.postCount++;
    }

    /**
     * 减少帖子数
     */
    public void decrementPostCount() {
        if (this.postCount > 0) {
            this.postCount--;
        }
    }

    /**
     * 增加关注人数
     */
    public void incrementFollowerCount() {
        this.followerCount++;
    }

    /**
     * 减少关注人数
     */
    public void decrementFollowerCount() {
        if (this.followerCount > 0) {
            this.followerCount--;
        }
    }

    /**
     * 更新热度（帖子数 * 10 + 关注人数）
     */
    public void updateHotness() {
        this.hotness = this.postCount * 10 + this.followerCount;
    }
}
