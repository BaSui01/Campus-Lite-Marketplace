package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 旋转验证码响应 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotateCaptchaResponse {

    /**
     * 旋转验证码ID（用于验证时匹配）
     */
    private String rotateId;

    /**
     * 原始图片（Base64 编码）
     */
    private String originalImage;

    /**
     * 旋转后的图片（Base64 编码）
     */
    private String rotatedImage;

    /**
     * 过期时间（秒）
     */
    @Builder.Default
    private Integer expiresIn = 300; // 5分钟
}
