/**
 * 认证 API 服务
 * @author BaSui 😎
 * @description 用户注册、登录、登出、Token 刷新等认证相关接口
 */

import { http } from '../utils/http';
import type {
  ApiResponse,
  RegisterRequest,
  RegisterResponse,
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
} from '../types';

/**
 * 认证 API 服务类
 */
class AuthService {
  /**
   * 用户注册
   * @param data 注册请求参数
   * @returns 注册响应数据（包含用户信息和 Token）
   */
  async register(data: RegisterRequest): Promise<ApiResponse<RegisterResponse>> {
    return http.post('/auth/register', data);
  }

  /**
   * 用户登录
   * @param data 登录请求参数
   * @returns 登录响应数据（包含用户信息和 Token）
   */
  async login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return http.post('/auth/login', data);
  }

  /**
   * 用户登出
   * @returns 登出响应
   */
  async logout(): Promise<ApiResponse<void>> {
    return http.post('/auth/logout');
  }

  /**
   * 刷新 Token
   * @param data Token 刷新请求参数
   * @returns 刷新后的 Token
   */
  async refreshToken(data: RefreshTokenRequest): Promise<ApiResponse<RefreshTokenResponse>> {
    return http.post('/auth/refresh', data);
  }

  /**
   * 发送验证码（邮箱）
   * @param email 邮箱地址
   * @returns 发送结果
   */
  async sendEmailCode(email: string): Promise<ApiResponse<void>> {
    return http.post('/auth/send-email-code', { email });
  }

  /**
   * 发送验证码（手机号）
   * @param phone 手机号
   * @returns 发送结果
   */
  async sendPhoneCode(phone: string): Promise<ApiResponse<void>> {
    return http.post('/auth/send-phone-code', { phone });
  }

  /**
   * 重置密码
   * @param data 重置密码参数
   * @returns 重置结果
   */
  async resetPassword(data: {
    username: string;
    code: string;
    newPassword: string;
  }): Promise<ApiResponse<void>> {
    return http.post('/auth/reset-password', data);
  }
}

// 导出单例
export const authService = new AuthService();
export default authService;
