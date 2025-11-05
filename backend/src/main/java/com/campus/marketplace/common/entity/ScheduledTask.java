package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * Scheduled Task - 定时任务配置
 * 
 * 管理系统中的定时任务（如：缓存预热、数据统计、消息推送等）
 * 支持软删除，便于任务历史追踪和恢复
 *
 * @author BaSui
 * @date 2025-10-29
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_scheduled_task", indexes = {
        @Index(name = "uk_scheduled_task_name", columnList = "name", unique = true)
})
@SQLRestriction("deleted = false")
public class ScheduledTask extends BaseEntity {

    /**
     * 任务名称（唯一标识）
     */
    @Column(nullable = false, unique = true, length = 128)
    private String name;

    /**
     * 任务状态
     * ENABLED - 启用
     * PAUSED - 暂停
     */
    @Column(length = 20, nullable = false)
    private String status;

    /**
     * 任务描述
     */
    @Column(length = 255)
    private String description;
}
