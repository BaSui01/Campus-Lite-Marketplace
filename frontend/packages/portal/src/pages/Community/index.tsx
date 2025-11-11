/**
 * 社区页面 - 发现校园精彩生活！🌐
 * @author BaSui 😎
 * @description 社区动态、帖子发布、点赞评论、标签筛选
 * @updated 2025-11-08 - 集成标签功能、美化UI
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input, Button, Skeleton, Modal, Tabs, TagSelector, TopicSelector } from '@campus/shared/components';
import type { TagOption, TopicOption } from '@campus/shared/components';
import { postService, tagService, topicService } from '@campus/shared/services';
import type { Tag } from '@campus/shared/services/tag';
import type { Topic } from '@campus/shared/services/topic';
import { useAuthStore, useNotificationStore } from '../../store';
import './Community.css';

// ==================== 类型定义 ====================

interface Post {
  postId: string;
  authorId: string;
  authorName: string;
  authorAvatar?: string;
  content: string;
  images?: string[];
  likeCount: number;
  commentCount: number;
  isLiked: boolean;
  createdAt: string;
}

interface Comment {
  commentId: string;
  postId: string;
  authorId: string;
  authorName: string;
  authorAvatar?: string;
  content: string;
  createdAt: string;
}

/**
 * 社区页面组件
 */
