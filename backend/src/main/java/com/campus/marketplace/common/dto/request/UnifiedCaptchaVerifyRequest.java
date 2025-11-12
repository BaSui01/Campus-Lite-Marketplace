package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统一验证码验证请求 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-11
 * @description 支持四种验证码类型：图形、滑块、旋转、点击
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedCaptchaVerifyRequest {

    /**
     * 验证码类型
     * - image: 图形验证码
     * - slider: 滑块验证码
     * - rotate: 旋转验证码
     * - click: 点击验证码
     */
    @NotBlank(message = "验证码类型不能为空")
    private String type;

    /**
     * 验证码ID（通用字段，所有类型都需要）
     */
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;

    // ========== 图形验证码字段 ==========
    /**
     * 图形验证码输入（4位字符）
     */
    private String captchaCode;

    // ========== 滑块验证码字段 ==========
    /**
     * 滑块位置（X轴坐标）
     */
    private Integer slidePosition;

    /**
     * 滑块轨迹（可选，用于高级验证）
     */
    private List<TrackPoint> track;

    // ========== 旋转验证码字段 ==========
    /**
     * 旋转角度（0-360度）
     */
    private Integer rotateAngle;

    // ========== 点击验证码字段 ==========
    /**
     * 点击坐标列表
     */
    private List<ClickPoint> clickPoints;

    /**
     * 滑块轨迹点
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackPoint {
        private Integer x;
        private Integer y;
        private Long t;
    }

    /**
     * 点击坐标点
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClickPoint {
        private Integer x;
        private Integer y;
    }
}
