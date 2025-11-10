package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.SlideVerifyRequest;
import com.campus.marketplace.common.dto.response.CaptchaResponse;
import com.campus.marketplace.common.dto.response.SlideCaptchaResponse;

/**
 * 验证码服务接口
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
public interface CaptchaService {

    /**
     * 生成图形验证码
     *
     * @return 验证码响应（包含验证码ID和Base64图片）
     */
    CaptchaResponse generateImageCaptcha();

    /**
     * 验证图形验证码
     *
     * @param captchaId 验证码ID
     * @param code      用户输入的验证码
     * @return 验证是否通过
     */
    boolean verifyImageCaptcha(String captchaId, String code);

    /**
     * 生成滑块验证码（简单版本，兼容旧接口）
     *
     * @return 滑块验证码响应（包含滑块ID和目标位置）
     */
    CaptchaResponse generateSlideCaptcha();

    /**
     * 生成滑块验证码（完整版本，包含拼图图片）
     *
     * @return 滑块验证码响应（包含背景图、滑块图、Y轴位置）
     */
    SlideCaptchaResponse generateSlideCaptchaWithImage();

    /**
     * 验证滑块验证码（简单版本，只验证X轴位置）
     *
     * @param slideId      滑块ID
     * @param userPosition 用户滑动的位置
     * @return 验证是否通过
     */
    boolean verifySlideCaptcha(String slideId, int userPosition);

    /**
     * 验证滑块验证码（完整版本，包含轨迹分析）
     *
     * @param request 滑块验证请求（包含滑块ID、位置、轨迹）
     * @return 验证是否通过
     */
    boolean verifySlideCaptchaWithTrack(SlideVerifyRequest request);

    /**
     * 生成旋转验证码
     *
     * @return 旋转验证码响应（包含原始图片、旋转后的图片）
     */
    com.campus.marketplace.common.dto.response.RotateCaptchaResponse generateRotateCaptcha();

    /**
     * 验证旋转验证码
     *
     * @param request 旋转验证请求（包含旋转验证码ID、用户旋转的角度）
     * @return 验证是否通过
     */
    boolean verifyRotateCaptcha(com.campus.marketplace.common.dto.request.RotateVerifyRequest request);

    /**
     * 生成点选验证码
     *
     * @return 点选验证码响应（包含背景图片、需要点击的文字）
     */
    com.campus.marketplace.common.dto.response.ClickCaptchaResponse generateClickCaptcha();

    /**
     * 验证点选验证码
     *
     * @param request 点选验证请求（包含点选验证码ID、用户点击的坐标）
     * @return 验证是否通过
     */
    boolean verifyClickCaptcha(com.campus.marketplace.common.dto.request.ClickVerifyRequest request);
}
