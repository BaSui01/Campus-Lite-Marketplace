/**
 * Logistics Service 测试文件
 * @author BaSui 😎
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { logisticsService } from './logistics';
import { http } from '../utils/apiClient';
import type { Logistics, LogisticsStatistics } from './logistics';

// Mock http
vi.mock('../utils/http');

describe('LogisticsService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ==================== getOrderLogistics 测试 ====================

  describe('getOrderLogistics', () => {
    const mockLogistics: Logistics = {
      orderId: 123,
      expressCode: 'SF',
      expressName: '顺丰速运',
      trackingNumber: 'SF1234567890',
      shippedAt: '2023-11-06T10:00:00Z',
      deliveredAt: '2023-11-07T15:00:00Z',
      status: 'DELIVERED',
      tracks: [
        {
          time: '2023-11-07T15:00:00Z',
          description: '快件已签收，签收人：本人',
          location: '北京市海淀区',
          status: 'DELIVERED',
        },
        {
          time: '2023-11-07T10:00:00Z',
          description: '快件正在派送中，配送员：张三',
          location: '北京市海淀区',
          status: 'OUT_FOR_DELIVERY',
        },
        {
          time: '2023-11-06T20:00:00Z',
          description: '快件已到达【北京海淀集散中心】',
          location: '北京市海淀区',
          status: 'IN_TRANSIT',
        },
        {
          time: '2023-11-06T10:00:00Z',
          description: '快件已揽收',
          location: '上海市浦东新区',
          status: 'PENDING',
        },
      ],
      createdAt: '2023-11-06T10:00:00Z',
      updatedAt: '2023-11-07T15:00:00Z',
    };

    it('应该成功查询订单物流信息', async () => {
      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockLogistics },
      } as any);

      const result = await logisticsService.getOrderLogistics(123);

      expect(result).toEqual(mockLogistics);
      expect(http.get).toHaveBeenCalledWith('/api/orders/123/logistics');
    });

    it('查询失败时应该抛出错误', async () => {
      vi.mocked(http.get).mockRejectedValue(new Error('网络错误'));

      await expect(logisticsService.getOrderLogistics(123)).rejects.toThrow('网络错误');
    });

    it('应该正确处理物流轨迹列表', async () => {
      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockLogistics },
      } as any);

      const result = await logisticsService.getOrderLogistics(123);

      expect(result.tracks).toHaveLength(4);
      expect(result.tracks[0].status).toBe('DELIVERED');
    });

    it('应该正确处理物流状态', async () => {
      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockLogistics },
      } as any);

      const result = await logisticsService.getOrderLogistics(123);

      expect(result.status).toBe('DELIVERED');
      expect(result.expressName).toBe('顺丰速运');
    });
  });

  // ==================== trackLogistics 测试 ====================

  describe('trackLogistics', () => {
    const mockLogistics: Logistics = {
      orderId: 0,
      expressCode: 'SF',
      expressName: '顺丰速运',
      trackingNumber: 'SF1234567890',
      status: 'IN_TRANSIT',
      tracks: [
        {
          time: '2023-11-06T20:00:00Z',
          description: '快件已到达【北京海淀集散中心】',
          location: '北京市海淀区',
          status: 'IN_TRANSIT',
        },
      ],
    };

    it('应该成功追踪物流轨迹', async () => {
      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockLogistics },
      } as any);

      const result = await logisticsService.trackLogistics('SF', 'SF1234567890');

      expect(result).toEqual(mockLogistics);
      expect(http.get).toHaveBeenCalledWith('/api/logistics/track', {
        params: {
          expressCode: 'SF',
          trackingNumber: 'SF1234567890',
        },
      });
    });

    it('追踪失败时应该抛出错误', async () => {
      vi.mocked(http.get).mockRejectedValue(new Error('快递单号不存在'));

      await expect(
        logisticsService.trackLogistics('SF', 'INVALID')
      ).rejects.toThrow('快递单号不存在');
    });

    it('应该正确处理不同快递公司', async () => {
      const ztLogistics = { ...mockLogistics, expressCode: 'ZTO', expressName: '中通快递' };
      
      vi.mocked(http.get).mockResolvedValue({
        data: { data: ztLogistics },
      } as any);

      const result = await logisticsService.trackLogistics('ZTO', 'ZTO1234567890');

      expect(result.expressCode).toBe('ZTO');
      expect(result.expressName).toBe('中通快递');
    });
  });

  // ==================== getLogisticsStatistics 测试 ====================

  describe('getLogisticsStatistics', () => {
    const mockStatistics: LogisticsStatistics = {
      totalOrders: 1000,
      pendingShipment: 50,
      inTransit: 300,
      delivered: 600,
      exception: 50,
      avgDeliveryTime: 48.5,
    };

    it('应该成功获取物流统计', async () => {
      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockStatistics },
      } as any);

      const result = await logisticsService.getLogisticsStatistics();

      expect(result).toEqual(mockStatistics);
      expect(http.get).toHaveBeenCalledWith('/api/logistics/statistics');
    });

    it('获取失败时应该抛出错误', async () => {
      vi.mocked(http.get).mockRejectedValue(new Error('服务器错误'));

      await expect(logisticsService.getLogisticsStatistics()).rejects.toThrow('服务器错误');
    });

    it('应该正确解析统计数据', async () => {
      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockStatistics },
      } as any);

      const result = await logisticsService.getLogisticsStatistics();

      expect(result.totalOrders).toBe(1000);
      expect(result.delivered).toBe(600);
      expect(result.avgDeliveryTime).toBe(48.5);
    });

    it('应该正确计算各状态订单数', async () => {
      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockStatistics },
      } as any);

      const result = await logisticsService.getLogisticsStatistics();

      const totalByStatus = 
        result.pendingShipment + 
        result.inTransit + 
        result.delivered + 
        result.exception;

      expect(totalByStatus).toBe(1000);
    });
  });
});
