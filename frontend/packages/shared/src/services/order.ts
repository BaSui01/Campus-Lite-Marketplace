/**
 * 订单 API 服务
 * @author BaSui 😎
 * @description 订单创建、支付、取消、确认收货、评价等接口
 */

import { http } from '../utils/http';
import type {
  ApiResponse,
  PageInfo,
  Order,
  Review,
  CreateOrderRequest,
  CreateOrderResponse,
  OrderListQuery,
  PayOrderRequest,
  PayOrderResponse,
  CancelOrderRequest,
  ConfirmReceiptRequest,
  RequestRefundRequest,
  CreateReviewRequest,
} from '../types';

/**
 * 订单 API 服务类
 */
export class OrderService {
  // ==================== 订单相关接口 ====================

  /**
   * 创建订单
   * @param data 创建订单请求参数
   * @returns 订单信息（包含订单号和金额）
   */
  async createOrder(data: CreateOrderRequest): Promise<ApiResponse<CreateOrderResponse>> {
    return http.post('/orders', data);
  }

  /**
   * 获取订单详情
   * @param orderNo 订单号
   * @returns 订单详情
   */
  async getOrderByNo(orderNo: string): Promise<ApiResponse<Order>> {
    return http.get(`/orders/${orderNo}`);
  }

  /**
   * 获取买家订单列表
   * @param params 查询参数
   * @returns 买家订单列表
   */
  async getBuyerOrders(params?: OrderListQuery): Promise<ApiResponse<PageInfo<Order>>> {
    return http.get('/orders/buyer', { params });
  }

  /**
   * 获取卖家订单列表
   * @param params 查询参数
   * @returns 卖家订单列表
   */
  async getSellerOrders(params?: OrderListQuery): Promise<ApiResponse<PageInfo<Order>>> {
    return http.get('/orders/seller', { params });
  }

  /**
   * 支付订单
   * @param data 支付请求参数
   * @returns 支付响应（包含支付跳转URL或二维码）
   */
  async payOrder(data: PayOrderRequest): Promise<ApiResponse<PayOrderResponse>> {
    return http.post('/payment/create', data);
  }

  /**
   * 查询订单支付状态
   * @param orderNo 订单号
   * @returns 支付状态
   */
  async getPaymentStatus(orderNo: string): Promise<ApiResponse<{ status: string }>> {
    return http.get(`/payment/status/${orderNo}`);
  }

  /**
   * 取消订单
   * @param data 取消订单请求参数
   * @returns 取消结果
   */
  async cancelOrder(data: CancelOrderRequest): Promise<ApiResponse<void>> {
    return http.post(`/orders/${data.orderNo}/cancel`, {
      reason: data.reason,
    });
  }

  /**
   * 确认收货
   * @param data 确认收货请求参数
   * @returns 确认结果
   */
  async confirmReceipt(data: ConfirmReceiptRequest): Promise<ApiResponse<void>> {
    return http.post(`/orders/${data.orderNo}/confirm`);
  }

  /**
   * 申请退款
   * @param data 申请退款请求参数
   * @returns 申请结果
   */
  async requestRefund(data: RequestRefundRequest): Promise<ApiResponse<void>> {
    return http.post(`/orders/${data.orderNo}/refund`, {
      reason: data.reason,
      amount: data.amount,
    });
  }

  // ==================== 评价相关接口 ====================

  /**
   * 创建订单评价
   * @param data 评价请求参数
   * @returns 评价信息
   */
  async createReview(data: CreateReviewRequest): Promise<ApiResponse<Review>> {
    return http.post('/reviews', data);
  }

  /**
   * 获取订单评价
   * @param orderId 订单ID
   * @returns 评价信息
   */
  async getReviewByOrderId(orderId: number): Promise<ApiResponse<Review>> {
    return http.get(`/reviews/order/${orderId}`);
  }

  /**
   * 获取用户评价列表
   * @param userId 用户ID
   * @param params 查询参数
   * @returns 评价列表
   */
  async getUserReviews(userId: number, params?: { page?: number; pageSize?: number }): Promise<ApiResponse<PageInfo<Review>>> {
    return http.get(`/reviews/user/${userId}`, { params });
  }

  /**
   * 获取物品评价列表
   * @param goodsId 物品ID
   * @param params 查询参数
   * @returns 评价列表
   */
  async getGoodsReviews(goodsId: number, params?: { page?: number; pageSize?: number }): Promise<ApiResponse<PageInfo<Review>>> {
    return http.get(`/reviews/goods/${goodsId}`, { params });
  }

  // ==================== 管理员功能 ====================

  /**
   * 获取订单列表（管理员视角）
   * @param params 查询参数
   * @returns 订单列表
   */
  async listOrdersAdmin(params: {
    keyword?: string;
    status?: string;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
  }): Promise<ApiResponse<PageInfo<Order>>> {
    return http.get('/orders/admin/list', { params });
  }

  /**
   * 取消订单（管理员）
   * @param orderNo 订单号
   * @param reason 取消原因
   * @returns 操作结果
   */
  async cancelOrderAdmin(orderNo: string, reason: string): Promise<ApiResponse<void>> {
    return http.post(`/orders/${orderNo}/cancel/admin`, { reason });
  }

  /**
   * 强制完成订单（管理员）
   * @param orderNo 订单号
   * @returns 操作结果
   */
  async forceCompleteOrder(orderNo: string): Promise<ApiResponse<void>> {
    return http.post(`/orders/${orderNo}/force-complete`);
  }
}

// 导出单例

export const orderService = new OrderService();
export default orderService;
