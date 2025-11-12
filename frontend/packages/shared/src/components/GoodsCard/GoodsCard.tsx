/**
 * GoodsCard 组件 - 商品卡片专家！🛍️
 * @author BaSui 😎
 * @description 商品卡片组件，用于商品列表展示，整合 Card、Tag、Badge 等基础组件
 */

import React from 'react';
import { Card, type CardProps } from '../Card';
import { Tag } from '../Tag';
import { Badge } from '../Badge';
import { UserAvatar } from '../UserAvatar';
import './GoodsCard.css';

/**
 * 商品状态
 */
export type GoodsStatus = 'on_sale' | 'sold_out' | 'off_shelf' | 'pending';

/**
 * 商品数据接口
 */
export interface GoodsData {
  /**
   * 商品 ID
   */
  id: string;

  /**
   * 商品名称
   */
  name: string;

  /**
   * 商品描述
   */
  description?: string;

  /**
   * 商品价格
   */
  price: number;

  /**
   * 原价（用于显示折扣）
   */
  originalPrice?: number;

  /**
   * 商品图片 URL（封面图）
   */
  imageUrl: string;

  /**
   * 所有商品图片 URL 列表（支持轮播）
   */
  images?: string[];

  /**
   * 商品状态
   */
  status: GoodsStatus;

  /**
   * 库存数量
   */
  stock: number;

  /**
   * 已售数量
   */
  soldCount?: number;

  /**
   * 商品标签
   */
  tags?: string[];

  /**
   * 卖家信息
   */
  seller?: {
    id: string;
    name: string;
    avatar?: string;
  };

  /**
   * 创建时间
   */
  createdAt?: string;
}

/**
 * GoodsCard 组件的 Props 接口
 */
export interface GoodsCardProps extends Omit<CardProps, 'children'> {
  /**
   * 商品数据
   */
  goods: GoodsData;

  /**
   * 是否显示卖家信息
   * @default true
   */
  showSeller?: boolean;

  /**
   * 是否显示已售数量
   * @default true
   */
  showSoldCount?: boolean;

  /**
   * 是否显示标签
   * @default true
   */
  showTags?: boolean;

  /**
   * 卡片点击回调
   */
  onCardClick?: (goods: GoodsData) => void;

  /**
   * 购买按钮点击回调
   */
  onBuyClick?: (goods: GoodsData) => void;

  /**
   * 加入购物车按钮点击回调
   */
  onCartClick?: (goods: GoodsData) => void;

  /**
   * 卖家点击回调
   */
  onSellerClick?: (sellerId: string) => void;

  // ===== 兼容旧版调用方式（可选）=====
  id?: string | number;
  title?: string;
  name?: string;
  price?: number;
  originalPrice?: number;
  coverImage?: string;
  imageUrl?: string;
  images?: string[];
  status?: GoodsStatus;
  stock?: number;
  soldCount?: number;
  tags?: string[];
  sellerName?: string;
  sellerId?: string | number;
  sellerAvatar?: string;
  seller?: { id: string; name: string; avatar?: string };
  createdAt?: string;
}

/**
 * 获取状态标签配置
 */
const getStatusConfig = (status: GoodsStatus) => {
  const configs = {
    on_sale: { label: '在售', color: 'success' as const },
    sold_out: { label: '售罄', color: 'error' as const },
    off_shelf: { label: '下架', color: 'default' as const },
    pending: { label: '审核中', color: 'warning' as const },
  };
  return configs[status] || configs.pending;
};

/**
 * 格式化价格
 */
const formatPrice = (price: number): string => {
  return `¥${price.toFixed(2)}`;
};

