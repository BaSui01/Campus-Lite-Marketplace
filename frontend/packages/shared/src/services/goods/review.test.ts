/**
 * Review Service 单元测试
 * @author BaSui 😎
 * @description 测试评价服务的各种功能
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { reviewService } from './review';
import { axiosInstance } from '../../utils/apiClient';

// Mock axiosInstance
vi.mock('../../utils/apiClient', () => ({
  axiosInstance: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('ReviewService 测试', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ==================== 核心评价接口测试 ====================

  describe('createReview', () => {
    it('应该成功创建评价', async () => {
      const mockReviewId = 123;
      const mockRequest = {
        orderId: 1,
        rating: 5,
        content: '商品非常好',
      };

      vi.mocked(http.post).mockResolvedValue({
        data: { data: mockReviewId },
      } as any);

      const result = await reviewService.createReview(mockRequest);

      expect(http.post).toHaveBeenCalledWith('/api/reviews', mockRequest);
      expect(result).toBe(mockReviewId);
    });
  });

  describe('listReviews', () => {
    it('应该成功获取评价列表', async () => {
      const mockGoodsId = 1;
      const mockParams = { page: 0, size: 10 };
      const mockResponse = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        number: 0,
        size: 10,
        first: true,
        last: true,
        empty: true,
      };

      vi.mocked(http.get).mockResolvedValue({
        data: mockResponse,
      } as any);

      const result = await reviewService.listReviews(mockGoodsId, mockParams);

      expect(http.get).toHaveBeenCalledWith(
        `/api/goods/${mockGoodsId}/reviews`,
        { params: mockParams }
      );
      expect(result).toEqual(mockResponse);
    });
  });

  describe('deleteReview', () => {
    it('应该成功删除评价', async () => {
      const mockReviewId = 123;

      vi.mocked(http.delete).mockResolvedValue({} as any);

      await reviewService.deleteReview(mockReviewId);

      expect(http.delete).toHaveBeenCalledWith(`/api/reviews/${mockReviewId}`);
    });
  });

  // ==================== 点赞接口测试 ====================

  describe('likeReview', () => {
    it('应该成功点赞评价', async () => {
      const mockReviewId = 123;

      vi.mocked(http.post).mockResolvedValue({} as any);

      await reviewService.likeReview(mockReviewId);

      expect(http.post).toHaveBeenCalledWith(`/api/reviews/${mockReviewId}/like`);
    });
  });

  describe('unlikeReview', () => {
    it('应该成功取消点赞', async () => {
      const mockReviewId = 123;

      vi.mocked(http.delete).mockResolvedValue({} as any);

      await reviewService.unlikeReview(mockReviewId);

      expect(http.delete).toHaveBeenCalledWith(
        `/api/reviews/${mockReviewId}/like`
      );
    });
  });

  describe('getLikeStatus', () => {
    it('应该成功获取点赞状态', async () => {
      const mockReviewId = 123;
      const mockStatus = true;

      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockStatus },
      } as any);

      const result = await reviewService.getLikeStatus(mockReviewId);

      expect(http.get).toHaveBeenCalledWith(
        `/api/reviews/${mockReviewId}/like/status`
      );
      expect(result).toBe(mockStatus);
    });
  });

  describe('getLikeCount', () => {
    it('应该成功获取点赞数', async () => {
      const mockReviewId = 123;
      const mockCount = 42;

      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockCount },
      } as any);

      const result = await reviewService.getLikeCount(mockReviewId);

      expect(http.get).toHaveBeenCalledWith(
        `/api/reviews/${mockReviewId}/likes/count`
      );
      expect(result).toBe(mockCount);
    });
  });

  // ==================== 回复接口测试 ====================

  describe('replyReview', () => {
    it('应该成功回复评价', async () => {
      const mockReviewId = 123;
      const mockContent = '感谢您的评价！';
      const mockReply = {
        id: 1,
        reviewId: mockReviewId,
        content: mockContent,
      };

      vi.mocked(http.post).mockResolvedValue({
        data: { data: mockReply },
      } as any);

      const result = await reviewService.replyReview(mockReviewId, mockContent);

      expect(http.post).toHaveBeenCalledWith(
        `/api/reviews/${mockReviewId}/replies`,
        { content: mockContent, replyType: 'SELLER_REPLY' }
      );
      expect(result).toEqual(mockReply);
    });
  });

  describe('getReviewReplies', () => {
    it('应该成功获取评价回复列表', async () => {
      const mockReviewId = 123;
      const mockReplies = [
        { id: 1, content: '回复1' },
        { id: 2, content: '回复2' },
      ];

      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockReplies },
      } as any);

      const result = await reviewService.getReviewReplies(mockReviewId);

      expect(http.get).toHaveBeenCalledWith(
        `/api/reviews/${mockReviewId}/replies`
      );
      expect(result).toEqual(mockReplies);
    });
  });

  // ==================== 媒体接口测试 ====================

  describe('uploadReviewMedia', () => {
    it('应该成功上传评价媒体', async () => {
      const mockReviewId = 123;
      const mockFiles = [
        new File([''], 'image1.jpg', { type: 'image/jpeg' }),
        new File([''], 'image2.jpg', { type: 'image/jpeg' }),
      ];
      const mockUrls = ['url1', 'url2'];

      vi.mocked(http.post).mockResolvedValue({
        data: { data: mockUrls },
      } as any);

      const result = await reviewService.uploadReviewMedia(
        mockReviewId,
        mockFiles
      );

      expect(http.post).toHaveBeenCalledWith(
        `/api/reviews/${mockReviewId}/media`,
        expect.any(FormData),
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );
      expect(result).toEqual(mockUrls);
    });
  });

  describe('getReviewMedia', () => {
    it('应该成功获取评价媒体列表', async () => {
      const mockReviewId = 123;
      const mockMedia = [
        { id: 1, mediaUrl: 'url1' },
        { id: 2, mediaUrl: 'url2' },
      ];

      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockMedia },
      } as any);

      const result = await reviewService.getReviewMedia(mockReviewId);

      expect(http.get).toHaveBeenCalledWith(
        `/api/reviews/${mockReviewId}/media`
      );
      expect(result).toEqual(mockMedia);
    });
  });

  describe('deleteReviewMedia', () => {
    it('应该成功删除媒体', async () => {
      const mockMediaId = 123;

      vi.mocked(http.delete).mockResolvedValue({} as any);

      await reviewService.deleteReviewMedia(mockMediaId);

      expect(http.delete).toHaveBeenCalledWith(
        `/api/reviews/media/${mockMediaId}`
      );
    });
  });
});
