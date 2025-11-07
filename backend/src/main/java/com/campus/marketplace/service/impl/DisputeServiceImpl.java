package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.DisputeDTO;
import com.campus.marketplace.common.dto.DisputeDetailDTO;
import com.campus.marketplace.common.dto.request.CreateDisputeRequest;
import com.campus.marketplace.common.entity.Dispute;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.*;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.DisputeRepository;
import com.campus.marketplace.service.*;
import lombok.RequiredArgsConstructor;
import com.campus.marketplace.websocket.DisputeWebSocketHandler;
import com.campus.marketplace.common.dto.websocket.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 纠纷核心业务服务实现
 *
 * 负责纠纷的创建、查询、状态变更等核心功能
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final com.campus.marketplace.repository.OrderRepository orderRepository;
    private final DisputeWebSocketHandler disputeWebSocketHandler;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    /**
     * 纠纷编号序列（用于生成唯一编号）
     */
    private static final AtomicLong DISPUTE_SEQUENCE = new AtomicLong(1);

    /**
     * 协商期限（48小时）
     */
    private static final int NEGOTIATION_DEADLINE_HOURS = 48;

    /**
     * 仲裁期限（7天）
     */
    private static final int ARBITRATION_DEADLINE_DAYS = 7;

    @Override
    @Transactional
    public Long submitDispute(CreateDisputeRequest request, Long userId) {
        log.info("用户提交纠纷: userId={}, orderId={}", userId, request.getOrderId());

        // 1. 验证订单是否已存在纠纷
        if (disputeRepository.existsByOrderId(request.getOrderId())) {
            log.warn("订单已存在纠纷: orderId={}", request.getOrderId());
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "该订单已存在纠纷");
        }

        // 2. 通过订单ID查询订单信息
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));

        // 3. 验证用户是否为订单参与方
        boolean isBuyer = order.getBuyerId().equals(userId);
        boolean isSeller = order.getSellerId().equals(userId);

        if (!isBuyer && !isSeller) {
            log.warn("用户不是订单参与方: userId={}, orderId={}", userId, request.getOrderId());
            throw new BusinessException(ErrorCode.FORBIDDEN, "您不是该订单的参与方，无权发起纠纷");
        }

        // 4. 确定发起人角色和对方ID
        DisputeRole initiatorRole = isBuyer ? DisputeRole.BUYER : DisputeRole.SELLER;
        Long respondentId = isBuyer ? order.getSellerId() : order.getBuyerId();

        // 5. 生成纠纷编号
        String disputeCode = generateDisputeCode();

        // 6. 创建纠纷记录
        Dispute dispute = Dispute.builder()
                .disputeCode(disputeCode)
                .orderId(request.getOrderId())
                .initiatorId(userId)
                .initiatorRole(initiatorRole)
                .respondentId(respondentId)
                .disputeType(request.getDisputeType())
                .description(request.getDescription())
                .status(DisputeStatus.SUBMITTED)
                .negotiationDeadline(LocalDateTime.now().plusHours(NEGOTIATION_DEADLINE_HOURS))
                .build();

        dispute = disputeRepository.save(dispute);
        log.info("纠纷创建成功: disputeId={}, disputeCode={}", dispute.getId(), dispute.getDisputeCode());

        // 7. 记录审计日志
        try {
            auditLogService.logEntityChange(
                    userId,
                    null, // username will be fetched by AuditLogService
                    AuditActionType.DISPUTE_CREATE,
                    "Dispute",
                    dispute.getId(),
                    null,
                    dispute
            );
        } catch (Exception e) {
            log.error("记录纠纷审计日志失败: disputeId={}", dispute.getId(), e);
        }

        // 8. 发送通知给对方
        try {
            notificationService.sendNotification(
                    respondentId,
                    NotificationType.DISPUTE_SUBMITTED,
                    "您有一个新的纠纷",
                    String.format("订单 %s 发起了纠纷，请及时处理", order.getOrderNo()),
                    dispute.getId(),
                    "Dispute",
                    "/disputes/" + dispute.getId()
            );
        } catch (Exception e) {
            log.error("发送纠纷通知失败: disputeId={}, respondentId={}", dispute.getId(), respondentId, e);
        }
        // 9. Send WebSocket real-time notification
        sendWebSocketNotification(
                respondentId,
                WebSocketMessage.TYPE_DISPUTE_CREATED,
                String.format("New dispute created for order %s", order.getOrderNo()),
                dispute.getId()
        );

        return dispute.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DisputeDTO> getUserDisputes(Long userId, DisputeStatus status, Pageable pageable) {
        log.debug("查询用户纠纷列表: userId={}, status={}", userId, status);

        Page<Dispute> disputes = disputeRepository.findByUserIdWithStatus(userId, status, pageable);
        return disputes.map(DisputeDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DisputeDTO> getArbitratorDisputes(Long arbitratorId, DisputeStatus status, Pageable pageable) {
        log.debug("查询仲裁员纠纷列表: arbitratorId={}, status={}", arbitratorId, status);

        // 调用 Repository 的按仲裁员ID查询方法 🎯
        Page<Dispute> disputes = disputeRepository.findByArbitratorIdWithStatus(arbitratorId, status, pageable);
        return disputes.map(DisputeDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public DisputeDetailDTO getDisputeDetail(Long disputeId) {
        log.debug("查询纠纷详情: disputeId={}", disputeId);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "纠纷不存在"));

        return DisputeDetailDTO.from(dispute);
    }

    @Override
    @Transactional
    public boolean escalateToArbitration(Long disputeId) {
        log.info("升级纠纷为仲裁状态: disputeId={}", disputeId);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "纠纷不存在"));

        // 验证状态（只允许协商中或已提交的纠纷升级）
        if (dispute.getStatus() != DisputeStatus.NEGOTIATING &&
            dispute.getStatus() != DisputeStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                "纠纷状态不允许升级为仲裁，当前状态: " + dispute.getStatus());
        }

        DisputeStatus oldStatus = dispute.getStatus();

        // 更新状态
        dispute.setStatus(DisputeStatus.PENDING_ARBITRATION);
        dispute.setArbitrationDeadline(LocalDateTime.now().plusDays(ARBITRATION_DEADLINE_DAYS));

        disputeRepository.save(dispute);
        log.info("纠纷已升级为待仲裁: disputeId={}, arbitrationDeadline={}",
                disputeId, dispute.getArbitrationDeadline());

        // 记录审计日志
        try {
            auditLogService.logEntityChange(
                    null, // 系统操作
                    "SYSTEM",
                    AuditActionType.DISPUTE_UPDATE,
                    "Dispute",
                    disputeId,
                    oldStatus,
                    DisputeStatus.PENDING_ARBITRATION
            );
        } catch (Exception e) {
            log.error("记录纠纷升级审计日志失败: disputeId={}", disputeId, e);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean closeDispute(Long disputeId, String closeReason) {
        log.info("关闭纠纷: disputeId={}, reason={}", disputeId, closeReason);

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "纠纷不存在"));

        DisputeStatus oldStatus = dispute.getStatus();

        // 更新状态
        dispute.setStatus(DisputeStatus.CLOSED);
        dispute.setCloseReason(closeReason);
        dispute.setClosedAt(LocalDateTime.now());

        disputeRepository.save(dispute);
        log.info("纠纷已关闭: disputeId={}, closedAt={}", disputeId, dispute.getClosedAt());

        // 记录审计日志
        try {
            auditLogService.logEntityChange(
                    null,
                    "SYSTEM",
                    AuditActionType.DISPUTE_CLOSE,
                    "Dispute",
                    disputeId,
                    oldStatus,
                    DisputeStatus.CLOSED
            );
        } catch (Exception e) {
            log.error("记录纠纷关闭审计日志失败: disputeId={}", disputeId, e);
        }

        return true;
    }

    @Override
    @Transactional
    public int markExpiredNegotiations() {
        log.info("开始标记协商期到期纠纷");

        List<Dispute> expiredDisputes = disputeRepository.findExpiredNegotiations(
                DisputeStatus.NEGOTIATING,
                LocalDateTime.now()
        );

        if (expiredDisputes.isEmpty()) {
            log.debug("没有协商期到期的纠纷");
            return 0;
        }

        // 批量升级为待仲裁
        expiredDisputes.forEach(dispute -> {
            dispute.setStatus(DisputeStatus.PENDING_ARBITRATION);
            dispute.setArbitrationDeadline(LocalDateTime.now().plusDays(ARBITRATION_DEADLINE_DAYS));
        });

        disputeRepository.saveAll(expiredDisputes);
        log.info("已标记{}个协商期到期纠纷为待仲裁", expiredDisputes.size());

        // 发送通知
        expiredDisputes.forEach(dispute -> {
            try {
                notificationService.sendNotification(
                        dispute.getInitiatorId(),
                        NotificationType.DISPUTE_ESCALATED,
                        "纠纷协商期已到期",
                        String.format("纠纷 %s 协商期已到期，已自动转入仲裁流程", dispute.getDisputeCode()),
                        dispute.getId(),
                        "Dispute",
                        "/disputes/" + dispute.getId()
                );
                notificationService.sendNotification(
                        dispute.getRespondentId(),
                        NotificationType.DISPUTE_ESCALATED,
                        "纠纷协商期已到期",
                        String.format("纠纷 %s 协商期已到期，已自动转入仲裁流程", dispute.getDisputeCode()),
                        dispute.getId(),
                        "Dispute",
                        "/disputes/" + dispute.getId()
                );
            } catch (Exception e) {
                log.error("发送协商期到期通知失败: disputeId={}", dispute.getId(), e);
            }
        });

        return expiredDisputes.size();
    }

    @Override
    @Transactional
    public int markExpiredArbitrations() {
        log.info("开始标记仲裁期到期纠纷");

        List<Dispute> expiredDisputes = disputeRepository.findExpiredArbitrations(
                DisputeStatus.ARBITRATING,
                LocalDateTime.now()
        );

        if (expiredDisputes.isEmpty()) {
            log.debug("没有仲裁期到期的纠纷");
            return 0;
        }

        // 批量关闭
        expiredDisputes.forEach(dispute -> {
            dispute.setStatus(DisputeStatus.CLOSED);
            dispute.setCloseReason("仲裁期到期，系统自动关闭");
            dispute.setClosedAt(LocalDateTime.now());
        });

        disputeRepository.saveAll(expiredDisputes);
        log.warn("已自动关闭{}个仲裁期到期纠纷", expiredDisputes.size());

        // 发送通知
        expiredDisputes.forEach(dispute -> {
            try {
                notificationService.sendNotification(
                        dispute.getInitiatorId(),
                        NotificationType.DISPUTE_CLOSED,
                        "纠纷已自动关闭",
                        String.format("纠纷 %s 仲裁期已到期，已自动关闭", dispute.getDisputeCode()),
                        dispute.getId(),
                        "Dispute",
                        "/disputes/" + dispute.getId()
                );
                if (dispute.getArbitratorId() != null) {
                    notificationService.sendNotification(
                            dispute.getArbitratorId(),
                            NotificationType.DISPUTE_CLOSED,
                            "仲裁任务已超时关闭",
                            String.format("纠纷 %s 仲裁期已到期，已自动关闭", dispute.getDisputeCode()),
                            dispute.getId(),
                            "Dispute",
                            "/disputes/" + dispute.getId()
                    );
                }
            } catch (Exception e) {
                log.error("发送仲裁期到期通知失败: disputeId={}", dispute.getId(), e);
            }
        });

        return expiredDisputes.size();
    }

    /**
     * 生成纠纷编号
     * 格式：DSP-YYYYMMDD-XXXXXX
     */
    /**
     * Send WebSocket notification to user
     *
     * @param userId user ID
     * @param type message type
     * @param content message content
     * @param disputeId dispute ID
     */
    private void sendWebSocketNotification(Long userId, String type, String content, Long disputeId) {
        try {
            WebSocketMessage wsMessage = WebSocketMessage.builder()
                    .type(type)
                    .content(content)
                    .messageId(disputeId)
                    .timestamp(System.currentTimeMillis())
                    .build();
            disputeWebSocketHandler.sendDisputeNotification(userId, wsMessage);
            log.debug("WebSocket notification sent: userId={}, type={}", userId, type);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification: userId={}, type={}", userId, type, e);
        }
    }


    private String generateDisputeCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequence = DISPUTE_SEQUENCE.getAndIncrement();
        return String.format("DSP-%s-%06d", datePart, sequence % 1000000);
    }
}
