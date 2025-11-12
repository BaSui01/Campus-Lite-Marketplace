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
 * 退款 API 服务
 * @author BaSui 😎
 * @description 退款申请、审核、查询等接口
 */

import { getApi } from '../utils/apiClient';
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
   * 获取我的退款列表（用户视角）
   * 💡 BaSui：调用后端 GET /refunds 接口（用户查询自己的退款列表）
   * @param params 查询参数
   * @returns 退款列表（分页）
   */
  async listMyRefunds(params?: RefundListParams): Promise<ApiResponse<PageInfo<Refund>>> {
    const api = getApi();
    // ✅ 构造完整的 RefundFilterRequest 对象
    const filterRequest = {
      page: params?.page ?? 0,
      size: params?.size ?? 20,
      status: params?.status as any,
      keyword: params?.keyword,
      startTime: params?.startDate,
      endTime: params?.endDate,
    };
    const response = await api.listMyRefunds({ filterRequest });
    return response.data as ApiResponse<PageInfo<Refund>>;
  }

  /**
   * 获取退款列表（管理员视角）
   * @param params 查询参数
   * @returns 退款列表
   */
  async listRefunds(params?: RefundListParams): Promise<ApiResponse<PageInfo<Refund>>> {
    const api = getApi();
    // ✅ 构造完整的 RefundFilterRequest 对象
    const filterRequest = {
      page: params?.page ?? 0,
      size: params?.size ?? 20,
      status: params?.status as any,
      keyword: params?.keyword,
      startTime: params?.startDate,
      endTime: params?.endDate,
    };
    const response = await api.listAllRefunds({ filterRequest });
    return response.data as ApiResponse<PageInfo<Refund>>;
  }

  /**
   * 获取我的退款详情（用户视角）
   * @param refundNo 退款单号
   * @returns 退款详情
   */
  async getMyRefundDetail(refundNo: string): Promise<ApiResponse<Refund>> {
    const api = getApi();
    const response = await api.getMyRefund({ refundNo });
    return response.data as ApiResponse<Refund>;
  }

  /**
   * 审批通过退款（管理员）
   * @param refundNo 退款单号
   * @returns 操作结果
   */
  async approveRefund(refundNo: string): Promise<ApiResponse<void>> {
    const api = getApi();
    const response = await api.approve({ refundNo });
    return response.data as ApiResponse<void>;
  }

  /**
   * 驳回退款（管理员）
   * @param refundNo 退款单号
   * @param reason 驳回原因
   * @returns 操作结果
   */
  async rejectRefund(refundNo: string, reason: string): Promise<ApiResponse<void>> {
    const api = getApi();
    const response = await api.reject({ refundNo, reason });
    return response.data as ApiResponse<void>;
  }
}

// 导出单例
export const refundService = new RefundService();
export default refundService;
