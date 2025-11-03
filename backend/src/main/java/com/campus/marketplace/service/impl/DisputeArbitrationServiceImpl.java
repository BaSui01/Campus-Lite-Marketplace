package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.ArbitrationDTO;
import com.campus.marketplace.common.dto.request.ArbitrateDisputeRequest;
import com.campus.marketplace.common.entity.Dispute;
import com.campus.marketplace.common.entity.DisputeArbitration;
import com.campus.marketplace.common.enums.*;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.DisputeArbitrationRepository;
import com.campus.marketplace.repository.DisputeRepository;
import com.campus.marketplace.service.AuditLogService;
import com.campus.marketplace.service.DisputeArbitrationService;
import com.campus.marketplace.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 纠纷仲裁服务实现
 *
 * 负责仲裁员分配、仲裁决定提交和执行
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeArbitrationServiceImpl implements DisputeArbitrationService {

    private final DisputeArbitrationRepository arbitrationRepository;
    private final DisputeRepository disputeRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    /**
     * 仲裁期限天数（从分配时开始计算）
     */
    private static final int ARBITRATION_DEADLINE_DAYS = 3;

    @Override
    @Transactional
    public boolean assignArbitrator(Long disputeId, Long arbitratorId) {
        log.info("分配仲裁员: disputeId={}, arbitratorId={}", disputeId, arbitratorId);

        // 1. 查询纠纷
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "纠纷不存在"));

        // 2. 验证纠纷状态
        if (dispute.getArbitratorId() != null) {
            log.warn("纠纷已分配仲裁员: disputeId={}, existingArbitratorId={}",
                    disputeId, dispute.getArbitratorId());
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "该纠纷已分配仲裁员");
        }

        // 3. 分配仲裁员并更新状态
        dispute.setArbitratorId(arbitratorId);
        dispute.setStatus(DisputeStatus.ARBITRATING);
        dispute.setArbitrationDeadline(LocalDateTime.now().plusDays(ARBITRATION_DEADLINE_DAYS));
        disputeRepository.save(dispute);

        log.info("仲裁员分配成功: disputeId={}, arbitratorId={}, deadline={}",
                disputeId, arbitratorId, dispute.getArbitrationDeadline());

        // 4. 记录审计日志
        auditLogService.logEntityChange(
                arbitratorId,
                "Dispute",
                AuditActionType.DISPUTE_UPDATE,
                "分配仲裁员",
                dispute.getId(),
                null,
                dispute
        );

        // 5. 通知买卖双方
        sendArbitratorAssignedNotification(dispute, arbitratorId);

        return true;
    }

    @Override
    @Transactional
    public Long submitArbitration(ArbitrateDisputeRequest request, Long arbitratorId) {
        log.info("提交仲裁决定: disputeId={}, arbitratorId={}, result={}",
                request.getDisputeId(), arbitratorId, request.getResult());

        // 1. 查询纠纷
        Dispute dispute = disputeRepository.findById(request.getDisputeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "纠纷不存在"));

        // 2. 验证仲裁员权限
        if (!arbitratorId.equals(dispute.getArbitratorId())) {
            log.warn("仲裁员无权限: userId={}, assignedArbitratorId={}",
                    arbitratorId, dispute.getArbitratorId());
            throw new BusinessException(ErrorCode.FORBIDDEN, "您不是该纠纷的仲裁员，无权操作");
        }

        // 3. 检查是否已有仲裁记录
        if (arbitrationRepository.existsByDisputeId(request.getDisputeId())) {
            log.warn("纠纷已有仲裁记录: disputeId={}", request.getDisputeId());
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "该纠纷已有仲裁记录，无法重复提交");
        }

        // 4. 验证退款金额
        validateRefundAmount(request);

        // 5. 创建仲裁记录
        DisputeArbitration arbitration = DisputeArbitration.builder()
                .disputeId(request.getDisputeId())
                .arbitratorId(arbitratorId)
                .result(request.getResult())
                .refundAmount(request.getRefundAmount())
                .reason(request.getReason())
                .buyerEvidenceAnalysis(request.getBuyerEvidenceAnalysis())
                .sellerEvidenceAnalysis(request.getSellerEvidenceAnalysis())
                .arbitratedAt(LocalDateTime.now())
                .executed(false)
                .build();

        arbitration = arbitrationRepository.save(arbitration);
        log.info("仲裁记录创建成功: arbitrationId={}", arbitration.getId());

        // 6. 更新纠纷状态
        dispute.setStatus(DisputeStatus.COMPLETED);
        dispute.setCompletedAt(LocalDateTime.now());
        disputeRepository.save(dispute);

        // 7. 记录审计日志
        auditLogService.logEntityChange(
                arbitratorId,
                "DisputeArbitration",
                AuditActionType.DISPUTE_UPDATE,
                "提交仲裁决定",
                dispute.getId(),
                null,
                arbitration
        );

        // 8. 通知买卖双方
        sendArbitrationResultNotification(dispute, arbitration);

        return arbitration.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ArbitrationDTO> getArbitrationDetail(Long disputeId) {
        log.debug("查询仲裁详情: disputeId={}", disputeId);

        return arbitrationRepository.findByDisputeId(disputeId)
                .map(ArbitrationDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArbitrationDTO> getArbitratorCases(Long arbitratorId) {
        log.debug("查询仲裁员案件列表: arbitratorId={}", arbitratorId);

        return arbitrationRepository.findByArbitratorIdOrderByArbitratedAtDesc(arbitratorId)
                .stream()
                .map(ArbitrationDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArbitrationDTO> getPendingExecutions() {
        log.debug("查询待执行仲裁列表");

        return arbitrationRepository.findPendingExecution()
                .stream()
                .map(ArbitrationDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean markExecuted(Long arbitrationId, String executionNote) {
        log.info("标记仲裁为已执行: arbitrationId={}", arbitrationId);

        // 1. 查询仲裁记录
        DisputeArbitration arbitration = arbitrationRepository.findById(arbitrationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "仲裁记录不存在"));

        // 2. 检查是否已执行
        if (arbitration.isExecuted()) {
            log.warn("仲裁已执行: arbitrationId={}", arbitrationId);
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "该仲裁已执行，无需重复操作");
        }

        // 3. 标记为已执行
        arbitration.markExecuted(executionNote);
        arbitrationRepository.save(arbitration);

        log.info("仲裁标记为已执行: arbitrationId={}, executedAt={}",
                arbitrationId, arbitration.getExecutedAt());

        return true;
    }

    /**
     * 验证退款金额
     */
    private void validateRefundAmount(ArbitrateDisputeRequest request) {
        if (request.getResult() == ArbitrationResult.FULL_REFUND ||
            request.getResult() == ArbitrationResult.PARTIAL_REFUND) {
            if (request.getRefundAmount() == null || request.getRefundAmount().signum() <= 0) {
                throw new BusinessException(ErrorCode.INVALID_PARAM,
                        "退款结果必须指定退款金额");
            }
        }
    }

    /**
     * 发送仲裁员分配通知
     */
    private void sendArbitratorAssignedNotification(Dispute dispute, Long arbitratorId) {
        try {
            // 通知买家
            notificationService.sendNotification(
                    dispute.getInitiatorId(),
                    NotificationType.DISPUTE_ESCALATED,
                    "纠纷已分配仲裁员",
                    "您的纠纷已升级至仲裁流程，仲裁员将在3个工作日内处理",
                    dispute.getId(),
                    "Dispute",
                    "/disputes/" + dispute.getId()
            );

            // 通知卖家
            notificationService.sendNotification(
                    dispute.getRespondentId(),
                    NotificationType.DISPUTE_ESCALATED,
                    "纠纷已分配仲裁员",
                    "纠纷已升级至仲裁流程，仲裁员将在3个工作日内处理",
                    dispute.getId(),
                    "Dispute",
                    "/disputes/" + dispute.getId()
            );
        } catch (Exception e) {
            log.error("发送仲裁员分配通知失败: disputeId={}", dispute.getId(), e);
        }
    }

    /**
     * 发送仲裁结果通知
     */
    private void sendArbitrationResultNotification(Dispute dispute, DisputeArbitration arbitration) {
        try {
            String resultMessage = formatArbitrationResult(arbitration);

            // 通知买家
            notificationService.sendNotification(
                    dispute.getInitiatorId(),
                    NotificationType.DISPUTE_RESOLVED,
                    "仲裁结果已出",
                    resultMessage,
                    dispute.getId(),
                    "Dispute",
                    "/disputes/" + dispute.getId()
            );

            // 通知卖家
            notificationService.sendNotification(
                    dispute.getRespondentId(),
                    NotificationType.DISPUTE_RESOLVED,
                    "仲裁结果已出",
                    resultMessage,
                    dispute.getId(),
                    "Dispute",
                    "/disputes/" + dispute.getId()
            );
        } catch (Exception e) {
            log.error("发送仲裁结果通知失败: disputeId={}", dispute.getId(), e);
        }
    }

    /**
     * 格式化仲裁结果消息
     */
    private String formatArbitrationResult(DisputeArbitration arbitration) {
        return switch (arbitration.getResult()) {
            case FULL_REFUND -> String.format("仲裁结果：全额退款%s元",
                    arbitration.getRefundAmount());
            case PARTIAL_REFUND -> String.format("仲裁结果：部分退款%s元",
                    arbitration.getRefundAmount());
            case REJECT -> "仲裁结果：驳回退款申请";
            case NEED_MORE_EVIDENCE -> "仲裁结果：需要补充证据";
        };
    }
}
