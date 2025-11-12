package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.PaymentLog;
import com.campus.marketplace.common.entity.RefundRequest;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.enums.PaymentLogType;
import com.campus.marketplace.common.enums.PaymentMethod;
import com.campus.marketplace.common.enums.RefundStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.PaymentLogRepository;
import com.campus.marketplace.repository.RefundRequestRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.AuditLogService;
import com.campus.marketplace.service.NotificationService;
import com.campus.marketplace.service.PaymentService;
import com.campus.marketplace.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Refund Service Impl
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundRequestRepository refundRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String applyRefund(String orderNo, String reason, Map<String, Object> evidence) {
        String username = SecurityUtil.getCurrentUsername();
        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyerId().equals(buyer.getId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "只能为自己的订单申请退款");
        }

        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "订单状态不支持退款");
        }

        if (refundRepository.existsActiveByOrderNo(orderNo)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "该订单已有进行中的退款");
        }

        String refundNo = generateRefundNo();
        RefundRequest refund = RefundRequest.builder()
                .refundNo(refundNo)
                .orderNo(orderNo)
                .applicantId(buyer.getId())
                .reason(reason)
                .evidence(evidence)
                .status(RefundStatus.APPLIED)
                .channel(order.getPaymentMethod())
                .amount(order.getActualAmount())
                .build();
        refundRepository.save(refund);

        notificationService.sendNotification(order.getSellerId(),
                com.campus.marketplace.common.enums.NotificationType.SYSTEM_ANNOUNCEMENT,
                "买家发起退款申请",
                "订单 " + orderNo + " 发起退款，原因：" + reason,
                order.getId(), "ORDER", "/orders/" + orderNo);

        auditLogService.logActionAsync(buyer.getId(), buyer.getUsername(),
                com.campus.marketplace.common.enums.AuditActionType.ORDER_CANCEL,
                "REFUND", refund.getId(), "APPLY", "SUCCESS", null, null);

        return refundNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveAndRefund(String refundNo) {
        RefundRequest refund = refundRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退款不存在"));

        if (refund.getStatus() == RefundStatus.REFUNDED) return; // 幂等
        if (refund.getStatus() == RefundStatus.REJECTED) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "已被拒绝");
        }

        Order order = orderRepository.findByOrderNo(refund.getOrderNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        refund.setStatus(RefundStatus.PROCESSING);
        refundRepository.save(refund);

        try {
            // 发起渠道退款（根据支付方式路由）
            PaymentMethod method = PaymentMethod.valueOf(order.getPaymentMethod());
            boolean ok = paymentService.refund(order, refund.getAmount(), method);
            if (!ok) {
                throw new RuntimeException("channel refund failed");
            }

            refund.setStatus(RefundStatus.REFUNDED);
            refundRepository.save(refund);

            // 支付日志
            Map<String, Object> payload = new HashMap<>();
            payload.put("refundNo", refundNo);
            payload.put("amount", refund.getAmount());
            PaymentLog logEntry = PaymentLog.builder()
                    .orderNo(order.getOrderNo())
                    .tradeNo(null)
                    .channel(order.getPaymentMethod())
                    .type(PaymentLogType.REFUND)
                    .payload(payload)
                    .success(true)
                    .build();
            paymentLogRepository.save(logEntry);

            // 通知买家
            notificationService.sendNotification(order.getBuyerId(),
                    com.campus.marketplace.common.enums.NotificationType.SYSTEM_ANNOUNCEMENT,
                    "退款成功", "订单 " + order.getOrderNo() + " 退款已完成",
                    order.getId(), "ORDER", "/orders/" + order.getOrderNo());

        } catch (Exception e) {
            log.error("渠道退款失败: refundNo={}", refundNo, e);
            refund.setStatus(RefundStatus.FAILED);
            refundRepository.save(refund);

            PaymentLog logEntry = PaymentLog.builder()
                    .orderNo(order.getOrderNo())
                    .tradeNo(null)
                    .channel(order.getPaymentMethod())
                    .type(PaymentLogType.REFUND)
                    .payload(java.util.Map.of("error", e.getMessage(), "refundNo", refundNo))
                    .success(false)
                    .build();
            paymentLogRepository.save(logEntry);

            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "渠道退款失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(String refundNo, String reason) {
        RefundRequest refund = refundRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退款不存在"));

        if (refund.getStatus() == RefundStatus.REFUNDED) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "已退款成功，无法拒绝");
        }
        refund.setStatus(RefundStatus.REJECTED);
        refundRepository.save(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundRequest getByRefundNo(String refundNo) {
        return refundRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退款不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public com.campus.marketplace.common.dto.response.RefundResponseDTO getRefundDetail(String refundNo) {
        RefundRequest refund = refundRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退款不存在"));

        // 通过订单号查询订单及其关联的商品、买家、卖家信息
        Order order = orderRepository.findByOrderNoWithDetails(refund.getOrderNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        return convertToResponseDTO(refund, order);
    }

    /**
     * 将 RefundRequest 转换为 RefundResponseDTO（包含关联信息）
     *
     * @param refund 退款实体
     * @param order 订单实体（已加载关联的 goods、buyer、seller）
     * @return RefundResponseDTO
     */
    private com.campus.marketplace.common.dto.response.RefundResponseDTO convertToResponseDTO(
            RefundRequest refund, Order order) {
        return com.campus.marketplace.common.dto.response.RefundResponseDTO.builder()
                // 基础退款信息
                .id(refund.getId())
                .refundNo(refund.getRefundNo())
                .orderNo(refund.getOrderNo())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .evidence(refund.getEvidence())
                .status(refund.getStatus().name())
                .channel(refund.getChannel())
                .retryCount(refund.getRetryCount())
                .lastError(refund.getLastError())
                .createdAt(refund.getCreatedAt())
                .updatedAt(refund.getUpdatedAt())
                // 关联商品信息
                .goodsId(order.getGoods() != null ? order.getGoods().getId() : order.getGoodsId())
                .goodsTitle(order.getGoods() != null ? order.getGoods().getTitle() : null)
                .goodsImage(order.getGoods() != null && order.getGoods().getImages() != null 
                        && order.getGoods().getImages().length > 0 ? order.getGoods().getImages()[0] : null)
                // 关联买家信息
                .buyerId(order.getBuyer() != null ? order.getBuyer().getId() : order.getBuyerId())
                .buyerUsername(order.getBuyer() != null ? order.getBuyer().getUsername() : null)
                .buyerAvatar(order.getBuyer() != null ? order.getBuyer().getAvatar() : null)
                // 关联卖家信息
                .sellerId(order.getSeller() != null ? order.getSeller().getId() : order.getSellerId())
                .sellerUsername(order.getSeller() != null ? order.getSeller().getUsername() : null)
                .sellerAvatar(order.getSeller() != null ? order.getSeller().getAvatar() : null)
                .build();
    }

    /**
     * 用户查询自己的退款列表（使用统一筛选参数）
     *
     * @param filterRequest 筛选请求参数
     * @return 分页退款列表
     */
    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<RefundRequest> listMyRefunds(com.campus.marketplace.common.dto.request.RefundFilterRequest filterRequest) {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 构建分页排序对象
        org.springframework.data.domain.Sort.Direction direction = "ASC".equalsIgnoreCase(filterRequest.getSortDirectionOrDefault())
                ? org.springframework.data.domain.Sort.Direction.ASC
                : org.springframework.data.domain.Sort.Direction.DESC;

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                filterRequest.getPageOrDefault(),
                filterRequest.getSizeOrDefault(),
                org.springframework.data.domain.Sort.by(direction, filterRequest.getSortByOrDefault())
        );

        // 根据状态筛选
        if (filterRequest.getStatus() != null) {
            return refundRepository.findByApplicantIdAndStatus(user.getId(), filterRequest.getStatus(), pageable);
        } else {
            return refundRepository.findByApplicantId(user.getId(), pageable);
        }
    }

    /**
     * 用户查询自己的退款详情（权限校验）
     *
     * @param refundNo 退款单号
     * @return 退款详情
     * @throws BusinessException 如果不是自己的退款
     */
    @Override
    @Transactional(readOnly = true)
    public RefundRequest getMyRefund(String refundNo) {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        RefundRequest refund = refundRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "退款不存在"));

        // 权限校验：只能查看自己的退款
        if (!refund.getApplicantId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "只能查看自己的退款");
        }

        return refund;
    }

    /**
     * 管理员查询所有退款列表（使用统一筛选参数）
     *
     * @param filterRequest 筛选请求参数
     * @return 分页退款列表
     */
    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<RefundRequest> listAllRefunds(com.campus.marketplace.common.dto.request.RefundFilterRequest filterRequest) {
        // 构建分页排序对象
        org.springframework.data.domain.Sort.Direction direction = "ASC".equalsIgnoreCase(filterRequest.getSortDirectionOrDefault())
                ? org.springframework.data.domain.Sort.Direction.ASC
                : org.springframework.data.domain.Sort.Direction.DESC;

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                filterRequest.getPageOrDefault(),
                filterRequest.getSizeOrDefault(),
                org.springframework.data.domain.Sort.by(direction, filterRequest.getSortByOrDefault())
        );

        String keyword = filterRequest.getKeyword();
        RefundStatus status = filterRequest.getStatus();

        // 优先级：关键词搜索 > 状态筛选 > 全部
        if (keyword != null && !keyword.isBlank()) {
            return refundRepository.findByRefundNoContainingOrOrderNoContaining(keyword, keyword, pageable);
        } else if (status != null) {
            return refundRepository.findByStatus(status, pageable);
        } else {
            return refundRepository.findAll(pageable);
        }
    }

    private String generateRefundNo() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "RFD" + timestamp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleRefundCallback(String orderNo, String channel, boolean success, Map<String, String> payload) {
        RefundRequest refund = refundRepository.findByOrderNo(orderNo).orElse(null);
        if (refund == null) {
            log.warn("退款回调未找到退款单: orderNo={}", orderNo);
            return false;
        }

        if (success && refund.getStatus() == RefundStatus.REFUNDED) {
            return true; // 幂等
        }

        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (success) {
            refund.setStatus(RefundStatus.REFUNDED);
            refund.setLastError(null);
        } else {
            refund.setStatus(RefundStatus.FAILED);
            refund.setRetryCount(refund.getRetryCount() == null ? 1 : refund.getRetryCount() + 1);
            refund.setLastError(payload != null ? payload.getOrDefault("error", "channel_failed") : "channel_failed");
        }
        refundRepository.save(refund);

        PaymentLog logEntry = PaymentLog.builder()
                .orderNo(order.getOrderNo())
                .tradeNo(payload != null ? payload.get("tradeNo") : null)
                .channel(channel)
                .type(PaymentLogType.REFUND)
                .payload(new java.util.HashMap<>(payload == null ? java.util.Map.of() : payload))
                .success(success)
                .build();
        paymentLogRepository.save(logEntry);

        try {
            if (success) {
                notificationService.sendNotification(order.getBuyerId(), com.campus.marketplace.common.enums.NotificationType.SYSTEM_ANNOUNCEMENT,
                        "退款成功", "订单 " + order.getOrderNo() + " 退款已完成", order.getId(), "ORDER", "/orders/" + order.getOrderNo());
            }
        } catch (Exception ignore) {}

        return success;
    }
}
