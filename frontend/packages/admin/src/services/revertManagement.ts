/**
 * 撤销管理服务（管理员专属）
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { PageRevertRequest, RevertStatistics } from '@campus/shared/api';

/**
 * 撤销管理服务类
 */
export class RevertManagementService {
  /**
   * 获取撤销请求列表
   */
  async listRequests(
    status?: string,
    page: number = 0,
    size: number = 10
  ): Promise<PageRevertRequest> {
    const api = getApi();
    const response = await api.listRevertRequests({ status: status as any, page, size });
    return response.data.data as PageRevertRequest;
  }

  /**
   * 获取撤销统计数据
   */
  async getStatistics(): Promise<RevertStatistics> {
    const api = getApi();
    const response = await api.getRevertStatistics();
    return response.data.data as RevertStatistics;
  }

  /**
   * 批准撤销请求
   */
  async approve(id: number, comment?: string): Promise<void> {
    const api = getApi();
    await api.approveRevert({ id, comment });
  }

  /**
   * 拒绝撤销请求
   */
  async reject(id: number, reason: string): Promise<void> {
    const api = getApi();
    await api.rejectRevert({ id, reason });
  }
}

export const revertManagementService = new RevertManagementService();
export default revertManagementService;
