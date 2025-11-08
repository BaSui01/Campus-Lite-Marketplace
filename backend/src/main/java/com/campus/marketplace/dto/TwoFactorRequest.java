package com.campus.marketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 两步验证请求
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "两步验证请求")
public class TwoFactorRequest {

    @Schema(description = "TOTP 验证码", example = "123456", required = true)
    @NotBlank(message = "验证码不能为空")
    private String code;
}