const Community: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();
  const currentUser = useAuthStore((state) => state.user);

  // ==================== 状态管理 ====================

  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [page, setPage] = useState(1);

  // 发布动态弹窗
  const [showPublishModal, setShowPublishModal] = useState(false);
  const [publishContent, setPublishContent] = useState('');
  const [publishImages, setPublishImages] = useState<string[]>([]);
  const [publishTagIds, setPublishTagIds] = useState<number[]>([]);
  const [publishTopicIds, setPublishTopicIds] = useState<number[]>([]); // 新增：话题ID列表
  const [publishing, setPublishing] = useState(false);

  // 评论弹窗
  const [showCommentModal, setShowCommentModal] = useState(false);
  const [currentPost, setCurrentPost] = useState<Post | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentContent, setCommentContent] = useState('');
  const [commenting, setCommenting] = useState(false);

  // 标签筛选
  const [activeTab, setActiveTab] = useState('all');
  const [tags, setTags] = useState<Tag[]>([]);
  const [loadingTags, setLoadingTags] = useState(false);
  const [selectedTagId, setSelectedTagId] = useState<number | null>(null);

  // 话题列表
  const [topics, setTopics] = useState<Topic[]>([]);
  const [hotTags, setHotTags] = useState<TagOption[]>([]);
  const [hotTopics, setHotTopics] = useState<TopicOption[]>([]);

  // ==================== 数据加载 ====================

  /**
   * 加载标签列表
   */
  const loadTags = async () => {
    try {
      setLoadingTags(true);
      // 🚀 调用真实后端 API 获取标签列表
      const response = await tagService.list({
        status: 'ENABLED' as any,
        size: 50,
      });

      if (Array.isArray(response)) {
        setTags(response);
      }
    } catch (err: any) {
      console.error('加载标签失败：', err);
      // 静默失败，不影响主要功能
    } finally {
      setLoadingTags(false);
    }
  };

  /**
   * 加载热门标签
   */
  const loadHotTags = async () => {
    try {
      const hotTagsData = await tagService.getHotTags(10);
      setHotTags(hotTagsData.map(tag => ({
        id: tag.id,
        name: tag.name,
        usageCount: tag.usageCount,
      })));
    } catch (err: any) {
      console.error('加载热门标签失败：', err);
    }
  };

  /**
   * 加载话题列表
   */
  const loadTopics = async () => {
    try {
      const allTopics = await topicService.getAll();
      setTopics(allTopics);
    } catch (err: any) {
      console.error('加载话题失败：', err);
    }
  };

  /**
   * 加载热门话题
   */
  const loadHotTopics = async () => {
    try {
      const hotTopicsData = await topicService.getHotTopics();
      setHotTopics(hotTopicsData.map(topic => ({
        id: topic.id,
        name: topic.name,
        description: topic.description,
        postCount: topic.postCount,
        followerCount: topic.followerCount,
      })));
    } catch (err: any) {
      console.error('加载热门话题失败：', err);
    }
  };

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
      // 🚀 调用真实后端 API 获取帖子列表
      const currentPage = isLoadMore ? page : 0;
      const response = await postService.getPosts({
        page: currentPage,
        pageSize: 10,
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

        // 判断是否还有更多
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

  useEffect(() => {
    loadPosts();
  }, [activeTab]);

  useEffect(() => {
    loadTags(); // 初始化加载标签
    loadHotTags(); // 加载热门标签
    loadTopics(); // 加载话题列表
    loadHotTopics(); // 加载热门话题
  }, []);

  // ==================== 事件处理 ====================

  /**
   * 打开发布动态弹窗
   */
  const handleOpenPublishModal = () => {
    if (!currentUser) {
      toast.warning('请先登录！😰');
      navigate('/login');
      return;
    }
    setShowPublishModal(true);
    setPublishContent('');
    setPublishImages([]);
    setPublishTagIds([]);
  };

  /**
   * 关闭发布动态弹窗
   */
  const handleClosePublishModal = () => {
    setShowPublishModal(false);
    setPublishContent('');
    setPublishImages([]);
    setPublishTagIds([]);
    setPublishTopicIds([]);
  };

  /**
   * 发布动态
   */
  const handlePublishPost = async () => {
    if (!publishContent.trim()) {
      toast.warning('请输入内容！😰');
      return;
    }

    setPublishing(true);

    try {
      // 🚀 调用真实后端 API 发布动态（带标签）
      await postService.createPost({
        title: publishContent.substring(0, 50), // 标题取前50字符
        content: publishContent,
        images: publishImages,
        tagIds: publishTagIds, // 🎯 新增：传递标签ID列表
      });

      toast.success('发布成功！🎉');
      handleClosePublishModal();
      loadPosts(); // 重新加载帖子列表
    } catch (err: any) {
      console.error('发布动态失败：', err);
      toast.error(err.response?.data?.message || '发布动态失败！😭');
    } finally {
      setPublishing(false);
    }
  };

  /**
   * 点赞/取消点赞
   */
  const handleToggleLike = async (post: Post) => {
    try {
      // 乐观更新 UI
      setPosts((prev) =>
        prev.map((p) =>
          p.postId === post.postId
            ? {
                ...p,
                isLiked: !p.isLiked,
                likeCount: p.isLiked ? p.likeCount - 1 : p.likeCount + 1,
              }
            : p
        )
      );

      // 🚀 调用真实后端 API 点赞/取消点赞
      if (post.isLiked) {
        await postService.unlikePost(Number(post.postId));
      } else {
        await postService.likePost(Number(post.postId));
      }
    } catch (err: any) {
      console.error('点赞失败：', err);
      toast.error(err.response?.data?.message || '操作失败！😭');

      // 回滚 UI
      setPosts((prev) =>
        prev.map((p) =>
          p.postId === post.postId
            ? {
                ...p,
                isLiked: post.isLiked,
                likeCount: post.likeCount,
              }
            : p
        )
      );
    }
  };

  /**
   * 打开评论弹窗
   */
  const handleOpenCommentModal = async (post: Post) => {
    setCurrentPost(post);
    setShowCommentModal(true);
    setCommentContent('');

    try {
      // 🚀 调用真实后端 API 获取评论列表
      const response = await postService.getReplies(Number(post.postId), { page: 0, pageSize: 50 });

      if (response.success && response.data) {
        const apiComments: Comment[] = response.data.content.map((c: any) => ({
          commentId: String(c.id),
          postId: post.postId,
          authorId: String(c.userId),
          authorName: c.userName || '未知用户',
          authorAvatar: c.userAvatar,
          content: c.content,
          createdAt: c.createTime,
        }));

        setComments(apiComments);
      }
    } catch (err: any) {
      console.error('加载评论失败：', err);
      toast.error(err.response?.data?.message || '加载评论失败！😭');
    }
  };

  /**
   * 关闭评论弹窗
   */
  const handleCloseCommentModal = () => {
    setShowCommentModal(false);
    setCurrentPost(null);
    setComments([]);
    setCommentContent('');
  };

  /**
   * 发布评论
   */
  const handlePublishComment = async () => {
    if (!commentContent.trim()) {
      toast.warning('请输入评论内容！😰');
      return;
    }

    if (!currentPost) return;

    setCommenting(true);

    try {
      // 🚀 调用真实后端 API 发布评论
      await postService.createReply({
        postId: Number(currentPost.postId),
        content: commentContent,
      });

      toast.success('评论成功！💬');
      setCommentContent('');

      // 重新加载评论列表
      handleOpenCommentModal(currentPost);

      // 更新帖子评论数
      setPosts((prev) =>
        prev.map((p) =>
          p.postId === currentPost.postId ? { ...p, commentCount: p.commentCount + 1 } : p
        )
      );
    } catch (err: any) {
      console.error('发布评论失败：', err);
      toast.error(err.response?.data?.message || '发布评论失败！😭');
    } finally {
      setCommenting(false);
    }
  };

  /**
   * 查看帖子详情
   */
  const handleViewPost = (postId: string) => {
    navigate(`/posts/${postId}`);
  };

  /**
   * 加载更多
   */
  const handleLoadMore = () => {
    setPage((prev) => prev + 1);
    loadPosts(true);
  };

  /**
   * 切换标签
   */
  const handleTabChange = (value: string) => {
    setActiveTab(value);
    setPage(1);
    setHasMore(true);
  };

  /**
   * 选择标签筛选
   */
  const handleSelectTag = (tagId: number | null) => {
    setSelectedTagId(tagId);
    setPage(1);
    setHasMore(true);
    setPosts([]);
    loadPosts(false, tagId); // 立即加载该标签的帖子
  };

  /**
   * 切换发布帖子的标签
   */
  const handleTogglePublishTag = (tagId: number) => {
    setPublishTagIds((prev) => {
      if (prev.includes(tagId)) {
        // 已选中，取消选择
        return prev.filter((id) => id !== tagId);
      } else {
        // 未选中，添加选择（最多10个）
        if (prev.length >= 10) {
          toast.warning('最多只能选择 10 个标签！😰');
          return prev;
        }
        return [...prev, tagId];
      }
    });
  };

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

  // ==================== 渲染 ====================

  return (
    <div className="community-page">
      <div className="community-container">
        {/* ==================== 顶部操作栏 ==================== */}
        <div className="community-header">
          <h1 className="community-header__title">🌐 校园社区</h1>
          <Button type="primary" size="large" onClick={handleOpenPublishModal}>
            ✍️ 发布动态
          </Button>
        </div>

        {/* ==================== 热门标签 ==================== */}
        {tags.length > 0 && (
          <div className="community-tags-section">
            <div className="tags-header">
              <span className="tags-title">🏷️ 热门标签</span>
              <span className="tags-subtitle">点击筛选相关内容</span>
            </div>
            <div className="tags-list">
              {/* "全部"标签 */}
              <button
                className={`tag-item ${selectedTagId === null ? 'tag-item--active' : ''}`}
                onClick={() => handleSelectTag(null)}
              >
                <span className="tag-name">全部</span>
              </button>

              {/* 热门标签列表 */}
              {tags.slice(0, 10).map((tag) => (
                <button
                  key={tag.id}
                  className={`tag-item ${selectedTagId === tag.id ? 'tag-item--active' : ''}`}
                  onClick={() => handleSelectTag(tag.id)}
                >
                  <span className="tag-name">{tag.name}</span>
                  {tag.hotCount > 0 && (
                    <span className="tag-count">{tag.hotCount}</span>
                  )}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* ==================== 标签筛选 ==================== */}
        <div className="community-tabs">
          <Tabs
            defaultValue="all"
            onChange={handleTabChange}
            tabs={[
              { label: '🔥 全部', value: 'all' },
              { label: '📝 帖子', value: 'post' },
              { label: '💬 讨论', value: 'discussion' },
              { label: '📸 分享', value: 'share' },
            ]}
          />
        </div>

        {/* ==================== 帖子列表 ==================== */}
        <div className="community-posts">
          {loading ? (
            <Skeleton type="card" count={3} animation="wave" />
          ) : posts.length === 0 ? (
            <div className="community-empty">
              <div className="empty-icon">📭</div>
              <p className="empty-text">还没有动态</p>
              <p className="empty-tip">快来发布第一条动态吧！</p>
            </div>
          ) : (
            <>
              {posts.map((post) => (
                <div
                  key={post.postId}
                  className="post-card"
                  onClick={() => handleViewPost(post.postId)}
                >
                  {/* 用户信息 */}
                  <div className="post-card__header">
                    <div className="post-card__avatar">
                      {post.authorAvatar ? (
                        <img src={post.authorAvatar} alt={post.authorName} />
                      ) : (
                        <span>👤</span>
                      )}
                    </div>
                    <div className="post-card__info">
                      <div className="post-card__name">{post.authorName}</div>
                      <div className="post-card__time">{formatTime(post.createdAt)}</div>
                    </div>
                  </div>

                  {/* 帖子内容 */}
                  <div className="post-card__content">
                    <p>{post.content}</p>
                  </div>

                  {/* 图片 */}
                  {post.images && post.images.length > 0 && (
                    <div className="post-card__images">
                      {post.images.map((image, index) => (
                        <img key={index} src={image} alt={`图片${index + 1}`} />
                      ))}
                    </div>
                  )}

                  {/* 操作栏 */}
                  <div className="post-card__actions">
                    <button
                      className={`post-card__action ${post.isLiked ? 'active' : ''}`}
                      onClick={(e) => {
                        e.stopPropagation();
                        handleToggleLike(post);
                      }}
                    >
                      {post.isLiked ? '❤️' : '🤍'} {post.likeCount}
                    </button>
                    <button
                      className="post-card__action"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleOpenCommentModal(post);
                      }}
                    >
                      💬 {post.commentCount}
                    </button>
                  </div>
                </div>
              ))}

              {/* 加载更多 */}
              {hasMore && (
                <div className="community-load-more">
                  <Button onClick={handleLoadMore} loading={loadingMore}>
                    {loadingMore ? '加载中...' : '加载更多'}
                  </Button>
                </div>
              )}

              {!hasMore && (
                <div className="community-no-more">
                  <p>已经到底啦！😊</p>
                </div>
              )}
            </>
          )}
        </div>
      </div>

      {/* ==================== 发布动态弹窗 ==================== */}
      {showPublishModal && (
        <Modal onClose={handleClosePublishModal} title="✍️ 发布动态">
          <div className="publish-modal">
            <textarea
              className="publish-modal__textarea"
              placeholder="分享你的生活...（最多500字）"
              value={publishContent}
              onChange={(e) => setPublishContent(e.target.value)}
              maxLength={500}
              rows={6}
            />

            {/* 标签选择区域 */}
            {tags.length > 0 && (
              <div className="publish-modal__tags">
                <div className="publish-modal__tags-header">
                  <span className="publish-modal__tags-title">🏷️ 选择标签</span>
                  <span className="publish-modal__tags-hint">（最多选择10个，已选{publishTagIds.length}个）</span>
                </div>
                <div className="publish-modal__tags-list">
                  {tags.slice(0, 20).map((tag) => (
                    <button
                      key={tag.id}
                      type="button"
                      className={`publish-tag-item ${publishTagIds.includes(tag.id) ? 'publish-tag-item--active' : ''}`}
                      onClick={() => handleTogglePublishTag(tag.id)}
                    >
                      {tag.name}
                    </button>
                  ))}
                </div>
              </div>
            )}

            <div className="publish-modal__footer">
              <Button onClick={handleClosePublishModal}>取消</Button>
              <Button
                type="primary"
                onClick={handlePublishPost}
                loading={publishing}
                disabled={!publishContent.trim()}
              >
                {publishing ? '发布中...' : '发布'}
              </Button>
            </div>
          </div>
        </Modal>
      )}

      {/* ==================== 评论弹窗 ==================== */}
      {showCommentModal && currentPost && (
        <Modal onClose={handleCloseCommentModal} title="💬 评论">
          <div className="comment-modal">
            {/* 评论列表 */}
            <div className="comment-modal__list">
              {comments.length === 0 ? (
                <div className="comment-modal__empty">还没有评论，快来抢沙发吧！</div>
              ) : (
                comments.map((comment) => (
                  <div key={comment.commentId} className="comment-item">
                    <div className="comment-item__avatar">
                      {comment.authorAvatar ? (
                        <img src={comment.authorAvatar} alt={comment.authorName} />
                      ) : (
                        <span>👤</span>
                      )}
                    </div>
                    <div className="comment-item__content">
                      <div className="comment-item__header">
                        <span className="comment-item__name">{comment.authorName}</span>
                        <span className="comment-item__time">{formatTime(comment.createdAt)}</span>
                      </div>
                      <p className="comment-item__text">{comment.content}</p>
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* 评论输入框 */}
            <div className="comment-modal__input">
              <Input
                type="text"
                size="large"
                placeholder="说点什么..."
                value={commentContent}
                onChange={(e) => setCommentContent(e.target.value)}
                maxLength={200}
              />
              <Button
                type="primary"
                size="large"
                onClick={handlePublishComment}
                loading={commenting}
                disabled={!commentContent.trim()}
              >
                {commenting ? '发送中...' : '发送'}
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default Community;
