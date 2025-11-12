package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.entity.Dispute;
import com.campus.marketplace.common.enums.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 纠纷基本信息DTO
 *
 * 用于列表展示，不包含详细信息
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 纠纷ID
     */
    private Long id;

    /**
     * 纠纷编号
     */
    private String disputeCode;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 涉及金额（从订单中获取）
     */
    private BigDecimal amount;

    /**
     * 发起人ID
     */
    private Long initiatorId;

    /**
     * 发起人昵称
     */
    private String initiatorNickname;

    /**
     * 发起人角色
     */
    private DisputeRole initiatorRole;

    /**
     * 被投诉人ID
     */
    private Long respondentId;

    /**
     * 被投诉人昵称
     */
    private String respondentNickname;

    /**
     * 纠纷类型
     */
    private DisputeType disputeType;

    /**
     * 纠纷描述（摘要，最多100字符）
     */
    private String descriptionSummary;

    /**
     * 纠纷状态
     */
    private DisputeStatus status;

    /**
     * 协商截止时间
     */
    private LocalDateTime negotiationDeadline;

    /**
     * 仲裁截止时间
     */
    private LocalDateTime arbitrationDeadline;

    /**
     * 仲裁结果
     */
    private ArbitrationResult arbitrationResult;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为DTO
     */
    public static DisputeDTO from(Dispute dispute) {
        if (dispute == null) {
            return null;
        }

        // 截取描述为摘要
        String descriptionSummary = dispute.getDescription();
        if (descriptionSummary != null && descriptionSummary.length() > 100) {
            descriptionSummary = descriptionSummary.substring(0, 100) + "...";
        }

        return DisputeDTO.builder()
                .id(dispute.getId())
                .disputeCode(dispute.getDisputeCode())
                .orderId(dispute.getOrderId())
                .orderNo(dispute.getOrder() != null ? dispute.getOrder().getOrderNo() : null)
                .amount(dispute.getOrder() != null ? dispute.getOrder().getAmount() : null)
                .initiatorId(dispute.getInitiatorId())
                .initiatorNickname(dispute.getInitiator() != null ? dispute.getInitiator().getNickname() : null)
                .initiatorRole(dispute.getInitiatorRole())
                .respondentId(dispute.getRespondentId())
                .respondentNickname(dispute.getRespondent() != null ? dispute.getRespondent().getNickname() : null)
                .disputeType(dispute.getDisputeType())
                .descriptionSummary(descriptionSummary)
                .status(dispute.getStatus())
                .negotiationDeadline(dispute.getNegotiationDeadline())
                .arbitrationDeadline(dispute.getArbitrationDeadline())
                .arbitrationResult(dispute.getArbitrationResult())
                .createdAt(dispute.getCreatedAt())
                .updatedAt(dispute.getUpdatedAt())
                .build();
    }

    /**
     * 检查是否协商中
     */
    public boolean isNegotiating() {
        return this.status == DisputeStatus.NEGOTIATING;
    }

    /**
     * 检查是否仲裁中
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
}
