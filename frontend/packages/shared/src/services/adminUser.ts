/**
 * ✅ 已重构：使用 OpenAPI 生成的 DefaultApi
 *
 * 管理端用户治理服务 - BaSui 搞笑专业版 😎
 *
 * @author BaSui
 * @date 2025-11-07
 */

import { getApi } from '../utils/apiClient';
import type { BanUserRequest } from '../api';

/**
 * 管理端用户治理服务类
 *
 * 功能：
 * - 封禁用户（支持指定天数）
 * - 解封用户
 * - 自动解封过期用户
 */
export class AdminUserService {
  /**
   * 封禁用户
   *
   * @param payload - 封禁请求参数
   * @returns Promise<void>
   *
   * @example
   * await adminUserService.banUser({
   *   userId: 10002,
   *   reason: '违反社区规则',
   *   days: 7
   * });
   */
  async banUser(payload: BanUserRequest): Promise<void> {
    const api = getApi();
    const response = await api.banUser({ banUserRequest: payload });

    // 检查响应是否成功
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '封禁用户失败');
    }
  }

  /**
   * 解封用户
   *
   * @param userId - 用户ID
   * @returns Promise<void>
   *
   * @example
   * await adminUserService.unbanUser(10002);
   */
  async unbanUser(userId: number): Promise<void> {
    const api = getApi();
    const response = await api.unbanUser({ userId });

    // 检查响应是否成功
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '解封用户失败');
    }
  }

  /**
   * 自动解封过期用户（定时任务）
   *
   * @returns Promise<number> - 解封的用户数量
   *
   * @example
   * const count = await adminUserService.autoUnbanExpired();
   * console.log(`成功解封 ${count} 个用户`);
   */
  async autoUnbanExpired(): Promise<number> {
    const api = getApi();
    const response = await api.autoUnbanExpiredUsers();

    // 检查响应是否成功
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '自动解封失败');
    }

    return response.data.data ?? 0;
  }
}

// 导出单例实例（方便使用）
export const adminUserService = new AdminUserService();
export default adminUserService;
