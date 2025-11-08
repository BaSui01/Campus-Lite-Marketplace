/**
 * ✅ 黑名单管理 API 服务 - 完全重构版
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi，零手写路径！
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type {
  BlacklistEntry,
  PageBlacklistEntry,
  CheckBlacklistRelationResponse,
  BatchUnblockRequest,
} from '@campus/shared/api';

export class BlacklistService {
  async listBlacklist(params?: {
    page?: number;
    size?: number;
  }): Promise<PageBlacklistEntry> {
    const api = getApi();
    const response = await api.listBlacklist({
      page: params?.page,
      size: params?.size,
    });
    return response.data.data as PageBlacklistEntry;
  }

  async addToBlacklist(blockedUserId: number, reason?: string): Promise<void> {
    const api = getApi();
    await api.addToBlacklist({ blockedUserId, reason });
  }

  async removeFromBlacklist(blockedUserId: number): Promise<void> {
    const api = getApi();
    await api.removeFromBlacklist({ blockedUserId });
  }

  async checkRelation(userId: number, targetUserId: number): Promise<CheckBlacklistRelationResponse> {
    const api = getApi();
    const response = await api.checkRelation({ userId, targetUserId });
    return response.data.data as CheckBlacklistRelationResponse;
  }

  async batchUnblock(request: BatchUnblockRequest): Promise<number> {
    const api = getApi();
    const response = await api.batchUnblock({ batchUnblockRequest: request });
    return response.data.data as number;
  }
}

export const blacklistService = new BlacklistService();
export default blacklistService;
