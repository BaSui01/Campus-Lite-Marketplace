/**
 * 支付服务单元测试
 * @author BaSui 😎
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { paymentService } from '../payment';
import * as apiClient from '../../utils/apiClient';

// Mock API 客户端
vi.mock('../../utils/apiClient', () => ({
  getApi: vi.fn(),
}));

describe('Payment Service', () => {
  let mockGetApi: ReturnType<typeof vi.fn>;
  let mockListOrdersAdmin: ReturnType<typeof vi.fn>;
  let mockGetOrderDetail: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    // 创建 mock API 方法
    mockListOrdersAdmin = vi.fn();
    mockGetOrderDetail = vi.fn();

    mockGetApi = vi.fn(() => ({
      listOrdersAdmin: mockListOrdersAdmin,
      getOrderDetail: mockGetOrderDetail,
    }));

    (apiClient.getApi as any) = mockGetApi;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('listPayments', () => {
    it('应该成功查询支付记录列表', async () => {
      const mockResponse = {
        data: {
          data: {
            content: [
              {
                id: 1,
                orderNo: 'ORDER001',
                totalAmount: 100.5,
                status: 'PAID',
              },
            ],
            totalElements: 1,
          },
        },
      };

      mockListOrdersAdmin.mockResolvedValueOnce(mockResponse);

      const result = await paymentService.listPayments({
        keyword: 'ORDER001',
        page: 0,
        size: 20,
      });

      expect(mockListOrdersAdmin).toHaveBeenCalledWith(
        'ORDER001',
        'PAID,SHIPPED,COMPLETED,REFUNDED',
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        0,
        20
      );

      expect(result).toEqual(mockResponse.data.data);
    });

    it('应该正确处理状态参数', async () => {
      const mockResponse = {
        data: {
          data: {
            content: [],
            totalElements: 0,
          },
        },
      };

      mockListOrdersAdmin.mockResolvedValueOnce(mockResponse);

      await paymentService.listPayments({
        status: 'PAID',
        page: 0,
        size: 10,
      });

      expect(mockListOrdersAdmin).toHaveBeenCalledWith(
        undefined,
        'PAID',
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        0,
        10
      );
    });

    it('应该支持日期范围查询', async () => {
      const mockResponse = {
        data: {
          data: {
            content: [],
            totalElements: 0,
          },
        },
      };

      mockListOrdersAdmin.mockResolvedValueOnce(mockResponse);

      await paymentService.listPayments({
        startDate: '2025-01-01',
        endDate: '2025-01-31',
        page: 0,
        size: 20,
      });

      expect(mockListOrdersAdmin).toHaveBeenCalledWith(
        undefined,
        'PAID,SHIPPED,COMPLETED,REFUNDED',
        undefined,
        undefined,
        undefined,
        '2025-01-01',
        '2025-01-31',
        0,
        20
      );
    });
  });

  describe('getPaymentDetail', () => {
    it('应该成功查询支付详情', async () => {
      const mockOrder = {
        id: 1,
        orderNo: 'ORDER001',
        paymentMethod: 'WECHAT_PAY',
        totalAmount: 100.5,
        status: 'PAID',
        paidAt: '2025-01-15T10:30:00',
        transactionId: 'TXN123456',
        buyerId: 10,
        buyerUsername: 'testuser',
        goodsTitle: 'Test Product',
        createdAt: '2025-01-15T10:00:00',
      };

      mockGetOrderDetail.mockResolvedValueOnce({
        data: {
          data: mockOrder,
        },
      });

      const result = await paymentService.getPaymentDetail('ORDER001');

      expect(mockGetOrderDetail).toHaveBeenCalledWith('ORDER001');

      expect(result).toEqual({
        id: 1,
        orderNo: 'ORDER001',
        paymentMethod: 'WECHAT_PAY',
        amount: 10050, // 元转分
        status: 'PAID',
        paidAt: '2025-01-15T10:30:00',
        transactionId: 'TXN123456',
        buyerId: 10,
        buyerUsername: 'testuser',
        goodsTitle: 'Test Product',
        createdAt: '2025-01-15T10:00:00',
      });
    });

    it('应该正确转换金额单位（元转分）', async () => {
      const mockOrder = {
        id: 1,
        orderNo: 'ORDER002',
        totalAmount: 99.99,
        paymentMethod: 'ALIPAY',
        status: 'PAID',
        buyerId: 10,
        buyerUsername: 'user',
        goodsTitle: 'Product',
        createdAt: '2025-01-15T10:00:00',
      };

      mockGetOrderDetail.mockResolvedValueOnce({
        data: {
          data: mockOrder,
        },
      });

      const result = await paymentService.getPaymentDetail('ORDER002');

      expect(result.amount).toBe(9999); // 99.99 * 100
    });
  });

  describe('getPaymentStatistics', () => {
    it('应该返回支付统计数据', async () => {
      const result = await paymentService.getPaymentStatistics();

      expect(result).toEqual({
        totalAmount: 0,
        totalCount: 0,
        successAmount: 0,
        successCount: 0,
        refundAmount: 0,
        refundCount: 0,
        wechatAmount: 0,
        alipayAmount: 0,
        balanceAmount: 0,
      });
    });

    it('应该支持日期范围参数', async () => {
      const result = await paymentService.getPaymentStatistics(
        '2025-01-01',
        '2025-01-31'
      );

      expect(result).toBeDefined();
    });
  });
});
