package com.campus.marketplace.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 验证 2FA 代码请求 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Schema(description = "验证 2FA 代码请求")
public record Verify2FARequest(

        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "^\\d{6}$", message = "验证码必须是6位数字")
        @Schema(description = "6位数字验证码", example = "123456")
        String code
) {
}
