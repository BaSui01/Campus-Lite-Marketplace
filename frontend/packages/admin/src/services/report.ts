/**
 * ✅ 重构完成：已使用 OpenAPI 生成的 API 客户端
 * 📋 使用方法：listPendingReports, handleReport
 */
/**
 * 举报管理服务（管理端）
 * @author BaSui 😎
 * @description 举报列表、处理举报等接口（基于 OpenAPI 生成代码）
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { PageResponse } from '@campus/shared/types';

/**
 * 举报摘要信息
 */
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

/**
 * 处理举报请求
 */
export interface HandleReportPayload {
  approved: boolean;
  handleResult: string;
}

/**
 * 举报管理 API 服务类
 */
export class ReportService {
  /**
   * 获取待处理举报列表（分页）
   * @param params 查询参数
   * @returns 待处理举报列表（分页）
   */
  async listPendingReports(params?: { page?: number; size?: number }): Promise<PageResponse<ReportSummary>> {
    const api = getApi();
    const response = await api.listPendingReports({
      page: params?.page,
      size: params?.size,
    });
    return response.data.data as PageResponse<ReportSummary>;
  }

  /**
   * 处理举报
   * @param id 举报ID
   * @param payload 处理结果
   */
  async handleReport(id: number, payload: HandleReportPayload): Promise<void> {
    const api = getApi();
    await api.handleReport({
      id,
      approved: payload.approved,
      handleResult: payload.handleResult,
    });
  }
}

/**
 * 举报服务实例
 */
export const reportService = new ReportService();
export default reportService;
