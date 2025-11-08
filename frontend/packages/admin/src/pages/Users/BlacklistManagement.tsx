/**
 * 黑名单管理页面
 *
 * 功能：
 * - 查看所有黑名单记录（分页）
 * - 按拉黑者/被拉黑者筛选
 * - 批量解除黑名单
 * - 黑名单统计数据
 * - 双向黑名单关系查询
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */

import { useState } from 'react';
import {
  Table,
  Button,
  Input,
  Space,
  Card,
  message,
  Tag,
  Modal,
  Row,
  Col,
  Statistic,
  Typography,
  Tooltip,
} from 'antd';
import {
  SearchOutlined,
  DeleteOutlined,
  UserOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { getApi } from '@campus/shared/utils/apiClient';

const { Text } = Typography;
const { confirm } = Modal;

interface BlacklistRecord {
  id: number;
  userId: number;
  userName: string;
  blockedUserId: number;
  blockedUserName: string;
  createdAt: string;
}

interface BlacklistStatistics {
  totalCount: number;
  activeUsers: number;
  mostBlockedUser: {
    userId: number;
    userName: string;
    blockCount: number;
  };
}

export const BlacklistManagement: React.FC = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(20);
  const [searchUserId, setSearchUserId] = useState<string>('');
  const [searchBlockedUserId, setSearchBlockedUserId] = useState<string>('');
  const [selectedRowKeys, setSelectedRowKeys] = useState<number[]>([]);

  // 查询黑名单列表（调用真实API）
  const { data, isLoading } = useQuery({
    queryKey: ['blacklistRecords', page, size, searchUserId, searchBlockedUserId],
    queryFn: async () => {
      const api = getApi();
      const response = await api.listAllBlacklist(
        searchUserId ? parseInt(searchUserId) : undefined,
        searchBlockedUserId ? parseInt(searchBlockedUserId) : undefined,
        page,
        size
      );
      return response.data.data;
    },
    staleTime: 2 * 60 * 1000,
  });

  // 查询黑名单统计（调用真实API）
  const { data: statistics } = useQuery({
    queryKey: ['blacklistStatistics'],
    queryFn: async (): Promise<BlacklistStatistics> => {
      const api = getApi();
      const response = await api.getBlacklistStatistics();
      return response.data.data as BlacklistStatistics;
    },
    staleTime: 5 * 60 * 1000,
  });

  // 批量解除黑名单 Mutation
  const batchUnblockMutation = useMutation({
    mutationFn: async (ids: number[]) => {
      const api = getApi();
      await api.batchRemoveBlacklist({ ids });
    },
    onSuccess: () => {
      message.success('批量解除黑名单成功！🎉');
      queryClient.invalidateQueries({ queryKey: ['blacklistRecords'] });
      queryClient.invalidateQueries({ queryKey: ['blacklistStatistics'] });
      setSelectedRowKeys([]);
    },
    onError: (error: any) => {
      message.error(`解除失败：${error.message} 😰`);
    },
  });

  // 单个解除黑名单
  const handleUnblock = (record: BlacklistRecord) => {
    confirm({
      title: '确认解除黑名单',
      icon: <ExclamationCircleOutlined />,
      content: (
        <div>
          <p>
            解除后，<Text strong>{record.blockedUserName}</Text> 将不再被{' '}
            <Text strong>{record.userName}</Text> 拉黑。
          </p>
        </div>
      ),
      okText: '确认解除',
      cancelText: '取消',
      onOk: () => {
        batchUnblockMutation.mutate([record.id]);
      },
    });
  };

  // 批量解除黑名单
  const handleBatchUnblock = () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请先选择要解除的黑名单记录！');
      return;
    }

    confirm({
      title: `批量解除黑名单`,
      icon: <ExclamationCircleOutlined />,
      content: `确定要解除选中的 ${selectedRowKeys.length} 条黑名单记录吗？`,
      okText: '确认解除',
      okType: 'danger',
      cancelText: '取消',
      onOk: () => {
        batchUnblockMutation.mutate(selectedRowKeys);
      },
    });
  };

  // 搜索
  const handleSearch = () => {
    setPage(0);
  };

  // 重置筛选
  const handleReset = () => {
    setSearchUserId('');
    setSearchBlockedUserId('');
    setPage(0);
  };

  // 表格列定义
  const columns: ColumnsType<BlacklistRecord> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '拉黑者',
      key: 'user',
      width: 200,
      render: (_, record) => (
        <div>
          <div>
            <UserOutlined style={{ marginRight: 4 }} />
            {record.userName}
          </div>
          <Text type="secondary" style={{ fontSize: 12 }}>
            ID: {record.userId}
          </Text>
        </div>
      ),
    },
    {
      title: '被拉黑者',
      key: 'blockedUser',
      width: 200,
      render: (_, record) => (
        <div>
          <div>
            <UserOutlined style={{ marginRight: 4 }} />
            {record.blockedUserName}
          </div>
          <Text type="secondary" style={{ fontSize: 12 }}>
            ID: {record.blockedUserId}
          </Text>
        </div>
      ),
    },
    {
      title: '拉黑时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (time) => dayjs(time).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '持续天数',
      key: 'duration',
      width: 120,
      render: (_, record) => {
        const days = dayjs().diff(dayjs(record.createdAt), 'day');
        return <Tag color={days > 30 ? 'red' : 'orange'}>{days} 天</Tag>;
      },
    },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right',
      width: 120,
      render: (_, record) => (
        <Button
          type="link"
          danger
          size="small"
          icon={<DeleteOutlined />}
          onClick={() => handleUnblock(record)}
        >
          解除
        </Button>
      ),
    },
  ];

  // 表格行选择配置
  const rowSelection = {
    selectedRowKeys,
    onChange: (keys: React.Key[]) => {
      setSelectedRowKeys(keys as number[]);
    },
  };

  return (
    <div className="blacklist-management" style={{ padding: '24px' }}>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={8}>
          <Card>
            <Statistic title="黑名单总数" value={statistics?.totalCount || 0} prefix={<UserOutlined />} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="活跃拉黑用户"
              value={statistics?.activeUsers || 0}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="被拉黑最多的用户"
              value={statistics?.mostBlockedUser?.blockCount || 0}
              suffix={`次`}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#ff4d4f' }}
            />
            {statistics?.mostBlockedUser && (
              <Text type="secondary" style={{ fontSize: 12 }}>
                {statistics.mostBlockedUser.userName}
              </Text>
            )}
          </Card>
        </Col>
      </Row>

      {/* 主内容卡片 */}
      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          {/* 筛选区域 */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Space wrap>
              <Input
                placeholder="拉黑者 ID"
                prefix={<SearchOutlined />}
                value={searchUserId}
                onChange={(e) => setSearchUserId(e.target.value)}
                onPressEnter={handleSearch}
                style={{ width: 150 }}
              />
              <Input
                placeholder="被拉黑者 ID"
                prefix={<SearchOutlined />}
                value={searchBlockedUserId}
                onChange={(e) => setSearchBlockedUserId(e.target.value)}
                onPressEnter={handleSearch}
                style={{ width: 150 }}
              />
              <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                搜索
              </Button>
              <Button onClick={handleReset}>重置</Button>
            </Space>
            {selectedRowKeys.length > 0 && (
              <Space>
                <Text type="secondary">已选中 {selectedRowKeys.length} 条</Text>
                <Button
                  danger
                  icon={<DeleteOutlined />}
                  onClick={handleBatchUnblock}
                  loading={batchUnblockMutation.isPending}
                >
                  批量解除
                </Button>
              </Space>
            )}
          </div>

          {/* 表格 */}
          <Table
            columns={columns}
            dataSource={data?.content || []}
            rowKey="id"
            loading={isLoading}
            rowSelection={rowSelection}
            scroll={{ x: 1000 }}
            pagination={{
              current: page + 1,
              pageSize: size,
              total: data?.totalElements || 0,
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total) => `共 ${total} 条记录`,
              onChange: (newPage, newSize) => {
                setPage(newPage - 1);
                if (newSize && newSize !== size) {
                  setSize(newSize);
                }
              },
            }}
          />
        </Space>
      </Card>
    </div>
  );
};

export default BlacklistManagement;
