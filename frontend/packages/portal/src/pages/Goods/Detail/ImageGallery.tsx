/**
 * 图片画廊组件 🖼️
 * @author BaSui 😎
 * @description 商品图片展示、缩放、切换、全屏
 */

import React, { useState } from 'react';
import './ImageGallery.css';

interface ImageGalleryProps {
  images: string[];
}

export const ImageGallery: React.FC<ImageGalleryProps> = ({ images }) => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFullscreen, setIsFullscreen] = useState(false);

  const hasMultipleImages = images.length > 1;

  // 上一张
  const handlePrev = () => {
    setCurrentIndex((prev) => (prev - 1 + images.length) % images.length);
  };

  // 下一张
  const handleNext = () => {
    setCurrentIndex((prev) => (prev + 1) % images.length);
  };

  // 选择图片
  const handleSelect = (index: number) => {
    setCurrentIndex(index);
  };

  // 全屏切换
  const handleToggleFullscreen = () => {
    setIsFullscreen(!isFullscreen);
  };

  // 如果没有图片，显示占位符
  if (images.length === 0) {
    images = ['/placeholder.jpg'];
  }

  return (
    <>
      <div className="image-gallery">
        {/* 主图 */}
        <div className="image-gallery__main">
          <img
            src={images[currentIndex]}
            alt="商品图片"
            className="image-gallery__main-img"
            onClick={handleToggleFullscreen}
          />

          {/* 左右箭头 */}
          {hasMultipleImages && (
            <>
              <button
                className="image-gallery__arrow image-gallery__arrow--left"
                onClick={handlePrev}
                aria-label="上一张"
              >
                ‹
              </button>
              <button
                className="image-gallery__arrow image-gallery__arrow--right"
                onClick={handleNext}
                aria-label="下一张"
              >
                ›
              </button>
            </>
          )}

          {/* 图片计数 */}
          {hasMultipleImages && (
            <div className="image-gallery__counter">
              {currentIndex + 1} / {images.length}
            </div>
          )}

          {/* 全屏按钮 */}
          <button
            className="image-gallery__fullscreen-btn"
            onClick={handleToggleFullscreen}
            aria-label="全屏"
          >
            🔍
          </button>
        </div>

        {/* 缩略图 */}
        {hasMultipleImages && (
          <div className="image-gallery__thumbnails">
            {images.map((image, index) => (
              <button
                key={index}
                className={`image-gallery__thumbnail ${index === currentIndex ? 'active' : ''}`}
                onClick={() => handleSelect(index)}
              >
                <img src={image} alt={`缩略图${index + 1}`} />
              </button>
            ))}
          </div>
        )}
      </div>

      {/* 全屏模式 */}
      {isFullscreen && (
        <div className="image-gallery-fullscreen" onClick={handleToggleFullscreen}>
          <button
            className="image-gallery-fullscreen__close"
            onClick={handleToggleFullscreen}
            aria-label="关闭"
          >
            ✕
          </button>

          <img
            src={images[currentIndex]}
            alt="全屏图片"
            className="image-gallery-fullscreen__img"
            onClick={(e) => e.stopPropagation()}
          />

          {hasMultipleImages && (
            <>
              <button
                className="image-gallery-fullscreen__arrow image-gallery-fullscreen__arrow--left"
                onClick={(e) => {
                  e.stopPropagation();
                  handlePrev();
                }}
                aria-label="上一张"
              >
                ‹
              </button>
              <button
                className="image-gallery-fullscreen__arrow image-gallery-fullscreen__arrow--right"
                onClick={(e) => {
                  e.stopPropagation();
                  handleNext();
                }}
                aria-label="下一张"
              >
                ›
              </button>

              <div className="image-gallery-fullscreen__counter">
                {currentIndex + 1} / {images.length}
              </div>
            </>
          )}
        </div>
      )}
    </>
  );
};

export default ImageGallery;
