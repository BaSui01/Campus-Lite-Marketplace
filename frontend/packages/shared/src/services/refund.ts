/**
 * 退款 API 服务
 * @author BaSui 😎
 * @description 退款申请、审核、查询等接口
 */

import { http } from '../utils/apiClient';
import type { ApiResponse, PageInfo } from '../types';

/**
 * 退款状态枚举
 */
export enum RefundStatus {
  /** 待审核 */
  PENDING = 'PENDING',
  /** 已批准 */
  APPROVED = 'APPROVED',
  /** 已拒绝 */
  REJECTED = 'REJECTED',
  /** 已取消 */
  CANCELLED = 'CANCELLED',
  /** 已完成 */
  COMPLETED = 'COMPLETED',
}

/**
 * 退款信息接口
 */
export interface Refund {
  id: number;
  refundNo: string;
  orderNo: string;
  goodsId: number;
  goodsTitle: string;
  goodsImage?: string;
  buyerId: number;
  buyerName: string;
  sellerId: number;
  sellerName: string;
  refundAmount: number;
  refundReason: string;
  refundProof?: string[];
  status: string;
  reviewerId?: number;
  reviewerName?: string;
  reviewReason?: string;
  createdAt: string;
  updatedAt: string;
  reviewedAt?: string;
}

/**
 * 退款列表查询参数
 */
export interface RefundListParams {
  keyword?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

/**
 * 退款审核请求
 */
export interface RefundReviewRequest {
  refundId: number;
  approved: boolean;
  reason?: string;
}

/**
 * 退款 API 服务类
 */
export class RefundService {
  /**
   * 获取退款列表（管理员视角）
   * @param params 查询参数
   * @returns 退款列表
   */
  async listRefunds(params?: RefundListParams): Promise<ApiResponse<PageInfo<Refund>>> {
    return http.get('/refunds', { params });
  }

  /**
   * 获取退款详情
   * @param refundId 退款ID
   * @returns 退款详情
   */
  async getRefundDetail(refundId: number): Promise<ApiResponse<Refund>> {
    return http.get(`/refunds/${refundId}`);
  }

  /**
   * 审核退款（管理员）
   * @param request 审核请求
   * @returns 操作结果
   */
  async reviewRefund(request: RefundReviewRequest): Promise<ApiResponse<void>> {
    return http.post(`/refunds/${request.refundId}/review`, {
      approved: request.approved,
      reason: request.reason,
    });
  }

  /**
   * 批量审核退款（管理员）
   * @param refundIds 退款ID列表
   * @param approved 是否批准
   * @param reason 审核原因
   * @returns 操作结果
   */
  async batchReviewRefunds(
    refundIds: number[],
    approved: boolean,
    reason?: string
  ): Promise<ApiResponse<{ successCount: number; failureCount: number }>> {
    return http.post('/refunds/batch-review', {
      refundIds,
      approved,
      reason,
    });
  }

  /**
   * 获取退款统计（管理员）
   * @returns 统计数据
   */
  async getRefundStatistics(): Promise<ApiResponse<{
    total: number;
    pending: number;
    approved: number;
    rejected: number;
  }>> {
    return http.get('/refunds/statistics');
  }
}

// 导出单例
export const refundService = new RefundService();
export default refundService;
