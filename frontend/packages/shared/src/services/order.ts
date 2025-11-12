/**
 * 订单 API 服务
 * @author BaSui 😎
 * @description 订单创建、支付、取消、物流等接口
 * ✅ 完全基于 OpenAPI 生成的 DefaultApi（自动处理 /api 前缀）
 * 🚫 禁止手写路径！必须使用 getApi() 获取 DefaultApi 实例！
 */

import { getApi } from '../utils/apiClient';
import type { CreateOrderRequest, PayOrderRequest, Order, PageOrderResponse, UpdateOrderDeliveryRequest, ShipOrderRequest } from '../api/models';

/**
 * 支付响应数据类型
 */
export interface PaymentResponseData {
  orderNo: string;           // 订单号
  paymentUrl: string;        // 支付链接或二维码
  qrCode?: string;           // 二维码（可选）
  expireSeconds?: number;    // 过期秒数（可选）
}

/**
 * 订单查询参数
 */
export interface OrderListParams {
  status?: string;  // 订单状态
  page?: number;    // 页码（从 0 开始）
  size?: number;    // 每页大小
}

/**
 * 订单 API 服务类
 * ✅ 完全基于 OpenAPI 生成的 DefaultApi，零手写路径！
 */
export class OrderService {
  // ==================== 订单相关接口 ====================

  /**
   * 创建订单
   * @param data 创建订单请求参数
   * @returns 订单号
   */
  async createOrder(data: CreateOrderRequest): Promise<string> {
    const api = getApi();
    const response = await api.createOrder({ createOrderRequest: data });
    return response.data.data as string;
  }

  /**
   * 更新订单配送/收货信息
   * @param orderNo 订单号
   * @param data 配送方式与收货信息
   */
  async updateOrderDelivery(orderNo: string, data: {
    deliveryMethod: 'FACE_TO_FACE' | 'EXPRESS',
    receiverName?: string,
    receiverPhone?: string,
    receiverAddress?: string,
    note?: string,
  }): Promise<void> {
    const api = getApi();
    await api.updateOrderDelivery({
      orderNo,
      updateOrderDeliveryRequest: data as UpdateOrderDeliveryRequest,
    });
  }

  /**
   * 获取订单详情
   * @param orderNo 订单号
   * @returns 订单详情
   */
  async getOrderDetail(orderNo: string): Promise<Order> {
    const api = getApi();
    const response = await api.getOrderDetail({ orderNo });
    return response.data.data as Order;
  }

  /**
   * 获取买家订单列表
   * @param params 查询参数
   * @returns 买家订单列表（分页）
   */
  async listBuyerOrders(params?: OrderListParams): Promise<PageOrderResponse> {
    const api = getApi();
    const response = await api.listBuyerOrders({
      status: params?.status,
      page: params?.page,
      size: params?.size
    });
    return response.data.data as PageOrderResponse;
  }

  /**
   * 获取卖家订单列表
   * @param params 查询参数
   * @returns 卖家订单列表（分页）
   */
  async listSellerOrders(params?: OrderListParams): Promise<PageOrderResponse> {
    const api = getApi();
    const response = await api.listSellerOrders({
      status: params?.status,
      page: params?.page,
      size: params?.size
    });
    return response.data.data as PageOrderResponse;
  }

  /**
   * 取消订单
   * @param orderNo 订单号
   * @returns 操作结果
   */
  async cancelOrder(orderNo: string): Promise<void> {
    const api = getApi();
    await api.cancelOrder({ orderNo });
  }

  // ==================== 支付相关接口 ====================

  /**
   * 支付订单（新接口）💳
   * @param orderNo 订单号
   * @param data 支付请求参数
   * @returns 支付响应（包含支付链接或二维码）
   */
  async payOrder(orderNo: string, data: PayOrderRequest): Promise<PaymentResponseData> {
    const api = getApi();
    const response = await api.payOrder({ orderNo, payOrderRequest: data });
    return response.data.data as PaymentResponseData;
  }

  // （已移除）createPayment：请使用 payOrder()

  /**
   * 查询订单支付状态
   * @param orderNo 订单号
   * @returns 支付状态
   */
  async queryPaymentStatus(orderNo: string): Promise<string> {
    const api = getApi();
    const response = await api.queryPaymentStatus({ orderNo });
    return response.data.data as string;
  }

  // ==================== 发货 / 确认收货 ====================

  /**
   * 卖家发货（快递）
   * @param orderNo 订单号
   * @param opts 物流信息
   */
  async shipOrder(orderNo: string, opts: { trackingNumber: string; company: string }): Promise<void> {
    const api = getApi();
    const payload: ShipOrderRequest = { trackingNumber: opts.trackingNumber, company: opts.company as any } as any;
    await api.shipOrder({ orderNo, shipOrderRequest: payload });
  }

  /**
   * 买家确认收货
   * @param params 包含 orderNo
   */
  async confirmReceipt(params: { orderNo: string }): Promise<void> {
    const api = getApi();
    await api.confirmReceipt({ orderNo: params.orderNo });
  }

  // ==================== 物流相关接口 ====================

  /**
   * 创建物流信息
   * @param orderId 订单 ID
   * @param trackingNumber 物流单号
   * @param company 物流公司
   * @returns 物流信息
   */
  async createLogistics(
    orderId: number,
    trackingNumber: string,
    company: 'SF' | 'YTO' | 'STO' | 'ZTO' | 'YD' | 'JTEXPRESS' | 'EMS' | 'OTHER'
  ): Promise<any> {
    const api = getApi();
    const response = await api.createLogistics({ orderId, trackingNumber, company: company as any });
    return response.data.data;
  }

  /**
   * 获取订单物流信息
   * @param orderId 订单 ID
   * @returns 物流信息
   */
  async getLogisticsByOrderId(orderId: number): Promise<any> {
    const api = getApi();
    const response = await api.getLogisticsByOrderId({ orderId });
    return response.data.data;
  }

  /**
   * 同步物流信息
   * @param orderId 订单 ID
   * @returns 最新物流信息
   */
  async syncLogistics(orderId: number): Promise<any> {
    const api = getApi();
    const response = await api.syncLogistics({ orderId });
    return response.data.data;
  }

  // ==================== 🎯 关键变更说明 ====================
  //
  // ✅ 已移除的手写路径方法（现使用 OpenAPI 生成代码）：
  //   - getOrderByNo()     → 改用 getOrderDetail()
  //   - getBuyerOrders()   → 改用 listBuyerOrders()
  //   - getSellerOrders()  → 改用 listSellerOrders()
  //   - payOrder()         →（已标准化）请继续使用 payOrder()
  //   - getPaymentStatus() → 改用 queryPaymentStatus()
  //
  // ✅ 已移除的功能（分散到专属服务）：
  //   - 确认收货         → 暂未实现后端接口
  //   - 申请退款         → 请使用 refundService
  //   - 评价相关         → 请使用 reviewService (goods/review.ts)
  //   - 管理员功能       → 暂未实现后端接口
  //
  // 📌 迁移指南：
  //   旧代码：orderService.getOrderByNo(orderNo)
  //   新代码：orderService.getOrderDetail(orderNo)
  //
  //   旧代码：orderService.payOrder(data)
  //   新代码：orderService.payOrder(orderNo, data)
  //
  // ==================== 🚀 重构完成 ====================
}

// 导出单例
export const orderService = new OrderService();
export default orderService;
