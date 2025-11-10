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
  const [isSliding, setIsSliding] = useState(false); // 是否正在滑动
  const [isSuccess, setIsSuccess] = useState(false); // 是否验证成功
  const [isFailed, setIsFailed] = useState(false); // 是否验证失败
  const [isLoading, setIsLoading] = useState(false); // 是否正在加载
  const [sliderLeft, setSliderLeft] = useState(0); // 滑块左侧位置（px）
  const [startX, setStartX] = useState(0); // 开始滑动的 X 坐标
  const [startTime, setStartTime] = useState(0); // 开始滑动的时间戳
  const [track, setTrack] = useState<TrackPoint[]>([]); // 滑动轨迹

  // ==================== DOM 引用 ====================
  const sliderRef = useRef<HTMLDivElement>(null);
  const trackRef = useRef<HTMLDivElement>(null);

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
    setIsSliding(false);
    setIsSuccess(false);
    setIsFailed(false);
    setSliderLeft(0);
    setTrack([]);
    generateSlider(); // 重新生成验证码
  };

  /**
   * 🎯 开始滑动
   */
  const handleMouseDown = (event: React.MouseEvent) => {
    if (isSuccess || !slideData) return; // 已验证成功或未加载，不再响应

    setIsSliding(true);
    setIsFailed(false);
    setStartX(event.clientX);
    setStartTime(Date.now());
    setTrack([{ x: 0, y: 0, t: 0 }]); // 初始化轨迹
  };

  /**
   * 🎯 滑动中
   */
  const handleMouseMove = (event: MouseEvent) => {
    if (!isSliding || isSuccess || !slideData) return;

    const trackWidth = trackRef.current?.offsetWidth || 0;
    const sliderWidth = sliderRef.current?.offsetWidth || 0;
    const maxLeft = trackWidth - sliderWidth;

    const distance = event.clientX - startX;
    const newLeft = Math.max(0, Math.min(distance, maxLeft));

    setSliderLeft(newLeft);

    // 📊 记录轨迹点（防作弊）
    const point: TrackPoint = {
      x: newLeft,
      y: 0,
      t: Date.now() - startTime,
    };
    setTrack((prev) => [...prev, point]);
  };

  /**
   * 🎯 结束滑动（验证）
   */
  const handleMouseUp = async () => {
    if (!isSliding || isSuccess || !slideData) return;

    setIsSliding(false);

    try {
      console.log('🔍 [SliderCaptcha] 正在验证滑块位置...');
      console.log('📊 滑动轨迹点数:', track.length);

      // ✅ 调用后端API验证（带轨迹分析）
      const request: SlideVerifyRequest = {
        slideId: slideData.slideId!,
        xposition: Math.round(sliderLeft), // 修正字段名（后端是 xposition 而不是 xPosition）
        track: track.map((p) => ({
          x: p.x,
          y: p.y,
          t: p.t,
        })),
      };

      const isValid = await slideCaptchaService.verifyWithTrack(request);

      if (isValid) {
        // ✅ 验证成功！
        console.log('✅ [SliderCaptcha] 验证成功！');
        setIsSuccess(true);
        onSuccess?.(slideData.slideId!, Math.round(sliderLeft));
      } else {
        // ❌ 验证失败！
        console.log('❌ [SliderCaptcha] 验证失败！');
        setIsFailed(true);
        setSliderLeft(0); // 回弹
        onFail?.();

        // 1.5 秒后重置
        setTimeout(() => {
          resetCaptcha();
        }, 1500);
      }
    } catch (error: any) {
      console.error('❌ [SliderCaptcha] 验证请求失败:', error);
      setIsFailed(true);
      setSliderLeft(0);
      onFail?.();

      // 1.5 秒后重置
      setTimeout(() => {
        resetCaptcha();
      }, 1500);
    }
  };

  /**
   * 📱 触摸事件处理（移动端兼容）
   */
  const handleTouchStart = (event: React.TouchEvent) => {
    if (isSuccess || !slideData) return;

    setIsSliding(true);
    setIsFailed(false);
    setStartX(event.touches[0].clientX);
    setStartTime(Date.now());
    setTrack([{ x: 0, y: 0, t: 0 }]);
  };

  const handleTouchMove = (event: TouchEvent) => {
    if (!isSliding || isSuccess || !slideData) return;

    const trackWidth = trackRef.current?.offsetWidth || 0;
    const sliderWidth = sliderRef.current?.offsetWidth || 0;
    const maxLeft = trackWidth - sliderWidth;

    const distance = event.touches[0].clientX - startX;
    const newLeft = Math.max(0, Math.min(distance, maxLeft));

    setSliderLeft(newLeft);

    // 记录轨迹
    const point: TrackPoint = {
      x: newLeft,
      y: 0,
      t: Date.now() - startTime,
    };
    setTrack((prev) => [...prev, point]);
  };

  const handleTouchEnd = () => {
    handleMouseUp();
  };

  /**
   * 🎣 监听全局鼠标/触摸事件
   */
  useEffect(() => {
    if (isSliding) {
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
      document.addEventListener('touchmove', handleTouchMove);
      document.addEventListener('touchend', handleTouchEnd);

      return () => {
        document.removeEventListener('mousemove', handleMouseMove);
        document.removeEventListener('mouseup', handleMouseUp);
        document.removeEventListener('touchmove', handleTouchMove);
        document.removeEventListener('touchend', handleTouchEnd);
      };
    }
  }, [isSliding, sliderLeft, slideData, track]);

  /**
   * 🎣 组件挂载时生成验证码
   */
  useEffect(() => {
    generateSlider();
  }, []);

  /**
   * 🎣 监听 reset 属性变化
   */
  useEffect(() => {
    if (reset) {
      resetCaptcha();
    }
  }, [reset]);

  /**
   * 🎨 组装 CSS 类名
   */
  const containerClassNames = [
    'slider-captcha',
    isSuccess ? 'slider-captcha--success' : '',
    isFailed ? 'slider-captcha--failed' : '',
    isLoading ? 'slider-captcha--loading' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={containerClassNames}>
      {/* 🖼️ 拼图背景图 */}
      {slideData && (
        <div className="slider-captcha__puzzle">
          <img
            src={slideData.backgroundImage}
            alt="验证码背景"
            className="slider-captcha__background"
          />
          {/* 🧩 拼图滑块图（跟随滑块移动） */}
          <img
            src={slideData.sliderImage}
            alt="拼图滑块"
            className="slider-captcha__puzzle-piece"
            style={{
              left: `${sliderLeft}px`,
              top: `${slideData.yposition || 0}px`, // 修正字段名
            }}
          />
        </div>
      )}

      {/* 加载中提示 */}
      {isLoading && (
        <div className="slider-captcha__loading-text">
          正在加载验证码...
        </div>
      )}

      {/* 滑动轨道 */}
      <div className="slider-captcha__track" ref={trackRef}>
        {/* 背景进度条 */}
        <div
          className="slider-captcha__progress"
          style={{ width: `${sliderLeft + 50}px` }} // 滑块宽度 50px
        />

        {/* 提示文字 */}
        <span className="slider-captcha__text">
          {isSuccess ? '✅ 验证成功' : isFailed ? '❌ 验证失败，请重试' : text}
        </span>

        {/* 滑块 */}
        <div
          className="slider-captcha__slider"
          ref={sliderRef}
          style={{ left: `${sliderLeft}px` }}
          onMouseDown={handleMouseDown}
          onTouchStart={handleTouchStart}
        >
          {isSuccess ? '✓' : '→'}
        </div>
      </div>

      {/* 刷新按钮 */}
      <button
        type="button"
        className="slider-captcha__refresh"
        onClick={resetCaptcha}
        disabled={isLoading}
        title="刷新验证码"
      >
        🔄
      </button>
    </div>
  );
};

export default SliderCaptcha;
