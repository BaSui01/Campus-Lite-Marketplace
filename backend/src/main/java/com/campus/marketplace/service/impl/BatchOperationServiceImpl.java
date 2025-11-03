package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreateBatchTaskRequest;
import com.campus.marketplace.common.dto.response.BatchTaskProgressResponse;
import com.campus.marketplace.common.dto.response.BatchTaskResponse;
import com.campus.marketplace.common.entity.BatchTask;
import com.campus.marketplace.common.enums.BatchTaskStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.BatchTaskRepository;
import com.campus.marketplace.service.BatchOperationService;
import com.campus.marketplace.service.batch.BatchTaskOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 批量操作服务实现
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchOperationServiceImpl implements BatchOperationService {

    private final BatchTaskRepository batchTaskRepository;
    private final BatchTaskOrchestrator taskOrchestrator;

    @Override
    @Transactional
    public Long createBatchTask(CreateBatchTaskRequest request, Long userId) {
        log.info("创建批量任务: userId={}, batchType={}", userId, request.getBatchType());

        // 生成唯一任务编码
        String taskCode = "BATCH-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);

        // 创建批量任务
        BatchTask task = BatchTask.builder()
                .taskCode(taskCode)
                .batchType(request.getBatchType())
                .userId(userId)
                .requestData(request.getRequestData())
                .estimatedDuration(request.getEstimatedDuration())
                .status(BatchTaskStatus.PENDING)
                .build();

        task = batchTaskRepository.save(task);
        log.info("批量任务创建成功: taskId={}, taskCode={}", task.getId(), taskCode);

        return task.getId();
    }

    @Override
    public void executeBatchTask(Long taskId) {
        log.info("触发批量任务执行: taskId={}", taskId);
        
        BatchTask task = batchTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "任务不存在"));

        if (task.getStatus() != BatchTaskStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "任务状态不正确，无法执行");
        }

        // 异步执行任务
        taskOrchestrator.orchestrateTask(taskId);
    }

    @Override
    @Transactional
    public boolean cancelBatchTask(Long taskId, Long userId) {
        log.info("取消批量任务: taskId={}, userId={}", taskId, userId);

        BatchTask task = batchTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "任务不存在"));

        // 验证任务所属用户
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此任务");
        }

        // 只有待执行和执行中的任务可以取消
        if (task.getStatus() != BatchTaskStatus.PENDING && task.getStatus() != BatchTaskStatus.PROCESSING) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "任务状态不支持取消");
        }

        task.setStatus(BatchTaskStatus.CANCELLED);
        batchTaskRepository.save(task);

        log.info("批量任务已取消: taskId={}", taskId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BatchTaskResponse> getUserBatchTasks(Long userId, BatchTaskStatus status, Pageable pageable) {
        log.debug("查询用户批量任务列表: userId={}, status={}", userId, status);

        Page<BatchTask> tasks;
        if (status != null) {
            tasks = batchTaskRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            tasks = batchTaskRepository.findByUserId(userId, pageable);
        }

        return tasks.map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BatchTaskResponse getBatchTaskDetail(Long taskId, Long userId) {
        log.debug("查询批量任务详情: taskId={}, userId={}", taskId, userId);

        BatchTask task = batchTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "任务不存在"));

        // 验证任务所属用户
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看此任务");
        }

        return convertToResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public BatchTaskProgressResponse getTaskProgress(Long taskId) {
        log.debug("查询批量任务进度: taskId={}", taskId);

        BatchTask task = batchTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "任务不存在"));

        int processedCount = task.getSuccessCount() + task.getErrorCount();

        return BatchTaskProgressResponse.builder()
                .taskId(task.getId())
                .taskCode(task.getTaskCode())
                .status(task.getStatus())
                .totalCount(task.getTotalCount())
                .processedCount(processedCount)
                .successCount(task.getSuccessCount())
                .failedCount(task.getErrorCount())
                .progressPercentage(task.getProgressPercentage())
                .completed(task.isCompleted())
                .build();
    }

    /**
     * 转换为响应对象
     */
    private BatchTaskResponse convertToResponse(BatchTask task) {
        return BatchTaskResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .batchType(task.getBatchType())
                .batchTypeDesc(task.getBatchType().getDescription())
                .totalCount(task.getTotalCount())
                .successCount(task.getSuccessCount())
                .errorCount(task.getErrorCount())
                .status(task.getStatus())
                .statusDesc(task.getStatus().getDescription())
                .progressPercentage(task.getProgressPercentage())
                .startTime(task.getStartTime())
                .endTime(task.getEndTime())
                .duration(task.calculateDuration())
                .errorSummary(task.getErrorSummary())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
