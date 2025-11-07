/**
 * 个人信息页面（增强版）
 *
 * 功能：
 * - 个人资料：查看和修改昵称、头像、简介
 * - 修改密码：安全的密码修改流程
 * - 联系方式：邮箱/手机号绑定（带验证码）
 * - 安全设置：两步验证（2FA）、登录通知
 * - 登录设备：查看和管理登录设备
 * - 账号信息：查看用户名、角色、权限等
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */

import React, { useState } from 'react';
import {
  Card,
  Form,
  Input,
  Button,
  Space,
  message,
  Tabs,
  Avatar,
  Upload,
  Divider,
  Tag,
  Row,
  Col,
  Typography,
  Table,
  Switch,
  Modal,
  QRCode,
  Steps,
  Badge,
  Tooltip,
  Alert,
} from 'antd';
import {
  UserOutlined,
  LockOutlined,
  SaveOutlined,
  CameraOutlined,
  InfoCircleOutlined,
  MailOutlined,
  PhoneOutlined,
  SafetyOutlined,
  MobileOutlined,
  DesktopOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  QrcodeOutlined,
  DeleteOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userService, uploadService } from '@campus/shared';
import { useAuth } from '@/hooks';
import type { UploadFile } from 'antd';
import type { RcFile } from 'antd/es/upload';
import type { ColumnsType } from 'antd/es/table';

const { TextArea } = Input;
const { Text, Title, Paragraph } = Typography;
const { Step } = Steps;

// 模拟登录设备数据
interface LoginDevice {
  id: string;
  deviceName: string;
  deviceType: 'mobile' | 'desktop' | 'tablet';
  os: string;
  browser: string;
  ip: string;
  location: string;
  lastActiveAt: string;
  isCurrent: boolean;
}

