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
    console.log('🔍 [AlipayRedirect] 收到 paymentHtml:', paymentHtml ? '有内容' : '为空');
    console.log('🔍 [AlipayRedirect] paymentHtml 长度:', paymentHtml?.length || 0);
    console.log('🔍 [AlipayRedirect] paymentHtml 前100字符:', paymentHtml?.substring(0, 100) || '无');
    
    if (paymentHtml && formRef.current) {
      // 将HTML表单注入到容器中并自动提交
      formRef.current.innerHTML = paymentHtml;
      const form = formRef.current.querySelector('form');
      if (form) {
        console.log('✅ [AlipayRedirect] 找到表单，准备提交');
        console.log('🔍 [AlipayRedirect] 表单 action:', form.action);
        console.log('🔍 [AlipayRedirect] 表单 method:', form.method);
        
        // 🎯 改为同页提交：不新开标签，保持当前标签页完成支付与回跳
        //    这样用户支付完成后会根据 return_url 回到站内结果页
        form.target = '_self';
        
        setTimeout(() => {
          console.log('🚀 [AlipayRedirect] 在当前窗口提交表单...');
          form.submit();
        }, 100);
      } else {
        console.error('❌ [AlipayRedirect] 未找到表单！paymentHtml 内容可能有问题');
      }
    } else {
      console.error('❌ [AlipayRedirect] paymentHtml 为空或 formRef 未就绪');
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
          <li>支付宝将在当前页面打开</li>
          <li>请按提示完成支付操作</li>
          <li>支付完成后将自动回到结果页</li>
          <li>如未跳转，请稍候或重试</li>
        </ul>
      </div>
    </div>
  );
};
