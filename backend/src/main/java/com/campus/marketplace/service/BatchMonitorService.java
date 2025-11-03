package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.BatchTaskProgressResponse;
import com.campus.marketplace.common.entity.BatchTask;
import com.campus.marketplace.common.enums.BatchTaskStatus;
import com.campus.marketplace.repository.BatchTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 批量操作监控服务
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchMonitorService {

    private final BatchTaskRepository batchTaskRepository;

    /**
     * 获取批量操作统计信息
     */
    public Map<String, Object> getBatchStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();

        // 统计各状态任务数量
        long totalTasks = batchTaskRepository.count();
        long pendingTasks = batchTaskRepository.countByStatus(BatchTaskStatus.PENDING);
        long processingTasks = batchTaskRepository.countByStatus(BatchTaskStatus.PROCESSING);
        long successTasks = batchTaskRepository.countByStatus(BatchTaskStatus.SUCCESS);
        long failedTasks = batchTaskRepository.countByStatus(BatchTaskStatus.FAILED);

        stats.put("totalTasks", totalTasks);
        stats.put("pendingTasks", pendingTasks);
        stats.put("processingTasks", processingTasks);
        stats.put("successTasks", successTasks);
        stats.put("failedTasks", failedTasks);
        stats.put("successRate", totalTasks > 0 ? (double) successTasks / totalTasks * 100 : 0);

        return stats;
    }

    /**
     * 获取任务实时进度
     */
    public BatchTaskProgressResponse getRealtimeProgress(Long taskId) {
        BatchTask task = batchTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        int processedCount = task.getSuccessCount() + task.getErrorCount();
        Long estimatedRemaining = calculateEstimatedRemaining(task);

        return BatchTaskProgressResponse.builder()
                .taskId(task.getId())
                .taskCode(task.getTaskCode())
                .status(task.getStatus())
                .totalCount(task.getTotalCount())
                .processedCount(processedCount)
                .successCount(task.getSuccessCount())
                .failedCount(task.getErrorCount())
                .progressPercentage(task.getProgressPercentage())
                .estimatedRemaining(estimatedRemaining)
                .completed(task.isCompleted())
                .build();
    }

    /**
     * 计算预计剩余时间（秒）
     */
    private Long calculateEstimatedRemaining(BatchTask task) {
        if (task.isCompleted() || task.getStartTime() == null) {
            return 0L;
        }

        int processedCount = task.getSuccessCount() + task.getErrorCount();
        if (processedCount == 0) {
            return task.getEstimatedDuration() != null ? task.getEstimatedDuration().longValue() : null;
        }

        long elapsedSeconds = java.time.Duration.between(task.getStartTime(), LocalDateTime.now()).getSeconds();
        double avgTimePerItem = (double) elapsedSeconds / processedCount;
        int remainingItems = task.getTotalCount() - processedCount;

        return (long) (avgTimePerItem * remainingItems);
    }

    /**
     * 检查超时任务
     */
    public java.util.List<BatchTask> checkTimeoutTasks() {
        return batchTaskRepository.findTimeoutTasks();
    }
}
