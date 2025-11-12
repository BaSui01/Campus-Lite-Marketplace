package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.LoginRequest;
import com.campus.marketplace.common.dto.request.RegisterRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.LoginResponse;
import com.campus.marketplace.service.AuthService;
import com.campus.marketplace.common.annotation.RateLimit;
import com.campus.marketplace.common.dto.request.ConfirmRegisterByEmailRequest;
import com.campus.marketplace.common.dto.request.ResetPasswordByEmailRequest;
import com.campus.marketplace.common.dto.request.ResetPasswordBySmsRequest;
import com.campus.marketplace.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;

import java.util.Map;

/**
 * 认证控制器
 * 
 * 处理用户注册、登录、登出等认证相关请求
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "认证管理", description = "用户注册、登录、登出等认证相关接口")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * 用户注册
     *
     * POST /api/auth/register
     */
    @Operation(
            summary = "用户注册",
            description = "使用校园邮箱注册新账号，注册成功后赠送 100 积分"
    )
        @PostMapping("/register")
    @RateLimit(key = "auth:register", maxRequests = 3, timeWindow = 3600, limitType = com.campus.marketplace.common.annotation.RateLimit.LimitType.IP)
    public ApiResponse<Long> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "注册信息",
                    required = true
            )
            @Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求: username={}", request.username());
        Long userId = authService.register(request);
        return ApiResponse.success("注册成功", userId);
    }

        @PostMapping("/register/code")
    @RateLimit(key = "auth:register:code", maxRequests = 5, timeWindow = 300)
    @Operation(summary = "发送注册邮箱验证码")
    public ApiResponse<Void> sendRegisterEmailCode(@RequestParam String email) {
        authService.sendRegisterEmailCode(email);
        return ApiResponse.success(null);
    }

        @PostMapping("/register/by-email")
    @Operation(summary = "邮箱验证码注册")
    public ApiResponse<Void> registerByEmail(@Valid @RequestBody ConfirmRegisterByEmailRequest request) {
        authService.registerByEmailCode(request);
        return ApiResponse.success(null);
    }

        @PostMapping("/password/reset/code/email")
    @RateLimit(key = "auth:pwd:reset:email:code", maxRequests = 5, timeWindow = 300)
    @Operation(summary = "发送重置密码邮箱验证码")
    public ApiResponse<Void> sendResetEmailCode(@RequestParam String email) {
        authService.sendResetEmailCode(email);
        return ApiResponse.success(null);
    }

        @PostMapping("/password/reset/email")
    @Operation(summary = "通过邮箱验证码重置密码")
    public ApiResponse<Void> resetPasswordByEmail(@Valid @RequestBody ResetPasswordByEmailRequest request) {
        authService.resetPasswordByEmailCode(request);
        return ApiResponse.success(null);
    }

        @PostMapping("/password/reset/code/sms")
    @RateLimit(key = "auth:pwd:reset:sms:code", maxRequests = 5, timeWindow = 300)
    @Operation(summary = "发送重置密码短信验证码")
    public ApiResponse<Void> sendResetSmsCode(@RequestParam String phone) {
        authService.sendResetSmsCode(phone);
        return ApiResponse.success(null);
    }

        @PostMapping("/password/reset/sms")
    @Operation(summary = "通过短信验证码重置密码")
    public ApiResponse<Void> resetPasswordBySms(@Valid @RequestBody ResetPasswordBySmsRequest request) {
        authService.resetPasswordBySmsCode(request);
        return ApiResponse.success(null);
    }

    /**
     * 用户登录
     * 
     * POST /api/auth/login
     */
    @Operation(
            summary = "用户登录",
            description = "使用用户名和密码登录，返回 JWT Token"
    )
    @PostMapping("/login")
    @RateLimit(key = "auth:login", maxRequests = 5, timeWindow = 60, limitType = com.campus.marketplace.common.annotation.RateLimit.LimitType.IP)
    public ApiResponse<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "登录凭证",
                    required = true
            )
            @Valid @RequestBody LoginRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        log.info("收到登录请求: username={}", request.username());
        LoginResponse response = authService.login(request, httpRequest);
        return ApiResponse.success(response);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorization) {
        log.info("收到登出请求");
        
        // 提取 Token（去掉 "Bearer " 前缀）
        String token = authorization.substring(7);
        authService.logout(token);
        
        return ApiResponse.success("登出成功", null);
    }

    @Operation(
            summary = "刷新Token",
            description = "使用 Refresh Token 刷新 Access Token，返回新的双 Token"
    )
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestBody Map<String, String> request) {
        log.info("收到刷新 Token 请求");

        // 从请求体获取 Refresh Token
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "refreshToken 不能为空");
        }

        LoginResponse response = authService.refreshToken(refreshToken);

        return ApiResponse.success(response);
    }

    // ========== BaSui 新增：注册实时校验接口 🎯 ==========

    /**
     * 校验用户名是否已存在
     *
     * GET /api/auth/check-username?username=xxx
     *
     * @param username 用户名
     * @return true-已存在（不可注册），false-可用（可以注册）
     */
    @Operation(
            summary = "校验用户名是否已存在",
            description = "注册时实时校验用户名是否已被占用"
    )
    @GetMapping("/check-username")
    public ApiResponse<Boolean> checkUsername(
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "用户名",
                    required = true,
                    example = "basui"
            )
            @RequestParam @NotBlank(message = "用户名不能为空") String username) {

        String normalizedUsername = username.trim();
        if (normalizedUsername.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        log.debug("校验用户名: {}", normalizedUsername);

        // 查询用户名是否存在
        boolean exists = userRepository.existsByUsername(normalizedUsername);

        String message = exists ? "用户名已存在" : "用户名可用";
        return ApiResponse.success(message, exists);
    }

    /**
     * 校验邮箱是否已存在
     *
     * GET /api/auth/check-email?email=xxx@campus.edu
     *
     * @param email 邮箱
     * @return true-已存在（不可注册），false-可用（可以注册）
     */
    @Operation(
            summary = "校验邮箱是否已存在",
            description = "注册时实时校验邮箱是否已被注册"
    )
    @GetMapping("/check-email")
    public ApiResponse<Boolean> checkEmail(
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "邮箱地址",
                    required = true,
                    example = "basui@campus.edu"
            )
            @RequestParam
            @NotBlank(message = "邮箱不能为空")
            @Email(message = "邮箱格式不正确")
            String email) {

        String normalizedEmail = email.trim().toLowerCase();
        if (normalizedEmail.isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }

        log.debug("校验邮箱: {}", normalizedEmail);

        // 查询邮箱是否存在
        boolean exists = userRepository.existsByEmail(normalizedEmail);

        String message = exists ? "邮箱已被注册" : "邮箱可用";
        return ApiResponse.success(message, exists);
    }
}
