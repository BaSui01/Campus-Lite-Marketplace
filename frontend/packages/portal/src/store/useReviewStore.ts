/**
 * 评价状态管理
 * @author BaSui 😎
 * @description 使用 Zustand 管理评价相关状态
 */

import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import {
  reviewService,
  type ReviewDetail,
  type CreateReviewRequest,
  type ReviewListQuery,
} from '@campus/shared';

/**
 * 评价状态接口
 */
interface ReviewState {
  // ==================== 状态 ====================

  /**
   * 我的评价列表
   */
  myReviews: ReviewDetail[];

  /**
   * 当前查看的评价列表（商品详情页）
   */
  currentGoodsReviews: ReviewDetail[];

  /**
   * 当前商品ID（用于标识 currentGoodsReviews）
   */
  currentGoodsId: number | null;

  /**
   * 总条数
   */
  totalElements: number;

  /**
   * 总页数
   */
  totalPages: number;

  /**
   * 当前页码
   */
  currentPage: number;

  /**
   * 是否正在加载
   */
  loading: boolean;

  /**
   * 错误信息
   */
  error: string | null;

  // ==================== 操作方法 ====================

  /**
   * 获取我的评价列表
   */
  fetchMyReviews: (params?: { page?: number; size?: number }) => Promise<void>;

  /**
   * 获取商品评价列表
   */
  fetchGoodsReviews: (
    goodsId: number,
    params?: ReviewListQuery
  ) => Promise<void>;

  /**
   * 创建评价
   */
  createReview: (request: CreateReviewRequest) => Promise<number>;

  /**
   * 删除评价
   */
  deleteReview: (reviewId: number) => Promise<void>;

  /**
   * 点赞评价
   */
  likeReview: (reviewId: number) => Promise<void>;

  /**
   * 取消点赞评价
   */
  unlikeReview: (reviewId: number) => Promise<void>;

  /**
   * 切换点赞状态
   */
  toggleLike: (reviewId: number) => Promise<void>;

  /**
   * 回复评价（卖家）
   */
  replyReview: (reviewId: number, content: string) => Promise<void>;

  /**
   * 重置状态
   */
  reset: () => void;

  /**
   * 清除错误
   */
  clearError: () => void;
}

/**
 * 评价状态管理 Store
 */
