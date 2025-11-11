/**
 * 商品列表页 📦
 * @author BaSui 😎
 * @description 商品浏览、筛选、排序、分页
 */

import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { GoodsCard, Skeleton, Empty } from '@campus/shared/components';
import { goodsService } from '@campus/shared/services';;
import type { GoodsResponse } from '@campus/shared/api/models';
import GoodsFilter from './GoodsFilter';
import GoodsSortBar from './GoodsSortBar';
import './GoodsList.css';

/**
 * 将API的GoodsResponse转换为GoodsCard所需的格式
 */
const transformGoodsData = (goods: GoodsResponse) => ({
  id: String(goods.id),
  name: goods.title || '未命名商品',
  description: goods.description,
  price: goods.price || 0,
  imageUrl: goods.coverImage || '/placeholder.jpg',
  images: goods.images || (goods.coverImage ? [goods.coverImage] : ['/placeholder.jpg']),  // ✅ 新增：所有图片（支持轮播）
  status: (goods.status?.toLowerCase() === 'on_sale' ? 'on_sale' : 
           goods.status?.toLowerCase() === 'sold_out' ? 'sold_out' :
           goods.status?.toLowerCase() === 'off_shelf' ? 'off_shelf' : 'pending') as any,
  stock: goods.stock || 1,
  soldCount: goods.soldCount || 0,
  originalPrice: goods.originalPrice ? Number(goods.originalPrice) : undefined,  // ✅ 新增：原价
  tags: goods.tags?.map(t => t.name || '').filter(Boolean),
  seller: goods.sellerId ? {
    id: String(goods.sellerId),
    name: goods.sellerUsername || '匿名用户',
    avatar: goods.sellerAvatar
  } : undefined,
  createdAt: goods.createdAt
});

