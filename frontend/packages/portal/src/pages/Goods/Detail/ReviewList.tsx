/**
 * ReviewList - 商品评价列表组件
 * @author BaSui 😎
 * @description 展示商品的所有评价，支持筛选、排序、分页、点赞
 */

import React, { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Tabs, Empty, Skeleton, Pagination } from '@campus/shared/components';
import { reviewService } from '@campus/shared/services';;
import { useReviewStore } from '../../../store/useReviewStore';
import { ReviewCard } from '../../../components/ReviewCard';
import type { TabItem } from '@campus/shared/components';
import './ReviewList.css';

/**
 * ReviewList Props
 */
interface ReviewListProps {
  /**
   * 商品ID
   */
  goodsId: number;
}

/**
 * 排序选项
 */
type SortOption = 'time' | 'like';

/**
 * ReviewList 组件
 */
export const ReviewList: React.FC<ReviewListProps> = ({ goodsId }) => {
  // 状态管理
  const [rating, setRating] = useState<number | undefined>(undefined);
  const [sortBy, setSortBy] = useState<SortOption>('time');
  const [page, setPage] = useState(0);
  const size = 10;

  // Zustand store
  const { toggleLike } = useReviewStore();

  // 评价列表查询
  const {
    data: reviewData,
    isLoading,
    refetch,
  } = useQuery({
    queryKey: ['reviews', 'list', goodsId, rating, sortBy, page],
    queryFn: async () => {
      const response = await reviewService.listReviews(goodsId, {
        page,
        size,
        rating,
        sortBy,
      });
      return response;
    },
    staleTime: 1 * 60 * 1000, // 1分钟缓存
  });

  // Tabs 配置
  const tabs: TabItem[] = [
    { key: 'all', label: '全部' },
    { key: '5', label: '5星' },
    { key: '4', label: '4星' },
    { key: '3', label: '3星' },
    { key: '2', label: '2星' },
    { key: '1', label: '1星' },
  ];

  // 当前 Tab
  const activeTab = rating ? String(rating) : 'all';

  // 切换 Tab
  const handleTabChange = (key: string) => {
    setRating(key === 'all' ? undefined : Number(key));
    setPage(0); // 重置页码
  };

  // 切换排序
  const handleSortChange = (newSort: SortOption) => {
    setSortBy(newSort);
    setPage(0); // 重置页码
  };

  // 切换页码
  const handlePageChange = (newPage: number) => {
    setPage(newPage);
    // 滚动到评价区域顶部
    document.getElementById('review-list')?.scrollIntoView({ behavior: 'smooth' });
  };

  // 处理点赞
  const handleLike = async (reviewId: number) => {
    try {
      await toggleLike(reviewId);
      // 刷新列表
      refetch();
    } catch (error) {
      console.error('点赞失败:', error);
    }
  };

  // Loading 状态
  if (isLoading) {
    return (
      <div className="review-list" id="review-list">
        <div className="review-list__header">
          <h2 className="review-list__title">商品评价</h2>
        </div>
        <Skeleton type="list" count={3} />
      </div>
    );
  }

  // 评价列表
  const reviews = reviewData?.content || [];
  const totalPages = reviewData?.totalPages || 0;
  const totalElements = reviewData?.totalElements || 0;

  return (
    <div className="review-list" id="review-list">
      {/* 头部 */}
      <div className="review-list__header">
        <h2 className="review-list__title">
          商品评价
          {totalElements > 0 && (
            <span className="review-list__count">({totalElements})</span>
          )}
        </h2>

        {/* 排序选择 */}
        {totalElements > 0 && (
          <div className="review-list__sort">
            <button
              className={`review-list__sort-btn ${sortBy === 'time' ? 'active' : ''}`}
              onClick={() => handleSortChange('time')}
            >
              按时间
            </button>
            <button
              className={`review-list__sort-btn ${sortBy === 'like' ? 'active' : ''}`}
              onClick={() => handleSortChange('like')}
            >
              按点赞
            </button>
          </div>
        )}
      </div>

      {/* Tabs 筛选 */}
      {totalElements > 0 && (
        <div className="review-list__tabs">
          <Tabs
            items={tabs}
            activeKey={activeTab}
            onChange={handleTabChange}
            size="medium"
          />
        </div>
      )}

      {/* 评价列表 */}
      {reviews.length > 0 ? (
        <>
          <div className="review-list__items">
            {reviews.map((review) => (
              <ReviewCard
                key={review.id}
                review={review}
                onLike={handleLike}
              />
            ))}
          </div>

          {/* 分页 */}
          {totalPages > 1 && (
            <div className="review-list__pagination">
              <Pagination
                current={page + 1}
                total={totalElements}
                pageSize={size}
                onChange={(newPage) => handlePageChange(newPage - 1)}
              />
            </div>
          )}
        </>
      ) : (
        <Empty
          icon="💬"
          title="暂无评价"
          description="快来做第一个评价的人吧！"
        />
      )}
    </div>
  );
};

export default ReviewList;
