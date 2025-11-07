/**
 * 纠纷服务测试
 * @author BaSui 😎
 * @description 测试纠纷API服务的所有方法
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { disputeService, DisputeStatus, DisputeType } from './dispute';
import { axiosInstance } from '../utils/apiClient';

// Mock axiosInstance
vi.mock('../utils/apiClient', () => ({
  axiosInstance: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('DisputeService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('listDisputes', () => {
    it('应该成功获取纠纷列表', async () => {
      const mockResponse = {
        data: {
          code: 200,
          message: 'success',
          data: {
            content: [
              {
                id: 1,
                disputeNo: 'DSP20251106001',
                orderNo: 'ORD1001',
                type: DisputeType.GOODS_QUALITY,
                status: DisputeStatus.PENDING,
                title: '商品质量问题',
                description: '商品有瑕疵',
                amount: 99.99,
                plaintiffId: 1,
                plaintiffName: '买家',
                defendantId: 2,
                defendantName: '卖家',
                createdAt: '2025-11-06T10:00:00',
                updatedAt: '2025-11-06T10:00:00',
              },
            ],
            totalElements: 1,
            totalPages: 1,
            number: 0,
            size: 20,
          },
        },
      };

      vi.mocked(http.get).mockResolvedValue(mockResponse);

      const result = await disputeService.listDisputes({
        page: 0,
        size: 20,
      });

      expect(http.get).toHaveBeenCalledWith('/admin/disputes', {
        params: { page: 0, size: 20 },
      });
      expect(result.data.data.content).toHaveLength(1);
      expect(result.data.data.content[0].disputeNo).toBe('DSP20251106001');
    });

    it('应该支持关键字搜索', async () => {
      const mockResponse = {
        data: {
          code: 200,
          message: 'success',
          data: {
            content: [],
            totalElements: 0,
            totalPages: 0,
            number: 0,
            size: 20,
          },
        },
      };

      vi.mocked(http.get).mockResolvedValue(mockResponse);

      await disputeService.listDisputes({
        keyword: 'DSP001',
        status: DisputeStatus.INVESTIGATING,
        page: 0,
        size: 20,
      });

      expect(http.get).toHaveBeenCalledWith('/admin/disputes', {
        params: {
          keyword: 'DSP001',
          status: DisputeStatus.INVESTIGATING,
          page: 0,
          size: 20,
        },
      });
    });
  });

  describe('getDisputeDetail', () => {
    it('应该成功获取纠纷详情', async () => {
      const mockResponse = {
        data: {
          code: 200,
          message: 'success',
          data: {
            id: 1,
            disputeNo: 'DSP20251106001',
            orderNo: 'ORD1001',
            type: DisputeType.GOODS_QUALITY,
            status: DisputeStatus.INVESTIGATING,
            title: '商品质量问题',
            description: '商品有瑕疵',
            amount: 99.99,
            plaintiffId: 1,
            plaintiffName: '买家',
            defendantId: 2,
            defendantName: '卖家',
            orderInfo: {
              orderNo: 'ORD1001',
              goodsId: 100,
              goodsTitle: '测试商品',
              goodsImage: 'https://example.com/image.jpg',
              totalAmount: 99.99,
              status: 'COMPLETED',
              buyerId: 1,
              buyerName: '买家',
              sellerId: 2,
              sellerName: '卖家',
            },
            evidenceMaterials: [],
            arbitrationHistory: [],
            createdAt: '2025-11-06T10:00:00',
            updatedAt: '2025-11-06T10:00:00',
          },
        },
      };

      vi.mocked(http.get).mockResolvedValue(mockResponse);

      const result = await disputeService.getDisputeDetail(1);

      expect(http.get).toHaveBeenCalledWith('/admin/disputes/1');
      expect(result.data.data).toBeDefined();
      expect(result.data.data.id).toBe(1);
      expect(result.data.data.orderInfo).toBeDefined();
    });

    it('应该处理纠纷不存在的情况', async () => {
      vi.mocked(http.get).mockRejectedValue(new Error('Dispute not found'));

      await expect(disputeService.getDisputeDetail(999)).rejects.toThrow(
        'Dispute not found'
      );
    });
  });

  describe('claimDispute', () => {
    it('应该成功认领纠纷', async () => {
      const mockResponse = {
        data: {
          code: 200,
          message: 'success',
          data: null,
        },
      };

      vi.mocked(http.post).mockResolvedValue(mockResponse);

      const result = await disputeService.claimDispute(1);

      expect(http.post).toHaveBeenCalledWith('/admin/disputes/1/claim');
      expect(result.data.code).toBe(200);
    });
  });

  describe('arbitrateDispute', () => {
    it('应该成功提交仲裁（解决纠纷）', async () => {
      const mockResponse = {
        data: {
          code: 200,
          message: 'success',
          data: null,
        },
      };

      vi.mocked(http.post).mockResolvedValue(mockResponse);

      const result = await disputeService.arbitrateDispute({
        disputeId: 1,
        action: 'RESOLVE',
        decision: '支持买家退款',
        compensationAmount: 99.99,
        reason: '商品确实存在质量问题',
      });

      expect(http.post).toHaveBeenCalledWith('/admin/disputes/1/arbitrate', {
        action: 'RESOLVE',
        decision: '支持买家退款',
        compensationAmount: 99.99,
        reason: '商品确实存在质量问题',
      });
      expect(result.data.code).toBe(200);
    });

    it('应该支持驳回纠纷', async () => {
      const mockResponse = {
        data: {
          code: 200,
          message: 'success',
          data: null,
        },
      };

      vi.mocked(http.post).mockResolvedValue(mockResponse);

      await disputeService.arbitrateDispute({
        disputeId: 1,
        action: 'REJECT',
        reason: '证据不足',
      });

      expect(http.post).toHaveBeenCalledWith('/admin/disputes/1/arbitrate', {
        action: 'REJECT',
        reason: '证据不足',
      });
    });
  });

  describe('submitEvidence', () => {
    it('应该成功提交证据', async () => {
      const mockResponse = {
        data: {
          code: 200,
          message: 'success',
          data: null,
        },
      };

      vi.mocked(http.post).mockResolvedValue(mockResponse);

      const result = await disputeService.submitEvidence({
        disputeId: 1,
        type: 'IMAGE',
        url: 'https://example.com/evidence.jpg',
        fileName: '证据.jpg',
        fileSize: 102400,
        description: '商品瑕疵照片',
      });

      expect(http.post).toHaveBeenCalledWith('/admin/disputes/1/evidence', {
        type: 'IMAGE',
        url: 'https://example.com/evidence.jpg',
        fileName: '证据.jpg',
        fileSize: 102400,
        description: '商品瑕疵照片',
      });
      expect(result.data.code).toBe(200);
    });
  });

  describe('closeDispute', () => {
    it('应该成功关闭纠纷', async () => {
      const mockResponse = {
        data: {
          code: 200,
          message: 'success',
          data: null,
        },
      };

      vi.mocked(http.post).mockResolvedValue(mockResponse);

      const result = await disputeService.closeDispute(1, '用户撤销申请');

      expect(http.post).toHaveBeenCalledWith('/admin/disputes/1/close', {
        reason: '用户撤销申请',
      });
      expect(result.data.code).toBe(200);
    });
  });

  describe('batchAssignArbitrator', () => {
    it('应该成功批量分配仲裁员', async () => {
      const mockResponse = {
        data: {
          code: 200,
          message: 'success',
          data: null,
        },
      };

      vi.mocked(http.post).mockResolvedValue(mockResponse);

      const result = await disputeService.batchAssignArbitrator([1, 2, 3], 100);

      expect(http.post).toHaveBeenCalledWith('/admin/disputes/batch-assign', {
        disputeIds: [1, 2, 3],
        arbitratorId: 100,
      });
      expect(result.data.code).toBe(200);
    });
  });

  describe('边界条件测试', () => {
    it('应该处理空列表', async () => {
      const mockResponse = {
        data: {
          code: 200,
          message: 'success',
          data: {
            content: [],
            totalElements: 0,
            totalPages: 0,
            number: 0,
            size: 20,
          },
        },
      };

      vi.mocked(http.get).mockResolvedValue(mockResponse);

      const result = await disputeService.listDisputes({ page: 0, size: 20 });

      expect(result.data.data.content).toHaveLength(0);
    });

    it('应该处理网络错误', async () => {
      vi.mocked(http.get).mockRejectedValue(new Error('Network Error'));

      await expect(
        disputeService.listDisputes({ page: 0, size: 20 })
      ).rejects.toThrow('Network Error');
    });
  });
});
