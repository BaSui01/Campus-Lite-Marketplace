/**
 * 支付宝跳转组件 💙
 * @author BaSui 😎
 */

import React, { useEffect, useRef } from 'react';
import './AlipayRedirect.css';

interface Props {
  paymentHtml: string;
  onCancel: () => void;
}

export const AlipayRedirect: React.FC<Props> = ({ paymentHtml, onCancel }) => {
  const formRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (paymentHtml && formRef.current) {
      // 将HTML表单注入到容器中并自动提交
      formRef.current.innerHTML = paymentHtml;
      const form = formRef.current.querySelector('form');
      if (form) {
        setTimeout(() => form.submit(), 100);
      }
    }
  }, [paymentHtml]);

  return (
    <div className="alipay-redirect">
      <div className="redirect-header">
        <div className="alipay-icon">💙</div>
        <h3>支付宝支付</h3>
        <p>正在跳转到支付宝支付页面...</p>
      </div>

      <div className="redirect-loading">
        <div className="spinner"></div>
        <p>请稍候...</p>
      </div>

      <div ref={formRef} style={{ display: 'none' }} />

      <div className="redirect-actions">
        <button className="btn-cancel-payment" onClick={onCancel}>
          取消支付
        </button>
      </div>

      <div className="redirect-tips">
        <h4>支付提示</h4>
        <ul>
          <li>页面即将自动跳转到支付宝</li>
          <li>请在支付宝完成支付操作</li>
          <li>支付完成后会自动返回</li>
          <li>如未自动跳转，请点击取消后重试</li>
        </ul>
      </div>
    </div>
  );
};
