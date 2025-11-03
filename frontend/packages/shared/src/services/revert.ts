/**
 * 撤销操作 API 服务
 * @author BaSui 😎
 * @description 数据撤销、审批管理等接口（基于后端RevertController）
 */

import { getApi } from '../utils/apiClient';
import type { 
  CreateRevertRequest, 
  RevertExecutionResult,
  RevertRequestResponse
} from '../types/revert';

/**
 * 撤销请求查询参数
 */
export interface RevertRequestParams {
  page?: number;       // 页码（从 0 开始）
  size?: number;       // 每页大小
  sortBy?: string;     // 排序字段
  sortDirection?: string;  // 排序方向
}

/**
 * 撤销操作服务类
 */
export class RevertService {
  /**
   * 申请撤销操作
   * @param auditLogId 审计日志ID
   * @param request 撤销请求数据
   * @returns 执行结果
   */
  async requestRevert(
    auditLogId: number, 
    request: CreateRevertRequest
  ): Promise<RevertExecutionResult> {
    const api = getApi();
    const response = await api.requestRevert(auditLogId, request);
    return response.data.data as RevertExecutionResult;
  }

  /**
   * 查询用户的撤销请求历史
   * @param params 查询参数
   * @returns 撤销请求列表（分页）
   */
  async getUserRevertRequests(
    params?: RevertRequestParams
  ): Promise<RevertRequestResponse> {
    const api = getApi();
    const response = await api.getUserRevertRequests(
      params?.page,
      params?.size,
      undefined, // sort
      undefined  // additional params
    );
    return response.data.data as RevertRequestResponse;
  }

  /**
   * 执行撤销操作（管理员）
   * @param revertRequestId 撤销请求ID
   * @returns 执行结果
   */
  async executeRevert(revertRequestId: number): Promise<RevertExecutionResult> {
    const api = getApi();
    const response = await api.executeRevert(revertRequestId);
    return response.data.data as RevertExecutionResult;
  }
}

// 导出单例
export const revertService = new RevertService();

// 导出类型
export type { CreateRevertRequest, RevertExecutionResult, RevertRequestParams };
