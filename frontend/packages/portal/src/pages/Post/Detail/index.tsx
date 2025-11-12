/**
 * 帖子详情页 - 深入了解帖子内容！📰
 * @author BaSui 😎
 * @description 帖子完整内容、作者信息、点赞评论、相关推荐
 * @created 2025-11-11 - 复用 Community 页面组件和逻辑
 */

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Skeleton, Modal, Input } from '@campus/shared/components';
import { postService, communityService } from '@campus/shared/services';
import { useAuthStore, useNotificationStore } from '../../../store';
import './PostDetail.css';

// ==================== 类型定义 ====================

interface PostDetail {
  id: number;
  title: string;
  content: string;
  authorId: number;
  authorName: string;
  authorAvatar?: string;
  images?: string[];
  likeCount: number;
  collectCount: number;
  viewCount: number;
  replyCount: number;
  isLiked: boolean;
  isCollected: boolean;
  createdAt: string;
  status: string;
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
 * 帖子详情页组件
 */
const PostDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const toast = useNotificationStore();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const currentUser = useAuthStore((state) => state.user);

  // ==================== 状态管理 ====================

  const [post, setPost] = useState<PostDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 评论相关
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [commentContent, setCommentContent] = useState('');
  const [commenting, setCommenting] = useState(false);

  // 相关推荐
  const [relatedPosts, setRelatedPosts] = useState<PostDetail[]>([]);
  const [relatedLoading, setRelatedLoading] = useState(false);

  // 图片预览
  const [showImagePreview, setShowImagePreview] = useState(false);
  const [previewImageIndex, setPreviewImageIndex] = useState(0);

  // ==================== 数据加载 ====================

  /**
   * 获取状态文本（用于轻提示）
   */
  const getStatusText = (status?: string) => {
    switch (status) {
      case 'PENDING':
        return '待审核';
      case 'APPROVED':
        return '已通过';
      case 'REJECTED':
        return '未通过';
      case 'LOCKED':
        return '锁定中';
      case 'SOLD':
        return '已售出';
      case 'OFFLINE':
        return '已下架';
      default:
        return '未知';
    }
  };

