package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建举报请求 DTO
 * 
 * @param targetType 举报类型（必填）
 * @param targetId 被举报对象 ID（必填）
 * @param reason 举报原因（必填，1-500字符）
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public record CreateReportRequest(
        
        @NotNull(message = "举报类型不能为空")
        ReportType targetType,
        
        @NotNull(message = "被举报对象 ID 不能为空")
        Long targetId,
        
        @NotBlank(message = "举报原因不能为空")
        @Size(min = 1, max = 500, message = "举报原因长度必须在 1-500 字符之间")
        String reason
) {
}
