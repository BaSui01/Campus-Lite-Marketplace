/**
 * 批量任务 API 服务
 * @author BaSui 😎
 * @description 批量任务相关接口（基于 OpenAPI 生成代码）
 */

import { getApi } from '../utils/apiClient';
import type {
  BatchTaskResponse,
  BatchTaskProgressResponse,
  CreateBatchTaskRequest,
} from '../api/models';

/**
 * 批量任务状态枚举
 */
export enum BatchTaskStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
}

/**
 * 批量任务类型枚举
 */
export enum BatchType {
  GOODS_BATCH = 'GOODS_BATCH',
  PRICE_BATCH = 'PRICE_BATCH',
  INVENTORY_BATCH = 'INVENTORY_BATCH',
  NOTIFICATION_BATCH = 'NOTIFICATION_BATCH',
}

/**
 * 批量任务列表查询参数
 */
export interface BatchTaskListParams {
  status?: BatchTaskStatus;
  page?: number;
  size?: number;
}

/**
 * 分页响应
 */
export interface PageBatchTaskResponse {
  content: BatchTaskResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/**
 * 批量任务 API 服务类
 */
export class BatchService {
  /**
   * 获取批量任务列表（分页）
   * @param params 查询参数
   * @returns 批量任务列表（分页）
   */
  async listBatchTasks(params?: BatchTaskListParams): Promise<PageBatchTaskResponse> {
    const api = getApi();
    const response = await api.getBatchTasks({
      status: params?.status as any,
      page: params?.page,
      size: params?.size,
    });
    return response.data.data as PageBatchTaskResponse;
  }

  /**
   * 获取批量任务详情
   * @param taskId 任务 ID
   * @returns 批量任务详情
   */
  async getBatchTaskDetail(taskId: number): Promise<BatchTaskResponse> {
    const api = getApi();
    const response = await api.getBatchTaskDetail({ taskId });
    return response.data.data as BatchTaskResponse;
  }

  /**
   * 获取批量任务进度
   * @param taskId 任务 ID
   * @returns 批量任务进度
   */
  async getTaskProgress(taskId: number): Promise<BatchTaskProgressResponse> {
    const api = getApi();
    const response = await api.getTaskProgress({ taskId });
    return response.data.data as BatchTaskProgressResponse;
  }

  /**
   * 创建批量任务
   * @param data 批量任务信息
   * @returns 创建的批量任务 ID
   */
  async createBatchTask(data: CreateBatchTaskRequest): Promise<number> {
    const api = getApi();
    const response = await api.createBatchTask({ createBatchTaskRequest: data });
    return response.data.data as number;
  }

  /**
   * 取消批量任务
   * @param taskId 任务 ID
   */
  async cancelBatchTask(taskId: number): Promise<void> {
    const api = getApi();
    await api.cancelBatchTask({ taskId });
  }
}

// 导出单例
export const batchService = new BatchService();
export default batchService;
