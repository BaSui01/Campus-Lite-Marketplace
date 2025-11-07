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
 * 限流管理服务
 */

import { getApi } from '../utils/apiClient';
import type { ApiResponse } from '../types';

export interface RateLimitRules {
  enabled: boolean;
  userWhitelist: number[];
  ipWhitelist: string[];
  ipBlacklist: string[];
}

export class RateLimitService {
  async getRules(): Promise<RateLimitRules> {
    const res = await http.get<ApiResponse<RateLimitRules>>('/api/admin/rate-limit/rules');
    return res.data;
  }

  async setEnabled(enabled: boolean): Promise<void> {
    await http.post<ApiResponse<void>>(`/api/admin/rate-limit/enabled/${enabled}`);
  }

  async addUserWhitelist(userId: number): Promise<void> {
    await http.post<ApiResponse<void>>(`/api/admin/rate-limit/whitelist/users/${userId}`);
  }

  async removeUserWhitelist(userId: number): Promise<void> {
    await http.delete<ApiResponse<void>>(`/api/admin/rate-limit/whitelist/users/${userId}`);
  }

  async addIpWhitelist(ip: string): Promise<void> {
    await http.post<ApiResponse<void>>(`/api/admin/rate-limit/whitelist/ips/${ip}`);
  }

  async removeIpWhitelist(ip: string): Promise<void> {
    await http.delete<ApiResponse<void>>(`/api/admin/rate-limit/whitelist/ips/${ip}`);
  }

  async addIpBlacklist(ip: string): Promise<void> {
    await http.post<ApiResponse<void>>(`/api/admin/rate-limit/blacklist/ips/${ip}`);
  }

  async removeIpBlacklist(ip: string): Promise<void> {
    await http.delete<ApiResponse<void>>(`/api/admin/rate-limit/blacklist/ips/${ip}`);
  }
}

export const rateLimitService = new RateLimitService();
export default rateLimitService;
