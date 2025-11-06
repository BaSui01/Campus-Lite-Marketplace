/**
 * 我的收藏页面 - 珍藏每一个心动瞬间!🌟
 * @author BaSui 😎
 * @description 商品收藏列表、快速访问、一键取消
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Skeleton, Pagination, GoodsCard } from '@campus/shared/components';
import { favoriteService } from '@campus/shared/services';
import { useNotificationStore } from '../../store';
import './Favorites.css';

// ==================== 类型定义 ====================

interface FavoriteGoods {
  goodsId: number;
  title: string;
  price: number;
  coverImage?: string;
  status: string;
  sellerName: string;
  viewCount: number;
  favoriteCount: number;
  favoritedAt: string;
}

/**
 * 收藏列表页面组件
 */
const Favorites: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [goods, setGoods] = useState<FavoriteGoods[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [pageSize] = useState(12);
  const [total, setTotal] = useState(0);
  
  // 排序状态
  const [sortBy, setSortBy] = useState<'createdAt' | 'price' | 'viewCount'>('createdAt');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');
  
  // 统计数据
  const [statistics, setStatistics] = useState({
    total: 0,
    onSale: 0,
    soldOut: 0,
    offShelf: 0,
  });

  // ==================== 数据加载 ====================

  /**
   * 加载收藏列表
   */
  const loadFavorites = async () => {
    setLoading(true);

    try {
      // ✅ 使用 favoriteService 获取收藏列表
      const response = await favoriteService.listFavorites({
        page,
        size: pageSize,
        sortBy,
        sortDirection,
      });

      const apiFavorites: FavoriteGoods[] = (response.content || []).map((item) => ({
        goodsId: item.id || 0,
        title: item.title || '',
        price: item.price || 0,
        coverImage: item.images?.[0],
        status: item.status || 'ON_SALE',
        sellerName: item.sellerName || '未知卖家',
        viewCount: item.viewCount || 0,
        favoriteCount: item.favoriteCount || 0,
        favoritedAt: item.createdAt || '',
      }));

      setGoods(apiFavorites);
      setTotal(response.totalElements || 0);
      
      // 加载统计数据
      loadStatistics();
    } catch (err: any) {
      console.error('加载收藏列表失败:', err);
      toast.error(err.response?.data?.message || '加载收藏列表失败!😭');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 加载统计数据
   */
  const loadStatistics = async () => {
    try {
      const stats = await favoriteService.getFavoriteStatistics();
      setStatistics(stats);
    } catch (err: any) {
      console.error('加载统计数据失败:', err);
    }
  };

  useEffect(() => {
    loadFavorites();
  }, [page, sortBy, sortDirection]);

  // ==================== 事件处理 ====================

  /**
   * 取消收藏
   */
  const handleRemoveFavorite = async (goodsId: number) => {
    if (!window.confirm('确定要取消收藏吗?')) {
      return;
    }

    try {
      // 乐观更新 UI
      setGoods((prev) => prev.filter((g) => g.goodsId !== goodsId));
      setTotal((prev) => prev - 1);

      // ✅ 使用 favoriteService 取消收藏
      await favoriteService.removeFavorite(goodsId);

      toast.success('取消收藏成功!💔');
    } catch (err: any) {
      console.error('取消收藏失败:', err);
      toast.error(err.response?.data?.message || '取消收藏失败!😭');

      // 回滚 UI
      loadFavorites();
    }
  };

  /**
   * 跳转到商品详情
   */
  const handleViewGoods = (goodsId: number) => {
    navigate(`/goods/${goodsId}`);
  };

  /**
   * 翻页
   */
  const handlePageChange = (newPage: number) => {
    setPage(newPage);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  /**
   * 切换排序方式
   */
  const handleSortChange = (newSortBy: typeof sortBy) => {
    if (newSortBy === sortBy) {
      // 同一字段，切换排序方向
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      // 不同字段，使用默认方向
      setSortBy(newSortBy);
      setSortDirection(newSortBy === 'price' ? 'asc' : 'desc');
    }
    setPage(0); // 重置到第一页
  };

  // ==================== 渲染 ====================

  return (
    <div className="favorites-page">
      <div className="favorites-container">
        {/* ==================== 头部 ==================== */}
        <div className="favorites-header">
          <h1 className="favorites-header__title">🌟 我的收藏</h1>
          <p className="favorites-header__subtitle">
            {total > 0 ? `共收藏了 ${total} 个宝贝` : '还没有收藏任何商品哦~'}
          </p>
        </div>

        {/* ==================== 统计卡片 ==================== */}
        {total > 0 && (
          <div className="favorites-stats">
            <div className="stat-card stat-card--total">
              <div className="stat-icon">🌟</div>
              <div className="stat-info">
                <div className="stat-value">{statistics.total}</div>
                <div className="stat-label">总收藏</div>
              </div>
            </div>
            <div className="stat-card stat-card--onsale">
              <div className="stat-icon">🛒</div>
              <div className="stat-info">
                <div className="stat-value">{statistics.onSale}</div>
                <div className="stat-label">在售中</div>
              </div>
            </div>
            <div className="stat-card stat-card--soldout">
              <div className="stat-icon">✅</div>
              <div className="stat-info">
                <div className="stat-value">{statistics.soldOut}</div>
                <div className="stat-label">已售出</div>
              </div>
            </div>
            <div className="stat-card stat-card--offshelf">
              <div className="stat-icon">📦</div>
              <div className="stat-info">
                <div className="stat-value">{statistics.offShelf}</div>
                <div className="stat-label">已下架</div>
              </div>
            </div>
          </div>
        )}

        {/* ==================== 排序选择器 ==================== */}
        {total > 0 && (
          <div className="favorites-toolbar">
            <div className="sort-buttons">
              <button
                className={`sort-btn ${sortBy === 'createdAt' ? 'active' : ''}`}
                onClick={() => handleSortChange('createdAt')}
              >
                按收藏时间
                {sortBy === 'createdAt' && (
                  <span className="sort-icon">{sortDirection === 'desc' ? '↓' : '↑'}</span>
                )}
              </button>
              <button
                className={`sort-btn ${sortBy === 'price' ? 'active' : ''}`}
                onClick={() => handleSortChange('price')}
              >
                按价格
                {sortBy === 'price' && (
                  <span className="sort-icon">{sortDirection === 'desc' ? '↓' : '↑'}</span>
                )}
              </button>
              <button
                className={`sort-btn ${sortBy === 'viewCount' ? 'active' : ''}`}
                onClick={() => handleSortChange('viewCount')}
              >
                按热度
                {sortBy === 'viewCount' && (
                  <span className="sort-icon">{sortDirection === 'desc' ? '↓' : '↑'}</span>
                )}
              </button>
            </div>
          </div>
        )}

        {/* ==================== 收藏列表 ==================== */}
        <div className="favorites-content">
          {loading ? (
            <div className="favorites-loading">
              <Skeleton type="grid" count={12} animation="wave" />
            </div>
          ) : goods.length === 0 ? (
            <div className="favorites-empty">
              <div className="empty-icon">🛒</div>
              <h3 className="empty-text">还没有收藏哦!</h3>
              <p className="empty-tip">快去逛逛,发现心仪的宝贝吧!</p>
              <Button type="primary" size="large" onClick={() => navigate('/')}>
                去逛逛 →
              </Button>
            </div>
          ) : (
            <>
              <div className="favorites-grid">
                {goods.map((item) => (
                  <div key={item.goodsId} className="favorite-item">
                    <GoodsCard
                      id={item.goodsId}
                      title={item.title}
                      price={item.price}
                      coverImage={item.coverImage}
                      sellerName={item.sellerName}
                      viewCount={item.viewCount}
                      favoriteCount={item.favoriteCount}
                      onClick={() => handleViewGoods(item.goodsId)}
                    />
                    <div className="favorite-item__actions">
                      <Button
                        type="default"
                        size="small"
                        onClick={() => handleViewGoods(item.goodsId)}
                      >
                        查看详情
                      </Button>
                      <Button
                        type="danger"
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleRemoveFavorite(item.goodsId);
                        }}
                      >
                        取消收藏
                      </Button>
                    </div>
                  </div>
                ))}
              </div>

              {/* 分页 */}
              {total > pageSize && (
                <div className="favorites-pagination">
                  <Pagination
                    current={page + 1}
                    pageSize={pageSize}
                    total={total}
                    onChange={(newPage) => handlePageChange(newPage - 1)}
                  />
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Favorites;
