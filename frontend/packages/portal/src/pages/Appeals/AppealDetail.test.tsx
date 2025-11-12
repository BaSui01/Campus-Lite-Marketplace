/**
 * AppealDetail 组件单元测试
 * @author BaSui 😎
 * @description 测试申诉详情页面的各种状态和功能
 * @date 2025-11-07
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AppealDetail } from './AppealDetail';
import { appealService } from '../../services';
import type { AppealDetailResponse } from '@campus/shared/api/models';

// Mock 路由参数
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useParams: () => ({ id: '1' }),
    useNavigate: () => vi.fn(),
  };
});

// Mock 申诉服务
vi.mock('../../services', () => ({
  appealService: {
    getAppealDetail: vi.fn(),
    cancelAppeal: vi.fn(),
  },
}));

/**
 * 创建测试用的 QueryClient
 */
const createTestQueryClient = () => {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });
};

/**
 * 渲染组件的辅助函数
 */
const renderAppealDetail = () => {
  const queryClient = createTestQueryClient();
  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AppealDetail />
      </BrowserRouter>
    </QueryClientProvider>
  );
};

/**
 * 创建模拟的申诉详情数据
 */
const createMockAppealDetail = (status: string): AppealDetailResponse => ({
  code: 200,
  message: 'success',
  data: {
    appeal: {
      id: 1,
      appealNo: 'AP20251107001',
      userId: 1,
      type: 'ORDER_DISPUTE',
      relatedId: 123,
      description: '订单商品与描述不符，申请退款',
      status,
      materials: [
        {
          type: 'IMAGE',
          url: 'https://example.com/image1.jpg',
          fileName: '证据1.jpg',
        },
        {
          type: 'DOCUMENT',
          url: 'https://example.com/doc1.pdf',
          fileName: '证据2.pdf',
        },
      ],
      createdAt: '2025-11-07T10:00:00',
      reviewedAt: status === 'APPROVED' || status === 'REJECTED' ? '2025-11-07T12:00:00' : undefined,
      reviewReason: status === 'APPROVED' ? '审核通过' : status === 'REJECTED' ? '证据不足' : undefined,
      reviewerName: status === 'APPROVED' || status === 'REJECTED' ? '管理员' : undefined,
      expireAt: '2025-11-14T10:00:00',
    },
  },
});

