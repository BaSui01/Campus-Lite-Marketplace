/**
 * 限流管理服务
 */

import { http } from '../utils/http';
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
