package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaResponse {

    /**
     * 验证码ID（用于验证时匹配）
     */
    private String captchaId;

    /**
     * 验证码图片（Base64 编码）
     */
    private String imageBase64;

    /**
     * 过期时间（秒）
     */
    @Builder.Default
    private Integer expiresIn = 300; // 5分钟
}
