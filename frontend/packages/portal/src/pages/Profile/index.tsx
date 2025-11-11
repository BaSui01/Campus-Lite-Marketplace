/**
 * 个人中心页 - 用户信息、我的发布、我的收藏、我的订单！👤
 * @author BaSui 😎
 * @description 完整的个人中心功能页面
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Skeleton } from '@campus/shared/components';
import { userService } from '@campus/shared/services/user';
import { goodsService } from '@campus/shared/services/goods';
import { postService } from '@campus/shared/services/post';
import { useNotificationStore, useAuthStore } from '../../store';
import type { PageGoodsResponse } from '@campus/shared/types';
import type { UserProfileResponse, PostResponse, PagePostResponse } from '@campus/shared/api/models';
import './Profile.css';

/**
 * Tab 类型
 */
type ProfileTab = 'info' | 'published' | 'favorites' | 'posts';

/**
 * 个人中心页组件
 */
const Profile: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();
  const currentUser = useAuthStore((state) => state.user);
  const [searchParams, setSearchParams] = useSearchParams();

  // ==================== 状态管理 ====================

  // 从 URL 读取 tab 参数，默认为 'info'
  const tabFromUrl = (searchParams.get('tab') as ProfileTab) || 'info';
  const [activeTab, setActiveTab] = useState<ProfileTab>(tabFromUrl);
  const [user, setUser] = useState<UserProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 我的发布
  const [publishedGoods, setPublishedGoods] = useState<any[]>([]);
  const [publishedLoading, setPublishedLoading] = useState(false);
  const [publishedPage, setPublishedPage] = useState(0);
  const [publishedHasMore, setPublishedHasMore] = useState(false);

  // 我的收藏
  const [favoriteGoods, setFavoriteGoods] = useState<any[]>([]);
  const [favoriteLoading, setFavoriteLoading] = useState(false);
  const [favoritePage, setFavoritePage] = useState(0);
  const [favoriteHasMore, setFavoriteHasMore] = useState(false);

  // 我的帖子
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [postsLoading, setPostsLoading] = useState(false);
  const [postsPage, setPostsPage] = useState(0);
  const [postsHasMore, setPostsHasMore] = useState(false);

  // ==================== 数据加载 ====================

  /**
   * 加载用户资料（使用真实后端 API！）
   */
  const loadUserProfile = async () => {
    setLoading(true);
    setError(null);

    try {
      // 🚀 调用真实后端 API 获取当前用户资料
      const userData = await userService.getProfile();
      setUser(userData);
    } catch (err: any) {
      console.error('加载用户资料失败：', err);
      setError(err.response?.data?.message || '加载用户资料失败，请稍后重试！😭');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 加载我的发布（使用真实后端 API！）
   */
  const loadPublishedGoods = async (isLoadMore = false) => {
    if (isLoadMore) {
      setPublishedLoading(true);
    }

    try {
      const currentPage = isLoadMore ? publishedPage + 1 : 0;

      // 🚀 调用真实后端 API 获取我的发布
      const pageData: PageGoodsResponse = await goodsService.getMyGoods({
        page: currentPage,
        size: 10,
      });

      const newGoods = pageData.content || [];

      if (isLoadMore) {
        setPublishedGoods((prev) => [...prev, ...newGoods]);
        setPublishedPage(currentPage);
      } else {
        setPublishedGoods(newGoods);
        setPublishedPage(0);
      }

      setPublishedHasMore(!pageData.last);
    } catch (err: any) {
      console.error('加载我的发布失败：', err);
      toast.error(err.response?.data?.message || '加载失败，请稍后重试！😭');
    } finally {
      setPublishedLoading(false);
    }
  };

  /**
   * 加载我的收藏（使用真实后端 API！）
   */
  const loadFavoriteGoods = async (isLoadMore = false) => {
    if (isLoadMore) {
      setFavoriteLoading(true);
    }

    try {
      const currentPage = isLoadMore ? favoritePage + 1 : 0;

      // 🚀 调用真实后端 API 获取我的收藏
      const pageData: PageGoodsResponse = await goodsService.getMyFavorites(currentPage, 10);

      const newGoods = pageData.content || [];

      if (isLoadMore) {
        setFavoriteGoods((prev) => [...prev, ...newGoods]);
        setFavoritePage(currentPage);
      } else {
        setFavoriteGoods(newGoods);
        setFavoritePage(0);
      }

      setFavoriteHasMore(!pageData.last);
    } catch (err: any) {
      console.error('加载我的收藏失败：', err);
      toast.error(err.response?.data?.message || '加载失败，请稍后重试！😭');
    } finally {
      setFavoriteLoading(false);
    }
  };

  /**
   * 加载我的帖子（使用真实后端 API！）
   */
  const loadMyPosts = async (isLoadMore = false) => {
    if (!currentUser?.id) {
      console.warn('用户未登录，无法加载帖子');
      return;
    }

    if (isLoadMore) {
      setPostsLoading(true);
    }

    try {
      const currentPage = isLoadMore ? postsPage + 1 : 0;

      // 🚀 调用真实后端 API 获取我的帖子
      const pageData: PagePostResponse = await postService.getPostsByAuthor(currentUser.id, {
        page: currentPage,
        size: 10,
      });

      const newPosts = pageData.content || [];

      if (isLoadMore) {
        setPosts((prev) => [...prev, ...newPosts]);
        setPostsPage(currentPage);
      } else {
        setPosts(newPosts);
        setPostsPage(0);
      }

      setPostsHasMore(!pageData.last);
    } catch (err: any) {
      console.error('加载我的帖子失败：', err);
      toast.error(err.response?.data?.message || '加载失败，请稍后重试！😭');
    } finally {
      setPostsLoading(false);
    }
  };

  // 初始加载用户资料
  useEffect(() => {
    loadUserProfile();
  }, []);

  // 监听 URL 参数变化，同步 activeTab
  useEffect(() => {
    const newTab = (searchParams.get('tab') as ProfileTab) || 'info';
    if (newTab !== activeTab) {
      setActiveTab(newTab);
    }
  }, [searchParams]);

  // 切换 Tab 时加载对应数据
  useEffect(() => {
    if (activeTab === 'published' && publishedGoods.length === 0) {
      loadPublishedGoods();
    } else if (activeTab === 'favorites' && favoriteGoods.length === 0) {
      loadFavoriteGoods();
    } else if (activeTab === 'posts' && posts.length === 0) {
      loadMyPosts();
    }
  }, [activeTab]);

  // ==================== 事件处理 ====================

  /**
   * 切换 Tab（同步更新 URL）
   */
  const handleTabChange = (tab: ProfileTab) => {
    if (tab === activeTab) return;
    setActiveTab(tab);
    // 更新 URL 参数
    setSearchParams({ tab });
  };

  /**
   * 查看商品详情
   */
  const handleViewGoods = (goodsId: number) => {
    navigate(`/goods/${goodsId}`);
  };

  /**
   * 加载更多 - 我的发布
   */
  const handleLoadMorePublished = () => {
    if (publishedLoading || !publishedHasMore) return;
    loadPublishedGoods(true);
  };

  /**
   * 加载更多 - 我的收藏
   */
  const handleLoadMoreFavorite = () => {
    if (favoriteLoading || !favoriteHasMore) return;
    loadFavoriteGoods(true);
  };

  /**
   * 加载更多 - 我的帖子
   */
  const handleLoadMorePosts = () => {
    if (postsLoading || !postsHasMore) return;
    loadMyPosts(true);
  };

  /**
   * 取消收藏
   */
  const handleUnfavorite = async (goodsId: number) => {
    if (!window.confirm('确定要取消收藏吗？🤔')) {
      return;
    }

    try {
      await goodsService.unfavoriteGoods(goodsId);
      toast.success('取消收藏成功！✅');

      // 从列表中移除该商品
      setFavoriteGoods((prev) => prev.filter((goods) => goods.id !== goodsId));
    } catch (err: any) {
      console.error('取消收藏失败：', err);
      toast.error(err.response?.data?.message || '取消收藏失败，请稍后重试！😭');
    }
  };

  // ==================== 工具函数 ====================

  /**
   * 格式化价格 - ¥X.XX
   */
  const formatPrice = (price?: number) => {
    if (!price) return '¥0.00';
    // 后端价格单位是分，需要除以100
    return `¥${(price / 100).toFixed(2)}`;
  };

  /**
   * 获取商品状态文本
   */
  const getStatusText = (status?: string) => {
    switch (status) {
      case 'PENDING':
        return '待审核';
      case 'APPROVED':
        return '已上架';
      case 'REJECTED':
        return '已拒绝';
      case 'SOLD':
        return '已售出';
      case 'OFF_SHELF':
        return '已下架';
      default:
        return '未知';
    }
  };

  /**
   * 获取商品状态样式类
   */
  const getStatusClass = (status?: string) => {
    switch (status) {
      case 'PENDING':
        return 'status-pending';
      case 'APPROVED':
        return 'status-approved';
      case 'REJECTED':
        return 'status-rejected';
      case 'SOLD':
        return 'status-sold';
      case 'OFF_SHELF':
        return 'status-off-shelf';
      default:
        return '';
    }
  };

  // ==================== 渲染 ====================

  // 加载中状态
  if (loading && !user) {
    return (
      <div className="profile-page">
        <div className="profile-container">
          {/* 头像和用户名骨架 */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '24px' }}>
            <Skeleton type="avatar" avatarSize={80} avatarShape="circle" animation="wave" />
            <Skeleton type="text" rows={2} animation="wave" style={{ flex: 1 }} />
          </div>
          {/* Tab 和内容骨架 */}
          <Skeleton type="card" animation="wave" style={{ marginBottom: '16px' }} />
          <Skeleton type="list" count={3} animation="wave" />
        </div>
      </div>
    );
  }

  // 错误状态
  if (error || !user) {
    return (
      <div className="profile-page">
        <div className="profile-error">
          <div className="error-icon">😭</div>
          <h2>{error || '加载失败'}</h2>
          <button onClick={() => loadUserProfile()} className="btn-retry">
            重试
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="profile-page">
      <div className="profile-container">
        {/* ==================== 用户信息卡片 ==================== */}
        <div className="user-card">
          <div className="user-avatar">
            {user.avatar ? (
              <img src={user.avatar} alt={user.username} />
            ) : (
              <div className="avatar-placeholder">👤</div>
            )}
          </div>
          <div className="user-info">
            <h2 className="user-name">{user.nickname || user.username}</h2>
            <p className="user-username">@{user.username}</p>
            {user.email && <p className="user-email">📧 {user.email}</p>}
            {user.phone && <p className="user-phone">📱 {user.phone}</p>}
          </div>
          <div className="user-stats">
            <div className="stat-item">
              <div className="stat-value">{user.points || 0}</div>
              <div className="stat-label">积分</div>
            </div>
          </div>
        </div>

        {/* ==================== Tab 切换 ==================== */}
        <div className="profile-tabs">
          <div
            className={`tab-item ${activeTab === 'info' ? 'active' : ''}`}
            onClick={() => handleTabChange('info')}
          >
            ℹ️ 基本信息
          </div>
          <div
            className={`tab-item ${activeTab === 'published' ? 'active' : ''}`}
            onClick={() => handleTabChange('published')}
          >
            📦 我的发布
          </div>
          <div
            className={`tab-item ${activeTab === 'favorites' ? 'active' : ''}`}
            onClick={() => handleTabChange('favorites')}
          >
            ❤️ 我的收藏
          </div>
          <div
            className={`tab-item ${activeTab === 'posts' ? 'active' : ''}`}
            onClick={() => handleTabChange('posts')}
          >
            💬 我的帖子
          </div>
        </div>

        {/* ==================== Tab 内容 ==================== */}
        {activeTab === 'info' && (
          <div className="tab-content">
            {/* 基本信息 */}
            <div className="info-section">
              <h3 className="section-title">基本信息</h3>
              <div className="info-list">
                <div className="info-item">
                  <span className="info-label">用户名：</span>
                  <span className="info-value">{user.username}</span>
                </div>
                {user.nickname && (
                  <div className="info-item">
                    <span className="info-label">昵称：</span>
                    <span className="info-value">{user.nickname}</span>
                  </div>
                )}
                {user.email && (
                  <div className="info-item">
                    <span className="info-label">邮箱：</span>
                    <span className="info-value">{user.email}</span>
                  </div>
                )}
                {user.phone && (
                  <div className="info-item">
                    <span className="info-label">手机号：</span>
                    <span className="info-value">{user.phone}</span>
                  </div>
                )}
                <div className="info-item">
                  <span className="info-label">积分：</span>
                  <span className="info-value points">{user.points || 0}</span>
                </div>
                <div className="info-item">
                  <span className="info-label">状态：</span>
                  <span className={`info-value status ${user.status === 'ACTIVE' ? 'active' : 'banned'}`}>
                    {user.status === 'ACTIVE' ? '正常' : '已封禁'}
                  </span>
                </div>
              </div>

              {/* 快捷操作 */}
              <div className="quick-actions">
                <button className="btn-action" onClick={() => navigate('/orders')}>
                  📋 我的订单
                </button>
                <button className="btn-action" onClick={() => navigate('/publish')}>
                  ➕ 发布商品
                </button>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'published' && (
          <div className="tab-content">
            {publishedGoods.length === 0 && !publishedLoading ? (
              <div className="empty-state">
                <div className="empty-icon">📦</div>
                <p className="empty-text">还没有发布任何商品</p>
                <p className="empty-tip">快去发布你的第一个商品吧！</p>
                <button className="btn-publish" onClick={() => navigate('/publish')}>
                  发布商品
                </button>
              </div>
            ) : (
              <>
                <div className="goods-grid">
                  {publishedGoods.map((goods) => (
                    <div
                      key={goods.id}
                      className="goods-card"
                      onClick={() => handleViewGoods(goods.id)}
                    >
                      <div className="goods-image">
                        {goods.images?.[0] ? (
                          <img src={goods.images[0]} alt={goods.title} />
                        ) : (
                          <div className="image-placeholder">📦</div>
                        )}
                        <div className={`goods-status ${getStatusClass(goods.status)}`}>
                          {getStatusText(goods.status)}
                        </div>
                      </div>
                      <div className="goods-info">
                        <h4 className="goods-title">{goods.title}</h4>
                        <p className="goods-price">{formatPrice(goods.price)}</p>
                        <div className="goods-meta">
                          <span className="goods-views">👁️ {goods.viewCount || 0}</span>
                          <span className="goods-favorites">❤️ {goods.favoriteCount || 0}</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>

                {/* 加载更多 */}
                {publishedHasMore && (
                  <div className="load-more-section">
                    <button
                      className="btn-load-more"
                      onClick={handleLoadMorePublished}
                      disabled={publishedLoading}
                    >
                      {publishedLoading ? '⏳ 加载中...' : '加载更多'}
                    </button>
                  </div>
                )}
              </>
            )}
          </div>
        )}

        {activeTab === 'favorites' && (
          <div className="tab-content">
            {favoriteGoods.length === 0 && !favoriteLoading ? (
              <div className="empty-state">
                <div className="empty-icon">❤️</div>
                <p className="empty-text">还没有收藏任何商品</p>
                <p className="empty-tip">快去逛逛，收藏喜欢的商品吧！</p>
                <button className="btn-browse" onClick={() => navigate('/')}>
                  去首页逛逛
                </button>
              </div>
            ) : (
              <>
                <div className="goods-grid">
                  {favoriteGoods.map((goods) => (
                    <div key={goods.id} className="goods-card favorite-card">
                      <div className="goods-image" onClick={() => handleViewGoods(goods.id)}>
                        {goods.images?.[0] ? (
                          <img src={goods.images[0]} alt={goods.title} />
                        ) : (
                          <div className="image-placeholder">📦</div>
                        )}
                      </div>
                      <div className="goods-info" onClick={() => handleViewGoods(goods.id)}>
                        <h4 className="goods-title">{goods.title}</h4>
                        <p className="goods-price">{formatPrice(goods.price)}</p>
                        <div className="goods-meta">
                          <span className="goods-views">👁️ {goods.viewCount || 0}</span>
                          <span className="goods-favorites">❤️ {goods.favoriteCount || 0}</span>
                        </div>
                      </div>
                      <button
                        className="btn-unfavorite"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleUnfavorite(goods.id);
                        }}
                      >
                        💔 取消收藏
                      </button>
                    </div>
                  ))}
                </div>

                {/* 加载更多 */}
                {favoriteHasMore && (
                  <div className="load-more-section">
                    <button
                      className="btn-load-more"
                      onClick={handleLoadMoreFavorite}
                      disabled={favoriteLoading}
                    >
                      {favoriteLoading ? '⏳ 加载中...' : '加载更多'}
                    </button>
                  </div>
                )}
              </>
            )}
          </div>
        )}

        {activeTab === 'posts' && (
          <div className="tab-content">
            {posts.length === 0 && !postsLoading ? (
              <div className="empty-state">
                <div className="empty-icon">💬</div>
                <p className="empty-text">还没有发布任何帖子</p>
                <button className="btn-action" onClick={() => navigate('/community')}>
                  去社区逛逛
                </button>
              </div>
            ) : (
              <>
                <div className="posts-list">
                  {posts.map((post) => (
                    <div
                      key={post.id}
                      className="post-card"
                      onClick={() => navigate(`/posts/${post.id}`)}
                    >
                      <div className="post-header">
                        <h4 className="post-title">{post.title}</h4>
                        <span className={`post-status ${post.status?.toLowerCase()}`}>
                          {post.status === 'APPROVED' ? '✅ 已通过' :
                           post.status === 'PENDING' ? '⏳ 审核中' :
                           post.status === 'REJECTED' ? '❌ 已拒绝' : '❓ 未知'}
                        </span>
                      </div>
                      <p className="post-content">
                        {post.content && post.content.length > 100
                          ? `${post.content.substring(0, 100)}...`
                          : post.content}
                      </p>
                      {post.images && post.images.length > 0 && (
                        <div className="post-images">
                          {post.images.slice(0, 3).map((img, idx) => (
                            <img key={idx} src={img} alt={`图片${idx + 1}`} />
                          ))}
                        </div>
                      )}
                      <div className="post-meta">
                        <span className="post-views">👁️ {post.viewCount || 0}</span>
                        <span className="post-replies">💬 {post.replyCount || 0}</span>
                        <span className="post-date">
                          📅 {post.createdAt ? new Date(post.createdAt).toLocaleDateString() : '未知'}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>

                {/* 加载更多 */}
                {postsHasMore && (
                  <div className="load-more-section">
                    <button
                      className="btn-load-more"
                      onClick={handleLoadMorePosts}
                      disabled={postsLoading}
                    >
                      {postsLoading ? '⏳ 加载中...' : '加载更多'}
                    </button>
                  </div>
                )}
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default Profile;
