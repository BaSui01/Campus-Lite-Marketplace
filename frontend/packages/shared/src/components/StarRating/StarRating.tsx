/**
 * StarRating - 星级评分组件
 * @author BaSui 😎
 * @description 支持只读/可编辑、半星、多尺寸的星级评分组件
 */

import React, { useState, useMemo, useCallback } from 'react';
import type { StarRatingProps } from './types';
import './StarRating.css';

/**
 * 星星 SVG 图标
 */
const StarIcon: React.FC<{ filled?: boolean }> = ({ filled = false }) => (
  <svg
    viewBox="0 0 24 24"
    xmlns="http://www.w3.org/2000/svg"
    className={filled ? 'star-rating__star-filled' : 'star-rating__star-empty'}
  >
    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
  </svg>
);

/**
 * 星级评分组件
 */
export const StarRating: React.FC<StarRatingProps> = ({
  value,
  onChange,
  readonly = false,
  size = 'medium',
  color = '#fadb14',
  allowHalf = false,
  showValue = false,
  className = '',
  disabled = false,
}) => {
  // 悬停状态
  const [hoveredValue, setHoveredValue] = useState<number | null>(null);

  // 边界值处理：限制在 0-5 范围内
  const normalizedValue = useMemo(() => {
    if (value < 0) return 0;
    if (value > 5) return 5;
    return value;
  }, [value]);

  // 当前显示的值（悬停值优先）
  const displayValue = hoveredValue ?? normalizedValue;

  // 判断是否可编辑
  const isEditable = !readonly && !disabled && !!onChange;

  // 容器类名
  const containerClassName = useMemo(() => {
    const classes = [
      'star-rating',
      `star-rating--${size}`,
      isEditable ? 'star-rating--editable' : 'star-rating--readonly',
      disabled && 'star-rating--disabled',
      className,
    ];
    return classes.filter(Boolean).join(' ');
  }, [size, isEditable, disabled, className]);

  // 计算每颗星的填充状态
  const getStarFillStatus = useCallback(
    (index: number): 'empty' | 'half' | 'full' => {
      const starValue = index + 1;
      
      if (displayValue >= starValue) {
        return 'full';
      }
      
      if (allowHalf && displayValue >= starValue - 0.5) {
        return 'half';
      }
      
      return 'empty';
    },
    [displayValue, allowHalf]
  );

  // 计算点击位置是否在左半边
  const isLeftHalf = useCallback((event: React.MouseEvent<HTMLDivElement>) => {
    const rect = event.currentTarget.getBoundingClientRect();
    const offsetX = event.clientX - rect.left;
    return offsetX < rect.width / 2;
  }, []);

  // 鼠标进入星星
  const handleStarMouseEnter = useCallback(
    (index: number, event: React.MouseEvent<HTMLDivElement>) => {
      if (!isEditable) return;

      if (allowHalf) {
        // 计算鼠标在星星中的位置
        const leftHalf = isLeftHalf(event);
        setHoveredValue(index + (leftHalf ? 0.5 : 1));
      } else {
        setHoveredValue(index + 1);
      }
    },
    [isEditable, allowHalf, isLeftHalf]
  );

  // 鼠标离开容器
  const handleMouseLeave = useCallback(() => {
    if (!isEditable) return;
    setHoveredValue(null);
  }, [isEditable]);

  // 点击星星
  const handleStarClick = useCallback(
    (index: number, event: React.MouseEvent<HTMLDivElement>) => {
      if (!isEditable || !onChange) return;

      let newValue: number;

      if (allowHalf) {
        // 计算点击位置
        const leftHalf = isLeftHalf(event);
        newValue = index + (leftHalf ? 0.5 : 1);
      } else {
        newValue = index + 1;
      }

      onChange(newValue);
    },
    [isEditable, onChange, allowHalf, isLeftHalf]
  );

  // 渲染星星
  const renderStars = () => {
    return Array.from({ length: 5 }, (_, index) => {
      const fillStatus = getStarFillStatus(index);
      
      const starClassName = [
        'star-rating__star',
        fillStatus === 'full' && 'star-rating__star--filled',
        fillStatus === 'half' && 'star-rating__star--half',
        hoveredValue !== null && hoveredValue >= index + 1 && 'star-rating__star--hovered',
      ]
        .filter(Boolean)
        .join(' ');

      return (
        <div
          key={index}
          className={starClassName}
          style={{ color }}
          onMouseEnter={(e) => handleStarMouseEnter(index, e)}
          onMouseMove={(e) => allowHalf && handleStarMouseEnter(index, e)}
          onClick={(e) => handleStarClick(index, e)}
        >
          {/* 空星背景 */}
          <StarIcon filled={false} />
          
          {/* 填充星覆盖层 */}
          {fillStatus !== 'empty' && (
            <div
              style={{
                position: 'absolute',
                top: 0,
                left: 0,
                width: '100%',
                height: '100%',
                overflow: 'hidden',
              }}
            >
              <StarIcon filled={true} />
            </div>
          )}
        </div>
      );
    });
  };

  return (
    <div className={containerClassName}>
      <div className="star-rating__stars" onMouseLeave={handleMouseLeave}>
        {renderStars()}
      </div>
      
      {showValue && (
        <span className="star-rating__value">
          {normalizedValue.toFixed(1)}
        </span>
      )}
    </div>
  );
};

export default StarRating;
