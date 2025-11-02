/**
 * 忘记密码页面 - 别担心，我们帮你找回！🔑
 * @author BaSui 😎
 * @description 支持邮箱/手机号重置密码 + 滑块验证
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input, Button, Tabs } from '@campus/shared/components';
import { SliderCaptcha } from '../../components/SliderCaptcha';
import { authService } from '@campus/shared/services/auth';
import type { ResetPasswordByEmailRequest, ResetPasswordBySmsRequest } from '@campus/shared/api/models';
import './ForgotPassword.css';

type ResetMethod = 'email' | 'phone';

/**
 * 忘记密码页面组件
 */
const ForgotPassword: React.FC = () => {
  const navigate = useNavigate();

  // 重置方式
  const [resetMethod, setResetMethod] = useState<ResetMethod>('email');

  // 表单状态（邮箱）
  const [emailData, setEmailData] = useState<ResetPasswordByEmailRequest>({
    email: '',
    code: '',
    newPassword: '',
  });

  // 表单状态（手机号）
  const [phoneData, setPhoneData] = useState<ResetPasswordBySmsRequest>({
    phone: '',
    code: '',
    newPassword: '',
  });

  // UI 状态
  const [loading, setLoading] = useState(false);
  const [sendingCode, setSendingCode] = useState(false);
  const [countdown, setCountdown] = useState(0); // 倒计时（秒）
  const [isVerified, setIsVerified] = useState(false); // 是否通过滑块验证
  const [resetCaptcha, setResetCaptcha] = useState(false); // 重置验证码
  const [errors, setErrors] = useState<Record<string, string>>({});

  /**
   * 📝 处理表单输入（邮箱）
   */
  const handleEmailInputChange = (field: keyof ResetPasswordByEmailRequest) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setEmailData(prev => ({
      ...prev,
      [field]: event.target.value,
    }));
    setErrors(prev => ({ ...prev, [field]: '' }));
  };

  /**
   * 📝 处理表单输入（手机号）
   */
  const handlePhoneInputChange = (field: keyof ResetPasswordBySmsRequest) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setPhoneData(prev => ({
      ...prev,
      [field]: event.target.value,
    }));
    setErrors(prev => ({ ...prev, [field]: '' }));
  };

  /**
   * 📧 发送验证码
   */
  const handleSendCode = async () => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const phoneRegex = /^1[3-9]\d{9}$/;

    // 验证输入
    if (resetMethod === 'email') {
      if (!emailData.email.trim()) {
        setErrors(prev => ({ ...prev, email: '邮箱不能为空！' }));
        return;
      }
      if (!emailRegex.test(emailData.email)) {
        setErrors(prev => ({ ...prev, email: '邮箱格式不正确！' }));
        return;
      }
    } else {
      if (!phoneData.phone.trim()) {
        setErrors(prev => ({ ...prev, phone: '手机号不能为空！' }));
        return;
      }
      if (!phoneRegex.test(phoneData.phone)) {
        setErrors(prev => ({ ...prev, phone: '手机号格式不正确！' }));
        return;
      }
    }

    // 检查滑块验证
    if (!isVerified) {
      setErrors(prev => ({ ...prev, captcha: '请先完成滑块验证！' }));
      return;
    }

    setSendingCode(true);

    try {
      if (resetMethod === 'email') {
        console.log('[ForgotPassword] 📧 发送邮箱验证码:', emailData.email);
        await authService.sendResetEmailCode(emailData.email);
      } else {
        console.log('[ForgotPassword] 📱 发送手机验证码:', phoneData.phone);
        await authService.sendResetSmsCode(phoneData.phone);
      }

      console.log('[ForgotPassword] ✅ 验证码发送成功！');

      // 开始倒计时（60 秒）
      setCountdown(60);
      const timer = setInterval(() => {
        setCountdown(prev => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (error: any) {
      console.error('[ForgotPassword] ❌ 发送验证码失败:', error);

      const errorMessage = error?.response?.data?.message || error?.message || '发送失败，请重试！';
      setErrors(prev => ({ ...prev, [resetMethod]: errorMessage }));
    } finally {
      setSendingCode(false);
    }
  };

  /**
   * ✅ 验证表单数据
   */
  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const phoneRegex = /^1[3-9]\d{9}$/;

    if (resetMethod === 'email') {
      if (!emailData.email.trim()) {
        newErrors.email = '邮箱不能为空！';
      } else if (!emailRegex.test(emailData.email)) {
        newErrors.email = '邮箱格式不正确！';
      }

      if (!emailData.code.trim()) {
        newErrors.code = '验证码不能为空！';
      } else if (emailData.code.length !== 6) {
        newErrors.code = '验证码为 6 位数字！';
      }

      if (!emailData.newPassword.trim()) {
        newErrors.newPassword = '新密码不能为空！';
      } else if (emailData.newPassword.length < 6) {
        newErrors.newPassword = '密码至少 6 位！';
      } else if (emailData.newPassword.length > 20) {
        newErrors.newPassword = '密码最多 20 位！';
      }
    } else {
      if (!phoneData.phone.trim()) {
        newErrors.phone = '手机号不能为空！';
      } else if (!phoneRegex.test(phoneData.phone)) {
        newErrors.phone = '手机号格式不正确！';
      }

      if (!phoneData.code.trim()) {
        newErrors.code = '验证码不能为空！';
      } else if (phoneData.code.length !== 6) {
        newErrors.code = '验证码为 6 位数字！';
      }

      if (!phoneData.newPassword.trim()) {
        newErrors.newPassword = '新密码不能为空！';
      } else if (phoneData.newPassword.length < 6) {
        newErrors.newPassword = '密码至少 6 位！';
      } else if (phoneData.newPassword.length > 20) {
        newErrors.newPassword = '密码最多 20 位！';
      }
    }

    if (!isVerified) {
      newErrors.captcha = '请先完成滑块验证！';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  /**
   * 🚀 处理重置密码提交
   */
  const handleResetPassword = async () => {
    // 1. 验证表单
    if (!validateForm()) {
      console.warn('[ForgotPassword] 表单验证失败');
      return;
    }

    setLoading(true);

    try {
      // 2. 调用真实后端 API
      if (resetMethod === 'email') {
        console.log('[ForgotPassword] 🚀 调用邮箱重置密码接口:', emailData);
        await authService.resetPasswordByEmail(emailData);
      } else {
        console.log('[ForgotPassword] 🚀 调用手机号重置密码接口:', phoneData);
        await authService.resetPasswordBySms(phoneData);
      }

      console.log('[ForgotPassword] ✅ 重置密码成功！');

      // 3. 跳转到登录页面
      setTimeout(() => {
        navigate('/login');
      }, 500);
    } catch (error: any) {
      console.error('[ForgotPassword] ❌ 重置密码失败:', error);

      const errorMessage = error?.response?.data?.message || error?.message || '重置失败，请重试！';
      setErrors({ form: errorMessage });

      // 重置滑块验证
      setIsVerified(false);
      setResetCaptcha(prev => !prev);
    } finally {
      setLoading(false);
    }
  };

  /**
   * 🎯 滑块验证成功
   */
  const handleCaptchaSuccess = () => {
    console.log('[ForgotPassword] ✅ 滑块验证通过！');
    setIsVerified(true);
    setErrors(prev => ({ ...prev, captcha: '' }));
  };

  /**
   * 🎯 滑块验证失败
   */
  const handleCaptchaFail = () => {
    console.warn('[ForgotPassword] ❌ 滑块验证失败！');
    setIsVerified(false);
  };

  /**
   * ⌨️ 按下回车键提交
   */
  const handlePressEnter = () => {
    handleResetPassword();
  };

  /**
   * 🎯 切换重置方式
   */
  const handleTabChange = (value: string) => {
    setResetMethod(value as ResetMethod);
    setErrors({});
    setIsVerified(false);
    setResetCaptcha(prev => !prev);
  };

  return (
    <div className="forgot-password-page">
      <div className="forgot-password-container">
        {/* 左侧提示区域 */}
        <div className="forgot-password-banner">
          <h1 className="forgot-password-banner__title">密码找回</h1>
          <p className="forgot-password-banner__subtitle">Password Recovery</p>
          <p className="forgot-password-banner__desc">
            🔐 通过邮箱或手机号找回密码
            <br />
            🔒 安全验证，保护你的账户
            <br />
            ⚡ 快速找回，继续使用
          </p>
        </div>

        {/* 右侧重置表单 */}
        <div className="forgot-password-form">
          <h2 className="forgot-password-form__title">重置密码 🔑</h2>
          <p className="forgot-password-form__subtitle">选择找回方式</p>

          {/* 表单错误提示 */}
          {errors.form && (
            <div className="forgot-password-form__error">
              ⚠️ {errors.form}
            </div>
          )}

          {/* 选项卡 */}
          <Tabs
            defaultActiveKey="email"
            onChange={handleTabChange}
            items={[
              {
                key: 'email',
                label: '📧 邮箱找回',
                children: null, // 内容在外部渲染
              },
              {
                key: 'phone',
                label: '📱 手机找回',
                children: null, // 内容在外部渲染
              },
            ]}
          />

          {/* 邮箱找回表单 */}
          {resetMethod === 'email' && (
            <>
              {/* 邮箱输入框 */}
              <div className="forgot-password-form__field">
                <label className="forgot-password-form__label">邮箱地址</label>
                <Input
                  type="email"
                  size="large"
                  placeholder="请输入邮箱"
                  value={emailData.email}
                  onChange={handleEmailInputChange('email')}
                  error={!!errors.email}
                  errorMessage={errors.email}
                  prefix={<span>📧</span>}
                  allowClear
                />
              </div>

              {/* 验证码输入框 */}
              <div className="forgot-password-form__field">
                <label className="forgot-password-form__label">邮箱验证码</label>
                <div className="forgot-password-form__code-input">
                  <Input
                    size="large"
                    placeholder="请输入 6 位验证码"
                    value={emailData.code}
                    onChange={handleEmailInputChange('code')}
                    error={!!errors.code}
                    errorMessage={errors.code}
                    prefix={<span>🔢</span>}
                    maxLength={6}
                  />
                  <Button
                    type="primary"
                    size="large"
                    loading={sendingCode}
                    disabled={countdown > 0}
                    onClick={handleSendCode}
                    className="forgot-password-form__code-button"
                  >
                    {countdown > 0 ? `${countdown}s` : sendingCode ? '发送中...' : '发送验证码'}
                  </Button>
                </div>
              </div>

              {/* 新密码输入框 */}
              <div className="forgot-password-form__field">
                <label className="forgot-password-form__label">新密码</label>
                <Input
                  type="password"
                  size="large"
                  placeholder="请输入新密码（6-20位）"
                  value={emailData.newPassword}
                  onChange={handleEmailInputChange('newPassword')}
                  onPressEnter={handlePressEnter}
                  error={!!errors.newPassword}
                  errorMessage={errors.newPassword}
                  prefix={<span>🔒</span>}
                  maxLength={20}
                />
              </div>
            </>
          )}

          {/* 手机找回表单 */}
          {resetMethod === 'phone' && (
            <>
              {/* 手机号输入框 */}
              <div className="forgot-password-form__field">
                <label className="forgot-password-form__label">手机号</label>
                <Input
                  type="tel"
                  size="large"
                  placeholder="请输入手机号"
                  value={phoneData.phone}
                  onChange={handlePhoneInputChange('phone')}
                  error={!!errors.phone}
                  errorMessage={errors.phone}
                  prefix={<span>📱</span>}
                  allowClear
                  maxLength={11}
                />
              </div>

              {/* 验证码输入框 */}
              <div className="forgot-password-form__field">
                <label className="forgot-password-form__label">短信验证码</label>
                <div className="forgot-password-form__code-input">
                  <Input
                    size="large"
                    placeholder="请输入 6 位验证码"
                    value={phoneData.code}
                    onChange={handlePhoneInputChange('code')}
                    error={!!errors.code}
                    errorMessage={errors.code}
                    prefix={<span>🔢</span>}
                    maxLength={6}
                  />
                  <Button
                    type="primary"
                    size="large"
                    loading={sendingCode}
                    disabled={countdown > 0}
                    onClick={handleSendCode}
                    className="forgot-password-form__code-button"
                  >
                    {countdown > 0 ? `${countdown}s` : sendingCode ? '发送中...' : '发送验证码'}
                  </Button>
                </div>
              </div>

              {/* 新密码输入框 */}
              <div className="forgot-password-form__field">
                <label className="forgot-password-form__label">新密码</label>
                <Input
                  type="password"
                  size="large"
                  placeholder="请输入新密码（6-20位）"
                  value={phoneData.newPassword}
                  onChange={handlePhoneInputChange('newPassword')}
                  onPressEnter={handlePressEnter}
                  error={!!errors.newPassword}
                  errorMessage={errors.newPassword}
                  prefix={<span>🔒</span>}
                  maxLength={20}
                />
              </div>
            </>
          )}

          {/* 滑块验证 */}
          <div className="forgot-password-form__field">
            <SliderCaptcha
              onSuccess={handleCaptchaSuccess}
              onFail={handleCaptchaFail}
              reset={resetCaptcha}
            />
            {errors.captcha && (
              <div className="forgot-password-form__field-error">{errors.captcha}</div>
            )}
          </div>

          {/* 重置按钮 */}
          <Button
            type="primary"
            size="large"
            block
            loading={loading}
            onClick={handleResetPassword}
          >
            {loading ? '重置中...' : '重置密码'}
          </Button>

          {/* 返回登录链接 */}
          <div className="forgot-password-form__footer">
            记起密码了？
            <a href="/login" className="forgot-password-form__link">
              返回登录
            </a>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
