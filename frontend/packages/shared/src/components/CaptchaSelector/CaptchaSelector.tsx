/**
 * 验证码选择器组件 - 随机选择验证码类型！🎲✨
 * @author BaSui 😎
 * @date 2025-11-10
 * @description 随机选择以下验证码类型：
 *   - 图形验证码（ImageCaptcha）
 *   - 滑块验证码（SliderCaptcha）
 *   - 旋转验证码（RotateCaptcha）- 待实现
 *   - 点选验证码（ClickCaptcha）- 待实现
 */

import React, { useState, useEffect, useMemo } from 'react';
import { ImageCaptcha } from '../ImageCaptcha/ImageCaptcha';

export type CaptchaType = 'image' | 'slider' | 'rotate' | 'click';

export interface CaptchaSelectorProps {
  /** 验证成功回调 */
  onSuccess?: (type: CaptchaType, data: any) => void;
  /** 验证失败回调 */
  onFail?: () => void;
  /** 自定义类名 */
  className?: string;
  /** 重置标志（触发刷新验证码） */
  reset?: boolean;
  /** 允许的验证码类型（默认全部） */
  allowedTypes?: CaptchaType[];
  /** 强制指定验证码类型（不随机） */
  forceType?: CaptchaType;
}

export const CaptchaSelector: React.FC<CaptchaSelectorProps> = ({
  onSuccess,
  onFail,
  className = '',
  reset = false,
  allowedTypes = ['image', 'slider'], // 目前只有图形和滑块
  forceType,
}) => {
  // 随机选择验证码类型（只在组件挂载时选择一次）
  const selectedType = useMemo<CaptchaType>(() => {
    if (forceType) {
      return forceType;
    }
    const randomIndex = Math.floor(Math.random() * allowedTypes.length);
    return allowedTypes[randomIndex];
  }, [forceType, allowedTypes]);

  const [currentType, setCurrentType] = useState<CaptchaType>(selectedType);

  // 当reset变化时，重新随机选择验证码类型
  useEffect(() => {
    if (reset && !forceType) {
      const randomIndex = Math.floor(Math.random() * allowedTypes.length);
      setCurrentType(allowedTypes[randomIndex]);
    }
  }, [reset, forceType, allowedTypes]);

  // 图形验证码成功回调
  const handleImageSuccess = (captchaId: string, code: string) => {
    console.log('✅ [CaptchaSelector] 图形验证码成功:', { captchaId, code });
    onSuccess?.('image', { captchaId, code });
  };

  // 滑块验证码成功回调
  const handleSliderSuccess = (slideId: string, xPosition: number) => {
    console.log('✅ [CaptchaSelector] 滑块验证码成功:', { slideId, xPosition });
    onSuccess?.('slider', { slideId, xPosition });
  };

  return (
    <div className={`captcha-selector ${className}`}>
      {/* 图形验证码 */}
      {currentType === 'image' && (
        <ImageCaptcha
          onSuccess={handleImageSuccess}
          onFail={onFail}
          reset={reset}
        />
      )}

      {/* 滑块验证码 - 需要从portal包导入 */}
      {currentType === 'slider' && (
        <div style={{ padding: '20px', textAlign: 'center', color: '#999' }}>
          🧩 滑块验证码（请在登录页面使用完整组件）
          <br />
          <small>提示：在 portal 或 admin 包中使用 SliderCaptcha 组件</small>
        </div>
      )}

      {/* 旋转验证码 - 待实现 */}
      {currentType === 'rotate' && (
        <div style={{ padding: '20px', textAlign: 'center', color: '#999' }}>
          🔄 旋转验证码开发中...
        </div>
      )}

      {/* 点选验证码 - 待实现 */}
      {currentType === 'click' && (
        <div style={{ padding: '20px', textAlign: 'center', color: '#999' }}>
          👆 点选验证码开发中...
        </div>
      )}

      {/* 调试信息（开发环境显示） */}
      {process.env.NODE_ENV === 'development' && (
        <div style={{ marginTop: '8px', fontSize: '12px', color: '#999', textAlign: 'center' }}>
          当前验证码类型: {currentType}
        </div>
      )}
    </div>
  );
};

export default CaptchaSelector;
