package com.campus.marketplace.revert.strategy;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.revert.dto.RevertExecutionResult;
import com.campus.marketplace.revert.dto.RevertValidationResult;

/**
 * 撤销策略接口 - 定义统一的撤销操作规范
 * 
 * 功能说明：
 * 1. 支持插件化扩展，新的实体类型只需实现此接口
 * 2. 验证和执行分离，确保操作安全性
 * 3. 支持撤销后处理，处理缓存、通知等副作用
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface RevertStrategy {
    
    /**
     * 获取支持的实体类型
     * 
     * @return 实体类型（如 "Goods"、"Order"、"User"）
     */
    String getSupportedEntityType();
    
    /**
     * 验证撤销条件
     * 
     * @param auditLog 审计日志
     * @param applicantId 申请人ID
     * @return 验证结果
     */
    RevertValidationResult validateRevert(AuditLog auditLog, Long applicantId);
    
    /**
     * 执行撤销操作
     * 
     * @param auditLog 审计日志
     * @param applicantId 申请人ID
     * @return 撤销结果
     */
    RevertExecutionResult executeRevert(AuditLog auditLog, Long applicantId);
    
    /**
     * 撤销后处理（更新缓存、发送通知等）
     * 
     * @param auditLog 原审计日志
     * @param revertAuditLog 撤销审计日志
     * @param result 撤销结果
     */
    void postRevertProcess(AuditLog auditLog, AuditLog revertAuditLog, RevertExecutionResult result);
    
    /**
     * 获取撤销时限（天）
     * 
     * @return 撤销时限天数
     */
    int getRevertTimeLimitDays();
    
    /**
     * 是否需要审批
     * 
     * @param auditLog 审计日志
     * @param applicantId 申请人ID
     * @return 是否需要审批
     */
    boolean requiresApproval(AuditLog auditLog, Long applicantId);
}
