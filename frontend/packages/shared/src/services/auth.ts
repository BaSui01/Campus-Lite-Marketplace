/**
 * 认证 API 服务
 * @author BaSui 😎
 * @description 用户注册、登录、登出、Token 刷新等认证相关接口
 * ✅ 使用 OpenAPI 生成的 DefaultApi（自动处理 /api 前缀）
 */

import { DefaultApi } from '../api';
import { createApi, apiClient } from '../utils/apiClient';
import type {
  RegisterRequest,
  LoginRequest,
  LoginResponse,
} from '../api/models';
import type { ApiResponse } from '../types';

// 注意：以下类型暂时从 types 导入，后续 OpenAPI 生成后应使用 API 类型
interface RefreshTokenRequest {
  refreshToken: string;
}

interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

interface ConfirmRegisterByEmailRequest {
  email: string;
  code: string;
  username: string;
  password: string;
}

interface ResetPasswordByEmailRequest {
  email: string;
  code: string;
  newPassword: string;
}

interface ResetPasswordBySmsRequest {
  username: string;  // ✅ 新增：用户名（必需）
  phone: string;
  code: string;
  newPassword: string;
}

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
    const response = await this.api.register({ registerRequest: data as any });
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
    // 🔧 BaSui 修复：从 localStorage 获取当前 Token 传递给 logout 接口
    // ⚠️ 注意：虽然 apiClient 拦截器会注入 Token 到 Header，
    //    但 OpenAPI 生成的代码会先校验参数不能为空，所以必须传递实际 Token
    const token = this.getAccessToken();
    if (!token) {
      console.warn('[AuthService] ⚠️ Token 不存在，跳过登出接口调用');
      return { code: 200, message: '登出成功', data: undefined } as ApiResponse<void>;
    }

    const response = await this.api.logout(`Bearer ${token}`);
    return response.data as ApiResponse<void>;
  }

  /**
   * 获取当前 Access Token（内部方法）
   * @private
   */
  private getAccessToken(): string | null {
    try {
      // 1. 尝试从 Portal 端 Zustand persist 读取
      const portalAuthStorage = localStorage.getItem('auth-storage');
      if (portalAuthStorage) {
        const portalAuthData = JSON.parse(portalAuthStorage);
        const portalToken = portalAuthData?.state?.accessToken;
        if (portalToken) return portalToken;
      }

      // 2. 尝试从 Admin 端 Zustand persist 读取
      const adminAuthStorage = localStorage.getItem('admin-auth-storage');
      if (adminAuthStorage) {
        const adminAuthData = JSON.parse(adminAuthStorage);
        const adminToken = adminAuthData?.state?.accessToken;
        if (adminToken) return adminToken;
      }

      // 3. 兜底：从 localStorage 直接读取
      return localStorage.getItem('access_token');
    } catch (error) {
      console.error('[AuthService] ❌ 获取 Token 失败:', error);
      return localStorage.getItem('access_token');
    }
  }

  /**
   * 刷新 Token
   * @param _data Token 刷新请求参数（未使用，Token 通过 Header 自动传递）
   * @returns 刷新后的 Token
   */
  async refreshToken(_data?: RefreshTokenRequest): Promise<ApiResponse<RefreshTokenResponse>> {
    // ✅ refresh 需要 requestBody 参数（必需，类型为空对象即可）
    const response = await this.api.refresh({ requestBody: {} });
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
   * 发送注册短信验证码
   * @param phone 手机号
   * @returns 发送结果
   */
  async sendRegisterSmsCode(phone: string): Promise<ApiResponse<void>> {
    const response = await apiClient.post('/auth/register/code/sms', null, { params: { phone } });
    return response.data as ApiResponse<void>;
  }

  /**
   * 邮箱验证码注册
   * @param data 注册请求参数（包含邮箱验证码）
   * @returns 注册响应
   */
  async registerByEmail(data: ConfirmRegisterByEmailRequest): Promise<ApiResponse<void>> {
    // ✅ 参数名应为 confirmRegisterByEmailRequest
    const response = await this.api.registerByEmail({ confirmRegisterByEmailRequest: data as any });
    return response.data as ApiResponse<void>;
  }

  /**
   * 发送重置密码邮箱验证码
   * @param email 邮箱地址
   * @returns 发送结果
   */
  async sendResetEmailCode(email: string): Promise<ApiResponse<void>> {
    const response = await this.api.sendResetEmailCode({ email });
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
    const response = await this.api.sendResetSmsCode({ phone });
    return response.data as ApiResponse<void>;
  }

  /**
   * 通过短信验证码重置密码
   * @param data 重置密码参数（包含 username, phone, code, newPassword）
   * @returns 重置结果
   */
  async resetPasswordBySms(data: ResetPasswordBySmsRequest): Promise<ApiResponse<void>> {
    // ✅ ResetPasswordBySmsRequest 包含 username 字段
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

  // ==================== 2FA 相关接口（新增 - BaSui 2025-11-09） ====================

  /**
   * 启用 2FA（生成密钥和 QR 码）
   * @returns 2FA 设置响应（包含密钥、QR 码、恢复码）
   * ✅ 修复：使用 OpenAPI 生成的 API（BaSui 2025-11-10）
   */
  async enable2FA(): Promise<ApiResponse<any>> {
    // ✅ 使用 OpenAPI 生成的 enable2FA 方法
    const response = await this.api.enable2FA();
    return response.data as ApiResponse<any>;
  }

  /**
   * 验证 2FA 代码并完成启用
   * @param data 验证码
   * @returns 验证结果
   * ✅ 修复：使用 OpenAPI 生成的 API（BaSui 2025-11-10）
   */
  async verify2FA(data: { code: string }): Promise<ApiResponse<void>> {
    // ✅ 使用 OpenAPI 生成的 verify2FA 方法
    const response = await this.api.verify2FA({ verify2FARequest: data });
    return response.data as ApiResponse<void>;
  }

  /**
   * 禁用 2FA
   * @param data 密码
   * @returns 禁用结果
   * ✅ 修复：使用 OpenAPI 生成的 API（BaSui 2025-11-10）
   */
  async disable2FA(data: { password: string }): Promise<ApiResponse<void>> {
    // ✅ 使用 OpenAPI 生成的 disable2FA 方法
    const response = await this.api.disable2FA({ disable2FARequest: data });
    return response.data as ApiResponse<void>;
  }

  /**
   * 重新生成恢复码
   * @param data 密码
   * @returns 新的恢复码列表
   * ✅ 修复:使用 OpenAPI 生成的 API（BaSui 2025-11-10）
   */
  async regenerateRecoveryCodes(data: { password: string }): Promise<ApiResponse<string[]>> {
    // ✅ 使用 OpenAPI 生成的 regenerateRecoveryCodes 方法
    const response = await this.api.regenerateRecoveryCodes({ disable2FARequest: data });
    return response.data as ApiResponse<string[]>;
  }

  /**
   * 检查 2FA 状态
   * @returns 是否启用 2FA
   * ✅ 修复：使用 OpenAPI 生成的 API（BaSui 2025-11-10）
   */
  async check2FAStatus(): Promise<ApiResponse<boolean>> {
    // ✅ 使用 OpenAPI 生成的 check2FAStatus 方法
    const response = await this.api.check2FAStatus();
    return response.data as ApiResponse<boolean>;
  }
}

// 导出单例
export const authService = new AuthService();
export default authService;
