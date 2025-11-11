/**
 * 点选验证码组件 - 按顺序点击文字！👆✨
 * @author BaSui 😎
 * @date 2025-11-10
 */

import React, { useState, useEffect, useRef } from 'react';
import { apiClient } from '../../utils/apiClient';
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
  const [clickedPoints, setClickedPoints] = useState<ClickPoint[]>([]); // 用于传给后端的坐标（原始尺寸）
  const [displayPoints, setDisplayPoints] = useState<ClickPoint[]>([]); // 用于显示标记的坐标（显示尺寸）
  const [isSuccess, setIsSuccess] = useState(false);
  const [isFailed, setIsFailed] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const imageRef = useRef<HTMLDivElement>(null);

  // 生成点选验证码
  const generateCaptcha = async () => {
    try {
      setIsLoading(true);
      const response = await apiClient.get('/api/captcha/click');
      
      if (response.data.code === 200 && response.data.data) {
        setCaptchaData(response.data.data);
        setClickedPoints([]);
        setDisplayPoints([]);
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
    const imgElement = imageRef.current?.querySelector('img');
    if (!rect || !imgElement) return;

    // 🎯 获取点击位置（相对于容器）
    const clickX = event.clientX - rect.left;
    const clickY = event.clientY - rect.top;

    // 🎯 获取图片的实际尺寸和显示尺寸
    const naturalWidth = imgElement.naturalWidth;   // 图片原始宽度（后端生成：300px）
    const naturalHeight = imgElement.naturalHeight; // 图片原始高度（后端生成：200px）
    const displayWidth = imgElement.clientWidth;    // 图片显示宽度（前端可能放大）
    const displayHeight = imgElement.clientHeight;  // 图片显示高度（前端可能放大）

    // 🎯 计算缩放比例
    const scaleX = naturalWidth / displayWidth;
    const scaleY = naturalHeight / displayHeight;

    // 🎯 转换为图片原始坐标（用于传给后端）
    const scaledX = Math.round(clickX * scaleX);
    const scaledY = Math.round(clickY * scaleY);

    console.log('👆 [ClickCaptcha] 坐标转换:', {
      点击位置: { clickX, clickY },
      图片原始尺寸: { naturalWidth, naturalHeight },
      图片显示尺寸: { displayWidth, displayHeight },
      缩放比例: { scaleX, scaleY },
      后端坐标: { scaledX, scaledY },
    });

    // ✅ 保存两组坐标
    const newScaledPoints = [...clickedPoints, { x: scaledX, y: scaledY }]; // 后端坐标
    const newDisplayPoints = [...displayPoints, { x: clickX, y: clickY }];  // 显示坐标

    setClickedPoints(newScaledPoints);
    setDisplayPoints(newDisplayPoints);

    console.log('👆 [ClickCaptcha] 点击位置 - 后端:', { x: scaledX, y: scaledY }, '显示:', { x: clickX, y: clickY }, '总共:', newScaledPoints.length);

    // 如果点击数量达到目标数量，自动验证
    if (newScaledPoints.length === captchaData.targetWords.length) {
      await verifyClick(newScaledPoints);
    }
  };

  // 🎯 收集点击数据（不调用后端验证，留给登录接口验证）
  //
  // 🔧 BaSui 修复 (2025-11-11)：
  // 问题：前端验证后Redis中的验证码被删除，登录时无法再次验证
  // 方案：前端只收集数据（clickId + clickPoints），真正验证由登录接口执行
  const verifyClick = async (points: ClickPoint[]) => {
    if (!captchaData) return;

    // ✅ 简单的前端校验（给用户反馈）
    // 注意：这不是真实验证，只是UI反馈，真实验证在后端
    const isLikelyCorrect = points.length === captchaData.targetWords.length;

    if (isLikelyCorrect) {
      console.log('✅ [ClickCaptcha] 点击已记录，等待后端验证:', {
        clickId: captchaData.clickId,
        points,
      });
      setIsSuccess(true);
      onSuccess?.(captchaData.clickId, points);
    } else {
      console.log('❌ [ClickCaptcha] 点击数量不正确');
      setIsFailed(true);
      onFail?.();

      // 1.5秒后重置
      setTimeout(() => {
        resetCaptcha();
      }, 1500);
    }
  };

  // 重置验证码
  const resetCaptcha = () => {
    setClickedPoints([]);
    setDisplayPoints([]);
    setIsSuccess(false);
    setIsFailed(false);
    generateCaptcha();
  };

  // 撤销上一次点击
  const undoLastClick = () => {
    if (clickedPoints.length > 0) {
      setClickedPoints(clickedPoints.slice(0, -1));
      setDisplayPoints(displayPoints.slice(0, -1));
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
            
            {/* 点击标记（使用显示坐标） */}
            {displayPoints.map((point, index) => (
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