/**
 * GoodsCard 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <GoodsCard
 *   goods={{
 *     id: '1',
 *     name: '全新 MacBook Pro',
 *     price: 12999,
 *     originalPrice: 14999,
 *     imageUrl: '/images/macbook.jpg',
 *     status: 'on_sale',
 *     stock: 5,
 *     soldCount: 10,
 *     tags: ['电子产品', '笔记本'],
 *     seller: { id: '1', name: '张三', avatar: '/avatar.jpg' },
 *   }}
 *   onBuyClick={(goods) => console.log('购买', goods)}
 * />
 *
 * // 简洁模式（不显示卖家和标签）
 * <GoodsCard
 *   goods={goodsData}
 *   showSeller={false}
 *   showTags={false}
 *   hoverable
 *   onCardClick={(goods) => navigate(`/goods/${goods.id}`)}
 * />
 * ```
 */
export const GoodsCard: React.FC<GoodsCardProps> = ({
  goods,
  showSeller = true,
  showSoldCount = true,
  showTags = true,
  onCardClick,
  onBuyClick,
  onCartClick,
  onSellerClick,
  ...cardProps
}) => {
  // 兼容：若未传入 goods，则从旧版扁平 props 构造
  const normalizedGoods: GoodsData = goods || {
    id: String(cardProps.id ?? ''),
    name: (cardProps.name || cardProps.title || '未命名商品') as string,
    price: Number(cardProps.price ?? 0),
    originalPrice: cardProps.originalPrice,
    imageUrl: (cardProps.coverImage || cardProps.imageUrl || '') as string,
    images: cardProps.images,
    status: (cardProps.status || 'on_sale') as GoodsStatus,
    stock: Number(cardProps.stock ?? 1),
    soldCount: cardProps.soldCount,
    tags: cardProps.tags,
    seller:
      cardProps.seller ||
      (cardProps.sellerName
        ? {
            id: String(cardProps.sellerId ?? ''),
            name: cardProps.sellerName,
            avatar: cardProps.sellerAvatar,
          }
        : undefined),
    createdAt: cardProps.createdAt,
  } as GoodsData;

  const g = normalizedGoods;
  const statusConfig = getStatusConfig(g.status);
  const isAvailable = g.status === 'on_sale' && g.stock > 0;
  const hasDiscount = !!g.originalPrice && g.originalPrice > g.price;

  // 图片轮播状态
  const [currentImageIndex, setCurrentImageIndex] = React.useState(0);
  const images = g.images && g.images.length > 0 ? g.images : [g.imageUrl].filter(Boolean);
  const safeImages = images.length > 0 ? images : [''];
  const imageList = safeImages;
  const hasMultipleImages = images.length > 1;

  /**
   * 处理卡片点击
   */
  const handleCardClick = (e?: React.MouseEvent) => {
    // 先透传旧版 onClick（CardProps）
    if (cardProps.onClick) {
      cardProps.onClick(e as any);
    }
    onCardClick?.(g);
  };

  /**
   * 处理购买按钮点击
   */
  const handleBuyClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    onBuyClick?.(g);
  };

  /**
   * 处理加入购物车按钮点击
   */
  const handleCartClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    onCartClick?.(g);
  };

  /**
   * 处理卖家点击
   */
  const handleSellerClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (g.seller) {
      onSellerClick?.(g.seller.id);
    }
  };

  /**
   * 处理图片切换（上一张/下一张）
   */
  const handleImageChange = (direction: 'prev' | 'next', e: React.MouseEvent) => {
    e.stopPropagation();
    setCurrentImageIndex((prev) => {
      if (direction === 'next') {
        return (prev + 1) % images.length;
      } else {
        return (prev - 1 + images.length) % images.length;
      }
    });
  };

  return (
    <Card
      {...cardProps}
      className={`campus-goods-card ${cardProps.className || ''}`}
      hoverable={cardProps.hoverable !== undefined ? cardProps.hoverable : true}
      onClick={handleCardClick}
      cover={
        <div className="campus-goods-card__image-wrapper">
          <img 
            src={imageList[currentImageIndex]} 
            alt={g.name} 
            className="campus-goods-card__image" 
          />
          
          {/* 状态标签 */}
          <div className="campus-goods-card__status">
            <Tag color={statusConfig.color} size="small">
              {statusConfig.label}
            </Tag>
          </div>
          
          {/* 折扣标签 */}
          {hasDiscount && (
            <div className="campus-goods-card__discount">
              <Badge
                content={`省${formatPrice(goods.originalPrice! - goods.price)}`}
                color="#ff4d4f"
              />
            </div>
          )}

          {/* 图片轮播控制 */}
          {hasMultipleImages && (
            <>
              {/* 左箭头 */}
              <button
                className="campus-goods-card__image-arrow campus-goods-card__image-arrow--left"
                onClick={(e) => handleImageChange('prev', e)}
                aria-label="上一张"
              >
                ‹
              </button>
              
              {/* 右箭头 */}
              <button
                className="campus-goods-card__image-arrow campus-goods-card__image-arrow--right"
                onClick={(e) => handleImageChange('next', e)}
                aria-label="下一张"
              >
                ›
              </button>

              {/* 图片指示器 */}
              <div className="campus-goods-card__image-indicators">
                {imageList.map((_, index) => (
                  <span
                    key={index}
                    className={`campus-goods-card__image-indicator ${
                      index === currentImageIndex ? 'active' : ''
                    }`}
                  />
                ))}
              </div>
            </>
          )}
        </div>
      }
      actions={
        isAvailable && (onBuyClick || onCartClick)
          ? [
              onCartClick && (
                <button
                  key="cart"
                  className="campus-goods-card__action-btn campus-goods-card__action-btn--secondary"
                  onClick={handleCartClick}
                >
                  加入购物车
                </button>
              ),
              onBuyClick && (
                <button
                  key="buy"
                  className="campus-goods-card__action-btn campus-goods-card__action-btn--primary"
                  onClick={handleBuyClick}
                >
                  立即购买
                </button>
              ),
            ].filter(Boolean)
          : undefined
      }
    >
      {/* 商品信息 */}
      <div className="campus-goods-card__content">
        {/* 商品名称 */}
        <h3 className="campus-goods-card__name" title={g.name}>
          {g.name}
        </h3>

        {/* 商品描述 */}
        {g.description && (
          <p className="campus-goods-card__description" title={g.description}>
            {g.description}
          </p>
        )}

        {/* 价格信息 */}
        <div className="campus-goods-card__price-wrapper">
          <span className="campus-goods-card__price">{formatPrice(g.price)}</span>
          {hasDiscount && (
            <span className="campus-goods-card__original-price">
              {formatPrice(g.originalPrice!)}
            </span>
          )}
        </div>

        {/* 标签 */}
        {showTags && g.tags && g.tags.length > 0 && (
          <div className="campus-goods-card__tags">
            {g.tags.slice(0, 3).map((tag, index) => (
              <Tag key={index} size="small" color="default">
                {tag}
              </Tag>
            ))}
            {g.tags.length > 3 && (
              <Tag size="small" color="default">
                +{g.tags.length - 3}
              </Tag>
            )}
          </div>
        )}

        {/* 底部信息 */}
        <div className="campus-goods-card__footer">
          {/* 卖家信息 - 使用 UserAvatar 组件保持一致性 */}
          {showSeller && g.seller && (
            <div className="campus-goods-card__seller">
              <UserAvatar
                userId={g.seller.id}
                username={g.seller.name}
                avatarUrl={g.seller.avatar}
                size="small"
                onAvatarClick={onSellerClick}
                showUsername
                className="campus-goods-card__seller-avatar-wrapper"
              />
            </div>
          )}

          {/* 已售数量 */}
          {showSoldCount && g.soldCount !== undefined && (
            <span className="campus-goods-card__sold-count">已售 {g.soldCount}</span>
          )}
        </div>
      </div>
    </Card>
  );
};

export default GoodsCard;
