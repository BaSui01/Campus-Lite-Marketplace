package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 完成隐私请求 DTO
 *
 * @author BaSui
 * @date 2025-10-29
 */

public record CompletePrivacyRequest(
        @NotBlank(message = "结果路径不能为空")
        String resultPath
) {
}
