package com.campus.marketplace.service.batch;

import com.campus.marketplace.common.entity.BatchTaskItem;
import com.campus.marketplace.common.enums.BatchType;

/**
 * 批量处理器接口
 * 
 * @author BaSui
 * @date 2025-11-02
 */
public interface BatchProcessor {

    /**
     * 获取支持的批量类型
     */
    BatchType getSupportedType();

    /**
     * 处理单个任务项
     * 
     * @param item 任务项
     * @return 处理结果
     */
    BatchItemResult processItem(BatchTaskItem item);

    /**
     * 批量处理结果
     */
    record BatchItemResult(
        boolean success,
        String message,
        Object data
    ) {}
}
