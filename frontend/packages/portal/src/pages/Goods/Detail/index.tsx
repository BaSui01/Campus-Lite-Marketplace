/**
 * 商品详情页 🛍️
 * @author BaSui 😎
 * @description 商品详细信息、图片画廊、卖家信息、操作按钮
 */

import React, { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Skeleton, Empty } from '@campus/shared/components';
import { goodsService } from '@campus/shared/services';
import { useNotificationStore } from '../../../store';
import { useAuthStore } from '../../../store';
import ImageGallery from './ImageGallery';
import GoodsInfo from './GoodsInfo';
import SellerCard from './SellerCard';
import ActionBar from './ActionBar';
import RecommendGoods from './RecommendGoods';
import ReviewList from './ReviewList';
import './GoodsDetail.css';

export const GoodsDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const toast = useNotificationStore();
  const currentUser = useAuthStore((state) => state.user);
  const goodsId = Number(id);

  // 获取商品详情
  const { data: goods, isLoading, error } = useQuery({
    queryKey: ['goods', 'detail', goodsId],
    queryFn: async () => {
      const response = await goodsService.getGoodsDetail(goodsId);
      return response;
    },
    enabled: !!goodsId,
    staleTime: 5 * 60 * 1000, // 5分钟缓存
  });

  // 收藏/取消收藏
  const favoriteMutation = useMutation({
    mutationFn: async (isFavorited: boolean) => {
      // 检查登录状态
      if (!currentUser) {
        throw new Error('请先登录');
      }

      if (isFavorited) {
        await goodsService.unfavoriteGoods(goodsId);
      } else {
        await goodsService.favoriteGoods(goodsId);
      }
    },
    onSuccess: (_, isFavorited) => {
      // 显示成功提示
      toast.success(isFavorited ? '取消收藏成功！' : '收藏成功！🎉');
      
      // 刷新商品详情
      queryClient.invalidateQueries({ queryKey: ['goods', 'detail', goodsId] });
    },
    onError: (error: any) => {
      // 显示错误提示
      const message = error.message || '操作失败，请稍后重试';
      toast.error(message);
      
      // 如果是未登录错误，跳转到登录页
      if (message.includes('登录')) {
        setTimeout(() => {
          navigate('/login', { state: { from: `/goods/${goodsId}` } });
        }, 1500);
      }
    },
  });

  // 记录浏览历史（LocalStorage）
  useEffect(() => {
    if (goods && goodsId) {
      const history = JSON.parse(localStorage.getItem('goods_history') || '[]');
      const newHistory = [
        goodsId,
        ...history.filter((id: number) => id !== goodsId),
      ].slice(0, 20); // 最多保存20条
      localStorage.setItem('goods_history', JSON.stringify(newHistory));
    }
  }, [goods, goodsId]);

  // 处理收藏
  const handleFavorite = () => {
    // 检查登录状态
    if (!currentUser) {
      toast.warning('请先登录！');
      navigate('/login', { state: { from: `/goods/${goodsId}` } });
      return;
    }

    favoriteMutation.mutate(!!goods?.isFavorited);
  };

  // 处理立即购买
  const handleBuy = () => {
    // 检查登录状态
    if (!currentUser) {
      toast.warning('请先登录！');
      navigate('/login', { state: { from: `/goods/${goodsId}` } });
      return;
    }

    // 检查是否是自己的商品
    if (goods?.sellerId === currentUser?.id) {
      toast.error('不能购买自己的商品！');
      return;
    }

    // 检查商品状态
    if (goods?.status !== 'APPROVED') {
      toast.error('该商品暂时无法购买！');
      return;
    }

    // 跳转到创建订单页面
    navigate(`/order/create?goodsId=${goodsId}`);
  };

  // 处理联系卖家
  const handleContact = () => {
    // 检查登录状态
    if (!currentUser) {
      toast.warning('请先登录！');
      navigate('/login', { state: { from: `/goods/${goodsId}` } });
      return;
    }

    // 检查是否是自己
    if (goods?.sellerId === currentUser?.id) {
      toast.info('这是您自己的商品！');
      return;
    }

    if (goods?.sellerId) {
      navigate(`/chat?userId=${goods.sellerId}`);
    }
  };

  // 处理分享
  const handleShare = () => {
    const url = window.location.href;
    if (navigator.share) {
      navigator.share({
        title: goods?.title || '商品分享',
        text: goods?.description,
        url,
      }).then(() => {
        toast.success('分享成功！🎉');
      }).catch(() => {
        // 浏览器不支持，复制链接
        navigator.clipboard.writeText(url);
        toast.success('链接已复制到剪贴板！📋');
      });
    } else {
      navigator.clipboard.writeText(url);
      toast.success('链接已复制到剪贴板！📋');
    }
  };

  // 处理举报
  const handleReport = () => {
    // 检查登录状态
    if (!currentUser) {
      toast.warning('请先登录！');
      navigate('/login', { state: { from: `/goods/${goodsId}` } });
      return;
    }

    navigate(`/report?type=goods&id=${goodsId}`);
  };

  // Loading状态
  if (isLoading) {
    return (
      <div className="goods-detail-page">
        <div className="goods-detail-container">
          <div className="goods-detail-main">
            <Skeleton type="image" />
            <div className="goods-detail-content">
              <Skeleton type="title" />
              <Skeleton type="paragraph" count={5} />
            </div>
          </div>
        </div>
      </div>
    );
  }

  // 错误状态
  if (error || !goods) {
    return (
      <div className="goods-detail-page">
        <div className="goods-detail-container">
          <Empty
            icon="❌"
            title="商品不存在"
            description="该商品可能已被删除或下架"
            action={
              <button onClick={() => navigate('/goods')}>
                返回商品列表
              </button>
            }
          />
        </div>
      </div>
    );
  }

  return (
    <div className="goods-detail-page">
      <div className="goods-detail-container">
        {/* 主要内容区 */}
        <div className="goods-detail-main">
          {/* 图片画廊 */}
          <ImageGallery
            images={goods.images || [goods.coverImage].filter(Boolean)}
          />

          {/* 商品信息 */}
          <div className="goods-detail-content">
            <GoodsInfo goods={goods} />

            {/* 操作按钮栏 */}
            <ActionBar
              isFavorited={!!goods.isFavorited}
              isOwner={goods.sellerId === currentUser?.id}
              isFavoriteLoading={favoriteMutation.isPending}
              onFavorite={handleFavorite}
              onBuy={handleBuy}
              onContact={handleContact}
              onShare={handleShare}
              onReport={handleReport}
            />

            {/* 卖家信息卡片 */}
            {goods.seller && (
              <SellerCard
                sellerId={goods.seller.id!}
                sellerName={goods.seller.username || '匿名用户'}
                sellerAvatar={goods.seller.avatar}
                sellerRating={goods.seller.rating}  // 🆕 卖家评分
                sellerGoodsCount={goods.seller.goodsCount}  // 🆕 在售商品数量
                onContact={handleContact}
              />
            )}
          </div>
        </div>

        {/* 商品评价 */}
        <ReviewList goodsId={goodsId} />

        {/* 相似推荐 */}
        <RecommendGoods
          currentGoodsId={goodsId}
          categoryId={goods.categoryId}
        />
      </div>
    </div>
  );
};

export default GoodsDetail;
