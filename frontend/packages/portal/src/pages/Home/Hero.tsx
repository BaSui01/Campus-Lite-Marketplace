/**
 * Hero 区域组件 - 首页头部横幅 🎨
 * @author BaSui 😎
 * @description 轮播图、平台介绍、全局搜索入口
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input } from '@campus/shared/components';

interface CarouselItem {
  id: number;
  title: string;
  description: string;
  imageUrl: string;
  link?: string;
}

// 轮播图数据（后续可以从后端API获取）
// 🎨 BaSui: 临时使用 Unsplash 免费图片，正式图片待设计
const CAROUSEL_DATA: CarouselItem[] = [
  {
    id: 1,
    title: '校园轻享集市',
    description: '让闲置物品找到新主人，让环保成为生活方式',
    imageUrl: 'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=1920&h=500&fit=crop&q=80',
  },
  {
    id: 2,
    title: '安全交易，放心购物',
    description: '实名认证，交易保障，让每一笔交易都安心',
    imageUrl: 'https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=1920&h=500&fit=crop&q=80',
  },
  {
    id: 3,
    title: '社区互动，分享生活',
    description: '不仅是交易平台，更是校园生活的分享社区',
    imageUrl: 'https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=1920&h=500&fit=crop&q=80',
  },
];

export const Hero: React.FC = () => {
  const navigate = useNavigate();
  const [currentIndex, setCurrentIndex] = useState(0);
  const [searchKeyword, setSearchKeyword] = useState('');

  // 自动轮播（5秒间隔）
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentIndex((prev) => (prev + 1) % CAROUSEL_DATA.length);
    }, 5000);

    return () => clearInterval(timer);
  }, []);

  // 处理搜索
  const handleSearch = () => {
    if (searchKeyword.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchKeyword.trim())}`);
    }
  };

  // 切换轮播图
  const handleDotClick = (index: number) => {
    setCurrentIndex(index);
  };

  // 上一张
  const handlePrev = () => {
    setCurrentIndex((prev) => (prev - 1 + CAROUSEL_DATA.length) % CAROUSEL_DATA.length);
  };

  // 下一张
  const handleNext = () => {
    setCurrentIndex((prev) => (prev + 1) % CAROUSEL_DATA.length);
  };

  const currentItem = CAROUSEL_DATA[currentIndex];

  return (
    <section className="hero">
      <div className="hero__carousel">
        {/* 轮播图背景 */}
        <div
          className="hero__carousel-background"
          style={{
            backgroundImage: `linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)), url(${currentItem.imageUrl})`,
          }}
        >
          {/* 左右箭头 */}
          <button
            className="hero__carousel-arrow hero__carousel-arrow--left"
            onClick={handlePrev}
            aria-label="上一张"
          >
            ‹
          </button>
          <button
            className="hero__carousel-arrow hero__carousel-arrow--right"
            onClick={handleNext}
            aria-label="下一张"
          >
            ›
          </button>

          {/* 轮播内容 */}
          <div className="hero__carousel-content">
            <h1 className="hero__title">{currentItem.title}</h1>
            <p className="hero__description">{currentItem.description}</p>

            {/* 搜索框 */}
            <div className="hero__search">
              <Input
                size="large"
                placeholder="搜索你想要的商品..."
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                onPressEnter={handleSearch}
                prefix={<span className="hero__search-icon">🔍</span>}
                allowClear
                className="hero__search-input"
              />
              <button className="hero__search-button" onClick={handleSearch}>
                搜索
              </button>
            </div>

            {/* 热门搜索 */}
            <div className="hero__hot-keywords">
              <span className="hero__hot-keywords-label">热门搜索：</span>
              {['二手教材', '自行车', '电子产品', '生活用品'].map((keyword) => (
                <button
                  key={keyword}
                  className="hero__hot-keyword"
                  onClick={() => {
                    setSearchKeyword(keyword);
                    navigate(`/search?q=${encodeURIComponent(keyword)}`);
                  }}
                >
                  {keyword}
                </button>
              ))}
            </div>
          </div>

          {/* 轮播指示器 */}
          <div className="hero__carousel-dots">
            {CAROUSEL_DATA.map((_, index) => (
              <button
                key={index}
                className={`hero__carousel-dot ${index === currentIndex ? 'active' : ''}`}
                onClick={() => handleDotClick(index)}
                aria-label={`第${index + 1}张`}
              />
            ))}
          </div>
        </div>
      </div>

      {/* 快捷入口 */}
      <div className="hero__quick-actions">
        <button
          className="hero__quick-action"
          onClick={() => navigate('/publish')}
        >
          <span className="hero__quick-action-icon">📝</span>
          <span className="hero__quick-action-text">发布商品</span>
        </button>
        <button
          className="hero__quick-action"
          onClick={() => navigate('/orders')}
        >
          <span className="hero__quick-action-icon">📦</span>
          <span className="hero__quick-action-text">我的订单</span>
        </button>
        <button
          className="hero__quick-action"
          onClick={() => navigate('/community')}
        >
          <span className="hero__quick-action-icon">💬</span>
          <span className="hero__quick-action-text">校园社区</span>
        </button>
        <button
          className="hero__quick-action"
          onClick={() => navigate('/profile')}
        >
          <span className="hero__quick-action-icon">👤</span>
          <span className="hero__quick-action-text">个人中心</span>
        </button>
      </div>
    </section>
  );
};

export default Hero;
