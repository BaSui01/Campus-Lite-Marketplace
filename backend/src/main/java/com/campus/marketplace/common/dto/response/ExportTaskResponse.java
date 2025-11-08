package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 导出任务响应 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskResponse {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务类型（COUPON_STATISTICS/COUPON_TREND/COUPON_USER_RANKING）
     */
    private String taskType;

    /**
     * 导出格式（EXCEL/CSV）
     */
    private String format;

    /**
     * 任务状态（PENDING/PROCESSING/COMPLETED/FAILED）
     */
    private String status;

    /**
     * 进度（0-100）
     */
    private Integer progress;

    /**
     * 文件下载URL
     */
    private String downloadUrl;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
}
