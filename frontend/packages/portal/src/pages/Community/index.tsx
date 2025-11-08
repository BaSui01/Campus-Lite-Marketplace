/**
 * 社区页面 - 发现校园精彩生活！🌐
 * @author BaSui 😎
 * @description 社区动态、帖子发布、点赞评论
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input, Button, Skeleton, Modal, Tabs } from '@campus/shared/components';
import { postService } from '@campus/shared/services';;
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
  const [publishing, setPublishing] = useState(false);

  // 评论弹窗
  const [showCommentModal, setShowCommentModal] = useState(false);
  const [currentPost, setCurrentPost] = useState<Post | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentContent, setCommentContent] = useState('');
  const [commenting, setCommenting] = useState(false);

  // 标签筛选
  const [activeTab, setActiveTab] = useState('all');

  // ==================== 数据加载 ====================

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
  };

  /**
   * 关闭发布动态弹窗
   */
  const handleClosePublishModal = () => {
    setShowPublishModal(false);
    setPublishContent('');
    setPublishImages([]);
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
      // 🚀 调用真实后端 API 发布动态
      await postService.createPost({
        content: publishContent,
        images: publishImages,
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
                <div key={post.postId} className="post-card">
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
                      onClick={() => handleToggleLike(post)}
                    >
                      {post.isLiked ? '❤️' : '🤍'} {post.likeCount}
                    </button>
                    <button
                      className="post-card__action"
                      onClick={() => handleOpenCommentModal(post)}
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
