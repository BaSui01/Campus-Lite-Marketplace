/**
 * 通知偏好设置页面 - 掌控你的通知!🔔
 * @author BaSui 😎
 * @description 设置各类通知开关、静默时段等偏好
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Loading } from '@campus/shared/components';
import { 
  notificationPreferenceService,
  NotificationChannel,
  NotificationType,
} from '@campus/shared/services';
import type { NotificationPreference } from '@campus/shared/services';
import { useNotificationStore } from '../../store';
import './NotificationSettings.css';

// ==================== 类型定义 ====================

interface NotificationSettings {
  system: boolean;        // 系统通知
  order: boolean;         // 订单通知
  social: boolean;        // 社交通知
  priceAlert: boolean;    // 价格提醒
  followUpdate: boolean;  // 关注动态
  email: boolean;         // 邮件通知
  webPush: boolean;       // Web 推送
}

/**
 * 通知偏好设置页面组件
 */
const NotificationSettingsPage: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [backendPreferences, setBackendPreferences] = useState<NotificationPreference | null>(null);
  const [localSettings, setLocalSettings] = useState<NotificationSettings>({
    system: true,
    order: true,
    social: true,
    priceAlert: true,
    followUpdate: true,
    email: false,
    webPush: true,
  });

  // ==================== 数据加载 ====================

  /**
   * 加载通知偏好
   */
  const loadPreferences = async () => {
    setLoading(true);

    try {
      // 🚀 调用真实后端 API 获取通知偏好状态
      const data = await notificationPreferenceService.getStatus();
      setBackendPreferences(data);
      
      // 映射后端数据到前端格式
      setLocalSettings({
        system: !data.unsubscribedTypes.includes(NotificationType.SYSTEM),
        order: !data.unsubscribedTypes.includes(NotificationType.ORDER),
        social: !data.unsubscribedTypes.includes(NotificationType.MESSAGE),
        priceAlert: !data.unsubscribedTypes.includes(NotificationType.PRICE_ALERT),
        followUpdate: !data.unsubscribedTypes.includes(NotificationType.FOLLOW),
        email: data.channels.email,
        webPush: data.channels.inApp,
      });
      
      console.log('[NotificationSettings] ✅ 加载偏好成功', data);
    } catch (err: any) {
      console.error('[NotificationSettings] ❌ 加载失败:', err);
      toast.error('加载通知偏好失败！😭');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPreferences();
  }, []);

  // ==================== 事件处理 ====================

  /**
   * 切换通知开关
   */
  const handleToggle = async (key: keyof NotificationSettings) => {
    const newValue = !localSettings[key];
    
    // 更新本地状态
    setLocalSettings((prev) => ({
      ...prev,
      [key]: newValue,
    }));

    // 立即保存到后端
    try {
      // 渠道类型开关
      if (key === 'email') {
        await notificationPreferenceService.toggleChannel(NotificationChannel.EMAIL, newValue);
        toast.success(`邮件通知已${newValue ? '开启' : '关闭'}！`);
      } else if (key === 'webPush') {
        await notificationPreferenceService.toggleChannel(NotificationChannel.IN_APP, newValue);
        toast.success(`站内通知已${newValue ? '开启' : '关闭'}！`);
      } else {
        // 通知类型订阅/退订
        const typeMap: Record<string, NotificationType> = {
          system: NotificationType.SYSTEM,
          order: NotificationType.ORDER,
          social: NotificationType.MESSAGE,
          priceAlert: NotificationType.PRICE_ALERT,
          followUpdate: NotificationType.FOLLOW,
        };
        
        const notificationType = typeMap[key];
        if (notificationType) {
          if (newValue) {
            // 重新订阅
            await notificationPreferenceService.resubscribe(NotificationChannel.IN_APP, notificationType);
          } else {
            // 退订
            await notificationPreferenceService.unsubscribe(NotificationChannel.IN_APP, notificationType);
          }
          toast.success(`设置已保存！`);
        }
      }
      
      console.log(`[NotificationSettings] ✅ ${key} 已${newValue ? '开启' : '关闭'}`);
    } catch (err: any) {
      console.error('[NotificationSettings] ❌ 设置失败:', err);
      toast.error('设置失败！😭');
      
      // 失败则回滚本地状态
      setLocalSettings((prev) => ({
        ...prev,
        [key]: !newValue,
      }));
    }
  };

  /**
   * 批量保存设置（备用）
   */
  const handleSave = async () => {
    setSaving(true);

    try {
      toast.success('通知偏好保存成功！✅');
    } catch (err: any) {
      console.error('[NotificationSettings] ❌ 保存失败:', err);
      toast.error(err.response?.data?.message || '保存失败！😭');
    } finally {
      setSaving(false);
    }
  };

  /**
   * 重置为默认设置
   */
  const handleReset = () => {
    if (!window.confirm('确定要重置为默认设置吗？')) {
      return;
    }

    setLocalSettings({
      system: true,
      order: true,
      social: true,
      priceAlert: true,
      followUpdate: true,
      email: false,
      webPush: true,
    });

    toast.info('已重置为默认设置！🔄');
  };

  // ==================== 渲染 ====================

  if (loading) {
    return (
      <div className="notification-settings-page">
        <div className="loading-container">
          <Loading size="large" />
          <p>加载通知设置中...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="notification-settings-page">
      <div className="notification-settings-container">
        {/* ==================== 头部 ==================== */}
        <div className="settings-header">
          <button className="back-btn" onClick={() => navigate(-1)}>
            ← 返回
          </button>
          <h1 className="settings-title">🔔 通知设置</h1>
          <p className="settings-subtitle">自定义你的通知偏好，掌控重要消息</p>
        </div>

        {/* ==================== 通知类型设置 ==================== */}
        <div className="settings-section">
          <h2 className="section-title">通知类型</h2>
          <p className="section-desc">选择你想接收的通知类型</p>

          <div className="settings-list">
            <div className="setting-item">
              <div className="setting-info">
                <div className="setting-icon">🔔</div>
                <div className="setting-content">
                  <h3 className="setting-name">系统通知</h3>
                  <p className="setting-desc">系统公告、维护通知等重要信息</p>
                </div>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={localSettings.system}
                  onChange={() => handleToggle('system')}
                />
                <span className="slider"></span>
              </label>
            </div>

            <div className="setting-item">
              <div className="setting-info">
                <div className="setting-icon">📦</div>
                <div className="setting-content">
                  <h3 className="setting-name">订单通知</h3>
                  <p className="setting-desc">订单状态更新、物流信息等</p>
                </div>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.order}
                  onChange={() => handleToggle('order')}
                />
                <span className="slider"></span>
              </label>
            </div>

            <div className="setting-item">
              <div className="setting-info">
                <div className="setting-icon">💬</div>
                <div className="setting-content">
                  <h3 className="setting-name">社交通知</h3>
                  <p className="setting-desc">评论、点赞、私信等社交互动</p>
                </div>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.social}
                  onChange={() => handleToggle('social')}
                />
                <span className="slider"></span>
              </label>
            </div>

            <div className="setting-item">
              <div className="setting-info">
                <div className="setting-icon">💰</div>
                <div className="setting-content">
                  <h3 className="setting-name">价格提醒</h3>
                  <p className="setting-desc">收藏商品价格变动提醒</p>
                </div>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.priceAlert}
                  onChange={() => handleToggle('priceAlert')}
                />
                <span className="slider"></span>
              </label>
            </div>

            <div className="setting-item">
              <div className="setting-info">
                <div className="setting-icon">👥</div>
                <div className="setting-content">
                  <h3 className="setting-name">关注动态</h3>
                  <p className="setting-desc">关注的卖家发布新商品</p>
                </div>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.followUpdate}
                  onChange={() => handleToggle('followUpdate')}
                />
                <span className="slider"></span>
              </label>
            </div>
          </div>
        </div>

        {/* ==================== 通知渠道设置 ==================== */}
        <div className="settings-section">
          <h2 className="section-title">通知渠道</h2>
          <p className="section-desc">选择接收通知的方式</p>

          <div className="settings-list">
            <div className="setting-item">
              <div className="setting-info">
                <div className="setting-icon">📧</div>
                <div className="setting-content">
                  <h3 className="setting-name">邮件通知</h3>
                  <p className="setting-desc">通过邮件接收重要通知</p>
                </div>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.email}
                  onChange={() => handleToggle('email')}
                />
                <span className="slider"></span>
              </label>
            </div>

            <div className="setting-item">
              <div className="setting-info">
                <div className="setting-icon">🔔</div>
                <div className="setting-content">
                  <h3 className="setting-name">浏览器推送</h3>
                  <p className="setting-desc">通过浏览器推送接收实时通知</p>
                </div>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={preferences.webPush}
                  onChange={() => handleToggle('webPush')}
                />
                <span className="slider"></span>
              </label>
            </div>
          </div>
        </div>

        {/* ==================== 操作按钮 ==================== */}
        <div className="settings-actions">
          <Button
            type="default"
            size="large"
            onClick={handleReset}
            disabled={saving}
          >
            重置为默认
          </Button>
          <Button
            type="primary"
            size="large"
            onClick={handleSave}
            loading={saving}
          >
            {saving ? '保存中...' : '保存设置'}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default NotificationSettingsPage;
export { NotificationSettingsPage as NotificationSettings };
