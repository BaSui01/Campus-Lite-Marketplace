/**
 * 封禁记录列表页
 * 
 * 功能：
 * - 分页查询封禁记录
 * - 状态筛选（封禁中/已解封）
 * - 用户搜索
 * - 解封操作
 * - 统计卡片
 * 
 * @author BaSui 😎
 * @date 2025-11-05
 */

import { useState } from 'react';
import {
  Table,
  Button,
  Input,
  Select,
  Space,
  Tag,
  message,
  Modal,
  Form,
  Card,
  Row,
  Col,
  Statistic,
  Avatar,
} from 'antd';
import {
  SearchOutlined,
  UnlockOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { bannedUserService } from '@/services';

const { Option } = Select;
const { TextArea } = Input;

/**
 * 封禁状态映射
 */
const STATUS_MAP: Record<string, { text: string; color: string }> = {
  ACTIVE: { text: '封禁中', color: 'red' },
  UNBANNED: { text: '已解封', color: 'green' },
};

export const BannedUserList: React.FC = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();

  const [keyword, setKeyword] = useState<string>('');
  const [status, setStatus] = useState<string | undefined>();
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(20);

  const [unbanModalVisible, setUnbanModalVisible] = useState(false);
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);

  // 查询封禁记录（调用真实API）
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['bannedUsers', { keyword, status, page, size }],
    queryFn: () => bannedUserService.list({
      userId: keyword ? parseInt(keyword) : undefined,
      isUnbanned: status === 'UNBANNED' ? true : status === 'ACTIVE' ? false : undefined,
      page,
      size,
    }),
    staleTime: 2 * 60 * 1000,
  });

  // 解封用户
  const unbanMutation = useMutation({
    mutationFn: async ({ userId, reason }: { userId: number; reason: string }) => {
      // TODO: 调用实际API
      // await userService.unbanUser(userId, reason);
      await new Promise(resolve => setTimeout(resolve, 500));
    },
    onSuccess: () => {
      message.success('解封成功');
      setUnbanModalVisible(false);
      setCurrentUserId(null);
      form.resetFields();
      refetch();
      queryClient.invalidateQueries({ queryKey: ['bannedUsers'] });
    },
    onError: () => {
      message.error('解封失败');
    },
  });

  const handleSearch = () => {
    setPage(0);
    refetch();
  };

  const handleReset = () => {
    setKeyword('');
    setStatus(undefined);
    setPage(0);
  };

  const handleOpenUnbanModal = (userId: number) => {
    setCurrentUserId(userId);
    form.resetFields();
    setUnbanModalVisible(true);
  };

  const handleUnbanSubmit = async () => {
    if (!currentUserId) return;
    try {
      const values = await form.validateFields();
      unbanMutation.mutate({ userId: currentUserId, reason: values.reason });
    } catch (error) {
      console.error('表单校验失败:', error);
    }
  };

  const columns = [
    {
      title: '用户',
      key: 'user',
      width: 200,
      render: (_: any, record: any) => (
        <Space>
          <Avatar icon={<UserOutlined />} src={record.userAvatar} />
          <span>{record.userName}</span>
        </Space>
      ),
    },
    {
      title: '封禁原因',
      dataIndex: 'banReason',
      key: 'banReason',
      ellipsis: true,
    },
    {
      title: '封禁时长',
      dataIndex: 'banDuration',
      key: 'banDuration',
      width: 100,
      render: (days: number) => `${days}天`,
    },
    {
      title: '操作人',
      dataIndex: 'operatorName',
      key: 'operatorName',
      width: 120,
    },
    {
      title: '封禁时间',
      dataIndex: 'bannedAt',
      key: 'bannedAt',
      width: 180,
      render: (date: string) => new Date(date).toLocaleString('zh-CN'),
    },
    {
      title: '解封时间',
      dataIndex: 'unbannedAt',
      key: 'unbannedAt',
      width: 180,
      render: (date: string | null) => date ? new Date(date).toLocaleString('zh-CN') : '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const info = STATUS_MAP[status];
        return <Tag color={info.color}>{info.text}</Tag>;
      },
    },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right' as const,
      width: 100,
      render: (_: any, record: any) => (
        record.status === 'ACTIVE' && (
          <Button
            type="link"
            size="small"
            icon={<UnlockOutlined />}
            onClick={() => handleOpenUnbanModal(record.userId)}
          >
            解封
          </Button>
        )
      ),
    },
  ];

  const bannedCount = data?.content?.filter((u: any) => u.status === 'ACTIVE').length || 0;
  const unbannedCount = data?.content?.filter((u: any) => u.status === 'UNBANNED').length || 0;

  return (
    <div style={{ padding: 24 }}>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={8}>
          <Card>
            <Statistic title="总封禁数" value={data?.totalElements || 0} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="封禁中" value={bannedCount} valueStyle={{ color: '#f5222d' }} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="已解封" value={unbannedCount} valueStyle={{ color: '#52c41a' }} />
          </Card>
        </Col>
      </Row>

      <Card style={{ marginBottom: 16 }}>
        <Space>
          <Input
            placeholder="搜索用户名"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={handleSearch}
            style={{ width: 200 }}
            prefix={<SearchOutlined />}
          />
          <Select
            placeholder="选择状态"
            value={status}
            onChange={setStatus}
            allowClear
            style={{ width: 120 }}
          >
            <Option value="ACTIVE">封禁中</Option>
            <Option value="UNBANNED">已解封</Option>
          </Select>
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
          </Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>
      </Card>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={data?.content || []}
        loading={isLoading}
        pagination={{
          current: page + 1,
          pageSize: size,
          total: data?.totalElements || 0,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条记录`,
          onChange: (p, s) => {
            setPage(p - 1);
            setSize(s);
          },
        }}
        scroll={{ x: 1200 }}
      />

      <Modal
        title="解封用户"
        open={unbanModalVisible}
        onOk={handleUnbanSubmit}
        onCancel={() => {
          setUnbanModalVisible(false);
          setCurrentUserId(null);
          form.resetFields();
        }}
        confirmLoading={unbanMutation.isPending}
        okText="确认解封"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="reason"
            label="解封原因"
            rules={[
              { required: true, message: '请填写解封原因' },
              { max: 200, message: '解封原因不能超过200字' },
            ]}
          >
            <TextArea
              rows={4}
              placeholder="请填写解封原因（必填，最多200字）"
              showCount
              maxLength={200}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default BannedUserList;
