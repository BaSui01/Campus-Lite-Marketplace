package com.campus.marketplace.service.batch;

import com.campus.marketplace.common.entity.BatchTask;
import com.campus.marketplace.common.entity.BatchTaskItem;
import com.campus.marketplace.common.enums.BatchItemStatus;
import com.campus.marketplace.common.enums.BatchTaskStatus;
import com.campus.marketplace.repository.BatchTaskItemRepository;
import com.campus.marketplace.repository.BatchTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 批量任务编排器
 * 负责任务的异步执行、分片处理、状态管理
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchTaskOrchestrator {

    private final BatchTaskRepository batchTaskRepository;
    private final BatchTaskItemRepository batchTaskItemRepository;
    private final BatchProcessorFactory processorFactory;

    /**
     * 任务编排执行（异步）
     */
    @Async
    @Transactional
    public void orchestrateTask(Long taskId) {
        log.info("开始执行批量任务: taskId={}", taskId);
        
        BatchTask task = batchTaskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        try {
            // 1. 更新任务状态为执行中
            task.setStatus(BatchTaskStatus.PROCESSING);
            task.setStartTime(LocalDateTime.now());
            batchTaskRepository.save(task);

            // 2. 获取处理器
            BatchProcessor processor = processorFactory.getProcessor(task.getBatchType());

            // 3. 获取待处理的任务项
            List<BatchTaskItem> items = batchTaskItemRepository.findPendingItems(taskId);
            log.info("任务 {} 共有 {} 个待处理项", taskId, items.size());

            // 4. 分片处理（当前简化版，直接处理）
            processItems(task, items, processor);

            // 5. 完成任务
            completeTask(task);

        } catch (Exception e) {
            log.error("批量任务执行失败: taskId={}", taskId, e);
            handleTaskError(task, e);
        }
    }

    /**
     * 处理任务项
     */
    private void processItems(BatchTask task, List<BatchTaskItem> items, BatchProcessor processor) {
        int successCount = 0;
        int errorCount = 0;

        for (BatchTaskItem item : items) {
            try {
                long startTime = System.currentTimeMillis();
                
                // 更新任务项状态为处理中
                item.setStatus(BatchItemStatus.PROCESSING);
                batchTaskItemRepository.save(item);

                // 执行处理
                BatchProcessor.BatchItemResult result = processor.processItem(item);

                // 更新任务项结果
                if (result.success()) {
                    item.markSuccess();
                    item.setOutputData(result.data() != null ? result.data().toString() : null);
                    successCount++;
                } else {
                    item.markFailed(result.message());
                    errorCount++;
                }

                item.setProcessingTime((int) (System.currentTimeMillis() - startTime));
                batchTaskItemRepository.save(item);

            } catch (Exception e) {
                log.error("处理任务项失败: itemId={}", item.getId(), e);
                item.markFailed(e.getMessage());
                batchTaskItemRepository.save(item);
                errorCount++;
            }

            // 更新任务进度
            task.setSuccessCount(successCount);
            task.setErrorCount(errorCount);
            task.updateProgress();
            batchTaskRepository.save(task);
        }
    }

    /**
     * 完成任务
     */
    private void completeTask(BatchTask task) {
        task.setEndTime(LocalDateTime.now());
        
        // 根据成功和失败数量判断最终状态
        if (task.getErrorCount() == 0) {
            task.setStatus(BatchTaskStatus.SUCCESS);
        } else if (task.getSuccessCount() > 0) {
            task.setStatus(BatchTaskStatus.PARTIAL_SUCCESS);
            task.setErrorSummary(String.format("部分成功：成功%d个，失败%d个", 
                task.getSuccessCount(), task.getErrorCount()));
        } else {
            task.setStatus(BatchTaskStatus.FAILED);
            task.setErrorSummary("全部失败");
        }

        batchTaskRepository.save(task);
        log.info("批量任务完成: taskId={}, status={}, success={}, error={}", 
            task.getId(), task.getStatus(), task.getSuccessCount(), task.getErrorCount());
    }

    /**
     * 任务异常处理
     */
    private void handleTaskError(BatchTask task, Exception exception) {
        task.setStatus(BatchTaskStatus.FAILED);
        task.setEndTime(LocalDateTime.now());
        task.setErrorSummary("任务执行异常: " + exception.getMessage());
        batchTaskRepository.save(task);
    }

    /**
     * 分片处理（高级版本，支持大批量数据并行处理）
     */
    @Async
    public CompletableFuture<Void> processShard(Long taskId, String shardKey) {
        log.info("处理分片: taskId={}, shardKey={}", taskId, shardKey);
        
        BatchTask task = batchTaskRepository.findById(taskId).orElseThrow();
        BatchProcessor processor = processorFactory.getProcessor(task.getBatchType());
        List<BatchTaskItem> items = batchTaskItemRepository.findByBatchTaskIdAndShardKey(taskId, shardKey);

        processItems(task, items, processor);

        return CompletableFuture.completedFuture(null);
    }
}
