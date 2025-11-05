/**
 * 商品信息组件 📋
 * @author BaSui 😎
 * @description 商品标题、价格、成色、描述等详细信息
 */

import React from 'react';
import type { GoodsResponse } from '@campus/shared/api/models';
import './GoodsInfo.css';

interface GoodsInfoProps {
  goods: GoodsResponse;
}

const CONDITION_MAP: Record<string, string> = {
  'BRAND_NEW': '全新',
  'LIKE_NEW': '几乎全新',
  'LIGHTLY_USED': '轻微使用痕迹',
  'WELL_USED': '明显使用痕迹',
  'HEAVILY_USED': '重度使用痕迹',
};

export const GoodsInfo: React.FC<GoodsInfoProps> = ({ goods }) => {
  const formatTime = (time?: string) => {
    if (!time) return '';
    const date = new Date(time);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  return (
    <div className="goods-info">
      {/* 商品标题 */}
      <h1 className="goods-info__title">{goods.title}</h1>

      {/* 价格 */}
      <div className="goods-info__price-section">
        <div className="goods-info__price">
          <span className="goods-info__price-symbol">¥</span>
          <span className="goods-info__price-value">{goods.price?.toFixed(2)}</span>
        </div>
        {goods.originalPrice && goods.originalPrice > goods.price! && (
          <div className="goods-info__original-price">
            原价：¥{goods.originalPrice.toFixed(2)}
          </div>
        )}
      </div>

      {/* 基本信息 */}
      <div className="goods-info__meta">
        <div className="goods-info__meta-item">
          <span className="goods-info__meta-label">成色：</span>
          <span className="goods-info__meta-value">
            {CONDITION_MAP[goods.condition || ''] || goods.condition || '未知'}
          </span>
        </div>

        {goods.categoryName && (
          <div className="goods-info__meta-item">
            <span className="goods-info__meta-label">分类：</span>
            <span className="goods-info__meta-value">{goods.categoryName}</span>
          </div>
        )}

        <div className="goods-info__meta-item">
          <span className="goods-info__meta-label">浏览：</span>
          <span className="goods-info__meta-value">{goods.viewCount || 0} 次</span>
        </div>

        <div className="goods-info__meta-item">
          <span className="goods-info__meta-label">收藏：</span>
          <span className="goods-info__meta-value">{goods.favoriteCount || 0} 人</span>
        </div>

        <div className="goods-info__meta-item">
          <span className="goods-info__meta-label">发布：</span>
          <span className="goods-info__meta-value">{formatTime(goods.createdAt)}</span>
        </div>
      </div>

      {/* 标签 */}
      {goods.tags && goods.tags.length > 0 && (
        <div className="goods-info__tags">
          {goods.tags.map((tag) => (
            <span key={tag.id} className="goods-info__tag">
              #{tag.name}
            </span>
          ))}
        </div>
      )}

      {/* 商品描述 */}
      {goods.description && (
        <div className="goods-info__description">
          <h3 className="goods-info__section-title">商品描述</h3>
          <div className="goods-info__description-content">
            {goods.description}
          </div>
        </div>
      )}

      {/* 交易方式 */}
      <div className="goods-info__trade">
        <h3 className="goods-info__section-title">交易方式</h3>
        <div className="goods-info__trade-methods">
          {goods.deliveryMethod?.includes('MEET') && (
            <div className="goods-info__trade-method">
              <span className="goods-info__trade-icon">🤝</span>
              <span>校园面交</span>
            </div>
          )}
          {goods.deliveryMethod?.includes('MAIL') && (
            <div className="goods-info__trade-method">
              <span className="goods-info__trade-icon">📦</span>
              <span>快递邮寄</span>
            </div>
          )}
        </div>
      </div>

      {/* 商品状态标签 */}
      {goods.status && goods.status !== 'ON_SALE' && (
        <div className={`goods-info__status goods-info__status--${goods.status.toLowerCase()}`}>
          {goods.status === 'SOLD_OUT' && '已售出'}
          {goods.status === 'OFF_SHELF' && '已下架'}
          {goods.status === 'PENDING' && '审核中'}
        </div>
      )}
    </div>
  );
};

export default GoodsInfo;
