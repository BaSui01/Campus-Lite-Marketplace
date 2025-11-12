/**
 * 2FA 验证组件 - 登录时输入 6 位验证码
 * @author BaSui 😎
 * @date 2025-11-09
 */

import React, { useState, useRef, useEffect } from 'react';
import { Button } from '@campus/shared/components';
import './TwoFactorVerify.css';

interface TwoFactorVerifyProps {
  onVerify: (code: string) => void;
  onCancel?: () => void;
  loading?: boolean;
  error?: string;
}

export const TwoFactorVerify: React.FC<TwoFactorVerifyProps> = ({
  onVerify,
  onCancel,
  loading = false,
  error = '',
}) => {
  const [code, setCode] = useState(['', '', '', '', '', '']);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  // 自动聚焦第一个输入框
  useEffect(() => {
    inputRefs.current[0]?.focus();
  }, []);

  // 处理输入
  const handleInput = (index: number, value: string) => {
    // 只允许输入数字
    if (value && !/^\d$/.test(value)) {
      return;
    }

    const newCode = [...code];
    newCode[index] = value;
    setCode(newCode);

    // 自动跳转到下一个输入框
    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }

    // 如果输入完成，自动提交
    if (index === 5 && value) {
      const fullCode = newCode.join('');
      if (fullCode.length === 6) {
        onVerify(fullCode);
      }
    }
  };

  // 处理键盘事件
  const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace') {
      if (!code[index] && index > 0) {
        // 如果当前输入框为空，删除前一个输入框的内容
        const newCode = [...code];
        newCode[index - 1] = '';
        setCode(newCode);
        inputRefs.current[index - 1]?.focus();
      } else {
        // 删除当前输入框的内容
        const newCode = [...code];
        newCode[index] = '';
        setCode(newCode);
      }
    } else if (e.key === 'ArrowLeft' && index > 0) {
      inputRefs.current[index - 1]?.focus();
    } else if (e.key === 'ArrowRight' && index < 5) {
      inputRefs.current[index + 1]?.focus();
    } else if (e.key === 'Enter') {
      const fullCode = code.join('');
      if (fullCode.length === 6) {
        onVerify(fullCode);
      }
    }
  };

  // 处理粘贴
  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text').trim();

    // 只允许粘贴 6 位数字
    if (/^\d{6}$/.test(pastedData)) {
      const newCode = pastedData.split('');
      setCode(newCode);
      inputRefs.current[5]?.focus();

      // 自动提交
      onVerify(pastedData);
    }
  };

  // 手动提交
  const handleSubmit = () => {
    const fullCode = code.join('');
    if (fullCode.length === 6) {
      onVerify(fullCode);
    }
  };

  return (
    <div className="two-factor-verify">
      <div className="two-factor-verify__header">
        <h3 className="two-factor-verify__title">🔐 双因素认证</h3>
        <p className="two-factor-verify__desc">
          请输入 Google Authenticator 中显示的 6 位验证码
        </p>
      </div>

      {/* 错误提示 */}
      {error && (
        <div className="two-factor-verify__error">
          ⚠️ {error}
        </div>
      )}

      {/* 验证码输入框 */}
      <div className="two-factor-verify__inputs">
        {code.map((digit, index) => (
          <input
            key={index}
            ref={(el) => (inputRefs.current[index] = el)}
            type="text"
            inputMode="numeric"
            maxLength={1}
            value={digit}
            onChange={(e) => handleInput(index, e.target.value)}
            onKeyDown={(e) => handleKeyDown(index, e)}
            onPaste={index === 0 ? handlePaste : undefined}
            className="two-factor-verify__input"
            disabled={loading}
          />
        ))}
      </div>

      {/* 提示信息 */}
      <div className="two-factor-verify__hint">
        <p>💡 提示：您也可以使用恢复码登录</p>
      </div>

      {/* 操作按钮 */}
      <div className="two-factor-verify__actions">
        <Button
          type="primary"
          size="large"
          onClick={handleSubmit}
          loading={loading}
          disabled={code.join('').length !== 6}
          block
        >
          验证
        </Button>
        {onCancel && (
          <Button
            type="default"
            size="large"
            onClick={onCancel}
            disabled={loading}
            block
          >
            取消
          </Button>
        )}
      </div>
    </div>
  );
};

export default TwoFactorVerify;
