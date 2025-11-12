/**
 * ReviewStats - 商品评价统计组件
 * @author BaSui 😎
 * @description 展示平均评分、好评率与三维评分（质量/服务/物流）
 */

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { StarRating, Skeleton } from '@campus/shared/components';
import { reviewService } from '@campus/shared/services';
import type { ReviewStatisticsDTO } from '@campus/shared/api/models';
import './ReviewStats.css';

interface ReviewStatsProps {
  goodsId: number;
  initialStats?: ReviewStatisticsDTO;
  /**
   * 点击星级时联动列表筛选
   */
  onSelectRating?: (rating: number | undefined) => void;
  /**
   * 点击“好评/中评/差评”时联动列表筛选
   */
  onSelectGroup?: (group: 'positive' | 'neutral' | 'negative') => void;
}

export const ReviewStats: React.FC<ReviewStatsProps> = ({ goodsId, initialStats, onSelectRating, onSelectGroup }) => {
  const { data: stats, isLoading } = useQuery({
    queryKey: ['reviews', 'stats', goodsId],
    queryFn: async () => {
      // 优先使用 initialStats（来自商品详情），否则请求接口
      if (initialStats) return initialStats;
      return await reviewService.getGoodsReviewStatistics(goodsId);
    },
    staleTime: 5 * 60 * 1000,
  });

  if (isLoading) {
    return (
      <div className="review-stats">
        <Skeleton type="paragraph" count={2} />
      </div>
    );
  }

  if (!stats) {
    return null;
  }

  const positiveRatePct = Math.round((stats.positiveRate || 0) * 100);
  const avgRating = stats.avgRating || 0;

  return (
    <div className="review-stats">
      {/* 概览 */}
      <div className="review-stats__overview">
        <div className="review-stats__score">
          <div className="review-stats__score-value">{avgRating.toFixed(1)}</div>
          <div onClick={() => onSelectRating?.(undefined)} style={{ cursor: onSelectRating ? 'pointer' : 'default' }} title="查看全部评价">
            <StarRating value={avgRating} readonly size="large" />
          </div>
          <div className="review-stats__count">
            共 {stats.totalCount || 0} 条评价
          </div>
        </div>
        <div className="review-stats__rate">
          <div className="review-stats__rate-title">好评率</div>
          <div className="review-stats__rate-value">{positiveRatePct}%</div>
          <div className="review-stats__rate-bar">
            <div
              className="review-stats__rate-bar-fill"
              style={{ width: `${positiveRatePct}%` }}
            />
          </div>
          <div className="review-stats__rate-meta">
            好评 {stats.positiveCount || 0} / 中评 {stats.neutralCount || 0} / 差评 {stats.negativeCount || 0}
          </div>
        </div>
      </div>

      {/* 星级快捷筛选 5→1 */}
      <div className="review-stats__dimensions" style={{ marginTop: 8 }}>
        {[5,4,3,2,1].map((s) => (
          <button
            key={s}
            className="review-stats__dim-item"
            onClick={() => onSelectRating?.(s)}
            title={`查看 ${s} 星评价`}
            style={{ cursor: onSelectRating ? 'pointer' : 'default' }}
          >
            <div className="review-stats__dim-label">{s} 星</div>
            <StarRating value={s} readonly size="small" />
          </button>
        ))}
      </div>

      {/* 三维评分 */}
      <div className="review-stats__dimensions">
        {/* 好评/中评/差评 快捷筛选 */}
        <button
          className="review-stats__dim-item"
          onClick={() => onSelectGroup?.('positive')}
          title="查看好评（4-5星）"
          style={{ cursor: onSelectGroup ? 'pointer' : 'default' }}
        >
          <div className="review-stats__dim-label">好评</div>
          <div className="review-stats__dim-value">4-5★</div>
        </button>
        <button
          className="review-stats__dim-item"
          onClick={() => onSelectGroup?.('neutral')}
          title="查看中评（3星）"
          style={{ cursor: onSelectGroup ? 'pointer' : 'default' }}
        >
          <div className="review-stats__dim-label">中评</div>
          <div className="review-stats__dim-value">3★</div>
        </button>
        <button
          className="review-stats__dim-item"
          onClick={() => onSelectGroup?.('negative')}
          title="查看差评（1-2星）"
          style={{ cursor: onSelectGroup ? 'pointer' : 'default' }}
        >
          <div className="review-stats__dim-label">差评</div>
          <div className="review-stats__dim-value">1-2★</div>
        </button>

        <div className="review-stats__dim-item">
          <div className="review-stats__dim-label">质量</div>
          <StarRating value={stats.qualityScore || 0} readonly size="small" />
          <div className="review-stats__dim-value">{(stats.qualityScore || 0).toFixed(1)}</div>
        </div>
        <div className="review-stats__dim-item">
          <div className="review-stats__dim-label">服务</div>
          <StarRating value={stats.serviceScore || 0} readonly size="small" />
          <div className="review-stats__dim-value">{(stats.serviceScore || 0).toFixed(1)}</div>
        </div>
        <div className="review-stats__dim-item">
          <div className="review-stats__dim-label">物流</div>
          <StarRating value={stats.logisticsScore || 0} readonly size="small" />
          <div className="review-stats__dim-value">{(stats.logisticsScore || 0).toFixed(1)}</div>
        </div>
      </div>
    </div>
  );
};

export default ReviewStats;
