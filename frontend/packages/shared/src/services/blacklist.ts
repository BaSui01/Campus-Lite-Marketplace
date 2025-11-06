/**
 * 黑名单 API 服务
 * @author BaSui 😎
 * @description 用户黑名单管理：拉黑、解除拉黑、查询黑名单
 */

import { http } from '../utils/http';

/**
 * 黑名单项
 */
export interface BlacklistItem {
  id: number;                    // 黑名单记录ID
  userId: number;                // 当前用户ID
  blockedUserId: number;         // 被拉黑的用户ID
  blockedUserName: string;       // 被拉黑用户的昵称
  blockedUserAvatar?: string;    // 被拉黑用户的头像
  reason?: string;               // 拉黑原因
  createdAt: string;             // 拉黑时间
}

/**
 * 黑名单列表查询参数
 */
export interface BlacklistListParams {
  page?: number;                 // 页码（从1开始）
  size?: number;                 // 每页数量
  keyword?: string;              // 搜索关键词（用户名）
}

/**
 * 黑名单列表响应
 */
export interface BlacklistListResponse {
  items: BlacklistItem[];        // 黑名单列表
  total: number;                 // 总数
  page: number;                  // 当前页
  size: number;                  // 每页数量
  totalPages: number;            // 总页数
}

/**
 * 拉黑用户请求
 */
export interface BlockUserRequest {
  blockedUserId: number;         // 被拉黑的用户ID
  reason?: string;               // 拉黑原因
}

/**
 * 黑名单 API 服务类
 */
export class BlacklistService {
  private BASE_PATH = '/api/blacklist';

  /**
   * 拉黑用户
   * 
   * @param request 拉黑请求参数
   */
  async blockUser(request: BlockUserRequest): Promise<void> {
    await http.post(`${this.BASE_PATH}/block`, request);
  }

  /**
   * 解除拉黑
   * 
   * @param blockedUserId 被拉黑的用户ID
   */
  async unblockUser(blockedUserId: number): Promise<void> {
    await http.delete(`${this.BASE_PATH}/unblock/${blockedUserId}`);
  }

  /**
   * 批量解除拉黑
   * 
   * @param blockedUserIds 被拉黑的用户ID列表
   */
  async batchUnblock(blockedUserIds: number[]): Promise<void> {
    await http.post(`${this.BASE_PATH}/batch-unblock`, { blockedUserIds });
  }

  /**
   * 查询黑名单列表
   * 
   * @param params 查询参数
   * @returns 黑名单列表
   */
  async getBlacklist(params?: BlacklistListParams): Promise<BlacklistListResponse> {
    const response = await http.get(`${this.BASE_PATH}`, { params });
    return response.data.data;
  }

  /**
   * 检查是否已拉黑某用户
   * 
   * @param userId 用户ID
   * @returns 是否已拉黑
   */
  async isBlocked(userId: number): Promise<boolean> {
    const response = await http.get(`${this.BASE_PATH}/check/${userId}`);
    return response.data.data;
  }

  /**
   * 获取黑名单统计
   * 
   * @returns 黑名单总数
   */
  async getBlockedCount(): Promise<number> {
    const response = await http.get(`${this.BASE_PATH}/count`);
    return response.data.data;
  }
}

/**
 * 导出单例实例
 */
export const blacklistService = new BlacklistService();
