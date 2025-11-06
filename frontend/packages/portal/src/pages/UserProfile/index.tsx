/**
 * 用户主页页面 - 了解TA的故事!👤
 * @author BaSui 😎
 * @description 查看用户信息、商品列表、帖子动态、关注/取消关注
 */

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Skeleton, Tabs, GoodsCard } from '@campus/shared/components';
import { creditService, CreditLevel, CREDIT_LEVEL_CONFIG } from '@campus/shared/services';
import { useAuthStore, useNotificationStore } from '../../store';
import { getApi } from '@campus/shared/utils';
import { BlacklistButton } from '../../components/BlacklistButton';
import './UserProfile.css';

// ==================== 类型定义 ====================

interface UserProfile {
  id: number;
  username: string;
  avatar?: string;
  bio?: string;
  campusName?: string;
  goodsCount: number;
  followingCount: number;
  followerCount: number;
  isFollowing: boolean;
  creditScore?: number;
  creditLevel?: CreditLevel;
}

interface Goods {
  id: number;
  title: string;
  price: number;
  coverImage?: string;
  status: string;
  viewCount: number;
  favoriteCount: number;
}

/**
 * 用户主页组件
 */
const UserProfile: React.FC = () => {
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();
  const toast = useNotificationStore();
  const currentUser = useAuthStore((state) => state.user);

  // ==================== 状态管理 ====================

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [goods, setGoods] = useState<Goods[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingGoods, setLoadingGoods] = useState(false);
  const [activeTab, setActiveTab] = useState('goods');
  const [following, setFollowing] = useState(false);

  // API 实例
  const api = getApi();

  // 是否是自己的主页
  const isOwnProfile = currentUser?.id === Number(userId);

  // ==================== 数据加载 ====================

  /**
   * 加载用户信息
   */
  const loadProfile = async () => {
    if (!userId) return;
    setLoading(true);

    try {
      // 🚀 调用真实后端 API 获取用户信息
      const response = await api.getUserProfile({ userId: Number(userId) });

      if (response.data.success && response.data.data) {
        const data = response.data.data;
        setProfile({
          id: data.id!,
          username: data.username || '未知用户',
          avatar: data.avatar,
          bio: data.bio,
          campusName: data.campusName,
          goodsCount: data.goodsCount || 0,
          followingCount: data.followingCount || 0,
          followerCount: data.followerCount || 0,
          isFollowing: data.isFollowing || false,
        });
        setFollowing(data.isFollowing || false);
      }
    } catch (err: any) {
      console.error('加载用户信息失败:', err);
      toast.error(err.response?.data?.message || '加载用户信息失败!😭');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 加载用户商品
   */
  const loadUserGoods = async () => {
    if (!userId) return;
    setLoadingGoods(true);

    try {
      // 🚀 调用真实后端 API 获取用户商品
      const response = await api.listGoods({ sellerId: Number(userId), page: 0, size: 12 });

      if (response.data.success && response.data.data) {
        const apiGoods: Goods[] = response.data.data.content.map((item: any) => ({
          id: item.id,
          title: item.title,
          price: item.price,
          coverImage: item.images?.[0],
          status: item.status,
          viewCount: item.viewCount || 0,
          favoriteCount: item.favoriteCount || 0,
        }));

        setGoods(apiGoods);
      }
    } catch (err: any) {
      console.error('加载用户商品失败:', err);
      toast.error(err.response?.data?.message || '加载商品失败!😭');
    } finally {
      setLoadingGoods(false);
    }
  };

  useEffect(() => {
    loadProfile();
    loadUserGoods();
  }, [userId]);

  // ==================== 事件处理 ====================

  /**
   * 关注/取消关注
   */
  const handleToggleFollow = async () => {
    if (!profile) return;

    try {
      // 乐观更新 UI
      setFollowing(!following);
      setProfile((prev) =>
        prev
          ? {
              ...prev,
              followerCount: following ? prev.followerCount - 1 : prev.followerCount + 1,
            }
          : null
      );

      // 🚀 调用真实后端 API
      if (following) {
        await api.unfollow({ sellerId: profile.id });
        toast.success('取消关注成功!👋');
      } else {
        await api.follow({ sellerId: profile.id });
        toast.success('关注成功!🎉');
      }
    } catch (err: any) {
      console.error('关注操作失败:', err);
      toast.error(err.response?.data?.message || '操作失败!😭');

      // 回滚 UI
      setFollowing(following);
      loadProfile();
    }
  };

  /**
   * 发送消息
   */
  const handleSendMessage = () => {
    if (!profile) return;
    navigate(`/chat?userId=${profile.id}`);
  };

  /**
   * 查看商品详情
   */
  const handleViewGoods = (goodsId: number) => {
    navigate(`/goods/${goodsId}`);
  };

  // ==================== 渲染 ====================

  if (loading) {
    return (
      <div className="user-profile-page">
        <div className="user-profile-container">
          <Skeleton type="card" count={1} animation="wave" />
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="user-profile-page">
        <div className="user-profile-container">
          <div className="user-profile-error">
            <h3>用户不存在</h3>
            <Button onClick={() => navigate(-1)}>返回上一页</Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="user-profile-page">
      <div className="user-profile-container">
        {/* ==================== 用户信息卡片 ==================== */}
        <div className="user-profile-card">
          <div className="user-profile-card__avatar">
            {profile.avatar ? (
              <img src={profile.avatar} alt={profile.username} />
            ) : (
              <div className="avatar-placeholder">👤</div>
            )}
          </div>

          <div className="user-profile-card__info">
            <div className="profile-header">
              <h1 className="user-profile-card__name">{profile.username}</h1>
              {profile.creditLevel && (
                <div 
                  className="profile-credit-badge" 
                  style={{ backgroundColor: CREDIT_LEVEL_CONFIG[profile.creditLevel].color }}
                  title={`信用分: ${profile.creditScore || 100}`}
                  onClick={() => !isOwnProfile ? navigate(`/user/${userId}/credit`) : navigate('/credit')}
                >
                  <span className="credit-icon">{CREDIT_LEVEL_CONFIG[profile.creditLevel].icon}</span>
                  <span className="credit-name">{CREDIT_LEVEL_CONFIG[profile.creditLevel].levelName}</span>
                </div>
              )}
            </div>
            {profile.campusName && <p className="user-profile-card__campus">🏫 {profile.campusName}</p>}
            {profile.bio && <p className="user-profile-card__bio">{profile.bio}</p>}

            <div className="user-profile-card__stats">
              <div className="stat-item">
                <span className="stat-value">{profile.goodsCount}</span>
                <span className="stat-label">商品</span>
              </div>
              <div className="stat-item">
                <span className="stat-value">{profile.followerCount}</span>
                <span className="stat-label">粉丝</span>
              </div>
              <div className="stat-item">
                <span className="stat-value">{profile.followingCount}</span>
                <span className="stat-label">关注</span>
              </div>
            </div>

            {!isOwnProfile && (
              <div className="user-profile-card__actions">
                <Button
                  type={following ? 'default' : 'primary'}
                  onClick={handleToggleFollow}
                >
                  {following ? '已关注' : '+ 关注'}
                </Button>
                <Button type="default" onClick={handleSendMessage}>
                  💬 发消息
                </Button>
                <BlacklistButton
                  userId={profile.id}
                  userName={profile.username}
                  size="middle"
                />
              </div>
            )}
          </div>
        </div>

        {/* ==================== 标签切换 ==================== */}
        <div className="user-profile-tabs">
          <Tabs
            activeKey={activeTab}
            onChange={setActiveTab}
            items={[
              { key: 'goods', label: `🛍️ 商品 (${profile.goodsCount})` },
            ]}
          />
        </div>

        {/* ==================== 内容区域 ==================== */}
        <div className="user-profile-content">
          {loadingGoods ? (
            <Skeleton type="grid" count={6} animation="wave" />
          ) : goods.length === 0 ? (
            <div className="user-profile-empty">
              <div className="empty-icon">📦</div>
              <p className="empty-text">还没有发布商品哦~</p>
            </div>
          ) : (
            <div className="user-profile-goods">
              {goods.map((item) => (
                <GoodsCard
                  key={item.id}
                  id={item.id}
                  title={item.title}
                  price={item.price}
                  coverImage={item.coverImage}
                  viewCount={item.viewCount}
                  favoriteCount={item.favoriteCount}
                  onClick={() => handleViewGoods(item.id)}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default UserProfile;
