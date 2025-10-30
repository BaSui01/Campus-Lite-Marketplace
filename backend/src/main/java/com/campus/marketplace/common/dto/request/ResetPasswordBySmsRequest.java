package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 通过短信验证码重置密码请求
 *
 * @author BaSui
 * @date 2025-10-29
 */

public record ResetPasswordBySmsRequest(
        @NotBlank String username,
        @NotBlank @Pattern(regexp = "^\\+?\\d{6,20}$") String phone,
        @NotBlank String code,
        @NotBlank @Size(min = 6, max = 100) String newPassword
) {}
