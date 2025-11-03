package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.entity.RevertRequest;
import com.campus.marketplace.common.enums.RevertRequestStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.AuditLogRepository;
import com.campus.marketplace.repository.RevertRequestRepository;
import com.campus.marketplace.revert.factory.RevertStrategyFactory;
import com.campus.marketplace.revert.strategy.RevertStrategy;
import com.campus.marketplace.service.RevertApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 撤销审批服务实现
 * 
 * 功能说明：
 * 1. 撤销请求审批管理
 * 2. 自动判断是否需要审批
 * 3. 审批流程跟踪
 * 4. 审批通知发送
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RevertApprovalServiceImpl implements RevertApprovalService {

    private final RevertRequestRepository revertRequestRepository;
    private final AuditLogRepository auditLogRepository;
    private final RevertStrategyFactory strategyFactory;
    private final com.campus.marketplace.service.RevertNotificationService notificationService;

    @Override
    @Transactional
    public void approveRevertRequest(Long revertRequestId, Long approverId, boolean approved, String comment) {
        // 1. 查询撤销请求
        RevertRequest revertRequest = revertRequestRepository.findById(revertRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "撤销请求不存在"));

        // 2. 检查状态
        if (revertRequest.getStatus() != RevertRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, 
                "撤销请求状态不是待审批，当前状态: " + revertRequest.getStatus());
        }

        // 3. 执行审批
        if (approved) {
            // 批准请求
            revertRequest.approve(approverId, null, comment);
            log.info("撤销请求已批准: revertRequestId={}, approverId={}", revertRequestId, approverId);
        } else {
            // 拒绝请求
            revertRequest.reject(approverId, null, comment);
            log.info("撤销请求已拒绝: revertRequestId={}, approverId={}, reason={}", 
                    revertRequestId, approverId, comment);
        }

        // 4. 保存审批结果
        revertRequestRepository.save(revertRequest);
        
        // 5. 发送审批结果通知
        try {
            notificationService.sendApprovalNotification(revertRequest, approved);
        } catch (Exception e) {
            log.error("发送审批通知失败，但不影响审批结果: revertRequestId={}", revertRequestId, e);
        }
    }

    @Override
    public boolean requiresApproval(Long revertRequestId) {
        // 1. 查询撤销请求
        RevertRequest revertRequest = revertRequestRepository.findById(revertRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "撤销请求不存在"));

        // 2. 查询审计日志
        AuditLog auditLog = auditLogRepository.findById(revertRequest.getAuditLogId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "审计日志不存在"));

        // 3. 获取撤销策略
        try {
            RevertStrategy strategy = strategyFactory.getStrategy(auditLog);
            return strategy.requiresApproval(auditLog, revertRequest.getRequesterId());
        } catch (Exception e) {
            log.error("判断是否需要审批失败: revertRequestId={}", revertRequestId, e);
            // 默认需要审批（安全策略）
            return true;
        }
    }

    @Override
    public long getPendingApprovalCount(Long approverId) {
        // 根据审批人权限查询待审批数量
        // 说明：
        // 1. 管理员可以看到所有待审批的撤销请求
        // 2. 普通审批人只能看到自己负责的审批请求
        // 3. 审批权限由 User 的 role 字段决定（ADMIN, SUPER_ADMIN 等）
        //
        // 注意：UserService 暂无 hasRole() 或 getUserRoles() 方法
        // 建议：在 UserService 或新建 RoleService 中添加角色查询方法
        // 
        // 接口定义：
        // public interface UserService {
        //     User getUserById(Long userId);
        //     boolean hasRole(Long userId, String roleName);  // 检查用户是否具有某个角色
        //     List<String> getUserRoles(Long userId);  // 获取用户的所有角色
        // }
        //
        // 实现示例：
        // User user = userService.getUserById(approverId);
        // if (user != null && userService.hasRole(approverId, "ADMIN")) {
        //     // 管理员：返回所有待审批
        //     return revertRequestRepository.countByStatus(RevertRequestStatus.PENDING);
        // } else {
        //     // 普通审批人：返回自己负责的待审批（需要RevertRequest表添加assignedTo字段）
        //     return revertRequestRepository.countByStatusAndAssignedTo(RevertRequestStatus.PENDING, approverId);
        // }

        // 当前简化实现：返回所有待审批的撤销请求数量
        long pendingCount = revertRequestRepository.countByStatus(RevertRequestStatus.PENDING);
        log.debug("待审批撤销请求数量查询（简化实现）: approverId={}, count={}", approverId, pendingCount);

        return pendingCount;
    }
}
