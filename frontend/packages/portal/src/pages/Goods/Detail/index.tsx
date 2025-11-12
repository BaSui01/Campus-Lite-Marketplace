/**
 * 商品详情页 🛍️
 * @author BaSui 😎
 * @description 商品详细信息、图片画廊、卖家信息、操作按钮
 */

import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Skeleton, Empty, Modal, Input, Button } from '@campus/shared/components';
import { goodsService, messageService, orderService } from '@campus/shared/services';
import { useNotificationStore } from '../../../store';
import { useAuthStore } from '../../../store';
import ImageGallery from './ImageGallery';
import GoodsInfo from './GoodsInfo';
import SellerCard from './SellerCard';
import ActionBar from './ActionBar';
import RecommendGoods from './RecommendGoods';
import ReviewList from './ReviewList';
import ReviewStats from './ReviewStats';
import './GoodsDetail.css';
import type { Order, PageOrderResponse, OrderResponse } from '@campus/shared/api/models';

export const GoodsDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const toast = useNotificationStore();
  const currentUser = useAuthStore((state) => state.user);
  const goodsId = Number(id);

  // 联系卖家弹窗状态
  const [contactVisible, setContactVisible] = useState(false);
  const [contactMessage, setContactMessage] = useState('');
  const [contactLoading, setContactLoading] = useState(false);
  // 评价选择弹窗
  const [reviewVisible, setReviewVisible] = useState(false);
  const [eligibleOrders, setEligibleOrders] = useState<OrderResponse[]>([]);
  const [loadingEligible, setLoadingEligible] = useState(false);

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

  // 处理联系卖家（弹出消息框）
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

    // 打开联系卖家弹窗
    setContactVisible(true);
  };

  // 发送首条消息
  const handleSendContact = async () => {
    if (!goods?.sellerId) return;
    const content = contactMessage.trim();
    if (!content) {
      toast.warning('请输入要发送的消息');
      return;
    }
    setContactLoading(true);
    try {
      await messageService.sendMessage({
        receiverId: goods.sellerId,
        messageType: 'TEXT',
        content,
      });
      toast.success('消息已发送！');
      setContactVisible(false);
      setContactMessage('');
      // 跳转到聊天页并定位到该卖家
      navigate(`/chat?userId=${goods.sellerId}`);
    } catch (err: any) {
      console.error('发送消息失败：', err);
      toast.error(err?.response?.data?.message || '发送消息失败，请稍后重试');
    } finally {
      setContactLoading(false);
    }
  };

  // 打开“选择订单进行评价”
  const handleReview = async () => {
    if (!currentUser) {
      toast.warning('请先登录！');
      navigate('/login', { state: { from: `/goods/${goodsId}` } });
      return;
    }
    setLoadingEligible(true);
    try {
      const respCompleted: PageOrderResponse = await orderService.listBuyerOrders({
        status: 'COMPLETED',
        page: 0,
        size: 50,
      });
      const listCompleted = (respCompleted.content || []).filter((o) => o.goodsId === goodsId);

      const respDelivered: PageOrderResponse = await orderService.listBuyerOrders({
        status: 'DELIVERED',
        page: 0,
        size: 50,
      });
      const listDelivered = (respDelivered.content || []).filter((o) => o.goodsId === goodsId);

      const candidates = [...listCompleted, ...listDelivered];

      if (candidates.length === 0) {
        toast.info('暂无可评价的相关订单，请完成交易后再来评价～');
        return;
      }

      if (candidates.length === 1) {
        const orderNo = candidates[0].orderNo!;
        const detail: Order = await orderService.getOrderDetail(orderNo);
        navigate('/review/create', { state: { order: detail } });
        return;
      }

      setEligibleOrders(candidates);
      setReviewVisible(true);
    } catch (e: any) {
      toast.error(e?.message || '获取可评价订单失败，请稍后再试');
    } finally {
      setLoadingEligible(false);
    }
  };

  // 选择订单并跳转评价页
  const handleSelectOrderForReview = async (orderNo: string) => {
    try {
      const detail: Order = await orderService.getOrderDetail(orderNo);
      setReviewVisible(false);
      navigate('/review/create', { state: { order: detail } });
    } catch (e: any) {
      toast.error(e?.message || '获取订单详情失败，请稍后再试');
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
              onReview={handleReview}
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

        {/* 商品评价统计（点击星级联动列表） */}
        <ReviewStats
          goodsId={goodsId}
          initialStats={goods.reviewStatistics as any}
          onSelectRating={(r) => {
            // 通过锚点跳转并广播一个自定义事件，交由 ReviewList 受控 props 处理
            document.getElementById('review-list')?.scrollIntoView({ behavior: 'smooth' });
            // 使用 URL 哈希存储一次性过滤（退路方案）
            if (r) {
              history.replaceState(null, '', `#rating=${r}`);
            } else {
              history.replaceState(null, '', `#reviews`);
            }
            // 触发一个全局事件（由 ReviewList 监听并同步到内部受控状态）
            const ev = new CustomEvent('goods-review:setRating', { detail: { rating: r } });
            window.dispatchEvent(ev);
          }}
          onSelectGroup={(g) => {
            document.getElementById('review-list')?.scrollIntoView({ behavior: 'smooth' });
            history.replaceState(null, '', `#group=${g}`);
            const ev = new CustomEvent('goods-review:setGroup', { detail: { group: g } });
            window.dispatchEvent(ev);
          }}
        />

        {/* 商品评价列表 */}
        <ReviewList
          goodsId={goodsId}
          /* 受控：通过全局事件同步（见上） */
          onRatingChange={(rating) => {
            // 同步 URL 哈希（可选）
            if (rating) {
              history.replaceState(null, '', `#rating=${rating}`);
            } else {
              history.replaceState(null, '', `#reviews`);
            }
          }}
        />

        {/* 相似推荐 */}
        <RecommendGoods
          currentGoodsId={goodsId}
          categoryId={goods.categoryId}
        />
      </div>
      {/* 联系卖家弹窗 */}
      <Modal
        visible={contactVisible}
        title="联系卖家"
        onCancel={() => setContactVisible(false)}
        onOk={handleSendContact}
        confirmLoading={contactLoading}
      >
        <div style={{ display: 'flex', gap: 8 }}>
          <Input
            type="text"
            placeholder="给卖家发一句话，比如：您好，这个还在吗？"
            value={contactMessage}
            onChange={(e) => setContactMessage(e.target.value)}
            maxLength={500}
          />
          <Button type="primary" onClick={handleSendContact} disabled={!contactMessage.trim()} loading={contactLoading}>
            发送
          </Button>
        </div>
      </Modal>
      {/* 选择订单进行评价 */}
      <Modal
        visible={reviewVisible}
        title="选择订单进行评价"
        onCancel={() => setReviewVisible(false)}
        onClose={() => setReviewVisible(false)}
        footer={null}
      >
        {eligibleOrders.length === 0 ? (
          <div style={{ padding: 12, color: '#8c8c8c' }}>
            {loadingEligible ? '加载可评价订单中...' : '暂无可评价订单'}
          </div>
        ) : (
          <div className="review-order-list">
            {eligibleOrders.map((o) => (
              <div key={o.orderNo} className="review-order-item">
                <div className="review-order-thumb">
                  <img src={o.goodsImage || '/placeholder.png'} alt={o.goodsTitle || '商品'} />
                </div>
                <div className="review-order-info">
                  <div className="review-order-title">{o.goodsTitle || `订单 ${o.orderNo}`}</div>
                  <div className="review-order-meta">
                    <span>订单号：{o.orderNo}</span>
                    <span>状态：{o.status}</span>
                  </div>
                </div>
                <div className="review-order-action">
                  <button
                    className="review-order-choose-btn"
                    onClick={() => handleSelectOrderForReview(o.orderNo!)}
                  >
                    选择
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default GoodsDetail;