export const GoodsList: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  // 从URL读取筛选条件
  const [filters, setFilters] = useState({
    keyword: searchParams.get('keyword') || '',
    categoryId: searchParams.get('categoryId') ? Number(searchParams.get('categoryId')) : undefined,
    minPrice: searchParams.get('minPrice') ? Number(searchParams.get('minPrice')) : undefined,
    maxPrice: searchParams.get('maxPrice') ? Number(searchParams.get('maxPrice')) : undefined,
    tags: searchParams.get('tags')?.split(',').filter(Boolean).map(Number) || [],
    sortBy: searchParams.get('sortBy') || 'createdAt',
    sortDirection: searchParams.get('sortDirection') || 'desc',
  });

  const [page, setPage] = useState(Number(searchParams.get('page')) || 0);
  const pageSize = 20;

  // 查询商品列表
  const { data: goodsData, isLoading, error } = useQuery({
    queryKey: ['goods', 'list', filters, page],
    queryFn: async () => {
      const response = await goodsService.listGoods({
        keyword: filters.keyword || undefined,
        categoryId: filters.categoryId,
        minPrice: filters.minPrice,
        maxPrice: filters.maxPrice,
        tags: filters.tags.length > 0 ? filters.tags : undefined,
        sortBy: filters.sortBy,
        sortDirection: filters.sortDirection,
        page,
        size: pageSize,
      });
      // 前端过滤仅保留审核通过的商品，避免后端不支持 status 参数导致 500
      return {
        ...response,
        content: (response.content || []).filter(
          (g) => (g.status || '').toUpperCase() === 'APPROVED'
        ),
      };
    },
    staleTime: 2 * 60 * 1000, // 2分钟缓存
  });

  // 同步筛选条件到URL
  useEffect(() => {
    const params: Record<string, string> = {};
    
    if (filters.keyword) params.keyword = filters.keyword;
    if (filters.categoryId) params.categoryId = String(filters.categoryId);
    if (filters.minPrice) params.minPrice = String(filters.minPrice);
    if (filters.maxPrice) params.maxPrice = String(filters.maxPrice);
    if (filters.tags.length > 0) params.tags = filters.tags.join(',');
    if (filters.sortBy !== 'createdAt') params.sortBy = filters.sortBy;
    if (filters.sortDirection !== 'desc') params.sortDirection = filters.sortDirection;
    if (page > 0) params.page = String(page);

    setSearchParams(params);
  }, [filters, page, setSearchParams]);

  // 处理筛选变更
  const handleFilterChange = (newFilters: Partial<typeof filters>) => {
    setFilters(prev => ({ ...prev, ...newFilters }));
    setPage(0); // 重置到第一页
  };

  // 处理排序变更
  const handleSortChange = (sortBy: string) => {
    setFilters(prev => ({
      ...prev,
      sortBy,
      sortDirection: prev.sortBy === sortBy && prev.sortDirection === 'desc' ? 'asc' : 'desc',
    }));
    setPage(0);
  };

  // 处理商品点击
  const handleGoodsClick = (goodsId: string) => {
    navigate(`/goods/${goodsId}`);
  };

  // 处理分页
  const handlePageChange = (newPage: number) => {
    setPage(newPage);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const goodsList = goodsData?.content || [];
  const totalPages = goodsData?.totalPages || 0;
  const totalElements = goodsData?.totalElements || 0;

  return (
    <div className="goods-list-page">
      <div className="goods-list-container">
        {/* 筛选栏 */}
        <aside className="goods-list-aside">
          <GoodsFilter
            filters={filters}
            onFilterChange={handleFilterChange}
          />
        </aside>

        {/* 主内容区 */}
        <main className="goods-list-main">
          {/* 排序栏 */}
          <GoodsSortBar
            sortBy={filters.sortBy}
            sortDirection={filters.sortDirection}
            totalCount={goodsList.length}
            onSortChange={handleSortChange}
          />

          {/* Loading状态 */}
          {isLoading && (
            <div className="goods-list-grid">
              {Array.from({ length: 12 }).map((_, index) => (
                <Skeleton key={index} type="card" animation="wave" />
              ))}
            </div>
          )}

          {/* 错误状态 */}
          {error && (
            <div className="goods-list-error">
              <Empty
                icon="❌"
                title="加载失败"
                description="无法加载商品列表，请稍后重试"
                action={
                  <button onClick={() => window.location.reload()}>
                    重新加载
                  </button>
                }
              />
            </div>
          )}

          {/* 商品网格 */}
          {!isLoading && !error && goodsList.length > 0 && (
            <>
              <div className="goods-list-grid">
                {goodsList.map((goods) => (
                  <GoodsCard
                    key={goods.id}
                    goods={transformGoodsData(goods)}
                    onCardClick={(goodsData) => handleGoodsClick(goodsData.id)}
                    showSeller={true}
                    showTags={true}
                    hoverable={true}
                  />
                ))}
              </div>

              {/* 分页器 */}
              {totalPages > 1 && (
                <div className="goods-list-pagination">
                  <button
                    className="pagination-btn"
                    disabled={page === 0}
                    onClick={() => handlePageChange(page - 1)}
                  >
                    上一页
                  </button>
                  
                  <div className="pagination-pages">
                    {Array.from({ length: Math.min(totalPages, 7) }).map((_, index) => {
                      let pageNum = index;
                      
                      // 智能分页：显示当前页附近的页码
                      if (totalPages > 7) {
                        if (page < 4) {
                          pageNum = index;
                        } else if (page >= totalPages - 4) {
                          pageNum = totalPages - 7 + index;
                        } else {
                          pageNum = page - 3 + index;
                        }
                      }

                      return (
                        <button
                          key={pageNum}
                          className={`pagination-page ${page === pageNum ? 'active' : ''}`}
                          onClick={() => handlePageChange(pageNum)}
                        >
                          {pageNum + 1}
                        </button>
                      );
                    })}
                  </div>

                  <button
                    className="pagination-btn"
                    disabled={page >= totalPages - 1}
                    onClick={() => handlePageChange(page + 1)}
                  >
                    下一页
                  </button>

                  <span className="pagination-info">
                    本页 {goodsList.length} 件 · 第 {page + 1}/{Math.max(totalPages, 1)} 页
                  </span>
                </div>
              )}
            </>
          )}

          {/* 空状态 */}
          {!isLoading && !error && goodsList.length === 0 && (
            <Empty
              icon="📭"
              title="暂无商品"
              description={filters.keyword ? `没有找到"${filters.keyword}"相关的商品` : '该分类下暂无商品'}
              action={
                filters.keyword || filters.categoryId ? (
                  <button onClick={() => handleFilterChange({ keyword: '', categoryId: undefined, tags: [] })}>
                    清除筛选
                  </button>
                ) : undefined
              }
            />
          )}
        </main>
      </div>
    </div>
  );
};

export default GoodsList;
