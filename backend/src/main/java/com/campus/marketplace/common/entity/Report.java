package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.ReportStatus;
import com.campus.marketplace.common.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 举报实体
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Entity
@Table(name = "t_report", indexes = {
        @Index(name = "idx_report_reporter", columnList = "reporter_id"),
        @Index(name = "idx_report_target", columnList = "target_type, target_id"),
        @Index(name = "idx_report_status", columnList = "status"),
        @Index(name = "idx_report_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {

    /**
     * 举报人 ID
     */
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    /**
     * 举报人（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", insertable = false, updatable = false)
    private User reporter;

    /**
     * 举报类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportType targetType;

    /**
     * 被举报对象 ID
     */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /**
     * 举报原因
     */
    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    /**
     * 举报状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    /**
     * 处理人 ID
     */
    @Column(name = "handler_id")
    private Long handlerId;

    /**
     * 处理人（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handler_id", insertable = false, updatable = false)
    private User handler;

    /**
     * 处理结果
     */
    @Column(name = "handle_result", length = 500)
    private String handleResult;

    /**
     * 检查是否已处理
     */
    public boolean isHandled() {
        return this.status == ReportStatus.HANDLED || this.status == ReportStatus.REJECTED;
    }
}
