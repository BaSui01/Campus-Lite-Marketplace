/**
 * Recommend Service 测试文件
 * @author BaSui 😎
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { recommendService } from './recommend';
import { http } from '../utils/apiClient';
import type { GoodsResponse } from '../api/models';

// Mock http
vi.mock('../utils/http');

describe('RecommendService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mockGoods: GoodsResponse[] = [
    {
      id: 1,
      title: '推荐商品1',
      price: 100,
      coverImage: 'image1.jpg',
    } as GoodsResponse,
    {
      id: 2,
      title: '推荐商品2',
      price: 200,
      coverImage: 'image2.jpg',
    } as GoodsResponse,
  ];

  describe('getPersonalizedRecommend', () => {
    it('应该成功获取个性化推荐', async () => {
      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockGoods },
      } as any);

      const result = await recommendService.getPersonalizedRecommend({
        userId: 1,
        size: 10,
      });

      expect(result).toEqual(mockGoods);
      expect(http.get).toHaveBeenCalledWith('/api/recommend/personalized', {
        params: { userId: 1, size: 10 },
      });
    });
  });

  describe('getSimilarGoods', () => {
    it('应该成功获取相似商品', async () => {
      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockGoods },
      } as any);

      const result = await recommendService.getSimilarGoods(123, 5);

      expect(result).toEqual(mockGoods);
      expect(http.get).toHaveBeenCalledWith('/api/recommend/similar/123', {
        params: { size: 5 },
      });
    });
  });

  describe('getHotGoods', () => {
    it('应该成功获取热门商品', async () => {
      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockGoods },
      } as any);

      const result = await recommendService.getHotGoods(1, 10);

      expect(result).toEqual(mockGoods);
      expect(http.get).toHaveBeenCalledWith('/api/recommend/hot', {
        params: { categoryId: 1, size: 10 },
      });
    });
  });

  describe('getGuessYouLike', () => {
    it('应该成功获取猜你喜欢', async () => {
      vi.mocked(http.get).mockResolvedValue({
        data: { data: mockGoods },
      } as any);

      const result = await recommendService.getGuessYouLike(1, 10);

      expect(result).toEqual(mockGoods);
      expect(http.get).toHaveBeenCalledWith('/api/recommend/guess/1', {
        params: { size: 10 },
      });
    });
  });
});
