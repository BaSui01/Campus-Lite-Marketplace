/**
 * ✅ 管理端用户服务 - 管理端专属版本
 * @author BaSui 😎
 * @description 使用 OpenAPI 生成的 DefaultApi
 *
 * 功能：
 * - 封禁用户（支持指定天数）
 * - 解封用户
 * - 自动解封过期用户
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { BanUserRequest } from '@campus/shared/api';

/**
 * 管理端用户治理服务类
 */
export class AdminUserService {
  /**
   * 封禁用户
   * @param payload - 封禁请求参数
   * @returns Promise<void>
   */
  async banUser(payload: BanUserRequest): Promise<void> {
    const api = getApi();
    const response = await api.banUser({ banUserRequest: payload });

    if (response.data.code !== 200) {
      throw new Error(response.data.message || '封禁用户失败');
    }
  }

  /**
   * 解封用户
   * @param userId - 用户ID
   * @returns Promise<void>
   */
  async unbanUser(userId: number): Promise<void> {
    const api = getApi();
    const response = await api.unbanUser({ userId });

    if (response.data.code !== 200) {
      throw new Error(response.data.message || '解封用户失败');
    }
  }

  /**
   * 自动解封过期用户（定时任务）
   * @returns Promise<number> - 解封的用户数量
   */
  async autoUnbanExpired(): Promise<number> {
    const api = getApi();
    const response = await api.autoUnbanExpiredUsers();

    if (response.data.code !== 200) {
      throw new Error(response.data.message || '自动解封失败');
    }

    return response.data.data ?? 0;
  }
}

// 导出单例实例
export const adminUserService = new AdminUserService();
export default adminUserService;

