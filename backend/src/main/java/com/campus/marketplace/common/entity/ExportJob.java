package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 数据导出任务实体。
 *
 * 记录导出作业的类型、参数、状态与产物信息。
 *
 * @author BaSui
 * @date 2025-10-28
 */
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

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 导出类型（GOODS/ORDERS/USERS 等）
     */
    @Column(nullable = false, length = 32)
    private String type;

    /**
     * 导出参数（JSON）
     */
    @Column(columnDefinition = "TEXT")
    private String paramsJson;

    /**
     * 作业状态（PENDING/RUNNING/SUCCESS/FAILED/CANCELLED）
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * 导出文件路径
     */
    private String filePath;

    /**
     * 导出文件大小（字节）
     */
    private Long fileSize;

    /**
     * 请求发起人用户名
     */
    private String requestedBy;

    /**
     * 归属校区 ID（可选）
     */
    private Long campusId;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 开始时间
     */
    private Instant startedAt;

    /**
     * 完成时间
     */
    private Instant completedAt;

    /**
     * 下载口令（短期有效）
     */
    @Column(length = 64)
    private String downloadToken;

    /**
     * 下载口令过期时间
     */
    private Instant expireAt;

    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String error;
}
