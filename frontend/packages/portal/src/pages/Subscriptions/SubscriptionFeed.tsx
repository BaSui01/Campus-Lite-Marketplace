/**
 * 订阅动态流页面 - 不错过任何心仪商品！📰
 * @author BaSui 😎
 * @description 时间轴展示订阅匹配的最新商品
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Skeleton, GoodsCard } from '@campus/shared/components';
import { subscriptionService } from '../../services/subscription';;
import { useNotificationStore } from '../../store';
import './SubscriptionFeed.css';

// ==================== 类型定义 ====================

interface FeedItem {
  subscriptionId: number;
  keyword: string;
  matchedGoods: any;
  matchedAt: string;
}

/**
 * 订阅动态流页面组件
 */
const SubscriptionFeed: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [feed, setFeed] = useState<FeedItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  // ==================== 数据加载 ====================

  /**
   * 加载订阅动态流
   */
  const loadFeed = async (pageNum: number = 0) => {
    if (pageNum === 0) {
      setLoading(true);
    }

    try {
      // ⚠️ 使用 subscriptionService.getSubscriptionFeed()
      // 注意：该方法目前是占位实现，需要后端提供 API 支持
      const response = await subscriptionService.getSubscriptionFeed(pageNum, 20);
      
      if (pageNum === 0) {
        setFeed(response);
      } else {
        setFeed((prev) => [...prev, ...response]);
      }
      
      setHasMore(response.length >= 20);
    } catch (err: any) {
      console.error('加载订阅动态失败:', err);
      // 不显示错误提示，因为这是预期的（后端未实现）
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFeed();
  }, []);

  // ==================== 事件处理 ====================

  /**
   * 加载更多
   */
  const handleLoadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    loadFeed(nextPage);
  };

  /**
   * 格式化时间
   */
  const formatTime = (time: string) => {
    const date = new Date(time);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / (1000 * 60));
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));

    if (minutes < 1) return '刚刚';
    if (minutes < 60) return `${minutes} 分钟前`;
    if (hours < 24) return `${hours} 小时前`;
    if (days < 30) return `${days} 天前`;
    
    return date.toLocaleDateString('zh-CN');
  };

  // ==================== 渲染 ====================

  return (
    <div className="subscription-feed-page">
      <div className="subscription-feed-container">
        {/* ==================== 头部 ==================== */}
        <div className="feed-header">
          <button className="back-btn" onClick={() => navigate('/subscriptions')}>
            ← 返回订阅管理
          </button>
          <h1 className="feed-header__title">📰 订阅动态流</h1>
          <p className="feed-header__subtitle">
            为你推荐订阅关键词匹配的最新商品
          </p>
        </div>

        {/* ==================== 动态流 ==================== */}
        <div className="feed-content">
          {loading ? (
            <div className="feed-loading">
              <Skeleton type="grid" count={6} animation="wave" />
            </div>
          ) : feed.length === 0 ? (
            <div className="feed-empty">
              <div className="empty-icon">📭</div>
              <h3 className="empty-text">暂无订阅动态</h3>
              <p className="empty-tip">
                订阅关键词后，匹配的新商品会在这里显示
              </p>
              <p className="empty-note">
                ⚠️ 该功能需要后端 API 支持：<br/>
                <code>GET /subscribe/feed</code>
              </p>
              <Button type="primary" size="large" onClick={() => navigate('/subscriptions')}>
                去管理订阅 →
              </Button>
            </div>
          ) : (
            <>
              <div className="feed-timeline">
                {feed.map((item, index) => (
                  <div key={index} className="timeline-item">
                    {/* 时间轴节点 */}
                    <div className="timeline-dot"></div>
                    <div className="timeline-line"></div>
                    
                    {/* 动态内容 */}
                    <div className="timeline-content">
                      <div className="timeline-header">
                        <span className="timeline-badge">🔔 订阅匹配</span>
                        <span className="timeline-keyword">关键词: {item.keyword}</span>
                        <span className="timeline-time">{formatTime(item.matchedAt)}</span>
                      </div>
                      
                      <div className="timeline-goods">
                        <GoodsCard
                          id={item.matchedGoods.id}
                          title={item.matchedGoods.title}
                          price={item.matchedGoods.price}
                          coverImage={item.matchedGoods.images?.[0]}
                          sellerName={item.matchedGoods.sellerName}
                          viewCount={item.matchedGoods.viewCount}
                          favoriteCount={item.matchedGoods.favoriteCount}
                          onClick={() => navigate(`/goods/${item.matchedGoods.id}`)}
                        />
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {/* 加载更多按钮 */}
              {hasMore && (
                <div className="feed-loadmore">
                  <Button type="default" size="large" onClick={handleLoadMore}>
                    加载更多 →
                  </Button>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default SubscriptionFeed;
