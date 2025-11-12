package com.campus.marketplace.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 禁用 2FA 请求 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Schema(description = "禁用 2FA 请求")
public record Disable2FARequest(

        @NotBlank(message = "密码不能为空")
        @Schema(description = "用户密码（用于验证身份）", example = "password123")
        String password
) {
}
