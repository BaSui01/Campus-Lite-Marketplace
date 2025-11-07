/**
 * 管理端用户服务
 * @author BaSui 😎
 * @description 用户列表、封禁/解封、自动解封等管理员专属功能
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { ApiResponse, PageInfo, User, UserListQuery } from '@campus/shared/types';

export interface BanUserPayload {
  userId: number;
  reason: string;
  days: number;
}

/**
 * 管理员用户服务类
 */
export class AdminUserService {
  /**
   * 获取用户列表（管理员）
   * @param params 查询参数
   * @returns 用户列表
   */
  async getUserList(params: UserListQuery): Promise<ApiResponse<PageInfo<User>>> {
    const api = getApi();
    // TODO: 使用 OpenAPI 生成的方法替换手写路径
    const response = await api.axiosInstance.get<ApiResponse<PageInfo<User>>>('/users', { params });
    return response.data;
  }

  /**
   * 封禁用户（管理员）
   * @param payload 封禁参数
   * @returns 封禁结果
   */
  async banUser(payload: BanUserPayload): Promise<void> {
    const api = getApi();
    await api.axiosInstance.post<ApiResponse<void>>('/admin/users/ban', payload);
  }

  /**
   * 解封用户（管理员）
   * @param userId 用户ID
   * @returns 解封结果
   */
  async unbanUser(userId: number): Promise<void> {
    const api = getApi();
    await api.axiosInstance.post<ApiResponse<void>>(`/admin/users/${userId}/unban`);
  }

  /**
   * 自动解封过期用户（管理员）
   * @returns 解封的用户数
   */
  async autoUnbanExpired(): Promise<number> {
    const api = getApi();
    const response = await api.axiosInstance.post<ApiResponse<number>>('/admin/users/auto-unban');
    return response.data.data ?? 0;
  }
}

export const adminUserService = new AdminUserService();
export default adminUserService;
