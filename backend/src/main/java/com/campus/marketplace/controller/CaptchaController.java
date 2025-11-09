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
}
