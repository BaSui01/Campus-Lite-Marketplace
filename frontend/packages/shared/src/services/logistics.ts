/**
 * Logistics Service - 物流服务（已完成OpenAPI集成）✅
 * @author BaSui 😎
 * @description 查询订单物流信息、追踪物流轨迹
 * @date 2025-11-10 - 完成后端API集成
 */

import { getApi } from '../utils/apiClient';
import type { LogisticsDTO, LogisticsStatisticsDTO } from '../api';

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
 * 快递公司枚举映射
 * 后端: SHUNFENG -> 前端: SF
 */
const COMPANY_MAPPING: Record<string, string> = {
  'SHUNFENG': 'SF',
  'ZHONGTONG': 'ZTO',
  'YUANTONG': 'YTO',
  'YUNDA': 'YD',
  'EMS': 'EMS',
  'JINGDONG': 'JD',
  'DEBANG': 'DBL',
  'SHENTONG': 'STO',
};

const COMPANY_NAMES: Record<string, string> = {
  'SHUNFENG': '顺丰速运',
  'ZHONGTONG': '中通快递',
  'YUANTONG': '圆通速递',
  'YUNDA': '韵达快递',
  'EMS': '邮政EMS',
  'JINGDONG': '京东物流',
  'DEBANG': '德邦物流',
  'SHENTONG': '申通快递',
};

/**
 * 状态映射
 * 后端: PENDING -> 前端: PENDING
 */
const STATUS_MAPPING: Record<string, Logistics['status']> = {
  'PENDING': 'PENDING',
  'PICKED_UP': 'IN_TRANSIT',
  'IN_TRANSIT': 'IN_TRANSIT',
  'DELIVERING': 'OUT_FOR_DELIVERY',
  'DELIVERED': 'DELIVERED',
  'REJECTED': 'EXCEPTION',
  'LOST': 'EXCEPTION',
};

/**
 * 物流服务实现类（已集成OpenAPI）✅
 * @author BaSui 😎
 */
class LogisticsServiceImpl implements LogisticsService {
  /**
   * 查询订单物流信息 ✅
   * 后端接口: GET /logistics/order/{orderId}
   */
  async getOrderLogistics(orderId: number): Promise<Logistics> {
    const api = getApi();
    const response = await api.getLogisticsByOrderId({ orderId });
    
    const data = response.data.data as LogisticsDTO;
    
    // 映射后端字段到前端格式
    return {
      orderId: data.orderId!,
      expressCode: COMPANY_MAPPING[data.logisticsCompany as string] || data.logisticsCompany as string,
      expressName: COMPANY_NAMES[data.logisticsCompany as string] || data.logisticsCompany as string,
      trackingNumber: data.trackingNumber!,
      shippedAt: data.createdAt,
      deliveredAt: data.actualDeliveryTime,
      status: STATUS_MAPPING[data.status as string] || 'PENDING',
      tracks: (data.trackRecords || []).map(track => ({
        time: track.time!,
        description: track.statusDesc!,
        location: track.location,
        status: track.statusDesc,
      })),
      createdAt: data.createdAt,
      updatedAt: data.updatedAt,
    };
  }

  /**
   * 追踪物流轨迹 ✅
   * 后端接口: GET /logistics/tracking/{trackingNumber}
   */
  async trackLogistics(_expressCode: string, trackingNumber: string): Promise<Logistics> {
    const api = getApi();
    const response = await api.getLogisticsByTrackingNumber({ trackingNumber });
    
    const data = response.data.data as LogisticsDTO;
    
    // 映射后端字段到前端格式
    return {
      orderId: data.orderId!,
      expressCode: COMPANY_MAPPING[data.logisticsCompany as string] || data.logisticsCompany as string,
      expressName: COMPANY_NAMES[data.logisticsCompany as string] || data.logisticsCompany as string,
      trackingNumber: data.trackingNumber!,
      shippedAt: data.createdAt,
      deliveredAt: data.actualDeliveryTime,
      status: STATUS_MAPPING[data.status as string] || 'PENDING',
      tracks: (data.trackRecords || []).map(track => ({
        time: track.time!,
        description: track.statusDesc!,
        location: track.location,
        status: track.statusDesc,
      })),
      createdAt: data.createdAt,
      updatedAt: data.updatedAt,
    };
  }

  /**
   * 获取物流统计 ✅
   * 后端接口: GET /logistics/statistics
   */
  async getLogisticsStatistics(startDate?: string, endDate?: string): Promise<LogisticsStatistics> {
    const api = getApi();
    
    // 默认查询最近30天
    const end = endDate || new Date().toISOString().split('T')[0];
    const start = startDate || new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
    
    const response = await api.getLogisticsStatistics({ startDate: start, endDate: end });
    const data = response.data.data as LogisticsStatisticsDTO;
    
    return {
      totalOrders: data.totalOrders || 0,
      pendingShipment: data.pendingShipment || 0,
      inTransit: data.inTransit || 0,
      delivered: data.delivered || 0,
      exception: data.exception || 0,
      avgDeliveryTime: data.avgDeliveryTime || 0,
    };
  }
  
  /**
   * 管理员物流列表 ✅
   * 后端接口: GET /admin/logistics
   */
  async listLogistics(params?: {
    keyword?: string;
    status?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: string;
  }): Promise<{ content: Logistics[]; totalElements: number; totalPages: number }> {
    const api = getApi();
    const response = await api.listLogistics({
      keyword: params?.keyword,
      status: params?.status as any,
      page: params?.page || 0,
      size: params?.size || 20,
      sortBy: params?.sortBy,
      sortDirection: params?.sortDirection,
    });
    
    const data = response.data.data;
    
    return {
      content: (data?.content || []).map((item: LogisticsDTO) => ({
        orderId: item.orderId!,
        expressCode: COMPANY_MAPPING[item.logisticsCompany as string] || item.logisticsCompany as string,
        expressName: COMPANY_NAMES[item.logisticsCompany as string] || item.logisticsCompany as string,
        trackingNumber: item.trackingNumber!,
        shippedAt: item.createdAt,
        deliveredAt: item.actualDeliveryTime,
        status: STATUS_MAPPING[item.status as string] || 'PENDING',
        tracks: (item.trackRecords || []).map(track => ({
          time: track.time!,
          description: track.statusDesc!,
          location: track.location,
          status: track.statusDesc,
        })),
        createdAt: item.createdAt,
        updatedAt: item.updatedAt,
      })),
      totalElements: data?.totalElements || 0,
      totalPages: data?.totalPages || 0,
    };
  }
}

/**
 * 物流服务实例
 */
export const logisticsService = new LogisticsServiceImpl();
