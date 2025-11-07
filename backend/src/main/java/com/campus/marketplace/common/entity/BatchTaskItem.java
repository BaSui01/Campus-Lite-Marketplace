package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.BatchItemStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 批量任务项实体
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Entity
@Table(name = "t_batch_task_item", indexes = {
    @Index(name = "idx_batch_task_item_task_id", columnList = "batch_task_id"),
    @Index(name = "idx_batch_task_item_status", columnList = "status"),
    @Index(name = "idx_batch_task_item_shard", columnList = "shard_key"),
    @Index(name = "idx_batch_task_item_target", columnList = "target_type,target_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchTaskItem extends BaseEntity {

    /**
     * 批量任务ID
     */
    @NotNull
    @Column(name = "batch_task_id", nullable = false)
    private Long batchTaskId;
    
    /**
     * 目标ID（如商品ID、用户ID等）
     */
    @NotNull
    @Column(name = "target_id", nullable = false)
    private Long targetId;
    
    /**
     * 目标类型（如GOODS、USER等）
     */
    @Column(name = "target_type", length = 30)
    private String targetType;
    
    /**
     * 任务项状态
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private BatchItemStatus status = BatchItemStatus.PENDING;
    
    /**
     * 输入数据（JSON格式）
     */
    @Lob
    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;
    
    /**
     * 输出数据（JSON格式）
     */
    @Lob
    @Column(name = "output_data", columnDefinition = "TEXT")
    private String outputData;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    /**
     * 重试次数
     */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;
    
    /**
     * 处理时间（毫秒）
     */
    @Column(name = "processing_time")
    private Integer processingTime;
    
    /**
     * 分片键（用于分片处理）
     */
    @Column(name = "shard_key", length = 50)
    private String shardKey;

    /**
     * 增加重试次数
     */
    public void incrementRetry() {
        this.retryCount++;
    }

    /**
     * 判断是否可以重试
     */
    public boolean canRetry(int maxRetry) {
        return this.retryCount < maxRetry && this.status == BatchItemStatus.FAILED;
    }

    /**
     * 标记为失败
     */
    public void markFailed(String errorMessage) {
        this.status = BatchItemStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    /**
     * 标记为成功
     */
    public void markSuccess() {
        this.status = BatchItemStatus.SUCCESS;
        this.errorMessage = null;
    }
}
