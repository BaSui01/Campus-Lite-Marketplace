/**
 * 微信支付二维码组件 💚
 * @author BaSui 😎
 */

import React, { useState, useEffect } from 'react';
import QRCode from 'qrcode';
import './WechatPayQRCode.css';

interface Props {
  qrCodeUrl: string;
  expireSeconds: number;
  onCancel: () => void;
}

export const WechatPayQRCode: React.FC<Props> = ({
  qrCodeUrl,
  expireSeconds,
  onCancel,
}) => {
  const [qrCodeDataUrl, setQrCodeDataUrl] = useState<string>('');
  const [remainingTime, setRemainingTime] = useState(expireSeconds);

  // 生成二维码图片
  useEffect(() => {
    if (qrCodeUrl) {
      QRCode.toDataURL(qrCodeUrl, {
        width: 280,
        margin: 2,
        color: {
          dark: '#000000',
          light: '#FFFFFF',
        },
      })
        .then((url) => setQrCodeDataUrl(url))
        .catch((err) => console.error('生成二维码失败:', err));
    }
  }, [qrCodeUrl]);

  // 倒计时
  useEffect(() => {
    const timer = setInterval(() => {
      setRemainingTime((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  const formatTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div className="wechat-pay-qrcode">
      <div className="qrcode-header">
        <div className="wechat-icon">💚</div>
        <h3>微信支付</h3>
        <p>请使用微信扫描二维码完成支付</p>
      </div>

      <div className="qrcode-container">
        {qrCodeDataUrl ? (
          <img src={qrCodeDataUrl} alt="微信支付二维码" className="qrcode-image" />
        ) : (
          <div className="qrcode-loading">
            <div className="spinner"></div>
            <p>二维码生成中...</p>
          </div>
        )}
      </div>

      <div className="qrcode-info">
        <div className="info-item">
          <span className="label">有效时间：</span>
          <span className={`value ${remainingTime < 300 ? 'warning' : ''}`}>
            {formatTime(remainingTime)}
          </span>
        </div>
        {remainingTime === 0 && (
          <div className="expired-notice">二维码已过期，请重新生成</div>
        )}
      </div>

      <div className="qrcode-actions">
        <button className="btn-cancel-payment" onClick={onCancel}>
          取消支付
        </button>
      </div>

      <div className="qrcode-tips">
        <h4>支付步骤</h4>
        <ol>
          <li>打开微信 APP</li>
          <li>点击右上角"+"号，选择"扫一扫"</li>
          <li>扫描上方二维码</li>
          <li>确认支付金额并完成支付</li>
        </ol>
      </div>
    </div>
  );
};
