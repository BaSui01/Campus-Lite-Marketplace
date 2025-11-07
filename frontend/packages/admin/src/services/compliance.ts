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
 * 合规管理服务
 */

import { apiClient } from '@campus/shared/utils/apiClient';
import type { ApiResponse, PageResponse } from '@campus/shared/types';

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
