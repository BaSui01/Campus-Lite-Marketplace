/**
 * 图形验证码组件 - 让机器人靠边站！🎨🚫
 * @author BaSui 😎
 * @date 2025-11-09
 * @updated 2025-11-10 - 重构：使用 captchaService 替代 DefaultApi（遵循规范）
 */

import React, { useState, useEffect } from 'react';
import { imageCaptchaService } from '../../services/captcha';
import type { CaptchaResponse } from '../../api/models';
import './ImageCaptcha.css';

export interface ImageCaptchaProps {
  /** 验证成功回调（传递 captchaId 和用户输入） */
  onSuccess?: (captchaId: string, code: string) => void;
  /** 验证失败回调 */
  onFail?: () => void;
  /** 自定义类名 */
  className?: string;
  /** 重置标志（触发刷新验证码） */
  reset?: boolean;
}

export const ImageCaptcha: React.FC<ImageCaptchaProps> = ({
  onSuccess,
  onFail: _onFail,  // ⚠️ 使用下划线前缀表示故意未使用
  className = '',
  reset = false,
}) => {
  const [captchaData, setCaptchaData] = useState<CaptchaResponse | null>(null);
  const [inputValue, setInputValue] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // 🎨 生成验证码
  const generateCaptcha = async () => {
    try {
      setLoading(true);
      setError('');
      setInputValue('');

      // ✅ 使用 Service 层（符合规范！）
      const data = await imageCaptchaService.generate();
      setCaptchaData(data);

      console.log('✅ [ImageCaptcha] 验证码生成成功:', data.captchaId);
    } catch (err: any) {
      setError('验证码加载失败，请重试 😰');
      console.error('❌ [ImageCaptcha] 生成验证码失败:', err);
    } finally {
      setLoading(false);
    }
  };

  // 组件挂载时生成验证码
  useEffect(() => {
    generateCaptcha();
  }, []);

  // 监听 reset 变化
  useEffect(() => {
    if (reset) {
      generateCaptcha();
    }
  }, [reset]);

  // 处理输入变化
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.toUpperCase();
    setInputValue(value);
    setError('');

    // 自动验证（当输入4位时）
    if (value.length === 4 && captchaData) {
      onSuccess?.(captchaData.captchaId!, value);
    }
  };

  // 刷新验证码
  const handleRefresh = () => {
    generateCaptcha();
  };

  return (
    <div className={`campus-image-captcha ${className}`}>
      <div className="campus-image-captcha__container">
        {/* 验证码图片 */}
        <div className="campus-image-captcha__image-wrapper">
          {loading ? (
            <div className="campus-image-captcha__loading">加载中...</div>
          ) : captchaData?.imageBase64 ? (
            <img
              src={captchaData.imageBase64}
              alt="验证码"
              className="campus-image-captcha__image"
            />
          ) : (
            <div className="campus-image-captcha__error">加载失败</div>
          )}
        </div>

        {/* 刷新按钮 */}
        <button
          type="button"
          className="campus-image-captcha__refresh"
          onClick={handleRefresh}
          disabled={loading}
          title="刷新验证码"
        >
          🔄
        </button>
      </div>

      {/* 输入框 */}
      <input
        type="text"
        className="campus-image-captcha__input"
        placeholder="请输入验证码"
        value={inputValue}
        onChange={handleInputChange}
        maxLength={4}
        disabled={loading}
      />

      {/* 错误提示 */}
      {error && <div className="campus-image-captcha__error-text">{error}</div>}
    </div>
  );
};

export default ImageCaptcha;
