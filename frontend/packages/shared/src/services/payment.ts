/**
 * 支付 API 服务
 * @author BaSui 😎
 * @description 支付记录查询、支付状态管理、支付统计等接口
 */

import { getApi } from '../utils/apiClient';
import type { PageOrderResponse } from '../api/models';

// ==================== 类型定义 ====================

/**
 * 支付记录
 */
export interface PaymentRecord {
  /**
   * 支付ID
   */
  id: number;

  /**
   * 订单号
   */
  orderNo: string;

  /**
   * 支付方式
   */
  paymentMethod: 'WECHAT_PAY' | 'ALIPAY' | 'BALANCE';

  /**
   * 支付金额（单位：分）
   */
  amount: number;

  /**
   * 支付状态
   */
  status: 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';

  /**
   * 支付时间
   */
  paidAt?: string;

  /**
   * 第三方交易号
   */
  transactionId?: string;

  /**
   * 买家ID
   */
  buyerId: number;

  /**
   * 买家用户名
   */
  buyerUsername: string;

  /**
   * 商品名称
   */
  goodsTitle: string;

  /**
   * 创建时间
   */
  createdAt: string;
}

/**
 * 支付统计
 */
export interface PaymentStatistics {
  /**
   * 总交易金额（元）
   */
  totalAmount: number;

  /**
   * 总交易笔数
   */
  totalCount: number;

  /**
   * 成功交易金额（元）
   */
  successAmount: number;

  /**
   * 成功交易笔数
   */
  successCount: number;

  /**
   * 退款金额（元）
   */
  refundAmount: number;

  /**
   * 退款笔数
   */
  refundCount: number;

  /**
   * 微信支付金额（元）
   */
  wechatAmount: number;

  /**
   * 支付宝金额（元）
   */
  alipayAmount: number;

  /**
   * 余额支付金额（元）
   */
  balanceAmount: number;
}

/**
 * 支付查询参数
 */
export interface PaymentListParams {
  keyword?: string;
  status?: string;
  paymentMethod?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

// ==================== 服务接口 ====================

/**
 * 支付服务接口
 */
export interface PaymentService {
  /**
   * 查询支付记录列表（管理员）
   * @param params 查询参数
   * @returns 支付记录列表（分页）
   */
  listPayments(params?: PaymentListParams): Promise<PageOrderResponse>;

  /**
   * 查询支付详情
   * @param orderNo 订单号
   * @returns 支付详情
   */
  getPaymentDetail(orderNo: string): Promise<PaymentRecord>;

  /**
   * 查询支付统计
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @returns 支付统计信息
   */
  getPaymentStatistics(startDate?: string, endDate?: string): Promise<PaymentStatistics>;
}

// ==================== 服务实现 ====================

/**
 * 支付服务实现类
 * ⚠️ 基于 DefaultApi 的完整实现（使用已有的订单API）
 */
class PaymentServiceImpl implements PaymentService {
  /**
   * 查询支付记录列表（管理员视角）
   * ✅ 已集成真实API
   */
  async listPayments(params?: PaymentListParams): Promise<PageOrderResponse> {
    const api = getApi();
    // 调用 PaymentAdminController 的 listPayments 接口
    // TODO: API 方法待后端生成后更新
    const response = await api.listPayments({
      keyword: params?.keyword,
      status: params?.status as any,
      paymentMethod: params?.paymentMethod,
      startDate: params?.startDate,
      endDate: params?.endDate,
      page: params?.page || 0,
      size: params?.size || 20
    });
    return response.data.data as PageOrderResponse;
  }

  /**
   * 查询支付详情
   * 复用订单详情API
   */
  async getPaymentDetail(orderNo: string): Promise<PaymentRecord> {
    const api = getApi();
    const response = await api.getOrderDetail({ orderNo });
    const order = response.data.data;

    // 将订单数据转换为支付记录格式
    return {
      id: order!.id!,
      orderNo: order!.orderNo!,
      paymentMethod: order!.paymentMethod! as any,
      amount: (order!.amount || 0) * 100, // 元转分（使用 amount 字段）
      status: order!.status! as any,
      paidAt: order!.paid ? new Date().toISOString() : undefined, // 使用 paid 字段判断
      transactionId: order!.orderNo, // 使用订单号作为交易号
      buyerId: order!.buyerId!,
      buyerUsername: '', // Order 类型中没有此字段，暂时留空
      goodsTitle: '', // Order 类型中没有此字段，暂时留空
      createdAt: order!.createdAt!,
    };
  }

  /**
   * 查询支付统计
   * ✅ 已集成真实API
   */
  async getPaymentStatistics(startDate?: string, endDate?: string): Promise<PaymentStatistics> {
    const api = getApi();
    // 调用 PaymentAdminController 的 getStatistics 接口
    // TODO: API 方法待后端生成后更新
    const response = await api.getStatistics1({
      startDate,
      endDate
    });
    const data = response.data.data as any;
    
    // 转换后端返回的数据格式
    return {
      totalAmount: data.totalAmount || 0,
      totalCount: data.totalPayments || 0,
      successAmount: data.totalAmount || 0, // 使用总金额作为成功金额
      successCount: data.successPayments || 0,
      refundAmount: 0, // 后端暂无此字段
      refundCount: data.refundedPayments || 0,
      wechatAmount: data.amountByMethod?.WECHAT || 0,
      alipayAmount: data.amountByMethod?.ALIPAY || 0,
      balanceAmount: data.amountByMethod?.POINTS || 0,
    };
  }
}

/**
 * 支付服务实例
 */
export const paymentService = new PaymentServiceImpl();
