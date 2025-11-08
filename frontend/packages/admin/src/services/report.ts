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
 * 举报管理服务（管理端）
 */

import { apiClient } from '@campus/shared/utils/apiClient';
import type { ApiResponse, PageResponse } from '@campus/shared/types';

export interface ReportSummary {
  id: number;
  reporterId: number;
  reporterName?: string;
  targetType: string;
  targetId: number;
  reason: string;
  status: string;
  createdAt: string;
}

export interface HandleReportPayload {
  approved: boolean;
  handleResult: string;
}

export class ReportService {
  async listPendingReports(params?: { page?: number; size?: number }): Promise<PageResponse<ReportSummary>> {
    const res = await http.get<ApiResponse<PageResponse<ReportSummary>>>('/api/reports/pending', {
      params,
    });
    return res.data;
  }

  async handleReport(id: number, payload: HandleReportPayload): Promise<void> {
    await http.post<ApiResponse<void>>(`/api/reports/${id}/handle`, null, {
      params: {
        approved: payload.approved,
        handleResult: payload.handleResult,
      },
    });
  }
}

export const reportService = new ReportService();
export default reportService;
