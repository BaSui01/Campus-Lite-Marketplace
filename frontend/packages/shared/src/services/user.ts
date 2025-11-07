/**
 * ⚠️ 警告：此文件仍使用手写 API 路径（http.get/post/put/delete）
 * 🔧 需要重构：将所有 http. 调用替换为 getApi() + DefaultApi 方法
 * 📋 参考：frontend/packages/shared/src/services/order.ts（已完成重构）
 * 👉 重构步骤：
 *    1. 找到对应的 OpenAPI 生成的方法名（在 api/api/default-api.ts）
 *    2. 替换为：const api = getApi(); api.methodName(...)
 *    3. 更新返回值类型
 */
/**
 * 用户 API 服务
 * @author BaSui 😎
 * @description 用户资料、密码修改、用户列表等接口
 */

import { getApi } from '../utils/apiClient';
import type {
  ApiResponse,
  PageInfo,
  User,
  UpdateProfileRequest,
  ChangePasswordRequest,
  UserListQuery,
} from '../types';

/**
 * 用户 API 服务类
 */
export class UserService {
  /**
   * 获取当前用户资料
   * @returns 当前用户信息
   */
  async getProfile(): Promise<ApiResponse<User>> {
    return http.get('/users/profile');
  }

  /**
   * 获取指定用户资料
   * @param userId 用户ID
   * @returns 用户信息
   */
  async getUserById(userId: number): Promise<ApiResponse<User>> {
    return http.get(`/users/${userId}`);
  }

  /**
   * 更新用户资料
   * @param data 更新资料请求参数
   * @returns 更新后的用户信息
   */
  async updateProfile(data: UpdateProfileRequest): Promise<ApiResponse<User>> {
    return http.put('/users/profile', data);
  }

  /**
   * 修改密码
   * @param data 修改密码请求参数
   * @returns 修改结果
   */
  async changePassword(data: ChangePasswordRequest): Promise<ApiResponse<void>> {
    return http.put('/users/password', data);
  }

  /**
   * 获取用户积分记录
   * @param params 查询参数
   * @returns 积分记录列表
   */
  async getPointsLogs(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<PageInfo<any>>> {
    return http.get('/users/points/logs', { params });
  }

  /**
   * 签到
   * @returns 签到结果（包含获得的积分）
   */
  async signIn(): Promise<ApiResponse<{ points: number }>> {
    return http.post('/users/sign-in');
  }
}

// 导出单例
export const userService = new UserService();
export default userService;
