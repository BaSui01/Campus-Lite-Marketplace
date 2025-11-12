/**
 * ReviewCard - 评价卡片组件
 * @author BaSui 😎
 * @description 展示单条评价信息，支持点赞、回复、编辑、删除等操作
 */

import React, { useMemo } from 'react';
import { StarRating, UserAvatar, Badge } from '@campus/shared/components';
import type { ReviewCardProps } from './types';
import './ReviewCard.css';

/**
 * 格式化时间（相对时间）
 */
const formatRelativeTime = (dateStr: string): string => {
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (days > 7) {
    return date.toLocaleDateString('zh-CN');
  }
  if (days > 0) return `${days}天前`;
  if (hours > 0) return `${hours}小时前`;
  if (minutes > 0) return `${minutes}分钟前`;
  return '刚刚';
};

/**
 * ReviewCard 组件
 */
export const ReviewCard: React.FC<ReviewCardProps> = ({
  review,
  showGoods = false,
  showActions = false,
  onLike,
  onEdit,
  onDelete,
  className = '',
}) => {
  // 买家信息
  const buyerName = useMemo(() => {
    if (review.isAnonymous) {
      return '匿名用户';
    }
    return review.buyer?.nickname || '未知用户';
  }, [review.isAnonymous, review.buyer]);

  const buyerAvatar = useMemo(() => {
    if (review.isAnonymous) {
      return '/default-avatar.png';
    }
    return review.buyer?.avatar || '/default-avatar.png';
  }, [review.isAnonymous, review.buyer]);

  // 格式化时间
  const formattedTime = useMemo(() => {
    return formatRelativeTime(review.createdAt || new Date().toISOString());
  }, [review.createdAt]);

  // 处理点赞
  const handleLike = () => {
    onLike?.(review.id!);
  };

  // 处理编辑
  const handleEdit = () => {
    onEdit?.(review.id!);
  };

  // 处理删除
  const handleDelete = () => {
    onDelete?.(review.id!);
  };

  // 点赞按钮类名
  const likeButtonClassName = useMemo(() => {
    return [
      'review-card__like-btn',
      review.isLiked && 'liked',
    ].filter(Boolean).join(' ');
  }, [review.isLiked]);

  return (
    <div className={`review-card ${className}`}>
      {/* 头部：用户信息 */}
      <div className="review-card__header">
        <div className="review-card__user">
          <UserAvatar
            src={buyerAvatar}
            alt={buyerName}
            size="medium"
          />
          <div className="review-card__user-info">
            <span className="review-card__user-name">{buyerName}</span>
            <div className="review-card__meta">
              <StarRating value={review.rating || 0} readonly size="small" showValue />
              <span className="review-card__time">{formattedTime}</span>
            </div>
          </div>
        </div>

        {/* 操作按钮 */}
        {showActions && (
          <div className="review-card__actions">
            <button
              className="review-card__action-btn"
              onClick={handleEdit}
              aria-label="编辑"
            >
              编辑
            </button>
            <button
              className="review-card__action-btn review-card__action-btn--danger"
              onClick={handleDelete}
              aria-label="删除"
            >
              删除
            </button>
          </div>
        )}
      </div>

      {/* 内容区 */}
      <div className="review-card__content">
        {review.content ? (
          <p className="review-card__text">{review.content}</p>
        ) : (
          <p className="review-card__text review-card__text--empty">
            暂无评价内容
          </p>
        )}
      </div>

      {/* 图片展示 */}
      {review.images && review.images.length > 0 && (
        <div className="review-card__images">
          {review.images.map((image, index) => (
            <img
              key={index}
              src={image}
              alt={`review-image-${index}`}
              className="review-card__image"
              loading="lazy"
            />
          ))}
        </div>
      )}

      {/* 底部：点赞 */}
      <div className="review-card__footer">
        <button
          className={likeButtonClassName}
          onClick={handleLike}
          aria-label="点赞"
        >
          <svg
            className="review-card__like-icon"
            viewBox="0 0 24 24"
            fill="currentColor"
          >
            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
          </svg>
          <span>{review.likeCount || 0}</span>
        </button>
      </div>

      {/* 卖家回复 */}
      {review.reply && (
        <div className="review-card__reply">
          <div className="review-card__reply-header">
            <Badge
              text="卖家回复"
              color="primary"
              size="small"
            />
            <span className="review-card__reply-time">
              {formatRelativeTime(review.reply.createdAt || new Date().toISOString())}
            </span>
          </div>
          <p className="review-card__reply-content">
            {review.reply.content}
          </p>
        </div>
      )}
    </div>
  );
};

export default ReviewCard;
