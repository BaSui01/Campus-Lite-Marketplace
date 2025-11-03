package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreateRevertRequestDto;
import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.entity.RevertRequest;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.AuditLogRepository;
import com.campus.marketplace.repository.RevertRequestRepository;
import com.campus.marketplace.revert.dto.RevertExecutionResult;
import com.campus.marketplace.revert.dto.RevertValidationResult;
import com.campus.marketplace.revert.factory.RevertStrategyFactory;
import com.campus.marketplace.revert.strategy.RevertStrategy;
import com.campus.marketplace.service.RevertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 撤销服务实现
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RevertServiceImpl implements RevertService {

    private final AuditLogRepository auditLogRepository;
    private final RevertRequestRepository revertRequestRepository;
    private final RevertStrategyFactory strategyFactory;

    @Override
    @Transactional
    public RevertExecutionResult requestRevert(Long auditLogId, CreateRevertRequestDto request, Long applicantId) {
        // 1. 查询审计日志
        AuditLog auditLog = auditLogRepository.findById(auditLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "审计日志不存在"));

        // 2. 检查是否已有撤销请求
        if (revertRequestRepository.existsByAuditLogId(auditLogId)) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "该操作已存在撤销请求");
        }

        // 3. 获取撤销策略
        RevertStrategy strategy = strategyFactory.getStrategy(auditLog);

        // 4. 验证撤销条件
        RevertValidationResult validationResult = strategy.validateRevert(auditLog, applicantId);
        if (!validationResult.isValid()) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, validationResult.getMessage());
        }

        // 5. 创建撤销请求
        RevertRequest revertRequest = RevertRequest.builder()
                .auditLogId(auditLogId)
                .requesterId(applicantId)
                .reason(request.getReason())
                .build();

        revertRequestRepository.save(revertRequest);

        log.info("撤销请求创建成功: auditLogId={}, applicantId={}", auditLogId, applicantId);

        return RevertExecutionResult.success("撤销请求已提交", auditLog.getEntityId());
    }

    @Override
    @Transactional
    public RevertExecutionResult executeRevert(Long revertRequestId, Long approverId) {
        // 1. 查询撤销请求
        RevertRequest revertRequest = revertRequestRepository.findById(revertRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "撤销请求不存在"));

        // 2. 检查是否可以执行
        if (!revertRequest.canExecute()) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "撤销请求状态不允许执行");
        }

        // 3. 查询审计日志
        AuditLog auditLog = auditLogRepository.findById(revertRequest.getAuditLogId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "审计日志不存在"));

        // 4. 获取策略并执行撤销
        try {
            RevertStrategy strategy = strategyFactory.getStrategy(auditLog);
            RevertExecutionResult result = strategy.executeRevert(auditLog, approverId);

            if (result.isSuccess()) {
                revertRequest.markExecuted(null); // TODO: 关联撤销审计日志
                revertRequestRepository.save(revertRequest);
            } else {
                revertRequest.markFailed(result.getMessage());
                revertRequestRepository.save(revertRequest);
            }

            return result;

        } catch (Exception e) {
            log.error("撤销执行失败: revertRequestId={}", revertRequestId, e);
            revertRequest.markFailed(e.getMessage());
            revertRequestRepository.save(revertRequest);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "撤销执行失败: " + e.getMessage());
        }
    }

    @Override
    public Page<?> getUserRevertRequests(Long userId, Pageable pageable) {
        return revertRequestRepository.findByRequesterIdOrderByCreatedAtDesc(userId, pageable);
    }
}
