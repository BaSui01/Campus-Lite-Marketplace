/**
 * HotGoods 热门商品区域 🔥
 * @author BaSui 😎
 * @description 展示热门商品，充分利用共享层组件
 */

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { GoodsCard, Skeleton, Empty } from '@campus/shared/components';
import { goodsService } from '@campus/shared/services';;
import type { GoodsResponse } from '@campus/shared/api/models';

/**
 * 将API的GoodsResponse转换为GoodsCard所需的GoodsData格式
 */
const transformGoodsData = (goods: GoodsResponse) => ({
  id: String(goods.id),
  name: goods.title || '未命名商品',
  description: goods.description,
  price: goods.price || 0,
  imageUrl: goods.coverImage || '/placeholder.jpg',
  // 后端状态（APPROVED/SOLD/OFFLINE/PENDING/REJECTED/LOCKED）→ 卡片状态
  status: (() => {
    const s = (goods.status || '').toUpperCase();
    if (s === 'APPROVED') return 'on_sale';
    if (s === 'SOLD') return 'sold_out';
    if (s === 'OFFLINE' || s === 'REJECTED') return 'off_shelf';
    return 'pending';
  })() as any,
  stock: 1, // 二手商品通常是1
  soldCount: 0,
  tags: goods.tags?.map(t => t.name || '').filter(Boolean),
  seller: goods.sellerId ? {
    id: String(goods.sellerId),
    name: goods.sellerUsername || '匿名用户',
    avatar: goods.sellerAvatar
  } : undefined,
  createdAt: goods.createdAt
});

export const HotGoods: React.FC = () => {
  const navigate = useNavigate();

  // 使用goodsService获取热门商品（充分利用共享层！）
  const { data: hotGoods, isLoading, error } = useQuery({
    queryKey: ['goods', 'hot'],
    queryFn: async () => {
      const response = await goodsService.getRecommendGoods(12);
      return response;
    },
    staleTime: 5 * 60 * 1000, // 5分钟内缓存有效
  });

  // 处理商品点击
  const handleGoodsClick = (goodsId: string) => {
    navigate(`/goods/${goodsId}`);
  };

  return (
    <section className="hot-goods">
      <div className="hot-goods__header">
        <h2 className="hot-goods__title">🔥 热门商品</h2>
        <button
          className="hot-goods__more"
          onClick={() => navigate('/goods?sort=viewCount')}
        >
          查看更多 →
        </button>
      </div>

      {/* Loading状态 - 使用共享层Skeleton */}
      {isLoading && (
        <div className="hot-goods__grid">
          {Array.from({ length: 12 }).map((_, index) => (
            <Skeleton key={index} type="card" animation="wave" />
          ))}
        </div>
      )}

      {/* 错误状态 */}
      {error && (
        <div className="hot-goods__error">
          <Empty
            icon="❌"
            title="加载失败"
            description="无法加载热门商品，请稍后重试"
            action={
              <button onClick={() => window.location.reload()}>
                重新加载
              </button>
            }
          />
        </div>
      )}

      {/* 商品列表 - 使用共享层GoodsCard */}
      {!isLoading && !error && hotGoods && hotGoods.length > 0 && (
        <div className="hot-goods__grid">
          {hotGoods.map((goods) => (
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
      )}

      {/* 空状态 */}
      {!isLoading && !error && (!hotGoods || hotGoods.length === 0) && (
        <Empty
          icon="📭"
          title="暂无商品"
          description="还没有热门商品，快来发布第一个吧！"
        />
      )}
    </section>
  );
};

export default HotGoods;
