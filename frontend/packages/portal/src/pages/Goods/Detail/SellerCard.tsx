/**
 * 卖家信息卡片 👤
 * @author BaSui 😎
 * @description 卖家头像、昵称、评分、联系按钮
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UserAvatar } from '@campus/shared/components';
import './SellerCard.css';

interface SellerCardProps {
  sellerId: number;
  sellerName: string;
  sellerAvatar?: string;
  sellerRating?: number;  // 🆕 卖家评分（0-5分）
  sellerGoodsCount?: number;  // 🆕 在售商品数量
  onContact: () => void;
}

export const SellerCard: React.FC<SellerCardProps> = ({
  sellerId,
  sellerName,
  sellerAvatar,
  sellerRating,  // 🆕 卖家评分
  sellerGoodsCount,  // 🆕 在售商品数量
  onContact,
}) => {
  const navigate = useNavigate();

  const handleViewProfile = () => {
    navigate(`/users/${sellerId}`);
  };

  return (
    <div className="seller-card">
      <h3 className="seller-card__title">卖家信息</h3>
      
      <div className="seller-card__content">
        {/* 卖家头像 - 使用 UserAvatar 组件保持一致性 */}
        <div className="seller-card__avatar-wrapper">
          <UserAvatar
            userId={sellerId.toString()}
            username={sellerName}
            avatarUrl={sellerAvatar}
            size="large"
            onAvatarClick={() => handleViewProfile()}
            showUsername={false}
          />
        </div>

        {/* 卖家信息 */}
        <div className="seller-card__info">
          <div className="seller-card__name" onClick={handleViewProfile}>
            {sellerName}
          </div>
          
          {/* ✅ 使用真实的API数据 */}
          <div className="seller-card__stats">
            {sellerRating !== undefined && sellerRating !== null && (
              <span className="seller-card__stat">
                <span className="seller-card__stat-icon">⭐</span>
                <span className="seller-card__stat-value">
                  {sellerRating.toFixed(1)}
                </span>
              </span>
            )}
            {sellerGoodsCount !== undefined && sellerGoodsCount !== null && (
              <span className="seller-card__stat">
                <span className="seller-card__stat-icon">📦</span>
                <span className="seller-card__stat-value">
                  {sellerGoodsCount}件在售
                </span>
              </span>
            )}
          </div>
        </div>

        {/* 操作按钮 */}
        <div className="seller-card__actions">
          <button
            className="seller-card__btn seller-card__btn--primary"
            onClick={onContact}
          >
            💬 联系卖家
          </button>
          <button
            className="seller-card__btn seller-card__btn--secondary"
            onClick={handleViewProfile}
          >
            👤 查看主页
          </button>
        </div>
      </div>
    </div>
  );
};

export default SellerCard;
