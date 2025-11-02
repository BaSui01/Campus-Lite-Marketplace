/**
 * 用户详情页面
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  Row,
  Col,
  Descriptions,
  Tag,
  Button,
  Space,
  Image,
  Modal,
  Form,
  Input,
  DatePicker,
  message,
  Tooltip,
} from 'antd';
import {
  ArrowLeftOutlined,
  EditOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userService, adminUserService } from '@campus/shared';
import { UserAvatar } from '@campus/shared';
import dayjs from 'dayjs';
import type { User } from '@campus/shared';

const UserDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [banModalVisible, setBanModalVisible] = useState(false);
  const [roleModalVisible, setRoleModalVisible] = useState(false);
  const [banForm] = Form.useForm();
  const [roleForm] = Form.useForm();

  // ===== 查询用户详情 =====
  const { data: user, isLoading } = useQuery({
    queryKey: ['user-detail', id],
    queryFn: async () => {
      const response = await userService.getUserById(Number(id));
      return response.data;
    },
    enabled: !!id,
  });

  // ===== 封禁用户 Mutation =====
  const banMutation = useMutation({
    mutationFn: async (payload: { userId: number; reason: string; days: number }) => {
      await adminUserService.banUser(payload);
    },
    onSuccess: () => {
      message.success('封禁成功！😎');
      queryClient.invalidateQueries({ queryKey: ['user-detail', id] });
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setBanModalVisible(false);
      banForm.resetFields();
    },
    onError: (error: any) => {
      message.error(`封禁失败：${error.message} 😰`);
    },
  });

  // ===== 解封用户 Mutation =====
  const unbanMutation = useMutation({
    mutationFn: async (userId: number) => {
      await adminUserService.unbanUser(userId);
    },
    onSuccess: () => {
      message.success('解封成功！🎉');
      queryClient.invalidateQueries({ queryKey: ['user-detail', id] });
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: (error: any) => {
      message.error(`解封失败：${error.message} 😰`);
    },
  });

  // ===== 分配角色 Mutation =====
  const assignRoleMutation = useMutation({
    mutationFn: async (payload: { userId: number; roles: string[] }) => {
      await adminUserService.assignUserRoles(payload);
    },
    onSuccess: () => {
      message.success('角色分配成功！🎉');
      queryClient.invalidateQueries({ queryKey: ['user-detail', id] });
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setRoleModalVisible(false);
      roleForm.resetFields();
    },
    onError: (error: any) => {
      message.error(`角色分配失败：${error.message} 😰`);
    },
  });

  // ===== 确认封禁 =====
  const handleBanSubmit = () => {
    banForm.validateFields().then((values) => {
      if (!user) return;

      const days = values.bannedUntil
        ? Math.max(dayjs(values.bannedUntil).endOf('day').diff(dayjs(), 'day'), 0)
        : 0;

      banMutation.mutate({
        userId: user.id,
        reason: values.reason,
        days,
      });
    });
  };

  // ===== 确认解封 =====
  const handleUnbanUser = () => {
    if (!user) return;
    
    Modal.confirm({
      title: '确认解封用户？',
      content: `确定要解封用户 "${user.nickname}" 吗？`,
      onOk: () => unbanMutation.mutate(user.id),
    });
  };

  // ===== 确认分配角色 =====
  const handleRoleSubmit = () => {
    if (!user) return;
    
    roleForm.validateFields().then((values) => {
      assignRoleMutation.mutate({
        userId: user.id,
        roles: values.roles,
      });
    });
  };

  if (isLoading) {
    return <div>加载中...</div>;
  }

  if (!user) {
    return <div>用户不存在</div>;
  }

  return (
    <div style={{ padding: '24px' }}>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        {/* 头部操作栏 */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Space>
            <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/admin/users/list')}>
              返回列表
            </Button>
          </Space>
          
          <Space>
            {user.status === 'BANNED' ? (
              <Button
                type="primary"
                onClick={handleUnbanUser}
                loading={unbanMutation.isPending}
              >
                解封用户
              </Button>
            ) : (
              <Button
                danger
                onClick={() => setBanModalVisible(true)}
              >
                封禁用户
              </Button>
            )}
            <Button
              type="default"
              icon={<EditOutlined />}
              onClick={() => setRoleModalVisible(true)}
            >
              分配角色
            </Button>
          </Space>
        </div>

        {/* 用户基本信息 */}
        <Row gutter={[24, 24]}>
          <Col xs={24} md={8}>
            <Card title="基本信息" loading={isLoading}>
              <div style={{ textAlign: 'center', marginBottom: '24px' }}>
                <UserAvatar
                  src={user.avatar}
                  alt={user.nickname}
                  size={100}
                />
              </div>
              <Descriptions column={1} size="small">
                <Descriptions.Item label="用户名">{user.username}</Descriptions.Item>
                <Descriptions.Item label="昵称">{user.nickname}</Descriptions.Item>
                <Descriptions.Item label="学号">{user.studentId}</Descriptions.Item>
                <Descriptions.Item label="校区">{user.campusName}</Descriptions.Item>
                <Descriptions.Item label="状态">
                  <Tag color={user.status === 'BANNED' ? 'red' : 'green'}>
                    {user.status === 'BANNED' ? '已封禁' : '正常'}
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="注册时间">
                  {dayjs(user.createdAt).format('YYYY-MM-DD HH:mm:ss')}
                </Descriptions.Item>
              </Descriptions>
            </Card>
          </Col>

          <Col xs={24} md={8}>
            <Card title="角色权限" loading={isLoading}>
              <div style={{ marginBottom: '16px' }}>
                <strong>当前角色：</strong>
              </div>
              <Space wrap style={{ marginBottom: '24px' }}>
                {user.roles?.map((role) => (
                  <Tag key={role} color="blue">
                    {role}
                  </Tag>
                )) || <span style={{ color: '#999' }}>暂无角色</span>}
              </Space>
              
              <div style={{ marginBottom: '16px' }}>
                <strong>权限列表：</strong>
              </div>
              <Space wrap>
                {user.permissions?.map((permission) => (
                  <Tooltip key={permission} title={permission}>
                    <Tag color="geekblue" style={{ cursor: 'pointer' }}>
                      {permission.split(':').pop()}
                    </Tag>
                  </Tooltip>
                )) || <span style={{ color: '#999' }}>暂无权限</span>}
              </Space>
            </Card>
          </Col>

          <Col xs={24} md={8}>
            <Card title="封禁信息" loading={isLoading}>
              {user.status === 'BANNED' ? (
                <div>
                  <Descriptions column={1} size="small">
                    <Descriptions.Item label="封禁原因">
                      {user.banReason || '未填写'}
                    </Descriptions.Item>
                    <Descriptions.Item label="封禁时间">
                      {user.bannedAt ? dayjs(user.bannedAt).format('YYYY-MM-DD HH:mm:ss') : '未知'}
                    </Descriptions.Item>
                    <Descriptions.Item label="解封时间">
                      {user.bannedUntil ? dayjs(user.bannedUntil).format('YYYY-MM-DD HH:mm:ss') : '永久'}
                    </Descriptions.Item>
                  </Descriptions>
                </div>
              ) : (
                <div style={{ textAlign: 'center', color: '#999', padding: '20px' }}>
                  <UserOutlined style={{ fontSize: '48px', marginBottom: '16px' }} />
                  <div>用户正常，未被封禁</div>
                </div>
              )}
            </Card>
          </Col>
        </Row>

        {/* 用户头像展示 */}
        {user.avatar && (
          <Card title="用户头像">
            <div style={{ textAlign: 'center' }}>
              <Image
                src={user.avatar}
                alt={user.nickname}
                style={{ maxWidth: '200px', maxHeight: '200px' }}
                placeholder={<UserAvatar alt={user.nickname} size={100} />}
              />
            </div>
          </Card>
        )}
      </Space>

      {/* 封禁用户弹窗 */}
      <Modal
        title={`封禁用户：${user.nickname}`}
        open={banModalVisible}
        onOk={handleBanSubmit}
        onCancel={() => {
          setBanModalVisible(false);
          banForm.resetFields();
        }}
        confirmLoading={banMutation.isPending}
      >
        <Form form={banForm} layout="vertical">
          <Form.Item
            name="reason"
            label="封禁原因"
            rules={[{ required: true, message: '请输入封禁原因！' }]}
          >
            <Input.TextArea rows={4} placeholder="请输入封禁原因" />
          </Form.Item>
          <Form.Item
            name="bannedUntil"
            label="封禁截止时间"
            extra="不选择则永久封禁"
          >
            <DatePicker
              showTime
              format="YYYY-MM-DD HH:mm:ss"
              style={{ width: '100%' }}
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* 分配角色弹窗 */}
      <Modal
        title={`分配角色：${user.nickname}`}
        open={roleModalVisible}
        onOk={handleRoleSubmit}
        onCancel={() => {
          setRoleModalVisible(false);
          roleForm.resetFields();
        }}
        confirmLoading={assignRoleMutation.isPending}
      >
        <Form form={roleForm} layout="vertical" initialValues={{ roles: user.roles }}>
          <Form.Item
            name="roles"
            label="选择角色"
            rules={[{ required: true, message: '请选择至少一个角色！' }]}
          >
            {/* 这里应该从后端获取角色列表，暂时使用静态数据 */}
            <Input.TextArea rows={4} placeholder="请输入角色名称，多个角色用逗号分隔" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default UserDetail;
