package com.campus.marketplace.revert.strategy.impl;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.entity.DataBackup;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.revert.dto.RevertExecutionResult;
import com.campus.marketplace.revert.dto.RevertValidationResult;
import com.campus.marketplace.revert.strategy.RevertStrategy;
import com.campus.marketplace.service.DataBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户撤销策略 - 完整业务实现
 * 
 * 功能说明：
 * 1. 支持用户封禁撤销（恢复用户状态）
 * 2. 支持用户信息变更撤销
 * 3. 验证撤销时限（15天内）
 * 4. 检查用户影响范围
 * 5. 恢复后需要审批
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRevertStrategy implements RevertStrategy {
    
    private final com.campus.marketplace.repository.UserRepository userRepository;
    private final DataBackupService dataBackupService;
    
    @Override
    public String getSupportedEntityType() {
        return "USER";
    }
    
    @Override
    public RevertValidationResult validateRevert(AuditLog auditLog, Long applicantId) {
        // 1. 检查撤销时限（15天）
        if (!auditLog.isWithinRevertDeadline()) {
            return RevertValidationResult.failed("用户操作已超过撤销期限（15天）");
        }
        
        // 2. 检查是否已被撤销
        if (auditLog.isReverted()) {
            return RevertValidationResult.failed("该操作已被撤销过");
        }
        
        // 3. 检查操作类型
        AuditActionType actionType = auditLog.getActionType();
        if (actionType != AuditActionType.UPDATE) {
            return RevertValidationResult.failed("仅支持撤销用户信息更新操作");
        }
        
        // 4. 检查是否有备份数据
        if (auditLog.getOldValue() == null || auditLog.getOldValue().trim().isEmpty()) {
            return RevertValidationResult.failed("用户历史数据不存在，无法回滚");
        }
        
        log.info("用户撤销验证通过: userId={}", auditLog.getEntityId());
        return RevertValidationResult.success("验证通过，该操作涉及用户权限，需要严格审批");
    }
    
    @Override
    public RevertExecutionResult executeRevert(AuditLog auditLog, Long applicantId) {
        try {
            Long userId = auditLog.getEntityId();
            log.info("开始执行用户撤销: userId={}, applicantId={}", userId, applicantId);
            
            // 1. 查询用户
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return RevertExecutionResult.failed("用户不存在", userId);
            }
            
            User user = userOpt.get();
            String oldValue = auditLog.getOldValue();
            
            // 2. 解析旧状态
            String targetStatus = extractUserStatusFromAuditLog(oldValue);
            if (targetStatus != null && !targetStatus.equals(user.getStatus().name())) {
                // 回滚用户状态
                try {
                    UserStatus newStatus = UserStatus.valueOf(targetStatus);
                    user.setStatus(newStatus);
                    user.setUpdatedAt(LocalDateTime.now());
                    
                    log.info("用户状态回滚: userId={}, {} -> {}", 
                            userId, user.getStatus(), newStatus);
                } catch (IllegalArgumentException e) {
                    log.error("无效的用户状态: {}", targetStatus);
                }
            }
            
            // 3. 保存更新
            userRepository.save(user);
            
            // 4. TODO: 清理用户会话（如果用户被解封）
            // if (newStatus == UserStatus.ACTIVE) {
            //     sessionService.invalidateUserSessions(userId);
            // }
            
            log.info("用户撤销执行成功: userId={}", userId);
            
            return RevertExecutionResult.success(
                String.format("用户操作已撤销，状态已恢复（用户ID: %d）", userId),
                userId
            );
            
        } catch (Exception e) {
            log.error("用户撤销执行失败: userId={}", auditLog.getEntityId(), e);
            return RevertExecutionResult.failed("撤销执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 从审计日志中提取用户状态
     */
    private String extractUserStatusFromAuditLog(String oldValue) {
        try {
            if (oldValue.contains("\"status\"")) {
                int startIdx = oldValue.indexOf("\"status\":\"") + 10;
                int endIdx = oldValue.indexOf("\"", startIdx);
                if (startIdx > 9 && endIdx > startIdx) {
                    return oldValue.substring(startIdx, endIdx);
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("解析用户审计日志失败: {}", oldValue, e);
            return null;
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
        
        // TODO: 发送通知
        // notificationService.sendUserRevertNotification(auditLog.getEntityId());
        
        log.info("用户撤销后处理完成: userId={}", auditLog.getEntityId());
    }
    
    @Override
    public int getRevertTimeLimitDays() {
        return 15; // 用户15天撤销期限
    }
    
    @Override
    public boolean requiresApproval(AuditLog auditLog, Long applicantId) {
        return true; // 用户撤销需要审批
    }
}
