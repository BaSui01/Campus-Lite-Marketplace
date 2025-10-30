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
@Table(name = "t_task_execution", indexes = {
        @Index(name = "idx_task_name_time", columnList = "taskName,startedAt")
})
/**
 * Task Execution
 *
 * @author BaSui
 * @date 2025-10-29
 */

public class TaskExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String taskName;

    @Column(nullable = false, length = 20)
    private String status; // RUNNING / SUCCESS / FAILED / CANCELLED

    @Column(columnDefinition = "TEXT")
    private String params;

    private Instant startedAt;
    private Instant endedAt;

    @Column(length = 64)
    private String node;

    @Column(columnDefinition = "TEXT")
    private String error;
}
