package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.RevertRequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 撤销请求实体
 * 
 * 功能说明：
 * 1. 记录用户的撤销申请
 * 2. 支持审批流程
 * 3. 跟踪执行状态
 * 4. 关联审计日志
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_revert_request", indexes = {
        @Index(name = "idx_revert_request_audit_log", columnList = "audit_log_id"),
        @Index(name = "idx_revert_request_requester", columnList = "requester_id"),
        @Index(name = "idx_revert_request_status", columnList = "status"),
        @Index(name = "idx_revert_request_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevertRequest extends BaseEntity {

    /**
     * 关联的审计日志ID
     */
    @NotNull
    @Column(name = "audit_log_id", nullable = false)
    private Long auditLogId;

    /**
     * 申请人ID
     */
    @NotNull
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    /**
     * 申请人名称
     */
    @Column(name = "requester_name", length = 50)
    private String requesterName;

    /**
     * 撤销原因
     */
    @Column(name = "reason", length = 500)
    private String reason;

    /**
     * 请求状态
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RevertRequestStatus status = RevertRequestStatus.PENDING;

    /**
     * 审批人ID
     */
    @Column(name = "approved_by")
    private Long approvedBy;

    /**
     * 审批人名称
     */
    @Column(name = "approved_by_name", length = 50)
    private String approvedByName;

    /**
     * 审批时间
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * 审批意见
     */
    @Column(name = "approval_comment", length = 500)
    private String approvalComment;

    /**
     * 执行结果审计日志ID
     */
    @Column(name = "revert_log_id")
    private Long revertLogId;

    /**
     * 执行时间
     */
    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    /**
     * 执行失败原因
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /**
     * 批准请求
     */
    public void approve(Long approverId, String approverName, String comment) {
        this.status = RevertRequestStatus.APPROVED;
        this.approvedBy = approverId;
        this.approvedByName = approverName;
        this.approvedAt = LocalDateTime.now();
        this.approvalComment = comment;
    }

    /**
     * 拒绝请求
     */
    public void reject(Long approverId, String approverName, String comment) {
        this.status = RevertRequestStatus.REJECTED;
        this.approvedBy = approverId;
        this.approvedByName = approverName;
        this.approvedAt = LocalDateTime.now();
        this.approvalComment = comment;
    }

    /**
     * 标记为已执行
     */
    public void markExecuted(Long revertLogId) {
        this.status = RevertRequestStatus.EXECUTED;
        this.revertLogId = revertLogId;
        this.executedAt = LocalDateTime.now();
    }

    /**
     * 标记为执行失败
     */
    public void markFailed(String errorMessage) {
        this.status = RevertRequestStatus.FAILED;
        this.errorMessage = errorMessage;
        this.executedAt = LocalDateTime.now();
    }

    /**
     * 取消请求
     */
    public void cancel() {
        this.status = RevertRequestStatus.CANCELLED;
    }

    /**
     * 检查是否可以执行
     */
    public boolean canExecute() {
        return this.status == RevertRequestStatus.APPROVED;
    }

    /**
     * 检查是否可以取消
     */
    public boolean canCancel() {
        return this.status == RevertRequestStatus.PENDING;
    }
}
