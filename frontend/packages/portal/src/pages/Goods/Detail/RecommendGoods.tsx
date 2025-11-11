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
  status: (goods.status?.toLowerCase() === 'on_sale' ? 'on_sale' : 
           goods.status?.toLowerCase() === 'sold_out' ? 'sold_out' :
           goods.status?.toLowerCase() === 'off_shelf' ? 'off_shelf' : 'pending') as any,
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
  const { data: recommendGoods, isLoading } = useQuery({
    queryKey: ['goods', 'recommend', categoryId, currentGoodsId],
    queryFn: async () => {
      const response = await goodsService.listGoods({
        categoryId,
        page: 0,
        size: 4,
        sortBy: 'createdAt',
        sortDirection: 'DESC',
      });
      
      // 过滤掉当前商品
      return response.content?.filter(g => g.id !== currentGoodsId) || [];
    },
    enabled: !!categoryId,
    staleTime: 10 * 60 * 1000, // 10分钟缓存
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
      {!isLoading && (!recommendGoods || recommendGoods.length === 0) && (
        <Empty
          icon="📭"
          title="暂无相似商品"
          description="该分类下暂时没有其他商品"
        />
      )}
    </div>
  );
};

export default RecommendGoods;
