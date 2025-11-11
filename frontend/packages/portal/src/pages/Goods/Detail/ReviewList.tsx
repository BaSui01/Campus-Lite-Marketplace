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

  /**
   * 外部受控：星级筛选（1-5），未传表示不受控
   */
  rating?: number;

  /**
   * 外部受控：只看有图
   */
  hasImages?: boolean;

  /**
   * 受控回调：星级筛选变更
   */
  onRatingChange?: (rating: number | undefined) => void;

  /**
   * 受控回调：只看有图变更
   */
  onHasImagesChange?: (hasImages: boolean) => void;
}

/**
 * 排序选项
 */
type SortOption = 'time' | 'like' | 'image_first';

/**
 * ReviewList 组件
 */
export const ReviewList: React.FC<ReviewListProps> = ({
  goodsId,
  rating: ratingProp,
  hasImages: hasImagesProp,
  onRatingChange,
  onHasImagesChange,
}) => {
  // 状态管理
  const [rating, setRating] = useState<number | undefined>(ratingProp);
  const [hasImages, setHasImages] = useState<boolean>(!!hasImagesProp);
  const [sortBy, setSortBy] = useState<SortOption>('time');
  const [group, setGroup] = useState<'positive' | 'neutral' | 'negative' | undefined>(undefined);
  const [timeAsc, setTimeAsc] = useState(false);
  const [page, setPage] = useState(0);
  const size = 10;

  // Zustand store
  const { toggleLike } = useReviewStore();

  // 同步受控 props → 本地状态
  useEffect(() => {
    if (ratingProp !== undefined) setRating(ratingProp);
  }, [ratingProp]);
  useEffect(() => {
    if (typeof hasImagesProp === 'boolean') setHasImages(hasImagesProp);
  }, [hasImagesProp]);

  // 监听来自父级的全局筛选事件（退路方案，便于从统计组件快速联动）
  useEffect(() => {
    const handler = (e: any) => {
      const next: number | undefined = e?.detail?.rating;
      setGroup(undefined);
      setRating(next);
      onRatingChange?.(next);
      setPage(0);
    };
    window.addEventListener('goods-review:setRating', handler as any);
    return () => window.removeEventListener('goods-review:setRating', handler as any);
  }, [onRatingChange]);

  // 监听分组事件（好评/中评/差评）
  useEffect(() => {
    const handler = (e: any) => {
      const g = e?.detail?.group as 'positive' | 'neutral' | 'negative' | undefined;
      setRating(undefined);
      setGroup(g);
      setPage(0);
    };
    window.addEventListener('goods-review:setGroup', handler as any);
    return () => window.removeEventListener('goods-review:setGroup', handler as any);
  }, []);

  // 首次挂载时读取 URL Hash（如 #rating=5）
  useEffect(() => {
    const hash = window.location.hash || '';
    const m = hash.match(/rating=(\d)/);
    if (m) {
      const initial = Number(m[1]);
      if (initial >= 1 && initial <= 5) {
        setRating(initial);
        onRatingChange?.(initial);
        setPage(0);
      }
    }
    const mg = hash.match(/group=(positive|neutral|negative)/);
    if (mg) {
      setGroup(mg[1] as any);
      setRating(undefined);
      setPage(0);
    }
  // 仅初始化一次
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 评价列表查询
  const {
    data: reviewData,
    isLoading,
    refetch,
  } = useQuery({
    queryKey: ['reviews', 'list', goodsId, rating, group, hasImages, sortBy, page],
    queryFn: async () => {
      const response = await reviewService.listReviews(goodsId, {
        page,
        size,
        rating,
        group,
        hasImages,
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
    const next = key === 'all' ? undefined : Number(key);
    setRating(next);
    onRatingChange?.(next);
    setPage(0); // 重置页码
  };

  // 切换排序
  const handleSortChange = (newSort: SortOption) => {
    setSortBy(newSort);
    setPage(0); // 重置页码
  };

  // 切换“只看有图”
  const handleToggleHasImages = () => {
    const next = !hasImages;
    setHasImages(next);
    onHasImagesChange?.(next);
    setPage(0);
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
  const displayedReviews = sortBy === 'time' && timeAsc ? [...reviews].reverse() : reviews;
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
              className={`review-list__sort-btn ${sortBy === 'time' && !timeAsc ? 'active' : ''}`}
              onClick={() => { setSortBy('time'); setTimeAsc(false); setPage(0); }}
              title="最新评论置顶"
            >
              最新
            </button>
            <button
              className={`review-list__sort-btn ${sortBy === 'time' && timeAsc ? 'active' : ''}`}
              onClick={() => { setSortBy('time'); setTimeAsc(true); setPage(0); }}
              title="最早评论优先"
            >
              最早
            </button>
            <button
              className={`review-list__sort-btn ${sortBy === 'like' ? 'active' : ''}`}
              onClick={() => handleSortChange('like')}
            >
              按点赞
            </button>
            <button
              className={`review-list__sort-btn ${sortBy === 'image_first' ? 'active' : ''}`}
              onClick={() => handleSortChange('image_first')}
              title="有图优先（页内重排）"
            >
              有图优先
            </button>
            <button
              className={`review-list__sort-btn ${hasImages ? 'active' : ''}`}
              onClick={handleToggleHasImages}
              title="只看包含图片的评价"
            >
              只看有图
            </button>
          </div>
        )}
      </div>

      {/* 好评/中评/差评 快捷筛选 Chips */}
      {totalElements > 0 && (
        <div className="review-list__chips">
          <button
            className={`chip ${!group ? 'active' : ''}`}
            onClick={() => { setGroup(undefined); setRating(undefined); history.replaceState(null, '', '#reviews'); setPage(0); }}
          >
            全部
          </button>
          <button
            className={`chip ${group === 'positive' ? 'active' : ''}`}
            onClick={() => { setGroup('positive'); setRating(undefined); history.replaceState(null, '', '#group=positive'); setPage(0); }}
          >
            好评
          </button>
          <button
            className={`chip ${group === 'neutral' ? 'active' : ''}`}
            onClick={() => { setGroup('neutral'); setRating(undefined); history.replaceState(null, '', '#group=neutral'); setPage(0); }}
          >
            中评
          </button>
          <button
            className={`chip ${group === 'negative' ? 'active' : ''}`}
            onClick={() => { setGroup('negative'); setRating(undefined); history.replaceState(null, '', '#group=negative'); setPage(0); }}
          >
            差评
          </button>
        </div>
      )}

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
            {displayedReviews.map((review) => (
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
