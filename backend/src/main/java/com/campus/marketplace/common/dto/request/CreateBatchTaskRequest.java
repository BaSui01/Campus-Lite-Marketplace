package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.BatchType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建批量任务请求
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBatchTaskRequest {

    /**
     * 批量操作类型
     */
    @NotNull(message = "批量操作类型不能为空")
    private BatchType batchType;

    /**
     * 请求数据（JSON格式）
     */
    @NotNull(message = "请求数据不能为空")
    private String requestData;

    /**
     * 预计执行时间（秒）
     */
    private Integer estimatedDuration;
}
