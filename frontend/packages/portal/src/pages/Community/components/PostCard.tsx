/**
 * PostCard 组件 - 帖子卡片（Linux.do 风格）
 * @author BaSui 😎
 * @description 单个帖子展示卡片，支持点赞、评论、查看详情
 */

import React, { useState } from 'react';
import ImagePreview from '../../../components/ImagePreview';
import './PostCard.css';

export interface Post {
  postId: string;
  authorId: string;
  authorName: string;
  authorAvatar?: string;
  title?: string;
  content: string;
  images?: string[];
  tags?: Array<{ id: number; name: string }>;
  likeCount: number;
  commentCount: number;
  viewCount?: number;
  isLiked: boolean;
  isPinned?: boolean;
  createdAt: string;
}

interface PostCardProps {
  post: Post;
  onLike: (post: Post) => void;
  onComment: (post: Post) => void;
  onView: (postId: string) => void;
  showImages?: boolean;
}

/**
 * 格式化时间
 */
const formatTime = (time?: string) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  // 1分钟内
  if (diff < 60 * 1000) {
    return '刚刚';
  }

  // 1小时内
  if (diff < 60 * 60 * 1000) {
    const minutes = Math.floor(diff / (60 * 1000));
    return `${minutes}分钟前`;
  }

  // 今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  }

  // 昨天
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
  }

  // 其他
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' });
};

const PostCard: React.FC<PostCardProps> = ({
  post,
  onLike,
  onComment,
  onView,
  showImages = true,
}) => {
  const [showImagePreview, setShowImagePreview] = useState(false);
  const [previewImageIndex, setPreviewImageIndex] = useState(0);

  /**
   * 打开图片预览
   */
  const handleImageClick = (index: number, e: React.MouseEvent) => {
    e.stopPropagation();
    setPreviewImageIndex(index);
    setShowImagePreview(true);
  };

  return (
    <>
      <div
        className={`post-card ${post.isPinned ? 'post-card--pinned' : ''}`}
        onClick={() => onView(post.postId)}
      >
      {/* 置顶标识 */}
      {post.isPinned && (
        <div className="post-card__pinned-badge">
          📌 置顶
        </div>
      )}

      {/* 帖子头部 */}
      <div className="post-card__header">
        <div className="post-card__avatar">
          {post.authorAvatar ? (
            <img src={post.authorAvatar} alt={post.authorName} />
          ) : (
            <span className="post-card__avatar-fallback">👤</span>
          )}
        </div>
        <div className="post-card__info">
          <div className="post-card__author">{post.authorName}</div>
          <div className="post-card__time">{formatTime(post.createdAt)}</div>
        </div>
      </div>

      {/* 帖子标题（如果有） */}
      {post.title && (
        <h3 className="post-card__title">{post.title}</h3>
      )}

      {/* 帖子内容 */}
      <div className="post-card__content">
        <p>{post.content.length > 200 ? `${post.content.substring(0, 200)}...` : post.content}</p>
      </div>

      {/* 帖子图片 */}
      {showImages && post.images && post.images.length > 0 && (
        <div className={`post-card__images post-card__images--count-${Math.min(post.images.length, 3)}`}>
          {post.images.slice(0, 3).map((image, index) => (
            <div
              key={index}
              className="post-card__image"
              onClick={(e) => handleImageClick(index, e)}
              style={{ cursor: 'pointer' }}
            >
              <img src={image} alt={`图片${index + 1}`} />
              {index === 2 && post.images && post.images.length > 3 && (
                <div className="post-card__image-overlay">
                  +{post.images.length - 3}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* 帖子标签 */}
      {post.tags && post.tags.length > 0 && (
        <div className="post-card__tags">
          {post.tags.slice(0, 5).map((tag) => (
            <span key={tag.id} className="post-card__tag">
              #{tag.name}
            </span>
          ))}
        </div>
      )}

      {/* 帖子底部 - 互动栏 */}
      <div className="post-card__footer">
        <button
          className={`post-card__action ${post.isLiked ? 'post-card__action--active' : ''}`}
          onClick={(e) => {
            e.stopPropagation();
            onLike(post);
          }}
        >
          <span className="post-card__action-icon">{post.isLiked ? '❤️' : '🤍'}</span>
          <span className="post-card__action-text">{post.likeCount}</span>
        </button>
        
        <button
          className="post-card__action"
          onClick={(e) => {
            e.stopPropagation();
            onComment(post);
          }}
        >
          <span className="post-card__action-icon">💬</span>
          <span className="post-card__action-text">{post.commentCount}</span>
        </button>

        {post.viewCount !== undefined && (
          <div className="post-card__stat">
            <span className="post-card__stat-icon">👁️</span>
            <span className="post-card__stat-text">{post.viewCount}</span>
          </div>
        )}
      </div>
    </div>

      {/* 图片预览 */}
      {showImagePreview && post.images && post.images.length > 0 && (
        <ImagePreview
          images={post.images}
          currentIndex={previewImageIndex}
          onClose={() => setShowImagePreview(false)}
          onPrev={() => setPreviewImageIndex((prev) => Math.max(0, prev - 1))}
          onNext={() => setPreviewImageIndex((prev) => Math.min(post.images!.length - 1, prev + 1))}
        />
      )}
    </>
  );
};

export default PostCard;
