/**
 * 设置页面 - 个性化你的账户！⚙️
 * @author BaSui 😎
 * @description 账户设置、隐私设置、通知设置
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input, Button, Tabs } from '@campus/shared/components';
import { useAuthStore, useNotificationStore } from '../../store';
import { authService } from '@campus/shared/services';
import { encryptPassword } from '@campus/shared/utils';
import './Settings.css';

/**
 * 设置页面组件
 */
const Settings: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();
  const currentUser = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);

  // ==================== 状态管理 ====================

  const [activeTab, setActiveTab] = useState('account');

  // 账户设置
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [changingPassword, setChangingPassword] = useState(false);

  // 隐私设置
  const [profilePrivacy, setProfilePrivacy] = useState<'public' | 'friends' | 'private'>('public');
  const [phoneVisibility, setPhoneVisibility] = useState(true);
  const [emailVisibility, setEmailVisibility] = useState(true);
  const [savingPrivacy, setSavingPrivacy] = useState(false);

  // 通知设置
  const [orderNotification, setOrderNotification] = useState(true);
  const [messageNotification, setMessageNotification] = useState(true);
  const [likeNotification, setLikeNotification] = useState(true);
  const [commentNotification, setCommentNotification] = useState(true);
  const [savingNotification, setSavingNotification] = useState(false);

  // ==================== 事件处理 ====================

  /**
   * 修改密码
   */
  const handleChangePassword = async () => {
    // 验证表单
    if (!oldPassword.trim()) {
      toast.warning('请输入旧密码！😰');
      return;
    }

    if (!newPassword.trim()) {
      toast.warning('请输入新密码！😰');
      return;
    }

    if (newPassword.length < 6) {
      toast.warning('新密码至少 6 位！😰');
      return;
    }

    if (newPassword !== confirmPassword) {
      toast.warning('两次输入的密码不一致！😰');
      return;
    }

    setChangingPassword(true);

    try {
      // 🔐 加密旧密码和新密码（防止明文传输）
      let encryptedOldPassword: string;
      let encryptedNewPassword: string;
      
      try {
        encryptedOldPassword = encryptPassword(oldPassword);
        encryptedNewPassword = encryptPassword(newPassword);
        console.log('[Settings] 🔐 密码已加密');
      } catch (error) {
        console.error('[Settings] ❌ 密码加密失败:', error);
        toast.error('密码加密失败，请重试！😭');
        setChangingPassword(false);
        return;
      }

      // 🚀 调用真实后端 API 修改密码
      await authService.updatePassword({
        oldPassword: encryptedOldPassword,
        newPassword: encryptedNewPassword,
      });

      toast.success('密码修改成功！请重新登录。🎉');

      // 清空表单
      setOldPassword('');
      setNewPassword('');
      setConfirmPassword('');

      // 登出并跳转到登录页
      setTimeout(() => {
        logout();
        navigate('/login');
      }, 1500);
    } catch (err: any) {
      console.error('修改密码失败：', err);
      toast.error(err.response?.data?.message || '修改密码失败！😭');
    } finally {
      setChangingPassword(false);
    }
  };

  /**
   * 保存隐私设置
   */
  const handleSavePrivacy = async () => {
    setSavingPrivacy(true);

    try {
      // 🚀 调用真实后端 API 保存隐私设置
      // TODO: 集成真实 API
      // await userService.updatePrivacySettings({
      //   profilePrivacy,
      //   phoneVisibility,
      //   emailVisibility,
      // });

      toast.success('隐私设置已保存！🎉');
    } catch (err: any) {
      console.error('保存隐私设置失败：', err);
      toast.error(err.response?.data?.message || '保存失败！😭');
    } finally {
      setSavingPrivacy(false);
    }
  };

  /**
   * 保存通知设置
   */
  const handleSaveNotification = async () => {
    setSavingNotification(true);

    try {
      // 🚀 调用真实后端 API 保存通知设置
      // TODO: 集成真实 API
      // await userService.updateNotificationSettings({
      //   orderNotification,
      //   messageNotification,
      //   likeNotification,
      //   commentNotification,
      // });

      toast.success('通知设置已保存！🎉');
    } catch (err: any) {
      console.error('保存通知设置失败：', err);
      toast.error(err.response?.data?.message || '保存失败！😭');
    } finally {
      setSavingNotification(false);
    }
  };

  /**
   * 退出登录
   */
  const handleLogout = () => {
    if (window.confirm('确定要退出登录吗？')) {
      logout();
      navigate('/login');
      toast.success('已退出登录！👋');
    }
  };

  // ==================== 渲染 ====================

  return (
    <div className="settings-page">
      <div className="settings-container">
        <h1 className="settings-title">⚙️ 设置</h1>

        {/* ==================== 标签切换 ==================== */}
        <div className="settings-tabs">
          <Tabs
            value={activeTab}
            onChange={setActiveTab}
            tabs={[
              { label: '🔐 账户设置', value: 'account' },
              { label: '🔒 隐私设置', value: 'privacy' },
              { label: '🔔 通知设置', value: 'notification' },
              { label: '🚫 黑名单', value: 'blacklist' },
            ]}
          />
        </div>

        {/* ==================== 账户设置 ==================== */}
        {activeTab === 'account' && (
          <div className="settings-content">
            {/* 基本信息 */}
            <div className="settings-section">
              <h2 className="settings-section__title">基本信息</h2>
              <div className="settings-section__content">
                <div className="settings-item">
                  <div className="settings-item__label">用户名</div>
                  <div className="settings-item__value">{currentUser?.username || '未设置'}</div>
                </div>
                <div className="settings-item">
                  <div className="settings-item__label">邮箱</div>
                  <div className="settings-item__value">{currentUser?.email || '未绑定'}</div>
                </div>
                <div className="settings-item">
                  <div className="settings-item__label">手机号</div>
                  <div className="settings-item__value">{currentUser?.phone || '未绑定'}</div>
                </div>
              </div>
            </div>

            {/* 修改密码 */}
            <div className="settings-section">
              <h2 className="settings-section__title">修改密码</h2>
              <div className="settings-section__content">
                <div className="settings-field">
                  <label className="settings-field__label">旧密码</label>
                  <Input
                    type="password"
                    size="large"
                    placeholder="请输入旧密码"
                    value={oldPassword}
                    onChange={(e) => setOldPassword(e.target.value)}
                    maxLength={20}
                  />
                </div>
                <div className="settings-field">
                  <label className="settings-field__label">新密码</label>
                  <Input
                    type="password"
                    size="large"
                    placeholder="请输入新密码（6-20位）"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    maxLength={20}
                  />
                </div>
                <div className="settings-field">
                  <label className="settings-field__label">确认密码</label>
                  <Input
                    type="password"
                    size="large"
                    placeholder="请再次输入新密码"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    maxLength={20}
                  />
                </div>
                <Button
                  type="primary"
                  size="large"
                  onClick={handleChangePassword}
                  loading={changingPassword}
                >
                  {changingPassword ? '修改中...' : '修改密码'}
                </Button>
              </div>
            </div>

            {/* 账户操作 */}
            <div className="settings-section">
              <h2 className="settings-section__title">账户操作</h2>
              <div className="settings-section__content">
                <Button type="danger" size="large" onClick={handleLogout}>
                  退出登录
                </Button>
              </div>
            </div>
          </div>
        )}

        {/* ==================== 隐私设置 ==================== */}
        {activeTab === 'privacy' && (
          <div className="settings-content">
            <div className="settings-section">
              <h2 className="settings-section__title">隐私设置</h2>
              <div className="settings-section__content">
                {/* 主页可见性 */}
                <div className="settings-item">
                  <div className="settings-item__label">
                    <div>主页可见性</div>
                    <div className="settings-item__desc">设置谁可以查看你的主页</div>
                  </div>
                  <select
                    className="settings-select"
                    value={profilePrivacy}
                    onChange={(e) => setProfilePrivacy(e.target.value as any)}
                  >
                    <option value="public">公开</option>
                    <option value="friends">仅好友</option>
                    <option value="private">私密</option>
                  </select>
                </div>

                {/* 手机号可见性 */}
                <div className="settings-item">
                  <div className="settings-item__label">
                    <div>手机号可见</div>
                    <div className="settings-item__desc">是否在主页显示手机号</div>
                  </div>
                  <label className="settings-switch">
                    <input
                      type="checkbox"
                      checked={phoneVisibility}
                      onChange={(e) => setPhoneVisibility(e.target.checked)}
                    />
                    <span className="settings-switch__slider"></span>
                  </label>
                </div>

                {/* 邮箱可见性 */}
                <div className="settings-item">
                  <div className="settings-item__label">
                    <div>邮箱可见</div>
                    <div className="settings-item__desc">是否在主页显示邮箱</div>
                  </div>
                  <label className="settings-switch">
                    <input
                      type="checkbox"
                      checked={emailVisibility}
                      onChange={(e) => setEmailVisibility(e.target.checked)}
                    />
                    <span className="settings-switch__slider"></span>
                  </label>
                </div>

                <Button
                  type="primary"
                  size="large"
                  onClick={handleSavePrivacy}
                  loading={savingPrivacy}
                >
                  {savingPrivacy ? '保存中...' : '保存设置'}
                </Button>
              </div>
            </div>
          </div>
        )}

        {/* ==================== 通知设置 ==================== */}
        {activeTab === 'notification' && (
          <div className="settings-content">
            {/* 高级通知设置入口 */}
            <div className="settings-section">
              <h2 className="settings-section__title">🔔 高级通知设置</h2>
              <div className="settings-section__content">
                <div className="settings-info-box">
                  <p className="info-text">
                    💡 您可以在高级设置中管理：通知渠道开关、免打扰时段、通知类型订阅等
                  </p>
                  <Button
                    type="primary"
                    size="large"
                    onClick={() => navigate('/settings/notifications')}
                  >
                    前往高级通知设置 →
                  </Button>
                </div>
              </div>
            </div>
            
            {/* 快捷通知开关 */}
            <div className="settings-section">
              <h2 className="settings-section__title">快捷通知开关</h2>
              <div className="settings-section__content">
                {/* 订单通知 */}
                <div className="settings-item">
                  <div className="settings-item__label">
                    <div>订单通知</div>
                    <div className="settings-item__desc">订单状态变更时通知</div>
                  </div>
                  <label className="settings-switch">
                    <input
                      type="checkbox"
                      checked={orderNotification}
                      onChange={(e) => setOrderNotification(e.target.checked)}
                    />
                    <span className="settings-switch__slider"></span>
                  </label>
                </div>

                {/* 消息通知 */}
                <div className="settings-item">
                  <div className="settings-item__label">
                    <div>消息通知</div>
                    <div className="settings-item__desc">收到新消息时通知</div>
                  </div>
                  <label className="settings-switch">
                    <input
                      type="checkbox"
                      checked={messageNotification}
                      onChange={(e) => setMessageNotification(e.target.checked)}
                    />
                    <span className="settings-switch__slider"></span>
                  </label>
                </div>

                {/* 点赞通知 */}
                <div className="settings-item">
                  <div className="settings-item__label">
                    <div>点赞通知</div>
                    <div className="settings-item__desc">有人点赞你的内容时通知</div>
                  </div>
                  <label className="settings-switch">
                    <input
                      type="checkbox"
                      checked={likeNotification}
                      onChange={(e) => setLikeNotification(e.target.checked)}
                    />
                    <span className="settings-switch__slider"></span>
                  </label>
                </div>

                {/* 评论通知 */}
                <div className="settings-item">
                  <div className="settings-item__label">
                    <div>评论通知</div>
                    <div className="settings-item__desc">有人评论你的内容时通知</div>
                  </div>
                  <label className="settings-switch">
                    <input
                      type="checkbox"
                      checked={commentNotification}
                      onChange={(e) => setCommentNotification(e.target.checked)}
                    />
                    <span className="settings-switch__slider"></span>
                  </label>
                </div>

                <Button
                  type="primary"
                  size="large"
                  onClick={handleSaveNotification}
                  loading={savingNotification}
                >
                  {savingNotification ? '保存中...' : '保存设置'}
                </Button>
              </div>
            </div>
          </div>
        )}

        {/* ==================== 黑名单设置 ==================== */}
        {activeTab === 'blacklist' && (
          <div className="settings-content">
            <div className="settings-section">
              <h2 className="settings-section__title">🚫 黑名单管理</h2>
              <div className="settings-section__content">
                <div className="settings-info-box">
                  <p className="info-text">
                    💡 黑名单功能可以帮助您屏蔽骚扰用户的消息和内容，打造清净的社交环境
                  </p>
                  <Button
                    type="primary"
                    size="large"
                    onClick={() => navigate('/settings/blacklist')}
                  >
                    前往黑名单管理 →
                  </Button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Settings;
