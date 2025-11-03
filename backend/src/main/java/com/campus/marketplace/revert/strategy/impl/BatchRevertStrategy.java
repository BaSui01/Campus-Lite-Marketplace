package com.campus.marketplace.revert.strategy.impl;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.entity.BatchTask;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.common.enums.BatchTaskStatus;
import com.campus.marketplace.revert.dto.RevertExecutionResult;
import com.campus.marketplace.revert.dto.RevertValidationResult;
import com.campus.marketplace.revert.strategy.RevertStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 批量操作撤销策略 - 完整业务实现 🎯
 *
 * 功能说明：
 * 1. 支持批量任务撤销（取消未完成任务、回滚已完成任务）
 * 2. 验证批量任务状态和撤销时限
 * 3. 检查批量任务影响范围
 * 4. 需要严格审批（批量操作风险高）
 *
 * @author BaSui 😎 - 完善了批量撤销逻辑！
 * @date 2025-11-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchRevertStrategy implements RevertStrategy {

    private final com.campus.marketplace.repository.BatchTaskRepository batchTaskRepository;

    @Override
    public String getSupportedEntityType() {
        return "BATCH_OPERATION";
    }

    @Override
    public RevertValidationResult validateRevert(AuditLog auditLog, Long applicantId) {
        try {
            // 1. 检查撤销时限（7天）
            if (!auditLog.isWithinRevertDeadline()) {
                return RevertValidationResult.failed("批量操作已超过撤销期限（7天）");
            }

            // 2. 检查是否已被撤销
            if (auditLog.isReverted()) {
                return RevertValidationResult.failed("该批量操作已被撤销过");
            }

            // 3. 检查操作类型（批量操作通常是 UPDATE 或其他批量操作）
            AuditActionType actionType = auditLog.getActionType();
            if (actionType != AuditActionType.UPDATE && 
                actionType != AuditActionType.GOODS_CREATE &&
                actionType != AuditActionType.POST_CREATE) {
                return RevertValidationResult.failed("不支持撤销该类型的批量操作");
            }

            // 4. 查询批量任务状态
            Long batchTaskId = auditLog.getEntityId();
            Optional<BatchTask> taskOpt = batchTaskRepository.findById(batchTaskId);

            if (taskOpt.isEmpty()) {
                return RevertValidationResult.failed("批量任务不存在");
            }

            BatchTask task = taskOpt.get();

            // 5. 检查批量任务状态（只能撤销进行中或已完成的任务）
            if (task.getStatus() == BatchTaskStatus.CANCELLED) {
                return RevertValidationResult.failed("批量任务已被取消，无需再次撤销");
            }

            // 6. 警告：已完成的批量任务撤销风险高
            if (task.getStatus() == BatchTaskStatus.SUCCESS ||
                task.getStatus() == BatchTaskStatus.PARTIAL_SUCCESS) {
                return RevertValidationResult.warning(
                    String.format("该批量任务已完成（成功%d条，失败%d条，共%d条），撤销操作影响范围大，需要严格审批！",
                        task.getSuccessCount(), task.getErrorCount(), task.getTotalCount())
                );
            }

            log.info("批量操作撤销验证通过: batchTaskId={}, status={}", batchTaskId, task.getStatus());
            return RevertValidationResult.success("验证通过，批量操作撤销需要严格审批");

        } catch (Exception e) {
            log.error("批量操作撤销验证失败: batchTaskId={}", auditLog.getEntityId(), e);
            return RevertValidationResult.failed("验证失败: " + e.getMessage());
        }
    }

    @Override
    public RevertExecutionResult executeRevert(AuditLog auditLog, Long applicantId) {
        try {
            Long batchTaskId = auditLog.getEntityId();
            log.info("开始执行批量操作撤销: batchTaskId={}, applicantId={}", batchTaskId, applicantId);

            // 1. 查询批量任务
            Optional<BatchTask> taskOpt = batchTaskRepository.findById(batchTaskId);
            if (taskOpt.isEmpty()) {
                return RevertExecutionResult.failed("批量任务不存在", batchTaskId);
            }

            BatchTask task = taskOpt.get();
            BatchTaskStatus originalStatus = task.getStatus();

            // 2. 根据任务状态执行不同的撤销策略
            if (task.getStatus() == BatchTaskStatus.PENDING ||
                task.getStatus() == BatchTaskStatus.PROCESSING) {
                // 进行中的任务：直接取消
                task.setStatus(BatchTaskStatus.CANCELLED);
                task.setEndTime(LocalDateTime.now());
                task.setErrorSummary("批量任务已被管理员撤销（申请人ID: " + applicantId + "）");

                batchTaskRepository.save(task);

                log.info("批量任务已取消: batchTaskId={}", batchTaskId);

                return RevertExecutionResult.success(
                    String.format("批量任务已取消（原状态：%s，已处理%d/%d条）",
                        originalStatus, task.getSuccessCount() + task.getErrorCount(), task.getTotalCount()),
                    batchTaskId
                );

            } else if (task.getStatus() == BatchTaskStatus.SUCCESS ||
                       task.getStatus() == BatchTaskStatus.PARTIAL_SUCCESS) {
                // 已完成的任务：标记为已撤销（实际数据回滚需要由具体业务处理）
                task.setStatus(BatchTaskStatus.CANCELLED);
                task.setErrorSummary("批量任务已被撤销（申请人ID: " + applicantId + "）- 注意：需要手动回滚受影响的数据！");

                batchTaskRepository.save(task);

                log.warn("批量任务已撤销（需人工回滚数据）: batchTaskId={}, 影响记录数={}",
                    batchTaskId, task.getSuccessCount());

                return RevertExecutionResult.success(
                    String.format("批量任务已撤销（原状态：%s，成功%d条，失败%d条）\n⚠️ 警告：请手动回滚受影响的%d条数据！",
                        originalStatus, task.getSuccessCount(), task.getErrorCount(), task.getSuccessCount()),
                    batchTaskId
                );

            } else {
                return RevertExecutionResult.failed("批量任务状态不允许撤销: " + task.getStatus(), batchTaskId);
            }

        } catch (Exception e) {
            log.error("批量操作撤销执行失败: batchTaskId={}", auditLog.getEntityId(), e);
            return RevertExecutionResult.failed("撤销执行失败: " + e.getMessage());
        }
    }

    @Override
    public void postRevertProcess(AuditLog auditLog, AuditLog revertAuditLog, RevertExecutionResult result) {
        if (!result.isSuccess()) {
            return;
        }

        try {
            // 1. 更新审计日志
            auditLog.setRevertedByLogId(revertAuditLog.getId());
            auditLog.setRevertedAt(LocalDateTime.now());
            auditLog.setRevertCount(auditLog.getRevertCount() + 1);

            // 2. 记录撤销通知（批量操作撤销应该通知相关管理员）
            log.info("批量操作撤销后处理完成: batchTaskId={}", auditLog.getEntityId());
            log.warn("⚠️ 注意：批量操作已撤销，如果任务已部分执行，请检查并手动回滚受影响的数据！");

            // 3. TODO: 发送邮件/站内信通知管理员
            // notificationService.sendBatchRevertNotification(auditLog.getEntityId(), result);

        } catch (Exception e) {
            log.error("批量操作撤销后处理失败: batchTaskId={}", auditLog.getEntityId(), e);
        }
    }

    @Override
    public int getRevertTimeLimitDays() {
        return 7; // 批量操作7天撤销期限
    }

    @Override
    public boolean requiresApproval(AuditLog auditLog, Long applicantId) {
        return true; // 批量操作撤销必须审批（风险高）
    }
}