  /**
   * 加载帖子详情
   */
  const loadPostDetail = async () => {
    if (!id) {
      setError('帖子 ID 不能为空！😰');
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // 🚀 调用真实后端 API 获取帖子详情
      const postData = await postService.getPostById(Number(id));

      // 转换数据格式
      const detailPost: PostDetail = {
        id: postData.id,
        title: postData.title || '',
        content: postData.content,
        authorId: postData.authorId,
        authorName: postData.authorName || '未知用户',
        authorAvatar: postData.authorAvatar,
        images: postData.images || [],
        likeCount: postData.likeCount || 0,
        collectCount: postData.collectCount || 0,
        viewCount: postData.viewCount || 0,
        replyCount: postData.replyCount || 0,
        isLiked: postData.isLiked || false,
        isCollected: postData.isCollected || false,
        createdAt: postData.createdAt || postData.createTime || new Date().toISOString(),
        status: postData.status || 'APPROVED',
      };

      setPost(detailPost);

      // 加载评论和相关推荐
      loadComments(Number(id));
      loadRelatedPosts();
    } catch (err: any) {
      console.error('加载帖子详情失败：', err);
      setError(err.response?.data?.message || '加载帖子详情失败，请稍后重试！😭');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 加载评论列表
   */
  const loadComments = async (postId: number) => {
    setCommentsLoading(true);

    try {
      // 🚀 调用真实后端 API 获取评论列表
      const pageReply = await postService.getReplies(postId, { page: 0, size: 100 });

      // postService.getReplies 已返回 data.data(即 PageReplyResponse)，无需再判 success/data
      const list = Array.isArray(pageReply?.content) ? pageReply.content : [];
      const apiComments: Comment[] = list.map((c: any) => ({
        commentId: String(c.id),
        postId: String(postId),
        // 兼容后端字段：优先 authorId/authorName，其次 userId/userName
        authorId: String(c.authorId ?? c.userId),
        authorName: c.authorName ?? c.userName ?? '未知用户',
        authorAvatar: c.authorAvatar ?? c.userAvatar,
        content: c.content,
        createdAt: c.createdAt ?? c.createTime,
      }));

      setComments(apiComments);
    } catch (err: any) {
      console.error('加载评论失败：', err);
      // 不显示错误提示，静默失败
    } finally {
      setCommentsLoading(false);
    }
  };
  /**
   * 判断是否管理员角色
   */
  const hasAdminRole = (): boolean => {
    const roles = (currentUser as any)?.roles;
    if (!roles) return false;
    // roles 可能是字符串数组或包含 name 的对象数组
    if (Array.isArray(roles)) {
      return roles.some((r: any) => {
        const name = typeof r === 'string' ? r : r?.name;
        return name === 'ROLE_ADMIN' || name === 'ADMIN';
      });
    }
    return false;
  };

  /** 删除评论 */
  const handleDeleteComment = async (commentId: string) => {
    if (!isAuthenticated) {
      toast.warning('请先登录！');
      navigate('/login');
      return;
    }
    try {
      await postService.deleteReply(Number(commentId));
      setComments((prev) => prev.filter((c) => c.commentId !== commentId));
      if (post && post.replyCount > 0) {
        setPost({ ...post, replyCount: post.replyCount - 1 });
      }
      toast.success('已删除评论');
    } catch (err: any) {
      if (err?.response?.status === 403) {
        toast.warning('无权限删除该评论');
      } else {
        toast.error(err?.response?.data?.message || '删除失败');
      }
    }
  };


  /**
   * 加载相关推荐帖子（最新帖子）
   */
  const loadRelatedPosts = async () => {
    setRelatedLoading(true);

    try {
      // 🚀 调用真实后端 API 获取推荐帖子
      const pagePosts = await postService.getPosts({
        page: 0,
        size: 5,
        sortBy: 'createdAt',
        sortDirection: 'DESC',
      });

      const list = Array.isArray(pagePosts?.content) ? pagePosts.content : [];
      const posts: PostDetail[] = list
        .filter((p: any) => p.id !== Number(id))
        .slice(0, 4)
        .map((p: any) => ({
          id: p.id,
          title: p.title || '',
          content: p.content,
          authorId: p.userId ?? p.authorId,
          authorName: p.userName ?? p.authorName ?? '未知用户',
          authorAvatar: p.userAvatar ?? p.authorAvatar,
          images: p.images || [],
          likeCount: p.likeCount || 0,
          collectCount: p.collectCount || 0,
          viewCount: p.viewCount || 0,
          replyCount: p.commentCount ?? p.replyCount ?? 0,
          isLiked: p.isLiked || false,
          isCollected: p.isCollected || false,
          createdAt: p.createTime || p.createdAt,
          status: p.status || 'APPROVED',
        }));

      setRelatedPosts(posts);
    } catch (err: any) {
      console.error('加载推荐帖子失败：', err);
      // 不显示错误提示
    } finally {
      setRelatedLoading(false);
    }
  };

  useEffect(() => {
    loadPostDetail();
  }, [id]);

  // ==================== 事件处理 ====================

  /**
   * 点赞/取消点赞
   */
  const handleToggleLike = async () => {
    if (!isAuthenticated) {
      toast.warning('请先登录！😰');
      navigate('/login');
      return;
    }

    if (!post) return;

    try {
      // 乐观更新 UI
      const newIsLiked = !post.isLiked;
      const newLikeCount = newIsLiked ? post.likeCount + 1 : post.likeCount - 1;

      setPost({
        ...post,
        isLiked: newIsLiked,
        likeCount: newLikeCount,
      });

      // 🚀 调用真实后端 API
      if (post.isLiked) {
        await communityService.unlikePost(post.id);
        toast.success('取消点赞成功！👋');
      } else {
        await communityService.likePost(post.id);
        toast.success('点赞成功！❤️');
      }
    } catch (err: any) {
      console.error('点赞操作失败：', err);
      toast.error(err.response?.data?.message || '操作失败！😭');

      // 回滚 UI
      setPost(post);
    }
  };

  /**
   * 收藏/取消收藏
   */
  const handleToggleCollect = async () => {
    if (!isAuthenticated) {
      toast.warning('请先登录！😰');
      navigate('/login');
      return;
    }

    if (!post) return;

    try {
      // 乐观更新 UI
      const newIsCollected = !post.isCollected;
      const newCollectCount = newIsCollected ? post.collectCount + 1 : post.collectCount - 1;

      setPost({
        ...post,
        isCollected: newIsCollected,
        collectCount: newCollectCount,
      });

      // 🚀 调用真实后端 API
      if (post.isCollected) {
        await communityService.uncollectPost(post.id);
        toast.success('取消收藏成功！👋');
      } else {
        await communityService.collectPost(post.id);
        toast.success('收藏成功！⭐');
      }
    } catch (err: any) {
      console.error('收藏操作失败：', err);
      toast.error(err.response?.data?.message || '操作失败！😭');

      // 回滚 UI
      setPost(post);
    }
  };

  /**
   * 发布评论
   */
  const handlePublishComment = async () => {
    if (!isAuthenticated) {
      toast.warning('请先登录！😰');
      navigate('/login');
      return;
    }

    if (!commentContent.trim()) {
      toast.warning('请输入评论内容！😰');
      return;
    }

    if (!post) return;

    setCommenting(true);

    try {
      // ⛔ 前置校验（放宽）：未审核且非作者（管理员放行由后端兜底）
      const isAuthor = currentUser?.id === post.authorId;
      if (post.status !== 'APPROVED' && !isAuthor) {
        toast.warning(`该帖子${getStatusText(post.status)}，仅作者或管理员可评论。`);
        return;
      }

      // 🚀 调用真实后端 API 发布评论
      await postService.createReply({
        postId: post.id,
        content: commentContent,
      });

      toast.success('评论成功！💬');
      setCommentContent('');

      // 重新加载评论列表
      loadComments(post.id);

      // 更新帖子评论数
      setPost({
        ...post,
        replyCount: post.replyCount + 1,
      });
    } catch (err: any) {
      console.error('发布评论失败：', err);
      toast.error(err.response?.data?.message || '发布评论失败！😭');
    } finally {
      setCommenting(false);
    }
  };

  /**
   * 查看作者主页
   */
  const handleViewAuthor = () => {
    if (!post) return;
    navigate(`/users/${post.authorId}`);
  };

  /**
   * 返回社区
   */
  const handleBack = () => {
    navigate('/community');
  };

  /**
   * 查看相关帖子
   */
  const handleViewRelatedPost = (postId: number) => {
    navigate(`/posts/${postId}`);
    // 重新加载详情
    window.scrollTo(0, 0);
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
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  /**
   * 打开图片预览
   */
  const handleImageClick = (index: number) => {
    setPreviewImageIndex(index);
    setShowImagePreview(true);
  };

  // ==================== 渲染 ====================

  if (loading) {
    return (
      <div className="post-detail-page">
        <div className="post-detail-container">
          <Skeleton type="card" count={3} animation="wave" />
        </div>
      </div>
    );
  }

  if (error || !post) {
    return (
      <div className="post-detail-page">
        <div className="post-detail-container">
          <div className="post-detail-error">
            <div className="error-icon">⚠️</div>
            <h3 className="error-text">{error || '帖子不存在'}</h3>
            <Button type="primary" size="large" onClick={handleBack}>
              返回社区 →
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="post-detail-page">
      <div className="post-detail-container">
        {/* ==================== 返回按钮 ==================== */}
        <div className="post-detail-back">
          <Button type="default" size="small" onClick={handleBack}>
            ← 返回社区
          </Button>
        </div>

        {/* ==================== 主内容区 ==================== */}
        <div className="post-detail-main">
          {/* 帖子内容 */}
          <div className="post-detail-content">
            {/* 标题 */}
            {post.title && (
              <h1 className="post-detail-title">{post.title}</h1>
            )}

            {/* 状态提示条（仅非已通过时展示，避免误操作评论） */}
            {post.status !== 'APPROVED' && (
              <div className="post-status-banner" style={{margin:'8px 0 16px', padding:'10px 12px', borderRadius:8, background:'#fff7e6', color:'#ad6800', fontSize:14}}>
                ⚠️ 当前帖子状态为「{getStatusText(post.status)}」，暂不开放评论。
              </div>
            )}

            {/* 作者信息 */}
            <div className="post-detail-author">
              <div className="author-info" onClick={handleViewAuthor}>
                <div className="author-avatar">
                  {post.authorAvatar ? (
                    <img src={post.authorAvatar} alt={post.authorName} />
                  ) : (
                    <span>👤</span>
                  )}
                </div>
                <div className="author-details">
                  <div className="author-name">{post.authorName}</div>
                  <div className="author-meta">
                    <span>{formatTime(post.createdAt)}</span>
                    <span className="separator">·</span>
                    <span>👀 {post.viewCount} 浏览</span>
                  </div>
                </div>
              </div>
            </div>

            {/* 正文内容 */}
            <div className="post-detail-body">
              <p>{post.content}</p>
            </div>

            {/* 图片 */}
            {post.images && post.images.length > 0 && (
              <div className="post-detail-images">
                {post.images.map((image, index) => (
                  <img
                    key={index}
                    src={image}
                    alt={`图片${index + 1}`}
                    onClick={() => handleImageClick(index)}
                  />
                ))}
              </div>
            )}

            {/* 互动区 */}
            <div className="post-detail-actions">
              <button
                className={`action-btn ${post.isLiked ? 'active' : ''}`}
                onClick={handleToggleLike}
              >
                {post.isLiked ? '❤️' : '🤍'} {post.likeCount}
              </button>
              <button
                className={`action-btn ${post.isCollected ? 'active' : ''}`}
                onClick={handleToggleCollect}
              >
                {post.isCollected ? '⭐' : '☆'} {post.collectCount}
              </button>
              <button className="action-btn">
                💬 {post.replyCount}
              </button>
            </div>
          </div>

          {/* 评论区 */}
          <div className="post-detail-comments">
            <div className="comments-header">
              <h3 className="comments-title">💬 评论 ({post.replyCount})</h3>
            </div>

            {/* 评论输入框 */}
            {isAuthenticated && (post.status === 'APPROVED' || currentUser?.id === post.authorId) && (
              <div className="comments-input">
                <Input
                  type="text"
                  placeholder="说点什么..."
                  value={commentContent}
                  onChange={(e) => setCommentContent(e.target.value)}
                  maxLength={200}
                />
                <Button
                  type="primary"
                  onClick={handlePublishComment}
                  loading={commenting}
                  disabled={!commentContent.trim()}
                >
                  {commenting ? '发送中...' : '发送'}
                </Button>
              </div>
            )}
            {isAuthenticated && post.status !== 'APPROVED' && currentUser?.id !== post.authorId && (
              <div className="comments-input-disabled" style={{marginTop:12, padding:'10px 12px', border:'1px dashed #ffd591', borderRadius:8, color:'#ad6800', background:'#fffbe6'}}>
                暂不可评论：帖子{getStatusText(post.status)}。仅作者或管理员可评论。
              </div>
            )}

            {/* 评论列表 */}
            <div className="comments-list">
              {commentsLoading ? (
                <Skeleton type="list" count={3} animation="wave" />
              ) : comments.length === 0 ? (
                <div className="comments-empty">
                  <div className="empty-icon">📭</div>
                  <p className="empty-text">还没有评论</p>
                  <p className="empty-tip">快来抢沙发吧！</p>
                </div>
              ) : (
                comments.map((comment) => {
                  const uid = Number(currentUser?.id);
                  const canDelete =
                    isAuthenticated && (
                      uid === Number(comment.authorId) ||
                      uid === post.authorId ||
                      hasAdminRole()
                    );
                  return (
                  <div key={comment.commentId} className="comment-item">
                    <div className="comment-avatar">
                      {comment.authorAvatar ? (
                        <img src={comment.authorAvatar} alt={comment.authorName} />
                      ) : (
                        <span>👤</span>
                      )}
                    </div>
                    <div className="comment-content">
                      <div className="comment-header">
                        <span className="comment-author">{comment.authorName}</span>
                        <span className="comment-time">{formatTime(comment.createdAt)}</span>
                        {canDelete && (
                          <button
                            className="comment-action-delete"
                            onClick={() => handleDeleteComment(comment.commentId)}
                            style={{ marginLeft: 8, color: '#ff4d4f', background: 'transparent', border: 'none', cursor: 'pointer' }}
                          >
                            删除
                          </button>
                        )}
                      </div>
                      <p className="comment-text">{comment.content}</p>
                    </div>
                  </div>
                  );
                })
              )}
            </div>
          </div>
        </div>

        {/* ==================== 侧边栏 ==================== */}
        <div className="post-detail-sidebar">
          {/* 相关推荐 */}
          <div className="sidebar-section">
            <h3 className="sidebar-title">📰 相关推荐</h3>
            {relatedLoading ? (
              <Skeleton type="card" count={3} animation="wave" />
            ) : relatedPosts.length === 0 ? (
              <div className="sidebar-empty">暂无推荐</div>
            ) : (
              <div className="related-posts">
                {relatedPosts.map((relatedPost) => (
                  <div
                    key={relatedPost.id}
                    className="related-post-item"
                    onClick={() => handleViewRelatedPost(relatedPost.id)}
                  >
                    <div className="related-post-content">
                      <h4 className="related-post-title">
                        {relatedPost.title || relatedPost.content.substring(0, 30) + '...'}
                      </h4>
                      <div className="related-post-meta">
                        <span>❤️ {relatedPost.likeCount}</span>
                        <span>💬 {relatedPost.replyCount}</span>
                      </div>
                    </div>
                    {relatedPost.images && relatedPost.images.length > 0 && (
                      <div className="related-post-image">
                        <img src={relatedPost.images[0]} alt="缩略图" />
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ==================== 图片预览弹窗 ==================== */}
      {showImagePreview && post.images && post.images.length > 0 && (
        <Modal
          onClose={() => setShowImagePreview(false)}
          title={`图片预览 (${previewImageIndex + 1}/${post.images.length})`}
        >
          <div className="image-preview-modal">
            <img
              src={post.images[previewImageIndex]}
              alt={`预览图片${previewImageIndex + 1}`}
              className="preview-image"
            />
            <div className="preview-controls">
              {previewImageIndex > 0 && (
                <Button
                  type="default"
                  onClick={() => setPreviewImageIndex(previewImageIndex - 1)}
                >
                  ← 上一张
                </Button>
              )}
              {previewImageIndex < post.images.length - 1 && (
                <Button
                  type="default"
                  onClick={() => setPreviewImageIndex(previewImageIndex + 1)}
                >
                  下一张 →
                </Button>
              )}
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default PostDetail;
