package com.campus.marketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送验证码请求
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发送验证码请求")
public class SendCodeRequest {

    @Schema(description = "邮箱地址（邮箱验证时使用）", example = "user@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号（手机验证时使用）", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
