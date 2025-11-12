/**
 * 性能优化版本的用户列表页面
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React, { useState, useCallback, useMemo } from 'react';
import {
  Table,
  Card,
  Input,
  Button,
  Tag,
  Space,
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
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userService, PERMISSION_CODES } from '@campus/shared';
import { adminUserService } from '@/services';
import { useVirtualList, debounce, useBatchProcessor, useLazyImage } from '@/utils/performance';
import { PermissionGuard } from '@/components';
import type { User, UserListQuery } from '@campus/shared';
import dayjs from 'dayjs';

const { Search } = Input;
const { TextArea } = Input;

// ===== 性能优化：虚拟化表格 =====
interface VirtualizedTableProps {
  dataSource: User[];
  columns: ColumnsType<User>;
  loading: boolean;
  pagination: TablePaginationConfig;
  onTableChange: (pagination: any) => void;
}

const VirtualizedTable: React.FC<VirtualizedTableProps> = ({
  dataSource,
  columns,
  loading,
  pagination,
  onTableChange,
}) => {
  const {
    containerRef,
    visibleItems,
    totalHeight,
    handleScroll,
  } = useVirtualList({
    items: dataSource,
    containerHeight: 600,
    itemHeight: 80,
    overscan: 5,
  });

  return (
    <div
      ref={containerRef}
      style={{ height: 600, overflow: 'auto' }}
      onScroll={handleScroll}
    >
      <div style={{ height: totalHeight, position: 'relative' }}>
        {visibleItems.map(({ item, index, top }) => (
          <div
            key={item.id}
            style={{
              position: 'absolute',
              top: `${top}px`,
              left: 0,
              right: 0,
              height: 80,
              border: '1px solid #f0f0f0',
              background: '#fff',
              display: 'flex',
              alignItems: 'center',
              padding: '0 16px',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', width: '100%' }}>
              <span style={{ minWidth: 60 }}>{item.id}</span>
              <div style={{ flex: 1, display: 'flex', alignItems: 'center' }}>
                <span style={{ minWidth: 100 }}>{item.nickname}</span>
                <span style={{ margin: '0 8px', color: '#999' }}>学生号: {item.studentId}</span>
                <Tag color={
                  item.status === 'BANNED' ? 'red' : 'green'
                }>
                  {item.status === 'BANNED' ? '已封禁' : '正常'}
                </Tag>
              </div>
              <Space>
                <Button type="link" size="small" icon={<EyeOutlined />}>
                  详情
                </Button>
                <Button
                  type="link"
                  icon={
                    item.status === 'BANNED' ? <CheckCircleOutlined /> : <StopOutlined />
                  }
                  danger={item.status !== 'BANNED'}
                >
                  {item.status === 'BANNED' ? '解封' : '封禁'}
                </Button>
              </Space>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

const OptimizedUserList: React.FC = () => {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useState<UserListQuery>({
    page: 0,
    pageSize: 20,
  });
  const [useVirtualization, setUseVirtualization] = useState(false);
  const [banModalVisible, setBanModalVisible] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [banForm] = Form.useForm();

  // ===== 性能优化：防抖搜索 =====
  const handleSearch = useMemo(
    () => debounce((keyword: string) => {
      setSearchParams(prev => ({ ...prev, keyword, page: 0 }));
    }, 300),
    []
  );

  // ===== 性能优化：批量处理 =====
  const { addToQueue, isProcessing, queueLength } = useBatchProcessor(
    async (users: User[]) => {
      // 批量操作逻辑
      console.log(`批量处理 ${users.length} 个用户`);
      await new Promise(resolve => setTimeout(resolve, 100));
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    5,
    500
  );

  // ===== 查询用户列表 =====
  const { data, isLoading } = useQuery({
    queryKey: ['users', searchParams],
    queryFn: async () => {
      const response = await userService.getUserList(searchParams);
      return response.data;
    },
    staleTime: 30000, // 30秒缓存
  });

  // ===== 性能优化：头像懒加载 =====
  const renderUserAvatar = useCallback((user: User) => {
    const AvatarWithLazyLoad = ({ avatar, nickname }: { avatar?: string; nickname: string }) => {
      const { loaded, error, imageSrc } = useLazyImage(avatar);
      
      return (
        <div style={{ marginRight: 12 }}>
          {loaded && imageSrc ? (
            <img
              src={imageSrc}
              alt={nickname}
              style={{ width: 32, height: 32, borderRadius: '50%' }}
            />
          ) : (
            <div
              style={{
                width: 32,
                height: 32,
                borderRadius: '50%',
                backgroundColor: '#1890ff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'white',
                fontWeight: 'bold',
              }}
            >
              {nickname?.[0]?.toUpperCase()}
            </div>
          )}
        </div>
      );
    };

    return <AvatarWithLazyLoad avatar={user.avatar} nickname={user.nickname} />;
  }, []);

  // ===== 表格列定义 =====
  const columns: ColumnsType<User> = useMemo(() => [
    {
      title: '用户信息',
      key: 'userInfo',
      render: (_, record) => (
        <Space>
          {renderUserAvatar(record)}
          <div>
            <div>{record.nickname}</div>
            <span style={{ fontSize: 12, color: '#999' }}>{record.username}</span>
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
        <Space wrap>
          {roles?.map((role) => (
            <Tag key={role} color="blue">
              {role}
            </Tag>
          ))}
        </Space>
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
  ], [renderUserAvatar]);

  // ===== 处理分页 =====
  const handleTableChange = (pagination: any) => {
    setSearchParams({
      ...searchParams,
      page: pagination.current - 1,
      pageSize: pagination.pageSize,
    });
  };

  // ===== 性能优化：表格配置 =====
  const tableConfig = useMemo(() => ({
    loading: isLoading,
    pagination: {
      current: (searchParams.page || 0) + 1,
      pageSize: searchParams.pageSize || 20,
      total: data?.totalElements || 0,
      showSizeChanger: true,
      showQuickJumper: true,
      showTotal: (total: number) => `共 ${total} 个用户`,
    },
    onChange: handleTableChange,
    scroll: { x: 1200 },
    rowKey: 'id',
  }), [isLoading, searchParams, data]);

  return (
    <div className="optimized-user-list" style={{ padding: '24px' }}>
      <PermissionGuard permission={PERMISSION_CODES.SYSTEM_USER_VIEW}>
        <Card>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            {/* 搜索栏 */}
            <div className="search-bar" style={{ display: 'flex', gap: 16, alignItems: 'center' }}>
              <Search
                placeholder="搜索昵称、用户名、学号"
                allowClear
                style={{ width: 300 }}
                size="large"
                onSearch={handleSearch}
                onChange={(e) => handleSearch(e.target.value)}
              />
              
              {/* 性能控制 */}
              <Space>
                <Button
                  type={useVirtualization ? 'primary' : 'default'}
                  onClick={() => setUseVirtualization(!useVirtualization)}
                  size="small"
                >
                  {useVirtualization ? '虚拟化开' : '虚拟化关'}
                </Button>
                
                {queueLength > 0 && (
                  <Tag color="blue">
                    队列: {queueLength}
                  </Tag>
                )}
                
                {isProcessing && (
                  <Tag color="orange">
                    处理中...
                  </Tag>
                )}
              </Space>
            </div>

            {/* 用户列表 */}
            {useVirtualization ? (
              <VirtualizedTable
                dataSource={data?.content || []}
                columns={columns}
                loading={isLoading}
                pagination={tableConfig.pagination}
                onTableChange={handleTableChange}
              />
            ) : (
              <Table
                columns={columns}
                dataSource={data?.content || []}
                {...tableConfig}
              />
            )}
          </Space>
        </Card>
      </PermissionGuard>
    </div>
  );
};

export default OptimizedUserList;
