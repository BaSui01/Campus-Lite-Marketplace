/**
 * 通知偏好设置页面 - 掌控你的通知!🔔
 * @author BaSui 😎
 * @description 设置各类通知开关、静默时段等偏好
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Loading } from '@campus/shared/components';
import { getApi } from '@campus/shared/utils';
import { useNotificationStore } from '../../store';
import './NotificationSettings.css';

// ==================== 类型定义 ====================

interface NotificationPreference {
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
const NotificationSettings: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();
  const api = getApi();

  // ==================== 状态管理 ====================

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [preferences, setPreferences] = useState<NotificationPreference>({
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
      const response = await api.status1();

      if (response.data.success && response.data.data) {
        const data = response.data.data;
        
        // 映射后端数据到前端格式
        setPreferences({
          system: true, // 默认开启，前端控制
          order: true,
          social: true,
          priceAlert: true,
          followUpdate: true,
          email: data.emailEnabled || false,
          webPush: data.webpushEnabled || false,
        });
      }
    } catch (err: any) {
      console.error('加载通知偏好失败:', err);
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
  const handleToggle = (key: keyof NotificationPreference) => {
    setPreferences((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
  };

  /**
   * 保存设置
   */
  const handleSave = async () => {
    setSaving(true);

    try {
      // 🚀 调用真实后端 API 保存通知偏好
      // 保存邮件通知开关
      await api.setChannelEnabled({
        channel: 'EMAIL',
        enabled: preferences.email,
      });

      // 保存 Web 推送开关
      await api.setChannelEnabled1({
        channel: 'WEB_PUSH',
        enabled: preferences.webPush,
      });

      toast.success('通知偏好保存成功！✅');
      
      // 可选：返回上一页
      // navigate(-1);
    } catch (err: any) {
      console.error('保存通知偏好失败:', err);
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

    setPreferences({
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
                  checked={preferences.system}
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

export default NotificationSettings;
