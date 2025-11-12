/**
 * ReviewCard 测试文件
 * @author BaSui 😎
 * @description TDD 先行：评价卡片组件测试
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { ReviewCard } from './ReviewCard';
import type { ReviewDetail } from '@campus/shared/services/goods';

// Mock 评价数据
const mockReview: ReviewDetail = {
  id: 1,
  orderId: 123,
  goodsId: 456,
  buyerId: 100,
  sellerId: 200,
  rating: 5,
  content: '商品质量非常好，卖家服务态度也很棒，物流很快，非常满意！',
  images: ['https://example.com/image1.jpg', 'https://example.com/image2.jpg'],
  isAnonymous: false,
  likeCount: 10,
  replyCount: 1,
  createdAt: '2023-11-06T10:00:00Z',
  updatedAt: '2023-11-06T10:00:00Z',
  buyer: {
    id: 100,
    nickname: '测试买家',
    avatar: 'https://example.com/avatar.jpg',
  },
  seller: {
    id: 200,
    nickname: '测试卖家',
    avatar: 'https://example.com/seller-avatar.jpg',
  },
  isLiked: false,
  reply: {
    id: 1,
    reviewId: 1,
    content: '感谢您的好评，期待下次光临！',
    replyType: 'SELLER_REPLY' as any,
    replyUserId: 200,
    createdAt: '2023-11-06T11:00:00Z',
    updatedAt: '2023-11-06T11:00:00Z',
  },
};

// 测试工具：渲染组件
const renderComponent = (props: Partial<React.ComponentProps<typeof ReviewCard>> = {}) => {
  return render(
    <BrowserRouter>
      <ReviewCard review={mockReview} {...props} />
    </BrowserRouter>
  );
};

describe('ReviewCard - 评价卡片组件', () => {
  let mockOnLike: ReturnType<typeof vi.fn>;
  let mockOnEdit: ReturnType<typeof vi.fn>;
  let mockOnDelete: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    vi.clearAllMocks();
    mockOnLike = vi.fn();
    mockOnEdit = vi.fn();
    mockOnDelete = vi.fn();
  });

  // ==================== 基础渲染测试 ====================

  it('应该正确渲染评价内容', () => {
    renderComponent();
    expect(screen.getByText(/商品质量非常好/)).toBeInTheDocument();
  });

  it('应该显示买家信息', () => {
    renderComponent();
    expect(screen.getByText('测试买家')).toBeInTheDocument();
  });

  it('应该显示星级评分', () => {
    renderComponent();
    // StarRating 组件应该显示 5.0
    expect(screen.getByText('5.0')).toBeInTheDocument();
  });

  it('应该显示评价时间', () => {
    renderComponent();
    // 应该显示相对时间或格式化时间
    const timeElement = screen.getByText(/2023/);
    expect(timeElement).toBeInTheDocument();
  });

  // ==================== 图片展示测试 ====================

  it('应该显示评价图片', () => {
    renderComponent();
    const images = screen.getAllByRole('img', { name: /review-image/ });
    expect(images.length).toBeGreaterThan(0);
  });

  it('没有图片时不显示图片区域', () => {
    const reviewWithoutImages = { ...mockReview, images: [] };
    renderComponent({ review: reviewWithoutImages });
    const images = screen.queryAllByRole('img', { name: /review-image/ });
    expect(images.length).toBe(0);
  });

  // ==================== 点赞功能测试 ====================

  it('应该显示点赞按钮和点赞数', () => {
    renderComponent();
    expect(screen.getByText('10')).toBeInTheDocument();
  });

  it('点击点赞按钮应该触发回调', () => {
    renderComponent({ onLike: mockOnLike });
    const likeBtn = screen.getByRole('button', { name: /点赞/ });
    fireEvent.click(likeBtn);
    expect(mockOnLike).toHaveBeenCalledWith(1);
  });

  it('已点赞时应该显示激活状态', () => {
    const likedReview = { ...mockReview, isLiked: true };
    renderComponent({ review: likedReview });
    const likeBtn = screen.getByRole('button', { name: /点赞/ });
    expect(likeBtn).toHaveClass('liked');
  });

  // ==================== 回复展示测试 ====================

  it('有卖家回复时应该显示回复内容', () => {
    renderComponent();
    expect(screen.getByText(/感谢您的好评/)).toBeInTheDocument();
  });

  it('应该显示卖家标识', () => {
    renderComponent();
    expect(screen.getByText(/卖家回复/)).toBeInTheDocument();
  });

  it('没有回复时不显示回复区域', () => {
    const reviewWithoutReply = { ...mockReview, reply: undefined };
    renderComponent({ review: reviewWithoutReply });
    expect(screen.queryByText(/卖家回复/)).not.toBeInTheDocument();
  });

  // ==================== 操作按钮测试 ====================

  it('showActions=true 时应该显示编辑和删除按钮', () => {
    renderComponent({ showActions: true, onEdit: mockOnEdit, onDelete: mockOnDelete });
    expect(screen.getByRole('button', { name: /编辑/ })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /删除/ })).toBeInTheDocument();
  });

  it('showActions=false 时不显示操作按钮', () => {
    renderComponent({ showActions: false });
    expect(screen.queryByRole('button', { name: /编辑/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /删除/ })).not.toBeInTheDocument();
  });

  it('点击编辑按钮应该触发回调', () => {
    renderComponent({ showActions: true, onEdit: mockOnEdit });
    const editBtn = screen.getByRole('button', { name: /编辑/ });
    fireEvent.click(editBtn);
    expect(mockOnEdit).toHaveBeenCalledWith(1);
  });

  it('点击删除按钮应该触发回调', () => {
    renderComponent({ showActions: true, onDelete: mockOnDelete });
    const deleteBtn = screen.getByRole('button', { name: /删除/ });
    fireEvent.click(deleteBtn);
    expect(mockOnDelete).toHaveBeenCalledWith(1);
  });

  // ==================== 匿名评价测试 ====================

  it('匿名评价应该显示"匿名用户"', () => {
    const anonymousReview = { ...mockReview, isAnonymous: true };
    renderComponent({ review: anonymousReview });
    expect(screen.getByText('匿名用户')).toBeInTheDocument();
  });

  // ==================== 边界情况测试 ====================

  it('内容为空时应该显示占位符', () => {
    const emptyReview = { ...mockReview, content: '' };
    renderComponent({ review: emptyReview });
    expect(screen.getByText(/暂无评价内容/)).toBeInTheDocument();
  });

  it('评分为0时应该显示默认状态', () => {
    const zeroRatingReview = { ...mockReview, rating: 0 };
    renderComponent({ review: zeroRatingReview });
    expect(screen.getByText('0.0')).toBeInTheDocument();
  });
});
