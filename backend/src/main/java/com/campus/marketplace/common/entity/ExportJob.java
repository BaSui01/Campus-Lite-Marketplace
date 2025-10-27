package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_export_job", indexes = {
        @Index(name = "idx_export_status_time", columnList = "status,createdAt")
})
public class ExportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String type; // GOODS / ORDERS / USERS

    @Column(columnDefinition = "TEXT")
    private String paramsJson;

    @Column(nullable = false, length = 20)
    private String status; // PENDING / RUNNING / SUCCESS / FAILED / CANCELLED

    private String filePath;
    private Long fileSize;

    private String requestedBy;
    private Long campusId;

    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;

    @Column(length = 64)
    private String downloadToken;
    private Instant expireAt;

    @Column(columnDefinition = "TEXT")
    private String error;
}
