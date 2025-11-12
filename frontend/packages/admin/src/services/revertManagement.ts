/**
 * 撤销管理服务（管理员专属）
 * @author BaSui 😎
 * @description 直接调用后端API（手动实现）
 */

import { apiClient } from '@campus/shared/utils/apiClient';

/**
 * 分页结果接口
 */
interface PageRevertRequest {
  content: any[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

/**
 * 统计数据接口
 */
interface RevertStatistics {
  pendingCount: number;
  todayRevertCount: number;
  successRate: number;
}

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
    try {
      const params: any = { page, size };
      if (status) {
        params.status = status;
      }
      const response = await apiClient.get('/revert/admin/requests', { params });
      return response.data.data as PageRevertRequest;
    } catch (error) {
      console.error('❌ 获取撤销请求列表失败:', error);
      return {
        content: [],
        totalElements: 0,
        totalPages: 0,
        number: 0,
        size: size,
      };
    }
  }

  /**
   * 获取撤销统计数据
   */
  async getStatistics(): Promise<RevertStatistics> {
    try {
      const response = await apiClient.get('/revert/admin/statistics');
      return response.data.data as RevertStatistics;
    } catch (error) {
      console.error('❌ 获取撤销统计数据失败:', error);
      return {
        pendingCount: 0,
        todayRevertCount: 0,
        successRate: 0,
      };
    }
  }

  /**
   * 批准撤销请求
   */
  async approve(id: number, comment?: string): Promise<void> {
    try {
      const params: any = {};
      if (comment) {
        params.comment = comment;
      }
      await apiClient.post(`/revert/admin/${id}/approve`, null, { params });
    } catch (error) {
      console.error('❌ 批准撤销请求失败:', error);
      throw error;
    }
  }

  /**
   * 拒绝撤销请求
   */
  async reject(id: number, reason: string): Promise<void> {
    try {
      await apiClient.post(`/revert/admin/${id}/reject`, null, { 
        params: { reason }
      });
    } catch (error) {
      console.error('❌ 拒绝撤销请求失败:', error);
      throw error;
    }
  }
}

export const revertManagementService = new RevertManagementService();
export default revertManagementService;
