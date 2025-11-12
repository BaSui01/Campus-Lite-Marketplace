/**
 * MyReviews 测试文件
 * @author BaSui 😎
 * @description TDD 先行：我的评价页测试
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import MyReviews from './MyReviews';
import { useReviewStore } from '../../store/useReviewStore';

// Mock 状态管理
vi.mock('../../store/useReviewStore');

// Mock 评价数据
const mockReviews = [
  {
    id: 1,
    orderId: 123,
    goodsId: 456,
    buyerId: 100,
    sellerId: 200,
    rating: 5,
    content: '商品质量非常好，卖家服务态度也很棒！',
    images: ['https://example.com/image1.jpg'],
    isAnonymous: false,
    likeCount: 10,
    replyCount: 1,
    createdAt: new Date().toISOString(), // 刚创建，可编辑
    updatedAt: new Date().toISOString(),
    buyer: {
      id: 100,
      nickname: '测试买家',
      avatar: 'https://example.com/avatar.jpg',
    },
    isLiked: false,
  },
  {
    id: 2,
    orderId: 124,
    goodsId: 457,
    buyerId: 100,
    sellerId: 201,
    rating: 4,
    content: '物流很快，质量也不错。',
    images: [],
    isAnonymous: false,
    likeCount: 5,
    replyCount: 0,
    createdAt: new Date(Date.now() - 25 * 60 * 60 * 1000).toISOString(), // 25小时前，不可编辑
    updatedAt: new Date(Date.now() - 25 * 60 * 60 * 1000).toISOString(),
    buyer: {
      id: 100,
      nickname: '测试买家',
      avatar: 'https://example.com/avatar.jpg',
    },
    isLiked: false,
  },
];

// 测试工具：创建 Query Client
const createTestQueryClient = () => new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

// 测试工具：渲染组件
const renderComponent = (queryClient = createTestQueryClient()) => {
  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <MyReviews />
      </BrowserRouter>
    </QueryClientProvider>
  );
};

describe('MyReviews - 我的评价页', () => {
  let mockFetchMyReviews: ReturnType<typeof vi.fn>;
  let mockDeleteReview: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    vi.clearAllMocks();
    mockFetchMyReviews = vi.fn();
    mockDeleteReview = vi.fn().mockResolvedValue(undefined);

    (useReviewStore as any).mockReturnValue({
      myReviews: mockReviews,
      totalPages: 1,
      totalElements: 2,
      currentPage: 0,
      loading: false,
      error: null,
      fetchMyReviews: mockFetchMyReviews,
      deleteReview: mockDeleteReview,
    });
  });

  // ==================== 基础渲染测试 ====================

  it('应该正确渲染页面标题', () => {
    renderComponent();
    expect(screen.getByText(/我的评价/i)).toBeInTheDocument();
  });

  it('应该显示评价总数', () => {
    renderComponent();
    expect(screen.getByText(/2/)).toBeInTheDocument();
  });

  it('应该调用 fetchMyReviews 获取数据', () => {
    renderComponent();
    expect(mockFetchMyReviews).toHaveBeenCalled();
  });

  // ==================== 评价列表测试 ====================

  it('应该显示评价列表', () => {
    renderComponent();
    expect(screen.getByText(/商品质量非常好/)).toBeInTheDocument();
    expect(screen.getByText(/物流很快/)).toBeInTheDocument();
  });

  it('应该使用 ReviewCard 组件展示评价', () => {
    renderComponent();
    // ReviewCard 会显示星级评分
    const ratings = screen.getAllByText(/5.0|4.0/);
    expect(ratings.length).toBeGreaterThan(0);
  });

  // ==================== 编辑功能测试 ====================

  it('24小时内的评价应该显示编辑按钮', () => {
    renderComponent();
    const editButtons = screen.getAllByRole('button', { name: /编辑/ });
    // 第一条评价可以编辑
    expect(editButtons.length).toBeGreaterThanOrEqual(1);
  });

  it('超过24小时的评价不应该显示编辑按钮', () => {
    renderComponent();
    // 第二条评价不可编辑，所以编辑按钮数量应该小于评价总数
    const editButtons = screen.queryAllByRole('button', { name: /编辑/ });
    expect(editButtons.length).toBeLessThan(2);
  });

  it('点击编辑按钮应该打开编辑弹窗', async () => {
    renderComponent();
    const editButtons = screen.getAllByRole('button', { name: /编辑/ });
    
    fireEvent.click(editButtons[0]);
    
    await waitFor(() => {
      expect(screen.getByText(/编辑评价/i)).toBeInTheDocument();
    });
  });

  // ==================== 删除功能测试 ====================

  it('应该显示删除按钮', () => {
    renderComponent();
    const deleteButtons = screen.getAllByRole('button', { name: /删除/ });
    expect(deleteButtons.length).toBe(2);
  });

  it('点击删除按钮应该显示确认弹窗', async () => {
    renderComponent();
    const deleteButtons = screen.getAllByRole('button', { name: /删除/ });
    
    fireEvent.click(deleteButtons[0]);
    
    await waitFor(() => {
      expect(screen.getByText(/确认删除/i)).toBeInTheDocument();
    });
  });

  it('确认删除应该调用 deleteReview', async () => {
    renderComponent();
    const deleteButtons = screen.getAllByRole('button', { name: /删除/ });
    
    fireEvent.click(deleteButtons[0]);
    
    await waitFor(() => {
      expect(screen.getByText(/确认删除/i)).toBeInTheDocument();
    });

    const confirmBtn = screen.getByRole('button', { name: /确认/ });
    fireEvent.click(confirmBtn);
    
    await waitFor(() => {
      expect(mockDeleteReview).toHaveBeenCalledWith(1);
    });
  });

  it('取消删除应该关闭弹窗', async () => {
    renderComponent();
    const deleteButtons = screen.getAllByRole('button', { name: /删除/ });
    
    fireEvent.click(deleteButtons[0]);
    
    await waitFor(() => {
      expect(screen.getByText(/确认删除/i)).toBeInTheDocument();
    });

    const cancelBtn = screen.getByRole('button', { name: /取消/ });
    fireEvent.click(cancelBtn);
    
    await waitFor(() => {
      expect(screen.queryByText(/确认删除/i)).not.toBeInTheDocument();
    });
  });

  // ==================== 空状态测试 ====================

  it('没有评价时应该显示空状态', () => {
    (useReviewStore as any).mockReturnValue({
      myReviews: [],
      totalPages: 0,
      totalElements: 0,
      currentPage: 0,
      loading: false,
      error: null,
      fetchMyReviews: mockFetchMyReviews,
      deleteReview: mockDeleteReview,
    });

    renderComponent();
    expect(screen.getByText(/暂无评价/i)).toBeInTheDocument();
  });

  // ==================== 加载状态测试 ====================

  it('加载中应该显示骨架屏', () => {
    (useReviewStore as any).mockReturnValue({
      myReviews: [],
      totalPages: 0,
      totalElements: 0,
      currentPage: 0,
      loading: true,
      error: null,
      fetchMyReviews: mockFetchMyReviews,
      deleteReview: mockDeleteReview,
    });

    renderComponent();
    // Skeleton 组件会渲染
    expect(screen.queryByText(/商品质量非常好/)).not.toBeInTheDocument();
  });

  // ==================== 分页测试 ====================

  it('多页时应该显示分页组件', () => {
    (useReviewStore as any).mockReturnValue({
      myReviews: mockReviews,
      totalPages: 3,
      totalElements: 25,
      currentPage: 0,
      loading: false,
      error: null,
      fetchMyReviews: mockFetchMyReviews,
      deleteReview: mockDeleteReview,
    });

    renderComponent();
    // Pagination 组件应该显示页码
    expect(screen.getByText(/1/)).toBeInTheDocument();
  });
});
