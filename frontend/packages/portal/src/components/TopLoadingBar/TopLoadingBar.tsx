/**
 * 顶部加载进度条组件 📊
 * @author BaSui 😎
 * @description 页面切换时显示的顶部进度条，提升用户体验
 * @date 2025-11-08
 */

import React, { useEffect, useState } from 'react';
import { useNavigation } from 'react-router-dom';
import './TopLoadingBar.css';

/**
 * 顶部加载进度条组件
 */
export const TopLoadingBar: React.FC = () => {
  const navigation = useNavigation();
  const [progress, setProgress] = useState(0);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (navigation.state === 'loading') {
      // 开始加载
      setIsLoading(true);
      setProgress(20);

      // 模拟进度增长
      const timer1 = setTimeout(() => setProgress(40), 100);
      const timer2 = setTimeout(() => setProgress(70), 300);
      const timer3 = setTimeout(() => setProgress(90), 500);

      return () => {
        clearTimeout(timer1);
        clearTimeout(timer2);
        clearTimeout(timer3);
      };
    } else {
      // 加载完成
      setProgress(100);
      const timer = setTimeout(() => {
        setIsLoading(false);
        setProgress(0);
      }, 300);

      return () => clearTimeout(timer);
    }
  }, [navigation.state]);

  if (!isLoading && progress === 0) return null;

  return (
    <div className="top-loading-bar">
      <div
        className="top-loading-bar__progress"
        style={{
          width: `${progress}%`,
          opacity: progress === 100 ? 0 : 1,
        }}
      />
    </div>
  );
};

export default TopLoadingBar;