export const ProfilePage: React.FC = () => {
  const { user: currentUser } = useAuth();
  const queryClient = useQueryClient();

  // 表单实例
  const [profileForm] = Form.useForm();
  const [passwordForm] = Form.useForm();
  const [emailForm] = Form.useForm();
  const [phoneForm] = Form.useForm();

  // 状态管理
  const [avatarFileList, setAvatarFileList] = useState<UploadFile[]>([]);
  const [avatarUrl, setAvatarUrl] = useState<string | undefined>(currentUser?.avatar);
  const [twoFactorEnabled, setTwoFactorEnabled] = useState(false);
  const [emailVerified, setEmailVerified] = useState(false);
  const [phoneVerified, setPhoneVerified] = useState(false);
  const [twoFactorModalVisible, setTwoFactorModalVisible] = useState(false);
  const [twoFactorStep, setTwoFactorStep] = useState(0);
  const [twoFactorSecret, setTwoFactorSecret] = useState('');
  const [emailBindModalVisible, setEmailBindModalVisible] = useState(false);
  const [phoneBindModalVisible, setPhoneBindModalVisible] = useState(false);
  const [emailCodeSent, setEmailCodeSent] = useState(false);
  const [phoneCodeSent, setPhoneCodeSent] = useState(false);
  const [countdown, setCountdown] = useState(0);

  // 查询用户资料
  const { data: userProfile, isLoading } = useQuery({
    queryKey: ['userProfile'],
    queryFn: async () => {
      const response = await userService.getProfile();
      // 设置已验证状态
      setEmailVerified(!!response.data.emailVerified);
      setPhoneVerified(!!response.data.phoneVerified);
      setTwoFactorEnabled(!!response.data.twoFactorEnabled);
      return response.data;
    },
    staleTime: 5 * 60 * 1000,
  });

  // 查询登录设备（模拟数据）
  const { data: loginDevices = [] } = useQuery<LoginDevice[]>({
    queryKey: ['loginDevices'],
    queryFn: async () => {
      // TODO: 调用真实API
      // const response = await userService.getLoginDevices();
      // return response.data;

      // 模拟数据
      return [
        {
          id: '1',
          deviceName: 'Windows 11 - Chrome',
          deviceType: 'desktop',
          os: 'Windows 11',
          browser: 'Chrome 120',
          ip: '192.168.1.100',
          location: '中国 北京',
          lastActiveAt: new Date().toISOString(),
          isCurrent: true,
        },
        {
          id: '2',
          deviceName: 'iPhone 15 Pro - Safari',
          deviceType: 'mobile',
          os: 'iOS 17.2',
          browser: 'Safari',
          ip: '192.168.1.101',
          location: '中国 上海',
          lastActiveAt: new Date(Date.now() - 86400000).toISOString(),
          isCurrent: false,
        },
      ];
    },
    staleTime: 2 * 60 * 1000,
  });

  // 更新个人资料 Mutation
  const updateProfileMutation = useMutation({
    mutationFn: async (values: any) => {
      await userService.updateProfile({
        nickname: values.nickname,
        avatar: avatarUrl,
        bio: values.bio,
      });
    },
    onSuccess: () => {
      message.success('个人资料更新成功！🎉');
      queryClient.invalidateQueries({ queryKey: ['userProfile'] });
    },
    onError: (error: any) => {
      message.error(`更新失败：${error.message} 😰`);
    },
  });

  // 修改密码 Mutation
  const changePasswordMutation = useMutation({
    mutationFn: async (values: any) => {
      await userService.changePassword({
        oldPassword: values.oldPassword,
        newPassword: values.newPassword,
      });
    },
    onSuccess: () => {
      message.success('密码修改成功！请重新登录 🎉');
      passwordForm.resetFields();
    },
    onError: (error: any) => {
      message.error(`修改失败：${error.message} 😰`);
    },
  });

  // 上传头像 Mutation
  const uploadAvatarMutation = useMutation({
    mutationFn: async (file: RcFile) => {
      const formData = new FormData();
      formData.append('file', file);
      const response = await uploadService.uploadImage(formData);
      return response.data.url;
    },
    onSuccess: (url: string) => {
      setAvatarUrl(url);
      message.success('头像上传成功！记得保存修改 ✅');
    },
    onError: (error: any) => {
      message.error(`上传失败：${error.message} 😰`);
    },
  });

  // 发送邮箱验证码
  const sendEmailCode = () => {
    // TODO: 调用真实API
    message.success('验证码已发送到您的邮箱！');
    setEmailCodeSent(true);
    startCountdown();
  };

  // 发送手机验证码
  const sendPhoneCode = () => {
    // TODO: 调用真实API
    message.success('验证码已发送到您的手机！');
    setPhoneCodeSent(true);
    startCountdown();
  };

  // 倒计时
  const startCountdown = () => {
    setCountdown(60);
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  // 绑定邮箱
  const handleBindEmail = () => {
    emailForm.validateFields().then((values) => {
      // TODO: 调用真实API
      message.success('邮箱绑定成功！🎉');
      setEmailVerified(true);
      setEmailBindModalVisible(false);
      emailForm.resetFields();
      queryClient.invalidateQueries({ queryKey: ['userProfile'] });
    });
  };

  // 绑定手机号
  const handleBindPhone = () => {
    phoneForm.validateFields().then((values) => {
      // TODO: 调用真实API
      message.success('手机号绑定成功！🎉');
      setPhoneVerified(true);
      setPhoneBindModalVisible(false);
      phoneForm.resetFields();
      queryClient.invalidateQueries({ queryKey: ['userProfile'] });
    });
  };

  // 启用两步验证
  const handleEnableTwoFactor = () => {
    // 生成模拟密钥
    setTwoFactorSecret('JBSWY3DPEHPK3PXP');
    setTwoFactorModalVisible(true);
    setTwoFactorStep(0);
  };

  // 确认两步验证
  const handleConfirmTwoFactor = () => {
    // TODO: 验证 TOTP 码
    message.success('两步验证启用成功！🎉');
    setTwoFactorEnabled(true);
    setTwoFactorModalVisible(false);
    setTwoFactorStep(0);
  };

  // 关闭两步验证
  const handleDisableTwoFactor = () => {
    Modal.confirm({
      title: '关闭两步验证',
      content: '关闭后您的账号安全性会降低，确定要关闭吗？',
      okText: '确认关闭',
      okType: 'danger',
      cancelText: '取消',
      onOk: () => {
        // TODO: 调用真实API
        message.success('两步验证已关闭');
        setTwoFactorEnabled(false);
      },
    });
  };

  // 踢出设备
  const handleKickDevice = (deviceId: string) => {
    Modal.confirm({
      title: '踢出设备',
      content: '确定要踢出这个设备吗？该设备需要重新登录。',
      okText: '确认踢出',
      okType: 'danger',
      cancelText: '取消',
      onOk: () => {
        // TODO: 调用真实API
        message.success('设备已踢出');
        queryClient.invalidateQueries({ queryKey: ['loginDevices'] });
      },
    });
  };

  // 提交个人资料
  const handleProfileSubmit = () => {
    profileForm.validateFields().then((values) => {
      updateProfileMutation.mutate(values);
    });
  };

  // 提交密码修改
  const handlePasswordSubmit = () => {
    passwordForm.validateFields().then((values) => {
      changePasswordMutation.mutate(values);
    });
  };

  // 头像上传前校验
  const beforeUpload = (file: RcFile) => {
    const isImage = file.type.startsWith('image/');
    if (!isImage) {
      message.error('只能上传图片文件！');
      return false;
    }
    const isLt2M = file.size / 1024 / 1024 < 2;
    if (!isLt2M) {
      message.error('图片大小不能超过 2MB！');
      return false;
    }
    uploadAvatarMutation.mutate(file);
    return false;
  };

  // 登录设备表格列定义
  const deviceColumns: ColumnsType<LoginDevice> = [
    {
      title: '设备信息',
      key: 'device',
      render: (_, record) => (
        <Space>
          {record.deviceType === 'mobile' ? <MobileOutlined style={{ fontSize: 18 }} /> : <DesktopOutlined style={{ fontSize: 18 }} />}
          <div>
            <div>
              <Text strong>{record.deviceName}</Text>
              {record.isCurrent && <Tag color="green" style={{ marginLeft: 8 }}>当前设备</Tag>}
            </div>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {record.os} · {record.browser}
            </Text>
          </div>
        </Space>
      ),
    },
    {
      title: 'IP 地址',
      dataIndex: 'ip',
      key: 'ip',
    },
    {
      title: '位置',
      dataIndex: 'location',
      key: 'location',
    },
    {
      title: '最后活跃',
      dataIndex: 'lastActiveAt',
      key: 'lastActiveAt',
      render: (time) => new Date(time).toLocaleString(),
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        !record.isCurrent ? (
          <Button
            type="link"
            danger
            size="small"
            icon={<DeleteOutlined />}
            onClick={() => handleKickDevice(record.id)}
          >
            踢出
          </Button>
        ) : null
      ),
    },
  ];

  // Tab 配置
  const tabItems = [
    {
      key: 'profile',
      label: (
        <span>
          <UserOutlined /> 个人资料
        </span>
      ),
      children: (
        <Card>
          <Form
            form={profileForm}
            layout="vertical"
            initialValues={{
              nickname: userProfile?.nickname,
              bio: userProfile?.bio,
            }}
          >
            {/* 头像上传 */}
            <Form.Item label="头像">
              <Space direction="vertical" align="center" style={{ width: '100%' }}>
                <Avatar
                  size={120}
                  src={avatarUrl || userProfile?.avatar}
                  icon={<UserOutlined />}
                />
                <Upload
                  accept="image/*"
                  showUploadList={false}
                  beforeUpload={beforeUpload}
                  fileList={avatarFileList}
                >
                  <Button icon={<CameraOutlined />} loading={uploadAvatarMutation.isPending}>
                    更换头像
                  </Button>
                </Upload>
                <Text type="secondary" style={{ fontSize: 12 }}>
                  支持 JPG、PNG 格式，大小不超过 2MB
                </Text>
              </Space>
            </Form.Item>

            <Divider />

            {/* 昵称 */}
            <Form.Item
              label="昵称"
              name="nickname"
              rules={[
                { required: true, message: '请输入昵称！' },
                { min: 2, max: 20, message: '昵称长度为 2-20 个字符！' },
              ]}
            >
              <Input placeholder="请输入昵称" maxLength={20} />
            </Form.Item>

            {/* 个人简介 */}
            <Form.Item
              label="个人简介"
              name="bio"
              rules={[{ max: 200, message: '简介不能超过 200 个字符！' }]}
            >
              <TextArea
                placeholder="介绍一下自己吧~"
                rows={4}
                maxLength={200}
                showCount
              />
            </Form.Item>

            {/* 提交按钮 */}
            <Form.Item>
              <Button
                type="primary"
                icon={<SaveOutlined />}
                onClick={handleProfileSubmit}
                loading={updateProfileMutation.isPending}
              >
                保存修改
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: 'password',
      label: (
        <span>
          <LockOutlined /> 修改密码
        </span>
      ),
      children: (
        <Card>
          <Alert
            message="密码安全提示"
            description="为了您的账号安全，建议定期更换密码，并使用包含大小写字母、数字和特殊字符的强密码。"
            type="info"
            showIcon
            style={{ marginBottom: 24 }}
          />
          <Form form={passwordForm} layout="vertical">
            <Form.Item
              label="当前密码"
              name="oldPassword"
              rules={[{ required: true, message: '请输入当前密码！' }]}
            >
              <Input.Password placeholder="请输入当前密码" />
            </Form.Item>

            <Form.Item
              label="新密码"
              name="newPassword"
              rules={[
                { required: true, message: '请输入新密码！' },
                { min: 6, max: 20, message: '密码长度为 6-20 个字符！' },
                {
                  pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,}$/,
                  message: '密码必须包含大小写字母和数字！',
                },
              ]}
            >
              <Input.Password placeholder="请输入新密码（6-20位，包含大小写字母和数字）" />
            </Form.Item>

            <Form.Item
              label="确认新密码"
              name="confirmPassword"
              dependencies={['newPassword']}
              rules={[
                { required: true, message: '请确认新密码！' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('newPassword') === value) {
                      return Promise.resolve();
                    }
                    return Promise.reject(new Error('两次输入的密码不一致！'));
                  },
                }),
              ]}
            >
              <Input.Password placeholder="请再次输入新密码" />
            </Form.Item>

            <Form.Item>
              <Button
                type="primary"
                icon={<SaveOutlined />}
                onClick={handlePasswordSubmit}
                loading={changePasswordMutation.isPending}
              >
                修改密码
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: 'contact',
      label: (
        <span>
          <MailOutlined /> 联系方式
        </span>
      ),
      children: (
        <Card>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            {/* 邮箱绑定 */}
            <div>
              <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
                <Col>
                  <Space>
                    <MailOutlined style={{ fontSize: 18 }} />
                    <div>
                      <div>
                        <Text strong>邮箱</Text>
                        {emailVerified && (
                          <Badge status="success" text="已验证" style={{ marginLeft: 8 }} />
                        )}
                      </div>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        {userProfile?.email || '未绑定邮箱'}
                      </Text>
                    </div>
                  </Space>
                </Col>
                <Col>
                  <Button
                    type={emailVerified ? 'default' : 'primary'}
                    onClick={() => setEmailBindModalVisible(true)}
                  >
                    {emailVerified ? '更换邮箱' : '绑定邮箱'}
                  </Button>
                </Col>
              </Row>
              <Paragraph type="secondary" style={{ fontSize: 12, margin: 0 }}>
                用于找回密码、接收重要通知
              </Paragraph>
            </div>

            <Divider />

            {/* 手机号绑定 */}
            <div>
              <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
                <Col>
                  <Space>
                    <PhoneOutlined style={{ fontSize: 18 }} />
                    <div>
                      <div>
                        <Text strong>手机号</Text>
                        {phoneVerified && (
                          <Badge status="success" text="已验证" style={{ marginLeft: 8 }} />
                        )}
                      </div>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        {userProfile?.phone || '未绑定手机号'}
                      </Text>
                    </div>
                  </Space>
                </Col>
                <Col>
                  <Button
                    type={phoneVerified ? 'default' : 'primary'}
                    onClick={() => setPhoneBindModalVisible(true)}
                  >
                    {phoneVerified ? '更换手机号' : '绑定手机号'}
                  </Button>
                </Col>
              </Row>
              <Paragraph type="secondary" style={{ fontSize: 12, margin: 0 }}>
                用于快速登录、接收验证码
              </Paragraph>
            </div>
          </Space>
        </Card>
      ),
    },
    {
      key: 'security',
      label: (
        <span>
          <SafetyOutlined /> 安全设置
        </span>
      ),
      children: (
        <Card>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            {/* 两步验证 */}
            <div>
              <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
                <Col>
                  <Space>
                    <QrcodeOutlined style={{ fontSize: 18 }} />
                    <div>
                      <div>
                        <Text strong>两步验证（2FA）</Text>
                        {twoFactorEnabled && (
                          <Badge status="success" text="已启用" style={{ marginLeft: 8 }} />
                        )}
                      </div>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        {twoFactorEnabled ? '使用 Google Authenticator 保护您的账号' : '未启用两步验证'}
                      </Text>
                    </div>
                  </Space>
                </Col>
                <Col>
                  {twoFactorEnabled ? (
                    <Button danger onClick={handleDisableTwoFactor}>
                      关闭
                    </Button>
                  ) : (
                    <Button type="primary" onClick={handleEnableTwoFactor}>
                      启用
                    </Button>
                  )}
                </Col>
              </Row>
              <Paragraph type="secondary" style={{ fontSize: 12, margin: 0 }}>
                启用后，登录时需要输入动态验证码，大幅提升账号安全性
              </Paragraph>
            </div>

            <Divider />

            {/* 登录通知 */}
            <div>
              <Row justify="space-between" align="middle">
                <Col>
                  <Space>
                    <SafetyOutlined style={{ fontSize: 18 }} />
                    <div>
                      <div>
                        <Text strong>登录通知</Text>
                      </div>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        有新设备登录时发送通知
                      </Text>
                    </div>
                  </Space>
                </Col>
                <Col>
                  <Switch defaultChecked />
                </Col>
              </Row>
            </div>
          </Space>
        </Card>
      ),
    },
    {
      key: 'devices',
      label: (
        <span>
          <DesktopOutlined /> 登录设备
        </span>
      ),
      children: (
        <Card>
          <Alert
            message="安全提示"
            description="如果发现陌生设备，请立即踢出并修改密码。"
            type="warning"
            showIcon
            style={{ marginBottom: 16 }}
          />
          <Table
            columns={deviceColumns}
            dataSource={loginDevices}
            rowKey="id"
            pagination={false}
          />
        </Card>
      ),
    },
    {
      key: 'info',
      label: (
        <span>
          <InfoCircleOutlined /> 账号信息
        </span>
      ),
      children: (
        <Card>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Row gutter={[16, 16]}>
              <Col span={24}>
                <Title level={5}>基本信息</Title>
              </Col>
              <Col span={12}>
                <Text type="secondary">用户名：</Text>
                <Text strong>{userProfile?.username}</Text>
              </Col>
              <Col span={12}>
                <Text type="secondary">用户ID：</Text>
                <Text strong>{userProfile?.id}</Text>
              </Col>
              <Col span={12}>
                <Text type="secondary">注册时间：</Text>
                <Text>{userProfile?.createdAt ? new Date(userProfile.createdAt).toLocaleString() : '-'}</Text>
              </Col>
              <Col span={12}>
                <Text type="secondary">最后登录：</Text>
                <Text>{userProfile?.lastLoginAt ? new Date(userProfile.lastLoginAt).toLocaleString() : '-'}</Text>
              </Col>
            </Row>

            <Divider />

            <Row gutter={[16, 16]}>
              <Col span={24}>
                <Title level={5}>角色权限</Title>
              </Col>
              <Col span={24}>
                <Text type="secondary">角色：</Text>
                <Space wrap style={{ marginLeft: 8 }}>
                  {currentUser?.roles?.map((role) => (
                    <Tag color="blue" key={role}>
                      {role}
                    </Tag>
                  ))}
                </Space>
              </Col>
              <Col span={24}>
                <Text type="secondary">权限数量：</Text>
                <Text strong style={{ marginLeft: 8 }}>
                  {currentUser?.permissions?.length || 0} 个
                </Text>
              </Col>
            </Row>
          </Space>
        </Card>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <Card
        title={
          <Space>
            <UserOutlined />
            <span>个人中心</span>
          </Space>
        }
        loading={isLoading}
      >
        <Tabs defaultActiveKey="profile" items={tabItems} />
      </Card>

      {/* 邮箱绑定弹窗 */}
      <Modal
        title="绑定邮箱"
        open={emailBindModalVisible}
        onCancel={() => {
          setEmailBindModalVisible(false);
          emailForm.resetFields();
          setEmailCodeSent(false);
        }}
        footer={null}
        width={500}
      >
        <Form form={emailForm} layout="vertical">
          <Form.Item
            label="邮箱地址"
            name="email"
            rules={[
              { required: true, message: '请输入邮箱地址！' },
              { type: 'email', message: '请输入有效的邮箱地址！' },
            ]}
          >
            <Input prefix={<MailOutlined />} placeholder="请输入邮箱地址" />
          </Form.Item>

          <Form.Item label="验证码" required>
            <Space.Compact style={{ width: '100%' }}>
              <Form.Item
                name="code"
                noStyle
                rules={[{ required: true, message: '请输入验证码！' }]}
              >
                <Input placeholder="请输入验证码" />
              </Form.Item>
              <Button
                onClick={sendEmailCode}
                disabled={countdown > 0}
              >
                {countdown > 0 ? `${countdown}秒后重试` : '发送验证码'}
              </Button>
            </Space.Compact>
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" onClick={handleBindEmail}>
                确认绑定
              </Button>
              <Button onClick={() => setEmailBindModalVisible(false)}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 手机号绑定弹窗 */}
      <Modal
        title="绑定手机号"
        open={phoneBindModalVisible}
        onCancel={() => {
          setPhoneBindModalVisible(false);
          phoneForm.resetFields();
          setPhoneCodeSent(false);
        }}
        footer={null}
        width={500}
      >
        <Form form={phoneForm} layout="vertical">
          <Form.Item
            label="手机号"
            name="phone"
            rules={[
              { required: true, message: '请输入手机号！' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号！' },
            ]}
          >
            <Input prefix={<PhoneOutlined />} placeholder="请输入手机号" />
          </Form.Item>

          <Form.Item label="验证码" required>
            <Space.Compact style={{ width: '100%' }}>
              <Form.Item
                name="code"
                noStyle
                rules={[{ required: true, message: '请输入验证码！' }]}
              >
                <Input placeholder="请输入验证码" />
              </Form.Item>
              <Button
                onClick={sendPhoneCode}
                disabled={countdown > 0}
              >
                {countdown > 0 ? `${countdown}秒后重试` : '发送验证码'}
              </Button>
            </Space.Compact>
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" onClick={handleBindPhone}>
                确认绑定
              </Button>
              <Button onClick={() => setPhoneBindModalVisible(false)}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 两步验证设置弹窗 */}
      <Modal
        title="启用两步验证"
        open={twoFactorModalVisible}
        onCancel={() => {
          setTwoFactorModalVisible(false);
          setTwoFactorStep(0);
        }}
        footer={null}
        width={600}
      >
        <Steps current={twoFactorStep} style={{ marginBottom: 24 }}>
          <Step title="扫描二维码" />
          <Step title="输入验证码" />
          <Step title="完成设置" />
        </Steps>

        {twoFactorStep === 0 && (
          <Space direction="vertical" align="center" style={{ width: '100%' }}>
            <Paragraph>
              请使用 Google Authenticator 或其他 TOTP 应用扫描下方二维码：
            </Paragraph>
            <QRCode
              value={`otpauth://totp/CampusMarketplace:${userProfile?.username}?secret=${twoFactorSecret}&issuer=CampusMarketplace`}
              size={200}
            />
            <Paragraph type="secondary">
              密钥（手动输入）：<Text code copyable>{twoFactorSecret}</Text>
            </Paragraph>
            <Button type="primary" onClick={() => setTwoFactorStep(1)}>
              下一步
            </Button>
          </Space>
        )}

        {twoFactorStep === 1 && (
          <Space direction="vertical" style={{ width: '100%' }}>
            <Paragraph>
              请输入 Google Authenticator 中显示的 6 位数字验证码：
            </Paragraph>
            <Form layout="vertical">
              <Form.Item label="验证码" required>
                <Input placeholder="请输入6位验证码" maxLength={6} />
              </Form.Item>
              <Form.Item>
                <Space>
                  <Button type="primary" onClick={handleConfirmTwoFactor}>
                    验证并启用
                  </Button>
                  <Button onClick={() => setTwoFactorStep(0)}>
                    上一步
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          </Space>
        )}
      </Modal>
    </div>
  );
};

export default ProfilePage;
