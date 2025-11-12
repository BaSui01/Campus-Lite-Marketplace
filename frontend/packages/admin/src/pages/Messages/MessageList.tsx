/**
 * 消息管理列表页
 * 
 * 功能：
 * - 分页查询系统消息（管理员视角）
 * - 搜索消息（关键词、用户）
 * - 消息类型筛选（文本、图片、商品卡片）
 * - 时间范围筛选
 * - 消息统计卡片
 * - 查看会话详情
 * - 搜索消息内容
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Table,
  Button,
  Input,
  Select,
  Space,
  Tag,
  Card,
  Row,
  Col,
  Statistic,
  DatePicker,
  message,
  Avatar,
} from 'antd';
import {
  SearchOutlined,
  EyeOutlined,
  MessageOutlined,
  UserOutlined,
  PictureOutlined,
  ShoppingOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { messageService } from '@campus/shared';
import type { ConversationResponse } from '@campus/shared/api';
import dayjs, { Dayjs } from 'dayjs';

const { Option } = Select;
const { RangePicker } = DatePicker;

/**
 * 消息类型映射
 */
const MESSAGE_TYPE_MAP: Record<string, { text: string; color: string; icon: React.ReactNode }> = {
  TEXT: { 
    text: '文本消息', 
    color: 'blue',
    icon: <MessageOutlined />
  },
  IMAGE: { 
    text: '图片消息', 
    color: 'green',
    icon: <PictureOutlined />
  },
  GOODS_CARD: { 
    text: '商品卡片', 
    color: 'orange',
    icon: <ShoppingOutlined />
  },
};

export const MessageList: React.FC = () => {
  const navigate = useNavigate();

  // 查询参数
  const [keyword, setKeyword] = useState<string>('');
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(20);

  // 构建查询参数
  const queryParams = {
    keyword,
    page,
    size,
  };

  // 查询消息列表（使用会话列表API - 管理员视角需要看所有会话）
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['messages', 'admin', 'conversations', queryParams],
    queryFn: () => messageService.getConversations({ page, size }),
    staleTime: 5 * 60 * 1000, // 缓存5分钟
  });

  // 搜索处理
  const handleSearch = () => {
    setPage(0);
    refetch();
  };

  // 重置筛选
  const handleReset = () => {
    setKeyword('');
    setPage(0);
  };

  // 查看会话详情
  const handleView = (conversationId: number) => {
    navigate(`/admin/messages/${conversationId}`);
  };

  // 表格列定义
  const columns = [
    {
      title: '会话ID',
      dataIndex: 'id',
      key: 'id',
      width: 100,
    },
    {
      title: '参与用户',
      key: 'users',
      width: 200,
      render: (_: unknown, record: ConversationResponse) => (
        <Space>
          <Avatar icon={<UserOutlined />} />
          <span>{record.otherUserUsername || '未知用户'}</span>
        </Space>
      ),
    },
    {
      title: '最后消息',
      dataIndex: 'lastMessage',
      key: 'lastMessage',
      width: 300,
      ellipsis: true,
      render: (lastMessage: string) => lastMessage || '-',
    },
    {
      title: '消息类型',
      dataIndex: 'lastMessageType',
      key: 'lastMessageType',
      width: 120,
      render: (type: string) => {
        const config = MESSAGE_TYPE_MAP[type] || { text: type, color: 'default', icon: null };
        return (
          <Tag color={config.color} icon={config.icon}>
            {config.text}
          </Tag>
        );
      },
    },
    {
      title: '未读数',
      dataIndex: 'unreadCount',
      key: 'unreadCount',
      width: 100,
      render: (count: number) => (
        <Tag color={count > 0 ? 'red' : 'default'}>
          {count}
        </Tag>
      ),
    },
    {
      title: '最后消息时间',
      dataIndex: 'lastMessageTime',
      key: 'lastMessageTime',
      width: 180,
      render: (time: string) => time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (createdAt: string) => dayjs(createdAt).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right' as const,
      width: 100,
      render: (_: unknown, record: ConversationResponse) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleView(record.id!)}
          >
            查看
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总会话数"
              value={data?.totalElements || 0}
              prefix={<MessageOutlined />}
              suffix="个"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="活跃会话"
              value={data?.content?.filter((c: ConversationResponse) => c.unreadCount && c.unreadCount > 0).length || 0}
              prefix={<MessageOutlined />}
              suffix="个"
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="总未读数"
              value={data?.content?.reduce((sum: number, c: ConversationResponse) => sum + (c.unreadCount || 0), 0) || 0}
              prefix={<MessageOutlined />}
              suffix="条"
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="今日消息"
              value={0}
              prefix={<MessageOutlined />}
              suffix="条"
            />
          </Card>
        </Col>
      </Row>

      {/* 搜索筛选区域 */}
      <Card style={{ marginBottom: 24 }}>
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <Space wrap>
            <Input
              placeholder="搜索用户名/消息内容"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onPressEnter={handleSearch}
              style={{ width: 300 }}
              prefix={<SearchOutlined />}
            />

            <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
              搜索
            </Button>

            <Button onClick={handleReset}>重置</Button>
          </Space>
        </Space>
      </Card>

      {/* 数据表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={data?.content || []}
          loading={isLoading}
          rowKey="id"
          scroll={{ x: 1300 }}
          pagination={{
            current: page + 1,
            pageSize: size,
            total: data?.totalElements || 0,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
            onChange: (newPage, newSize) => {
              setPage(newPage - 1);
              setSize(newSize!);
            },
          }}
        />
      </Card>
    </div>
  );
};

export default MessageList;
