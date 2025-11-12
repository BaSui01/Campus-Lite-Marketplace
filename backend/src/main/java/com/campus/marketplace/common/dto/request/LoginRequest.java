package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求 DTO
 *
 * @author BaSui 😎
 * @date 2025-10-25
 * @updated 2025-11-11 - 同时支持方案A(直接验证)和方案B(验证码通行证)
 */
public record LoginRequest(

        @NotBlank(message = "用户名不能为空")
        String username,

        @NotBlank(message = "密码不能为空")
        String password,

        /**
         * 🎯 方案B：验证码通行证(推荐)
         * 通过 POST /api/captcha/verify 接口获取
         * 有效期60秒,一次性使用
         */
        String captchaToken,

        /**
         * 🔄 方案A：验证码ID(兼容旧方式)
         * 用于图形/滑块/旋转/点击验证码
         */
        String captchaId,

        /**
         * 🔄 方案A：图形验证码答案
         */
        String captchaCode,

        /**
         * 🔄 方案A：滑块验证码位置
         */
        Integer slidePosition,

        /**
         * 🔄 方案A：旋转验证码角度
         */
        Integer rotateAngle,

        /**
         * 🔄 方案A：点击验证码坐标列表
         */
        java.util.List<ClickPoint> clickPoints,

        /**
         * 2FA 验证码(可选,6位数字,用于双因素认证)
         */
        String twoFactorCode
) {
    /**
     * 点击验证码坐标点
     */
    public record ClickPoint(int x, int y) {}
}
