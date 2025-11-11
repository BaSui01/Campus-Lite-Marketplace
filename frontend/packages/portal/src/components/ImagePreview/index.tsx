/**
 * ImagePreview 组件 - 图片预览（全屏）
 * @author BaSui 😎
 * @description 点击图片放大预览，支持左右切换
 */

import React, { useEffect, useCallback } from 'react';
import './ImagePreview.css';

interface ImagePreviewProps {
  images: string[];
  currentIndex: number;
  onClose: () => void;
  onNext?: () => void;
  onPrev?: () => void;
}

const ImagePreview: React.FC<ImagePreviewProps> = ({
  images,
  currentIndex,
  onClose,
  onNext,
  onPrev,
}) => {
  /**
   * 键盘事件处理
   */
  const handleKeyDown = useCallback((e: KeyboardEvent) => {
    switch (e.key) {
      case 'Escape':
        onClose();
        break;
      case 'ArrowLeft':
        onPrev?.();
        break;
      case 'ArrowRight':
        onNext?.();
        break;
    }
  }, [onClose, onPrev, onNext]);

  useEffect(() => {
    document.body.style.overflow = 'hidden';
    document.addEventListener('keydown', handleKeyDown);

    return () => {
      document.body.style.overflow = '';
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [handleKeyDown]);

  const hasMultipleImages = images.length > 1;
  const canGoPrev = currentIndex > 0;
  const canGoNext = currentIndex < images.length - 1;

  return (
    <div className="image-preview-overlay" onClick={onClose}>
      {/* 关闭按钮 */}
      <button className="image-preview__close" onClick={onClose} aria-label="关闭">
        ✕
      </button>

      {/* 主图 */}
      <img
        src={images[currentIndex]}
        alt={`预览图片 ${currentIndex + 1}`}
        className="image-preview__image"
        onClick={(e) => e.stopPropagation()}
      />

      {/* 左右切换按钮 */}
      {hasMultipleImages && (
        <>
          {canGoPrev && (
            <button
              className="image-preview__arrow image-preview__arrow--left"
              onClick={(e) => {
                e.stopPropagation();
                onPrev?.();
              }}
              aria-label="上一张"
            >
              ‹
            </button>
          )}
          {canGoNext && (
            <button
              className="image-preview__arrow image-preview__arrow--right"
              onClick={(e) => {
                e.stopPropagation();
                onNext?.();
              }}
              aria-label="下一张"
            >
              ›
            </button>
          )}
        </>
      )}

      {/* 图片计数 */}
      {hasMultipleImages && (
        <div className="image-preview__counter">
          {currentIndex + 1} / {images.length}
        </div>
      )}
    </div>
  );
};

export default ImagePreview;
