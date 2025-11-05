/**
 * 商品排序栏组件 ⬆️⬇️
 * @author BaSui 😎
 * @description 排序选项和统计信息
 */

import React from 'react';
import './GoodsSortBar.css';

interface GoodsSortBarProps {
  sortBy: string;
  sortDirection: string;
  totalCount: number;
  onSortChange: (sortBy: string) => void;
}

const SORT_OPTIONS = [
  { value: 'createdAt', label: '最新发布' },
  { value: 'price', label: '价格' },
  { value: 'viewCount', label: '浏览量' },
  { value: 'favoriteCount', label: '收藏量' },
];

export const GoodsSortBar: React.FC<GoodsSortBarProps> = ({
  sortBy,
  sortDirection,
  totalCount,
  onSortChange,
}) => {
  return (
    <div className="goods-sort-bar">
      <div className="goods-sort-bar__left">
        <span className="goods-sort-bar__label">排序：</span>
        <div className="goods-sort-bar__options">
          {SORT_OPTIONS.map((option) => (
            <button
              key={option.value}
              className={`goods-sort-bar__option ${sortBy === option.value ? 'active' : ''}`}
              onClick={() => onSortChange(option.value)}
            >
              {option.label}
              {sortBy === option.value && (
                <span className="goods-sort-bar__arrow">
                  {sortDirection === 'desc' ? '↓' : '↑'}
                </span>
              )}
            </button>
          ))}
        </div>
      </div>

      <div className="goods-sort-bar__right">
        <span className="goods-sort-bar__count">
          共 <strong>{totalCount}</strong> 件商品
        </span>
      </div>
    </div>
  );
};

export default GoodsSortBar;
