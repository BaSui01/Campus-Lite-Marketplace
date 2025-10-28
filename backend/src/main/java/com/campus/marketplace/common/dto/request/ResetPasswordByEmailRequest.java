package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 通过邮箱验证码重置密码请求
 */
public record ResetPasswordByEmailRequest(
        @Email @NotBlank String email,
        @NotBlank String code,
        @NotBlank @Size(min = 6, max = 100) String newPassword
) {}
