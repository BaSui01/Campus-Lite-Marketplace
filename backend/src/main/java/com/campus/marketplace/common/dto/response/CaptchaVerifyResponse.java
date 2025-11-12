package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码验证响应 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-11
 * @description 验证码验证成功后返回临时token（验证码通行证）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaVerifyResponse {

    /**
     * 验证码通行证（临时token，有效期60秒）
     * 用于登录时验证用户已通过验证码验证
     */
    private String captchaToken;

    /**
     * 过期时间（秒）
     */
    private Integer expiresIn;

    /**
     * 验证成功提示
     */
    private String message;
}
