package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 纠纷实体
 *
 * 纠纷主表，记录买卖双方的纠纷信息
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Entity
@Table(name = "t_dispute", indexes = {
    @Index(name = "idx_dispute_code", columnList = "dispute_code", unique = true),
    @Index(name = "idx_dispute_order", columnList = "order_id"),
    @Index(name = "idx_dispute_initiator", columnList = "initiator_id"),
    @Index(name = "idx_dispute_respondent", columnList = "respondent_id"),
    @Index(name = "idx_dispute_status", columnList = "status"),
    @Index(name = "idx_dispute_arbitrator", columnList = "arbitrator_id"),
    @Index(name = "idx_dispute_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute extends BaseEntity {

    /**
     * 纠纷编号（唯一）
     * 格式：DSP-YYYYMMDD-XXXXXX
     */
    @Column(name = "dispute_code", nullable = false, unique = true, length = 50)
    private String disputeCode;

    /**
     * 关联订单ID
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 订单（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    /**
     * 发起人ID（买家或卖家）
     */
    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;

    /**
     * 发起人（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", insertable = false, updatable = false)
    private User initiator;

    /**
     * 发起人角色
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "initiator_role", nullable = false, length = 20)
    private DisputeRole initiatorRole;

    /**
     * 被投诉人ID（买家或卖家）
     */
    @Column(name = "respondent_id", nullable = false)
    private Long respondentId;

    /**
     * 被投诉人（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondent_id", insertable = false, updatable = false)
    private User respondent;

    /**
     * 纠纷类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "dispute_type", nullable = false, length = 30)
    private DisputeType disputeType;

    /**
     * 纠纷描述
     */
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * 纠纷状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private DisputeStatus status = DisputeStatus.SUBMITTED;

    /**
     * 协商截止时间（48小时）
     */
    @Column(name = "negotiation_deadline")
    private LocalDateTime negotiationDeadline;

    /**
     * 仲裁截止时间（7天）
     */
    @Column(name = "arbitration_deadline")
    private LocalDateTime arbitrationDeadline;

    /**
     * 仲裁员ID
     */
    @Column(name = "arbitrator_id")
    private Long arbitratorId;

    /**
     * 仲裁员（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arbitrator_id", insertable = false, updatable = false)
    private User arbitrator;

    /**
     * 仲裁结果
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "arbitration_result", length = 30)
    private ArbitrationResult arbitrationResult;

    /**
     * 完成时间
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 关闭时间
     */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    /**
     * 关闭原因
     */
    @Column(name = "close_reason", columnDefinition = "TEXT")
    private String closeReason;

    /**
     * 检查是否处于协商阶段
     */
    public boolean isNegotiating() {
        return this.status == DisputeStatus.NEGOTIATING;
    }

    /**
     * 检查是否处于仲裁阶段
     */
    public boolean isArbitrating() {
        return this.status == DisputeStatus.ARBITRATING;
    }

    /**
     * 检查是否已完成
     */
    public boolean isCompleted() {
        return this.status == DisputeStatus.COMPLETED;
    }

    /**
     * 检查是否已关闭
     */
    public boolean isClosed() {
        return this.status == DisputeStatus.CLOSED;
    }

    /**
     * 检查协商是否超时
     */
    public boolean isNegotiationExpired() {
        return negotiationDeadline != null &&
               LocalDateTime.now().isAfter(negotiationDeadline);
    }

    /**
     * 检查仲裁是否超时
     */
    public boolean isArbitrationExpired() {
        return arbitrationDeadline != null &&
               LocalDateTime.now().isAfter(arbitrationDeadline);
    }
}
