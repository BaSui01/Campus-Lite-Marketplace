/**
 * 封禁记录 API 服务
 * @author BaSui 😎
 * @description 封禁记录相关接口（基于 OpenAPI 生成代码）
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type {
  BanLogResponse,
  ApiResponsePageBanLogResponse,
} from '@campus/shared/api';

/**
 * 封禁记录列表查询参数
 */
export interface BannedUserListParams {
  userId?: number;
  isUnbanned?: boolean;
  page?: number;
  size?: number;
}

/**
 * 封禁记录分页响应
 */
export interface BannedUserListResponse {
  content: BanLogResponse[];
  totalElements: number;
  totalPages: number;
}

/**
 * 封禁记录 API 服务类
 */
export class BannedUserService {
  /**
   * 获取封禁记录列表（分页）
   * @param params 查询参数
   * @returns 封禁记录列表（分页）
   */
  async list(params?: BannedUserListParams): Promise<BannedUserListResponse> {
    const api = getApi();
    const response = await api.listBannedUsers(
      params?.userId,
      params?.isUnbanned,
      params?.page,
      params?.size
    );

    const data = response.data.data as ApiResponsePageBanLogResponse['data'];
    return {
      content: data?.content || [],
      totalElements: data?.totalElements || 0,
      totalPages: data?.totalPages || 0,
    };
  }

  /**
   * 解封用户
   * @param userId 用户ID
   */
  async unbanUser(userId: number): Promise<void> {
    const api = getApi();
    await api.unbanUser({ userId });
  }
}

// 导出单例
export const bannedUserService = new BannedUserService();
export default bannedUserService;
