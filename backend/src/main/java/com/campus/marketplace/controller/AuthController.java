package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.LoginRequest;
import com.campus.marketplace.common.dto.request.RegisterRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.LoginResponse;
import com.campus.marketplace.service.AuthService;
import com.campus.marketplace.common.annotation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
@Tag(name = "认证管理", description = "用户注册、登录、登出等认证相关接口")
public class AuthController {

    private final AuthService authService;

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
    public ApiResponse<Void> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "注册信息",
                    required = true
            )
            @Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求: username={}", request.username());
        authService.register(request);
        return ApiResponse.success("注册成功", null);
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
            @Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求: username={}", request.username());
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    /**
     * 用户登出
     * 
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorization) {
        log.info("收到登出请求");
        
        // 提取 Token（去掉 "Bearer " 前缀）
        String token = authorization.substring(7);
        authService.logout(token);
        
        return ApiResponse.success("登出成功", null);
    }

    /**
     * 刷新 Token
     * 
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestHeader("Authorization") String authorization) {
        log.info("收到刷新 Token 请求");
        
        // 提取 Token（去掉 "Bearer " 前缀）
        String token = authorization.substring(7);
        LoginResponse response = authService.refreshToken(token);
        
        return ApiResponse.success(response);
    }
}
