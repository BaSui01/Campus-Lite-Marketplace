package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.CaptchaResponse;
import com.campus.marketplace.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 验证码控制器
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Slf4j
@RestController
@RequestMapping("/api/captcha")
@Tag(name = "验证码管理", description = "图形验证码、滑块验证码生成和验证")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * 生成图形验证码
     *
     * GET /api/captcha/image
     */
    @GetMapping("/image")
    @Operation(
            summary = "生成图形验证码",
            description = "生成4位数字+字母的图形验证码，返回验证码ID和Base64图片"
    )
    public ApiResponse<CaptchaResponse> generateImageCaptcha() {
        log.info("收到生成图形验证码请求");
        CaptchaResponse response = captchaService.generateImageCaptcha();
        return ApiResponse.success("验证码生成成功", response);
    }

    /**
     * 生成滑块验证码
     *
     * GET /api/captcha/slide
     */
    @GetMapping("/slide")
    @Operation(
            summary = "生成滑块验证码",
            description = "生成滑块验证码，返回滑块ID和目标位置"
    )
    public ApiResponse<CaptchaResponse> generateSlideCaptcha() {
        log.info("收到生成滑块验证码请求");
        CaptchaResponse response = captchaService.generateSlideCaptcha();
        return ApiResponse.success("滑块验证码生成成功", response);
    }

    /**
     * 生成滑块验证码（完整版本，包含拼图图片）
     *
     * GET /api/captcha/slide/image
     */
    @GetMapping("/slide/image")
    @Operation(
            summary = "生成滑块验证码（带图片）",
            description = "生成滑块验证码，返回背景图、滑块图和Y轴位置"
    )
    public ApiResponse<com.campus.marketplace.common.dto.response.SlideCaptchaResponse> generateSlideCaptchaWithImage() {
        log.info("收到生成滑块验证码（带图片）请求");
        com.campus.marketplace.common.dto.response.SlideCaptchaResponse response = captchaService.generateSlideCaptchaWithImage();
        return ApiResponse.success("滑块验证码生成成功", response);
    }

    /**
     * 验证滑块验证码（简单版本）
     *
     * POST /api/captcha/slide/verify
     */
    @PostMapping("/slide/verify")
    @Operation(
            summary = "验证滑块验证码（简单版本）",
            description = "验证用户滑动的位置是否正确（允许±5px误差）"
    )
    public ApiResponse<Boolean> verifySlideCaptcha(
            @RequestParam String slideId,
            @RequestParam int position
    ) {
        log.info("收到验证滑块请求: slideId={}, position={}", slideId, position);
        boolean isValid = captchaService.verifySlideCaptcha(slideId, position);
        return ApiResponse.success("验证完成", isValid);
    }

    /**
     * 验证滑块验证码（完整版本，包含轨迹分析）
     *
     * POST /api/captcha/slide/verify/track
     */
    @PostMapping("/slide/verify/track")
    @Operation(
            summary = "验证滑块验证码（带轨迹分析）",
            description = "验证用户滑动的位置和轨迹，防止机器人作弊"
    )
    public ApiResponse<Boolean> verifySlideCaptchaWithTrack(
            @RequestBody @org.springframework.validation.annotation.Validated com.campus.marketplace.common.dto.request.SlideVerifyRequest request
    ) {
        log.info("收到验证滑块请求（带轨迹）: slideId={}, position={}, trackSize={}",
                request.getSlideId(), request.getXPosition(),
                request.getTrack() != null ? request.getTrack().size() : 0);
        boolean isValid = captchaService.verifySlideCaptchaWithTrack(request);
        return ApiResponse.success("验证完成", isValid);
    }

    /**
     * 生成旋转验证码
     *
     * GET /api/captcha/rotate
     */
    @GetMapping("/rotate")
    @Operation(
            summary = "生成旋转验证码",
            description = "生成旋转验证码，返回原始图片和旋转后的图片"
    )
    public ApiResponse<com.campus.marketplace.common.dto.response.RotateCaptchaResponse> generateRotateCaptcha() {
        log.info("收到生成旋转验证码请求");
        com.campus.marketplace.common.dto.response.RotateCaptchaResponse response = captchaService.generateRotateCaptcha();
        return ApiResponse.success("旋转验证码生成成功", response);
    }

    /**
     * 验证旋转验证码
     *
     * POST /api/captcha/rotate/verify
     */
    @PostMapping("/rotate/verify")
    @Operation(
            summary = "验证旋转验证码",
            description = "验证用户旋转的角度是否正确（允许±10度误差）"
    )
    public ApiResponse<Boolean> verifyRotateCaptcha(
            @RequestBody @org.springframework.validation.annotation.Validated com.campus.marketplace.common.dto.request.RotateVerifyRequest request
    ) {
        log.info("收到验证旋转请求: rotateId={}, angle={}", request.getRotateId(), request.getAngle());
        boolean isValid = captchaService.verifyRotateCaptcha(request);
        return ApiResponse.success("验证完成", isValid);
    }

    /**
     * 生成点选验证码
     *
     * GET /api/captcha/click
     */
    @GetMapping("/click")
    @Operation(
            summary = "生成点选验证码",
            description = "生成点选验证码，返回背景图片和需要点击的文字"
    )
    public ApiResponse<com.campus.marketplace.common.dto.response.ClickCaptchaResponse> generateClickCaptcha() {
        log.info("收到生成点选验证码请求");
        com.campus.marketplace.common.dto.response.ClickCaptchaResponse response = captchaService.generateClickCaptcha();
        return ApiResponse.success("点选验证码生成成功", response);
    }

    /**
     * 验证点选验证码
     *
     * POST /api/captcha/click/verify
     */
    @PostMapping("/click/verify")
    @Operation(
            summary = "验证点选验证码",
            description = "验证用户点击的位置是否正确（允许±20px误差）"
    )
    public ApiResponse<Boolean> verifyClickCaptcha(
            @RequestBody @org.springframework.validation.annotation.Validated com.campus.marketplace.common.dto.request.ClickVerifyRequest request
    ) {
        log.info("收到验证点选请求: clickId={}, points={}", request.getClickId(), request.getClickPoints().size());
        boolean isValid = captchaService.verifyClickCaptcha(request);
        return ApiResponse.success("验证完成", isValid);
    }
}
