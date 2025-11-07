/**
 * Logistics Service - 物流服务
 * @author BaSui 😎
 * @description 查询订单物流信息、追踪物流轨迹
 */

import { http } from '../utils/apiClient';

// ==================== 类型定义 ====================

/**
 * 物流信息
 */
export interface Logistics {
  /**
   * 订单ID
   */
  orderId: number;

  /**
   * 快递公司代码
   */
  expressCode: string;

  /**
   * 快递公司名称
   */
  expressName: string;

  /**
   * 快递单号
   */
  trackingNumber: string;

  /**
   * 发货时间
   */
  shippedAt?: string;

  /**
   * 签收时间
   */
  deliveredAt?: string;

  /**
   * 当前状态
   * - PENDING: 待揽件
   * - IN_TRANSIT: 运输中
   * - OUT_FOR_DELIVERY: 派送中
   * - DELIVERED: 已签收
   * - EXCEPTION: 异常
   */
  status: 'PENDING' | 'IN_TRANSIT' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'EXCEPTION';

  /**
   * 物流轨迹
   */
  tracks: LogisticsTrack[];

  /**
   * 创建时间
   */
  createdAt?: string;

  /**
   * 更新时间
   */
  updatedAt?: string;
}

/**
 * 物流轨迹
 */
export interface LogisticsTrack {
  /**
   * 轨迹时间
   */
  time: string;

  /**
   * 轨迹描述
   */
  description: string;

  /**
   * 位置信息（可选）
   */
  location?: string;

  /**
   * 状态
   */
  status?: string;
}

/**
 * 物流统计
 */
export interface LogisticsStatistics {
  /**
   * 总订单数
   */
  totalOrders: number;

  /**
   * 待发货订单数
   */
  pendingShipment: number;

  /**
   * 运输中订单数
   */
  inTransit: number;

  /**
   * 已送达订单数
   */
  delivered: number;

  /**
   * 异常订单数
   */
  exception: number;

  /**
   * 平均配送时长（小时）
   */
  avgDeliveryTime: number;
}

// ==================== 服务接口 ====================

/**
 * 物流服务接口
 */
export interface LogisticsService {
  /**
   * 查询订单物流信息
   * @param orderId 订单ID
   * @returns 物流信息
   */
  getOrderLogistics(orderId: number): Promise<Logistics>;

  /**
   * 追踪物流轨迹
   * @param expressCode 快递公司代码
   * @param trackingNumber 快递单号
   * @returns 物流信息
   */
  trackLogistics(expressCode: string, trackingNumber: string): Promise<Logistics>;

  /**
   * 获取物流统计
   * @returns 物流统计信息
   */
  getLogisticsStatistics(): Promise<LogisticsStatistics>;
}

// ==================== 服务实现 ====================

/**
 * 物流服务实现类
 */
class LogisticsServiceImpl implements LogisticsService {
  /**
   * 查询订单物流信息
   */
  async getOrderLogistics(orderId: number): Promise<Logistics> {
    const response = await http.get<{ data: Logistics }>(`/api/orders/${orderId}/logistics`);
    return response.data.data;
  }

  /**
   * 追踪物流轨迹
   */
  async trackLogistics(expressCode: string, trackingNumber: string): Promise<Logistics> {
    const response = await http.get<{ data: Logistics }>('/api/logistics/track', {
      params: {
        expressCode,
        trackingNumber,
      },
    });
    return response.data.data;
  }

  /**
   * 获取物流统计
   */
  async getLogisticsStatistics(): Promise<LogisticsStatistics> {
    const response = await http.get<{ data: LogisticsStatistics }>('/api/logistics/statistics');
    return response.data.data;
  }
}

/**
 * 物流服务实例
 */
export const logisticsService = new LogisticsServiceImpl();
