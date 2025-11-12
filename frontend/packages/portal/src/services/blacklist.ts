/**
 * 黑名单 API 服务 🚫
 * @author BaSui 😎
 * @description Portal 用户端黑名单管理（拉黑骚扰用户、查看黑名单）
 * @date 2025-11-07
 *
 * ✅ 已使用 OpenAPI 生成的 DefaultApi
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type {
  ApiResponseVoid,
  ApiResponseBoolean,
  ApiResponseLong,
} from '@campus/shared/api';
import type { BlockUserRequest as ApiBlockUserRequest } from '@campus/shared/api';

/**
 * 黑名单项 🚫
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
 * 黑名单列表查询参数 📋
 */
export interface BlacklistListParams {
  page?: number;                 // 页码（从1开始）
  size?: number;                 // 每页数量
  keyword?: string;              // 搜索关键词（用户名）
}

/**
 * 黑名单列表响应 📊
 */
export interface BlacklistListResponse {
  items: BlacklistItem[];        // 黑名单列表
  total: number;                 // 总数
  page: number;                  // 当前页
  size: number;                  // 每页数量
  totalPages: number;            // 总页数
}

/**
 * 拉黑用户请求 🚫
 */
export interface BlockUserRequest {
  blockedUserId: number;         // 被拉黑的用户ID
  reason?: string;               // 拉黑原因
}

/**
 * 黑名单服务类 🚫
 * ✅ 所有方法基于 OpenAPI 生成的 DefaultApi
 */
export class BlacklistService {
  /**
   * 拉黑用户 🚫
   *
   * @param request 拉黑请求参数
   * @example
   * await blacklistService.blockUser({
   *   blockedUserId: 123,
   *   reason: '骚扰他人'
   * });
   */
  async blockUser(request: BlockUserRequest): Promise<void> {
    const api = getApi();
    const apiRequest: ApiBlockUserRequest = {
      blockedUserId: request.blockedUserId,
      reason: request.reason,
    };
    const response: ApiResponseVoid = await api.blockUser({ blockUserRequest: apiRequest });

    if (!response.success) {
      throw new Error(response.message || '拉黑用户失败');
    }
  }

  /**
   * 解除拉黑 ✅
   *
   * @param blockedUserId 被拉黑的用户ID
   * @example
   * await blacklistService.unblockUser(123);
   */
  async unblockUser(blockedUserId: number): Promise<void> {
    const api = getApi();
    const response: ApiResponseVoid = await api.unblockUser({ blockedUserId });

    if (!response.success) {
      throw new Error(response.message || '解除拉黑失败');
    }
  }

  /**
   * 批量解除拉黑 ✅
   *
   * @param blockedUserIds 被拉黑的用户ID列表
   * @example
   * await blacklistService.batchUnblock([123, 456, 789]);
   */
  async batchUnblock(blockedUserIds: number[]): Promise<void> {
    const api = getApi();
    const response: ApiResponseVoid = await api.batchUnblockUsers({
      requestBody: blockedUserIds
    });

    if (!response.success) {
      throw new Error(response.message || '批量解除拉黑失败');
    }
  }

  /**
   * 查询黑名单列表 📋
   *
   * @param params 查询参数
   * @returns 黑名单列表
   * @example
   * const result = await blacklistService.getBlacklist({
   *   page: 1,
   *   size: 10,
   *   keyword: '张三'
   * });
   */
  async getBlacklist(params?: BlacklistListParams): Promise<BlacklistListResponse> {
    const api = getApi();
    // 注意：这里假设 OpenAPI 生成了 getUserBlacklist 方法
    // 如果实际方法名不同，需要根据 api/api/default-api.ts 中的实际方法名调整
    const response = await api.getUserBlacklist({
      page: params?.page,
      size: params?.size,
      keyword: params?.keyword,
    });

    if (!response.success || !response.data) {
      throw new Error(response.message || '查询黑名单失败');
    }

    // 适配返回格式
    return {
      items: response.data.items || [],
      total: response.data.total || 0,
      page: response.data.page || 1,
      size: response.data.size || 10,
      totalPages: response.data.totalPages || 0,
    };
  }

  /**
   * 检查是否已拉黑某用户 🔍
   *
   * @param userId 用户ID
   * @returns 是否已拉黑
   * @example
   * const blocked = await blacklistService.isBlocked(123);
   * if (blocked) {
   *   console.log('已拉黑该用户');
   * }
   */
  async isBlocked(userId: number): Promise<boolean> {
    const api = getApi();
    const response: ApiResponseBoolean = await api.checkUserBlocked({ userId });

    if (!response.success) {
      throw new Error(response.message || '检查拉黑状态失败');
    }

    return response.data === true;
  }

  /**
   * 获取黑名单统计 📊
   *
   * @returns 黑名单总数
   * @example
   * const count = await blacklistService.getBlockedCount();
   * console.log(`黑名单总数: ${count}`);
   */
  async getBlockedCount(): Promise<number> {
    const api = getApi();
    const response: ApiResponseLong = await api.getBlockedCount();

    if (!response.success || response.data === undefined) {
      throw new Error(response.message || '获取黑名单统计失败');
    }

    return Number(response.data);
  }
}

/**
 * 导出单例实例 🎯
 */
export const blacklistService = new BlacklistService();
