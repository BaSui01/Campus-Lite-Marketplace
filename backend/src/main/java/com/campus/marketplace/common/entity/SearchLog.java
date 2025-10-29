package com.campus.marketplace.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_search_log", indexes = {
        @Index(name = "idx_search_created_at", columnList = "created_at"),
        @Index(name = "idx_search_campus", columnList = "campus_id")
})
/**
 * Search Log
 *
 * @author BaSui
 * @date 2025-10-29
 */

public class SearchLog extends BaseEntity {

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "keyword", length = 200, nullable = false)
    private String keyword;

    @Column(name = "campus_id")
    private Long campusId;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "result_count")
    private Long resultCount;
}
