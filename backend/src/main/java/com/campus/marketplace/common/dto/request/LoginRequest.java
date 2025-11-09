package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求 DTO
 *
 * @author BaSui
 * @date 2025-10-25
 * @updated 2025-11-09 - 添加验证码字段
 */
public record LoginRequest(

        @NotBlank(message = "用户名不能为空")
        String username,

        @NotBlank(message = "密码不能为空")
        String password,

        /**
         * 验证码ID（可选，用于图形验证码或滑块验证码）
         */
        String captchaId,

        /**
         * 验证码输入（可选，用于图形验证码）
         */
        String captchaCode,

        /**
         * 滑块位置（可选，用于滑块验证码）
         */
        Integer slidePosition,

        /**
         * 2FA 验证码（可选，6位数字，用于双因素认证）
         */
        String twoFactorCode
) {
}
