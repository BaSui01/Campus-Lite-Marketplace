package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.EvidenceDTO;
import com.campus.marketplace.common.dto.EvidenceSummaryDTO;
import com.campus.marketplace.common.dto.request.UploadEvidenceRequest;
import com.campus.marketplace.common.entity.Dispute;
import com.campus.marketplace.common.entity.DisputeEvidence;
import com.campus.marketplace.common.enums.*;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.DisputeEvidenceRepository;
import com.campus.marketplace.repository.DisputeRepository;
import com.campus.marketplace.service.AuditLogService;
import com.campus.marketplace.service.DisputeEvidenceService;
import com.campus.marketplace.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 纠纷证据服务实现
 *
 * 负责证据的上传、查询、评估和删除
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeEvidenceServiceImpl implements DisputeEvidenceService {

    private final DisputeEvidenceRepository evidenceRepository;
    private final DisputeRepository disputeRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public Long uploadEvidence(UploadEvidenceRequest request, Long uploaderId) {
        log.info("上传证据: disputeId={}, uploaderId={}, type={}",
                request.getDisputeId(), uploaderId, request.getEvidenceType());

        // 1. 查询纠纷并验证权限
        Dispute dispute = validateDisputeAndPermission(request.getDisputeId(), uploaderId);

        // 2. 确定上传者角色
        DisputeRole uploaderRole = determineUploaderRole(dispute, uploaderId);

        // 3. 创建证据记录
        DisputeEvidence evidence = DisputeEvidence.builder()
                .disputeId(request.getDisputeId())
                .uploaderId(uploaderId)
                .uploaderRole(uploaderRole)
                .evidenceType(request.getEvidenceType())
                .fileUrl(request.getFileUrl())
                .fileName(request.getFileName())
                .fileSize(request.getFileSize())
                .description(request.getDescription())
                .build();

        evidence = evidenceRepository.save(evidence);
        log.info("证据上传成功: evidenceId={}, type={}", evidence.getId(), evidence.getEvidenceType());

        // 4. 记录审计日志
        auditLogService.logEntityChange(
                uploaderId,
                "DisputeEvidence",
                AuditActionType.DISPUTE_UPDATE,
                "上传证据",
                dispute.getId(),
                null,
                evidence
        );

        // 5. 通知对方
        Long receiverId = getOtherPartyId(dispute, uploaderId);
        sendEvidenceUploadNotification(receiverId, dispute, uploaderRole);

        return evidence.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceDTO> getDisputeEvidence(Long disputeId) {
        log.debug("查询纠纷所有证据: disputeId={}", disputeId);

        return evidenceRepository.findByDisputeIdOrderByCreatedAtAsc(disputeId)
                .stream()
                .map(EvidenceDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceDTO> getBuyerEvidence(Long disputeId) {
        log.debug("查询买家证据: disputeId={}", disputeId);

        return evidenceRepository.findByDisputeIdAndUploaderRoleOrderByCreatedAtAsc(
                        disputeId, DisputeRole.BUYER)
                .stream()
                .map(EvidenceDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceDTO> getSellerEvidence(Long disputeId) {
        log.debug("查询卖家证据: disputeId={}", disputeId);

        return evidenceRepository.findByDisputeIdAndUploaderRoleOrderByCreatedAtAsc(
                        disputeId, DisputeRole.SELLER)
                .stream()
                .map(EvidenceDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean evaluateEvidence(Long evidenceId, EvidenceValidity validity,
                                    String reason, Long evaluatorId) {
        log.info("评估证据有效性: evidenceId={}, validity={}, evaluatorId={}",
                evidenceId, validity, evaluatorId);

        // 1. 查询证据
        DisputeEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "证据不存在"));

        // 2. 验证是否已评估
        if (evidence.getValidity() != null) {
            log.warn("证据已被评估: evidenceId={}, currentValidity={}",
                    evidenceId, evidence.getValidity());
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "该证据已被评估，无法重复操作");
        }

        // 3. 更新有效性评估
        evidence.setValidity(validity);
        evidence.setValidityReason(reason);
        evidence.setEvaluatedBy(evaluatorId);
        evidenceRepository.save(evidence);

        log.info("证据评估完成: evidenceId={}, validity={}", evidenceId, validity);

        // 4. 记录审计日志
        auditLogService.logEntityChange(
                evaluatorId,
                "DisputeEvidence",
                AuditActionType.DISPUTE_UPDATE,
                "评估证据有效性",
                evidence.getId(),
                null,
                evidence
        );

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceSummaryDTO getEvidenceSummary(Long disputeId) {
        log.debug("查询证据统计: disputeId={}", disputeId);

        long totalCount = evidenceRepository.countByDisputeId(disputeId);
        long buyerCount = evidenceRepository.countByDisputeIdAndUploaderRole(disputeId, DisputeRole.BUYER);
        long sellerCount = evidenceRepository.countByDisputeIdAndUploaderRole(disputeId, DisputeRole.SELLER);
        long validCount = evidenceRepository.countByDisputeIdAndValidity(disputeId, EvidenceValidity.VALID);
        long invalidCount = evidenceRepository.countByDisputeIdAndValidity(disputeId, EvidenceValidity.INVALID);
        long doubtfulCount = evidenceRepository.countByDisputeIdAndValidity(disputeId, EvidenceValidity.DOUBTFUL);
        long unevaluatedCount = evidenceRepository.findUnevaluatedEvidence(disputeId).size();

        return EvidenceSummaryDTO.builder()
                .disputeId(disputeId)
                .totalCount(totalCount)
                .buyerEvidenceCount(buyerCount)
                .sellerEvidenceCount(sellerCount)
                .validEvidenceCount(validCount)
                .invalidEvidenceCount(invalidCount)
                .doubtfulEvidenceCount(doubtfulCount)
                .unevaluatedEvidenceCount(unevaluatedCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceDTO> getUnevaluatedEvidence(Long disputeId) {
        log.debug("查询待评估证据: disputeId={}", disputeId);

        return evidenceRepository.findUnevaluatedEvidence(disputeId)
                .stream()
                .map(EvidenceDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteEvidence(Long evidenceId, Long userId) {
        log.info("删除证据: evidenceId={}, userId={}", evidenceId, userId);

        // 1. 查询证据
        DisputeEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "证据不存在"));

        // 2. 验证权限
        if (!evidence.getUploaderId().equals(userId)) {
            log.warn("用户无权删除证据: userId={}, uploaderId={}", userId, evidence.getUploaderId());
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能删除自己上传的证据");
        }

        // 3. 验证是否已评估
        if (evidence.getValidity() != null) {
            log.warn("已评估的证据不能删除: evidenceId={}", evidenceId);
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "已评估的证据不能删除");
        }

        // 4. 删除证据
        evidenceRepository.delete(evidence);
        log.info("证据删除成功: evidenceId={}", evidenceId);

        // 5. 记录审计日志
        auditLogService.logEntityChange(
                userId,
                "DisputeEvidence",
                AuditActionType.DISPUTE_UPDATE,
                "删除证据",
                evidence.getId(),
                evidence,
                null
        );

        return true;
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

        return dispute;
    }

    /**
     * 确定上传者角色
     */
    private DisputeRole determineUploaderRole(Dispute dispute, Long uploaderId) {
        if (dispute.getInitiatorId().equals(uploaderId)) {
            return dispute.getInitiatorRole();
        } else if (dispute.getRespondentId().equals(uploaderId)) {
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
     * 发送证据上传通知
     */
    private void sendEvidenceUploadNotification(Long receiverId, Dispute dispute, DisputeRole uploaderRole) {
        try {
            String uploaderRoleName = uploaderRole == DisputeRole.BUYER ? "买家" : "卖家";
            notificationService.sendNotification(
                    receiverId,
                    NotificationType.DISPUTE_SUBMITTED,
                    "对方上传了新证据",
                    String.format("%s上传了新的证据材料，请查看", uploaderRoleName),
                    dispute.getId(),
                    "Dispute",
                    "/disputes/" + dispute.getId()
            );
        } catch (Exception e) {
            log.error("发送证据上传通知失败: disputeId={}", dispute.getId(), e);
        }
    }
}
