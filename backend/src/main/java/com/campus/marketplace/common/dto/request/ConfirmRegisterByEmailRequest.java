package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 邮箱验证码注册请求
 *
 * @author BaSui
 * @date 2025-10-29
 */

public record ConfirmRegisterByEmailRequest(
        @Email @NotBlank String email,
        @NotBlank String code,
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 6, max = 100) String password
) {}
