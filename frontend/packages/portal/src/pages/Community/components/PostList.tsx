/**
 * 帖子列表组件 - 通用可复用组件
 * @author BaSui 😎
 * @description 支持不同筛选条件的帖子列表
 */

import React from 'react';
import { Button, Skeleton } from '@campus/shared/components';
import PostCard from './PostCard';
import type { Post } from './PostCard';

export interface PostListProps {
  posts: Post[];
  loading: boolean;
  loadingMore: boolean;
  hasMore: boolean;
  onLike: (post: Post) => void;
  onComment: (post: Post) => void;
  onView: (postId: string) => void;
  onLoadMore: () => void;
  emptyText?: string;
  emptyTip?: string;
}

/**
 * 通用帖子列表组件
 */
const PostList: React.FC<PostListProps> = ({
  posts,
  loading,
  loadingMore,
  hasMore,
  onLike,
  onComment,
  onView,
  onLoadMore,
  emptyText = '还没有动态',
  emptyTip = '快来发布第一条动态吧！',
}) => {
  if (loading) {
    return <Skeleton type="card" count={3} animation="wave" />;
  }

  if (posts.length === 0) {
    return (
      <div className="community-empty">
        <div className="empty-icon">📭</div>
        <p className="empty-text">{emptyText}</p>
        <p className="empty-tip">{emptyTip}</p>
      </div>
    );
  }

  return (
    <>
      {posts.map((post) => (
        <PostCard
          key={post.postId}
          post={post}
          onLike={onLike}
          onComment={onComment}
          onView={onView}
        />
      ))}

      {/* 加载更多 */}
      {hasMore && (
        <div className="community-load-more">
          <Button onClick={onLoadMore} loading={loadingMore}>
            {loadingMore ? '加载中...' : '加载更多'}
          </Button>
        </div>
      )}

      {!hasMore && posts.length > 0 && (
        <div className="community-no-more">
          <p>已经到底啦！😊</p>
        </div>
      )}
    </>
  );
};

export default PostList;
