/**
 * 导出服务单元测试
 * @author BaSui 😎
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { exportService, ExportType } from '../export';
import * as apiClient from '../../utils/apiClient';

// Mock API 客户端
vi.mock('../../utils/apiClient', () => ({
  getApi: vi.fn(),
}));

describe('Export Service', () => {
  let mockGetApi: ReturnType<typeof vi.fn>;
  let mockRequestExport: ReturnType<typeof vi.fn>;
  let mockListExports: ReturnType<typeof vi.fn>;
  let mockCancelExport: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    // 创建 mock API 方法
    mockRequestExport = vi.fn();
    mockListExports = vi.fn();
    mockCancelExport = vi.fn();

    mockGetApi = vi.fn(() => ({
      requestExport: mockRequestExport,
      listExports: mockListExports,
      cancelExport: mockCancelExport,
    }));

    (apiClient.getApi as any) = mockGetApi;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('requestExport', () => {
    it('应该成功创建导出任务', async () => {
      const mockTaskId = 12345;
      mockRequestExport.mockResolvedValueOnce({
        data: {
          data: mockTaskId,
        },
      });

      const result = await exportService.requestExport({
        type: ExportType.ORDERS,
        params: JSON.stringify({ status: 'COMPLETED' }),
      });

      expect(mockRequestExport).toHaveBeenCalledWith(
        ExportType.ORDERS,
        JSON.stringify({ status: 'COMPLETED' })
      );

      expect(result).toBe(mockTaskId);
    });

    it('应该支持不带参数创建导出任务', async () => {
      const mockTaskId = 12346;
      mockRequestExport.mockResolvedValueOnce({
        data: {
          data: mockTaskId,
        },
      });

      const result = await exportService.requestExport({
        type: ExportType.USERS,
      });

      expect(mockRequestExport).toHaveBeenCalledWith(
        ExportType.USERS,
        undefined
      );

      expect(result).toBe(mockTaskId);
    });

    it('应该支持所有导出类型', async () => {
      const exportTypes = [
        ExportType.ORDERS,
        ExportType.USERS,
        ExportType.GOODS,
        ExportType.REVIEWS,
        ExportType.DISPUTES,
        ExportType.REFUNDS,
      ];

      for (const type of exportTypes) {
        mockRequestExport.mockResolvedValueOnce({
          data: { data: 100 },
        });

        await exportService.requestExport({ type });

        expect(mockRequestExport).toHaveBeenCalledWith(type, undefined);
      }

      expect(mockRequestExport).toHaveBeenCalledTimes(exportTypes.length);
    });
  });

  describe('listMyExports', () => {
    it('应该成功查询导出任务列表', async () => {
      const mockExports = [
        {
          id: 1,
          type: 'orders',
          status: 'COMPLETED',
          fileName: 'orders.csv',
          createdAt: '2025-01-15T10:00:00',
        },
        {
          id: 2,
          type: 'users',
          status: 'PROCESSING',
          fileName: null,
          createdAt: '2025-01-15T11:00:00',
        },
      ];

      mockListExports.mockResolvedValueOnce({
        data: {
          data: mockExports,
        },
      });

      const result = await exportService.listMyExports();

      expect(mockListExports).toHaveBeenCalled();
      expect(result).toEqual(mockExports);
    });

    it('应该处理空列表', async () => {
      mockListExports.mockResolvedValueOnce({
        data: {
          data: [],
        },
      });

      const result = await exportService.listMyExports();

      expect(result).toEqual([]);
    });
  });

  describe('cancelExport', () => {
    it('应该成功取消导出任务', async () => {
      mockCancelExport.mockResolvedValueOnce({
        data: {
          data: null,
        },
      });

      await exportService.cancelExport(12345);

      expect(mockCancelExport).toHaveBeenCalledWith(12345);
    });
  });

  describe('downloadExport', () => {
    it('应该返回正确的下载URL', () => {
      const token = 'DL-20250115-abcdef';
      const url = exportService.downloadExport(token);

      expect(url).toBe('/api/exports/download/DL-20250115-abcdef');
    });

    it('应该正确处理特殊字符的 token', () => {
      const token = 'DL-2025-01-15_special-chars';
      const url = exportService.downloadExport(token);

      expect(url).toBe('/api/exports/download/DL-2025-01-15_special-chars');
    });
  });
});

describe('ExportType Enum', () => {
  it('应该包含所有导出类型', () => {
    expect(ExportType.ORDERS).toBe('orders');
    expect(ExportType.USERS).toBe('users');
    expect(ExportType.GOODS).toBe('goods');
    expect(ExportType.REVIEWS).toBe('reviews');
    expect(ExportType.DISPUTES).toBe('disputes');
    expect(ExportType.REFUNDS).toBe('refunds');
  });
});
