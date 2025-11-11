/**
 * 登录页面 - 欢迎回家！🏠
 * @author BaSui 😎
 * @description 用户名/密码登录 + 滑块验证
 */

import React, { useState } from 'react';
import { useNavigate, useSearchParams, useLocation } from 'react-router-dom';
import { Input, Button, TwoFactorVerify } from '@campus/shared/components'; // ✅ 从 shared 包导入
import { encryptPassword } from '@campus/shared/utils'; // 🔐 导入密码加密工具
import { verifyCaptcha } from '@campus/shared/services/captcha'; // 🎯 导入方案B验证方法
import type { CaptchaVerifyRequest } from '@campus/shared/types/captcha'; // 🎯 导入方案B类型
import { UnifiedCaptcha, type CaptchaResult } from '../../components/UnifiedCaptcha';
import { useAuthStore } from '../../store';
import type { LoginRequest } from '@campus/shared/api/models';
import './Login.css';

/**
 * 登录页面组件
 */
const Login: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const location = useLocation();
  const { login } = useAuthStore();

  const redirectPath = searchParams.get('redirect') || '/';

  // 表单状态（自动填充注册页传来的用户名密码）
  const [formData, setFormData] = useState<LoginRequest>({
    username: location.state?.username || '',
    password: location.state?.password || '',
  });

  // UI 状态
  const [loading, setLoading] = useState(false);
  const [isVerified, setIsVerified] = useState(false); // 是否通过验证码验证
  const [resetCaptcha, setResetCaptcha] = useState(false); // 重置验证码
  const [errors, setErrors] = useState<Record<string, string>>({});

  // 🎯 方案B：验证码通行证（新增 - BaSui 2025-11-11）
  const [captchaToken, setCaptchaToken] = useState<string>(''); // 验证码通行证
  const [verifyStatus, setVerifyStatus] = useState<'idle' | 'verifying' | 'success' | 'error'>('idle'); // 验证状态

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
   * 🔍 智能识别登录凭证类型
   */
  const detectCredentialType = (credential: string): 'email' | 'phone' | 'username' | 'invalid' => {
    const trimmed = credential.trim();

    // 邮箱：包含 @
    if (trimmed.includes('@')) {
      const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      return emailRegex.test(trimmed) ? 'email' : 'invalid';
    }

    // 手机号：11位纯数字
    if (/^1[3-9]\d{9}$/.test(trimmed)) {
      return 'phone';
    }

    // 用户名：3-20位字母数字下划线
    if (/^[a-zA-Z0-9_]{3,20}$/.test(trimmed)) {
      return 'username';
    }

    return 'invalid';
  };

  /**
   * ✅ 验证表单数据
   */
  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    // 验证用户名/邮箱/手机号
    if (!formData.username.trim()) {
      newErrors.username = '请输入用户名/邮箱/手机号！';
    } else {
      const credentialType = detectCredentialType(formData.username);
      if (credentialType === 'invalid') {
        newErrors.username = '格式错误！用户名3-20位字母数字下划线，或输入有效的邮箱/手机号';
      }
    }

    if (!formData.password.trim()) {
      newErrors.password = '密码不能为空！';
    } else if (formData.password.length < 6) {
      newErrors.password = '密码至少 6 位！';
    }

    if (!isVerified) {
      newErrors.captcha = '请先完成验证码验证！';
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
      // 2. 🔐 加密密码（防止明文传输）
      let encryptedPassword: string;
      try {
        encryptedPassword = encryptPassword(formData.password);
        console.log('[Login] ✅ 密码已加密');
      } catch (error) {
        console.error('[Login] ❌ 密码加密失败:', error);
        setErrors({ form: '密码加密失败，请重试' });
        setLoading(false);
        return;
      }

      // 3. ✅ 使用 useAuthStore.login() 方法（会自动更新状态）
      // 🎯 方案B：使用验证码通行证（captchaToken）
      const loginParams = {
        username: formData.username,
        password: encryptedPassword, // 使用加密后的密码
        captchaToken: captchaToken, // ✅ 方案B：验证码通行证
      };
      console.log('[Login] 🚀 调用登录接口（方案B：使用验证码通行证）');
      const response = await login(loginParams);

      // 4. 🔐 检查是否需要 2FA 验证（新增 - BaSui 2025-11-09）
      if (response?.requires2FA) {
        console.log('[Login] 🔐 需要 2FA 验证');
        setShow2FAVerify(true);
        setTempToken(response.tempToken || '');
        setLoading(false);
        return;
      }

      console.log('[Login] ✅ 登录成功，状态已更新');

      // 5. 跳转到重定向路径或首页
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
      // 🔐 加密密码
      const encryptedPassword = encryptPassword(formData.password);

      // 使用 2FA 代码再次登录
      console.log('[Login] 🔐 提交 2FA 验证码');
      await login({
        ...formData,
        password: encryptedPassword, // 使用加密后的密码
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
   * 🎯 验证码验证成功（方案B：先验证验证码，获取通行证 - BaSui 2025-11-11）
   */
  const handleCaptchaSuccess = async (result: CaptchaResult) => {
    console.log('[Login] ✅ 验证码验证通过！类型:', result.type, '数据:', result);

    // 🔄 设置验证中状态
    setVerifyStatus('verifying');

    try {
      // 🎯 方案B：调用验证码验证接口，获取验证码通行证
      const request: CaptchaVerifyRequest = {
        type: result.type as 'image' | 'slider' | 'rotate' | 'click',
        captchaId: (result.captchaId || result.slideId || result.rotateId || result.clickId) as string,
        captchaCode: result.captchaCode,
        slidePosition: result.slidePosition,
        rotateAngle: result.rotateAngle,
        clickPoints: result.clickPoints,
      };

      console.log('[Login] 🔐 开始验证验证码，请求:', request);

      const response = await verifyCaptcha(request);

      console.log('[Login] ✅ 验证码验证成功，获得通行证:', response.captchaToken);

      // ✅ 设置成功状态
      setVerifyStatus('success');

      // 保存验证码通行证
      setCaptchaToken(response.captchaToken);
      setIsVerified(true);
      setErrors(prev => ({ ...prev, captcha: '' }));
    } catch (error: any) {
      console.error('[Login] ❌ 验证码验证失败:', error);

      // ❌ 设置失败状态
      setVerifyStatus('error');

      // 显示错误提示
      setErrors(prev => ({
        ...prev,
        captcha: error?.message || '验证码验证失败，请重试',
      }));

      // 重置验证状态
      setIsVerified(false);
      setCaptchaToken('');

      // 2秒后重置验证码
      setTimeout(() => {
        setVerifyStatus('idle');
        setResetCaptcha(prev => !prev);
      }, 2000);
    }
  };

  /**
   * 🎯 验证码验证失败
   */
  const handleCaptchaFail = () => {
    console.warn('[Login] ❌ 验证码验证失败！');
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
            <label className="login-form__label">用户名/邮箱/手机号</label>
            <Input
              size="large"
              placeholder="请输入用户名/邮箱/手机号"
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

          {/* 智能验证码（随机展示四种之一） */}
          <div className="login-form__field">
            <UnifiedCaptcha
              onSuccess={handleCaptchaSuccess}
              onFail={handleCaptchaFail}
              reset={resetCaptcha}
              verifyStatus={verifyStatus}
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
