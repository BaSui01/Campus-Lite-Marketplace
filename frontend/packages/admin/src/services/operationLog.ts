/**
 * 操作日志 API 服务
 * @author BaSui 😎
 * @description 操作日志相关接口（基于 OpenAPI 生成代码）
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { AuditLogResponse } from '@campus/shared/api';

/**
 * 操作日志列表查询参数
 */
export interface OperationLogListParams {
  operatorId?: number;
  actionType?: string;
  startTime?: string;
  endTime?: string;
  page?: number;
  size?: number;
}

/**
 * 操作日志统计数据
 */
export interface OperationLogStatistics {
  totalOperations: number;
  successCount: number;
  failureCount: number;
  todayCount: number;
}

/**
 * 操作日志分页响应
 */
export interface OperationLogListResponse {
  content: AuditLogResponse[];
  totalElements: number;
  totalPages: number;
  statistics: OperationLogStatistics;
}

/**
 * 操作日志 API 服务类
 */
export class OperationLogService {
  /**
   * 获取操作日志列表（分页+统计）
   * @param params 查询参数
   * @returns 操作日志列表（分页+统计）
   */
  async list(params?: OperationLogListParams): Promise<OperationLogListResponse> {
    const api = getApi();
    const response = await api.listOperationLogs(
      params?.operatorId,
      params?.actionType as any,
      params?.startTime,
      params?.endTime,
      params?.page,
      params?.size
    );

    const data = response.data.data as any;
    return {
      content: data?.content || [],
      totalElements: data?.totalElements || 0,
      totalPages: data?.totalPages || 0,
      statistics: data?.statistics || {
        totalOperations: 0,
        successCount: 0,
        failureCount: 0,
        todayCount: 0,
      },
    };
  }
}

// 导出单例
export const operationLogService = new OperationLogService();
export default operationLogService;
