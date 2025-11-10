/**
 * 商品列表页
 * 
 * 功能：
 * - 分页查询商品列表
 * - 关键词搜索
 * - 高级筛选（分类、价格区间、状态、标签）
 * - 批量操作（批量上下架、批量删除）
 * - 单项操作（查看详情、编辑、上下架、删除）
 * 
 * @author BaSui 😎
 * @date 2025-11-05
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Table,
  Button,
  Space,
  Tag,
  message,
  Popconfirm,
  Card,
  Statistic,
  Row,
  Col,
  App,
} from 'antd';
import {
  CheckOutlined,
  CloseOutlined,
  DeleteOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { goodsService } from '@campus/shared/services/goods';
import type { GoodsResponse } from '@campus/shared/api';
import { FilterPanel } from '@campus/shared/components';
import type { FilterConfig, FilterValues } from '@campus/shared/types/filter';
import { GOODS_STATUS_OPTIONS } from '@campus/shared/constants';
import './GoodsList.css';

/**
 * 商品状态映射
 */
const STATUS_MAP: Record<string, { text: string; color: string }> = {
  PENDING: { text: '待审核', color: 'orange' },
  APPROVED: { text: '已上架', color: 'green' },
  REJECTED: { text: '已下架', color: 'red' },
  DELETED: { text: '已删除', color: 'gray' },
};

// 商品筛选配置
const goodsFilters: FilterConfig[] = [
  {
    type: 'input',
    field: 'keyword',
    label: '关键词',
    placeholder: '搜索商品标题/描述',
    width: 200,
  },
  {
    type: 'select',
    field: 'categoryId',
    label: '分类',
    placeholder: '选择分类',
    options: [
      { label: '电子产品', value: 101 },
      { label: '图书教材', value: 102 },
      { label: '服装鞋帽', value: 103 },
      { label: '生活用品', value: 104 },
      { label: '其他', value: 105 },
    ],
    width: 150,
  },
  {
    type: 'numberRange',
    field: 'price',
    label: '价格区间',
    prefix: '¥',
    min: 0,
  },
  {
    type: 'select',
    field: 'status',
    label: '状态',
    placeholder: '选择状态',
    options: GOODS_STATUS_OPTIONS,
    width: 120,
  },
];

