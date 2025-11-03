package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 搜索历史实体
 * 
 * 记录用户的搜索历史，用于智能搜索提示
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_search_history", indexes = {
    @Index(name = "idx_search_history_user", columnList = "user_id"),
    @Index(name = "idx_search_history_keyword", columnList = "keyword"),
    @Index(name = "idx_search_history_time", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory extends BaseEntity {

    /**
     * 用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 搜索关键词
     */
    @Column(name = "keyword", nullable = false, length = 100)
    private String keyword;

    /**
     * 搜索结果数量
     */
    @Column(name = "result_count", nullable = false)
    private Integer resultCount;

    /**
     * 是否有点击行为
     */
    @Column(name = "has_click", nullable = false)
    private Boolean hasClick;

    /**
     * 用户（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
