/**
 * 注册页面 - 欢迎加入我们！🎉
 * @author BaSui 😎
 * @description 邮箱验证码注册 + 滑块验证
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input, Button } from '@campus/shared/components';
import { SliderCaptcha } from '../../components/SliderCaptcha';
import { authService } from '@campus/shared/services/auth';
import type { ConfirmRegisterByEmailRequest } from '@campus/shared/api/models';
import './Register.css';

/**
 * 注册页面组件
 */
const Register: React.FC = () => {
  const navigate = useNavigate();

  // 表单状态
  const [formData, setFormData] = useState<ConfirmRegisterByEmailRequest>({
    username: '',
    password: '',
    email: '',
    code: '',
  });

  // BaSui新增：确认密码字段（用于验证，不提交到后端）
  const [confirmPassword, setConfirmPassword] = useState('');

  // UI 状态
  const [loading, setLoading] = useState(false);
  const [sendingCode, setSendingCode] = useState(false);
  const [countdown, setCountdown] = useState(0); // 倒计时（秒）
  const [isVerified, setIsVerified] = useState(false); // 是否通过滑块验证
  const [resetCaptcha, setResetCaptcha] = useState(false); // 重置验证码
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [checkingUsername, setCheckingUsername] = useState(false); // 正在校验用户名
  const [checkingEmail, setCheckingEmail] = useState(false); // 正在校验邮箱

  /**
   * 🔍 实时校验用户名是否已存在（BaSui 新增 🎯）
   */
  const handleCheckUsername = async (username: string) => {
    // 跳过空值和过短的用户名
    if (!username.trim() || username.length < 3) {
      return;
    }

    setCheckingUsername(true);

    try {
      console.log('[Register] 🔍 校验用户名:', username);
      const response = await authService.checkUsername(username);

      if (response.data === true) {
        // 用户名已存在
        setErrors(prev => ({ ...prev, username: '❌ 用户名已被占用！' }));
      } else {
        // 用户名可用
        setErrors(prev => ({ ...prev, username: '✅ 用户名可用！' }));
        // 3秒后清除成功提示
        setTimeout(() => {
          setErrors(prev => ({ ...prev, username: '' }));
        }, 3000);
      }
    } catch (error: any) {
      console.error('[Register] ❌ 校验用户名失败:', error);
      // 网络错误不显示，避免干扰用户
    } finally {
      setCheckingUsername(false);
    }
  };

  /**
   * 🔍 实时校验邮箱是否已存在（BaSui 新增 🎯）
   */
  const handleCheckEmail = async (email: string) => {
    // 跳过空值和格式错误的邮箱
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email.trim() || !emailRegex.test(email)) {
      return;
    }

    setCheckingEmail(true);

    try {
      console.log('[Register] 🔍 校验邮箱:', email);
      const response = await authService.checkEmail(email);

      if (response.data === true) {
        // 邮箱已被注册
        setErrors(prev => ({ ...prev, email: '❌ 邮箱已被注册！' }));
      } else {
        // 邮箱可用
        setErrors(prev => ({ ...prev, email: '✅ 邮箱可用！' }));
        // 3秒后清除成功提示
        setTimeout(() => {
          setErrors(prev => ({ ...prev, email: '' }));
        }, 3000);
      }
    } catch (error: any) {
      console.error('[Register] ❌ 校验邮箱失败:', error);
      // 网络错误不显示，避免干扰用户
    } finally {
      setCheckingEmail(false);
    }
  };

  /**
   * 📝 处理表单输入
   */
  const handleInputChange = (field: keyof ConfirmRegisterByEmailRequest) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.value,
    }));
    // 清除该字段的错误提示
    setErrors(prev => ({ ...prev, [field]: '' }));
  };

  /**
   * 📧 发送邮箱验证码
   */
  const handleSendCode = async () => {
    // 验证邮箱格式
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!formData.email.trim()) {
      setErrors(prev => ({ ...prev, email: '邮箱不能为空！' }));
      return;
    }
    if (!emailRegex.test(formData.email)) {
      setErrors(prev => ({ ...prev, email: '邮箱格式不正确！' }));
      return;
    }

    // 检查滑块验证
    if (!isVerified) {
      setErrors(prev => ({ ...prev, captcha: '请先完成滑块验证！' }));
      return;
    }

    setSendingCode(true);

    try {
      console.log('[Register] 📧 发送邮箱验证码:', formData.email);
      await authService.sendRegisterEmailCode(formData.email);

      console.log('[Register] ✅ 验证码发送成功！');

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
      console.error('[Register] ❌ 发送验证码失败:', error);

      const errorMessage = error?.response?.data?.message || error?.message || '发送失败，请重试！';
      setErrors(prev => ({ ...prev, email: errorMessage }));
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

    if (!formData.username.trim()) {
      newErrors.username = '用户名不能为空！';
    } else if (formData.username.length < 3) {
      newErrors.username = '用户名至少 3 位！';
    } else if (formData.username.length > 20) {
      newErrors.username = '用户名最多 20 位！';
    }

    if (!formData.email.trim()) {
      newErrors.email = '邮箱不能为空！';
    } else if (!emailRegex.test(formData.email)) {
      newErrors.email = '邮箱格式不正确！';
    }

    if (!formData.code.trim()) {
      newErrors.code = '验证码不能为空！';
    } else if (formData.code.length !== 6) {
      newErrors.code = '验证码为 6 位数字！';
    }

    if (!formData.password.trim()) {
      newErrors.password = '密码不能为空！';
    } else if (formData.password.length < 6) {
      newErrors.password = '密码至少 6 位！';
    } else if (formData.password.length > 20) {
      newErrors.password = '密码最多 20 位！';
    }

    // ✅ BaSui新增：密码确认验证
    if (!confirmPassword.trim()) {
      newErrors.confirmPassword = '请再次输入密码！';
    } else if (confirmPassword !== formData.password) {
      newErrors.confirmPassword = '两次密码不一致！';
    }

    if (!isVerified) {
      newErrors.captcha = '请先完成滑块验证！';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  /**
   * 🚀 处理注册提交
   */
  const handleRegister = async () => {
    // 1. 验证表单
    if (!validateForm()) {
      console.warn('[Register] 表单验证失败');
      return;
    }

    setLoading(true);

    try {
      // 2. 调用真实后端 API
      console.log('[Register] 🚀 调用注册接口:', formData);
      await authService.registerByEmail(formData);

      console.log('[Register] ✅ 注册成功！');

      // 3. 跳转到登录页面
      setTimeout(() => {
        navigate('/login');
      }, 500);
    } catch (error: any) {
      console.error('[Register] ❌ 注册失败:', error);

      // 显示错误提示
      const errorMessage = error?.response?.data?.message || error?.message || '注册失败，请重试！';
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
    console.log('[Register] ✅ 滑块验证通过！');
    setIsVerified(true);
    setErrors(prev => ({ ...prev, captcha: '' }));
  };

  /**
   * 🎯 滑块验证失败
   */
  const handleCaptchaFail = () => {
    console.warn('[Register] ❌ 滑块验证失败！');
    setIsVerified(false);
  };

  /**
   * ⌨️ 按下回车键注册
   */
  const handlePressEnter = () => {
    handleRegister();
  };

  return (
    <div className="register-page">
      <div className="register-container">
        {/* 左侧欢迎区域 */}
        <div className="register-welcome">
          <h1 className="register-welcome__title">加入我们</h1>
          <p className="register-welcome__subtitle">Join Campus Lite Marketplace</p>
          <p className="register-welcome__desc">
            🌟 开启你的校园二手交易之旅
            <br />
            📦 发布闲置 | 淘好物 | 交朋友
            <br />
            🎁 注册即送 100 积分！
          </p>
        </div>

        {/* 右侧注册表单 */}
        <div className="register-form">
          <h2 className="register-form__title">创建账号 🚀</h2>
          <p className="register-form__subtitle">填写信息完成注册</p>

          {/* 表单错误提示 */}
          {errors.form && (
            <div className="register-form__error">
              ⚠️ {errors.form}
            </div>
          )}

          {/* 用户名输入框 */}
          <div className="register-form__field">
            <label className="register-form__label">用户名</label>
            <Input
              size="large"
              placeholder="请输入用户名（3-20位）"
              value={formData.username}
              onChange={handleInputChange('username')}
              onBlur={(e) => handleCheckUsername(e.target.value)} // ✅ BaSui 新增：失焦时校验
              error={!!errors.username && !errors.username.startsWith('✅')}
              errorMessage={errors.username}
              prefix={<span>👤</span>}
              suffix={checkingUsername ? <span>⏳</span> : null} // 显示加载状态
              allowClear
              maxLength={20}
            />
          </div>

          {/* 邮箱输入框 */}
          <div className="register-form__field">
            <label className="register-form__label">校园邮箱</label>
            <Input
              type="email"
              size="large"
              placeholder="请输入校园邮箱"
              value={formData.email}
              onChange={handleInputChange('email')}
              onBlur={(e) => handleCheckEmail(e.target.value)} // ✅ BaSui 新增：失焦时校验
              error={!!errors.email && !errors.email.startsWith('✅')}
              errorMessage={errors.email}
              prefix={<span>📧</span>}
              suffix={checkingEmail ? <span>⏳</span> : null} // 显示加载状态
              allowClear
            />
          </div>

          {/* 验证码输入框 */}
          <div className="register-form__field">
            <label className="register-form__label">邮箱验证码</label>
            <div className="register-form__code-input">
              <Input
                size="large"
                placeholder="请输入 6 位验证码"
                value={formData.code}
                onChange={handleInputChange('code')}
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
                className="register-form__code-button"
              >
                {countdown > 0 ? `${countdown}s` : sendingCode ? '发送中...' : '发送验证码'}
              </Button>
            </div>
          </div>

          {/* 密码输入框 */}
          <div className="register-form__field">
            <label className="register-form__label">密码</label>
            <Input
              type="password"
              size="large"
              placeholder="请输入密码（6-20位）"
              value={formData.password}
              onChange={handleInputChange('password')}
              error={!!errors.password}
              errorMessage={errors.password}
              prefix={<span>🔒</span>}
              maxLength={20}
            />
          </div>

          {/* ✅ BaSui新增：确认密码输入框 */}
          <div className="register-form__field">
            <label className="register-form__label">确认密码</label>
            <Input
              type="password"
              size="large"
              placeholder="请再次输入密码"
              value={confirmPassword}
              onChange={(e) => {
                setConfirmPassword(e.target.value);
                // 清除确认密码的错误提示
                setErrors(prev => ({ ...prev, confirmPassword: '' }));
              }}
              onPressEnter={handlePressEnter}
              error={!!errors.confirmPassword}
              errorMessage={errors.confirmPassword}
              prefix={<span>🔑</span>}
              maxLength={20}
            />
          </div>

          {/* 滑块验证 */}
          <div className="register-form__field">
            <SliderCaptcha
              onSuccess={handleCaptchaSuccess}
              onFail={handleCaptchaFail}
              reset={resetCaptcha}
            />
            {errors.captcha && (
              <div className="register-form__field-error">{errors.captcha}</div>
            )}
          </div>

          {/* 注册按钮 */}
          <Button
            type="primary"
            size="large"
            block
            loading={loading}
            onClick={handleRegister}
          >
            {loading ? '注册中...' : '立即注册'}
          </Button>

          {/* 登录链接 */}
          <div className="register-form__footer">
            已有账号？
            <a href="/login" className="register-form__link">
              立即登录
            </a>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
