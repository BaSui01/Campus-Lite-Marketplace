package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.NegotiationMessageDTO;
import com.campus.marketplace.common.dto.request.ProposeDisputeRequest;
import com.campus.marketplace.common.dto.request.RespondProposalRequest;
import com.campus.marketplace.common.dto.request.SendNegotiationRequest;
import com.campus.marketplace.common.entity.Dispute;
import com.campus.marketplace.common.entity.DisputeNegotiation;
import com.campus.marketplace.common.enums.*;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.DisputeNegotiationRepository;
import com.campus.marketplace.repository.DisputeRepository;
import com.campus.marketplace.service.DisputeNegotiationService;
import com.campus.marketplace.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 纠纷协商服务实现
 *
 * 负责买卖双方的协商沟通、解决方案提议和响应
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeNegotiationServiceImpl implements DisputeNegotiationService {

    private final DisputeNegotiationRepository negotiationRepository;
    private final DisputeRepository disputeRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Long sendTextMessage(SendNegotiationRequest request, Long senderId) {
        log.info("用户发送协商消息: disputeId={}, senderId={}", request.disputeId(), senderId);

        // 1. 查询纠纷并验证权限
        Dispute dispute = validateDisputeAndPermission(request.disputeId(), senderId);

        // 2. 确定发送者角色
        DisputeRole senderRole = determineSenderRole(dispute, senderId);

        // 3. 创建文字消息
        DisputeNegotiation message = DisputeNegotiation.builder()
                .disputeId(request.disputeId())
                .senderId(senderId)
                .senderRole(senderRole)
                .messageType(NegotiationMessageType.TEXT)
                .content(request.content())
                .build();

        message = negotiationRepository.save(message);
        log.info("协商消息发送成功: messageId={}", message.getId());

        // 4. 通知对方
        Long receiverId = getOtherPartyId(dispute, senderId);
        sendMessageNotification(receiverId, dispute, "收到新的协商消息", request.content());

        return message.getId();
    }

    @Override
    @Transactional
    public Long proposeResolution(ProposeDisputeRequest request, Long proposerId) {
        log.info("用户提出解决方案: disputeId={}, proposerId={}", request.getDisputeId(), proposerId);

        // 1. 查询纠纷并验证权限
        Dispute dispute = validateDisputeAndPermission(request.getDisputeId(), proposerId);

        // 2. 检查是否已有待响应的方案
        if (negotiationRepository.hasPendingProposal(request.getDisputeId())) {
            log.warn("纠纷已有待响应方案: disputeId={}", request.getDisputeId());
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "已有待响应的方案，请等待对方响应后再提出新方案");
        }

        // 3. 确定提议人角色
        DisputeRole proposerRole = determineSenderRole(dispute, proposerId);

        // 4. 创建方案消息
        DisputeNegotiation proposal = DisputeNegotiation.builder()
                .disputeId(request.getDisputeId())
                .senderId(proposerId)
                .senderRole(proposerRole)
                .messageType(NegotiationMessageType.PROPOSAL)
                .content(request.getContent())
                .proposedRefundAmount(request.getProposedRefundAmount())
                .proposalStatus(ProposalStatus.PENDING)
                .build();

        proposal = negotiationRepository.save(proposal);
        log.info("解决方案提交成功: proposalId={}", proposal.getId());

        // 5. 通知对方
        Long receiverId = getOtherPartyId(dispute, proposerId);
        sendMessageNotification(
                receiverId,
                dispute,
                "收到新的解决方案",
                String.format("对方提出退款%s元的解决方案，请查看详情", request.getProposedRefundAmount())
        );

        return proposal.getId();
    }

    @Override
    @Transactional
    public boolean respondToProposal(RespondProposalRequest request, Long responderId) {
        log.info("用户响应方案: proposalId={}, responderId={}, accepted={}",
                request.getProposalId(), responderId, request.getAccepted());

        // 1. 查询方案
        DisputeNegotiation proposal = negotiationRepository.findById(request.getProposalId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "方案不存在"));

        // 2. 验证方案状态
        if (proposal.getProposalStatus() != ProposalStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "方案已被响应，无法重复操作");
        }

        // 3. 验证响应权限（必须是对方响应）
        if (proposal.getSenderId().equals(responderId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无法响应自己的方案");
        }

        // 4. 查询纠纷并验证权限
        Dispute dispute = validateDisputeAndPermission(proposal.getDisputeId(), responderId);

        // 5. 更新方案状态
        if (request.getAccepted()) {
            proposal.accept(responderId, request.getResponseNote());
            log.info("方案已接受: proposalId={}", proposal.getId());

            // 6. 更新纠纷状态为已解决
            dispute.setStatus(DisputeStatus.COMPLETED);
            dispute.setCompletedAt(java.time.LocalDateTime.now());
            disputeRepository.save(dispute);
            log.info("纠纷已解决: disputeId={}", dispute.getId());

            // 7. 通知提议人
            sendMessageNotification(
                    proposal.getSenderId(),
                    dispute,
                    "解决方案已被接受",
                    "您的解决方案已被接受，纠纷已解决"
            );
        } else {
            proposal.reject(responderId, request.getResponseNote());
            log.info("方案已拒绝: proposalId={}", proposal.getId());

            // 7. 通知提议人
            sendMessageNotification(
                    proposal.getSenderId(),
                    dispute,
                    "解决方案已被拒绝",
                    "对方拒绝了您的解决方案：" + request.getResponseNote()
            );
        }

        negotiationRepository.save(proposal);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NegotiationMessageDTO> getNegotiationHistory(Long disputeId) {
        log.debug("查询协商历史: disputeId={}", disputeId);

        List<DisputeNegotiation> messages = negotiationRepository
                .findByDisputeIdOrderByCreatedAtAsc(disputeId);

        return messages.stream()
                .map(NegotiationMessageDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NegotiationMessageDTO> getPendingProposal(Long disputeId) {
        log.debug("查询待响应方案: disputeId={}", disputeId);

        return negotiationRepository.findLatestPendingProposal(disputeId)
                .map(NegotiationMessageDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NegotiationMessageDTO> getAcceptedProposal(Long disputeId) {
        log.debug("查询已接受方案: disputeId={}", disputeId);

        return negotiationRepository.findAcceptedProposal(disputeId)
                .map(NegotiationMessageDTO::from);
    }

    /**
     * 验证纠纷存在且用户有权限参与
     */
    private Dispute validateDisputeAndPermission(Long disputeId, Long userId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "纠纷不存在"));

        // 验证用户是否为纠纷参与方
        if (!dispute.getInitiatorId().equals(userId) && !dispute.getRespondentId().equals(userId)) {
            log.warn("用户不是纠纷参与方: userId={}, disputeId={}", userId, disputeId);
            throw new BusinessException(ErrorCode.FORBIDDEN, "您不是该纠纷的参与方，无权操作");
        }

        // 验证纠纷状态
        if (dispute.getStatus() != DisputeStatus.NEGOTIATING &&
            dispute.getStatus() != DisputeStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "纠纷当前状态不允许协商，状态: " + dispute.getStatus());
        }

        return dispute;
    }

    /**
     * 确定发送者角色
     */
    private DisputeRole determineSenderRole(Dispute dispute, Long senderId) {
        if (dispute.getInitiatorId().equals(senderId)) {
            return dispute.getInitiatorRole();
        } else if (dispute.getRespondentId().equals(senderId)) {
            // 对方角色与发起人角色相反
            return dispute.getInitiatorRole() == DisputeRole.BUYER ?
                    DisputeRole.SELLER : DisputeRole.BUYER;
        }
        throw new BusinessException(ErrorCode.FORBIDDEN, "无法确定用户角色");
    }

    /**
     * 获取对方ID
     */
    private Long getOtherPartyId(Dispute dispute, Long userId) {
        if (dispute.getInitiatorId().equals(userId)) {
            return dispute.getRespondentId();
        } else {
            return dispute.getInitiatorId();
        }
    }

    /**
     * 发送消息通知
     */
    private void sendMessageNotification(Long receiverId, Dispute dispute, String title, String content) {
        try {
            notificationService.sendNotification(
                    receiverId,
                    NotificationType.DISPUTE_SUBMITTED, // 使用纠纷通知类型
                    title,
                    content,
                    dispute.getId(),
                    "Dispute",
                    "/disputes/" + dispute.getId()
            );
        } catch (Exception e) {
            log.error("发送协商通知失败: receiverId={}, disputeId={}", receiverId, dispute.getId(), e);
        }
    }
}
