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
 * 纠纷仲裁 API 服务
 * @author BaSui 😎
 * @description 纠纷提交、仲裁、查询等接口
 */

import { getApi } from '../utils/apiClient';
import type { ApiResponse, PageInfo } from '../types';

/**
 * 纠纷状态枚举
 */
export enum DisputeStatus {
  PENDING = 'PENDING', // 待处理
  INVESTIGATING = 'INVESTIGATING', // 调查中
  ARBITRATING = 'ARBITRATING', // 仲裁中
  RESOLVED = 'RESOLVED', // 已解决
  REJECTED = 'REJECTED', // 已驳回
  CLOSED = 'CLOSED', // 已关闭
}

/**
 * 纠纷类型枚举
 */
export enum DisputeType {
  GOODS_QUALITY = 'GOODS_QUALITY', // 商品质量
  GOODS_DESCRIPTION = 'GOODS_DESCRIPTION', // 商品描述不符
  DELIVERY_ISSUE = 'DELIVERY_ISSUE', // 物流问题
  REFUND_ISSUE = 'REFUND_ISSUE', // 退款问题
  SERVICE_ATTITUDE = 'SERVICE_ATTITUDE', // 服务态度
  OTHER = 'OTHER', // 其他
}

/**
 * 纠纷信息接口
 */
export interface Dispute {
  id: number;
  disputeNo: string;
  orderNo: string;
  type: DisputeType;
  status: DisputeStatus;
  title: string;
  description: string;
  amount: number; // 涉及金额
  plaintiffId: number; // 申诉方ID
  plaintiffName: string;
  plaintiffAvatar?: string;
  defendantId: number; // 被诉方ID
  defendantName: string;
  defendantAvatar?: string;
  arbitratorId?: number; // 仲裁员ID
  arbitratorName?: string;
  createdAt: string;
  updatedAt: string;
  closedAt?: string;
}

/**
 * 纠纷详情
 */
export interface DisputeDetail extends Dispute {
  orderInfo: {
    orderNo: string;
    goodsId: number;
    goodsTitle: string;
    goodsImage: string;
    totalAmount: number;
    status: string;
    buyerId: number;
    buyerName: string;
    sellerId: number;
    sellerName: string;
  };
  evidenceMaterials: DisputeEvidence[]; // 证据材料
  arbitrationHistory: ArbitrationRecord[]; // 仲裁历史
  chatMessages?: DisputeMessage[]; // 协商消息（可选）
}

/**
 * 纠纷证据材料
 */
export interface DisputeEvidence {
  id: number;
  disputeId: number;
  uploaderId: number; // 上传者ID (plaintiff/defendant)
  uploaderName: string;
  type: 'IMAGE' | 'FILE';
  url: string;
  fileName?: string;
  fileSize?: number;
  description?: string;
  createdAt: string;
}

/**
 * 仲裁记录
 */
export interface ArbitrationRecord {
  id: number;
  disputeId: number;
  arbitratorId: number;
  arbitratorName: string;
  action: 'ACCEPT' | 'INVESTIGATE' | 'RESOLVE' | 'REJECT' | 'CLOSE'; // 操作类型
  decision?: string; // 仲裁决定
  compensationAmount?: number; // 赔偿金额
  reason: string; // 操作理由
  createdAt: string;
}

/**
 * 纠纷协商消息
 */
export interface DisputeMessage {
  id: number;
  disputeId: number;
  senderId: number;
  senderName: string;
  senderType: 'PLAINTIFF' | 'DEFENDANT' | 'ARBITRATOR';
  content: string;
  createdAt: string;
}

/**
 * 纠纷列表查询参数
 */
export interface DisputeListParams {
  keyword?: string; // 搜索关键字（纠纷编号、订单号）
  type?: DisputeType;
  status?: DisputeStatus;
  arbitratorId?: number;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

/**
 * 纠纷仲裁请求
 */
export interface DisputeArbitrationRequest {
  disputeId: number;
  action: 'ACCEPT' | 'INVESTIGATE' | 'RESOLVE' | 'REJECT' | 'CLOSE';
  decision?: string; // 仲裁决定（RESOLVE 时必填）
  compensationAmount?: number; // 赔偿金额（RESOLVE 时可选）
  reason: string; // 操作理由（必填）
}

/**
 * 提交纠纷证据请求
 */
export interface SubmitEvidenceRequest {
  disputeId: number;
  type: 'IMAGE' | 'FILE';
  url: string;
  fileName?: string;
  fileSize?: number;
  description?: string;
}

/**
 * 纠纷 API 服务类
 */
export class DisputeService {
  /**
   * 获取纠纷列表（管理员视角）
   */
  async listDisputes(params?: DisputeListParams): Promise<ApiResponse<PageInfo<Dispute>>> {
    return http.get('/admin/disputes', { params });
  }

  /**
   * 获取待处理纠纷列表
   */
  async listPendingDisputes(params?: {
    page?: number;
    size?: number;
  }): Promise<ApiResponse<PageInfo<Dispute>>> {
    return http.get('/admin/disputes/pending', { params });
  }

  /**
   * 获取我的仲裁纠纷列表（当前仲裁员）
   */
  async listMyDisputes(params?: {
    status?: DisputeStatus;
    page?: number;
    size?: number;
  }): Promise<ApiResponse<PageInfo<Dispute>>> {
    return http.get('/admin/disputes/my', { params });
  }

  /**
   * 获取纠纷详情
   */
  async getDisputeDetail(id: number): Promise<ApiResponse<DisputeDetail>> {
    return http.get(`/admin/disputes/${id}`);
  }

  /**
   * 认领纠纷（仲裁员接受处理）
   */
  async claimDispute(id: number): Promise<ApiResponse<void>> {
    return http.post(`/admin/disputes/${id}/claim`);
  }

  /**
   * 仲裁纠纷（处理纠纷）
   */
  async arbitrateDispute(
    request: DisputeArbitrationRequest
  ): Promise<ApiResponse<void>> {
    const { disputeId, ...body } = request;
    return http.post(`/admin/disputes/${disputeId}/arbitrate`, body);
  }

  /**
   * 提交证据材料
   */
  async submitEvidence(request: SubmitEvidenceRequest): Promise<ApiResponse<void>> {
    const { disputeId, ...body } = request;
    return http.post(`/admin/disputes/${disputeId}/evidence`, body);
  }

  /**
   * 发送协商消息
   */
  async sendMessage(disputeId: number, content: string): Promise<ApiResponse<void>> {
    return http.post(`/admin/disputes/${disputeId}/messages`, { content });
  }

  /**
   * 关闭纠纷
   */
  async closeDispute(id: number, reason: string): Promise<ApiResponse<void>> {
    return http.post(`/admin/disputes/${id}/close`, { reason });
  }

  /**
   * 批量分配仲裁员
   */
  async batchAssignArbitrator(
    disputeIds: number[],
    arbitratorId: number
  ): Promise<ApiResponse<void>> {
    return http.post('/admin/disputes/batch-assign', { disputeIds, arbitratorId });
  }
}

/**
 * 导出服务实例
 */
export const disputeService = new DisputeService();
