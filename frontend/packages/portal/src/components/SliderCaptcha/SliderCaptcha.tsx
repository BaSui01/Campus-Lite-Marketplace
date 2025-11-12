/**
 * 滑块验证组件 - 拼图式人机验证！🧩🔒
 * @author BaSui 😎
 * @description 集成后端API的高级滑块验证，支持轨迹记录防作弊
 * @updated 2025-11-10 - 重构：使用 slideCaptchaService 替代纯前端验证
 */

import React, { useState, useRef, useEffect } from 'react';
import { slideCaptchaService } from '@campus/shared/services/captcha';
import type { SlideCaptchaResponse, SlideVerifyRequest } from '@campus/shared/api/models';
import './SliderCaptcha.css';

/**
 * 滑块验证组件的 Props 接口
 */
export interface SliderCaptchaProps {
  /**
   * 验证成功回调（返回 slideId 和 position 用于登录验证）
   */
  onSuccess?: (slideId: string, position: number) => void;

  /**
   * 验证失败回调
   */
  onFail?: () => void;

  /**
   * 自定义提示文字
   * @default '拖动滑块完成拼图'
   */
  text?: string;

  /**
   * 自定义类名
   */
  className?: string;

  /**
   * 是否重置验证状态（触发重新生成验证码）
   */
  reset?: boolean;
}

/**
 * 轨迹点类型
 */
type TrackPoint = {
  x: number;
  y: number;
  t: number;
};

/**
 * SliderCaptcha 组件
 *
 * @example
 * ```tsx
 * <SliderCaptcha
 *   onSuccess={(slideId, position) => {
 *     console.log('验证成功！slideId:', slideId, 'position:', position);
 *     // 将 slideId 和 position 传递给登录接口
 *   }}
 *   onFail={() => console.log('验证失败！')}
 * />
 * ```
 */
