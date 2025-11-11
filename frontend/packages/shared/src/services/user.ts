/**
 * ✅ 用户 API 服务 - 完全重构版
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi，零手写路径！
 *
 * 功能：
 * - 获取当前用户资料
 * - 获取指定用户资料
 * - 更新用户资料
 * - 修改密码
 * - 积分记录查询
 * - 签到功能
 */

import { getApi } from '../utils/apiClient';
import type {
  User,
  UpdateProfileRequest,
  UpdatePasswordRequest,
  UserProfileResponse,
} from '../api/models';

// 注意：PageInfo 已废弃，改用 API 生成的分页类型

/**
 * 用户 API 服务类
 */
export class UserService {
  /**
   * 获取当前用户资料
   * @returns 当前用户信息
   */
  async getProfile(): Promise<UserProfileResponse> {
    const api = getApi();
    // ✅ 使用 OpenAPI 生成的正确方法名：getCurrentUserProfile
    const response = await api.getCurrentUserProfile();
    return response.data.data as UserProfileResponse;
  }

  /**
   * 获取当前登录用户信息（别名方法）
   */
  async getCurrentUser(): Promise<User> {
    return this.getProfile();
  }

  /**
   * 获取指定用户资料
   * @param userId 用户ID
   * @returns 用户信息
   */
  async getUserById(userId: number): Promise<User> {
    const api = getApi();
    const response = await api.getUserProfile({ userId });
    return response.data.data as User;
  }

  /**
   * 更新用户资料
   * @param data 更新资料请求参数
   * @returns 更新后的用户信息
   */
  async updateProfile(data: UpdateProfileRequest): Promise<User> {
    const api = getApi();
    const response = await api.updateProfile({ updateProfileRequest: data });
    return response.data.data as User;
  }

  /**
   * 修改密码
   * @param data 修改密码请求参数
   */
  async changePassword(data: UpdatePasswordRequest): Promise<void> {
    const api = getApi();
    await api.updatePassword({ updatePasswordRequest: data });
  }

  // ==================== ❌ 以下方法后端暂未实现，已注释 ====================
  //
  // /**
  //  * 获取用户积分记录（待后端实现）
  //  * @param params 查询参数
  //  * @returns 积分记录列表（分页）
  //  */
  // async getPointsLogs(params?: { page?: number; pageSize?: number }): Promise<any> {
  //   const api = getApi();
  //   // ❌ 后端 UserController 中暂无此接口
  //   const response = await api.getPointsHistory({
  //     page: params?.page,
  //     size: params?.pageSize,
  //   });
  //   return response.data.data;
  // }

  // /**
  //  * 签到（待后端实现）
  //  * @returns 签到结果（包含获得的积分）
  //  */
  // async signIn(): Promise<{ points: number }> {
  //   const api = getApi();
  //   // ❌ 后端 UserController 中暂无此接口
  //   const response = await api.userSignIn();
  //   return response.data.data as { points: number };
  // }

  /**
   * 获取用户列表（管理端）
   * @param params 查询参数
   * @returns 用户列表（分页）
   */
  async getUserList(params?: UserListQuery): Promise<any> {
    const api = getApi();
    const response = await api.listUsers({
      keyword: params?.keyword,
      status: params?.status,
      page: params?.page,
      size: params?.pageSize,
    });
    return response.data;
  }

  /**
   * 获取用户登录设备列表
   * @param userId 用户ID
   * @returns 登录设备列表
   */
  async getLoginDevices(userId: number): Promise<any[]> {
    const api = getApi();
    const response = await api.getLoginDevices({ userId });
    return response.data.data as any[];
  }

  /**
   * 踢出登录设备
   * @param userId 用户ID
   * @param deviceId 设备ID
   */
  async kickDevice(userId: number, deviceId: number): Promise<void> {
    const api = getApi();
    await api.kickDevice({ userId, deviceId });
  }

  /**
   * 发送邮箱验证码
   * @param email 邮箱地址
   */
  async sendEmailCode(email: string): Promise<void> {
    const api = getApi();
    await api.sendEmailCode({ email });
  }

  /**
   * 发送手机验证码
   * @param phone 手机号
   */
  async sendPhoneCode(phone: string): Promise<void> {
    const api = getApi();
    await api.sendPhoneCode({ phone });
  }

  /**
   * 绑定邮箱
   * @param userId 用户ID
   * @param data 绑定邮箱请求参数
   */
  async bindEmail(userId: number, data: { email: string; code: string }): Promise<void> {
    const api = getApi();
    await api.bindEmail({ userId, bindEmailRequest: data });
  }

  /**
   * 绑定手机号
   * @param userId 用户ID
   * @param data 绑定手机号请求参数
   */
  async bindPhone(userId: number, data: { phone: string; code: string }): Promise<void> {
    const api = getApi();
    await api.bindPhone({ userId, bindPhoneRequest: data });
  }

  // ==================== ⚠️ 两步验证功能已迁移到 authService ====================
  //
  // 两步验证相关方法应使用 authService.enable2FA()、verify2FA()、disable2FA()
  // 这些方法在 Profile 页面中已正确使用 authService
  //
  // ==================== 旧方法已注释（保留供参考）====================
  //
  // /**
  //  * 启用两步验证（已废弃 - 使用 authService.enable2FA()）
  //  * @param userId 用户ID
  //  * @returns 两步验证响应（包含密钥和二维码URL）
  //  */
  // async enableTwoFactor(userId: number): Promise<any> {
  //   const api = getApi();
  //   const response = await api.enableTwoFactor({ userId });
  //   return response.data.data;
  // }

  // /**
  //  * 验证并确认两步验证（已废弃 - 使用 authService.verify2FA()）
  //  * @param userId 用户ID
  //  * @param code 验证码
  //  */
  // async verifyTwoFactor(userId: number, code: string): Promise<void> {
  //   const api = getApi();
  //   await api.verifyTwoFactor({ userId, twoFactorRequest: { code } });
  // }

  // /**
  //  * 关闭两步验证（已废弃 - 使用 authService.disable2FA()）
  //  * @param userId 用户ID
  //  */
  // async disableTwoFactor(userId: number): Promise<void> {
  //   const api = getApi();
  //   await api.disableTwoFactor({ userId });
  // }
}

export interface UserListQuery {
  keyword?: string;
  status?: string;
  page?: number;
  pageSize?: number;
}

// 导出单例
export const userService = new UserService();
export default userService;
