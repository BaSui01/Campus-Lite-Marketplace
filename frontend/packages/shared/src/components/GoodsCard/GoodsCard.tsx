/**
 * GoodsCard 组件 - 商品卡片专家！🛍️
 * @author BaSui 😎
 * @description 商品卡片组件，用于商品列表展示，整合 Card、Tag、Badge 等基础组件
 */

import React from 'react';
import { Card, type CardProps } from '../Card';
import { Tag } from '../Tag';
import { Badge } from '../Badge';
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
   * 商品图片 URL
   */
  imageUrl: string;

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
  const statusConfig = getStatusConfig(goods.status);
  const isAvailable = goods.status === 'on_sale' && goods.stock > 0;
  const hasDiscount = goods.originalPrice && goods.originalPrice > goods.price;

  /**
   * 处理卡片点击
   */
  const handleCardClick = () => {
    onCardClick?.(goods);
  };

  /**
   * 处理购买按钮点击
   */
  const handleBuyClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    onBuyClick?.(goods);
  };

  /**
   * 处理加入购物车按钮点击
   */
  const handleCartClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    onCartClick?.(goods);
  };

  /**
   * 处理卖家点击
   */
  const handleSellerClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (goods.seller) {
      onSellerClick?.(goods.seller.id);
    }
  };

  return (
    <Card
      {...cardProps}
      className={`campus-goods-card ${cardProps.className || ''}`}
      hoverable={cardProps.hoverable !== undefined ? cardProps.hoverable : true}
      onClick={handleCardClick}
      cover={
        <div className="campus-goods-card__image-wrapper">
          <img src={goods.imageUrl} alt={goods.name} className="campus-goods-card__image" />
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
        <h3 className="campus-goods-card__name" title={goods.name}>
          {goods.name}
        </h3>

        {/* 商品描述 */}
        {goods.description && (
          <p className="campus-goods-card__description" title={goods.description}>
            {goods.description}
          </p>
        )}

        {/* 价格信息 */}
        <div className="campus-goods-card__price-wrapper">
          <span className="campus-goods-card__price">{formatPrice(goods.price)}</span>
          {hasDiscount && (
            <span className="campus-goods-card__original-price">
              {formatPrice(goods.originalPrice!)}
            </span>
          )}
        </div>

        {/* 标签 */}
        {showTags && goods.tags && goods.tags.length > 0 && (
          <div className="campus-goods-card__tags">
            {goods.tags.slice(0, 3).map((tag, index) => (
              <Tag key={index} size="small" color="default">
                {tag}
              </Tag>
            ))}
            {goods.tags.length > 3 && (
              <Tag size="small" color="default">
                +{goods.tags.length - 3}
              </Tag>
            )}
          </div>
        )}

        {/* 底部信息 */}
        <div className="campus-goods-card__footer">
          {/* 卖家信息 */}
          {showSeller && goods.seller && (
            <div
              className="campus-goods-card__seller"
              onClick={handleSellerClick}
            >
              {goods.seller.avatar && (
                <img
                  src={goods.seller.avatar}
                  alt={goods.seller.name}
                  className="campus-goods-card__seller-avatar"
                />
              )}
              <span className="campus-goods-card__seller-name">{goods.seller.name}</span>
            </div>
          )}

          {/* 已售数量 */}
          {showSoldCount && goods.soldCount !== undefined && (
            <span className="campus-goods-card__sold-count">已售 {goods.soldCount}</span>
          )}
        </div>
      </div>
    </Card>
  );
};

export default GoodsCard;