export const SliderCaptcha: React.FC<SliderCaptchaProps> = ({
  onSuccess,
  onFail,
  text = '拖动滑块完成拼图',
  className = '',
  reset = false,
}) => {
  // ==================== 状态管理 ====================
  const [slideData, setSlideData] = useState<SlideCaptchaResponse | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isFailed, setIsFailed] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [puzzleLeft, setPuzzleLeft] = useState(0);
  const [puzzleTop, setPuzzleTop] = useState(0);
  const [startX, setStartX] = useState(0);
  const [startY, setStartY] = useState(0);
  const [startTime, setStartTime] = useState(0);
  const [track, setTrack] = useState<TrackPoint[]>([]);

  // ==================== DOM 引用 ====================
  const puzzleRef = useRef<HTMLImageElement>(null);

  /**
   * 🎨 生成滑块验证码
   */
  const generateSlider = async () => {
    try {
      setIsLoading(true);
      console.log('🔄 [SliderCaptcha] 正在生成滑块验证码...');

      // ✅ 使用 Service 层（符合规范！）
      const data = await slideCaptchaService.generateWithImage();
      setSlideData(data);

      console.log('✅ [SliderCaptcha] 滑块验证码生成成功:', data.slideId);
    } catch (err: any) {
      console.error('❌ [SliderCaptcha] 生成滑块验证码失败:', err);
      setIsFailed(true);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * 🔄 重置验证状态
   */
  const resetCaptcha = () => {
    setIsDragging(false);
    setIsSuccess(false);
    setIsFailed(false);
    setPuzzleLeft(0);
    setPuzzleTop(slideData?.yposition || 0);
    setTrack([]);
    generateSlider();
  };

  /**
   * 🎯 开始拖动拼图（鼠标事件）
   */
  const handleMouseDown = (event: React.MouseEvent) => {
    if (isSuccess || !slideData) return;
    // ✅ 移除 preventDefault()，避免 Edge 浏览器兼容性问题
    // event.preventDefault();

    setIsDragging(true);
    setIsFailed(false);
    setStartX(event.clientX - puzzleLeft);
    setStartY(event.clientY - puzzleTop);
    setStartTime(Date.now());
    setTrack([{ x: puzzleLeft, y: puzzleTop, t: 0 }]);
  };

  /**
   * 🎯 开始拖动拼图（触摸事件）
   */
  const handleTouchStart = (event: React.TouchEvent) => {
    if (isSuccess || !slideData) return;
    const touch = event.touches[0];

    setIsDragging(true);
    setIsFailed(false);
    setStartX(touch.clientX - puzzleLeft);
    setStartY(touch.clientY - puzzleTop);
    setStartTime(Date.now());
    setTrack([{ x: puzzleLeft, y: puzzleTop, t: 0 }]);
  };

  /**
   * 🎯 拖动中（鼠标事件）
   */
  const handleMouseMove = (event: MouseEvent) => {
    if (!isDragging || isSuccess || !slideData) return;

    const newLeft = Math.max(0, Math.min(event.clientX - startX, 250));
    const newTop = Math.max(0, Math.min(event.clientY - startY, 150));

    setPuzzleLeft(newLeft);
    setPuzzleTop(newTop);

    setTrack((prev) => [...prev, {
      x: newLeft,
      y: newTop,
      t: Date.now() - startTime,
    }]);
  };

  /**
   * 🎯 拖动中（触摸事件）
   */
  const handleTouchMove = (event: TouchEvent) => {
    if (!isDragging || isSuccess || !slideData) return;
    const touch = event.touches[0];

    const newLeft = Math.max(0, Math.min(touch.clientX - startX, 250));
    const newTop = Math.max(0, Math.min(touch.clientY - startY, 150));

    setPuzzleLeft(newLeft);
    setPuzzleTop(newTop);

    setTrack((prev) => [...prev, {
      x: newLeft,
      y: newTop,
      t: Date.now() - startTime,
    }]);
  };

  /**
   * 🎯 结束拖动并收集数据（不调用后端验证，留给登录接口验证）
   * 
   * 🔧 BaSui 修复 (2025-11-11)：
   * 问题：前端验证后Redis中的验证码被删除，登录时无法再次验证
   * 方案：前端只收集数据（slideId + position），真正验证由登录接口执行
   */
  const handleMouseUp = async () => {
    if (!isDragging || isSuccess || !slideData) return;

    setIsDragging(false);

    const finalPosition = Math.round(puzzleLeft);

    // ✅ 简单的前端位置校验（允许一定误差，给用户反馈）
    // 注意：这不是真实验证，只是UI反馈，真实验证在后端
    const isLikelyCorrect = finalPosition > 40; // 简单判断：至少拖动了40px

    if (isLikelyCorrect) {
      console.log('✅ [SliderCaptcha] 位置已记录，等待后端验证:', { slideId: slideData.slideId, position: finalPosition });
      setIsSuccess(true);
      onSuccess?.(slideData.slideId!, finalPosition);
    } else {
      console.log('❌ [SliderCaptcha] 位置太短，请重新拖动');
      setIsFailed(true);
      onFail?.();

      // 1.5秒后自动重置
      setTimeout(() => {
        resetCaptcha();
      }, 1500);
    }
  };

  /**
   * 🎣 监听全局鼠标和触摸事件
   */
  useEffect(() => {
    if (isDragging) {
      // 鼠标事件
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);

      // 触摸事件
      document.addEventListener('touchmove', handleTouchMove);
      document.addEventListener('touchend', handleMouseUp);

      return () => {
        document.removeEventListener('mousemove', handleMouseMove);
        document.removeEventListener('mouseup', handleMouseUp);
        document.removeEventListener('touchmove', handleTouchMove);
        document.removeEventListener('touchend', handleMouseUp);
      };
    }
  }, [isDragging, puzzleLeft, puzzleTop, slideData, track]);

  useEffect(() => {
    generateSlider();
  }, []);

  useEffect(() => {
    if (reset) resetCaptcha();
  }, [reset]);

  useEffect(() => {
    if (slideData?.yposition !== undefined) {
      setPuzzleTop(slideData.yposition);
    }
  }, [slideData]);

  return (
    <div className={`slider-captcha ${isSuccess ? 'slider-captcha--success' : ''} ${isFailed ? 'slider-captcha--failed' : ''} ${isLoading ? 'slider-captcha--loading' : ''} ${className}`}>
      {slideData && (
        <div className="slider-captcha__puzzle" style={{ position: 'relative', width: '300px', height: '200px' }}>
          <img
            src={slideData.backgroundImage}
            alt="验证码背景"
            style={{ width: '100%', height: '100%', display: 'block' }}
          />
          <img
            ref={puzzleRef}
            src={slideData.sliderImage}
            alt="拼图滑块"
            onMouseDown={handleMouseDown}
            onTouchStart={handleTouchStart}
            style={{
              position: 'absolute',
              left: `${puzzleLeft}px`,
              top: `${puzzleTop}px`,
              width: '50px',
              height: '50px',
              cursor: isDragging ? 'grabbing' : 'grab',
              userSelect: 'none',
              touchAction: 'none', // ✅ 禁用浏览器默认触摸行为
            }}
          />
        </div>
      )}

      {isLoading && <div>正在加载验证码...</div>}

      <div style={{ marginTop: '10px', textAlign: 'center' }}>
        {isSuccess ? '✅ 验证成功' : isFailed ? '❌ 验证失败，请重试' : '🧩 拖动拼图到正确位置'}
      </div>

      <button
        type="button"
        onClick={resetCaptcha}
        disabled={isLoading}
        style={{ marginTop: '10px' }}
      >
        🔄 刷新
      </button>
    </div>
  );
};

export default SliderCaptcha;
