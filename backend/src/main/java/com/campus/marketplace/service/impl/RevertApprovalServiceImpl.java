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
            
            // TODO: 发送审批通过通知
            // notificationService.sendApprovalNotification(revertRequest, true);
        } else {
            // 拒绝请求
            revertRequest.reject(approverId, null, comment);
            log.info("撤销请求已拒绝: revertRequestId={}, approverId={}, reason={}", 
                    revertRequestId, approverId, comment);
            
            // TODO: 发送审批拒绝通知
            // notificationService.sendApprovalNotification(revertRequest, false);
        }

        // 4. 保存审批结果
        revertRequestRepository.save(revertRequest);
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
        // TODO: 实现根据审批人权限查询待审批数量
        // 这里简化处理，返回所有待审批的撤销请求数量
        return revertRequestRepository.countByRequesterIdAndStatus(
            approverId, RevertRequestStatus.PENDING
        );
    }
}
