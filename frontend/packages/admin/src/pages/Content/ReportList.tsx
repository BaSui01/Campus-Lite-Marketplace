/**
 * 举报管理页面
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React, { useState } from 'react';
import {
  Table,
  Card,
  Button,
  Tag,
  Space,
  Modal,
  Form,
  Input,
  message,
  Typography,
  Tooltip,
} from 'antd';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { reportService } from '@campus/shared';
import { PermissionGuard } from '@/components';
import { PERMISSION_CODES } from '@campus/shared';
import type { ReportSummary } from '@campus/shared';

const { Text } = Typography;
const { TextArea } = Input;

const ReportList: React.FC = () => {
  const queryClient = useQueryClient();
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [handleModalVisible, setHandleModalVisible] = useState(false);
  const [selectedReport, setSelectedReport] = useState<ReportSummary | null>(null);
  const [handleForm] = Form.useForm();

  // ===== 查询待处理举报列表 =====
  const { data, isLoading } = useQuery({
    queryKey: ['pending-reports', currentPage, pageSize],
    queryFn: () => reportService.listPendingReports({
      page: currentPage - 1,
      size: pageSize,
    }),
  });

  // ===== 处理举报 Mutation =====
  const handleReportMutation = useMutation({
    mutationFn: async (payload: { id: number; approved: boolean; handleResult: string }) => {
      await reportService.handleReport(payload.id, { approved: payload.approved, handleResult: payload.handleResult });
    },
    onSuccess: () => {
      message.success('举报处理完成！🎉');
      queryClient.invalidateQueries({ queryKey: ['pending-reports'] });
      setHandleModalVisible(false);
      handleForm.resetFields();
      setSelectedReport(null);
    },
    onError: (error: any) => {
      message.error(`处理失败：${error.message} 😰`);
    },
  });

  // ===== 打开处理弹窗 =====
  const handleOpenModal = (report: ReportSummary) => {
    setSelectedReport(report);
    setHandleModalVisible(true);
  };

  // ===== 确认处理举报 =====
  const handleApprove = (approved: boolean) => {
    if (!selectedReport) return;

    handleForm.validateFields().then((values) => {
      handleReportMutation.mutate({
        id: selectedReport.id,
        approved,
        handleResult: values.handleResult,
      });
    });
  };

  // ===== 处理页码变化 =====
  const handlePageChange = (page: number, size?: number) => {
    setCurrentPage(page);
    if (size && size !== pageSize) {
      setPageSize(size);
    }
  };

  // ===== 获取目标类型标签 =====
  const getTargetTypeTag = (targetType: string) => {
    const colors: Record<string, string> = {
      GOODS: 'blue',
      POST: 'green',
      USER: 'orange',
      COMMENT: 'purple',
    };
    const labels: Record<string, string> = {
      GOODS: '商品',
      POST: '帖子',
      USER: '用户',
      COMMENT: '评论',
    };
    return (
      <Tag color={colors[targetType] || 'default'}>
        {labels[targetType] || targetType}
      </Tag>
    );
  };

  // ===== 获取状态标签 =====
  const getStatusTag = (status: string) => {
    if (status === 'PENDING') {
      return <Tag color="orange">待处理</Tag>;
    }
    if (status === 'APPROVED') {
      return <Tag color="green">已批准</Tag>;
    }
    if (status === 'REJECTED') {
      return <Tag color="red">已拒绝</Tag>;
    }
    return <Tag color="default">{status}</Tag>;
  };

  // ===== 表格列定义 =====
  const columns: ColumnsType<ReportSummary> = [
    {
      title: '举报ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '举报人',
      dataIndex: 'reporterName',
      key: 'reporterName',
      render: (text, record) => (
        <div>
          <div>{text || `用户${record.reporterId}`}</div>
          <Text type="secondary" style={{ fontSize: 12 }}>ID: {record.reporterId}</Text>
        </div>
      ),
    },
    {
      title: '举报对象',
      key: 'target',
      render: (_, record) => (
        <div>
          {getTargetTypeTag(record.targetType)}
          <div style={{ marginTop: 4 }}>
            <Text type="secondary">ID: {record.targetId}</Text>
          </div>
        </div>
      ),
    },
    {
      title: '举报原因',
      dataIndex: 'reason',
      key: 'reason',
      render: (text) => (
        <Tooltip title={text} placement="topLeft">
          <div style={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {text}
          </div>
        </Tooltip>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => getStatusTag(status),
    },
    {
      title: '举报时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (time) => new Date(time).toLocaleString(),
      width: 180,
    },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right',
      width: 120,
      render: (_, record) => (
        <PermissionGuard permission={PERMISSION_CODES.SYSTEM_REPORT_HANDLE}>
          <Space>
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleOpenModal(record)}
            >
              处理
            </Button>
          </Space>
        </PermissionGuard>
      ),
    },
  ];

  return (
    <div className="report-list" style={{ padding: '24px' }}>
      <PermissionGuard permission={PERMISSION_CODES.SYSTEM_REPORT_HANDLE}>
        <Card>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h2>📋 待处理举报</h2>
              <Text type="secondary">
                共 {data?.totalElements || 0} 条待处理举报
              </Text>
            </div>

            <Table
              columns={columns}
              dataSource={data?.content || []}
              rowKey="id"
              loading={isLoading}
              pagination={{
                current: currentPage,
                pageSize,
                total: data?.totalElements || 0,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total) => `共 ${total} 条记录`,
                onChange: handlePageChange,
                onShowSizeChange: handlePageChange,
              }}
              scroll={{ x: 1000 }}
            />
          </Space>
        </Card>
      </PermissionGuard>

      {/* 处理举报弹窗 */}
      <Modal
        title={`处理举报 #${selectedReport?.id}`}
        open={handleModalVisible}
        onCancel={() => {
          setHandleModalVisible(false);
          setSelectedReport(null);
          handleForm.resetFields();
        }}
        footer={[
          <Button key="reject" danger onClick={() => handleApprove(false)} loading={handleReportMutation.isPending}>
            <CloseCircleOutlined /> 拒绝举报
          </Button>,
          <Button key="approve" type="primary" onClick={() => handleApprove(true)} loading={handleReportMutation.isPending}>
            <CheckCircleOutlined /> 批准举报
          </Button>,
        ]}
        width={600}
      >
        {selectedReport && (
          <div>
            <Card size="small" style={{ marginBottom: 16 }}>
              <Space direction="vertical" style={{ width: '100%' }}>
                <div>
                  <Text strong>举报人：</Text> {selectedReport.reporterName || `用户${selectedReport.reporterId}`}
                </div>
                <div>
                  <Text strong>举报对象：</Text> {getTargetTypeTag(selectedReport.targetType)}
                  <Text style={{ marginLeft: 8 }}>ID: {selectedReport.targetId}</Text>
                </div>
                <div>
                  <Text strong>举报原因：</Text> {selectedReport.reason}
                </div>
                <div>
                  <Text strong>举报时间：</Text> {new Date(selectedReport.createdAt).toLocaleString()}
                </div>
              </Space>
            </Card>

            <Form form={handleForm} layout="vertical">
              <Form.Item
                name="handleResult"
                label="处理意见"
                rules={[{ required: true, message: '请填写处理意见！' }]}
              >
                <TextArea
                  rows={4}
                  placeholder="请详细说明处理理由和具体操作"
                />
              </Form.Item>
            </Form>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default ReportList;
