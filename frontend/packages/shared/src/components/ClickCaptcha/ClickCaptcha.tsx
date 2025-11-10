/**
 * 点选验证码组件 - 按顺序点击文字！👆✨
 * @author BaSui 😎
 * @date 2025-11-10
 */

import React, { useState, useEffect, useRef } from 'react';
import { apiClient } from '../../services/api-client';
import './ClickCaptcha.css';

export interface ClickCaptchaProps {
  /** 验证成功回调 */
  onSuccess?: (clickId: string, points: ClickPoint[]) => void;
  /** 验证失败回调 */
  onFail?: () => void;
  /** 自定义类名 */
  className?: string;
}

interface ClickPoint {
  x: number;
  y: number;
}

interface ClickCaptchaData {
  clickId: string;
  backgroundImage: string;
  targetWords: string[];
  hint: string;
  expiresIn: number;
}

export const ClickCaptcha: React.FC<ClickCaptchaProps> = ({
  onSuccess,
  onFail,
  className = '',
}) => {
  const [captchaData, setCaptchaData] = useState<ClickCaptchaData | null>(null);
  const [clickedPoints, setClickedPoints] = useState<ClickPoint[]>([]);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isFailed, setIsFailed] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const imageRef = useRef<HTMLDivElement>(null);

  // 生成点选验证码
  const generateCaptcha = async () => {
    try {
      setIsLoading(true);
      const response = await apiClient.get('/api/captcha/click');
      
      if (response.data.code === 0 && response.data.data) {
        setCaptchaData(response.data.data);
        setClickedPoints([]);
        setIsSuccess(false);
        setIsFailed(false);
        console.log('✅ [ClickCaptcha] 验证码生成成功:', response.data.data);
      }
    } catch (error) {
      console.error('❌ [ClickCaptcha] 生成验证码失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  // 组件挂载时生成验证码
  useEffect(() => {
    generateCaptcha();
  }, []);

  // 处理点击
  const handleClick = async (event: React.MouseEvent<HTMLDivElement>) => {
    if (!captchaData || isSuccess || isFailed) return;

    const rect = imageRef.current?.getBoundingClientRect();
    if (!rect) return;

    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    const newPoints = [...clickedPoints, { x, y }];
    setClickedPoints(newPoints);

    console.log('👆 [ClickCaptcha] 点击位置:', { x, y }, '总共:', newPoints.length);

    // 如果点击数量达到目标数量，自动验证
    if (newPoints.length === captchaData.targetWords.length) {
      await verifyClick(newPoints);
    }
  };

  // 验证点击
  const verifyClick = async (points: ClickPoint[]) => {
    if (!captchaData) return;

    try {
      const response = await apiClient.post('/api/captcha/click/verify', {
        clickId: captchaData.clickId,
        clickPoints: points,
      });

      if (response.data.code === 0 && response.data.data === true) {
        console.log('✅ [ClickCaptcha] 验证成功！');
        setIsSuccess(true);
        onSuccess?.(captchaData.clickId, points);
      } else {
        console.log('❌ [ClickCaptcha] 验证失败！');
        setIsFailed(true);
        onFail?.();

        // 1.5秒后重置
        setTimeout(() => {
          resetCaptcha();
        }, 1500);
      }
    } catch (error) {
      console.error('❌ [ClickCaptcha] 验证请求失败:', error);
      setIsFailed(true);
      onFail?.();
      setTimeout(() => {
        resetCaptcha();
      }, 1500);
    }
  };

  // 重置验证码
  const resetCaptcha = () => {
    setClickedPoints([]);
    setIsSuccess(false);
    setIsFailed(false);
    generateCaptcha();
  };

  // 撤销上一次点击
  const undoLastClick = () => {
    if (clickedPoints.length > 0) {
      setClickedPoints(clickedPoints.slice(0, -1));
    }
  };

  return (
    <div className={`click-captcha ${className}`}>
      {/* 提示文字 */}
      {captchaData && (
        <div className={`click-captcha__hint ${isSuccess ? 'click-captcha__hint--success' : ''} ${isFailed ? 'click-captcha__hint--failed' : ''}`}>
          {isSuccess ? '✅ 验证成功' : isFailed ? '❌ 验证失败，请重试' : captchaData.hint}
        </div>
      )}

      {/* 图片区域 */}
      <div
        ref={imageRef}
        className={`click-captcha__image-container ${isSuccess ? 'click-captcha__image-container--success' : ''} ${isFailed ? 'click-captcha__image-container--failed' : ''}`}
        onClick={handleClick}
      >
        {isLoading ? (
          <div className="click-captcha__loading">加载中...</div>
        ) : captchaData ? (
          <>
            <img src={captchaData.backgroundImage} alt="点选验证码" />
            
            {/* 点击标记 */}
            {clickedPoints.map((point, index) => (
              <div
                key={index}
                className="click-captcha__marker"
                style={{
                  left: `${point.x}px`,
                  top: `${point.y}px`,
                }}
              >
                {index + 1}
              </div>
            ))}
          </>
        ) : (
          <div className="click-captcha__error">加载失败</div>
        )}
      </div>

      {/* 操作按钮 */}
      <div className="click-captcha__actions">
        <button
          type="button"
          className="click-captcha__button click-captcha__button--undo"
          onClick={undoLastClick}
          disabled={clickedPoints.length === 0 || isLoading}
        >
          ↶ 撤销
        </button>

        <div className="click-captcha__progress">
          {captchaData && `${clickedPoints.length} / ${captchaData.targetWords.length}`}
        </div>

        <button
          type="button"
          className="click-captcha__button click-captcha__button--refresh"
          onClick={resetCaptcha}
          disabled={isLoading}
        >
          🔄 刷新
        </button>
      </div>
    </div>
  );
};

export default ClickCaptcha;
