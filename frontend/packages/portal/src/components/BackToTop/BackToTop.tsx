/**
 * 回到顶部按钮组件 ⬆️
 * @author BaSui 😎
 * @description 滚动时显示，点击平滑滚动到页面顶部
 * @date 2025-11-08
 */

import React, { useState, useEffect } from 'react';
import './BackToTop.css';

/**
 * 回到顶部按钮Props
 */
export interface BackToTopProps {
  /** 显示按钮的滚动阈值（px） */
  threshold?: number;
}

/**
 * 回到顶部按钮组件
 */
export const BackToTop: React.FC<BackToTopProps> = ({
  threshold = 300,
}) => {
  const [isVisible, setIsVisible] = useState(false);

  /**
   * 监听滚动，控制按钮显示/隐藏
   */
  useEffect(() => {
    const handleScroll = () => {
      const scrollTop = window.scrollY || document.documentElement.scrollTop;
      setIsVisible(scrollTop > threshold);
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, [threshold]);

  /**
   * 平滑滚动到顶部
   */
  const scrollToTop = () => {
    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  };

  if (!isVisible) return null;

  return (
    <button
      className="back-to-top"
      onClick={scrollToTop}
      title="回到顶部"
      aria-label="回到顶部"
    >
      <span className="back-to-top__icon">⬆️</span>
      <span className="back-to-top__text">TOP</span>
    </button>
  );
};

export default BackToTop;