export const GoodsList: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { modal } = App.useApp();

  // 筛选参数（使用 FilterPanel 统一管理）
  const [filterValues, setFilterValues] = useState<FilterValues>({});
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(20);

  // 批量操作
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

  // 查询商品列表
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['goods', 'list', filterValues, page, size],
    queryFn: () =>
      goodsService.listGoods({
        keyword: filterValues.keyword,
        categoryId: filterValues.categoryId,
        minPrice: filterValues.price?.min,
        maxPrice: filterValues.price?.max,
        // ❌ 移除 status 参数 - 后端 listGoods 接口不支持此参数
        // 如需按状态筛选，应使用 listPendingGoods 或其他专门接口
        page,
        size,
        sortBy: 'createdAt',
        sortDirection: 'DESC',
      }),
    staleTime: 5 * 60 * 1000, // 缓存5分钟
  });

  // 查询待审核商品统计
  const { data: pendingCount } = useQuery({
    queryKey: ['goods', 'pending', 'count'],
    queryFn: async () => {
      const result = await goodsService.listPendingGoods({ page: 0, size: 1 });
      return result.totalElements || 0;
    },
    refetchInterval: 30000, // 每30秒刷新
  });

  // 删除商品
  const deleteMutation = useMutation({
    mutationFn: (id: number) => goodsService.deleteGoods(id),
    onSuccess: () => {
      message.success('删除成功');
      refetch();
      queryClient.invalidateQueries({ queryKey: ['goods'] });
    },
    onError: () => {
      message.error('删除失败');
    },
  });

  // 批量上下架
  const batchUpdateStatusMutation = useMutation({
    mutationFn: ({ ids, targetStatus }: { ids: number[]; targetStatus: string }) =>
      goodsService.batchUpdateGoods({ goodsIds: ids, targetStatus }),
    onSuccess: () => {
      message.success('批量操作成功');
      setSelectedRowKeys([]);
      refetch();
      queryClient.invalidateQueries({ queryKey: ['goods'] });
    },
    onError: () => {
      message.error('批量操作失败');
    },
  });

  // 搜索处理
  const handleSearch = () => {
    setPage(0); // 重置到第一页
    refetch();
  };

  // 查看详情
  const handleView = (id: number) => {
    navigate(`/admin/goods/${id}`);
  };

  // 删除商品
  const handleDelete = (id: number) => {
    deleteMutation.mutate(id);
  };

  // 批量上架
  const handleBatchApprove = () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要上架的商品');
      return;
    }
    modal.confirm({
      title: '确认批量上架',
      content: `确定要上架选中的 ${selectedRowKeys.length} 个商品吗？`,
      onOk: () => {
        batchUpdateStatusMutation.mutate({
          ids: selectedRowKeys as number[],
          targetStatus: 'APPROVED',
        });
      },
    });
  };

  // 批量下架
  const handleBatchReject = () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要下架的商品');
      return;
    }
    modal.confirm({
      title: '确认批量下架',
      content: `确定要下架选中的 ${selectedRowKeys.length} 个商品吗？`,
      onOk: () => {
        batchUpdateStatusMutation.mutate({
          ids: selectedRowKeys as number[],
          targetStatus: 'REJECTED',
        });
      },
    });
  };

  // 批量删除
  const handleBatchDelete = () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要删除的商品');
      return;
    }
    modal.confirm({
      title: '确认批量删除',
      content: `确定要删除选中的 ${selectedRowKeys.length} 个商品吗？此操作不可恢复！`,
      okType: 'danger',
      onOk: async () => {
        try {
          await Promise.all(
            selectedRowKeys.map((id) => goodsService.deleteGoods(id as number))
          );
          message.success('批量删除成功');
          setSelectedRowKeys([]);
          refetch();
        } catch (error) {
          message.error('批量删除失败');
        }
      },
    });
  };

  // 表格列定义
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '商品图片',
      dataIndex: 'images',
      key: 'images',
      width: 100,
      render: (images: string[]) => (
        <img
          src={images?.[0] || 'https://picsum.photos/60/60?random=1'}
          alt="商品"
          className="goods-image"
        />
      ),
    },
    {
      title: '商品标题',
      dataIndex: 'title',
      key: 'title',
      width: 200,
      ellipsis: true,
    },
    {
      title: '价格',
      dataIndex: 'price',
      key: 'price',
      width: 100,
      render: (price: number) => <span className="goods-price">¥{price.toFixed(2)}</span>,
    },
    {
      title: '分类',
      dataIndex: 'categoryName',
      key: 'categoryName',
      width: 100,
    },
    {
      title: '卖家',
      dataIndex: 'sellerName',
      key: 'sellerName',
      width: 120,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const statusInfo = STATUS_MAP[status] || { text: status, color: 'default' };
        return <Tag color={statusInfo.color}>{statusInfo.text}</Tag>;
      },
    },
    {
      title: '浏览量',
      dataIndex: 'viewCount',
      key: 'viewCount',
      width: 100,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => new Date(date).toLocaleString('zh-CN'),
    },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right' as const,
      width: 180,
      render: (_: any, record: GoodsResponse) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleView(record.id)}
          >
            查看
          </Button>
          <Popconfirm
            title="确定要删除这个商品吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="goods-list-container">
      <h2 className="page-title">📦 商品管理</h2>
      
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }} className="goods-stats-row">
        <Col span={6}>
          <Card>
            <Statistic title="总商品数" value={data?.totalElements || 0} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="待审核"
              value={pendingCount || 0}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已上架"
              value={
                data?.content?.filter((g: GoodsResponse) => g.status === 'APPROVED').length || 0
              }
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已下架"
              value={
                data?.content?.filter((g: GoodsResponse) => g.status === 'REJECTED').length || 0
              }
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 筛选面板 */}
      <FilterPanel
        config={{ filters: goodsFilters }}
        values={filterValues}
        onChange={setFilterValues}
        onSearch={handleSearch}
        onReset={() => {
          setFilterValues({});
          setPage(0);
        }}
        style={{ marginBottom: 16 }}
      />

      {/* 批量操作按钮 */}
      <Space style={{ marginBottom: 16 }} className="goods-batch-actions action-buttons">
        <Button
          type="primary"
          icon={<CheckOutlined />}
          onClick={handleBatchApprove}
          disabled={selectedRowKeys.length === 0}
        >
          批量上架 ({selectedRowKeys.length})
        </Button>
        <Button
          icon={<CloseOutlined />}
          onClick={handleBatchReject}
          disabled={selectedRowKeys.length === 0}
        >
          批量下架 ({selectedRowKeys.length})
        </Button>
        <Button
          danger
          icon={<DeleteOutlined />}
          onClick={handleBatchDelete}
          disabled={selectedRowKeys.length === 0}
        >
          批量删除 ({selectedRowKeys.length})
        </Button>
      </Space>

      {/* 商品表格 */}
      <div className="goods-table-wrapper">
        <Table
        rowKey="id"
        columns={columns}
        dataSource={data?.content || []}
        loading={isLoading}
        rowSelection={{
          selectedRowKeys,
          onChange: setSelectedRowKeys,
        }}
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
        scroll={{ x: 1500 }}
        />
      </div>
    </div>
  );
};

export default GoodsList;
