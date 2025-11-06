/**
 * 申诉详情页 + 审核
 * 
 * @author BaSui 😎
 * @date 2025-11-05
 */

import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card, Descriptions, Button, Space, Tag, Modal, Form, Radio, Input, message, Spin, Image, Avatar, Timeline,
} from 'antd';
import { ArrowLeftOutlined, CheckOutlined, CloseOutlined, UserOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { appealService } from '@campus/shared/services/appeal';

const STATUS_MAP: Record<string, { text: string; color: string }> = {
  PENDING: { text: '待审核', color: 'orange' },
  APPROVED: { text: '已批准', color: 'green' },
  REJECTED: { text: '已拒绝', color: 'red' },
};

export const AppealDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [reviewModalVisible, setReviewModalVisible] = useState(false);

  const { data: appeal, isLoading } = useQuery({
    queryKey: ['appeal', 'detail', id],
    queryFn: async () => {
      const response = await appealService.getAppealDetail(Number(id));
      return response.data;
    },
    enabled: !!id,
  });

  const reviewMutation = useMutation({
    mutationFn: (params: { approved: boolean; reason: string }) =>
      appealService.reviewAppeal({ appealId: Number(id), ...params }),
    onSuccess: () => {
      message.success('审核成功');
      setReviewModalVisible(false);
      form.resetFields();
      queryClient.invalidateQueries({ queryKey: ['appeal', 'detail', id] });
      queryClient.invalidateQueries({ queryKey: ['appeals'] });
    },
    onError: () => message.error('审核失败'),
  });

  const handleReviewSubmit = async () => {
    try {
      const values = await form.validateFields();
      reviewMutation.mutate(values);
    } catch (error) {
      console.error('表单校验失败:', error);
    }
  };

  if (isLoading) return <div style={{ textAlign: 'center', padding: '100px 0' }}><Spin size="large" /></div>;
  if (!appeal) return <Card><p>申诉不存在</p><Button onClick={() => navigate(-1)}>返回</Button></Card>;

  const statusInfo = STATUS_MAP[appeal.status] || { text: appeal.status, color: 'default' };

  return (
    <div style={{ padding: 24 }}>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/admin/appeals/list')}>返回列表</Button>
        {appeal.status === 'PENDING' && (
          <Button type="primary" icon={<CheckOutlined />} onClick={() => { form.resetFields(); form.setFieldsValue({ approved: true }); setReviewModalVisible(true); }}>
            审核申诉
          </Button>
        )}
      </Space>

      <Card title="申诉基本信息" style={{ marginBottom: 16 }}>
        <Descriptions column={2} bordered>
          <Descriptions.Item label="申诉ID">{appeal.id}</Descriptions.Item>
          <Descriptions.Item label="状态"><Tag color={statusInfo.color}>{statusInfo.text}</Tag></Descriptions.Item>
          <Descriptions.Item label="申诉标题" span={2}>{appeal.title}</Descriptions.Item>
          <Descriptions.Item label="申诉描述" span={2}>{appeal.description}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{new Date(appeal.createdAt).toLocaleString('zh-CN')}</Descriptions.Item>
          <Descriptions.Item label="更新时间">{new Date(appeal.updatedAt).toLocaleString('zh-CN')}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="申诉人信息" style={{ marginBottom: 16 }}>
        <Space>
          <Avatar size={64} icon={<UserOutlined />} src={appeal.userAvatar} />
          <div>
            <h3>{appeal.userName}</h3>
            <p style={{ margin: 0, color: '#8c8c8c' }}>联系方式：{appeal.userPhone || '未提供'}</p>
          </div>
        </Space>
      </Card>

      {appeal.materials && appeal.materials.length > 0 && (
        <Card title="申诉材料" style={{ marginBottom: 16 }}>
          <Image.PreviewGroup>
            <Space>
              {appeal.materials.map((m: any, i: number) => (
                <Image key={i} src={m.url} width={100} height={100} style={{ objectFit: 'cover' }} />
              ))}
            </Space>
          </Image.PreviewGroup>
        </Card>
      )}

      {appeal.appealHistory && appeal.appealHistory.length > 0 && (
        <Card title="审核历史">
          <Timeline>
            {appeal.appealHistory.map((h: any) => (
              <Timeline.Item key={h.id} color={h.approved ? 'green' : 'red'}>
                <p><strong>{h.reviewerName}</strong> {h.approved ? '批准' : '拒绝'}了申诉</p>
                {h.reason && <p style={{ color: '#8c8c8c' }}>原因：{h.reason}</p>}
                <p style={{ fontSize: 12, color: '#8c8c8c' }}>{new Date(h.createdAt).toLocaleString('zh-CN')}</p>
              </Timeline.Item>
            ))}
          </Timeline>
        </Card>
      )}

      <Modal title="审核申诉" open={reviewModalVisible} onOk={handleReviewSubmit} onCancel={() => setReviewModalVisible(false)} confirmLoading={reviewMutation.isPending}>
        <Form form={form} layout="vertical" initialValues={{ approved: true }}>
          <Form.Item name="approved" label="审核结果" rules={[{ required: true }]}>
            <Radio.Group>
              <Radio value={true}>批准申诉</Radio>
              <Radio value={false}>拒绝申诉</Radio>
            </Radio.Group>
          </Form.Item>
          <Form.Item name="reason" label="审核意见" rules={[{ required: true, message: '请填写审核意见' }, { max: 200 }]}>
            <Input.TextArea rows={4} placeholder="必填，最多200字" maxLength={200} showCount />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AppealDetail;
