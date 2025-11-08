package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.BatchTaskStatus;
import com.campus.marketplace.common.enums.BatchType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 批量任务实体
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Entity
@Table(name = "t_batch_task", indexes = {
    @Index(name = "idx_task_code", columnList = "task_code", unique = true),
    @Index(name = "idx_batch_task_user_id", columnList = "user_id"),
    @Index(name = "idx_batch_task_status", columnList = "status"),
    @Index(name = "idx_batch_task_type", columnList = "batch_type"),
    @Index(name = "idx_batch_task_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchTask extends BaseEntity {

    /**
     * 任务编码（唯一标识）
     */
    @NotNull
    @Column(name = "task_code", unique = true, nullable = false, length = 50)
    private String taskCode;
    
    /**
     * 批量操作类型
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "batch_type", nullable = false, length = 30)
    private BatchType batchType;
    
    /**
     * 用户ID
     */
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 总数量
     */
    @Column(name = "total_count", nullable = false)
    @Builder.Default
    private Integer totalCount = 0;
    
    /**
     * 成功数量
     */
    @Column(name = "success_count", nullable = false)
    @Builder.Default
    private Integer successCount = 0;
    
    /**
     * 错误数量
     */
    @Column(name = "error_count", nullable = false)
    @Builder.Default
    private Integer errorCount = 0;
    
    /**
     * 任务状态
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private BatchTaskStatus status = BatchTaskStatus.PENDING;
    
    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    /**
     * 预计执行时间（秒）
     */
    @Column(name = "estimated_duration")
    private Integer estimatedDuration;
    
    /**
     * 进度百分比（0-100）
     */
    @Column(name = "progress_percentage")
    @Builder.Default
    private Double progressPercentage = 0.0;
    
    /**
     * 请求数据（JSON格式）
     */
    @Lob
    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;
    
    /**
     * 结果数据（JSON格式）
     */
    @Lob
    @Column(name = "result_data", columnDefinition = "TEXT")
    private String resultData;
    
    /**
     * 错误摘要
     */
    @Column(name = "error_summary", length = 1000)
    private String errorSummary;

    /**
     * 计算执行时长（秒）
     */
    public Long calculateDuration() {
        if (startTime == null || endTime == null) {
            return null;
        }
        return java.time.Duration.between(startTime, endTime).getSeconds();
    }

    /**
     * 更新进度
     */
    public void updateProgress() {
        if (totalCount > 0) {
            int processedCount = successCount + errorCount;
            this.progressPercentage = (processedCount * 100.0) / totalCount;
        }
    }

    /**
     * 判断任务是否完成
     */
    public boolean isCompleted() {
        return status == BatchTaskStatus.SUCCESS 
            || status == BatchTaskStatus.PARTIAL_SUCCESS 
            || status == BatchTaskStatus.FAILED 
            || status == BatchTaskStatus.CANCELLED;
    }
}
