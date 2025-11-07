/**
 * 举报管理服务（管理端）
 */

import { http } from '../utils/apiClient';
import type { ApiResponse, PageResponse } from '../types';

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
