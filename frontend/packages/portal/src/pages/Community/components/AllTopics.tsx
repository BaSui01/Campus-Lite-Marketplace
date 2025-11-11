/**
 * 全部话题视图 - 默认首页
 * @author BaSui 😎
 * @description 显示所有话题的帖子，支持分页加载
 */

import React, { useState, useEffect } from 'react';
import { postService } from '@campus/shared/services';
import { useNotificationStore } from '../../../store';
import PostList from './PostList';
import type { Post } from './PostCard';

export interface AllTopicsProps {
  selectedTopicId: number | null;
  selectedTagId: number | null;
  onLike: (post: Post) => void;
  onComment: (post: Post) => void;
  onView: (postId: string) => void;
}

/**
 * 全部话题视图组件
 */
const AllTopics: React.FC<AllTopicsProps> = ({
  selectedTopicId,
  selectedTagId,
  onLike,
  onComment,
  onView,
}) => {
  const toast = useNotificationStore();
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [page, setPage] = useState(0);

  /**
   * 加载帖子列表
   */
  const loadPosts = async (isLoadMore = false) => {
    if (isLoadMore) {
      setLoadingMore(true);
    } else {
      setLoading(true);
    }

    try {
      const response = await postService.getPosts({
        page: isLoadMore ? page : 0,
        pageSize: 10,
        topicId: selectedTopicId || undefined,
        tagId: selectedTagId || undefined,
      });

      if (response.success && response.data) {
        const apiPosts: Post[] = response.data.content.map((p: any) => ({
          postId: String(p.id),
          authorId: String(p.userId),
          authorName: p.userName || '未知用户',
          authorAvatar: p.userAvatar,
          content: p.content,
          images: p.images || [],
          likeCount: p.likeCount || 0,
          commentCount: p.commentCount || 0,
          isLiked: p.isLiked || false,
          createdAt: p.createTime,
        }));

        if (isLoadMore) {
          setPosts((prev) => [...prev, ...apiPosts]);
        } else {
          setPosts(apiPosts);
        }

        setHasMore(response.data.content.length >= 10);
      }
    } catch (err: any) {
      console.error('加载帖子失败：', err);
      toast.error(err.response?.data?.message || '加载帖子失败！😭');
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  };

  // 初始加载
  useEffect(() => {
    setPage(0);
    setPosts([]);
    loadPosts();
  }, [selectedTopicId, selectedTagId]);

  /**
   * 加载更多
   */
  const handleLoadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    loadPosts(true);
  };

  return (
    <PostList
      posts={posts}
      loading={loading}
      loadingMore={loadingMore}
      hasMore={hasMore}
      onLike={onLike}
      onComment={onComment}
      onView={onView}
      onLoadMore={handleLoadMore}
      emptyText="还没有帖子"
      emptyTip="快来发布第一条帖子吧！"
    />
  );
};

export default AllTopics;
