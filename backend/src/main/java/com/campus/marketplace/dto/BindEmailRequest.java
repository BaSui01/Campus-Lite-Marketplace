package com.campus.marketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 绑定邮箱请求
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "绑定邮箱请求")
public class BindEmailRequest {

    @Schema(description = "邮箱地址", example = "user@example.com", required = true)
    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "验证码", example = "123456", required = true)
    @NotBlank(message = "验证码不能为空")
    private String code;
}
