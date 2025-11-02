/**
 * 合规管理服务
 */

import { http } from '../utils/http';
import type { ApiResponse, PageResponse } from '../types';

export interface ComplianceWhitelistItem {
  id: number;
  type: string;
  targetId: number;
  createdAt?: string;
}

export interface ComplianceAuditLog {
  id: number;
  targetType: string;
  targetId: number;
  action: string;
  operatorId: number;
  operatorName?: string;
  remark?: string;
  createdAt: string;
}

export class ComplianceService {
  async addWhitelist(type: string, targetId: number): Promise<ComplianceWhitelistItem> {
    const res = await http.post<ApiResponse<ComplianceWhitelistItem>>('/api/admin/compliance/whitelist', null, {
      params: { type, targetId },
    });
    return res.data;
  }

  async removeWhitelist(id: number): Promise<void> {
    await http.delete<ApiResponse<void>>(`/api/admin/compliance/whitelist/${id}`);
  }

  async listAudit(
    targetType: string,
    targetId: number,
    params?: { page?: number; size?: number }
  ): Promise<PageResponse<ComplianceAuditLog>> {
    const res = await http.get<ApiResponse<PageResponse<ComplianceAuditLog>>>('/api/admin/compliance/audit', {
      params: {
        targetType,
        targetId,
        page: params?.page ?? 0,
        size: params?.size ?? 20,
      },
    });
    return res.data;
  }
}

export const complianceService = new ComplianceService();
export default complianceService;
