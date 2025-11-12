/**
 * 审计日志 API 服务
 * @author BaSui 😎
 * @description 审计日志查询服务（管理端专属）
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { PageAuditLogResponse } from '@campus/shared/api';

/**
 * 审计日志列表查询参数
 */
export interface AuditLogListParams {
  operatorId?: number;
  operationType?: string;
  startTime?: string;
  endTime?: string;
  page?: number;
  size?: number;
}

/**
 * 审计日志 API 服务类
 */
export class AuditLogService {
  /**
   * 获取审计日志列表（分页）
   * @param params 查询参数
   * @returns 审计日志列表（分页）
   */
  async list(params?: AuditLogListParams): Promise<PageAuditLogResponse> {
    const api = getApi();
    const response = await api.listAuditLogs({
      operatorId: params?.operatorId,
      operationType: params?.operationType as any,
      startTime: params?.startTime,
      endTime: params?.endTime,
      page: params?.page,
      size: params?.size,
    });
    return response.data.data as PageAuditLogResponse;
  }
}

/**
 * 审计日志服务实例
 */
export const auditLogService = new AuditLogService();

/**
 * 导出单例
 */
export default auditLogService;
