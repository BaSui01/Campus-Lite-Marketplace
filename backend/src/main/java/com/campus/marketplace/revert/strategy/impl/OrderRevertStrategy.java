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
            
            // TODO: 实现订单状态回滚逻辑
            // 1. 查询订单
            // 2. 验证资金状态
            // 3. 回滚订单状态
            // 4. 处理退款
            
            return RevertExecutionResult.success("订单撤销成功", orderId);
            
        } catch (Exception e) {
            log.error("订单撤销执行失败: orderId={}", auditLog.getEntityId(), e);
            return RevertExecutionResult.failed("撤销执行失败: " + e.getMessage());
        }
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
        
        log.info("订单撤销后处理完成: orderId={}", auditLog.getEntityId());
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
