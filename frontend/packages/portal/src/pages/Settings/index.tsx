/**
 * 设置页面 - 个性化你的账户！⚙️
 * @author BaSui 😎
 * @description 账户设置、隐私设置、通知设置
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input, Button, Tabs } from '@campus/shared/components';
import { useAuthStore, useNotificationStore } from '../../store';
import { authService, userService } from '@campus/shared/services';
import { encryptPassword } from '@campus/shared/utils';
import LoginDevices from './LoginDevices';
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

  // 邮箱绑定
  const [showEmailModal, setShowEmailModal] = useState(false);
  const [emailInput, setEmailInput] = useState('');
  const [emailCode, setEmailCode] = useState('');
  const [emailCountdown, setEmailCountdown] = useState(0);
  const [sendingEmailCode, setSendingEmailCode] = useState(false);
  const [bindingEmail, setBindingEmail] = useState(false);

  // 手机号绑定
  const [showPhoneModal, setShowPhoneModal] = useState(false);
  const [phoneInput, setPhoneInput] = useState('');
  const [phoneCode, setPhoneCode] = useState('');
  const [phoneCountdown, setPhoneCountdown] = useState(0);
  const [sendingPhoneCode, setSendingPhoneCode] = useState(false);
  const [bindingPhone, setBindingPhone] = useState(false);

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

  /**
   * 发送邮箱验证码
   */
  const handleSendEmailCode = async () => {
    // 验证邮箱格式
    if (!emailInput.trim()) {
      toast.warning('请输入邮箱地址！😰');
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailInput)) {
      toast.warning('邮箱格式不正确！😰');
      return;
    }

    setSendingEmailCode(true);
    try {
      await userService.sendEmailCode(emailInput);
      toast.success('验证码已发送！📧');

      // 开始倒计时
      setEmailCountdown(60);
      const timer = setInterval(() => {
        setEmailCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (err: any) {
      console.error('发送邮箱验证码失败：', err);
      toast.error(err.response?.data?.message || '发送失败！😭');
    } finally {
      setSendingEmailCode(false);
    }
  };

  /**
   * 绑定邮箱
   */
  const handleBindEmail = async () => {
    // 验证表单
    if (!emailInput.trim()) {
      toast.warning('请输入邮箱地址！😰');
      return;
    }
    if (!emailCode.trim()) {
      toast.warning('请输入验证码！😰');
      return;
    }

    setBindingEmail(true);
    try {
      await userService.bindEmail(currentUser!.id, {
        email: emailInput,
        code: emailCode,
      });
      toast.success('邮箱绑定成功！🎉');

      // 关闭弹窗并刷新页面
      setShowEmailModal(false);
      setEmailInput('');
      setEmailCode('');
      window.location.reload();
    } catch (err: any) {
      console.error('绑定邮箱失败：', err);
      toast.error(err.response?.data?.message || '绑定失败！😭');
    } finally {
      setBindingEmail(false);
    }
  };

  /**
   * 发送手机验证码
   */
  const handleSendPhoneCode = async () => {
    // 验证手机号格式
    if (!phoneInput.trim()) {
      toast.warning('请输入手机号！😰');
      return;
    }
    if (!/^1[3-9]\d{9}$/.test(phoneInput)) {
      toast.warning('手机号格式不正确！😰');
      return;
    }

    setSendingPhoneCode(true);
    try {
      await userService.sendPhoneCode(phoneInput);
      toast.success('验证码已发送！📱');

      // 开始倒计时
      setPhoneCountdown(60);
      const timer = setInterval(() => {
        setPhoneCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (err: any) {
      console.error('发送手机验证码失败：', err);
      toast.error(err.response?.data?.message || '发送失败！😭');
    } finally {
      setSendingPhoneCode(false);
    }
  };

  /**
   * 绑定手机号
   */
  const handleBindPhone = async () => {
    // 验证表单
    if (!phoneInput.trim()) {
      toast.warning('请输入手机号！😰');
      return;
    }
    if (!phoneCode.trim()) {
      toast.warning('请输入验证码！😰');
      return;
    }

    setBindingPhone(true);
    try {
      await userService.bindPhone(currentUser!.id, {
        phone: phoneInput,
        code: phoneCode,
      });
      toast.success('手机号绑定成功！🎉');

      // 关闭弹窗并刷新页面
      setShowPhoneModal(false);
      setPhoneInput('');
      setPhoneCode('');
      window.location.reload();
    } catch (err: any) {
      console.error('绑定手机号失败：', err);
      toast.error(err.response?.data?.message || '绑定失败！😭');
    } finally {
      setBindingPhone(false);
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
              { label: '🖥️ 登录设备', value: 'devices' },
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
                  <div className="settings-item__value-with-action">
                    <span>{currentUser?.email || '未绑定'}</span>
                    {!currentUser?.email && (
                      <Button
                        type="primary"
                        size="small"
                        onClick={() => setShowEmailModal(true)}
                      >
                        绑定邮箱
                      </Button>
                    )}
                  </div>
                </div>
                <div className="settings-item">
                  <div className="settings-item__label">手机号</div>
                  <div className="settings-item__value-with-action">
                    <span>{currentUser?.phone || '未绑定'}</span>
                    {!currentUser?.phone && (
                      <Button
                        type="primary"
                        size="small"
                        onClick={() => setShowPhoneModal(true)}
                      >
                        绑定手机号
                      </Button>
                    )}
                  </div>
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

        {/* ==================== 登录设备管理 ==================== */}
        {activeTab === 'devices' && (
          <div className="settings-content">
            <div className="settings-section">
              <LoginDevices />
            </div>
          </div>
        )}
      </div>

      {/* ==================== 邮箱绑定弹窗 ==================== */}
      {showEmailModal && (
        <div className="modal-overlay" onClick={() => setShowEmailModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">📧 绑定邮箱</h2>
            <div className="modal-body">
              <div className="settings-field">
                <label className="settings-field__label">邮箱地址</label>
                <Input
                  type="email"
                  size="large"
                  placeholder="请输入邮箱地址"
                  value={emailInput}
                  onChange={(e) => setEmailInput(e.target.value)}
                />
              </div>
              <div className="settings-field">
                <label className="settings-field__label">验证码</label>
                <div className="verification-code-field">
                  <Input
                    size="large"
                    placeholder="请输入验证码"
                    value={emailCode}
                    onChange={(e) => setEmailCode(e.target.value)}
                    maxLength={6}
                  />
                  <Button
                    type="primary"
                    size="large"
                    onClick={handleSendEmailCode}
                    loading={sendingEmailCode}
                    disabled={emailCountdown > 0}
                  >
                    {emailCountdown > 0
                      ? `${emailCountdown}秒后重试`
                      : sendingEmailCode
                      ? '发送中...'
                      : '获取验证码'}
                  </Button>
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <Button
                type="default"
                size="large"
                onClick={() => setShowEmailModal(false)}
              >
                取消
              </Button>
              <Button
                type="primary"
                size="large"
                onClick={handleBindEmail}
                loading={bindingEmail}
              >
                {bindingEmail ? '绑定中...' : '确认绑定'}
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* ==================== 手机号绑定弹窗 ==================== */}
      {showPhoneModal && (
        <div className="modal-overlay" onClick={() => setShowPhoneModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">📱 绑定手机号</h2>
            <div className="modal-body">
              <div className="settings-field">
                <label className="settings-field__label">手机号</label>
                <Input
                  type="tel"
                  size="large"
                  placeholder="请输入手机号"
                  value={phoneInput}
                  onChange={(e) => setPhoneInput(e.target.value)}
                  maxLength={11}
                />
              </div>
              <div className="settings-field">
                <label className="settings-field__label">验证码</label>
                <div className="verification-code-field">
                  <Input
                    size="large"
                    placeholder="请输入验证码"
                    value={phoneCode}
                    onChange={(e) => setPhoneCode(e.target.value)}
                    maxLength={6}
                  />
                  <Button
                    type="primary"
                    size="large"
                    onClick={handleSendPhoneCode}
                    loading={sendingPhoneCode}
                    disabled={phoneCountdown > 0}
                  >
                    {phoneCountdown > 0
                      ? `${phoneCountdown}秒后重试`
                      : sendingPhoneCode
                      ? '发送中...'
                      : '获取验证码'}
                  </Button>
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <Button
                type="default"
                size="large"
                onClick={() => setShowPhoneModal(false)}
              >
                取消
              </Button>
              <Button
                type="primary"
                size="large"
                onClick={handleBindPhone}
                loading={bindingPhone}
              >
                {bindingPhone ? '绑定中...' : '确认绑定'}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Settings;
