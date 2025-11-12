/**
 * useReviewStore 单元测试
 * @author BaSui 😎
 * @description 测试评价状态管理的各种功能
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useReviewStore } from './useReviewStore';
import { Services } from '@campus/shared';

// 🔧 BaSui 修复：从 Services 命名空间解构
const { reviewService } = Services;

// Mock reviewService
vi.mock('@campus/shared', () => ({
  Services: {
    reviewService: {
    getMyReviews: vi.fn(),
    listReviews: vi.fn(),
    createReview: vi.fn(),
    deleteReview: vi.fn(),
    likeReview: vi.fn(),
    unlikeReview: vi.fn(),
    replyReview: vi.fn(),
    },
  },
}));

describe('useReviewStore 测试', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // 重置 store 状态
    useReviewStore.getState().reset();
  });

  // ==================== 初始状态测试 ====================

  describe('初始状态', () => {
    it('应该有正确的初始状态', () => {
      const { result } = renderHook(() => useReviewStore());

      expect(result.current.myReviews).toEqual([]);
      expect(result.current.currentGoodsReviews).toEqual([]);
      expect(result.current.currentGoodsId).toBeNull();
      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBeNull();
    });
  });

  // ==================== 获取我的评价测试 ====================

  describe('fetchMyReviews', () => {
    it('应该成功获取我的评价列表', async () => {
      const mockReviews = {
        content: [
          { id: 1, rating: 5, content: '很好' },
          { id: 2, rating: 4, content: '不错' },
        ],
        totalElements: 2,
        totalPages: 1,
        number: 0,
        size: 20,
      };

      vi.mocked(reviewService.getMyReviews).mockResolvedValue(mockReviews as any);

      const { result } = renderHook(() => useReviewStore());

      await act(async () => {
        await result.current.fetchMyReviews();
      });

      expect(result.current.myReviews).toHaveLength(2);
      expect(result.current.totalElements).toBe(2);
      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBeNull();
    });

    it('应该处理获取失败的情况', async () => {
      const mockError = new Error('网络错误');
      vi.mocked(reviewService.getMyReviews).mockRejectedValue(mockError);

      const { result } = renderHook(() => useReviewStore());

      await act(async () => {
        try {
          await result.current.fetchMyReviews();
        } catch (error) {
          // 预期会抛出错误
        }
      });

      expect(result.current.error).toBe('网络错误');
      expect(result.current.loading).toBe(false);
    });
  });

  // ==================== 获取商品评价测试 ====================

  describe('fetchGoodsReviews', () => {
    it('应该成功获取商品评价列表', async () => {
      const mockGoodsId = 123;
      const mockReviews = {
        content: [{ id: 1, rating: 5 }],
        totalElements: 1,
        totalPages: 1,
        number: 0,
      };

      vi.mocked(reviewService.listReviews).mockResolvedValue(mockReviews as any);

      const { result } = renderHook(() => useReviewStore());

      await act(async () => {
        await result.current.fetchGoodsReviews(mockGoodsId);
      });

      expect(result.current.currentGoodsReviews).toHaveLength(1);
      expect(result.current.currentGoodsId).toBe(mockGoodsId);
      expect(result.current.loading).toBe(false);
    });
  });

  // ==================== 创建评价测试 ====================

  describe('createReview', () => {
    it('应该成功创建评价', async () => {
      const mockReviewId = 123;
      const mockRequest = {
        orderId: 1,
        rating: 5,
        content: '很好',
      };

      vi.mocked(reviewService.createReview).mockResolvedValue(mockReviewId);
      vi.mocked(reviewService.getMyReviews).mockResolvedValue({
        content: [{ id: mockReviewId, ...mockRequest }],
        totalElements: 1,
        totalPages: 1,
        number: 0,
      } as any);

      const { result } = renderHook(() => useReviewStore());

      let reviewId: number | undefined;
      await act(async () => {
        reviewId = await result.current.createReview(mockRequest);
      });

      expect(reviewId).toBe(mockReviewId);
      expect(reviewService.createReview).toHaveBeenCalledWith(mockRequest);
      expect(result.current.loading).toBe(false);
    });
  });

  // ==================== 删除评价测试 ====================

  describe('deleteReview', () => {
    it('应该成功删除评价', async () => {
      // 先设置初始评价
      const { result } = renderHook(() => useReviewStore());

      act(() => {
        useReviewStore.setState({
          myReviews: [
            { id: 1, rating: 5 } as any,
            { id: 2, rating: 4 } as any,
          ],
        });
      });

      vi.mocked(reviewService.deleteReview).mockResolvedValue();

      await act(async () => {
        await result.current.deleteReview(1);
      });

      expect(result.current.myReviews).toHaveLength(1);
      expect(result.current.myReviews[0].id).toBe(2);
      expect(result.current.loading).toBe(false);
    });
  });

  // ==================== 点赞测试 ====================

  describe('likeReview', () => {
    it('应该成功点赞评价（乐观更新）', async () => {
      const { result } = renderHook(() => useReviewStore());

      act(() => {
        useReviewStore.setState({
          myReviews: [
            { id: 1, rating: 5, isLiked: false, likeCount: 0 } as any,
          ],
        });
      });

      vi.mocked(reviewService.likeReview).mockResolvedValue();

      await act(async () => {
        await result.current.likeReview(1);
      });

      expect(result.current.myReviews[0].isLiked).toBe(true);
      expect(result.current.myReviews[0].likeCount).toBe(1);
    });

    it('应该在点赞失败时回滚状态', async () => {
      const { result } = renderHook(() => useReviewStore());

      act(() => {
        useReviewStore.setState({
          myReviews: [
            { id: 1, rating: 5, isLiked: false, likeCount: 0 } as any,
          ],
        });
      });

      vi.mocked(reviewService.likeReview).mockRejectedValue(
        new Error('点赞失败')
      );

      await act(async () => {
        try {
          await result.current.likeReview(1);
        } catch (error) {
          // 预期会抛出错误
        }
      });

      expect(result.current.myReviews[0].isLiked).toBe(false);
      expect(result.current.myReviews[0].likeCount).toBe(0);
      expect(result.current.error).toBe('点赞失败');
    });
  });

  // ==================== 取消点赞测试 ====================

  describe('unlikeReview', () => {
    it('应该成功取消点赞（乐观更新）', async () => {
      const { result } = renderHook(() => useReviewStore());

      act(() => {
        useReviewStore.setState({
          myReviews: [
            { id: 1, rating: 5, isLiked: true, likeCount: 1 } as any,
          ],
        });
      });

      vi.mocked(reviewService.unlikeReview).mockResolvedValue();

      await act(async () => {
        await result.current.unlikeReview(1);
      });

      expect(result.current.myReviews[0].isLiked).toBe(false);
      expect(result.current.myReviews[0].likeCount).toBe(0);
    });
  });

  // ==================== 切换点赞测试 ====================

  describe('toggleLike', () => {
    it('应该在未点赞时点赞', async () => {
      const { result } = renderHook(() => useReviewStore());

      act(() => {
        useReviewStore.setState({
          myReviews: [
            { id: 1, rating: 5, isLiked: false, likeCount: 0 } as any,
          ],
        });
      });

      vi.mocked(reviewService.likeReview).mockResolvedValue();

      await act(async () => {
        await result.current.toggleLike(1);
      });

      expect(result.current.myReviews[0].isLiked).toBe(true);
    });

    it('应该在已点赞时取消点赞', async () => {
      const { result } = renderHook(() => useReviewStore());

      act(() => {
        useReviewStore.setState({
          myReviews: [
            { id: 1, rating: 5, isLiked: true, likeCount: 1 } as any,
          ],
        });
      });

      vi.mocked(reviewService.unlikeReview).mockResolvedValue();

      await act(async () => {
        await result.current.toggleLike(1);
      });

      expect(result.current.myReviews[0].isLiked).toBe(false);
    });
  });

  // ==================== 回复评价测试 ====================

  describe('replyReview', () => {
    it('应该成功回复评价', async () => {
      const mockReply = {
        id: 1,
        reviewId: 1,
        content: '感谢评价',
      };

      const { result } = renderHook(() => useReviewStore());

      act(() => {
        useReviewStore.setState({
          myReviews: [
            { id: 1, rating: 5, replyCount: 0 } as any,
          ],
        });
      });

      vi.mocked(reviewService.replyReview).mockResolvedValue(mockReply as any);

      await act(async () => {
        await result.current.replyReview(1, '感谢评价');
      });

      expect(result.current.myReviews[0].reply).toEqual(mockReply);
      expect(result.current.myReviews[0].replyCount).toBe(1);
    });
  });

  // ==================== 重置状态测试 ====================

  describe('reset', () => {
    it('应该重置所有状态', () => {
      const { result } = renderHook(() => useReviewStore());

      act(() => {
        useReviewStore.setState({
          myReviews: [{ id: 1 } as any],
          error: '错误信息',
          loading: true,
        });
      });

      act(() => {
        result.current.reset();
      });

      expect(result.current.myReviews).toEqual([]);
      expect(result.current.error).toBeNull();
      expect(result.current.loading).toBe(false);
    });
  });

  // ==================== 清除错误测试 ====================

  describe('clearError', () => {
    it('应该清除错误信息', () => {
      const { result } = renderHook(() => useReviewStore());

      act(() => {
        useReviewStore.setState({ error: '错误信息' });
      });

      act(() => {
        result.current.clearError();
      });

      expect(result.current.error).toBeNull();
    });
  });
});
