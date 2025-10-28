package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.LoginRequest;
import com.campus.marketplace.common.dto.request.RegisterRequest;
import com.campus.marketplace.common.dto.response.LoginResponse;

/**
 * 认证服务接口
 * 
 * @author BaSui
 * @date 2025-10-25
 */
public interface AuthService {

    /**
     * 用户注册
     * 
     * @param request 注册请求
     */
    void register(RegisterRequest request);

    // 发送注册邮箱验证码
    void sendRegisterEmailCode(String email);

    // 使用邮箱验证码注册
    void registerByEmailCode(com.campus.marketplace.common.dto.request.ConfirmRegisterByEmailRequest request);

    // 发送重置密码邮箱验证码
    void sendResetEmailCode(String email);

    // 通过邮箱验证码重置密码
    void resetPasswordByEmailCode(com.campus.marketplace.common.dto.request.ResetPasswordByEmailRequest request);

    // 发送重置密码短信验证码（开发阶段仅日志）
    void sendResetSmsCode(String phone);

    // 通过短信验证码重置密码（需提供用户名+手机号匹配）
    void resetPasswordBySmsCode(com.campus.marketplace.common.dto.request.ResetPasswordBySmsRequest request);

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应（包含 Token 和用户信息）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     * 
     * @param token JWT Token
     */
    void logout(String token);

    /**
     * 刷新 Token
     * 
     * @param token 旧 Token
     * @return 新 Token
     */
    LoginResponse refreshToken(String token);
}
