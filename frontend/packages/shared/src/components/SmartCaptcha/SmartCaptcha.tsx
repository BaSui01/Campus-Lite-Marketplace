/**
 * 智能验证码组件 - 自动随机选择验证码类型！🎲🔐
 * @author BaSui 😎
 * @date 2025-11-10
 * @description 
 *   在登录/注册页面使用，随机选择不同验证码类型
 *   目前支持：图形验证码、滑块验证码
 *   未来支持：旋转验证码、点选验证码
 */

import React, { useState, useEffect, useMemo } from 'react';
import { ImageCaptcha } from '../ImageCaptcha/ImageCaptcha';
import { RotateCaptcha } from '../RotateCaptcha/RotateCaptcha';
import { ClickCaptcha } from '../ClickCaptcha/ClickCaptcha';

export type CaptchaType = 'image' | 'rotate' | 'click';

export interface CaptchaResult {
  type: CaptchaType;
  captchaId?: string;
  captchaCode?: string;
  rotateId?: string;
  rotateAngle?: number;
  clickId?: string;
  clickPoints?: Array<{x: number; y: number}>;
}

export interface SmartCaptchaProps {
  /** 验证成功回调 */
  onSuccess?: (result: CaptchaResult) => void;
  /** 验证失败回调 */
  onFail?: () => void;
  /** 自定义类名 */
  className?: string;
  /** 重置标志 */
  reset?: boolean;
  /** 强制指定类型（不随机） */
  forceType?: CaptchaType;
}

export const SmartCaptcha: React.FC<SmartCaptchaProps> = ({
  onSuccess,
  onFail,
  className = '',
  reset = false,
  forceType,
}) => {
  // 随机选择验证码类型
  const selectedType = useMemo<CaptchaType>(() => {
    if (forceType) {
      return forceType;
    }
    // 随机选择验证码类型
    const types: CaptchaType[] = ['image', 'rotate', 'click'];
    return types[Math.floor(Math.random() * types.length)];
  }, [forceType]);

  const [currentType] = useState<CaptchaType>(selectedType);

  // 图形验证码成功回调
  const handleImageSuccess = (captchaId: string, code: string) => {
    console.log('✅ [SmartCaptcha] 图形验证码成功:', { captchaId, code });
    onSuccess?.({
      type: 'image',
      captchaId,
      captchaCode: code,
    });
  };

  // 旋转验证码成功回调
  const handleRotateSuccess = (rotateId: string, angle: number) => {
    console.log('✅ [SmartCaptcha] 旋转验证码成功:', { rotateId, angle });
    onSuccess?.({
      type: 'rotate',
      rotateId,
      rotateAngle: angle,
    });
  };

  // 点选验证码成功回调
  const handleClickSuccess = (clickId: string, points: Array<{x: number; y: number}>) => {
    console.log('✅ [SmartCaptcha] 点选验证码成功:', { clickId, points });
    onSuccess?.({
      type: 'click',
      clickId,
      clickPoints: points,
    });
  };

  return (
    <div className={`smart-captcha ${className}`}>
      {/* 图形验证码 */}
      {currentType === 'image' && (
        <ImageCaptcha
          onSuccess={handleImageSuccess}
          onFail={onFail}
          reset={reset}
        />
      )}

      {/* 旋转验证码 */}
      {currentType === 'rotate' && (
        <RotateCaptcha
          onSuccess={handleRotateSuccess}
          onFail={onFail}
        />
      )}

      {/* 点选验证码 */}
      {currentType === 'click' && (
        <ClickCaptcha
          onSuccess={handleClickSuccess}
          onFail={onFail}
        />
      )}
    </div>
  );
};

export default SmartCaptcha;
