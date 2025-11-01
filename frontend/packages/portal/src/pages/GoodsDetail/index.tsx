/**
 * 商品详情页 - 查看商品详细信息！🛍️
 * @author BaSui 😎
 * @description 展示商品详情、卖家信息、收藏、立即购买等功能
 */

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Skeleton } from '@campus/shared/components';
import { goodsService } from '@campus/shared/services/goods';
import { orderService } from '@campus/shared/services/order';
import { useNotificationStore } from '../../store';
import type { GoodsDetailResponse } from '@campus/shared/api/models';
import './GoodsDetail.css';

/**
 * 商品详情页组件
 */
const GoodsDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [goods, setGoods] = useState<GoodsDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [isFavorited, setIsFavorited] = useState(false);
  const [favoriteLoading, setFavoriteLoading] = useState(false);
  const [purchasing, setPurchasing] = useState(false);

  // ==================== 数据加载 ====================

  /**
   * 加载商品详情（使用真实后端 API！）
   */
  const loadGoodsDetail = async () => {
    if (!id) {
      setError('商品 ID 不能为空！😰');
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const goodsId = parseInt(id, 10);

      // 🚀 调用真实后端 API 获取商品详情
      const detail = await goodsService.getGoodsDetail(goodsId);
      setGoods(detail);

      // 🚀 调用真实后端 API 检查收藏状态
      const favorited = await goodsService.isFavorited(goodsId);
      setIsFavorited(favorited);
    } catch (err: any) {
      console.error('加载商品详情失败：', err);
      setError(err.response?.data?.message || '加载商品详情失败，请稍后重试！😭');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadGoodsDetail();
  }, [id]);

  // ==================== 事件处理 ====================

  /**
   * 收藏/取消收藏商品
   */
  const handleFavorite = async () => {
    if (!goods?.id) return;

    setFavoriteLoading(true);

    try {
      if (isFavorited) {
        // 🚀 调用真实后端 API 取消收藏
        await goodsService.unfavoriteGoods(goods.id);
        setIsFavorited(false);

        // 更新收藏数
        if (goods.favoriteCount !== undefined) {
          setGoods({
            ...goods,
            favoriteCount: Math.max(0, goods.favoriteCount - 1),
          });
        }
      } else {
        // 🚀 调用真实后端 API 收藏
        await goodsService.favoriteGoods(goods.id);
        setIsFavorited(true);

        // 更新收藏数
        if (goods.favoriteCount !== undefined) {
          setGoods({
            ...goods,
            favoriteCount: goods.favoriteCount + 1,
          });
        }
      }
    } catch (err: any) {
      console.error('收藏操作失败：', err);
      toast.error(err.response?.data?.message || '操作失败，请稍后重试！😭');
    } finally {
      setFavoriteLoading(false);
    }
  };

  /**
   * 立即购买 - 创建订单并跳转到订单详情页
   */
  const handlePurchase = async () => {
    if (!goods?.id) return;

    // 检查商品状态
    if (goods.status !== 'APPROVED') {
      toast.warning('该商品暂时无法购买！😰');
      return;
    }

    setPurchasing(true);

    try {
      // 🚀 调用真实后端 API 创建订单
      const response = await orderService.createOrder({
        goodsId: goods.id,
        quantity: 1,
      });

      const orderData = response.data;
      const orderNo = orderData?.orderNo;

      if (orderNo) {
        // 跳转到订单详情页
        navigate(`/orders/${orderNo}`);
      } else {
        toast.error('创建订单失败，未返回订单号！😭');
      }
    } catch (err: any) {
      console.error('创建订单失败：', err);
      toast.error(err.response?.data?.message || '创建订单失败，请稍后重试！😭');
    } finally {
      setPurchasing(false);
    }
  };

  /**
   * 联系卖家 - 跳转到聊天页面（暂时先跳转到首页，后续实现聊天功能）
   */
  const handleContactSeller = () => {
    if (!goods?.seller?.id) return;

    // TODO: 后续实现聊天功能时，跳转到聊天页面
    toast.info('聊天功能正在开发中，敬请期待！🚧');
    // navigate(`/chat/${goods.seller.id}`);
  };

  /**
   * 切换图片
   */
  const handleImageChange = (index: number) => {
    setCurrentImageIndex(index);
  };

  // ==================== 工具函数 ====================

  /**
   * 格式化价格 - ¥X.XX
   */
  const formatPrice = (price?: number) => {
    if (!price) return '¥0.00';
    return `¥${price.toFixed(2)}`;
  };

  /**
   * 格式化时间 - 相对时间
   */
  const formatTime = (time?: string) => {
    if (!time) return '';

    const date = new Date(time);
    const now = new Date();
    const diff = now.getTime() - date.getTime();

    const minutes = Math.floor(diff / 1000 / 60);
    const hours = Math.floor(diff / 1000 / 60 / 60);
    const days = Math.floor(diff / 1000 / 60 / 60 / 24);

    if (minutes < 1) return '刚刚';
    if (minutes < 60) return `${minutes}分钟前`;
    if (hours < 24) return `${hours}小时前`;
    if (days < 30) return `${days}天前`;

    return date.toLocaleDateString('zh-CN');
  };

  /**
   * 获取商品状态文本
   */
  const getStatusText = (status?: string) => {
    switch (status) {
      case 'PENDING':
        return '审核中';
      case 'APPROVED':
        return '在售';
      case 'REJECTED':
        return '已拒绝';
      case 'SOLD':
        return '已售出';
      case 'OFFLINE':
        return '已下架';
      default:
        return '未知';
    }
  };

  /**
   * 获取商品状态样式类
   */
  const getStatusClass = (status?: string) => {
    switch (status) {
      case 'PENDING':
        return 'status-pending';
      case 'APPROVED':
        return 'status-approved';
      case 'REJECTED':
        return 'status-rejected';
      case 'SOLD':
        return 'status-sold';
      case 'OFFLINE':
        return 'status-offline';
      default:
        return '';
    }
  };

  // ==================== 渲染 ====================

  // 加载中状态
  if (loading) {
    return (
      <div className="goods-detail-page">
        <div className="goods-detail-container">
          {/* 左侧：图片骨架 */}
          <div className="goods-detail-images">
            <Skeleton type="image" height={400} animation="wave" />
            <div className="thumbnail-list" style={{ display: 'flex', gap: '12px', marginTop: '12px' }}>
              {Array.from({ length: 4 }).map((_, index) => (
                <Skeleton key={index} type="image" width={80} height={80} animation="wave" />
              ))}
            </div>
          </div>

          {/* 右侧：信息骨架 */}
          <div className="goods-detail-info">
            <Skeleton type="text" rows={1} animation="wave" style={{ marginBottom: '16px' }} />
            <Skeleton type="text" rows={2} animation="wave" style={{ marginBottom: '24px' }} />
            <Skeleton type="text" rows={1} animation="wave" style={{ marginBottom: '16px' }} />
            <Skeleton type="button" width={120} animation="wave" style={{ marginBottom: '24px' }} />
            <Skeleton type="form" count={3} animation="wave" />
          </div>
        </div>
      </div>
    );
  }

  // 错误状态
  if (error || !goods) {
    return (
      <div className="goods-detail-page">
        <div className="goods-detail-error">
          <div className="error-icon">😭</div>
          <h2>{error || '商品不存在'}</h2>
          <button onClick={() => navigate('/')} className="btn-back-home">
            返回首页
          </button>
        </div>
      </div>
    );
  }

  // 商品图片列表
  const images = goods.images && goods.images.length > 0 ? goods.images : [];
  const currentImage = images.length > 0 ? images[currentImageIndex] : null;

  return (
    <div className="goods-detail-page">
      <div className="goods-detail-container">
        {/* ==================== 左侧：商品图片 ==================== */}
        <div className="goods-detail-images">
          {/* 主图 */}
          <div className="main-image">
            {currentImage ? (
              <img src={currentImage} alt={goods.title} />
            ) : (
              <div className="image-placeholder">
                <span className="placeholder-icon">📦</span>
                <p>暂无图片</p>
              </div>
            )}
          </div>

          {/* 缩略图列表 */}
          {images.length > 1 && (
            <div className="thumbnail-list">
              {images.map((img, index) => (
                <div
                  key={index}
                  className={`thumbnail-item ${index === currentImageIndex ? 'active' : ''}`}
                  onClick={() => handleImageChange(index)}
                >
                  <img src={img} alt={`${goods.title} - ${index + 1}`} />
                </div>
              ))}
            </div>
          )}
        </div>

        {/* ==================== 右侧：商品信息 ==================== */}
        <div className="goods-detail-info">
          {/* 商品状态标签 */}
          <div className={`goods-status ${getStatusClass(goods.status)}`}>
            {getStatusText(goods.status)}
          </div>

          {/* 商品标题 */}
          <h1 className="goods-title">{goods.title}</h1>

          {/* 商品价格 */}
          <div className="goods-price-section">
            <div className="price-label">价格</div>
            <div className="price-value">{formatPrice(goods.price)}</div>
          </div>

          {/* 商品标签 */}
          {goods.tags && goods.tags.length > 0 && (
            <div className="goods-tags">
              {goods.tags.map((tag) => (
                <span key={tag.id} className="tag-item">
                  {tag.name}
                </span>
              ))}
            </div>
          )}

          {/* 商品统计信息 */}
          <div className="goods-stats">
            <div className="stat-item">
              <span className="stat-icon">👀</span>
              <span className="stat-value">{goods.viewCount || 0}</span>
              <span className="stat-label">浏览</span>
            </div>
            <div className="stat-item">
              <span className="stat-icon">❤️</span>
              <span className="stat-value">{goods.favoriteCount || 0}</span>
              <span className="stat-label">收藏</span>
            </div>
            <div className="stat-item">
              <span className="stat-icon">📂</span>
              <span className="stat-label">{goods.categoryName || '未分类'}</span>
            </div>
          </div>

          {/* 操作按钮 */}
          <div className="goods-actions">
            <button
              className={`btn-favorite ${isFavorited ? 'favorited' : ''}`}
              onClick={handleFavorite}
              disabled={favoriteLoading}
            >
              {favoriteLoading ? '⏳' : isFavorited ? '💖' : '🤍'} {isFavorited ? '已收藏' : '收藏'}
            </button>
            <button
              className="btn-contact"
              onClick={handleContactSeller}
            >
              💬 联系卖家
            </button>
            <button
              className="btn-purchase"
              onClick={handlePurchase}
              disabled={purchasing || goods.status !== 'APPROVED'}
            >
              {purchasing ? '⏳ 创建订单中...' : '🛒 立即购买'}
            </button>
          </div>

          {/* 卖家信息 */}
          {goods.seller && (
            <div className="seller-info">
              <h3 className="seller-title">卖家信息</h3>
              <div className="seller-card">
                <div className="seller-avatar">
                  {goods.seller.avatar ? (
                    <img src={goods.seller.avatar} alt={goods.seller.username} />
                  ) : (
                    <span className="avatar-placeholder">👤</span>
                  )}
                </div>
                <div className="seller-details">
                  <div className="seller-name">{goods.seller.username}</div>
                  <div className="seller-points">⭐ 积分：{goods.seller.points || 0}</div>
                  {goods.seller.phone && (
                    <div className="seller-contact">📱 {goods.seller.phone}</div>
                  )}
                  {goods.seller.email && (
                    <div className="seller-contact">📧 {goods.seller.email}</div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* 发布时间 */}
          <div className="goods-time">
            <span className="time-label">发布时间：</span>
            <span className="time-value">{formatTime(goods.createdAt)}</span>
          </div>
        </div>
      </div>

      {/* ==================== 底部：商品描述 ==================== */}
      <div className="goods-description-section">
        <div className="description-container">
          <h2 className="description-title">商品描述</h2>
          <div className="description-content">
            {goods.description ? (
              <p>{goods.description}</p>
            ) : (
              <p className="no-description">暂无商品描述</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default GoodsDetail;
