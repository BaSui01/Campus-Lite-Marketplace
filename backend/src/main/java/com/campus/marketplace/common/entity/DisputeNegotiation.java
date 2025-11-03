package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 纠纷协商实体
 *
 * 存储买卖双方的协商消息和解决方案
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Entity
@Table(name = "t_dispute_negotiation", indexes = {
    @Index(name = "idx_negotiation_dispute", columnList = "dispute_id"),
    @Index(name = "idx_negotiation_sender", columnList = "sender_id"),
    @Index(name = "idx_negotiation_type", columnList = "message_type"),
    @Index(name = "idx_negotiation_status", columnList = "proposal_status"),
    @Index(name = "idx_negotiation_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeNegotiation extends BaseEntity {

    /**
     * 关联纠纷ID
     */
    @Column(name = "dispute_id", nullable = false)
    private Long disputeId;

    /**
     * 纠纷（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispute_id", insertable = false, updatable = false)
    private Dispute dispute;

    /**
     * 发送者ID（买家或卖家）
     */
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /**
     * 发送者（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    private User sender;

    /**
     * 发送者角色
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false, length = 20)
    private DisputeRole senderRole;

    /**
     * 消息类型（文字消息/解决方案）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private NegotiationMessageType messageType;

    /**
     * 消息内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 提议的退款金额（仅PROPOSAL类型）
     */
    @Column(name = "proposed_refund_amount", precision = 10, scale = 2)
    private BigDecimal proposedRefundAmount;

    /**
     * 方案状态（仅PROPOSAL类型）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "proposal_status", length = 20)
    private ProposalStatus proposalStatus;

    /**
     * 方案响应时间
     */
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    /**
     * 响应人ID
     */
    @Column(name = "responded_by")
    private Long respondedBy;

    /**
     * 响应说明
     */
    @Column(name = "response_note", columnDefinition = "TEXT")
    private String responseNote;

    /**
     * 检查是否为文字消息
     */
    public boolean isTextMessage() {
        return this.messageType == NegotiationMessageType.TEXT;
    }

    /**
     * 检查是否为解决方案
     */
    public boolean isProposal() {
        return this.messageType == NegotiationMessageType.PROPOSAL;
    }

    /**
     * 检查方案是否待响应
     */
    public boolean isPending() {
        return this.proposalStatus == ProposalStatus.PENDING;
    }

    /**
     * 检查方案是否已接受
     */
    public boolean isAccepted() {
        return this.proposalStatus == ProposalStatus.ACCEPTED;
    }

    /**
     * 检查方案是否已拒绝
     */
    public boolean isRejected() {
        return this.proposalStatus == ProposalStatus.REJECTED;
    }

    /**
     * 接受方案
     */
    public void accept(Long responderId, String note) {
        this.proposalStatus = ProposalStatus.ACCEPTED;
        this.respondedBy = responderId;
        this.respondedAt = LocalDateTime.now();
        this.responseNote = note;
    }

    /**
     * 拒绝方案
     */
    public void reject(Long responderId, String note) {
        this.proposalStatus = ProposalStatus.REJECTED;
        this.respondedBy = responderId;
        this.respondedAt = LocalDateTime.now();
        this.responseNote = note;
    }
}
