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
 * 申诉 API 服务
 * @author BaSui 😎
 * @description 申诉提交、审核、查询等接口
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { ApiResponse, PageInfo } from '@campus/shared/types';

/**
 * 申诉信息接口
 */
export interface Appeal {
  id: number;
  appealNo: string;
  type: string; // ACCOUNT_BAN, GOODS_REJECTION, ORDER_DISPUTE, OTHER
  status: string; // PENDING, REVIEWING, APPROVED, REJECTED, EXPIRED
  userId: number;
  userName: string;
  userAvatar?: string;
  userPhone?: string;
  title: string;
  description: string;
  relatedId?: number; // 关联ID（商品ID、订单ID等）
  relatedInfo?: string; // 关联信息
  materialsCount: number;
  createdAt: string;
  updatedAt: string;
  expireAt?: string;
  reviewedAt?: string;
  reviewerId?: number;
  reviewerName?: string;
  reviewReason?: string;
}

/**
 * 申诉详情
 */
export interface AppealDetail extends Appeal {
  materials: AppealMaterial[];
  banRecord?: {
    reason: string;
    duration: number;
    bannedAt: string;
    operatorName: string;
  };
  goodsInfo?: {
    id: number;
    title: string;
    image: string;
    status: string;
    rejectionReason?: string;
  };
  orderInfo?: {
    orderNo: string;
    goodsTitle: string;
    totalAmount: number;
    status: string;
  };
  appealHistory?: {
    id: number;
    reviewerName: string;
    approved: boolean;
    reason: string;
    createdAt: string;
  }[];
}

/**
 * 申诉材料
 */
export interface AppealMaterial {
  id: number;
  appealId: number;
  type: string; // IMAGE, FILE
  url: string;
  fileName?: string;
  fileSize?: number;
  createdAt: string;
}

/**
 * 申诉列表查询参数
 */
export interface AppealListParams {
  keyword?: string;
  type?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

/**
 * 申诉审核请求
 */
export interface AppealReviewRequest {
  appealId: number;
  approved: boolean;
  reason: string;
}

/**
 * 批量审核请求
 */
export interface BatchAppealReviewRequest {
  appealIds: number[];
  approved: boolean;
  reason: string;
}

/**
 * 申诉统计
 */
export interface AppealStatistics {
  total: number;
  pending: number;
  reviewing: number;
  approved: number;
  rejected: number;
  expired: number;
}

/**
 * 申诉 API 服务类
 */
export class AppealService {
  /**
   * 获取申诉列表（管理员视角）
   * @param params 查询参数
   * @returns 申诉列表
   */
  async listAppeals(params?: AppealListParams): Promise<ApiResponse<PageInfo<Appeal>>> {
    return http.get('/admin/appeals', { params });
  }

  /**
   * 获取待审核申诉列表
   * @param params 查询参数
   * @returns 待审核申诉列表
   */
  async listPendingAppeals(params?: {
    page?: number;
    size?: number;
  }): Promise<ApiResponse<PageInfo<Appeal>>> {
    return http.get('/admin/appeals/pending', { params });
  }

  /**
   * 获取申诉详情
   * @param appealId 申诉ID
   * @returns 申诉详情
   */
  async getAppealDetail(appealId: number): Promise<ApiResponse<AppealDetail>> {
    return http.get(`/admin/appeals/${appealId}`);
  }

  /**
   * 审核申诉（管理员）
   * @param request 审核请求
   * @returns 操作结果
   */
  async reviewAppeal(request: AppealReviewRequest): Promise<ApiResponse<Appeal>> {
    return http.post(`/admin/appeals/${request.appealId}/review`, {
      approved: request.approved,
      reason: request.reason,
    });
  }

  /**
   * 批量审核申诉（管理员）
   * @param request 批量审核请求
   * @returns 操作结果
   */
  async batchReviewAppeals(request: BatchAppealReviewRequest): Promise<ApiResponse<{
    successCount: number;
    failureCount: number;
    failedAppeals?: { appealId: number; reason: string }[];
  }>> {
    return http.post('/admin/appeals/batch-review', request);
  }

  /**
   * 获取申诉统计（管理员）
   * @returns 统计数据
   */
  async getAppealStatistics(): Promise<ApiResponse<AppealStatistics>> {
    return http.get('/admin/appeals/statistics');
  }

  /**
   * 获取申诉材料
   * @param appealId 申诉ID
   * @returns 申诉材料列表
   */
  async getAppealMaterials(appealId: number): Promise<ApiResponse<AppealMaterial[]>> {
    return http.get(`/appeals/materials/${appealId}`);
  }
}

// 导出单例
export const appealService = new AppealService();
export default appealService;
