/**
 * 相似推荐组件 🔄
 * @author BaSui 😎
 * @description 推荐相似商品
 */

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { GoodsCard, Skeleton, Empty } from '@campus/shared/components';
import { goodsService } from '@campus/shared/services';;
import type { GoodsResponse } from '@campus/shared/api/models';
import './RecommendGoods.css';

interface RecommendGoodsProps {
  currentGoodsId: number;
  categoryId?: number;
}

/**
 * 将API的GoodsResponse转换为GoodsCard所需的格式
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
  stock: 1,
  soldCount: 0,
  tags: goods.tags?.map(t => t.name || '').filter(Boolean),
  seller: goods.sellerId ? {
    id: String(goods.sellerId),
    name: goods.sellerUsername || '匿名用户',
    avatar: goods.sellerAvatar
  } : undefined,
  createdAt: goods.createdAt
});

export const RecommendGoods: React.FC<RecommendGoodsProps> = ({
  currentGoodsId,
  categoryId,
}) => {
  const navigate = useNavigate();

  // 获取相似商品（同分类）
  const { data: recommendGoods, isLoading, isError } = useQuery({
    queryKey: ['goods', 'recommend', categoryId, currentGoodsId],
    queryFn: async () => {
      try {
        const response = await goodsService.listGoods({
          categoryId,
          page: 0,
          size: 4,
          sortBy: 'createdAt',
          sortDirection: 'DESC',
        });
        
        // 过滤掉当前商品 + 仅保留已审核通过（后端 listGoods 不支持 status，前端过滤）
        return response.content
          ?.filter(g => g.id !== currentGoodsId)
          ?.filter(g => (g.status || '').toUpperCase() === 'APPROVED') || [];
      } catch (e) {
        // 降级兜底：使用热门推荐（不基于分类）
        const hot = await goodsService.getRecommendGoods(4);
        return hot
          ?.filter(g => g.id !== currentGoodsId)
          ?.filter(g => (g.status || '').toUpperCase() === 'APPROVED') || [];
      }
    },
    enabled: !!categoryId,
    staleTime: 10 * 60 * 1000, // 10分钟缓存
    retry: 1, // 失败只重试1次
  });

  const handleGoodsClick = (goodsId: string) => {
    navigate(`/goods/${goodsId}`);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  if (!categoryId) return null;

  return (
    <div className="recommend-goods">
      <h2 className="recommend-goods__title">🔄 相似推荐</h2>

      {/* Loading状态 */}
      {isLoading && (
        <div className="recommend-goods__grid">
          {Array.from({ length: 4 }).map((_, index) => (
            <Skeleton key={index} type="card" animation="wave" />
          ))}
        </div>
      )}

      {/* 推荐列表 */}
      {!isLoading && recommendGoods && recommendGoods.length > 0 && (
        <div className="recommend-goods__grid">
          {recommendGoods.slice(0, 4).map((goods) => (
            <GoodsCard
              key={goods.id}
              goods={transformGoodsData(goods)}
              onCardClick={(goodsData) => handleGoodsClick(goodsData.id)}
              showSeller={true}
              showTags={false}
              hoverable={true}
            />
          ))}
        </div>
      )}

      {/* 空状态 */}
      {!isLoading && !isError && (!recommendGoods || recommendGoods.length === 0) && (
        <Empty
          icon="📭"
          title="暂无相似商品"
          description="该分类下暂时没有其他商品"
        />
      )}

      {/* 错误状态 */}
      {isError && (
        <Empty
          icon="⚠️"
          title="加载失败"
          description="推荐商品加载失败，请稍后再试"
        />
      )}
    </div>
  );
};

export default RecommendGoods;
