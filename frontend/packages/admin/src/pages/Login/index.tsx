/**
 * 登录页面
 *
 * @author BaSui 😎
 * @date 2025-11-01
 * @updated 2025-11-06 - 添加密码加密传输
 * @updated 2025-11-10 - 集成图形验证码人机验证
 */

import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, App } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { encryptPassword } from '@campus/shared/utils/crypto';
import { ImageCaptcha, TwoFactorVerify } from '@campus/shared/components'; // ✅ 导入图形验证码和2FA组件
import type { LoginRequest } from '@campus/shared';
import './Login.css';

const { Title, Text } = Typography;

const Login: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const { message } = App.useApp(); // ✅ 使用 App 提供的 message 实例
  const [loading, setLoading] = useState(false);

  // 🎨 验证码状态（新增 - BaSui 2025-11-10）
  const [captchaData, setCaptchaData] = useState<{ captchaId: string; code: string } | null>(null);
  const [resetCaptcha, setResetCaptcha] = useState(false);

  // 🔐 2FA 状态（新增 - BaSui 2025-11-10）
  const [show2FAVerify, setShow2FAVerify] = useState(false);
  const [tempToken, setTempToken] = useState<string>('');
  const [formValues, setFormValues] = useState<LoginRequest | null>(null);

  // ===== 提交登录 =====
  const handleSubmit = async (values: LoginRequest) => {
    // 1️⃣ 验证图形验证码
    if (!captchaData) {
      message.error('请先完成图形验证码！');
      return;
    }

    setLoading(true);

    try {
      // 🔐 加密密码
      const encryptedPassword = encryptPassword(values.password);
      console.log('✅ 密码已加密传输');

      // ✅ 发送加密后的密码 + 验证码数据
      const loginParams = {
        username: values.username,
        password: encryptedPassword,
        captchaId: captchaData.captchaId,   // ✅ 验证码ID
        captchaCode: captchaData.code,       // ✅ 验证码输入
      };

      const response = await login(loginParams);

      // 🔐 检查是否需要 2FA 验证（新增 - BaSui 2025-11-10）
      if (response?.requires2FA) {
        console.log('[AdminLogin] 🔐 需要 2FA 验证');
        setShow2FAVerify(true);
        setTempToken(response.tempToken || '');
        setFormValues(loginParams);
        setLoading(false);
        return;
      }

      message.success('欢迎回来，管理员！😎');

      // 跳转到仪表盘
      navigate('/admin/dashboard');
    } catch (error: any) {
      console.error('❌ 登录失败:', error);
      message.error(error?.message || '登录失败，请稍后再试');

      // 🔄 重置验证码
      setCaptchaData(null);
      setResetCaptcha(prev => !prev);
    } finally {
      setLoading(false);
    }
  };

  /**
   * 🔐 处理 2FA 验证（新增 - BaSui 2025-11-10）
   */
  const handle2FAVerify = async (code: string) => {
    if (!formValues) {
      message.error('登录信息丢失，请重新登录！');
      setShow2FAVerify(false);
      return;
    }

    setLoading(true);

    try {
      // 使用 2FA 代码再次登录
      console.log('[AdminLogin] 🔐 提交 2FA 验证码');
      await login({
        ...formValues,
        twoFactorCode: code,
      });

      console.log('[AdminLogin] ✅ 2FA 验证成功，登录成功');
      message.success('欢迎回来，管理员！😎');

      // 跳转到仪表盘
      navigate('/admin/dashboard');
    } catch (error: any) {
      console.error('[AdminLogin] ❌ 2FA 验证失败:', error);
      message.error(error?.message || '验证码错误，请重试');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 🔙 取消 2FA 验证（新增 - BaSui 2025-11-10）
   */
  const handleCancel2FA = () => {
    setShow2FAVerify(false);
    setTempToken('');
    setFormValues(null);
    setCaptchaData(null);
    setResetCaptcha(prev => !prev);
  };

  /**
   * 🎨 图形验证码成功回调
   */
  const handleCaptchaSuccess = (captchaId: string, code: string) => {
    console.log('✅ [AdminLogin] 图形验证码验证成功:', { captchaId, code });
    setCaptchaData({ captchaId, code });
  };

  /**
   * 🎨 图形验证码失败回调
   */
  const handleCaptchaFail = () => {
    console.warn('❌ [AdminLogin] 图形验证码验证失败');
    setCaptchaData(null);
  };

  // 🔐 如果需要 2FA 验证，显示 2FA 验证组件（新增 - BaSui 2025-11-10）
  if (show2FAVerify) {
    return (
      <div className="login-container">
        {/* 装饰性元素 */}
        <div className="decoration-circle decoration-top-left"></div>
        <div className="decoration-circle decoration-bottom-right"></div>
        
        <Card className="login-card" variant="borderless">
          {/* 顶部装饰条 */}
          <div className="card-decoration-bar"></div>
          
          {/* Logo区域 */}
          <div className="login-logo">
            <div className="logo-circle">
              <svg viewBox="0 0 1024 1024" width="56" height="56">
                <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z" fill="url(#gradient1)"/>
                <path d="M512 140c-205.4 0-372 166.6-372 372s166.6 372 372 372 372-166.6 372-372-166.6-372-372-372z m0 684c-172.3 0-312-139.7-312-312s139.7-312 312-312 312 139.7 312 312-139.7 312-312 312z" fill="url(#gradient2)"/>
                <defs>
                  <linearGradient id="gradient1" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style={{stopColor: '#667eea', stopOpacity: 1}} />
                    <stop offset="100%" style={{stopColor: '#764ba2', stopOpacity: 1}} />
                  </linearGradient>
                  <linearGradient id="gradient2" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style={{stopColor: '#f093fb', stopOpacity: 1}} />
                    <stop offset="100%" style={{stopColor: '#f5576c', stopOpacity: 1}} />
                  </linearGradient>
                </defs>
              </svg>
            </div>
          </div>

          <div className="login-header">
            <Title level={2}>校园轻享集市</Title>
            <Text type="secondary">管理后台系统 - 双因素认证</Text>
          </div>

          {/* 2FA 验证组件 */}
          <TwoFactorVerify
            onVerify={handle2FAVerify}
            onCancel={handleCancel2FA}
            loading={loading}
          />

          <div className="login-footer">
            <Text type="secondary">
              © 2025 校园轻享集市 · Powered by BaSui 😎
            </Text>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="login-container">
      {/* 装饰性元素 */}
      <div className="decoration-circle decoration-top-left"></div>
      <div className="decoration-circle decoration-bottom-right"></div>
      
      <Card className="login-card" variant="borderless">
        {/* 顶部装饰条 */}
        <div className="card-decoration-bar"></div>
        
        {/* Logo区域 */}
        <div className="login-logo">
          <div className="logo-circle">
            <svg viewBox="0 0 1024 1024" width="56" height="56">
              <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z" fill="url(#gradient1)"/>
              <path d="M512 140c-205.4 0-372 166.6-372 372s166.6 372 372 372 372-166.6 372-372-166.6-372-372-372z m0 684c-172.3 0-312-139.7-312-312s139.7-312 312-312 312 139.7 312 312-139.7 312-312 312z" fill="url(#gradient2)"/>
              <defs>
                <linearGradient id="gradient1" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style={{stopColor: '#667eea', stopOpacity: 1}} />
                  <stop offset="100%" style={{stopColor: '#764ba2', stopOpacity: 1}} />
                </linearGradient>
                <linearGradient id="gradient2" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style={{stopColor: '#f093fb', stopOpacity: 1}} />
                  <stop offset="100%" style={{stopColor: '#f5576c', stopOpacity: 1}} />
                </linearGradient>
              </defs>
            </svg>
          </div>
        </div>

        <div className="login-header">
          <Title level={2}>校园轻享集市</Title>
          <Text type="secondary">管理后台系统</Text>
        </div>

        <Form
          name="login"
          size="large"
          onFinish={handleSubmit}
          autoComplete="off"
        >
          <Form.Item
            name="username"
            rules={[
              { required: true, message: '请输入用户名！' },
              { min: 3, message: '用户名至少 3 个字符！' },
            ]}
          >
            <div className="input-wrapper">
              <Input
                prefix={<UserOutlined className="input-icon" />}
                placeholder="用户名"
                autoComplete="username"
                className="styled-input"
              />
            </div>
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: '请输入密码！' },
              { min: 6, message: '密码至少 6 个字符！' },
            ]}
          >
            <div className="input-wrapper">
              <Input.Password
                prefix={<LockOutlined className="input-icon" />}
                placeholder="密码"
                autoComplete="current-password"
                className="styled-input"
              />
            </div>
          </Form.Item>

          {/* 🎨 图形验证码（新增 - BaSui 2025-11-10） */}
          <Form.Item>
            <ImageCaptcha
              onSuccess={handleCaptchaSuccess}
              onFail={handleCaptchaFail}
              reset={resetCaptcha}
              className="admin-captcha"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              className="login-button"
            >
              <span className="button-text">
                {loading ? '登录中...' : '立即登录'}
              </span>
            </Button>
          </Form.Item>
        </Form>

        <div className="login-footer">
          <Text type="secondary">
            © 2025 校园轻享集市 · Powered by BaSui 😎
          </Text>
        </div>
      </Card>
    </div>
  );
};

export default Login;
