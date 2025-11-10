/**
 * 旋转验证码组件 - 转出正确角度！🔄✨
 * @author BaSui 😎
 * @date 2025-11-10
 */

import React, { useState, useEffect, useRef } from 'react';
import { apiClient } from '../../services/api-client';
import './RotateCaptcha.css';

export interface RotateCaptchaProps {
  /** 验证成功回调 */
  onSuccess?: (rotateId: string, angle: number) => void;
  /** 验证失败回调 */
  onFail?: () => void;
  /** 自定义类名 */
  className?: string;
  /** 提示文字 */
  text?: string;
}

interface RotateCaptchaData {
  rotateId: string;
  originalImage: string;
  rotatedImage: string;
  expiresIn: number;
}

export const RotateCaptcha: React.FC<RotateCaptchaProps> = ({
  onSuccess,
  onFail,
  className = '',
  text = '拖动滑块旋转图片',
}) => {
  const [captchaData, setCaptchaData] = useState<RotateCaptchaData | null>(null);
  const [currentAngle, setCurrentAngle] = useState(0);
  const [isRotating, setIsRotating] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isFailed, setIsFailed] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [startX, setStartX] = useState(0);
  const sliderRef = useRef<HTMLDivElement>(null);
  const trackRef = useRef<HTMLDivElement>(null);

  // 生成旋转验证码
  const generateCaptcha = async () => {
    try {
      setIsLoading(true);
      const response = await apiClient.get('/api/captcha/rotate');
      
      if (response.data.code === 0 && response.data.data) {
        setCaptchaData(response.data.data);
        setCurrentAngle(0);
        setIsSuccess(false);
        setIsFailed(false);
        console.log('✅ [RotateCaptcha] 验证码生成成功');
      }
    } catch (error) {
      console.error('❌ [RotateCaptcha] 生成验证码失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  // 组件挂载时生成验证码
  useEffect(() => {
    generateCaptcha();
  }, []);

  // 开始旋转
  const handleMouseDown = (event: React.MouseEvent<HTMLDivElement>) => {
    if (isSuccess || !captchaData) return;
    setIsRotating(true);
    setStartX(event.clientX);
  };

  const handleTouchStart = (event: React.TouchEvent<HTMLDivElement>) => {
    if (isSuccess || !captchaData) return;
    setIsRotating(true);
    setStartX(event.touches[0].clientX);
  };

  // 旋转中
  useEffect(() => {
    if (!isRotating) return;

    const handleMouseMove = (event: MouseEvent) => {
      if (!trackRef.current) return;

      const trackWidth = trackRef.current.offsetWidth;
      const distance = event.clientX - startX;
      
      // 计算旋转角度（滑动整个轨道 = 360度）
      const angle = Math.round((distance / trackWidth) * 360);
      setCurrentAngle((angle + 360) % 360);
    };

    const handleTouchMove = (event: TouchEvent) => {
      if (!trackRef.current) return;

      const trackWidth = trackRef.current.offsetWidth;
      const distance = event.touches[0].clientX - startX;
      
      const angle = Math.round((distance / trackWidth) * 360);
      setCurrentAngle((angle + 360) % 360);
    };

    const handleMouseUp = async () => {
      setIsRotating(false);
      await verifyRotation();
    };

    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('touchmove', handleTouchMove);
    document.addEventListener('mouseup', handleMouseUp);
    document.addEventListener('touchend', handleMouseUp);

    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('touchmove', handleTouchMove);
      document.removeEventListener('mouseup', handleMouseUp);
      document.removeEventListener('touchend', handleMouseUp);
    };
  }, [isRotating, startX, captchaData]);

  // 验证旋转角度
  const verifyRotation = async () => {
    if (!captchaData || isSuccess) return;

    try {
      const response = await apiClient.post('/api/captcha/rotate/verify', {
        rotateId: captchaData.rotateId,
        angle: currentAngle,
      });

      if (response.data.code === 0 && response.data.data === true) {
        console.log('✅ [RotateCaptcha] 验证成功！');
        setIsSuccess(true);
        onSuccess?.(captchaData.rotateId, currentAngle);
      } else {
        console.log('❌ [RotateCaptcha] 验证失败！');
        setIsFailed(true);
        onFail?.();

        // 1.5秒后重置
        setTimeout(() => {
          resetCaptcha();
        }, 1500);
      }
    } catch (error) {
      console.error('❌ [RotateCaptcha] 验证请求失败:', error);
      setIsFailed(true);
      onFail?.();
      setTimeout(() => {
        resetCaptcha();
      }, 1500);
    }
  };

  // 重置验证码
  const resetCaptcha = () => {
    setCurrentAngle(0);
    setIsSuccess(false);
    setIsFailed(false);
    generateCaptcha();
  };

  // 计算滑块位置（基于当前角度）
  const sliderPosition = trackRef.current
    ? (currentAngle / 360) * (trackRef.current.offsetWidth - 50)
    : 0;

  return (
    <div className={`rotate-captcha ${className}`}>
      {/* 图片区域 */}
      <div className="rotate-captcha__image-container">
        {isLoading ? (
          <div className="rotate-captcha__loading">加载中...</div>
        ) : captchaData ? (
          <>
            {/* 原始图片（参考） */}
            <div className="rotate-captcha__original">
              <img src={captchaData.originalImage} alt="原始图片" />
              <div className="rotate-captcha__label">参考图</div>
            </div>

            {/* 旋转图片 */}
            <div className="rotate-captcha__rotated">
              <img
                src={captchaData.rotatedImage}
                alt="旋转图片"
                style={{
                  transform: `rotate(${currentAngle}deg)`,
                }}
              />
              <div className="rotate-captcha__angle">{currentAngle}°</div>
            </div>
          </>
        ) : (
          <div className="rotate-captcha__error">加载失败</div>
        )}
      </div>

      {/* 滑动轨道 */}
      <div className="rotate-captcha__track" ref={trackRef}>
        <div className="rotate-captcha__progress" style={{ width: `${sliderPosition + 50}px` }} />
        
        <span className="rotate-captcha__text">
          {isSuccess ? '✅ 验证成功' : isFailed ? '❌ 验证失败，请重试' : text}
        </span>

        <div
          className={`rotate-captcha__slider ${isSuccess ? 'rotate-captcha__slider--success' : ''} ${isFailed ? 'rotate-captcha__slider--failed' : ''}`}
          ref={sliderRef}
          style={{ left: `${sliderPosition}px` }}
          onMouseDown={handleMouseDown}
          onTouchStart={handleTouchStart}
        >
          {isSuccess ? '✓' : '↔'}
        </div>
      </div>

      {/* 刷新按钮 */}
      <button
        type="button"
        className="rotate-captcha__refresh"
        onClick={resetCaptcha}
        disabled={isLoading}
        title="刷新验证码"
      >
        🔄
      </button>
    </div>
  );
};

export default RotateCaptcha;
