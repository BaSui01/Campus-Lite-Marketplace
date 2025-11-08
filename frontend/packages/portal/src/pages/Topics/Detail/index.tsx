/**
 * 话题详情页面 - 深入了解话题！💬
 * @author BaSui 😎
 * @description 话题信息、统计、相关帖子
 */

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Skeleton } from '@campus/shared/components';
import { topicService, postService, type Topic } from '@campus/shared/services';;
import { useAuthStore, useNotificationStore } from '../../../store';
import './TopicDetail.css';

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

/**
 * 话题详情页面组件
 */
const TopicDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const toast = useNotificationStore();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  // ==================== 状态管理 ====================

  const [topic, setTopic] = useState<Topic | null>(null);
  const [loading, setLoading] = useState(true);
  const [isFollowed, setIsFollowed] = useState(false);
  const [followLoading, setFollowLoading] = useState(false);

  // 相关内容
  const [posts, setPosts] = useState<Post[]>([]);
  const [postsLoading, setPostsLoading] = useState(false);

  // ==================== 数据加载 ====================

  /**
   * 加载话题详情
   */
  const loadTopicDetail = async () => {
    if (!id) return;

    setLoading(true);

    try {
      // ✅ 获取话题详情
      const topicData = await topicService.getById(Number(id));
      setTopic(topicData);

      // ✅ 检查是否已关注
      if (isAuthenticated) {
        const followed = await topicService.checkFollowed(Number(id));
        setIsFollowed(followed);
      }
    } catch (err: any) {
      console.error('加载话题详情失败:', err);
      toast.error(err.response?.data?.message || '加载话题详情失败！😭');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 加载相关帖子
   * ⚠️ 注意：目前 postService 可能不支持按话题筛选，这里作为占位实现
   * 后续需要后端添加 GET /posts?topicId={topicId} 接口
   */
  const loadRelatedPosts = async () => {
    if (!id) return;

    setPostsLoading(true);

    try {
      // ⚠️ 临时实现：获取所有帖子（后续需要按话题筛选）
      const response = await postService.getPosts({
        page: 0,
        pageSize: 10,
      });

      if (response.success && response.data) {
        // 转换数据格式
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
          createdAt: p.createdAt,
        }));

        setPosts(apiPosts);
      }
    } catch (err: any) {
      console.error('加载相关帖子失败:', err);
      // 不显示错误提示（可能后端未实现）
    } finally {
      setPostsLoading(false);
    }
  };

  useEffect(() => {
    loadTopicDetail();
    loadRelatedPosts();
  }, [id]);

  // ==================== 事件处理 ====================

  /**
   * 关注/取消关注话题
   */
  const handleToggleFollow = async () => {
    if (!isAuthenticated) {
      toast.warning('请先登录！😰');
      navigate('/login');
      return;
    }

    if (!topic) return;

    setFollowLoading(true);

    try {
      // 乐观更新 UI
      setIsFollowed(!isFollowed);

      // ✅ 调用真实 API
      if (isFollowed) {
        await topicService.unfollow(topic.id);
        toast.success('取消关注成功！👋');

        // 更新话题统计
        setTopic({
          ...topic,
          followerCount: (topic.followerCount || 1) - 1,
        });
      } else {
        await topicService.follow(topic.id);
        toast.success('关注成功！🎉');

        // 更新话题统计
        setTopic({
          ...topic,
          followerCount: (topic.followerCount || 0) + 1,
        });
      }
    } catch (err: any) {
      console.error('关注操作失败:', err);
      toast.error(err.response?.data?.message || '操作失败！😭');

      // 回滚 UI
      setIsFollowed(!isFollowed);
    } finally {
      setFollowLoading(false);
    }
  };

  /**
   * 返回话题列表
   */
  const handleBack = () => {
    navigate('/topics');
  };

  /**
   * 查看帖子详情
   */
  const handleViewPost = (postId: string) => {
    // TODO: 实现帖子详情页
    navigate(`/community`);
  };

  /**
   * 格式化时间
   */
  const formatTime = (time: string) => {
    const date = new Date(time);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  // ==================== 渲染 ====================

  if (loading) {
    return (
      <div className="topic-detail-page">
        <div className="topic-detail-container">
          <Skeleton type="card" count={3} animation="wave" />
        </div>
      </div>
    );
  }

  if (!topic) {
    return (
      <div className="topic-detail-page">
        <div className="topic-detail-container">
          <div className="topic-detail-error">
            <div className="error-icon">⚠️</div>
            <h3 className="error-text">话题不存在</h3>
            <Button type="primary" size="large" onClick={handleBack}>
              返回话题列表 →
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="topic-detail-page">
      <div className="topic-detail-container">
        {/* ==================== 返回按钮 ==================== */}
        <div className="topic-detail-back">
          <Button type="default" size="small" onClick={handleBack}>
            ← 返回话题列表
          </Button>
        </div>

        {/* ==================== 话题头部 ==================== */}
        <div className="topic-detail-header">
          <div className="topic-detail-header__icon">
            {topic.isHot ? '🔥' : '💬'}
          </div>
          <div className="topic-detail-header__content">
            <h1 className="topic-detail-header__title">{topic.name}</h1>
            {topic.description && (
              <p className="topic-detail-header__description">
                {topic.description}
              </p>
            )}
            <div className="topic-detail-header__meta">
              <span className="meta-item">
                📅 创建于 {formatTime(topic.createdAt)}
              </span>
              {topic.updatedAt && (
                <span className="meta-item">
                  🔄 更新于 {formatTime(topic.updatedAt)}
                </span>
              )}
            </div>
          </div>
          <div className="topic-detail-header__actions">
            {isAuthenticated && (
              <Button
                type={isFollowed ? 'default' : 'primary'}
                size="large"
                onClick={handleToggleFollow}
                loading={followLoading}
              >
                {isFollowed ? '✓ 已关注' : '➕ 关注话题'}
              </Button>
            )}
          </div>
        </div>

        {/* ==================== 话题统计 ==================== */}
        <div className="topic-detail-stats">
          <div className="stat-card">
            <div className="stat-card__icon">📝</div>
            <div className="stat-card__content">
              <div className="stat-card__value">{topic.postCount || 0}</div>
              <div className="stat-card__label">帖子数</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-card__icon">👥</div>
            <div className="stat-card__content">
              <div className="stat-card__value">{topic.followerCount || 0}</div>
              <div className="stat-card__label">关注数</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-card__icon">👀</div>
            <div className="stat-card__content">
              <div className="stat-card__value">{topic.viewCount || 0}</div>
              <div className="stat-card__label">浏览数</div>
            </div>
          </div>
        </div>

        {/* ==================== 相关内容 ==================== */}
        <div className="topic-detail-content">
          <h2 className="section-title">📰 相关内容</h2>

          {postsLoading ? (
            <div className="content-loading">
              <Skeleton type="list" count={5} animation="wave" />
            </div>
          ) : posts.length === 0 ? (
            <div className="content-empty">
              <div className="empty-icon">📭</div>
              <h3 className="empty-text">暂无相关内容</h3>
              <p className="empty-tip">
                这个话题还没有帖子，快来发布第一条吧！
              </p>
              <p className="empty-note">
                ⚠️ 该功能需要后端 API 支持：<br />
                <code>GET /posts?topicId={'{topicId}'}</code>
              </p>
              <Button
                type="primary"
                size="large"
                onClick={() => navigate('/community')}
              >
                去社区发帖 →
              </Button>
            </div>
          ) : (
            <div className="content-list">
              {posts.map((post) => (
                <div
                  key={post.postId}
                  className="content-item"
                  onClick={() => handleViewPost(post.postId)}
                >
                  <div className="content-item__header">
                    <div className="content-item__avatar">
                      {post.authorAvatar ? (
                        <img src={post.authorAvatar} alt={post.authorName} />
                      ) : (
                        <div className="avatar-placeholder">👤</div>
                      )}
                    </div>
                    <div className="content-item__info">
                      <div className="content-item__name">{post.authorName}</div>
                      <div className="content-item__time">
                        {formatTime(post.createdAt)}
                      </div>
                    </div>
                  </div>
                  <div className="content-item__body">
                    <p className="content-item__text">{post.content}</p>
                    {post.images && post.images.length > 0 && (
                      <div className="content-item__images">
                        {post.images.slice(0, 3).map((img, idx) => (
                          <img key={idx} src={img} alt={`图片${idx + 1}`} />
                        ))}
                      </div>
                    )}
                  </div>
                  <div className="content-item__footer">
                    <span className="footer-stat">
                      ❤️ {post.likeCount} 赞
                    </span>
                    <span className="footer-stat">
                      💬 {post.commentCount} 评论
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default TopicDetail;
