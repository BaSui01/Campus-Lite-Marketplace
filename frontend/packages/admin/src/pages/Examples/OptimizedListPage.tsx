/**
 * 优化后的列表页面示例
 * 
 * 展示如何使用公共组件和 Hooks 重构列表页面
 * 
 * 对比：
 * - 重构前：~300 行代码，分页、搜索、状态管理都要手写
 * - 重构后：~150 行代码，复用 useTable、SearchBar、StatusTag 等
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import React, { useEffect } from 'react';
import { Table, Space, Button, Card, Row, Col, Statistic, message } from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTable, useModal, useDebounce } from '@/hooks';
import { PageHeader, SearchBar, ConfirmButton, StatusTag } from '@/components/Common';
import type { SearchField } from '@/components/Common';

/**
 * 模拟数据类型
 */
interface ListItem {
  id: number;
  name: string;
  status: 'ACTIVE' | 'DISABLED' | 'PENDING';
  createTime: string;
}

/**
 * 状态映射
 */
const STATUS_MAP = {
  ACTIVE: { text: '启用', color: 'green', icon: <CheckCircleOutlined /> },
  DISABLED: { text: '禁用', color: 'red', icon: <CloseCircleOutlined /> },
  PENDING: { text: '待审核', color: 'orange', icon: <SyncOutlined spin /> },
};

/**
 * 优化后的列表页面
 */
export const OptimizedListPage: React.FC = () => {
  const queryClient = useQueryClient();

  // ===== 使用公共 Hooks =====
  
  // 1. 表格状态管理（分页、排序、筛选）
  const { page, size, total, setTotal, handleTableChange, tableParams } = useTable({
    defaultPageSize: 20,
  });

  // 2. 弹窗状态管理
  const { visible, data: modalData, open, close } = useModal<ListItem>();

  // 3. 搜索防抖
  const [keyword, setKeyword] = React.useState('');
  const debouncedKeyword = useDebounce(keyword, 500);

  // ===== 数据查询 =====
  
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['list', page, size, debouncedKeyword],
    queryFn: async () => {
      // 模拟 API 调用
      return {
        content: [
          {
            id: 1,
            name: '测试数据1',
            status: 'ACTIVE' as const,
            createTime: '2025-11-08 10:00:00',
          },
          {
            id: 2,
            name: '测试数据2',
            status: 'PENDING' as const,
            createTime: '2025-11-08 11:00:00',
          },
        ],
        totalElements: 2,
      };
    },
  });

  // 更新总条数
  useEffect(() => {
    if (data) {
      setTotal(data.totalElements);
    }
  }, [data, setTotal]);

  // ===== 删除操作 =====
  
  const deleteMutation = useMutation({
    mutationFn: async (id: number) => {
      // 模拟 API 调用
      return Promise.resolve();
    },
    onSuccess: () => {
      message.success('删除成功');
      queryClient.invalidateQueries({ queryKey: ['list'] });
    },
  });

  // ===== 搜索配置 =====
  
  const searchFields: SearchField[] = [
    {
      name: 'keyword',
      label: '关键词',
      type: 'input',
      placeholder: '请输入名称关键词',
      span: 8,
    },
    {
      name: 'status',
      label: '状态',
      type: 'select',
      options: [
        { label: '全部', value: '' },
        { label: '启用', value: 'ACTIVE' },
        { label: '禁用', value: 'DISABLED' },
        { label: '待审核', value: 'PENDING' },
      ],
      span: 6,
    },
    {
      name: 'dateRange',
      label: '创建时间',
      type: 'dateRange',
      span: 10,
    },
  ];

  // ===== 表格列配置 =====
  
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <StatusTag status={status} statusMap={STATUS_MAP} />
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: any, record: ListItem) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => open(record)}
          >
            编辑
          </Button>
          <ConfirmButton
            title="删除确认"
            content={`确定要删除"${record.name}"吗？`}
            onConfirm={() => deleteMutation.mutate(record.id)}
            type="link"
            size="small"
            icon={<DeleteOutlined />}
            danger
          >
            删除
          </ConfirmButton>
        </Space>
      ),
    },
  ];

  return (
    <div>
      {/* 页面头部 */}
      <PageHeader
        title="列表页面"
        subTitle="使用公共组件和 Hooks 优化后的列表页面"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => open()}>
            新增
          </Button>
        }
      />

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总数"
              value={total}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="启用"
              value={data?.content.filter((item) => item.status === 'ACTIVE').length || 0}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="禁用"
              value={data?.content.filter((item) => item.status === 'DISABLED').length || 0}
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="待审核"
              value={data?.content.filter((item) => item.status === 'PENDING').length || 0}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 搜索栏 */}
      <SearchBar
        fields={searchFields}
        onSearch={(values) => {
          console.log('搜索参数:', values);
          setKeyword(values.keyword || '');
        }}
        onReset={() => {
          console.log('重置搜索');
          setKeyword('');
        }}
        loading={isLoading}
      />

      {/* 数据表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={data?.content}
          rowKey="id"
          loading={isLoading}
          pagination={tableParams.pagination}
          onChange={handleTableChange}
        />
      </Card>
    </div>
  );
};
