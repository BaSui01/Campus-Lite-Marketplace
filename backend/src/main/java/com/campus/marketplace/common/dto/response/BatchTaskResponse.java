package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.enums.BatchTaskStatus;
import com.campus.marketplace.common.enums.BatchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 批量任务响应
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTaskResponse {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务编码
     */
    private String taskCode;

    /**
     * 批量操作类型
     */
    private BatchType batchType;

    /**
     * 批量操作类型描述
     */
    private String batchTypeDesc;

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 错误数量
     */
    private Integer errorCount;

    /**
     * 任务状态
     */
    private BatchTaskStatus status;

    /**
     * 任务状态描述
     */
    private String statusDesc;

    /**
     * 进度百分比
     */
    private Double progressPercentage;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行时长（秒）
     */
    private Long duration;

    /**
     * 错误摘要
     */
    private String errorSummary;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
