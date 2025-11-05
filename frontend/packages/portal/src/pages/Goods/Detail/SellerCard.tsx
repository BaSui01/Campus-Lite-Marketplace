/**
 * 卖家信息卡片 👤
 * @author BaSui 😎
 * @description 卖家头像、昵称、评分、联系按钮
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import './SellerCard.css';

interface SellerCardProps {
  sellerId: number;
  sellerName: string;
  sellerAvatar?: string;
  onContact: () => void;
}

export const SellerCard: React.FC<SellerCardProps> = ({
  sellerId,
  sellerName,
  sellerAvatar,
  onContact,
}) => {
  const navigate = useNavigate();

  const handleViewProfile = () => {
    navigate(`/user/${sellerId}`);
  };

  return (
    <div className="seller-card">
      <h3 className="seller-card__title">卖家信息</h3>
      
      <div className="seller-card__content">
        {/* 卖家头像 */}
        <div className="seller-card__avatar-wrapper" onClick={handleViewProfile}>
          {sellerAvatar ? (
            <img
              src={sellerAvatar}
              alt={sellerName}
              className="seller-card__avatar"
            />
          ) : (
            <div className="seller-card__avatar seller-card__avatar--placeholder">
              {sellerName.charAt(0).toUpperCase()}
            </div>
          )}
        </div>

        {/* 卖家信息 */}
        <div className="seller-card__info">
          <div className="seller-card__name" onClick={handleViewProfile}>
            {sellerName}
          </div>
          
          {/* TODO: 从API获取卖家评分和商品数量 */}
          <div className="seller-card__stats">
            <span className="seller-card__stat">
              <span className="seller-card__stat-icon">⭐</span>
              <span className="seller-card__stat-value">4.8</span>
            </span>
            <span className="seller-card__stat">
              <span className="seller-card__stat-icon">📦</span>
              <span className="seller-card__stat-value">12件在售</span>
            </span>
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
