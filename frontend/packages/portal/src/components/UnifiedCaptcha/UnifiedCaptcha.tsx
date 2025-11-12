/**
 * 统一验证码组件 - 随机展示四种人机验证！🎲🔒
 * @author BaSui 😎
 * @date 2025-11-11
 * @description
 *   在登录/注册页面使用，随机选择四种验证码之一：
 *   1. 图形验证码（ImageCaptcha）- 输入4位字符
 *   2. 滑块验证码（SliderCaptcha）- 拖动拼图到正确位置
 *   3. 旋转验证码（RotateCaptcha）- 旋转图片到正确角度
 *   4. 点选验证码（ClickCaptcha）- 按顺序点击指定文字
 */

import React, { useState, useMemo } from 'react';
import { ImageCaptcha, RotateCaptcha, ClickCaptcha } from '@campus/shared/components';
import { SliderCaptcha } from '../SliderCaptcha/SliderCaptcha';

export type CaptchaType = 'image' | 'slider' | 'rotate' | 'click';

export interface CaptchaResult {
  type: CaptchaType;
  captchaId?: string;
  captchaCode?: string;
  slideId?: string;
  slidePosition?: number;
  rotateId?: string;
  rotateAngle?: number;
  clickId?: string;
  clickPoints?: Array<{ x: number; y: number }>;
}

export interface UnifiedCaptchaProps {
  onSuccess?: (result: CaptchaResult) => void;
  onFail?: () => void;
  className?: string;
  reset?: boolean;
  forceType?: CaptchaType;
  /** 验证状态（由父组件控制） */
  verifyStatus?: 'idle' | 'verifying' | 'success' | 'error';
}

export const UnifiedCaptcha: React.FC<UnifiedCaptchaProps> = ({
  onSuccess,
  onFail,
  className = '',
  reset = false,
  forceType,
  verifyStatus = 'idle',
}) => {
  const selectedType = useMemo<CaptchaType>(() => {
    if (forceType) {
      return forceType;
    }
    const types: CaptchaType[] = ['image', 'slider', 'rotate', 'click'];
    return types[Math.floor(Math.random() * types.length)];
  }, [forceType]);

  const [currentType] = useState<CaptchaType>(selectedType);

  const handleImageSuccess = (captchaId: string, code: string) => {
    console.log('✅ [UnifiedCaptcha] 图形验证成功:', { captchaId, code });
    onSuccess?.({ type: 'image', captchaId, captchaCode: code });
  };

  const handleSliderSuccess = (slideId: string, position: number) => {
    console.log('✅ [UnifiedCaptcha] 滑块验证成功:', { slideId, position });
    onSuccess?.({ type: 'slider', slideId, slidePosition: position });
  };

  const handleRotateSuccess = (rotateId: string, angle: number) => {
    console.log('✅ [UnifiedCaptcha] 旋转验证成功:', { rotateId, angle });
    onSuccess?.({ type: 'rotate', rotateId, rotateAngle: angle });
  };

  const handleClickSuccess = (clickId: string, points: Array<{ x: number; y: number }>) => {
    console.log('✅ [UnifiedCaptcha] 点选验证成功:', { clickId, points });
    onSuccess?.({ type: 'click', clickId, clickPoints: points });
  };

  // 🎨 根据验证状态显示不同的边框颜色和提示
  const getStatusStyle = () => {
    switch (verifyStatus) {
      case 'verifying':
        return {
          border: '2px solid #1890ff',
          backgroundColor: '#e6f7ff',
        };
      case 'success':
        return {
          border: '2px solid #52c41a',
          backgroundColor: '#f6ffed',
        };
      case 'error':
        return {
          border: '2px solid #ff4d4f',
          backgroundColor: '#fff1f0',
        };
      default:
        return {
          border: '2px solid #d9d9d9',
          backgroundColor: '#ffffff',
        };
    }
  };

  const getStatusMessage = () => {
    switch (verifyStatus) {
      case 'verifying':
        return (
          <div style={{
            marginTop: '10px',
            padding: '8px 12px',
            borderRadius: '4px',
            backgroundColor: '#e6f7ff',
            color: '#1890ff',
            fontSize: '14px',
            textAlign: 'center',
          }}>
            🔄 验证中...
          </div>
        );
      case 'success':
        return (
          <div style={{
            marginTop: '10px',
            padding: '8px 12px',
            borderRadius: '4px',
            backgroundColor: '#f6ffed',
            color: '#52c41a',
            fontSize: '14px',
            textAlign: 'center',
            fontWeight: 'bold',
          }}>
            ✅ 验证成功！
          </div>
        );
      case 'error':
        return (
          <div style={{
            marginTop: '10px',
            padding: '8px 12px',
            borderRadius: '4px',
            backgroundColor: '#fff1f0',
            color: '#ff4d4f',
            fontSize: '14px',
            textAlign: 'center',
            fontWeight: 'bold',
          }}>
            ❌ 验证失败，请重试
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className={`unified-captcha ${className}`}>
      <div style={{
        ...getStatusStyle(),
        borderRadius: '8px',
        padding: '15px',
        transition: 'all 0.3s ease',
      }}>
        {currentType === 'image' && (
          <ImageCaptcha onSuccess={handleImageSuccess} onFail={onFail} reset={reset} />
        )}
        {currentType === 'slider' && (
          <SliderCaptcha onSuccess={handleSliderSuccess} onFail={onFail} reset={reset} />
        )}
        {currentType === 'rotate' && (
          <RotateCaptcha onSuccess={handleRotateSuccess} onFail={onFail} />
        )}
        {currentType === 'click' && (
          <ClickCaptcha onSuccess={handleClickSuccess} onFail={onFail} />
        )}
      </div>
      {getStatusMessage()}
    </div>
  );
};
