package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_scheduled_task", indexes = {
        @Index(name = "uk_scheduled_task_name", columnList = "name", unique = true)
})
/**
 * Scheduled Task
 *
 * @author BaSui
 * @date 2025-10-29
 */

public class ScheduledTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String name;

    @Column(length = 20, nullable = false)
    private String status; // ENABLED / PAUSED

    @Column(length = 255)
    private String description;
}
