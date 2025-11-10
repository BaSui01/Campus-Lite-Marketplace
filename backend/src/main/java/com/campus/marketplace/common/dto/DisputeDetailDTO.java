package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.entity.Dispute;
import com.campus.marketplace.common.enums.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 纠纷详情DTO
 *
 * 包含完整信息、证据列表、协商记录、仲裁结果
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeDetailDTO implements Serializable {

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
     * 纠纷描述（完整）
     */
    private String description;

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
     * 仲裁员ID
     */
    private Long arbitratorId;

    /**
     * 仲裁员昵称
     */
    private String arbitratorNickname;

    /**
     * 仲裁结果
     */
    private ArbitrationResult arbitrationResult;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 关闭时间
     */
    private LocalDateTime closedAt;

    /**
     * 关闭原因
     */
    private String closeReason;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 证据列表
     */
    private List<EvidenceDTO> evidences;

    /**
     * 协商消息列表
     */
    private List<NegotiationMessageDTO> negotiations;

    /**
     * 仲裁信息
     */
    private ArbitrationDTO arbitration;

    /**
     * 从实体转换为DTO（不含关联数据）
     */
    public static DisputeDetailDTO from(Dispute dispute) {
        if (dispute == null) {
            return null;
        }

        return DisputeDetailDTO.builder()
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
                .description(dispute.getDescription())
                .status(dispute.getStatus())
                .negotiationDeadline(dispute.getNegotiationDeadline())
                .arbitrationDeadline(dispute.getArbitrationDeadline())
                .arbitratorId(dispute.getArbitratorId())
                .arbitratorNickname(dispute.getArbitrator() != null ? dispute.getArbitrator().getNickname() : null)
                .arbitrationResult(dispute.getArbitrationResult())
                .completedAt(dispute.getCompletedAt())
                .closedAt(dispute.getClosedAt())
                .closeReason(dispute.getCloseReason())
                .createdAt(dispute.getCreatedAt())
                .updatedAt(dispute.getUpdatedAt())
                .build();
    }
}
