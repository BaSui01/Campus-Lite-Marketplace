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
