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
  ChangePasswordRequest,
  PageInfo,
} from '../api/models';

/**
 * 用户 API 服务类
 */
export class UserService {
  /**
   * 获取当前用户资料
   * @returns 当前用户信息
   */
  async getProfile(): Promise<User> {
    const api = getApi();
    // 注意：后端可能需要传userId，这里先尝试getCurrentUser
    const response = await api.getCurrentUser();
    return response.data.data as User;
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
    const response = await api.updateUserProfile({ updateProfileRequest: data });
    return response.data.data as User;
  }

  /**
   * 修改密码
   * @param data 修改密码请求参数
   */
  async changePassword(data: ChangePasswordRequest): Promise<void> {
    const api = getApi();
    await api.changePassword({ changePasswordRequest: data });
  }

  /**
   * 获取用户积分记录
   * @param params 查询参数
   * @returns 积分记录列表
   */
  async getPointsLogs(params?: { page?: number; pageSize?: number }): Promise<PageInfo<any>> {
    const api = getApi();
    const response = await api.getUserPointsLogs({
      page: params?.page,
      size: params?.pageSize,
    });
    return response.data.data as PageInfo<any>;
  }

  /**
   * 签到
   * @returns 签到结果（包含获得的积分）
   */
  async signIn(): Promise<{ points: number }> {
    const api = getApi();
    const response = await api.userSignIn();
    return response.data.data as { points: number };
  }
}

// 导出单例
export const userService = new UserService();
export default userService;
