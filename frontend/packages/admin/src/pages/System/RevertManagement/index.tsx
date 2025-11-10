/**
 * 撤销管理页面 - Admin端
 * @author BaSui 😎
 * @description 管理员管理数据撤销请求（待审批、历史记录、统计分析）
 */

import React, { useState, useCallback } from 'react';
import {
  Table,
  Button,
  Tag,
  Badge,
  Loading,
  toast,
  Modal,
  Form,
  FormItem,
  Input,
  type TableColumn,
  type RevertRequest,
  type RevertRequestStatus,
  type RevertExecutionResult
} from '@campus/shared';
import { revertService } from '@campus/shared';
import { revertManagementService } from '@/services';
import './index.css';

/**
 * 待审批请求数据
 */
interface PendingRequest extends RevertRequest {
  requesterName: string;
  entityType: string;
  entityName?: string;
  actionType: string;
  originalActionTime: string;
}

/**
 * 统计数据
 */
interface Statistics {
  pendingCount: number;
  todayRevertCount: number;
  successRate: number;
}

/**
 * 撤销管理页面
 */
const RevertManagement: React.FC = () => {
  // 状态管理
  const [activeTab, setActiveTab] = useState<'pending' | 'history'>('pending');
  const [loading, setLoading] = useState(false);
  const [pendingData, setPendingData] = useState<PendingRequest[]>([]);
  const [historyData, setHistoryData] = useState<RevertRequest[]>([]);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [statistics, setStatistics] = useState<Statistics>({
    pendingCount: 0,
    todayRevertCount: 0,
    successRate: 0
  });

  // 审批弹窗
  const [approvalModalVisible, setApprovalModalVisible] = useState(false);
  const [selectedRequest, setSelectedRequest] = useState<PendingRequest | null>(null);
  const [approvalComment, setApprovalComment] = useState('');
  const [approving, setApproving] = useState(false);

  // 加载数据
  React.useEffect(() => {
    loadData();
    loadStatistics();
  }, [activeTab, currentPage, pageSize]);

  const loadData = async () => {
    setLoading(true);

    try {
      if (activeTab === 'pending') {
        const response = await revertManagementService.listRequests(
          'PENDING',
          currentPage - 1,
          pageSize
        );
        setPendingData(response.content || []);
        setTotal(response.totalElements || 0);
      } else {
        const response = await revertManagementService.listRequests(
          undefined,
          currentPage - 1,
          pageSize
        );
        setHistoryData(response.content || []);
        setTotal(response.totalElements || 0);
      }
    } catch (error: any) {
      toast.error(error.message || '加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  const loadStatistics = async () => {
    try {
      const stats = await revertManagementService.getStatistics();
      setStatistics({
        pendingCount: stats?.pendingCount || 0,
        todayRevertCount: stats?.todayRevertCount || 0,
        successRate: stats?.successRate || 0
      });
    } catch (error: any) {
      console.error('加载统计数据失败', error);
    }
  };

  // 打开审批弹窗
  const handleOpenApproval = useCallback((request: PendingRequest) => {
    setSelectedRequest(request);
    setApprovalModalVisible(true);
    setApprovalComment('');
  }, []);

  // 批准撤销
  const handleApprove = useCallback(async () => {
    if (!selectedRequest) return;

    setApproving(true);

    try {
      await revertManagementService.approve(selectedRequest.id, approvalComment);

      toast.success('撤销申请已批准并执行');
      setApprovalModalVisible(false);
      setSelectedRequest(null);
      loadData();
      loadStatistics();
    } catch (error: any) {
      toast.error(error.message || '批准失败');
    } finally {
      setApproving(false);
    }
  }, [selectedRequest, approvalComment, loadData, loadStatistics]);

  // 拒绝撤销
  const handleReject = useCallback(async () => {
    if (!selectedRequest) return;
    if (!approvalComment || approvalComment.trim().length < 5) {
      toast.error('拒绝原因至少需要5个字符');
      return;
    }

    setApproving(true);

    try {
      await revertManagementService.reject(selectedRequest.id, approvalComment);

      toast.success('撤销申请已拒绝');
      setApprovalModalVisible(false);
      setSelectedRequest(null);
      loadData();
      loadStatistics();
    } catch (error: any) {
      toast.error(error.message || '拒绝失败');
    } finally {
      setApproving(false);
    }
  }, [selectedRequest, approvalComment, loadData, loadStatistics]);

  // 处理分页变化
  const handlePageChange = useCallback((page: number, size: number) => {
    setCurrentPage(page);
    setPageSize(size);
  }, []);

  // 待审批列表列定义
  const pendingColumns: TableColumn<PendingRequest>[] = [
    {
      key: 'id',
      title: '请求ID',
      dataIndex: 'id',
      width: 80,
      align: 'center'
    },
    {
      key: 'requesterName',
      title: '申请人',
      dataIndex: 'requesterName',
      width: 100,
      align: 'center'
    },
    {
      key: 'entityType',
      title: '实体类型',
      dataIndex: 'entityType',
      width: 100,
      align: 'center',
      render: (value: string) => {
        const colors: Record<string, any> = {
          Goods: 'blue',
          Order: 'green',
          User: 'orange'
        };
        return <Tag color={colors[value] || 'gray'}>{value}</Tag>;
      }
    },
    {
      key: 'entityName',
      title: '实体信息',
      dataIndex: 'entityName',
      width: 200
    },
    {
      key: 'actionType',
      title: '操作类型',
      dataIndex: 'actionType',
      width: 100,
      align: 'center',
      render: (value: string) => {
        const colors: Record<string, any> = {
          DELETE: 'red',
          UPDATE: 'orange',
          CREATE: 'green'
        };
        return <Tag color={colors[value] || 'gray'}>{value}</Tag>;
      }
    },
    {
      key: 'reason',
      title: '撤销原因',
      dataIndex: 'reason',
      width: 250,
      render: (value: string) => (
        <div className="revert-reason-text" title={value}>
          {value}
        </div>
      )
    },
    {
      key: 'createdAt',
      title: '申请时间',
      dataIndex: 'createdAt',
      width: 160,
      render: (value: string) => new Date(value).toLocaleString('zh-CN')
    },
    {
      key: 'actions',
      title: '操作',
      width: 120,
      align: 'center',
      render: (_: any, record: PendingRequest) => (
        <Button
          type="primary"
          size="small"
          onClick={() => handleOpenApproval(record)}
        >
          审批
        </Button>
      )
    }
  ];

  return (
    <div className="revert-management-page">
      {/* 页头 */}
      <div className="page-header">
        <h1 className="page-title">🔄 数据撤销管理</h1>
        <p className="page-description">
          管理用户的数据撤销请求，包括审批、执行和历史记录查询
        </p>
      </div>

      {/* 统计卡片 */}
      <div className="stats-cards">
        <div className="stat-card">
          <div className="stat-icon" style={{ background: '#fef3c7' }}>
            <span style={{ color: '#f59e0b' }}>⏳</span>
          </div>
          <div className="stat-content">
            <div className="stat-value">{statistics.pendingCount}</div>
            <div className="stat-label">待审批</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{ background: '#dbeafe' }}>
            <span style={{ color: '#3b82f6' }}>📊</span>
          </div>
          <div className="stat-content">
            <div className="stat-value">{statistics.todayRevertCount}</div>
            <div className="stat-label">今日撤销</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{ background: '#d1fae5' }}>
            <span style={{ color: '#10b981' }}>✓</span>
          </div>
          <div className="stat-content">
            <div className="stat-value">{statistics.successRate}%</div>
            <div className="stat-label">成功率</div>
          </div>
        </div>
      </div>

      {/* 标签页 */}
      <div className="page-tabs">
        <button
          className={`tab-button ${activeTab === 'pending' ? 'active' : ''}`}
          onClick={() => setActiveTab('pending')}
        >
          待审批 {statistics.pendingCount > 0 && (
            <Badge status="warning" text={statistics.pendingCount.toString()} />
          )}
        </button>
        <button
          className={`tab-button ${activeTab === 'history' ? 'active' : ''}`}
          onClick={() => setActiveTab('history')}
        >
          历史记录
        </button>
      </div>

      {/* 内容区域 */}
      <div className="page-content">
        {loading ? (
          <Loading type="spinner" size="large" />
        ) : activeTab === 'pending' ? (
          <Table
            columns={pendingColumns}
            dataSource={pendingData}
            rowKey="id"
            pagination={{
              current: currentPage,
              pageSize: pageSize,
              total: total,
              onChange: handlePageChange,
              showSizeChanger: true,
              showTotal: (total) => `共 ${total} 条`
            }}
          />
        ) : (
          <div className="empty-state">
            <p>暂无历史记录</p>
          </div>
        )}
      </div>

      {/* 审批弹窗 */}
      <Modal
        visible={approvalModalVisible}
        title="审批撤销申请"
        size="medium"
        onClose={() => {
          setApprovalModalVisible(false);
          setSelectedRequest(null);
        }}
        footer={
          <div className="modal-footer">
            <Button
              type="default"
              onClick={() => setApprovalModalVisible(false)}
            >
              取消
            </Button>
            <Button
              type="danger"
              onClick={handleReject}
              disabled={approving}
            >
              拒绝
            </Button>
            <Button
              type="primary"
              onClick={handleApprove}
              loading={approving}
            >
              批准
            </Button>
          </div>
        }
      >
        {selectedRequest && (
          <div className="approval-content">
            <div className="approval-info">
              <div className="info-row">
                <span className="info-label">申请人：</span>
                <span>{selectedRequest.requesterName}</span>
              </div>
              <div className="info-row">
                <span className="info-label">实体类型：</span>
                <Tag color="blue">{selectedRequest.entityType}</Tag>
              </div>
              <div className="info-row">
                <span className="info-label">实体信息：</span>
                <span>{selectedRequest.entityName}</span>
              </div>
              <div className="info-row">
                <span className="info-label">操作类型：</span>
                <Tag color="orange">{selectedRequest.actionType}</Tag>
              </div>
              <div className="info-row">
                <span className="info-label">撤销原因：</span>
                <p className="reason-text">{selectedRequest.reason}</p>
              </div>
            </div>

            <div className="approval-comment-section">
              <h4>审批意见（拒绝时必填）</h4>
              <Input
                type="textarea"
                value={approvalComment}
                onChange={(e) => setApprovalComment(e.target.value)}
                placeholder="请输入审批意见..."
                rows={3}
              />
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default RevertManagement;
