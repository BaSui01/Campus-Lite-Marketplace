/**
 * 限流管理页面
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React, { useState } from 'react';
import {
  Card,
  Switch,
  Table,
  Button,
  Input,
  Space,
  Modal,
  Form,
  message,
  Typography,
  Tag,
  Divider,
  Alert,
} from 'antd';
import {
  PlusOutlined,
  DeleteOutlined,
  UserOutlined,
  GlobalOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { rateLimitService } from '@campus/shared';
import { PermissionGuard } from '@/components';
import { PERMISSION_CODES } from '@campus/shared';
import type { RateLimitRules } from '@campus/shared';

const { Title } = Typography;

const RateLimit: React.FC = () => {
  const queryClient = useQueryClient();
  const [userWhitelistModalVisible, setUserWhitelistModalVisible] = useState(false);
  const [ipWhitelistModalVisible, setIpWhitelistModalVisible] = useState(false);
  const [ipBlacklistModalVisible, setIpBlacklistModalVisible] = useState(false);
  const [userWhitelistForm] = Form.useForm();
  const [ipWhitelistForm] = Form.useForm();
  const [ipBlacklistForm] = Form.useForm();

  // ===== 查询限流规则 =====
  const { data: rules, isLoading } = useQuery({
    queryKey: ['rate-limit-rules'],
    queryFn: () => rateLimitService.getRules(),
  });

  // ===== 切换限流开关 =====
  const toggleEnabledMutation = useMutation({
    mutationFn: (enabled: boolean) => rateLimitService.setEnabled(enabled),
    onSuccess: (_, enabled) => {
      message.success(`限流已${enabled ? '开启' : '关闭'}！🎉`);
      queryClient.invalidateQueries({ queryKey: ['rate-limit-rules'] });
    },
    onError: (error: any) => {
      message.error(`操作失败：${error.message} 😰`);
    },
  });

  // ===== 用户白名单操作 =====
  const addUserWhitelistMutation = useMutation({
    mutationFn: (userId: number) => rateLimitService.addUserWhitelist(userId),
    onSuccess: () => {
      message.success('用户已加入白名单！🎉');
      queryClient.invalidateQueries({ queryKey: ['rate-limit-rules'] });
      setUserWhitelistModalVisible(false);
      userWhitelistForm.resetFields();
    },
    onError: (error: any) => {
      message.error(`操作失败：${error.message} 😰`);
    },
  });

  const removeUserWhitelistMutation = useMutation({
    mutationFn: (userId: number) => rateLimitService.removeUserWhitelist(userId),
    onSuccess: () => {
      message.success('用户已从白名单移除！🎉');
      queryClient.invalidateQueries({ queryKey: ['rate-limit-rules'] });
    },
    onError: (error: any) => {
      message.error(`操作失败：${error.message} 😰`);
    },
  });

  // ===== IP白名单操作 =====
  const addIpWhitelistMutation = useMutation({
    mutationFn: (ip: string) => rateLimitService.addIpWhitelist(ip),
    onSuccess: () => {
      message.success('IP已加入白名单！🎉');
      queryClient.invalidateQueries({ queryKey: ['rate-limit-rules'] });
      setIpWhitelistModalVisible(false);
      ipWhitelistForm.resetFields();
    },
    onError: (error: any) => {
      message.error(`操作失败：${error.message} 😰`);
    },
  });

  const removeIpWhitelistMutation = useMutation({
    mutationFn: (ip: string) => rateLimitService.removeIpWhitelist(ip),
    onSuccess: () => {
      message.success('IP已从白名单移除！🎉');
      queryClient.invalidateQueries({ queryKey: ['rate-limit-rules'] });
    },
    onError: (error: any) => {
      message.error(`操作失败：${error.message} 😰`);
    },
  });

  // ===== IP黑名单操作 =====
  const addIpBlacklistMutation = useMutation({
    mutationFn: (ip: string) => rateLimitService.addIpBlacklist(ip),
    onSuccess: () => {
      message.success('IP已加入黑名单！🎉');
      queryClient.invalidateQueries({ queryKey: ['rate-limit-rules'] });
      setIpBlacklistModalVisible(false);
      ipBlacklistForm.resetFields();
    },
    onError: (error: any) => {
      message.error(`操作失败：${error.message} 😰`);
    },
  });

  const removeIpBlacklistMutation = useMutation({
    mutationFn: (ip: string) => rateLimitService.removeIpBlacklist(ip),
    onSuccess: () => {
      message.success('IP已从黑名单移除！🎉');
      queryClient.invalidateQueries({ queryKey: ['rate-limit-rules'] });
    },
    onError: (error: any) => {
      message.error(`操作失败：${error.message} 😰`);
    },
  });

  // ===== 用户白名单表格列 =====
  const userWhitelistColumns: ColumnsType<{ userId: number; userIdFormatted: string }> = [
    {
      title: '用户ID',
      dataIndex: 'userIdFormatted',
      key: 'userIdFormatted',
      render: (_, record) => (
        <Space>
          <UserOutlined />
          <span>用户 {record.userIdFormatted}</span>
        </Space>
      ),
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        <Button
          type="link"
          danger
          size="small"
          icon={<DeleteOutlined />}
          onClick={() => removeUserWhitelistMutation.mutate(record.userId)}
          loading={removeUserWhitelistMutation.isPending}
        >
          移除
        </Button>
      ),
    },
  ];

  // ===== IP白名单表格列 =====
  const ipWhitelistColumns: ColumnsType<{ ip: string }> = [
    {
      title: 'IP地址',
      dataIndex: 'ip',
      key: 'ip',
      render: (ip) => (
        <Space>
          <GlobalOutlined />
          <Tag color="blue">{ip}</Tag>
        </Space>
      ),
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        <Button
          type="link"
          danger
          size="small"
          icon={<DeleteOutlined />}
          onClick={() => removeIpWhitelistMutation.mutate(record.ip)}
          loading={removeIpWhitelistMutation.isPending}
        >
          移除
        </Button>
      ),
    },
  ];

  // ===== IP黑名单表格列 =====
  const ipBlacklistColumns: ColumnsType<{ ip: string }> = [
    {
      title: 'IP地址',
      dataIndex: 'ip',
      key: 'ip',
      render: (ip) => (
        <Space>
          <GlobalOutlined />
          <Tag color="red">{ip}</Tag>
        </Space>
      ),
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        <Button
          type="link"
          danger
          size="small"
          icon={<DeleteOutlined />}
          onClick={() => removeIpBlacklistMutation.mutate(record.ip)}
          loading={removeIpBlacklistMutation.isPending}
        >
          移除
        </Button>
      ),
    },
  ];

  // ===== 表单提交处理 =====
  const handleUserWhitelistSubmit = () => {
    userWhitelistForm.validateFields().then((values) => {
      addUserWhitelistMutation.mutate(values.userId);
    });
  };

  const handleIpWhitelistSubmit = () => {
    ipWhitelistForm.validateFields().then((values) => {
      addIpWhitelistMutation.mutate(values.ip);
    });
  };

  const handleIpBlacklistSubmit = () => {
    ipBlacklistForm.validateFields().then((values) => {
      addIpBlacklistMutation.mutate(values.ip);
    });
  };

  return (
    <div className="rate-limit" style={{ padding: '24px' }}>
      <PermissionGuard permission={PERMISSION_CODES.SYSTEM_RATE_LIMIT_MANAGE}>
        <Card loading={isLoading}>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Title level={2}>🛡️ 限流管理</Title>

            {/* 限流开关 */}
            <Card title="限流开关" size="small">
              <Space>
                <Switch
                  checked={rules?.enabled || false}
                  onChange={(checked) => toggleEnabledMutation.mutate(checked)}
                  loading={toggleEnabledMutation.isPending}
                />
                <span>系统限流功能</span>
                {!rules?.enabled && (
                  <Alert
                    message="限流已关闭，系统将不受访问频率限制"
                    type="warning"
                    showIcon
                    style={{ marginLeft: 16 }}
                  />
                )}
              </Space>
            </Card>

            {/* 用户白名单 */}
            <Card 
              title={
                <Space>
                  <span>用户白名单</span>
                  <Button
                    type="primary"
                    size="small"
                    icon={<PlusOutlined />}
                    onClick={() => setUserWhitelistModalVisible(true)}
                  >
                    添加用户
                  </Button>
                </Space>
              }
              size="small"
            >
              <Table
                columns={userWhitelistColumns}
                dataSource={rules?.userWhitelist?.map((userId, index) => ({
                  key: index,
                  userId,
                  userIdFormatted: userId.toString(),
                })) || []}
                pagination={false}
                size="small"
                locale={{ emptyText: '暂无白名单用户' }}
              />
            </Card>

            <Divider />

            {/* IP白名单 */}
            <Card 
              title={
                <Space>
                  <span>IP白名单</span>
                  <Button
                    type="primary"
                    size="small"
                    icon={<PlusOutlined />}
                    onClick={() => setIpWhitelistModalVisible(true)}
                  >
                    添加IP
                  </Button>
                </Space>
              }
              size="small"
            >
              <Table
                columns={ipWhitelistColumns}
                dataSource={rules?.ipWhitelist?.map((ip, index) => ({
                  key: index,
                  ip,
                })) || []}
                pagination={false}
                size="small"
                locale={{ emptyText: '暂无白名单IP' }}
              />
            </Card>

            {/* IP黑名单 */}
            <Card 
              title={
                <Space>
                  <span>IP黑名单</span>
                  <Button
                    type="primary"
                    size="small"
                    icon={<PlusOutlined />}
                    onClick={() => setIpBlacklistModalVisible(true)}
                  >
                    添加IP
                  </Button>
                </Space>
              }
              size="small"
            >
              <Table
                columns={ipBlacklistColumns}
                dataSource={rules?.ipBlacklist?.map((ip, index) => ({
                  key: index,
                  ip,
                })) || []}
                pagination={false}
                size="small"
                locale={{ emptyText: '暂无黑名单IP' }}
              />
            </Card>
          </Space>
        </Card>
      </PermissionGuard>

      {/* 添加用户到白名单 */}
      <Modal
        title="添加用户到白名单"
        open={userWhitelistModalVisible}
        onOk={handleUserWhitelistSubmit}
        onCancel={() => {
          setUserWhitelistModalVisible(false);
          userWhitelistForm.resetFields();
        }}
        confirmLoading={addUserWhitelistMutation.isPending}
      >
        <Form form={userWhitelistForm} layout="vertical">
          <Form.Item
            name="userId"
            label="用户ID"
            rules={[
              { required: true, message: '请输入用户ID！' },
              { type: 'number', message: '请输入有效的数字ID！' },
            ]}
          >
            <Input placeholder="请输入用户ID" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 添加IP到白名单 */}
      <Modal
        title="添加IP到白名单"
        open={ipWhitelistModalVisible}
        onOk={handleIpWhitelistSubmit}
        onCancel={() => {
          setIpWhitelistModalVisible(false);
          ipWhitelistForm.resetFields();
        }}
        confirmLoading={addIpWhitelistMutation.isPending}
      >
        <Form form={ipWhitelistForm} layout="vertical">
          <Form.Item
            name="ip"
            label="IP地址"
            rules={[
              { required: true, message: '请输入IP地址！' },
              { pattern: /^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/, message: '请输入有效的IP地址！' },
            ]}
          >
            <Input placeholder="例如: 192.168.1.100" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 添加IP到黑名单 */}
      <Modal
        title="添加IP到黑名单"
        open={ipBlacklistModalVisible}
        onOk={handleIpBlacklistSubmit}
        onCancel={() => {
          setIpBlacklistModalVisible(false);
          ipBlacklistForm.resetFields();
        }}
        confirmLoading={addIpBlacklistMutation.isPending}
      >
        <Form form={ipBlacklistForm} layout="vertical">
          <Form.Item
            name="ip"
            label="IP地址"
            rules={[
              { required: true, message: '请输入IP地址！' },
              { pattern: /^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/, message: '请输入有效的IP地址！' },
            ]}
          >
            <Input placeholder="例如: 192.168.1.100" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default RateLimit;
