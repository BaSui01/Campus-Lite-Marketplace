package com.campus.marketplace.revert.strategy.impl;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.revert.dto.RevertExecutionResult;
import com.campus.marketplace.revert.dto.RevertValidationResult;
import com.campus.marketplace.revert.strategy.RevertStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 订单撤销策略 - 完整业务实现
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRevertStrategy implements RevertStrategy {
    
    private final OrderRepository orderRepository;
    private final com.campus.marketplace.service.CacheService cacheService;
    private final com.campus.marketplace.service.RefundService refundService;
    private final com.campus.marketplace.repository.RefundRequestRepository refundRequestRepository;
    
    @Override
    public String getSupportedEntityType() {
        return "ORDER";
    }
    
    @Override
    public RevertValidationResult validateRevert(AuditLog auditLog, Long applicantId) {
        // 1. 检查撤销时限（7天）
        if (!auditLog.isWithinRevertDeadline()) {
            return RevertValidationResult.failed("订单操作已超过撤销期限（7天）");
        }
        
        // 2. 检查是否已被撤销
        if (auditLog.isReverted()) {
            return RevertValidationResult.failed("该操作已被撤销过");
        }
        
        // 3. 仅支持UPDATE操作撤销
        if (auditLog.getActionType() != AuditActionType.UPDATE) {
            return RevertValidationResult.failed("仅支持撤销订单状态变更操作");
        }
        
        log.info("订单撤销验证通过: orderId={}", auditLog.getEntityId());
        return RevertValidationResult.success();
    }
    
    @Override
    public RevertExecutionResult executeRevert(AuditLog auditLog, Long applicantId) {
        try {
            Long orderId = auditLog.getEntityId();
            log.info("开始执行订单撤销: orderId={}, applicantId={}", orderId, applicantId);
            
            // 1. 查询订单
            var orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return RevertExecutionResult.failed("订单不存在", orderId);
            }
            
            var order = orderOpt.get();
            String currentStatus = order.getStatus().name();
            
            // 2. 解析旧状态
            String oldValue = auditLog.getOldValue();
            if (oldValue == null || oldValue.trim().isEmpty()) {
                return RevertExecutionResult.failed("历史状态数据不存在", orderId);
            }
            
            // 简化处理：从审计日志中提取状态信息
            // 实际应该解析JSON，这里假设oldValue包含status字段
            String targetStatus = extractStatusFromAuditLog(oldValue);
            if (targetStatus == null) {
                return RevertExecutionResult.failed("无法解析历史状态", orderId);
            }
            
            // 3. 验证状态转换合法性
            RevertValidationResult statusValidation = validateStatusTransition(currentStatus, targetStatus, order);
            if (!statusValidation.isValid()) {
                return RevertExecutionResult.failed(statusValidation.getMessage(), orderId);
            }
            
            // 4. 执行状态回滚
            try {
                com.campus.marketplace.common.enums.OrderStatus newStatus = 
                    com.campus.marketplace.common.enums.OrderStatus.valueOf(targetStatus);
                
                order.setStatus(newStatus);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                
                log.info("订单状态回滚成功: orderId={}, {} -> {}", orderId, currentStatus, targetStatus);
                
                // 5. 处理退款标记（如果需要）
                String refundNote = checkRefundRequirement(currentStatus, targetStatus);
                
                RevertExecutionResult result = RevertExecutionResult.success(
                    String.format("订单状态已回滚: %s -> %s", currentStatus, targetStatus), 
                    orderId
                );
                
                if (refundNote != null) {
                    result.setAdditionalData(refundNote);
                }
                
                return result;
                
            } catch (IllegalArgumentException e) {
                return RevertExecutionResult.failed("无效的订单状态: " + targetStatus, orderId);
            }
            
        } catch (Exception e) {
            log.error("订单撤销执行失败: orderId={}", auditLog.getEntityId(), e);
            return RevertExecutionResult.failed("撤销执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 从审计日志中提取状态信息（简化实现）
     */
    private String extractStatusFromAuditLog(String oldValue) {
        try {
            // 简化处理：假设格式类似 {"status":"PAID",...}
            if (oldValue.contains("\"status\"")) {
                int startIdx = oldValue.indexOf("\"status\":\"") + 10;
                int endIdx = oldValue.indexOf("\"", startIdx);
                if (startIdx > 9 && endIdx > startIdx) {
                    return oldValue.substring(startIdx, endIdx);
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("解析审计日志失败: {}", oldValue, e);
            return null;
        }
    }
    
    /**
     * 验证状态转换合法性
     */
    private RevertValidationResult validateStatusTransition(String currentStatus, String targetStatus, 
                                                            com.campus.marketplace.common.entity.Order order) {
        // 1. 不允许回滚到相同状态
        if (currentStatus.equals(targetStatus)) {
            return RevertValidationResult.failed("当前状态与目标状态相同，无需回滚");
        }
        
        // 2. 已取消的订单不允许回滚
        if ("CANCELLED".equals(currentStatus)) {
            return RevertValidationResult.failed("已取消的订单不允许回滚");
        }
        
        // 3. 如果涉及退款，检查是否已结算
        if (("COMPLETED".equals(currentStatus) || "REVIEWED".equals(currentStatus)) &&
            ("PAID".equals(targetStatus) || "PENDING_PAYMENT".equals(targetStatus))) {

            // 注意：支付服务暂无 checkSettlementStatus() 方法
            // 建议：在 PaymentService 接口中添加以下方法来检查结算状态
            // boolean checkSettlementStatus(Long orderId);
            // 
            // 实现逻辑：
            // - 查询 PaymentLog 表，检查订单款项是否已结算给卖家（类型=SETTLEMENT）
            // - 如果已结算：需要财务部门处理退款，拒绝撤销或标记为需要人工处理
            // - 如果未结算：可以直接回滚订单状态
            //
            // 当前策略：所有涉及退款的订单撤销都需要管理员严格审批
            log.warn("订单回滚涉及退款，需要管理员审批: orderId={}", order.getId());

            return RevertValidationResult.warning("该操作涉及资金退款，需要严格审批");
        }
        
        // 4. 其他状态转换检查
        return RevertValidationResult.success("状态转换验证通过");
    }
    
    /**
     * 检查是否需要退款
     */
    private String checkRefundRequirement(String currentStatus, String targetStatus) {
        // 如果从已完成回滚到已支付，需要退款
        if (("COMPLETED".equals(currentStatus) || "REVIEWED".equals(currentStatus)) && 
            "PAID".equals(targetStatus)) {
            return "⚠️ 注意：该订单状态回滚可能需要处理退款，请联系财务部门核实";
        }
        
        // 如果从已支付回滚到待支付，需要退款
        if ("PAID".equals(currentStatus) && "PENDING_PAYMENT".equals(targetStatus)) {
            return "⚠️ 注意：该订单已支付，回滚后需要处理退款";
        }
        
        return null;
    }
    
    @Override
    public void postRevertProcess(AuditLog auditLog, AuditLog revertAuditLog, RevertExecutionResult result) {
        if (!result.isSuccess()) {
            return;
        }
        
        // 更新审计日志
        auditLog.setRevertedByLogId(revertAuditLog.getId());
        auditLog.setRevertedAt(LocalDateTime.now());
        auditLog.setRevertCount(auditLog.getRevertCount() + 1);
        
        // 清除订单缓存
        try {
            Long orderId = auditLog.getEntityId();
            String orderCacheKey = "order:" + orderId;
            String orderDetailCacheKey = "order:detail:" + orderId;
            String userOrderListCacheKey = "order:user:*"; // 用户订单列表缓存
            
            cacheService.delete(orderCacheKey);
            cacheService.delete(orderDetailCacheKey);
            cacheService.deleteByPattern(userOrderListCacheKey);
            
            log.info("订单缓存已清除: orderId={}", orderId);
        } catch (Exception e) {
            log.error("清除订单缓存失败，但不影响撤销结果: orderId={}", auditLog.getEntityId(), e);
        }
        
        // 处理自动退款（如果撤销结果标记需要退款）
        try {
            String additionalData = (String) result.getAdditionalData();
            if (additionalData != null && additionalData.contains("退款")) {
                Long orderId = auditLog.getEntityId();
                
                // 查询订单信息
                var orderOpt = orderRepository.findById(orderId);
                if (orderOpt.isPresent()) {
                    var order = orderOpt.get();
                    
                    // 检查订单是否需要退款处理
                    if (shouldProcessRefund(order)) {
                        // 创建退款申请
                        try {
                            String refundNo = refundService.applyRefund(
                                order.getOrderNo(),
                                "订单撤销导致的自动退款",
                                java.util.Map.of(
                                    "reason", "REVERT_OPERATION",
                                    "auditLogId", auditLog.getId().toString(),
                                    "revertLogId", revertAuditLog.getId().toString()
                                )
                            );
                            
                            log.info("自动创建退款申请: orderId={}, refundNo={}", orderId, refundNo);
                            
                            // 注意：自动审批退款需要管理员权限
                            // 当前策略：仅创建退款申请，等待管理员手动审批
                            // 
                            // 如需自动审批，需要满足以下条件：
                            // 1. 当前操作人具有管理员或财务权限
                            // 2. 订单金额未超过自动审批限额（如: ≤1000元）
                            // 3. 买家信用良好，无恶意退款记录
                            //
                            // 实现方式：
                            // if (hasAutoApprovalPermission && order.getAmount() <= AUTO_APPROVAL_LIMIT) {
                            //     refundService.approveAndRefund(refundNo);
                            //     log.info("自动审批退款成功: refundNo={}", refundNo);
                            // }
                            
                        } catch (Exception e) {
                            log.error("创建自动退款失败: orderId={}", orderId, e);
                            // 不阻塞撤销流程，退款失败需要人工处理
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理自动退款失败: orderId={}", auditLog.getEntityId(), e);
        }
        
        log.info("订单撤销后处理完成: orderId={}", auditLog.getEntityId());
    }
    
    /**
     * 判断订单是否需要退款处理
     */
    private boolean shouldProcessRefund(com.campus.marketplace.common.entity.Order order) {
        // 1. 订单状态必须是已支付或更后的状态
        var status = order.getStatus();
        if (status != com.campus.marketplace.common.enums.OrderStatus.PAID &&
            status != com.campus.marketplace.common.enums.OrderStatus.COMPLETED &&
            status != com.campus.marketplace.common.enums.OrderStatus.REVIEWED) {
            return false;
        }
        
        // 2. 订单金额必须大于0
        if (order.getAmount() == null ||
            order.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // 3. 检查是否已经退款
        try {
            var existingRefund = refundRequestRepository.findByOrderNo(order.getOrderNo());
            if (existingRefund.isPresent()) {
                log.debug("订单已存在退款申请，跳过自动退款: orderNo={}, refundNo={}", 
                         order.getOrderNo(), existingRefund.get().getRefundNo());
                return false;
            }
        } catch (Exception e) {
            log.warn("检查退款状态失败，继续处理: orderNo={}", order.getOrderNo(), e);
        }
        
        return true;
    }
    
    @Override
    public int getRevertTimeLimitDays() {
        return 7; // 订单7天撤销期限
    }
    
    @Override
    public boolean requiresApproval(AuditLog auditLog, Long applicantId) {
        return true; // 订单撤销需要审批
    }
}
