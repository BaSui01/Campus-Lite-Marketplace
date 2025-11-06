/**
 * 登录页面
 *
 * @author BaSui 😎
 * @date 2025-11-01
 * @updated 2025-11-06 - 添加密码加密传输
 */

import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, App } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { encryptPassword } from '@campus/shared/utils/crypto';
import type { LoginRequest } from '@campus/shared';
import './Login.css';

const { Title, Text } = Typography;

const Login: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const { message } = App.useApp(); // ✅ 使用 App 提供的 message 实例
  const [loading, setLoading] = useState(false);

  // ===== 提交登录 =====
  const handleSubmit = async (values: LoginRequest) => {
    setLoading(true);

    try {
      // 🔐 加密密码
      const encryptedPassword = encryptPassword(values.password);
      console.log('✅ 密码已加密传输');
      
      // 发送加密后的密码
      await login({
        username: values.username,
        password: encryptedPassword,
      });
      
      message.success('欢迎回来，管理员！😎');

      // 跳转到仪表盘
      navigate('/admin/dashboard');
    } catch (error: any) {
      console.error('❌ 登录失败:', error);
      message.error(error?.message || '登录失败，请稍后再试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <Card className="login-card">
        <div className="login-header">
          <Title level={2}>校园轻享集市</Title>
          <Text type="secondary">管理后台</Text>
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
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名"
              autoComplete="username"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: '请输入密码！' },
              { min: 6, message: '密码至少 6 个字符！' },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
              autoComplete="current-password"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
            >
              {loading ? '登录中...' : '登录'}
            </Button>
          </Form.Item>
        </Form>

        <div className="login-footer">
          <Text type="secondary">
            © 2025 校园轻享集市 | Powered by BaSui 😎
          </Text>
        </div>
      </Card>
    </div>
  );
};

export default Login;
