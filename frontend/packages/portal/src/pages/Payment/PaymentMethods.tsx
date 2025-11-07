/**
 * 支付方式管理页面
 * @author BaSui 😎
 * @description 管理用户的支付方式选择和使用统计
 */

import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { PaymentMethodCard } from './components/PaymentMethodCard';

interface PaymentMethod {
  id: string;
  name: string;
  icon: string;
  description: string;
  isDefault: boolean;
  usageCount: number;
  isEnabled: boolean;
}

const PaymentMethods: React.FC = () => {
  const [methods, setMethods] = useState<PaymentMethod[]>([]);
  const [updating, setUpdating] = useState(false);

  // 加载支付方式
  useEffect(() => {
    const loadPaymentMethods = () => {
      // 模拟数据，实际应该从API获取
      const defaultMethod = localStorage.getItem('defaultPaymentMethod') || 'WECHAT';
      const wechatUsageCount = parseInt(localStorage.getItem('wechatUsageCount') || '0');
      const alipayUsageCount = parseInt(localStorage.getItem('alipayUsageCount') || '0');

      const defaultMethods: PaymentMethod[] = [
        {
          id: 'WECHAT',
          name: '微信支付',
          icon: '💚',
          description: '推荐使用，安全便捷的支付方式',
          isDefault: defaultMethod === 'WECHAT',
          usageCount: wechatUsageCount,
          isEnabled: true,
        },
        {
          id: 'ALIPAY',
          name: '支付宝',
          icon: '💙',
          description: '支付宝支付，快速到账有保障',
          isDefault: defaultMethod === 'ALIPAY',
          usageCount: alipayUsageCount,
          isEnabled: true,
        },
        // 后续可扩展更多支付方式
        // {
        //   id: 'UNIONPAY',
        //   name: '银联支付',
        //   icon: '💳',
        //   description: '银联卡支付',
        //   isDefault: false,
        //   usageCount: 0,
        //   isEnabled: false, // 暂未开放
        // },
      ];

      setMethods(defaultMethods);
    };

    loadPaymentMethods();
  }, []);

  // 设置默认支付方式
  const handleSetDefault = async (methodId: string) => {
    setUpdating(true);
    try {
      // 乐观更新UI
      setMethods(prev => prev.map(method => ({
        ...method,
        isDefault: method.id === methodId
      })));

      // 保存到本地存储
      localStorage.setItem('defaultPaymentMethod', methodId);

      // 更新使用统计
      const usageKey = `${methodId.toLowerCase()}UsageCount`;
      const currentCount = parseInt(localStorage.getItem(usageKey) || '0');
      localStorage.setItem(usageKey, String(currentCount + 1));

      toast.success('默认支付方式已更新');
    } catch (error) {
      toast.error('更新失败，请重试');
    } finally {
      setUpdating(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            支付方式管理
          </h1>
          <p className="text-gray-600">
            选择您偏好的支付方式，让付款更便捷
          </p>
        </div>

        {/* 支付方式列表 */}
        <div className="grid gap-4 md:grid-cols-2 mb-8">
          {methods.map(method => (
            <PaymentMethodCard
              key={method.id}
              method={method}
              onSetDefault={handleSetDefault}
              disabled={updating}
            />
          ))}
        </div>

        {/* 使用统计 */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-8">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">
            💡 使用统计
          </h3>
          <div className="space-y-4">
            {methods
              .filter(method => method.isEnabled && method.usageCount > 0)
              .sort((a, b) => b.usageCount - a.usageCount)
              .map(method => (
                <div key={method.id} className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <span className="text-xl">{method.icon}</span>
                    <span className="font-medium">{method.name}</span>
                  </div>
                  <div className="flex items-center space-x-4">
                    <div className="text-right">
                      <div className="text-sm text-gray-500">使用次数</div>
                      <div className="font-semibold">{method.usageCount}</div>
                    </div>
                    <div className="w-24 bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-blue-500 h-2 rounded-full"
                        style={{
                          width: `${Math.min(100, (method.usageCount / Math.max(...methods.map(m => m.usageCount))) * 100)}%`
                        }}
                      />
                    </div>
                  </div>
                </div>
              ))}
          </div>
        </div>

        {/* 安全提示 */}
        <div className="bg-blue-50 rounded-lg p-6 mb-8">
          <h3 className="text-lg font-semibold text-blue-900 mb-3">
            💎 安全提示
          </h3>
          <div className="grid md:grid-cols-2 gap-6">
            <div>
              <h4 className="font-semibold text-blue-800 mb-2">安全保障</h4>
              <ul className="space-y-1 text-blue-700 text-sm">
                <li>• 所有支付信息均经过加密处理</li>
                <li>• 支持人脸识别和指纹验证</li>
                <li>• 异常交易实时监控提醒</li>
                <li>• 账户资金保险保障</li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold text-blue-800 mb-2">使用建议</h4>
              <ul className="space-y-1 text-blue-700 text-sm">
                <li>• 定期检查支付方式安全设置</li>
                <li>• 开启支付验证通知功能</li>
                <li>• 支付时仔细核对收款方信息</li>
                <li>• 发现异常立即联系客服</li>
              </ul>
            </div>
          </div>
        </div>

        {/* 即将上线 */}
        <div className="bg-gray-100 rounded-lg p-6 mb-8">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">
            🚀 即将上线
          </h3>
          <div className="grid md:grid-cols-3 gap-4">
            <div className="text-center p-4 bg-white rounded-lg">
              <div className="text-2xl mb-2">💳</div>
              <h4 className="font-semibold text-gray-900">银联支付</h4>
              <p className="text-sm text-gray-600 mt-1">银行卡直接支付</p>
            </div>
            <div className="text-center p-4 bg-white rounded-lg">
              <div className="text-2xl mb-2">🍎</div>
              <h4 className="font-semibold text-gray-900">Apple Pay</h4>
              <p className="text-sm text-gray-600 mt-1">苹果设备专属支付</p>
            </div>
            <div className="text-center p-4 bg-white rounded-lg">
              <div className="text-2xl mb-2">📱</div>
              <h4 className="font-semibold text-gray-900">数字钱包</h4>
              <p className="text-sm text-gray-600 mt-1">更多钱包选择</p>
            </div>
          </div>
        </div>

        {/* 帮助中心 */}
        <div className="text-center">
          <p className="text-gray-600 mb-4">
            遇到支付问题？我们随时为您提供帮助
          </p>
          <div className="space-x-4">
            <button
              onClick={() => window.open('/customer-service', '_blank')}
              className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              💬 联系客服
            </button>
            <button
              onClick={() => window.open('/help/payment', '_blank')}
              className="bg-gray-200 text-gray-700 px-6 py-2 rounded-lg hover:bg-gray-300 transition-colors"
            >
              📖 支付帮助
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentMethods;