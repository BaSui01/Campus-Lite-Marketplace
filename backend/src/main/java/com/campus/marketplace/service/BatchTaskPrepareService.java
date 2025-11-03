package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.BatchTask;
import com.campus.marketplace.common.entity.BatchTaskItem;
import com.campus.marketplace.common.enums.BatchType;
import com.campus.marketplace.repository.BatchTaskItemRepository;
import com.campus.marketplace.repository.BatchTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量任务准备服务
 * 负责创建批量任务项
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchTaskPrepareService {

    private final BatchTaskRepository batchTaskRepository;
    private final BatchTaskItemRepository batchTaskItemRepository;

    /**
     * 为批量任务准备任务项
     * 
     * @param taskId 任务ID
     * @param targetIds 目标ID列表
     * @param requestData 请求数据（JSON）
     */
    @Transactional
    public void prepareTaskItems(Long taskId, List<Long> targetIds, String requestData) {
        log.info("准备批量任务项: taskId={}, targetCount={}", taskId, targetIds.size());

        BatchTask task = batchTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        // 更新总数量
        task.setTotalCount(targetIds.size());
        batchTaskRepository.save(task);

        // 创建任务项
        List<BatchTaskItem> items = new ArrayList<>();
        int shardSize = calculateShardSize(targetIds.size());
        
        for (int i = 0; i < targetIds.size(); i++) {
            Long targetId = targetIds.get(i);
            String shardKey = "shard-" + (i / shardSize);

            BatchTaskItem item = BatchTaskItem.builder()
                    .batchTaskId(taskId)
                    .targetId(targetId)
                    .targetType(getTargetType(task.getBatchType()))
                    .inputData(requestData)
                    .shardKey(shardKey)
                    .build();
            items.add(item);
        }

        batchTaskItemRepository.saveAll(items);
        log.info("批量任务项准备完成: taskId={}, itemCount={}", taskId, items.size());
    }

    /**
     * 计算分片大小
     */
    private int calculateShardSize(int totalCount) {
        if (totalCount <= 100) {
            return totalCount; // 小批量不分片
        } else if (totalCount <= 1000) {
            return 100; // 中批量，每片100个
        } else {
            return 500; // 大批量，每片500个
        }
    }

    /**
     * 获取目标类型
     */
    private String getTargetType(BatchType batchType) {
        return switch (batchType) {
            case GOODS_BATCH, PRICE_BATCH, INVENTORY_BATCH -> "GOODS";
            case NOTIFICATION_BATCH -> "USER";
        };
    }
}
