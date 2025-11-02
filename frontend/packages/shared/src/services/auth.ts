/**
 * 认证 API 服务
 * @author BaSui 😎
 * @description 用户注册、登录、登出、Token 刷新等认证相关接口
 * ✅ 使用 OpenAPI 生成的 DefaultApi（自动处理 /api 前缀）
 */

import { DefaultApi } from '../api';
import { createApi } from '../utils/apiClient';
import type {
  ApiResponse,
  RegisterRequest,
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
  ConfirmRegisterByEmailRequest,
  ResetPasswordByEmailRequest,
  ResetPasswordBySmsRequest,
} from '../types';

/**
 * 认证 API 服务类
 * ✅ 基于 OpenAPI 生成的代码，路径自动包含 /api 前缀
 */
export class AuthService {
  private api: DefaultApi;

  constructor() {
    // 使用共享 API 客户端创建与当前环境一致的 DefaultApi 实例
    this.api = createApi();
  }

  /**
   * 用户注册
   * @param data 注册请求参数
   * @returns 注册响应数据（用户 ID）
   */
  async register(data: RegisterRequest): Promise<ApiResponse<number>> {
    const response = await this.api.register({ registerRequest: data });
    return response.data as ApiResponse<number>;
  }

  /**
   * 用户登录
   * @param data 登录请求参数
   * @returns 登录响应数据（包含用户信息和 Token）
   */
  async login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    const response = await this.api.login({ loginRequest: data });
    return response.data as ApiResponse<LoginResponse>;
  }

  /**
   * 用户登出
   * @returns 登出响应
   */
  async logout(): Promise<ApiResponse<void>> {
    const response = await this.api.logout();
    return response.data as ApiResponse<void>;
  }

  /**
   * 刷新 Token
   * @param data Token 刷新请求参数
   * @returns 刷新后的 Token
   */
  async refreshToken(data: RefreshTokenRequest): Promise<ApiResponse<RefreshTokenResponse>> {
    const response = await this.api.refreshToken({ refreshTokenRequest: data });
    return response.data as ApiResponse<RefreshTokenResponse>;
  }

  /**
   * 发送注册邮箱验证码
   * @param email 邮箱地址
   * @returns 发送结果
   */
  async sendRegisterEmailCode(email: string): Promise<ApiResponse<void>> {
    const response = await this.api.sendRegisterEmailCode({ email });
    return response.data as ApiResponse<void>;
  }

  /**
   * 邮箱验证码注册
   * @param data 注册请求参数（包含邮箱验证码）
   * @returns 注册响应
   */
  async registerByEmail(data: ConfirmRegisterByEmailRequest): Promise<ApiResponse<void>> {
    const response = await this.api.confirmRegisterByEmail({ confirmRegisterByEmailRequest: data });
    return response.data as ApiResponse<void>;
  }

  /**
   * 发送重置密码邮箱验证码
   * @param email 邮箱地址
   * @returns 发送结果
   */
  async sendResetEmailCode(email: string): Promise<ApiResponse<void>> {
    const response = await this.api.sendPasswordResetEmailCode({ email });
    return response.data as ApiResponse<void>;
  }

  /**
   * 通过邮箱验证码重置密码
   * @param data 重置密码参数
   * @returns 重置结果
   */
  async resetPasswordByEmail(data: ResetPasswordByEmailRequest): Promise<ApiResponse<void>> {
    const response = await this.api.resetPasswordByEmail({ resetPasswordByEmailRequest: data });
    return response.data as ApiResponse<void>;
  }

  /**
   * 发送重置密码短信验证码
   * @param phone 手机号
   * @returns 发送结果
   */
  async sendResetSmsCode(phone: string): Promise<ApiResponse<void>> {
    const response = await this.api.sendPasswordResetSmsCode({ phone });
    return response.data as ApiResponse<void>;
  }

  /**
   * 通过短信验证码重置密码
   * @param data 重置密码参数
   * @returns 重置结果
   */
  async resetPasswordBySms(data: ResetPasswordBySmsRequest): Promise<ApiResponse<void>> {
    const response = await this.api.resetPasswordBySms({ resetPasswordBySmsRequest: data });
    return response.data as ApiResponse<void>;
  }

  // ========== BaSui 新增：注册实时校验方法 🎯 ==========

  /**
   * 检查用户名是否已存在
   * @param username 用户名
   * @returns true-已存在（不可注册），false-可用（可以注册）
   */
  async checkUsername(username: string): Promise<ApiResponse<boolean>> {
    const response = await this.api.checkUsername({ username });
    return response.data as ApiResponse<boolean>;
  }

  /**
   * 检查邮箱是否已存在
   * @param email 邮箱
   * @returns true-已存在（不可注册），false-可用（可以注册）
   */
  async checkEmail(email: string): Promise<ApiResponse<boolean>> {
    const response = await this.api.checkEmail({ email });
    return response.data as ApiResponse<boolean>;
  }
}

// 导出单例
export const authService = new AuthService();
export default authService;
