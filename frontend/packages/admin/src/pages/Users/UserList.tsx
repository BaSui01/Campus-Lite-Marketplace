/**
 * 用户列表页面
 *
 * 功能：
 * - 用户列表展示（表格 + 分页）
 * - 搜索/筛选（昵称、学号、状态）
 * - 封禁/解封用户
 * - 查看用户详情
 *
 * @author BaSui 😎
 * @date 2025-11-01
 */

import React, { useState } from 'react';
import {
  Table,
  Card,
  Input,
  Button,
  Tag,
  Space,
  Avatar,
  Modal,
  Form,
  DatePicker,
  message,
  Tooltip,
} from 'antd';
import {
  SearchOutlined,
  StopOutlined,
  CheckCircleOutlined,
  EyeOutlined,
  UserOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { userService, adminUserService } from '@campus/shared';
import type { User, UserListQuery } from '@campus/shared';
import dayjs from 'dayjs';
import './UserList.css';

const { Search } = Input;
const { TextArea } = Input;

const UserList: React.FC = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useState<UserListQuery>({
    page: 0,
    pageSize: 20,
  });
  const [banModalVisible, setBanModalVisible] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [banForm] = Form.useForm();

  // ===== 查询用户列表 =====
  const { data, isLoading } = useQuery({
    queryKey: ['users', searchParams],
    queryFn: async () => {
      const response = await userService.getUserList(searchParams);
      return response.data;
    },
  });

  // ===== 封禁用户 Mutation =====
  const banMutation = useMutation({
    mutationFn: async (payload: { userId: number; reason: string; days: number }) => {
      await adminUserService.banUser(payload);
    },
    onSuccess: () => {
      message.success('封禁成功！😎');
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
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: (error: any) => {
      message.error(`解封失败：${error.message} 😰`);
    },
  });

  // ===== 处理搜索 =====
  const handleSearch = (keyword: string) => {
    setSearchParams({ ...searchParams, keyword, page: 0 });
  };

  // ===== 处理分页 =====
  const handleTableChange = (pagination: any) => {
    setSearchParams({
      ...searchParams,
      page: pagination.current - 1,
      pageSize: pagination.pageSize,
    });
  };

  // ===== 打开封禁弹窗 =====
  const handleBanUser = (user: User) => {
    setSelectedUser(user);
    setBanModalVisible(true);
  };

  // ===== 确认封禁 =====
  const handleBanSubmit = () => {
    banForm.validateFields().then((values) => {
      if (!selectedUser) return;

      const days = values.bannedUntil
        ? Math.max(dayjs(values.bannedUntil).endOf('day').diff(dayjs(), 'day'), 0)
        : 0;

      banMutation.mutate({
        userId: selectedUser.id,
        reason: values.reason,
        days,
      });
    });
  };

  // ===== 确认解封 =====
  const handleUnbanUser = (user: User) => {
    Modal.confirm({
      title: '确认解封用户？',
      content: `确定要解封用户 "${user.nickname}" 吗？`,
      onOk: () => unbanMutation.mutate(user.id),
    });
  };

  // ===== 表格列定义 =====
  const columns: ColumnsType<User> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '用户信息',
      key: 'userInfo',
      render: (_, record) => (
        <Space>
          <Avatar src={record.avatar} icon={<UserOutlined />} />
          <div>
            <div>{record.nickname}</div>
            <div style={{ fontSize: 12, color: '#999' }}>{record.username}</div>
          </div>
        </Space>
      ),
    },
    {
      title: '学号',
      dataIndex: 'studentId',
      key: 'studentId',
    },
    {
      title: '校区',
      dataIndex: 'campusName',
      key: 'campusName',
    },
    {
      title: '角色',
      dataIndex: 'roles',
      key: 'roles',
      render: (roles: string[]) => (
        <>
          {roles.map((role) => (
            <Tag key={role} color="blue">
              {role}
            </Tag>
          ))}
        </>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string, record) => {
        if (status === 'BANNED') {
          return (
            <Tooltip title={`原因：${record.banReason}`}>
              <Tag color="red" icon={<StopOutlined />}>
                已封禁
              </Tag>
            </Tooltip>
          );
        }
        return (
          <Tag color="green" icon={<CheckCircleOutlined />}>
            正常
          </Tag>
        );
      },
    },
    {
      title: '注册时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right',
      width: 180,
      render: (_, record) => (
        <Space>
          <Button 
            type="link" 
            size="small" 
            icon={<EyeOutlined />}
            onClick={() => navigate(`/admin/users/${record.id}`)}
          >
            详情
          </Button>
          {record.status === 'BANNED' ? (
            <Button
              type="link"
              size="small"
              onClick={() => handleUnbanUser(record)}
              icon={<CheckCircleOutlined />}
            >
              解封
            </Button>
          ) : (
            <Button
              type="link"
              danger
              size="small"
              onClick={() => handleBanUser(record)}
              icon={<StopOutlined />}
            >
              封禁
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="user-list">
      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          {/* 搜索栏 */}
          <div className="search-bar">
            <Search
              placeholder="搜索昵称、用户名、学号"
              allowClear
              enterButton={<SearchOutlined />}
              size="large"
              onSearch={handleSearch}
              style={{ width: 400 }}
            />
          </div>

          {/* 用户表格 */}
          <Table
            columns={columns}
            dataSource={data?.content || []}
            loading={isLoading}
            pagination={{
              current: (searchParams.page || 0) + 1,
              pageSize: searchParams.pageSize || 20,
              total: data?.totalElements || 0,
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total) => `共 ${total} 个用户`,
            }}
            onChange={handleTableChange}
            scroll={{ x: 1200 }}
          />
        </Space>
      </Card>

      {/* 封禁用户弹窗 */}
      <Modal
        title={`封禁用户：${selectedUser?.nickname}`}
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
            <TextArea rows={4} placeholder="请输入封禁原因" />
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
    </div>
  );
};

export default UserList;
