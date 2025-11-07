package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 消息搜索历史实体
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */
@Entity
@Table(name = "message_search_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchHistoryEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "dispute_id", nullable = false)
    private Long disputeId;

    @Column(name = "keyword", length = 200, nullable = false)
    private String keyword;

    @Column(name = "result_count", nullable = false)
    private Integer resultCount;

    @Column(name = "filters", columnDefinition = "TEXT")
    @Type(com.hibernate.type.JdbcType.JSON)
    private Map<String, Object> filters;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (searchedAt == null) {
            searchedAt = LocalDateTime.now();
        }
    }

    @Builder
    public static class MessageSearchHistoryEntityBuilder {
        private String id = UUID.randomUUID().toString();
        private LocalDateTime searchedAt = LocalDateTime.now();
    }
}