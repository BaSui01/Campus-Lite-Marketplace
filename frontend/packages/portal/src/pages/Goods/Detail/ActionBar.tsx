/**
 * 操作按钮栏 🎯
 * @author BaSui 😎
 * @description 收藏、购买、联系、分享、举报等操作
 */

import React from 'react';
import './ActionBar.css';

interface ActionBarProps {
  isFavorited: boolean;
  isOwner: boolean;
  onFavorite: () => void;
  onBuy: () => void;
  onContact: () => void;
  onShare: () => void;
  onReport: () => void;
}

export const ActionBar: React.FC<ActionBarProps> = ({
  isFavorited,
  isOwner,
  onFavorite,
  onBuy,
  onContact,
  onShare,
  onReport,
}) => {
  return (
    <div className="action-bar">
      <div className="action-bar__main">
        {/* 收藏按钮 */}
        <button
          className={`action-bar__btn action-bar__btn--favorite ${isFavorited ? 'active' : ''}`}
          onClick={onFavorite}
        >
          <span className="action-bar__btn-icon">
            {isFavorited ? '❤️' : '🤍'}
          </span>
          <span className="action-bar__btn-text">
            {isFavorited ? '已收藏' : '收藏'}
          </span>
        </button>

        {/* 主操作按钮 */}
        {!isOwner && (
          <>
            <button
              className="action-bar__btn action-bar__btn--contact"
              onClick={onContact}
            >
              <span className="action-bar__btn-icon">💬</span>
              <span className="action-bar__btn-text">联系卖家</span>
            </button>

            <button
              className="action-bar__btn action-bar__btn--buy"
              onClick={onBuy}
            >
              <span className="action-bar__btn-icon">🛒</span>
              <span className="action-bar__btn-text">立即购买</span>
            </button>
          </>
        )}
      </div>

      <div className="action-bar__secondary">
        {/* 分享按钮 */}
        <button
          className="action-bar__icon-btn"
          onClick={onShare}
          title="分享"
        >
          <span>🔗</span>
        </button>

        {/* 举报按钮 */}
        {!isOwner && (
          <button
            className="action-bar__icon-btn"
            onClick={onReport}
            title="举报"
          >
            <span>🚩</span>
          </button>
        )}
      </div>
    </div>
  );
};

export default ActionBar;
