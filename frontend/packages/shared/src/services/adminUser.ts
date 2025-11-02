/**
 * 管理端用户治理服务
 */

import { http } from '../utils/http';
import type { ApiResponse } from '../types';

export interface BanUserPayload {
  userId: number;
  reason: string;
  days: number;
}

export class AdminUserService {
  async banUser(payload: BanUserPayload): Promise<void> {
    await http.post<ApiResponse<void>>('/api/admin/users/ban', payload);
  }

  async unbanUser(userId: number): Promise<void> {
    await http.post<ApiResponse<void>>(`/api/admin/users/${userId}/unban`);
  }

  async autoUnbanExpired(): Promise<number> {
    const res = await http.post<ApiResponse<number>>('/api/admin/users/auto-unban');
    return res.data ?? 0;
  }
}

export const adminUserService = new AdminUserService();
export default adminUserService;
