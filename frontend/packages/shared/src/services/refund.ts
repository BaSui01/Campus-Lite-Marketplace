/**
 * 退款 API 服务
 * @author BaSui 😎
 * @description 退款申请、查询、审批等接口（咱可是专业的！）
 */

import { http } from '../utils/http';
import type { ApiResponse, PageInfo } from '../types';

/**
 * 退款状态枚举（和后端保持一致！）
 */
export enum RefundStatus {
  APPLIED = 'APPLIED',       // 已申请
  APPROVED = 'APPROVED',     // 已审核通过
  REJECTED = 'REJECTED',     // 审核拒绝
  PROCESSING = 'PROCESSING', // 渠道退款中
  REFUNDED = 'REFUNDED',     // 退款成功
  FAILED = 'FAILED',         // 退款失败
}

/**
 * 退款请求实体（和后端 RefundRequest 对应）
 */
export interface RefundRequest {
  id?: number;
  refundNo: string;          // 退款单号
  orderNo: string;           // 订单号
  applicantId: number;       // 申请人ID
  reason: string;            // 退款原因
  evidence?: Record<string, any>; // 退款凭证（JSON）
  status: RefundStatus;      // 退款状态
  channel?: string;          // 支付渠道（ALIPAY/WECHAT）
  amount: number;            // 退款金额
  retryCount?: number;       // 重试次数
  lastError?: string;        // 最后错误信息
  createdAt?: string;        // 创建时间
  updatedAt?: string;        // 更新时间
}

/**
 * 申请退款请求参数
 */
export interface ApplyRefundRequest {
  orderNo: string;
  reason: string;
  evidence?: {
    images?: string[];       // 图片凭证
    note?: string;           // 文字说明
    [key: string]: any;      // 其他凭证
  };
}

/**
 * 退款列表查询参数
 */
export interface RefundListQuery {
  page?: number;             // 页码（从0开始）
  size?: number;             // 每页大小
  status?: RefundStatus;     // 退款状态筛选
  keyword?: string;          // 搜索关键词（管理员用）
}

/**
 * 退款 API 服务类（退款功能全靠它了！😎）
 */
export class RefundService {
  // ==================== 用户端接口 ====================

  /**
   * 申请退款（用户提交退款申请）
   * @param data 退款申请参数
   * @returns 退款单号
   */
  async applyRefund(data: ApplyRefundRequest): Promise<ApiResponse<string>> {
    const { orderNo, reason, evidence } = data;
    return http.post(`/orders/${orderNo}/refunds`, evidence || {}, {
      params: { reason },
    });
  }

  /**
   * 查询我的退款列表（用户查看自己的退款）
   * @param params 查询参数
   * @returns 分页退款列表
   */
  async listMyRefunds(params?: RefundListQuery): Promise<ApiResponse<PageInfo<RefundRequest>>> {
    return http.get('/refunds', { params });
  }

  /**
   * 查询我的退款详情（用户查看自己的退款详情）
   * @param refundNo 退款单号
   * @returns 退款详情
   */
  async getMyRefund(refundNo: string): Promise<ApiResponse<RefundRequest>> {
    return http.get(`/refunds/${refundNo}`);
  }

  // ==================== 管理员接口 ====================

  /**
   * 管理员查询所有退款列表（支持筛选和搜索）
   * @param params 查询参数
   * @returns 分页退款列表
   */
  async listAllRefunds(params?: RefundListQuery): Promise<ApiResponse<PageInfo<RefundRequest>>> {
    return http.get('/admin/refunds', { params });
  }

  /**
   * 管理员查询退款详情
   * @param refundNo 退款单号
   * @returns 退款详情
   */
  async getRefundDetail(refundNo: string): Promise<ApiResponse<RefundRequest>> {
    return http.get(`/admin/refunds/${refundNo}`);
  }

  /**
   * 管理员审批通过并退款
   * @param refundNo 退款单号
   * @returns 审批结果
   */
  async approveRefund(refundNo: string): Promise<ApiResponse<void>> {
    return http.put(`/admin/refunds/${refundNo}/approve`);
  }

  /**
   * 管理员驳回退款申请
   * @param refundNo 退款单号
   * @param reason 驳回原因
   * @returns 驳回结果
   */
  async rejectRefund(refundNo: string, reason: string): Promise<ApiResponse<void>> {
    return http.put(`/admin/refunds/${refundNo}/reject`, null, {
      params: { reason },
    });
  }
}

// 导出单例（全局共享，省内存！🎉）
export const refundService = new RefundService();
export default refundService;
