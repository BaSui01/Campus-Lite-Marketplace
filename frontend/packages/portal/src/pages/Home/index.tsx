/**
 * 首页 - 商品列表展示 🏠
 * @author BaSui 😎
 * @description 商品列表、搜索、筛选、分页、推荐
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Input, Button, Skeleton } from '@campus/shared/components';
import { goodsService } from '@campus/shared/services/goods';
import type { GoodsResponse, PageGoodsResponse } from '@campus/shared/api/models';
import './Home.css';

/**
 * 首页组件
 */
const Home: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  // 状态管理
  const [goodsList, setGoodsList] = useState<GoodsResponse[]>([]);
  const [recommendList, setRecommendList] = useState<GoodsResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [keyword, setKeyword] = useState(searchParams.get('keyword') || '');
  const [page, setPage] = useState(0);
  const [pageSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [hasMore, setHasMore] = useState(false);

  // 筛选条件
  const [minPrice, setMinPrice] = useState<number | undefined>();
  const [maxPrice, setMaxPrice] = useState<number | undefined>();
  const [sortBy, setSortBy] = useState<string>('createdAt');
  const [sortDirection, setSortDirection] = useState<string>('desc');

  /**
   * 🚀 加载商品列表
   */
  const loadGoodsList = async (isLoadMore = false) => {
    try {
      if (isLoadMore) {
        setLoadingMore(true);
      } else {
        setLoading(true);
      }

      const currentPage = isLoadMore ? page + 1 : 0;

      console.log('[Home] 🚀 加载商品列表:', {
        keyword,
        page: currentPage,
        size: pageSize,
        minPrice,
        maxPrice,
        sortBy,
        sortDirection,
      });

      const response: PageGoodsResponse = await goodsService.listGoods({
        keyword: keyword || undefined,
        page: currentPage,
        size: pageSize,
        minPrice,
        maxPrice,
        sortBy,
        sortDirection,
      });

      console.log('[Home] ✅ 商品列表加载成功:', response);

      const newGoods = response.content || [];

      if (isLoadMore) {
        setGoodsList(prev => [...prev, ...newGoods]);
      } else {
        setGoodsList(newGoods);
      }

      setPage(currentPage);
      setTotalPages(response.totalPages || 0);
      setTotalElements(response.totalElements || 0);
      setHasMore(!response.last);
    } catch (error) {
      console.error('[Home] ❌ 加载商品列表失败:', error);
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  };

  /**
   * 🌟 加载推荐商品
   */
  const loadRecommendGoods = async () => {
    try {
      console.log('[Home] 🌟 加载推荐商品');
      const response = await goodsService.getRecommendGoods(10);
      console.log('[Home] ✅ 推荐商品加载成功:', response);
      setRecommendList(response);
    } catch (error) {
      console.error('[Home] ❌ 加载推荐商品失败:', error);
    }
  };

  /**
   * 🔍 处理搜索
   */
  const handleSearch = () => {
    console.log('[Home] 🔍 搜索商品:', keyword);
    setPage(0);
    setSearchParams(keyword ? { keyword } : {});
    loadGoodsList(false);
  };

  /**
   * 🔄 处理排序变更
   */
  const handleSortChange = (newSortBy: string) => {
    if (sortBy === newSortBy) {
      // 切换排序方向
      setSortDirection(prev => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(newSortBy);
      setSortDirection('desc');
    }
  };

  /**
   * 📄 加载更多
   */
  const handleLoadMore = () => {
    if (!loadingMore && hasMore) {
      loadGoodsList(true);
    }
  };

  /**
   * 🎯 跳转到商品详情
   */
  const handleGoodsClick = (goodsId: number) => {
    navigate(`/goods/${goodsId}`);
  };

  /**
   * 🎣 初始化加载
   */
  useEffect(() => {
    loadGoodsList(false);
    loadRecommendGoods();
  }, [sortBy, sortDirection, minPrice, maxPrice]);

  /**
   * 💰 格式化价格
   */
  const formatPrice = (price?: number) => {
    if (!price) return '¥0';
    return `¥${price.toFixed(2)}`;
  };

  /**
   * 📅 格式化时间
   */
  const formatTime = (time?: string) => {
    if (!time) return '';
    const date = new Date(time);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 1000 / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (minutes < 1) return '刚刚';
    if (minutes < 60) return `${minutes}分钟前`;
    if (hours < 24) return `${hours}小时前`;
    if (days < 7) return `${days}天前`;
    return date.toLocaleDateString();
  };

  return (
    <div className="home-page">
      {/* 搜索栏 */}
      <div className="home-search">
        <div className="home-search__container">
          <Input
            size="large"
            placeholder="搜索商品..."
            value={keyword}
            onChange={e => setKeyword(e.target.value)}
            onPressEnter={handleSearch}
            prefix={<span>🔍</span>}
            allowClear
          />
          <Button type="primary" size="large" onClick={handleSearch}>
            搜索
          </Button>
        </div>
      </div>

      {/* 主内容区 */}
      <div className="home-content">
        {/* 左侧推荐 */}
        <aside className="home-sidebar">
          <div className="home-recommend">
            <h3 className="home-recommend__title">🌟 推荐商品</h3>
            {recommendList.length > 0 ? (
              <div className="home-recommend__list">
                {recommendList.map(goods => (
                  <div
                    key={goods.id}
                    className="home-recommend__item"
                    onClick={() => handleGoodsClick(goods.id!)}
                  >
                    {goods.coverImage && (
                      <img
                        src={goods.coverImage}
                        alt={goods.title}
                        className="home-recommend__item-image"
                      />
                    )}
                    <div className="home-recommend__item-info">
                      <p className="home-recommend__item-title">{goods.title}</p>
                      <p className="home-recommend__item-price">{formatPrice(goods.price)}</p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="home-recommend__empty">暂无推荐</p>
            )}
          </div>
        </aside>

        {/* 右侧商品列表 */}
        <main className="home-main">
          {/* 筛选栏 */}
          <div className="home-filter">
            <div className="home-filter__sort">
              <button
                className={`home-filter__sort-item ${sortBy === 'createdAt' ? 'active' : ''}`}
                onClick={() => handleSortChange('createdAt')}
              >
                最新发布 {sortBy === 'createdAt' && (sortDirection === 'desc' ? '↓' : '↑')}
              </button>
              <button
                className={`home-filter__sort-item ${sortBy === 'price' ? 'active' : ''}`}
                onClick={() => handleSortChange('price')}
              >
                价格 {sortBy === 'price' && (sortDirection === 'desc' ? '↓' : '↑')}
              </button>
              <button
                className={`home-filter__sort-item ${sortBy === 'viewCount' ? 'active' : ''}`}
                onClick={() => handleSortChange('viewCount')}
              >
                浏览量 {sortBy === 'viewCount' && (sortDirection === 'desc' ? '↓' : '↑')}
              </button>
            </div>
            <div className="home-filter__info">
              找到 <span className="home-filter__count">{totalElements}</span> 件商品
            </div>
          </div>

          {/* 商品列表 */}
          {loading ? (
            <div className="home-goods-grid">
              {/* 显示 8 个骨架屏卡片 */}
              {Array.from({ length: 8 }).map((_, index) => (
                <Skeleton key={index} type="card" animation="wave" />
              ))}
            </div>
          ) : goodsList.length > 0 ? (
            <>
              <div className="home-goods-grid">
                {goodsList.map(goods => (
                  <div
                    key={goods.id}
                    className="home-goods-card"
                    onClick={() => handleGoodsClick(goods.id!)}
                  >
                    {/* 商品图片 */}
                    <div className="home-goods-card__image">
                      {goods.coverImage ? (
                        <img src={goods.coverImage} alt={goods.title} />
                      ) : (
                        <div className="home-goods-card__placeholder">📦</div>
                      )}
                    </div>

                    {/* 商品信息 */}
                    <div className="home-goods-card__info">
                      <h3 className="home-goods-card__title">{goods.title}</h3>
                      <p className="home-goods-card__desc">{goods.description}</p>

                      {/* 标签 */}
                      {goods.tags && goods.tags.length > 0 && (
                        <div className="home-goods-card__tags">
                          {goods.tags.map(tag => (
                            <span key={tag.id} className="home-goods-card__tag">
                              #{tag.name}
                            </span>
                          ))}
                        </div>
                      )}

                      {/* 底部信息 */}
                      <div className="home-goods-card__footer">
                        <div className="home-goods-card__price">{formatPrice(goods.price)}</div>
                        <div className="home-goods-card__meta">
                          <span>👁️ {goods.viewCount}</span>
                          <span>❤️ {goods.favoriteCount}</span>
                          <span>{formatTime(goods.createdAt)}</span>
                        </div>
                      </div>

                      {/* 卖家信息 */}
                      <div className="home-goods-card__seller">
                        <span>👤 {goods.sellerUsername}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {/* 加载更多按钮 */}
              {hasMore && (
                <div className="home-load-more">
                  <Button
                    type="default"
                    size="large"
                    loading={loadingMore}
                    onClick={handleLoadMore}
                  >
                    {loadingMore ? '加载中...' : '加载更多'}
                  </Button>
                </div>
              )}

              {/* 分页信息 */}
              {!hasMore && goodsList.length > 0 && (
                <div className="home-pagination-info">
                  已加载全部商品（共 {totalElements} 件）
                </div>
              )}
            </>
          ) : (
            <div className="home-empty">
              <p className="home-empty__icon">📭</p>
              <p className="home-empty__text">暂无商品</p>
              <p className="home-empty__tip">换个关键词试试？</p>
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

export default Home;
