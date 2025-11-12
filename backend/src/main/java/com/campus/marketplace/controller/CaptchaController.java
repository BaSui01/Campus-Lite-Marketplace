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
     * 统一验证码验证接口（新增 - BaSui 2025-11-11）
     *
     * POST /api/captcha/verify
     */
    @PostMapping("/verify")
    @Operation(
            summary = "统一验证码验证接口",
            description = "支持四种验证码类型（image/slider/rotate/click），验证成功后返回验证码通行证（临时token，有效期60秒）"
    )
    public ApiResponse<com.campus.marketplace.common.dto.response.CaptchaVerifyResponse> verifyUnifiedCaptcha(
            @RequestBody @org.springframework.validation.annotation.Validated com.campus.marketplace.common.dto.request.UnifiedCaptchaVerifyRequest request
    ) {
        log.info("收到统一验证码验证请求: type={}, captchaId={}", request.getType(), request.getCaptchaId());
        com.campus.marketplace.common.dto.response.CaptchaVerifyResponse response = captchaService.verifyUnifiedCaptcha(request);
        return ApiResponse.success("验证码验证成功", response);
    }
}
