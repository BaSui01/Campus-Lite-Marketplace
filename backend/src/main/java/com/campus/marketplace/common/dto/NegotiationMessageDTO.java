package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.entity.DisputeNegotiation;
import com.campus.marketplace.common.enums.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 协商消息DTO
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 纠纷ID
     */
    private Long disputeId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者昵称
     */
    private String senderNickname;

    /**
     * 发送者角色
     */
    private DisputeRole senderRole;

    /**
     * 消息类型
     */
    private NegotiationMessageType messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 提议退款金额（仅PROPOSAL类型）
     */
    private BigDecimal proposedRefundAmount;

    /**
     * 方案状态（仅PROPOSAL类型）
     */
    private ProposalStatus proposalStatus;

    /**
     * 方案响应时间
     */
    private LocalDateTime respondedAt;

    /**
     * 响应人ID
     */
    private Long respondedBy;

    /**
     * 响应人昵称
     */
    private String responderNickname;

    /**
     * 响应说明
     */
    private String responseNote;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 从实体转换为DTO
     */
    public static NegotiationMessageDTO from(DisputeNegotiation negotiation) {
        if (negotiation == null) {
            return null;
        }

        return NegotiationMessageDTO.builder()
                .id(negotiation.getId())
                .disputeId(negotiation.getDisputeId())
                .senderId(negotiation.getSenderId())
                .senderNickname(negotiation.getSender() != null ? negotiation.getSender().getNickname() : null)
                .senderRole(negotiation.getSenderRole())
                .messageType(negotiation.getMessageType())
                .content(negotiation.getContent())
                .proposedRefundAmount(negotiation.getProposedRefundAmount())
                .proposalStatus(negotiation.getProposalStatus())
                .respondedAt(negotiation.getRespondedAt())
                .respondedBy(negotiation.getRespondedBy())
                .responseNote(negotiation.getResponseNote())
                .createdAt(negotiation.getCreatedAt())
                .build();
    }

    /**
     * 检查是否为方案
     */
    public boolean isProposal() {
        return this.messageType == NegotiationMessageType.PROPOSAL;
    }

    /**
     * 检查方案是否已接受
     */
    public boolean isAccepted() {
        return this.proposalStatus == ProposalStatus.ACCEPTED;
    }
}