describe('AppealDetail 组件测试', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('组件渲染测试', () => {
    it('应该显示加载状态', () => {
      vi.mocked(appealService.getAppealDetail).mockImplementation(
        () => new Promise(() => {}) // 永不 resolve，保持加载状态
      );

      renderAppealDetail();

      expect(screen.getByText('加载中...')).toBeInTheDocument();
    });

    it('应该正确渲染 PENDING 状态的申诉', async () => {
      const mockData = createMockAppealDetail('PENDING');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('申诉详情')).toBeInTheDocument();
        expect(screen.getByText('待处理')).toBeInTheDocument();
        expect(screen.getByText('取消申诉')).toBeInTheDocument();
        expect(screen.getByText('📬 状态变更通知')).toBeInTheDocument();
      });
    });

    it('应该正确渲染 REVIEWING 状态的申诉', async () => {
      const mockData = createMockAppealDetail('REVIEWING');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('审核中')).toBeInTheDocument();
        expect(screen.getByText('📬 状态变更通知')).toBeInTheDocument();
      });
    });

    it('应该正确渲染 APPROVED 状态的申诉', async () => {
      const mockData = createMockAppealDetail('APPROVED');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('已通过')).toBeInTheDocument();
        expect(screen.getByText('审核通过')).toBeInTheDocument();
        expect(screen.queryByText('取消申诉')).not.toBeInTheDocument();
        expect(screen.queryByText('📬 状态变更通知')).not.toBeInTheDocument();
      });
    });

    it('应该正确渲染 REJECTED 状态的申诉', async () => {
      const mockData = createMockAppealDetail('REJECTED');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('已驳回')).toBeInTheDocument();
        expect(screen.getByText('证据不足')).toBeInTheDocument();
        expect(screen.queryByText('取消申诉')).not.toBeInTheDocument();
      });
    });

    it('应该正确渲染 EXPIRED 状态的申诉', async () => {
      const mockData = createMockAppealDetail('EXPIRED');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('已过期')).toBeInTheDocument();
      });
    });
  });

  describe('申诉材料展示测试', () => {
    it('应该正确展示申诉材料列表', async () => {
      const mockData = createMockAppealDetail('PENDING');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('附件材料')).toBeInTheDocument();
        expect(screen.getByText('证据1.jpg')).toBeInTheDocument();
        expect(screen.getByText('证据2.pdf')).toBeInTheDocument();
      });
    });

    it('应该在没有材料时显示提示', async () => {
      const mockData = createMockAppealDetail('PENDING');
      mockData.data!.appeal!.materials = [];
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('暂无材料')).toBeInTheDocument();
      });
    });
  });

  describe('时间轴展示测试', () => {
    it('应该显示申诉提交节点', async () => {
      const mockData = createMockAppealDetail('PENDING');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('申诉提交')).toBeInTheDocument();
        expect(screen.getByText('您已成功提交申诉，等待审核')).toBeInTheDocument();
      });
    });

    it('应该在 REVIEWING 状态显示审核中节点', async () => {
      const mockData = createMockAppealDetail('REVIEWING');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('审核中')).toBeInTheDocument();
        expect(screen.getByText('管理员正在审核您的申诉，请耐心等待')).toBeInTheDocument();
      });
    });

    it('应该在 APPROVED 状态显示申诉通过节点', async () => {
      const mockData = createMockAppealDetail('APPROVED');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('申诉通过')).toBeInTheDocument();
      });
    });

    it('应该在 REJECTED 状态显示申诉驳回节点', async () => {
      const mockData = createMockAppealDetail('REJECTED');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('申诉驳回')).toBeInTheDocument();
      });
    });
  });

  describe('状态变更通知测试', () => {
    it('应该在 PENDING 状态显示通知提示', async () => {
      const mockData = createMockAppealDetail('PENDING');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('📬 状态变更通知')).toBeInTheDocument();
        expect(screen.getByText(/当申诉状态发生变更时/)).toBeInTheDocument();
      });
    });

    it('应该在 REVIEWING 状态显示通知提示', async () => {
      const mockData = createMockAppealDetail('REVIEWING');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('📬 状态变更通知')).toBeInTheDocument();
      });
    });

    it('应该在 APPROVED 状态隐藏通知提示', async () => {
      const mockData = createMockAppealDetail('APPROVED');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.queryByText('📬 状态变更通知')).not.toBeInTheDocument();
      });
    });

    it('应该在 REJECTED 状态隐藏通知提示', async () => {
      const mockData = createMockAppealDetail('REJECTED');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.queryByText('📬 状态变更通知')).not.toBeInTheDocument();
      });
    });
  });

  describe('取消申诉功能测试', () => {
    it('应该在 PENDING 状态显示取消按钮', async () => {
      const mockData = createMockAppealDetail('PENDING');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('取消申诉')).toBeInTheDocument();
      });
    });

    it('应该在非 PENDING 状态隐藏取消按钮', async () => {
      const mockData = createMockAppealDetail('APPROVED');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.queryByText('取消申诉')).not.toBeInTheDocument();
      });
    });

    it('应该能够取消申诉', async () => {
      const mockData = createMockAppealDetail('PENDING');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);
      vi.mocked(appealService.cancelAppeal).mockResolvedValue({ code: 200, message: 'success' });

      // Mock window.confirm
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
      // Mock window.alert
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('取消申诉')).toBeInTheDocument();
      });

      const cancelButton = screen.getByText('取消申诉');
      fireEvent.click(cancelButton);

      await waitFor(() => {
        expect(confirmSpy).toHaveBeenCalledWith('确定要取消此申诉吗？此操作不可撤销。');
        expect(appealService.cancelAppeal).toHaveBeenCalledWith(1);
        expect(alertSpy).toHaveBeenCalledWith('申诉已取消');
      });

      confirmSpy.mockRestore();
      alertSpy.mockRestore();
    });
  });

  describe('错误处理测试', () => {
    it('应该处理加载失败的情况', async () => {
      vi.mocked(appealService.getAppealDetail).mockRejectedValue(new Error('网络错误'));

      // Mock window.alert
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});

      renderAppealDetail();

      await waitFor(() => {
        expect(alertSpy).toHaveBeenCalledWith('加载失败: 网络错误');
      });

      alertSpy.mockRestore();
    });

    it('应该处理取消申诉失败的情况', async () => {
      const mockData = createMockAppealDetail('PENDING');
      vi.mocked(appealService.getAppealDetail).mockResolvedValue(mockData);
      vi.mocked(appealService.cancelAppeal).mockRejectedValue(new Error('取消失败'));

      // Mock window.confirm
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
      // Mock window.alert
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});

      renderAppealDetail();

      await waitFor(() => {
        expect(screen.getByText('取消申诉')).toBeInTheDocument();
      });

      const cancelButton = screen.getByText('取消申诉');
      fireEvent.click(cancelButton);

      await waitFor(() => {
        expect(alertSpy).toHaveBeenCalledWith('取消失败: 取消失败');
      });

      confirmSpy.mockRestore();
      alertSpy.mockRestore();
    });
  });
});
