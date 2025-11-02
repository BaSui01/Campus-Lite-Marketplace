/**
 * 我的收藏页面 - 珍藏每一个心动瞬间!🌟
 * @author BaSui 😎
 * @description 商品收藏列表、快速访问、一键取消
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Skeleton, Pagination, GoodsCard } from '@campus/shared/components';
import { useNotificationStore } from '../../store';
import { getApi } from '@campus/shared/utils';
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

  // API 实例
  const api = getApi();

  // ==================== 数据加载 ====================

  /**
   * 加载收藏列表
   */
  const loadFavorites = async () => {
    setLoading(true);

    try {
      // 🚀 调用真实后端 API 获取收藏列表
      const response = await api.listFavorites({ page, size: pageSize });

      if (response.data.success && response.data.data) {
        const apiFavorites: FavoriteGoods[] = response.data.data.content.map((item: any) => ({
          goodsId: item.id,
          title: item.title,
          price: item.price,
          coverImage: item.images?.[0],
          status: item.status,
          sellerName: item.sellerName || '未知卖家',
          viewCount: item.viewCount || 0,
          favoriteCount: item.favoriteCount || 0,
          favoritedAt: item.createdAt,
        }));

        setGoods(apiFavorites);
        setTotal(response.data.data.totalElements || 0);
      }
    } catch (err: any) {
      console.error('加载收藏列表失败:', err);
      toast.error(err.response?.data?.message || '加载收藏列表失败!😭');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFavorites();
  }, [page]);

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

      // 🚀 调用真实后端 API 取消收藏
      await api.removeFavorite({ goodsId });

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
