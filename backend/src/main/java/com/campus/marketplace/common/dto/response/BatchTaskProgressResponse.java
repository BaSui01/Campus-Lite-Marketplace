package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.enums.BatchTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量任务进度响应
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTaskProgressResponse {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务编码
     */
    private String taskCode;

    /**
     * 任务状态
     */
    private BatchTaskStatus status;

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 已处理数量
     */
    private Integer processedCount;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failedCount;

    /**
     * 进度百分比（0-100）
     */
    private Double progressPercentage;

    /**
     * 预计剩余时间（秒）
     */
    private Long estimatedRemaining;

    /**
     * 是否已完成
     */
    private Boolean completed;
}
