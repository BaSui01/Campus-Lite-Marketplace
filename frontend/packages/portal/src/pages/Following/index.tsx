/**
 * 我的关注页面 - 追随你感兴趣的人!👥
 * @author BaSui 😎
 * @description 关注列表、取消关注、查看主页
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Skeleton, Avatar } from '@campus/shared/components';
import { followService } from '@campus/shared/services';
import { useNotificationStore } from '../../store';
import './Following.css';

// ==================== 类型定义 ====================

interface Following {
  sellerId: number;
  sellerName: string;
  sellerAvatar?: string;
  followedAt: string;
}

/**
 * 关注列表页面组件
 */
const Following: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [followings, setFollowings] = useState<Following[]>([]);
  const [loading, setLoading] = useState(true);

  // ==================== 数据加载 ====================

  /**
   * 加载关注列表
   */
  const loadFollowings = async () => {
    setLoading(true);

    try {
      // ✅ 使用 followService 获取关注列表
      const response = await followService.listFollowings();

      const apiFollowings: Following[] = response.map((item) => ({
        sellerId: item.sellerId || 0,
        sellerName: item.sellerName || '未知用户',
        sellerAvatar: item.sellerAvatar,
        followedAt: item.followedAt || '',
      }));

      setFollowings(apiFollowings);
    } catch (err: any) {
      console.error('加载关注列表失败:', err);
      toast.error(err.response?.data?.message || '加载关注列表失败!😭');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFollowings();
  }, []);

  // ==================== 事件处理 ====================

  /**
   * 取消关注
   */
  const handleUnfollow = async (sellerId: number, sellerName: string) => {
    if (!window.confirm(`确定要取消关注「${sellerName}」吗?`)) {
      return;
    }

    try {
      // 乐观更新 UI
      setFollowings((prev) => prev.filter((f) => f.sellerId !== sellerId));

      // ✅ 使用 followService 取消关注
      await followService.unfollowSeller(sellerId);

      toast.success('取消关注成功!👋');
    } catch (err: any) {
      console.error('取消关注失败:', err);
      toast.error(err.response?.data?.message || '取消关注失败!😭');

      // 回滚 UI
      loadFollowings();
    }
  };

  /**
   * 查看用户主页
   */
  const handleViewProfile = (sellerId: number) => {
    navigate(`/users/${sellerId}`);
  };

  /**
   * 格式化时间
   */
  const formatTime = (time: string) => {
    const date = new Date(time);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));

    if (days === 0) {
      return '今天关注';
    } else if (days === 1) {
      return '昨天关注';
    } else if (days < 30) {
      return `${days} 天前关注`;
    } else if (days < 365) {
      return `${Math.floor(days / 30)} 个月前关注`;
    } else {
      return `${Math.floor(days / 365)} 年前关注`;
    }
  };

  // ==================== 渲染 ====================

  return (
    <div className="following-page">
      <div className="following-container">
        {/* ==================== 头部 ==================== */}
        <div className="following-header">
          <h1 className="following-header__title">👥 我的关注</h1>
          <p className="following-header__subtitle">
            {followings.length > 0 ? `关注了 ${followings.length} 个用户` : '还没有关注任何人哦~'}
          </p>
        </div>

        {/* ==================== 关注列表 ==================== */}
        <div className="following-content">
          {loading ? (
            <div className="following-loading">
              <Skeleton type="list" count={6} animation="wave" />
            </div>
          ) : followings.length === 0 ? (
            <div className="following-empty">
              <div className="empty-icon">👤</div>
              <h3 className="empty-text">还没有关注哦!</h3>
              <p className="empty-tip">快去发现感兴趣的用户吧!</p>
              <Button type="primary" size="large" onClick={() => navigate('/community')}>
                去社区逛逛 →
              </Button>
            </div>
          ) : (
            <div className="following-list">
              {followings.map((item) => (
                <div key={item.sellerId} className="following-item">
                  <div className="following-item__avatar" onClick={() => handleViewProfile(item.sellerId)}>
                    {item.sellerAvatar ? (
                      <img src={item.sellerAvatar} alt={item.sellerName} />
                    ) : (
                      <div className="avatar-placeholder">👤</div>
                    )}
                  </div>
                  <div className="following-item__info">
                    <div className="following-item__name" onClick={() => handleViewProfile(item.sellerId)}>
                      {item.sellerName}
                    </div>
                    <div className="following-item__time">{formatTime(item.followedAt)}</div>
                  </div>
                  <div className="following-item__actions">
                    <Button
                      type="default"
                      size="small"
                      onClick={() => handleViewProfile(item.sellerId)}
                    >
                      查看主页
                    </Button>
                    <Button
                      type="danger"
                      size="small"
                      onClick={() => handleUnfollow(item.sellerId, item.sellerName)}
                    >
                      取消关注
                    </Button>
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

export default Following;
