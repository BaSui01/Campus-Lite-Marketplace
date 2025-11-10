package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 点选验证码响应 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickCaptchaResponse {

    /**
     * 点选验证码ID（用于验证时匹配）
     */
    private String clickId;

    /**
     * 背景图片（Base64 编码）
     */
    private String backgroundImage;

    /**
     * 需要点击的文字列表（例如：["春", "天", "来", "了"]）
     */
    private List<String> targetWords;

    /**
     * 提示文字（例如："请依次点击【春】【天】"）
     */
    private String hint;

    /**
     * 过期时间（秒）
     */
    @Builder.Default
    private Integer expiresIn = 300; // 5分钟
}
