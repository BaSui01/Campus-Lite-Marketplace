/**
 * 评价管理列表页
 * @author BaSui 😎
 */

import { useState } from 'react';
import { Table, Button, Input, Select, Space, Tag, Card, Row, Col, Statistic, Rate, Modal, Image } from 'antd';
import { SearchOutlined, EyeOutlined, DeleteOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getApi } from '@campus/shared/utils/apiClient';

const { Option } = Select;

export const ReviewList: React.FC = () => {
  const queryClient = useQueryClient();
  const [keyword, setKeyword] = useState('');
  const [rating, setRating] = useState<number | undefined>();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentReview, setCurrentReview] = useState<any>(null);

  const { data, isLoading, refetch } = useQuery({
    queryKey: ['reviews', { keyword, rating, page, size }],
    queryFn: async () => {
      const api = getApi();
      const response = await api.listReviews(
        keyword || undefined,
        rating,
        page,
        size
      );
      return response.data.data;
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: number) => {
      const api = getApi();
      await api.deleteReview(id);
    },
    onSuccess: () => { refetch(); queryClient.invalidateQueries({ queryKey: ['reviews'] }); },
  });

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '商品', dataIndex: 'goodsTitle', key: 'goodsTitle', width: 200, ellipsis: true },
    { title: '买家', dataIndex: 'buyerName', key: 'buyerName', width: 120 },
    { title: '评分', dataIndex: 'rating', key: 'rating', width: 150, render: (r: number) => <Rate disabled value={r} /> },
    { title: '内容', dataIndex: 'content', key: 'content', ellipsis: true },
    { title: '图片', dataIndex: 'images', key: 'images', width: 80, render: (imgs: string[]) => imgs.length + '张' },
    { title: '时间', dataIndex: 'createdAt', key: 'createdAt', width: 180, render: (d: string) => new Date(d).toLocaleString('zh-CN') },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right' as const,
      width: 150,
      render: (_: any, record: any) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => { setCurrentReview(record); setDetailVisible(true); }}>查看</Button>
          <Button type="link" danger size="small" icon={<DeleteOutlined />} onClick={() => Modal.confirm({ title: '确认删除？', onOk: () => deleteMutation.mutate(record.id) })}>删除</Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={8}><Card><Statistic title="总评价数" value={data?.totalElements || 0} /></Card></Col>
        <Col span={8}><Card><Statistic title="好评数" value={35} valueStyle={{ color: '#52c41a' }} /></Card></Col>
        <Col span={8}><Card><Statistic title="差评数" value={5} valueStyle={{ color: '#f5222d' }} /></Card></Col>
      </Row>

      <Card style={{ marginBottom: 16 }}>
        <Space>
          <Input placeholder="搜索商品/买家" value={keyword} onChange={(e) => setKeyword(e.target.value)} style={{ width: 200 }} prefix={<SearchOutlined />} />
          <Select placeholder="评分" value={rating} onChange={setRating} allowClear style={{ width: 120 }}>
            {[1, 2, 3, 4, 5].map(r => <Option key={r} value={r}>{r}星</Option>)}
          </Select>
          <Button type="primary" icon={<SearchOutlined />} onClick={() => { setPage(0); refetch(); }}>搜索</Button>
          <Button onClick={() => { setKeyword(''); setRating(undefined); setPage(0); }}>重置</Button>
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
          onChange: (p, s) => { setPage(p - 1); setSize(s); },
        }}
        scroll={{ x: 1200 }}
      />

      <Modal title="评价详情" open={detailVisible} onCancel={() => setDetailVisible(false)} footer={[<Button key="close" onClick={() => setDetailVisible(false)}>关闭</Button>]} width={700}>
        {currentReview && (
          <div>
            <p><strong>商品：</strong>{currentReview.goodsTitle}</p>
            <p><strong>买家：</strong>{currentReview.buyerName}</p>
            <p><strong>评分：</strong><Rate disabled value={currentReview.rating} /></p>
            <p><strong>内容：</strong>{currentReview.content}</p>
            {currentReview.images.length > 0 && (
              <div><strong>图片：</strong><Image.PreviewGroup><Space>{currentReview.images.map((url: string, i: number) => <Image key={i} src={url} width={100} />)}</Space></Image.PreviewGroup></div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default ReviewList;
