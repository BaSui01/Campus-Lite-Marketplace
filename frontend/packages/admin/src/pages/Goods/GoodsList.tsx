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

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Table,
  Button,
  Input,
  Select,
  Space,
  Tag,
  message,
  Modal,
  Popconfirm,
  InputNumber,
  Card,
  Statistic,
  Row,
  Col,
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  CheckOutlined,
  CloseOutlined,
  DeleteOutlined,
  EyeOutlined,
  EditOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { goodsService } from '@campus/shared/services/goods';
import type { GoodsResponse } from '@campus/shared/api';

const { Option } = Select;

/**
 * 商品状态映射
 */
const STATUS_MAP: Record<string, { text: string; color: string }> = {
  PENDING: { text: '待审核', color: 'orange' },
  APPROVED: { text: '已上架', color: 'green' },
  REJECTED: { text: '已下架', color: 'red' },
  DELETED: { text: '已删除', color: 'gray' },
};

export const GoodsList: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  // 查询参数
  const [keyword, setKeyword] = useState<string>('');
  const [categoryId, setCategoryId] = useState<number | undefined>();
  const [minPrice, setMinPrice] = useState<number | undefined>();
  const [maxPrice, setMaxPrice] = useState<number | undefined>();
  const [status, setStatus] = useState<string | undefined>();
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(20);

  // 批量操作
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

  // 查询商品列表
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['goods', 'list', { keyword, categoryId, minPrice, maxPrice, status, page, size }],
    queryFn: () =>
      goodsService.listGoods({
        keyword,
        categoryId,
        minPrice,
        maxPrice,
        status,
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

  // 重置筛选
  const handleReset = () => {
    setKeyword('');
    setCategoryId(undefined);
    setMinPrice(undefined);
    setMaxPrice(undefined);
    setStatus(undefined);
    setPage(0);
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
    Modal.confirm({
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
    Modal.confirm({
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
    Modal.confirm({
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
          src={images?.[0] || 'https://via.placeholder.com/60'}
          alt="商品"
          style={{ width: 60, height: 60, objectFit: 'cover', borderRadius: 4 }}
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
      render: (price: number) => `¥${price.toFixed(2)}`,
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
    <div style={{ padding: 24 }}>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
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

      {/* 搜索和筛选栏 */}
      <Card style={{ marginBottom: 16 }}>
        <Space wrap style={{ width: '100%' }}>
          <Input
            placeholder="搜索商品标题/描述"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={handleSearch}
            style={{ width: 200 }}
            prefix={<SearchOutlined />}
          />
          <Select
            placeholder="选择分类"
            value={categoryId}
            onChange={setCategoryId}
            allowClear
            style={{ width: 150 }}
          >
            <Option value={101}>电子产品</Option>
            <Option value={102}>图书教材</Option>
            <Option value={103}>服装鞋帽</Option>
            <Option value={104}>生活用品</Option>
            <Option value={105}>其他</Option>
          </Select>
          <InputNumber
            placeholder="最低价格"
            value={minPrice}
            onChange={(value) => setMinPrice(value || undefined)}
            min={0}
            style={{ width: 120 }}
            prefix="¥"
          />
          <InputNumber
            placeholder="最高价格"
            value={maxPrice}
            onChange={(value) => setMaxPrice(value || undefined)}
            min={0}
            style={{ width: 120 }}
            prefix="¥"
          />
          <Select
            placeholder="选择状态"
            value={status}
            onChange={setStatus}
            allowClear
            style={{ width: 120 }}
          >
            <Option value="PENDING">待审核</Option>
            <Option value="APPROVED">已上架</Option>
            <Option value="REJECTED">已下架</Option>
          </Select>
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
          </Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>
      </Card>

      {/* 批量操作按钮 */}
      <Space style={{ marginBottom: 16 }}>
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
  );
};

export default GoodsList;
