/**
 * 注册页面 📝
 * @author BaSui 😎
 * @description 手机号/邮箱注册，验证码验证
 */

import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { Input, Button } from '@campus/shared/components';
import { authService } from '@campus/shared/services';;
import { encryptPassword } from '@campus/shared/utils';
import { useAuthStore } from '../../store';
import './Register.css';

type RegisterType = 'phone' | 'email';

export const Register: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const setAuth = useAuthStore((state) => state.setAuth);

  const [registerType, setRegisterType] = useState<RegisterType>('phone');
  const [formData, setFormData] = useState({
    phone: '',
    email: '',
    password: '',
    confirmPassword: '',
    verificationCode: '',
    username: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [codeSent, setCodeSent] = useState(false);
  const [countdown, setCountdown] = useState(0);

  // 实时校验状态
  const [usernameChecking, setUsernameChecking] = useState(false);
  const [emailChecking, setEmailChecking] = useState(false);
  const [usernameAvailable, setUsernameAvailable] = useState<boolean | null>(null);
  const [emailAvailable, setEmailAvailable] = useState<boolean | null>(null);

  // 发送验证码
  const sendCodeMutation = useMutation({
    mutationFn: async () => {
      if (registerType === 'phone') {
        await authService.sendRegisterSmsCode(formData.phone);
      } else {
        await authService.sendRegisterEmailCode(formData.email);
      }
    },
    onSuccess: () => {
      setCodeSent(true);
      setCountdown(60);
      
      // 倒计时
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    },
    onError: (error: any) => {
      setErrors({ code: error?.message || '发送验证码失败' });
    },
  });

  // 注册
  const registerMutation = useMutation({
    mutationFn: async () => {
      // 🔐 加密密码（防止明文传输）
      let encryptedPassword: string;
      try {
        encryptedPassword = encryptPassword(formData.password);
      } catch (error) {
        console.error('❌ 密码加密失败:', error);
        throw new Error('密码加密失败，请重试');
      }
      
      const data = registerType === 'phone'
        ? {
            phone: formData.phone,
            password: encryptedPassword,
            code: formData.verificationCode,
            username: formData.username,
          }
        : {
            email: formData.email,
            password: encryptedPassword,
            code: formData.verificationCode,
            username: formData.username,
          };
      
      const response = registerType === 'phone'
        ? await authService.registerByPhone(data)
        : await authService.registerByEmail(data);
      
      return response;
    },
    onSuccess: () => {
      alert('🎉 注册成功！即将跳转到登录页...');
      navigate('/login', {
        state: {
          username: formData.username,
          password: formData.password,
        },
      });
    },
    onError: (error: any) => {
      console.error('[Register] ❌ 注册失败:', error);
      // 提取后端返回的错误信息
      const errorMessage = error?.response?.data?.message 
        || error?.message 
        || '注册失败，请检查输入信息';
      setErrors({ submit: errorMessage });
    },
  });

  // 表单验证
  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    // 用户名验证（与后端规则一致：3-50字符）
    if (!formData.username.trim()) {
      newErrors.username = '请输入用户名';
    } else if (formData.username.length < 3 || formData.username.length > 50) {
      newErrors.username = '用户名长度为3-50个字符';
    } else if (!/^[a-zA-Z0-9_]+$/.test(formData.username)) {
      newErrors.username = '用户名只能包含字母、数字和下划线';
    }

    // 手机号/邮箱验证
    if (registerType === 'phone') {
      if (!formData.phone.trim()) {
        newErrors.phone = '请输入手机号';
      } else if (!/^1[3-9]\d{9}$/.test(formData.phone)) {
        newErrors.phone = '请输入有效的手机号';
      }
    } else {
      if (!formData.email.trim()) {
        newErrors.email = '请输入邮箱';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
        newErrors.email = '请输入有效的邮箱地址';
      }
    }

    // 密码验证
    if (!formData.password) {
      newErrors.password = '请输入密码';
    } else if (formData.password.length < 6) {
      newErrors.password = '密码长度至少6位';
    }

    // 确认密码验证
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = '请确认密码';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = '两次输入的密码不一致';
    }

    // 验证码验证
    if (!formData.verificationCode.trim()) {
      newErrors.verificationCode = '请输入验证码';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 发送验证码
  const handleSendCode = () => {
    const newErrors: Record<string, string> = {};

    if (registerType === 'phone') {
      if (!formData.phone.trim()) {
        newErrors.phone = '请输入手机号';
      } else if (!/^1[3-9]\d{9}$/.test(formData.phone)) {
        newErrors.phone = '请输入有效的手机号';
      }
    } else {
      if (!formData.email.trim()) {
        newErrors.email = '请输入邮箱';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
        newErrors.email = '请输入有效的邮箱地址';
      }
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    sendCodeMutation.mutate();
  };

  // 提交注册
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    registerMutation.mutate();
  };

  // 切换注册方式
  const handleSwitchType = (type: RegisterType) => {
    setRegisterType(type);
    setErrors({});
    setCodeSent(false);
    setCountdown(0);
  };

  // ==================== 实时校验逻辑 ====================

  /**
   * 用户名实时校验（防抖 500ms）
   */
  useEffect(() => {
    // 重置校验状态
    setUsernameAvailable(null);

    // 用户名长度不足，不校验
    if (formData.username.length < 2) {
      return;
    }

    // 防抖：500ms 后执行校验
    const timer = setTimeout(async () => {
      setUsernameChecking(true);

      try {
        console.log('[Register] 🔍 校验用户名:', formData.username);
        const response = await authService.checkUsername(formData.username);
        const exists = response.data; // true-已存在，false-可用

        setUsernameAvailable(!exists);

        if (exists) {
          setErrors((prev) => ({ ...prev, username: '❌ 用户名已被占用' }));
        } else {
          setErrors((prev) => {
            const newErrors = { ...prev };
            delete newErrors.username;
            return newErrors;
          });
        }

        console.log('[Register] ✅ 用户名校验完成:', exists ? '已占用' : '可用');
      } catch (error: any) {
        console.error('[Register] ❌ 用户名校验失败:', error);
        // 校验失败不影响注册流程
      } finally {
        setUsernameChecking(false);
      }
    }, 500);

    return () => clearTimeout(timer);
  }, [formData.username]);

  /**
   * 邮箱实时校验（防抖 500ms）
   */
  useEffect(() => {
    // 只在邮箱注册模式下校验
    if (registerType !== 'email') {
      return;
    }

    // 重置校验状态
    setEmailAvailable(null);

    // 邮箱格式不正确，不校验
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      return;
    }

    // 防抖：500ms 后执行校验
    const timer = setTimeout(async () => {
      setEmailChecking(true);

      try {
        console.log('[Register] 🔍 校验邮箱:', formData.email);
        const response = await authService.checkEmail(formData.email);
        const exists = response.data; // true-已存在，false-可用

        setEmailAvailable(!exists);

        if (exists) {
          setErrors((prev) => ({ ...prev, email: '❌ 邮箱已被注册' }));
        } else {
          setErrors((prev) => {
            const newErrors = { ...prev };
            delete newErrors.email;
            return newErrors;
          });
        }

        console.log('[Register] ✅ 邮箱校验完成:', exists ? '已注册' : '可用');
      } catch (error: any) {
        console.error('[Register] ❌ 邮箱校验失败:', error);
        // 校验失败不影响注册流程
      } finally {
        setEmailChecking(false);
      }
    }, 500);

    return () => clearTimeout(timer);
  }, [formData.email, registerType]);

  return (
    <div className="register-page">
      <div className="register-container">
        {/* 左侧欢迎区域 */}
        <div className="register-welcome">
          <h1 className="register-welcome__title">加入我们</h1>
          <p className="register-welcome__subtitle">Join Campus Marketplace</p>
          <p className="register-welcome__desc">
            🎓 学生专属的二手交易平台
            <br />
            💡 买卖闲置 | 交友互动 | 绿色环保
            <br />
            🔥 注册即刻开启你的轻享之旅
          </p>
        </div>

        {/* 右侧注册表单 */}
        <div className="register-card">
          {/* Logo和标题 */}
          <div className="register-header">
            <h1 className="register-title">创建新账号 🎉</h1>
            <p className="register-subtitle">快速注册，开启轻享生活</p>
          </div>

          {/* 注册方式切换 */}
          <div className="register-tabs">
            <button
              className={`register-tab ${registerType === 'phone' ? 'active' : ''}`}
              onClick={() => handleSwitchType('phone')}
            >
              📱 手机号注册
            </button>
            <button
              className={`register-tab ${registerType === 'email' ? 'active' : ''}`}
              onClick={() => handleSwitchType('email')}
            >
              📧 邮箱注册
            </button>
          </div>

          {/* 注册表单 */}
          <form className="register-form" onSubmit={handleSubmit}>
            {/* 用户名 */}
            <div className="form-field">
              <label>用户名</label>
              <div className="input-with-status">
                <Input
                  size="large"
                  placeholder="请输入用户名（2-20个字符）"
                  value={formData.username}
                  onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                  prefix={<span>👤</span>}
                  allowClear
                />
                {/* 校验状态图标 */}
                {formData.username.length >= 2 && (
                  <span className="validation-status">
                    {usernameChecking ? (
                      <span className="status-loading">🔄</span>
                    ) : usernameAvailable === true ? (
                      <span className="status-success">✅</span>
                    ) : usernameAvailable === false ? (
                      <span className="status-error">❌</span>
                    ) : null}
                  </span>
                )}
              </div>
              {/* 错误提示 */}
              {errors.username && (
                <div className="form-field-error">{errors.username}</div>
              )}
              {/* 成功提示 */}
              {!errors.username && usernameAvailable === true && (
                <div className="form-field-success">✅ 用户名可用</div>
              )}
            </div>

            {/* 手机号/邮箱 */}
            {registerType === 'phone' ? (
              <div className="form-field">
                <label>手机号</label>
                <Input
                  size="large"
                  placeholder="请输入手机号"
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  prefix={<span>📱</span>}
                  allowClear
                />
                {errors.phone && (
                  <div className="form-field-error">{errors.phone}</div>
                )}
              </div>
            ) : (
              <div className="form-field">
                <label>邮箱</label>
                <div className="input-with-status">
                  <Input
                    size="large"
                    type="email"
                    placeholder="请输入邮箱"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    prefix={<span>📧</span>}
                    allowClear
                  />
                  {/* 校验状态图标 */}
                  {/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email) && (
                    <span className="validation-status">
                      {emailChecking ? (
                        <span className="status-loading">🔄</span>
                      ) : emailAvailable === true ? (
                        <span className="status-success">✅</span>
                      ) : emailAvailable === false ? (
                        <span className="status-error">❌</span>
                      ) : null}
                    </span>
                  )}
                </div>
                {/* 错误提示 */}
                {errors.email && (
                  <div className="form-field-error">{errors.email}</div>
                )}
                {/* 成功提示 */}
                {!errors.email && emailAvailable === true && (
                  <div className="form-field-success">✅ 邮箱可用</div>
                )}
              </div>
            )}

            {/* 验证码 */}
            <div className="form-field">
              <label>验证码</label>
              <div className="verification-code-field">
                <Input
                  size="large"
                  placeholder="请输入验证码"
                  value={formData.verificationCode}
                  onChange={(e) => setFormData({ ...formData, verificationCode: e.target.value })}
                  prefix={<span>🔐</span>}
                />
                <button
                  type="button"
                  className="send-code-btn"
                  onClick={handleSendCode}
                  disabled={countdown > 0 || sendCodeMutation.isPending}
                >
                  {countdown > 0
                    ? `${countdown}秒后重试`
                    : sendCodeMutation.isPending
                    ? '发送中...'
                    : codeSent
                    ? '重新发送'
                    : '获取验证码'}
                </button>
              </div>
              {errors.verificationCode && (
                <div className="form-field-error">{errors.verificationCode}</div>
              )}
              {errors.code && (
                <div className="form-field-error">{errors.code}</div>
              )}
            </div>

            {/* 密码 */}
            <div className="form-field">
              <label>密码</label>
              <Input
                size="large"
                type="password"
                placeholder="请输入密码（至少6位）"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                prefix={<span>🔒</span>}
              />
              {errors.password && (
                <div className="form-field-error">{errors.password}</div>
              )}
            </div>

            {/* 确认密码 */}
            <div className="form-field">
              <label>确认密码</label>
              <Input
                size="large"
                type="password"
                placeholder="请再次输入密码"
                value={formData.confirmPassword}
                onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                prefix={<span>🔒</span>}
              />
              {errors.confirmPassword && (
                <div className="form-field-error">{errors.confirmPassword}</div>
              )}
            </div>

            {/* 提交错误 */}
            {errors.submit && (
              <div className="form-submit-error">⚠️ {errors.submit}</div>
            )}

            {/* 注册按钮 */}
            <Button
              type="primary"
              size="large"
              htmlType="submit"
              loading={registerMutation.isPending}
              block
            >
              {registerMutation.isPending ? '注册中...' : '注册'}
            </Button>
          </form>

          {/* 底部链接 */}
          <div className="register-footer">
            <span>已有账号？</span>
            <Link to="/login" className="register-footer-link">
              立即登录
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
