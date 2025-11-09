package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 滑块验证码响应 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlideCaptchaResponse {

    /**
     * 滑块验证码ID（用于验证时匹配）
     */
    private String slideId;

    /**
     * 背景图片（Base64 编码）
     */
    private String backgroundImage;

    /**
     * 滑块图片（Base64 编码）
     */
    private String sliderImage;

    /**
     * 滑块Y轴位置（用于前端定位滑块）
     */
    private Integer yPosition;

    /**
     * 过期时间（秒）
     */
    @Builder.Default
    private Integer expiresIn = 300; // 5分钟
}
