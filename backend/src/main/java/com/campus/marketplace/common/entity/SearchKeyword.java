package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 热门搜索关键词实体
 * 
 * 记录热门搜索关键词及其搜索热度
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_search_keyword", indexes = {
    @Index(name = "idx_search_keyword_hotness", columnList = "search_count DESC"),
    @Index(name = "idx_search_keyword_time", columnList = "last_search_time DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchKeyword extends BaseEntity {

    /**
     * 搜索关键词
     */
    @Column(name = "keyword", nullable = false, unique = true, length = 100)
    private String keyword;

    /**
     * 搜索次数
     */
    @Column(name = "search_count", nullable = false)
    private Long searchCount;

    /**
     * 最后搜索时间
     */
    @Column(name = "last_search_time", nullable = false)
    private java.time.LocalDateTime lastSearchTime;

    /**
     * 增加搜索次数
     */
    public void incrementSearchCount() {
        this.searchCount++;
        this.lastSearchTime = java.time.LocalDateTime.now();
    }
}