export const useReviewStore = create<ReviewState>()(
  devtools(
    (set, get) => ({
      // ==================== 初始状态 ====================
      myReviews: [],
      currentGoodsReviews: [],
      currentGoodsId: null,
      totalElements: 0,
      totalPages: 0,
      currentPage: 0,
      loading: false,
      error: null,

      // ==================== 获取我的评价 ====================
      fetchMyReviews: async (params = { page: 0, size: 20 }) => {
        set({ loading: true, error: null });

        try {
          const response = await reviewService.getMyReviews(params);

          set({
            myReviews: response.content || [],
            totalElements: response.totalElements || 0,
            totalPages: response.totalPages || 0,
            currentPage: response.number || 0,
            loading: false,
          });

          console.log('✅ 我的评价列表获取成功:', response.content?.length);
        } catch (error: any) {
          const errorMessage = error.message || '获取评价列表失败';
          set({
            error: errorMessage,
            loading: false,
          });
          console.error('❌ 获取我的评价失败:', error);
          throw error;
        }
      },

      // ==================== 获取商品评价列表 ====================
      fetchGoodsReviews: async (
        goodsId: number,
        params: ReviewListQuery = { page: 0, size: 10 }
      ) => {
        set({ loading: true, error: null, currentGoodsId: goodsId });

        try {
          const response = await reviewService.listReviews(goodsId, params);

          set({
            currentGoodsReviews: response.content || [],
            totalElements: response.totalElements || 0,
            totalPages: response.totalPages || 0,
            currentPage: response.number || 0,
            loading: false,
          });

          console.log(
            `✅ 商品 ${goodsId} 评价列表获取成功:`,
            response.content?.length
          );
        } catch (error: any) {
          const errorMessage = error.message || '获取评价列表失败';
          set({
            error: errorMessage,
            loading: false,
          });
          console.error('❌ 获取商品评价失败:', error);
          throw error;
        }
      },

      // ==================== 创建评价 ====================
      createReview: async (request: CreateReviewRequest) => {
        set({ loading: true, error: null });

        try {
          const reviewId = await reviewService.createReview(request);

          // 创建成功后，刷新我的评价列表
          await get().fetchMyReviews();

          set({ loading: false });
          console.log('✅ 评价创建成功:', reviewId);

          return reviewId;
        } catch (error: any) {
          const errorMessage = error.message || '创建评价失败';
          set({
            error: errorMessage,
            loading: false,
          });
          console.error('❌ 创建评价失败:', error);
          throw error;
        }
      },

      // ==================== 删除评价 ====================
      deleteReview: async (reviewId: number) => {
        set({ loading: true, error: null });

        try {
          await reviewService.deleteReview(reviewId);

          // 删除成功后，从本地列表中移除
          set((state) => ({
            myReviews: state.myReviews.filter((r) => r.id !== reviewId),
            currentGoodsReviews: state.currentGoodsReviews.filter(
              (r) => r.id !== reviewId
            ),
            loading: false,
          }));

          console.log('✅ 评价删除成功:', reviewId);
        } catch (error: any) {
          const errorMessage = error.message || '删除评价失败';
          set({
            error: errorMessage,
            loading: false,
          });
          console.error('❌ 删除评价失败:', error);
          throw error;
        }
      },

      // ==================== 点赞评价 ====================
      likeReview: async (reviewId: number) => {
        try {
          await reviewService.likeReview(reviewId);

          // 乐观更新：立即更新本地状态
          set((state) => {
            const updateReview = (review: ReviewDetail) => {
              if (review.id === reviewId) {
                return {
                  ...review,
                  isLiked: true,
                  likeCount: (review.likeCount || 0) + 1,
                };
              }
              return review;
            };

            return {
              myReviews: state.myReviews.map(updateReview),
              currentGoodsReviews: state.currentGoodsReviews.map(updateReview),
            };
          });

          console.log('✅ 点赞成功:', reviewId);
        } catch (error: any) {
          // 点赞失败，回滚状态
          set((state) => {
            const rollbackReview = (review: ReviewDetail) => {
              if (review.id === reviewId) {
                return {
                  ...review,
                  isLiked: false,
                  likeCount: Math.max((review.likeCount || 0) - 1, 0),
                };
              }
              return review;
            };

            return {
              myReviews: state.myReviews.map(rollbackReview),
              currentGoodsReviews: state.currentGoodsReviews.map(rollbackReview),
              error: error.message || '点赞失败',
            };
          });

          console.error('❌ 点赞失败:', error);
          throw error;
        }
      },

      // ==================== 取消点赞 ====================
      unlikeReview: async (reviewId: number) => {
        try {
          await reviewService.unlikeReview(reviewId);

          // 乐观更新：立即更新本地状态
          set((state) => {
            const updateReview = (review: ReviewDetail) => {
              if (review.id === reviewId) {
                return {
                  ...review,
                  isLiked: false,
                  likeCount: Math.max((review.likeCount || 0) - 1, 0),
                };
              }
              return review;
            };

            return {
              myReviews: state.myReviews.map(updateReview),
              currentGoodsReviews: state.currentGoodsReviews.map(updateReview),
            };
          });

          console.log('✅ 取消点赞成功:', reviewId);
        } catch (error: any) {
          // 取消失败，回滚状态
          set((state) => {
            const rollbackReview = (review: ReviewDetail) => {
              if (review.id === reviewId) {
                return {
                  ...review,
                  isLiked: true,
                  likeCount: (review.likeCount || 0) + 1,
                };
              }
              return review;
            };

            return {
              myReviews: state.myReviews.map(rollbackReview),
              currentGoodsReviews: state.currentGoodsReviews.map(rollbackReview),
              error: error.message || '取消点赞失败',
            };
          });

          console.error('❌ 取消点赞失败:', error);
          throw error;
        }
      },

      // ==================== 切换点赞状态 ====================
      toggleLike: async (reviewId: number) => {
        const review =
          get().myReviews.find((r) => r.id === reviewId) ||
          get().currentGoodsReviews.find((r) => r.id === reviewId);

        if (!review) {
          throw new Error('评价不存在');
        }

        if (review.isLiked) {
          await get().unlikeReview(reviewId);
        } else {
          await get().likeReview(reviewId);
        }
      },

      // ==================== 回复评价 ====================
      replyReview: async (reviewId: number, content: string) => {
        set({ loading: true, error: null });

        try {
          const reply = await reviewService.replyReview(reviewId, content);

          // 更新本地状态：添加回复
          set((state) => {
            const updateReview = (review: ReviewDetail) => {
              if (review.id === reviewId) {
                return {
                  ...review,
                  reply,
                  replyCount: (review.replyCount || 0) + 1,
                };
              }
              return review;
            };

            return {
              myReviews: state.myReviews.map(updateReview),
              currentGoodsReviews: state.currentGoodsReviews.map(updateReview),
              loading: false,
            };
          });

          console.log('✅ 回复成功:', reviewId);
        } catch (error: any) {
          const errorMessage = error.message || '回复失败';
          set({
            error: errorMessage,
            loading: false,
          });
          console.error('❌ 回复评价失败:', error);
          throw error;
        }
      },

      // ==================== 重置状态 ====================
      reset: () => {
        set({
          myReviews: [],
          currentGoodsReviews: [],
          currentGoodsId: null,
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          loading: false,
          error: null,
        });
        console.log('✅ 评价状态已重置');
      },

      // ==================== 清除错误 ====================
      clearError: () => {
        set({ error: null });
      },
    }),
    {
      name: 'ReviewStore', // Redux DevTools 名称
    }
  )
);

export default useReviewStore;
