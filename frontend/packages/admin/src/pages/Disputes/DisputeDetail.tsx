/**
 * 纠纷详情页 + 仲裁处理
 * 
 * @author BaSui 😎
 * @date 2025-11-06
 */

import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  Descriptions,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Radio,
  Input,
  InputNumber,
  message,
  Spin,
  Image,
  Avatar,
  Timeline,
  Row,
  Col,
  Divider,
  Tabs,
  App,
} from 'antd';
import {
  ArrowLeftOutlined,
  CheckOutlined,
  CloseOutlined,
  UserOutlined,
  InfoCircleOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { disputeService, DisputeStatus } from '@/services';

const { TextArea } = Input;
const { TabPane } = Tabs;

/**
 * 纠纷状态映射
 */
const STATUS_MAP: Record<string, { text: string; color: string }> = {
  PENDING: { text: '待处理', color: 'orange' },
  INVESTIGATING: { text: '调查中', color: 'blue' },
  ARBITRATING: { text: '仲裁中', color: 'cyan' },
  RESOLVED: { text: '已解决', color: 'green' },
  REJECTED: { text: '已驳回', color: 'red' },
  CLOSED: { text: '已关闭', color: 'default' },
};

/**
 * 纠纷类型映射
 */
const TYPE_MAP: Record<string, string> = {
  GOODS_QUALITY: '商品质量问题',
  GOODS_DESCRIPTION: '商品描述不符',
  DELIVERY_ISSUE: '物流配送问题',
  REFUND_ISSUE: '退款纠纷',
  SERVICE_ATTITUDE: '服务态度问题',
  OTHER: '其他纠纷',
};

export const DisputeDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { modal } = App.useApp();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [arbitrateModalVisible, setArbitrateModalVisible] = useState(false);

  // 查询纠纷详情
  const { data: dispute, isLoading } = useQuery({
    queryKey: ['dispute', 'detail', id],
    queryFn: async () => {
      const response = await disputeService.getDisputeDetail(Number(id));
      return response.data;
    },
    enabled: !!id,
  });

  // 认领纠纷
  const claimMutation = useMutation({
    mutationFn: () => disputeService.claimDispute(Number(id)),
    onSuccess: () => {
      message.success('认领成功，开始处理纠纷');
      queryClient.invalidateQueries({ queryKey: ['dispute', 'detail', id] });
      queryClient.invalidateQueries({ queryKey: ['disputes'] });
    },
    onError: () => message.error('认领失败'),
  });

  // 仲裁纠纷
  const arbitrateMutation = useMutation({
    mutationFn: (params: {
      action: 'ACCEPT' | 'INVESTIGATE' | 'RESOLVE' | 'REJECT' | 'CLOSE';
      decision?: string;
      compensationAmount?: number;
      reason: string;
    }) =>
      disputeService.arbitrateDispute({
        disputeId: Number(id),
        ...params,
      }),
    onSuccess: () => {
      message.success('仲裁操作成功');
      setArbitrateModalVisible(false);
      form.resetFields();
      queryClient.invalidateQueries({ queryKey: ['dispute', 'detail', id] });
      queryClient.invalidateQueries({ queryKey: ['disputes'] });
    },
    onError: () => message.error('仲裁操作失败'),
  });

  // 返回列表
  const handleBack = () => {
    navigate('/admin/disputes/list');
  };

  // 认领纠纷
  const handleClaim = () => {
    modal.confirm({
      title: '确认认领纠纷',
      content: '认领后将由你负责仲裁此纠纷，确定要认领吗？',
      onOk: () => claimMutation.mutate(),
    });
  };

  // 打开仲裁弹窗
  const handleOpenArbitrateModal = (action: string) => {
    form.resetFields();
    form.setFieldsValue({ action });
    setArbitrateModalVisible(true);
  };

  // 提交仲裁
  const handleArbitrateSubmit = async () => {
    try {
      const values = await form.validateFields();
      arbitrateMutation.mutate(values);
    } catch (error) {
      console.error('表单校验失败:', error);
    }
  };

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: '100px 0' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!dispute) {
    return (
      <Card>
        <div style={{ textAlign: 'center', padding: '50px 0' }}>
          <p>纠纷不存在或已被删除</p>
          <Button type="primary" onClick={handleBack}>
            返回列表
          </Button>
        </div>
      </Card>
    );
  }

  const statusInfo = STATUS_MAP[dispute.status] || { text: dispute.status, color: 'default' };
  const canClaim = dispute.status === 'PENDING' && !dispute.arbitratorId;
  const canArbitrate =
    dispute.arbitratorId &&
    (dispute.status === 'PENDING' ||
      dispute.status === 'INVESTIGATING' ||
      dispute.status === 'ARBITRATING');

  return (
    <div style={{ padding: 24 }}>
      {/* 顶部操作栏 */}
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={handleBack}>
          返回列表
        </Button>
        {canClaim && (
          <Button type="primary" icon={<CheckOutlined />} onClick={handleClaim}>
            认领纠纷
          </Button>
        )}
        {canArbitrate && dispute.status === 'PENDING' && (
          <Button
            type="primary"
            icon={<InfoCircleOutlined />}
            onClick={() => handleOpenArbitrateModal('INVESTIGATE')}
          >
            开始调查
          </Button>
        )}
        {canArbitrate &&
          (dispute.status === 'INVESTIGATING' || dispute.status === 'ARBITRATING') && (
            <>
              <Button
                type="primary"
                icon={<CheckOutlined />}
                onClick={() => handleOpenArbitrateModal('RESOLVE')}
              >
                解决纠纷
              </Button>
              <Button
                danger
                icon={<CloseOutlined />}
                onClick={() => handleOpenArbitrateModal('REJECT')}
              >
                驳回纠纷
              </Button>
            </>
          )}
      </Space>

      {/* 纠纷基本信息卡片 */}
      <Card title="纠纷基本信息" style={{ marginBottom: 16 }}>
        <Descriptions column={2} bordered>
          <Descriptions.Item label="纠纷编号">{dispute.disputeNo}</Descriptions.Item>
          <Descriptions.Item label="订单号">
            <a onClick={() => navigate(`/admin/orders/${dispute.orderNo}`)}>
              {dispute.orderNo}
            </a>
          </Descriptions.Item>
          <Descriptions.Item label="纠纷类型">
            {TYPE_MAP[dispute.type] || dispute.type}
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={statusInfo.color}>{statusInfo.text}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="纠纷标题" span={2}>
            {dispute.title}
          </Descriptions.Item>
          <Descriptions.Item label="纠纷描述" span={2}>
            {dispute.description}
          </Descriptions.Item>
          <Descriptions.Item label="涉及金额">
            <span style={{ color: '#f5222d', fontWeight: 'bold', fontSize: 16 }}>
              ¥{dispute.amount.toFixed(2)}
            </span>
          </Descriptions.Item>
          <Descriptions.Item label="仲裁员">
            {dispute.arbitratorName || '未分配'}
          </Descriptions.Item>
          <Descriptions.Item label="创建时间">
            {new Date(dispute.createdAt).toLocaleString('zh-CN')}
          </Descriptions.Item>
          <Descriptions.Item label="更新时间">
            {new Date(dispute.updatedAt).toLocaleString('zh-CN')}
          </Descriptions.Item>
          {dispute.closedAt && (
            <Descriptions.Item label="关闭时间">
              {new Date(dispute.closedAt).toLocaleString('zh-CN')}
            </Descriptions.Item>
          )}
        </Descriptions>
      </Card>

      <Row gutter={16} style={{ marginBottom: 16 }}>
        {/* 申诉方信息卡片 */}
        <Col span={12}>
          <Card title="申诉方信息">
            <Space>
              <Avatar size={64} icon={<UserOutlined />} src={dispute.plaintiffAvatar} />
              <div>
                <h3>{dispute.plaintiffName}</h3>
                <p style={{ color: '#8c8c8c', margin: 0 }}>用户ID：{dispute.plaintiffId}</p>
              </div>
            </Space>
          </Card>
        </Col>

        {/* 被诉方信息卡片 */}
        <Col span={12}>
          <Card title="被诉方信息">
            <Space>
              <Avatar size={64} icon={<UserOutlined />} src={dispute.defendantAvatar} />
              <div>
                <h3>{dispute.defendantName}</h3>
                <p style={{ color: '#8c8c8c', margin: 0 }}>用户ID：{dispute.defendantId}</p>
              </div>
            </Space>
          </Card>
        </Col>
      </Row>

      {/* 订单信息卡片 */}
      <Card title="关联订单信息" style={{ marginBottom: 16 }}>
        <Row gutter={16} align="middle">
          <Col span={4}>
            <Image
              src={dispute.orderInfo.goodsImage || 'https://picsum.photos/150/150?random=6'}
              alt={dispute.orderInfo.goodsTitle}
              width={150}
              height={150}
              style={{ objectFit: 'cover', borderRadius: 8 }}
            />
          </Col>
          <Col span={20}>
            <Descriptions column={2}>
              <Descriptions.Item label="商品标题" span={2}>
                <a
                  onClick={() => navigate(`/admin/goods/${dispute.orderInfo.goodsId}`)}
                >
                  {dispute.orderInfo.goodsTitle}
                </a>
              </Descriptions.Item>
              <Descriptions.Item label="订单号">
                <a onClick={() => navigate(`/admin/orders/${dispute.orderInfo.orderNo}`)}>
                  {dispute.orderInfo.orderNo}
                </a>
              </Descriptions.Item>
              <Descriptions.Item label="订单状态">
                {dispute.orderInfo.status}
              </Descriptions.Item>
              <Descriptions.Item label="订单金额">
                <span style={{ fontSize: 18, color: '#f5222d', fontWeight: 'bold' }}>
                  ¥{dispute.orderInfo.totalAmount.toFixed(2)}
                </span>
              </Descriptions.Item>
              <Descriptions.Item label="买家">
                {dispute.orderInfo.buyerName}
              </Descriptions.Item>
              <Descriptions.Item label="卖家">
                {dispute.orderInfo.sellerName}
              </Descriptions.Item>
            </Descriptions>
          </Col>
        </Row>
      </Card>

      {/* 证据材料与仲裁历史（Tabs） */}
      <Card>
        <Tabs defaultActiveKey="evidence">
          <TabPane tab="证据材料" key="evidence">
            {dispute.evidenceMaterials && dispute.evidenceMaterials.length > 0 ? (
              <div>
                {dispute.evidenceMaterials.map((evidence, index) => (
                  <div key={evidence.id} style={{ marginBottom: 16 }}>
                    <Divider orientation="left">
                      证据 {index + 1} - 上传者：{evidence.uploaderName}
                    </Divider>
                    {evidence.description && (
                      <p style={{ color: '#8c8c8c' }}>说明：{evidence.description}</p>
                    )}
                    {evidence.type === 'IMAGE' ? (
                      <Image src={evidence.url} width={200} height={200} style={{ objectFit: 'cover' }} />
                    ) : (
                      <p>
                        <a href={evidence.url} target="_blank" rel="noopener noreferrer">
                          {evidence.fileName} ({(evidence.fileSize || 0) / 1024}KB)
                        </a>
                      </p>
                    )}
                    <p style={{ fontSize: 12, color: '#8c8c8c' }}>
                      上传时间：{new Date(evidence.createdAt).toLocaleString('zh-CN')}
                    </p>
                  </div>
                ))}
              </div>
            ) : (
              <p style={{ textAlign: 'center', color: '#8c8c8c', padding: '50px 0' }}>
                暂无证据材料
              </p>
            )}
          </TabPane>

          <TabPane tab="仲裁历史" key="history">
            {dispute.arbitrationHistory && dispute.arbitrationHistory.length > 0 ? (
              <Timeline>
                {dispute.arbitrationHistory.map((record) => (
                  <Timeline.Item
                    key={record.id}
                    color={
                      record.action === 'RESOLVE'
                        ? 'green'
                        : record.action === 'REJECT'
                        ? 'red'
                        : 'blue'
                    }
                  >
                    <p>
                      <strong>{record.arbitratorName}</strong> -{' '}
                      {record.action === 'ACCEPT' && '认领纠纷'}
                      {record.action === 'INVESTIGATE' && '开始调查'}
                      {record.action === 'RESOLVE' && '解决纠纷'}
                      {record.action === 'REJECT' && '驳回纠纷'}
                      {record.action === 'CLOSE' && '关闭纠纷'}
                    </p>
                    {record.decision && (
                      <p style={{ color: '#595959' }}>仲裁决定：{record.decision}</p>
                    )}
                    {record.compensationAmount !== undefined && (
                      <p style={{ color: '#f5222d', fontWeight: 'bold' }}>
                        赔偿金额：¥{record.compensationAmount.toFixed(2)}
                      </p>
                    )}
                    <p style={{ color: '#8c8c8c' }}>理由：{record.reason}</p>
                    <p style={{ fontSize: 12, color: '#8c8c8c' }}>
                      {new Date(record.createdAt).toLocaleString('zh-CN')}
                    </p>
                  </Timeline.Item>
                ))}
              </Timeline>
            ) : (
              <p style={{ textAlign: 'center', color: '#8c8c8c', padding: '50px 0' }}>
                暂无仲裁历史
              </p>
            )}
          </TabPane>
        </Tabs>
      </Card>

      {/* 仲裁操作弹窗 */}
      <Modal
        title="纠纷仲裁"
        open={arbitrateModalVisible}
        onOk={handleArbitrateSubmit}
        onCancel={() => {
          setArbitrateModalVisible(false);
          form.resetFields();
        }}
        confirmLoading={arbitrateMutation.isPending}
        okText="确认"
        cancelText="取消"
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="action" label="仲裁操作" hidden>
            <Input />
          </Form.Item>

          {form.getFieldValue('action') === 'RESOLVE' && (
            <>
              <Form.Item
                name="decision"
                label="仲裁决定"
                rules={[{ required: true, message: '请填写仲裁决定' }]}
              >
                <TextArea
                  rows={4}
                  placeholder="请详细描述仲裁决定和解决方案"
                  maxLength={500}
                  showCount
                />
              </Form.Item>
              <Form.Item
                name="compensationAmount"
                label="赔偿金额（可选）"
                tooltip="如需赔偿，请填写赔偿金额"
              >
                <InputNumber
                  min={0}
                  max={100000}
                  precision={2}
                  style={{ width: '100%' }}
                  placeholder="0.00"
                  addonBefore="¥"
                />
              </Form.Item>
            </>
          )}

          <Form.Item
            name="reason"
            label={
              form.getFieldValue('action') === 'INVESTIGATE'
                ? '调查说明'
                : form.getFieldValue('action') === 'REJECT'
                ? '驳回理由'
                : '操作理由'
            }
            rules={[
              { required: true, message: '请填写操作理由' },
              { max: 500, message: '最多500字' },
            ]}
          >
            <TextArea rows={4} placeholder="必填，最多500字" maxLength={500} showCount />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default DisputeDetail;
