/**
 * 导出 API 服务
 * @author BaSui 😎
 * @description 导出任务管理、导出历史、导出下载等接口
 */

// import { getApi } from '../utils/apiClient'; // TODO: 等待后端API实现后取消注释

// ==================== 类型定义 ====================

/**
 * 导出任务
 */
export interface ExportJob {
  /**
   * 任务ID
   */
  id: number;

  /**
   * 导出类型
   */
  type: string;

  /**
   * 导出参数（JSON）
   */
  params?: string;

  /**
   * 任务状态
   * - PENDING: 待处理
   * - PROCESSING: 处理中
   * - COMPLETED: 已完成
   * - FAILED: 失败
   * - CANCELLED: 已取消
   */
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';

  /**
   * 文件名
   */
  fileName?: string;

  /**
   * 文件大小（字节）
   */
  fileSize?: number;

  /**
   * 下载令牌
   */
  downloadToken?: string;

  /**
   * 过期时间
   */
  expiredAt?: string;

  /**
   * 错误信息
   */
  errorMessage?: string;

  /**
   * 创建时间
   */
  createdAt: string;

  /**
   * 完成时间
   */
  completedAt?: string;
}

/**
 * 导出类型
 */
export enum ExportType {
  ORDERS = 'orders',
  USERS = 'users',
  GOODS = 'goods',
  REVIEWS = 'reviews',
  DISPUTES = 'disputes',
  REFUNDS = 'refunds',
}

/**
 * 导出请求参数
 */
export interface ExportRequest {
  /**
   * 导出类型
   */
  type: ExportType | string;

  /**
   * 导出参数（JSON字符串）
   */
  params?: string;
}

// ==================== 服务接口 ====================

/**
 * 导出服务接口
 */
export interface ExportService {
  /**
   * 申请导出
   * @param request 导出请求参数
   * @returns 任务ID
   */
  requestExport(request: ExportRequest): Promise<number>;

  /**
   * 查询我的导出任务列表
   * @returns 导出任务列表
   */
  listMyExports(): Promise<ExportJob[]>;

  /**
   * 取消导出任务
   * @param id 任务ID
   */
  cancelExport(id: number): Promise<void>;

  /**
   * 下载导出文件
   * @param token 下载令牌
   * @returns 文件URL
   */
  downloadExport(token: string): string;
}

// ==================== 服务实现 ====================

/**
 * 导出服务实现类
 * ⚠️ 基于 DefaultApi 的完整实现
 * ⚠️ 注意：导出相关API暂未在后端实现，以下方法暂时不可用
 */
class ExportServiceImpl implements ExportService {
  /**
   * 申请导出
   * TODO: 等待后端实现 requestExport API
   */
  async requestExport(_request: ExportRequest): Promise<number> {
    // const api = getApi();
    // const response = await api.requestExport({
    //   type: request.type,
    //   params: request.params
    // });
    // return response.data.data as number;

    // 临时实现：返回模拟ID
    throw new Error('导出功能暂未实现，请等待后端API开发');
  }

  /**
   * 查询我的导出任务列表
   * TODO: 等待后端实现 listExports API
   */
  async listMyExports(): Promise<ExportJob[]> {
    // const api = getApi();
    // const response = await api.listExports();
    // return response.data.data as ExportJob[];

    // 临时实现：返回空列表
    return [];
  }

  /**
   * 取消导出任务
   * TODO: 等待后端实现 cancelExport API
   */
  async cancelExport(_id: number): Promise<void> {
    // const api = getApi();
    // await api.cancelExport({ id });

    // 临时实现：什么都不做
    console.warn('取消导出功能暂未实现');
  }

  /**
   * 下载导出文件
   * 返回下载URL（前端直接window.open或a标签下载）
   */
  downloadExport(token: string): string {
    // 返回完整的下载URL
    return `/api/exports/download/${token}`;
  }
}

/**
 * 导出服务实例
 */
export const exportService = new ExportServiceImpl();
