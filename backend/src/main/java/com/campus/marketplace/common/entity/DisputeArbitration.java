package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.ArbitrationResult;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 纠纷仲裁实体
 *
 * 存储仲裁员的仲裁决定和结果
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Entity
@Table(name = "t_dispute_arbitration", indexes = {
    @Index(name = "idx_arbitration_dispute", columnList = "dispute_id", unique = true),
    @Index(name = "idx_arbitration_arbitrator", columnList = "arbitrator_id"),
    @Index(name = "idx_arbitration_result", columnList = "result"),
    @Index(name = "idx_arbitration_time", columnList = "arbitrated_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeArbitration extends BaseEntity {

    /**
     * 关联纠纷ID（一对一关系）
     */
    @Column(name = "dispute_id", nullable = false, unique = true)
    private Long disputeId;

    /**
     * 纠纷（懒加载）
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispute_id", insertable = false, updatable = false)
    private Dispute dispute;

    /**
     * 仲裁员ID
     */
    @Column(name = "arbitrator_id", nullable = false)
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
    @Column(name = "result", nullable = false, length = 30)
    private ArbitrationResult result;

    /**
     * 退款金额
     */
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    /**
     * 仲裁理由
     */
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    /**
     * 买家证据分析
     */
    @Column(name = "buyer_evidence_analysis", columnDefinition = "TEXT")
    private String buyerEvidenceAnalysis;

    /**
     * 卖家证据分析
     */
    @Column(name = "seller_evidence_analysis", columnDefinition = "TEXT")
    private String sellerEvidenceAnalysis;

    /**
     * 仲裁时间
     */
    @Column(name = "arbitrated_at", nullable = false)
    private LocalDateTime arbitratedAt;

    /**
     * 执行状态（是否已执行退款等操作）
     */
    @Column(name = "executed", nullable = false)
    @Builder.Default
    private boolean executed = false;

    /**
     * 执行时间
     */
    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    /**
     * 执行说明
     */
    @Column(name = "execution_note", columnDefinition = "TEXT")
    private String executionNote;

    /**
     * 检查是否全额退款
     */
    public boolean isFullRefund() {
        return this.result == ArbitrationResult.FULL_REFUND;
    }

    /**
     * 检查是否部分退款
     */
    public boolean isPartialRefund() {
        return this.result == ArbitrationResult.PARTIAL_REFUND;
    }

    /**
     * 检查是否驳回申请
     */
    public boolean isRejected() {
        return this.result == ArbitrationResult.REJECT;
    }

    /**
     * 检查是否需要补充证据
     */
    public boolean needsMoreEvidence() {
        return this.result == ArbitrationResult.NEED_MORE_EVIDENCE;
    }

    /**
     * 检查是否需要退款
     */
    public boolean requiresRefund() {
        return this.result == ArbitrationResult.FULL_REFUND ||
               this.result == ArbitrationResult.PARTIAL_REFUND;
    }

    /**
     * 检查是否已执行
     */
    public boolean isExecuted() {
        return this.executed;
    }

    /**
     * 标记为已执行
     */
    public void markExecuted(String note) {
        this.executed = true;
        this.executedAt = LocalDateTime.now();
        this.executionNote = note;
    }
}
