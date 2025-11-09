/**
 * 登录页面 - 欢迎回家！🏠
 * @author BaSui 😎
 * @description 用户名/密码登录 + 滑块验证
 */

import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Input, Button } from '@campus/shared/components';
import { SliderCaptcha } from '../../components/SliderCaptcha';
import { TwoFactorVerify } from '../../components/TwoFactorVerify'; // ✅ 导入 2FA 验证组件
import { useAuthStore } from '../../store'; // ✅ 导入 useAuthStore
import type { LoginRequest } from '@campus/shared/api/models';
import './Login.css';

/**
 * 登录页面组件
 */
const Login: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { login } = useAuthStore(); // ✅ 获取 login 方法
  
  // 获取重定向路径（登录成功后跳转）
  const redirectPath = searchParams.get('redirect') || '/';

  // 表单状态
  const [formData, setFormData] = useState<LoginRequest>({
    username: '',
    password: '',
  });

  // UI 状态
  const [loading, setLoading] = useState(false);
  const [isVerified, setIsVerified] = useState(false); // 是否通过滑块验证
  const [resetCaptcha, setResetCaptcha] = useState(false); // 重置验证码
  const [errors, setErrors] = useState<Record<string, string>>({});

  // 2FA 状态（新增 - BaSui 2025-11-09）
  const [show2FAVerify, setShow2FAVerify] = useState(false); // 是否显示 2FA 验证
  const [tempToken, setTempToken] = useState<string>(''); // 临时 Token

  /**
   * 📝 处理表单输入
   */
  const handleInputChange = (field: keyof LoginRequest) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.value,
    }));
    // 清除该字段的错误提示
    setErrors(prev => ({ ...prev, [field]: '' }));
  };

  /**
   * ✅ 验证表单数据
   */
  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.username.trim()) {
      newErrors.username = '用户名不能为空！';
    }

    if (!formData.password.trim()) {
      newErrors.password = '密码不能为空！';
    } else if (formData.password.length < 6) {
      newErrors.password = '密码至少 6 位！';
    }

    if (!isVerified) {
      newErrors.captcha = '请先完成滑块验证！';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  /**
   * 🚀 处理登录提交
   */
  const handleLogin = async () => {
    // 1. 验证表单
    if (!validateForm()) {
      console.warn('[Login] 表单验证失败');
      return;
    }

    setLoading(true);

    try {
      // 2. ✅ 使用 useAuthStore.login() 方法（会自动更新状态）
      console.log('[Login] 🚀 调用登录接口:', formData);
      const response = await login(formData);

      // 3. 🔐 检查是否需要 2FA 验证（新增 - BaSui 2025-11-09）
      if (response?.requires2FA) {
        console.log('[Login] 🔐 需要 2FA 验证');
        setShow2FAVerify(true);
        setTempToken(response.tempToken || '');
        setLoading(false);
        return;
      }

      console.log('[Login] ✅ 登录成功，状态已更新');

      // 4. 跳转到重定向路径或首页
      console.log('[Login] 📍 跳转到:', redirectPath);
      setTimeout(() => {
        navigate(redirectPath, { replace: true });
      }, 500);
    } catch (error: any) {
      console.error('[Login] ❌ 登录失败:', error);

      // 显示错误提示
      const errorMessage = error?.response?.data?.message || error?.message || '登录失败，请重试！';
      setErrors({ form: errorMessage });

      // 重置滑块验证
      setIsVerified(false);
      setResetCaptcha(prev => !prev);
    } finally {
      setLoading(false);
    }
  };

  /**
   * 🔐 处理 2FA 验证（新增 - BaSui 2025-11-09）
   */
  const handle2FAVerify = async (code: string) => {
    setLoading(true);
    setErrors({});

    try {
      // 使用 2FA 代码再次登录
      console.log('[Login] 🔐 提交 2FA 验证码');
      await login({
        ...formData,
        twoFactorCode: code,
      });

      console.log('[Login] ✅ 2FA 验证成功，登录成功');

      // 跳转到重定向路径或首页
      setTimeout(() => {
        navigate(redirectPath, { replace: true });
      }, 500);
    } catch (error: any) {
      console.error('[Login] ❌ 2FA 验证失败:', error);

      // 显示错误提示
      const errorMessage = error?.response?.data?.message || error?.message || '验证码错误，请重试';
      setErrors({ form: errorMessage });
    } finally {
      setLoading(false);
    }
  };

  /**
   * 🔙 取消 2FA 验证（新增 - BaSui 2025-11-09）
   */
  const handleCancel2FA = () => {
    setShow2FAVerify(false);
    setTempToken('');
    setErrors({});
  };

  /**
   * 🎯 滑块验证成功
   */
  const handleCaptchaSuccess = () => {
    console.log('[Login] ✅ 滑块验证通过！');
    setIsVerified(true);
    setErrors(prev => ({ ...prev, captcha: '' }));
  };

  /**
   * 🎯 滑块验证失败
   */
  const handleCaptchaFail = () => {
    console.warn('[Login] ❌ 滑块验证失败！');
    setIsVerified(false);
  };

  /**
   * ⌨️ 按下回车键登录
   */
  const handlePressEnter = () => {
    handleLogin();
  };

  // 🔐 如果需要 2FA 验证，显示 2FA 验证组件（新增 - BaSui 2025-11-09）
  if (show2FAVerify) {
    return (
      <div className="login-page">
        <div className="login-container">
          {/* 左侧欢迎区域 */}
          <div className="login-welcome">
            <h1 className="login-welcome__title">校园轻享集市</h1>
            <p className="login-welcome__subtitle">Campus Lite Marketplace</p>
            <p className="login-welcome__desc">
              🎓 专为大学生打造的二手交易平台
              <br />
              💰 买卖闲置 | 交友互动 | 环保生活
            </p>
          </div>

          {/* 右侧 2FA 验证 */}
          <div className="login-form">
            <TwoFactorVerify
              onVerify={handle2FAVerify}
              onCancel={handleCancel2FA}
              loading={loading}
              error={errors.form}
            />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="login-page">
      <div className="login-container">
        {/* 左侧欢迎区域 */}
        <div className="login-welcome">
          <h1 className="login-welcome__title">校园轻享集市</h1>
          <p className="login-welcome__subtitle">Campus Lite Marketplace</p>
          <p className="login-welcome__desc">
            🎓 专为大学生打造的二手交易平台
            <br />
            💰 买卖闲置 | 交友互动 | 环保生活
          </p>
        </div>

        {/* 右侧登录表单 */}
        <div className="login-form">
          <h2 className="login-form__title">欢迎回来！👋</h2>
          <p className="login-form__subtitle">登录您的账号</p>

          {/* 表单错误提示 */}
          {errors.form && (
            <div className="login-form__error">
              ⚠️ {errors.form}
            </div>
          )}

          {/* 用户名输入框 */}
          <div className="login-form__field">
            <label className="login-form__label">用户名</label>
            <Input
              size="large"
              placeholder="请输入用户名"
              value={formData.username}
              onChange={handleInputChange('username')}
              onPressEnter={handlePressEnter}
              error={!!errors.username}
              errorMessage={errors.username}
              prefix={<span>👤</span>}
              allowClear
            />
          </div>

          {/* 密码输入框 */}
          <div className="login-form__field">
            <label className="login-form__label">密码</label>
            <Input
              type="password"
              size="large"
              placeholder="请输入密码"
              value={formData.password}
              onChange={handleInputChange('password')}
              onPressEnter={handlePressEnter}
              error={!!errors.password}
              errorMessage={errors.password}
              prefix={<span>🔒</span>}
            />
          </div>

          {/* 滑块验证 */}
          <div className="login-form__field">
            <SliderCaptcha
              onSuccess={handleCaptchaSuccess}
              onFail={handleCaptchaFail}
              reset={resetCaptcha}
            />
            {errors.captcha && (
              <div className="login-form__field-error">{errors.captcha}</div>
            )}
          </div>

          {/* 忘记密码链接 */}
          <div className="login-form__extra">
            <a href="/forgot-password" className="login-form__link">
              忘记密码？
            </a>
          </div>

          {/* 登录按钮 */}
          <Button
            type="primary"
            size="large"
            block
            loading={loading}
            onClick={handleLogin}
          >
            {loading ? '登录中...' : '登录'}
          </Button>

          {/* 注册链接 */}
          <div className="login-form__footer">
            还没有账号？
            <a href="/register" className="login-form__link">
              立即注册
            </a>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
