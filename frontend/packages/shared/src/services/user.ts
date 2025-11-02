/**
 * 用户 API 服务
 * @author BaSui 😎
 * @description 用户资料、密码修改、用户列表等接口
 */

import { http } from '../utils/http';
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
   * 获取用户列表（管理员）
   * @param params 查询参数
   * @returns 用户列表
   */
  async getUserList(params: UserListQuery): Promise<ApiResponse<PageInfo<User>>> {
    return http.get('/users', { params });
  }

  /**
   * 封禁用户（管理员）
   * @param userId 用户ID
   * @param reason 封禁原因
   * @param bannedUntil 封禁截止时间（可选，不传则永久封禁）
   * @returns 封禁结果
   */
  async banUser(userId: number, reason: string, bannedUntil?: string): Promise<ApiResponse<void>> {
    return http.post(`/users/${userId}/ban`, { reason, bannedUntil });
  }

  /**
   * 解封用户（管理员）
   * @param userId 用户ID
   * @returns 解封结果
   */
  async unbanUser(userId: number): Promise<ApiResponse<void>> {
    return http.post(`/users/${userId}/unban`);
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
