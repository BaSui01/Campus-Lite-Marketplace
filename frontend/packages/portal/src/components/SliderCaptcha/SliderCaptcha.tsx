/**
 * 滑块验证组件 - 点击式人机验证！🔒
 * @author BaSui 😎
 * @description 滑动解锁验证组件，防止机器人乱搞！
 */

import React, { useState, useRef, useEffect } from 'react';
import './SliderCaptcha.css';

/**
 * 滑块验证组件的 Props 接口
 */
export interface SliderCaptchaProps {
  /**
   * 验证成功回调
   */
  onSuccess?: () => void;

  /**
   * 验证失败回调
   */
  onFail?: () => void;

  /**
   * 自定义提示文字
   * @default '向右滑动完成验证'
   */
  text?: string;

  /**
   * 自定义类名
   */
  className?: string;

  /**
   * 是否重置验证状态
   */
  reset?: boolean;
}

/**
 * SliderCaptcha 组件
 *
 * @example
 * ```tsx
 * <SliderCaptcha
 *   onSuccess={() => console.log('验证成功！')}
 *   onFail={() => console.log('验证失败！')}
 * />
 * ```
 */
export const SliderCaptcha: React.FC<SliderCaptchaProps> = ({
  onSuccess,
  onFail,
  text = '向右滑动完成验证',
  className = '',
  reset = false,
}) => {
  // 状态管理
  const [isSliding, setIsSliding] = useState(false); // 是否正在滑动
  const [isSuccess, setIsSuccess] = useState(false); // 是否验证成功
  const [isFailed, setIsFailed] = useState(false); // 是否验证失败
  const [sliderLeft, setSliderLeft] = useState(0); // 滑块左侧位置
  const [startX, setStartX] = useState(0); // 开始滑动的 X 坐标

  // DOM 引用
  const sliderRef = useRef<HTMLDivElement>(null);
  const trackRef = useRef<HTMLDivElement>(null);

  /**
   * 🔄 重置验证状态
   */
  const resetCaptcha = () => {
    setIsSliding(false);
    setIsSuccess(false);
    setIsFailed(false);
    setSliderLeft(0);
  };

  /**
   * 🎯 开始滑动
   */
  const handleMouseDown = (event: React.MouseEvent) => {
    if (isSuccess) return; // 已验证成功，不再响应

    setIsSliding(true);
    setIsFailed(false);
    setStartX(event.clientX);
  };

  /**
   * 🎯 滑动中
   */
  const handleMouseMove = (event: MouseEvent) => {
    if (!isSliding || isSuccess) return;

    const trackWidth = trackRef.current?.offsetWidth || 0;
    const sliderWidth = sliderRef.current?.offsetWidth || 0;
    const maxLeft = trackWidth - sliderWidth;

    const distance = event.clientX - startX;
    const newLeft = Math.max(0, Math.min(distance, maxLeft));

    setSliderLeft(newLeft);
  };

  /**
   * 🎯 结束滑动
   */
  const handleMouseUp = () => {
    if (!isSliding || isSuccess) return;

    setIsSliding(false);

    const trackWidth = trackRef.current?.offsetWidth || 0;
    const sliderWidth = sliderRef.current?.offsetWidth || 0;
    const successThreshold = trackWidth - sliderWidth - 5; // 允许 5px 误差

    // 判断是否滑动到右侧边缘
    if (sliderLeft >= successThreshold) {
      // ✅ 验证成功！
      setIsSuccess(true);
      setSliderLeft(trackWidth - sliderWidth); // 完全贴边
      onSuccess?.();
    } else {
      // ❌ 验证失败，回弹！
      setIsFailed(true);
      setSliderLeft(0);
      onFail?.();

      // 1 秒后重置失败状态
      setTimeout(() => {
        setIsFailed(false);
      }, 1000);
    }
  };

  /**
   * 📱 触摸事件处理（移动端兼容）
   */
  const handleTouchStart = (event: React.TouchEvent) => {
    if (isSuccess) return;

    setIsSliding(true);
    setIsFailed(false);
    setStartX(event.touches[0].clientX);
  };

  const handleTouchMove = (event: TouchEvent) => {
    if (!isSliding || isSuccess) return;

    const trackWidth = trackRef.current?.offsetWidth || 0;
    const sliderWidth = sliderRef.current?.offsetWidth || 0;
    const maxLeft = trackWidth - sliderWidth;

    const distance = event.touches[0].clientX - startX;
    const newLeft = Math.max(0, Math.min(distance, maxLeft));

    setSliderLeft(newLeft);
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
  }, [isSliding, sliderLeft]);

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
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={containerClassNames}>
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
    </div>
  );
};

export default SliderCaptcha;
