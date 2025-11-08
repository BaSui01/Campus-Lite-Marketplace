package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 消息搜索统计实体
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */
@Entity
@Table(name = "message_search_statistics",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "dispute_id", "search_date"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchStatisticsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "dispute_id", nullable = false)
    private Long disputeId;

    @Column(name = "search_date", nullable = false)
    private LocalDate searchDate;

    @Column(name = "total_searches", nullable = false)
    private Integer totalSearches;

    @Column(name = "successful_searches", nullable = false)
    private Integer successfulSearches;

    @Column(name = "popular_keywords", columnDefinition = "TEXT")
    private String popularKeywords;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}