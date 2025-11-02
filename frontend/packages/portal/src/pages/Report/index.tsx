/**
 * 举报/投诉页面 - 共建和谐社区!🚨
 * @author BaSui 😎
 * @description 举报商品/用户/帖子,查看我的举报记录
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Button, Input, Skeleton, Tabs, Modal } from '@campus/shared/components';
import { useNotificationStore } from '../../store';
import { CreateReportRequest } from '@campus/shared/api/models';
import { getApi } from '@campus/shared/utils';
import './Report.css';

// ==================== 类型定义 ====================

type TargetType = 'GOODS' | 'POST' | 'REPLY' | 'USER';
type ReportStatus = 'PENDING' | 'HANDLED' | 'REJECTED';

interface Report {
  id: number;
  targetType: TargetType;
  targetId: number;
  reason: string;
  status: ReportStatus;
  createdAt: string;
  handleResult?: string;
}

/**
 * 举报页面组件
 */
const Report: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [activeTab, setActiveTab] = useState<'create' | 'my'>('create');
  const [reports, setReports] = useState<Report[]>([]);
  const [loading, setLoading] = useState(false);

  // 创建举报表单
  const [targetType, setTargetType] = useState<TargetType>('GOODS');
  const [targetId, setTargetId] = useState('');
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // API 实例
  const api = getApi();

  // ==================== 数据加载 ====================

  /**
   * 加载我的举报列表
   */
  const loadMyReports = async () => {
    setLoading(true);

    try {
      const response = await api.listMyReports({ page: 0, size: 50 });

      if (response.data.success && response.data.data) {
        const apiReports: Report[] = response.data.data.content.map((item: any) => ({
          id: item.id,
          targetType: item.targetType,
          targetId: item.targetId,
          reason: item.reason,
          status: item.status,
          createdAt: item.createdAt,
          handleResult: item.handleResult,
        }));

        setReports(apiReports);
      }
    } catch (err: any) {
      console.error('加载举报列表失败:', err);
      toast.error(err.response?.data?.message || '加载举报列表失败!😭');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // 从 URL 参数获取举报类型和 ID
    const type = searchParams.get('type') as TargetType;
    const id = searchParams.get('id');

    if (type && id) {
      setTargetType(type);
      setTargetId(id);
      setActiveTab('create');
    }

    if (activeTab === 'my') {
      loadMyReports();
    }
  }, [activeTab, searchParams]);

  // ==================== 事件处理 ====================

  /**
   * 提交举报
   */
  const handleSubmit = async () => {
    if (!targetId || !reason.trim()) {
      toast.warning('请填写完整信息!😰');
      return;
    }

    setSubmitting(true);

    try {
      const request: CreateReportRequest = {
        targetType,
        targetId: Number(targetId),
        reason: reason.trim(),
      };

      await api.createReport({ createReportRequest: request });

      toast.success('举报提交成功!我们会尽快处理!🚀');
      setTargetId('');
      setReason('');
      setActiveTab('my');
    } catch (err: any) {
      console.error('提交举报失败:', err);
      toast.error(err.response?.data?.message || '提交举报失败!😭');
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * 格式化状态
   */
  const formatStatus = (status: ReportStatus) => {
    const statusMap = {
      PENDING: { text: '待处理', color: '#faad14' },
      HANDLED: { text: '已处理', color: '#52c41a' },
      REJECTED: { text: '已驳回', color: '#f5222d' },
    };
    return statusMap[status];
  };

  /**
   * 格式化类型
   */
  const formatType = (type: TargetType) => {
    const typeMap = {
      GOODS: '商品',
      POST: '帖子',
      REPLY: '评论',
      USER: '用户',
    };
    return typeMap[type];
  };

  // ==================== 渲染 ====================

  return (
    <div className="report-page">
      <div className="report-container">
        {/* ==================== 头部 ==================== */}
        <div className="report-header">
          <h1 className="report-header__title">🚨 举报/投诉</h1>
          <p className="report-header__subtitle">共建和谐社区,从你我做起!</p>
        </div>

        {/* ==================== 标签切换 ==================== */}
        <div className="report-tabs">
          <Tabs
            activeKey={activeTab}
            onChange={(key) => setActiveTab(key as 'create' | 'my')}
            items={[
              { key: 'create', label: '📝 提交举报' },
              { key: 'my', label: '📋 我的举报' },
            ]}
          />
        </div>

        {/* ==================== 内容区域 ==================== */}
        <div className="report-content">
          {activeTab === 'create' ? (
            <div className="report-form">
              <div className="form-group">
                <label>举报类型</label>
                <select value={targetType} onChange={(e) => setTargetType(e.target.value as TargetType)}>
                  <option value="GOODS">商品</option>
                  <option value="POST">帖子</option>
                  <option value="REPLY">评论</option>
                  <option value="USER">用户</option>
                </select>
              </div>

              <div className="form-group">
                <label>目标ID</label>
                <Input
                  type="text"
                  placeholder={`请输入${formatType(targetType)}ID`}
                  value={targetId}
                  onChange={(e) => setTargetId(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label>举报理由</label>
                <textarea
                  placeholder="请详细描述举报理由..."
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  rows={6}
                  maxLength={500}
                />
                <div className="char-count">{reason.length}/500</div>
              </div>

              <div className="form-actions">
                <Button type="primary" size="large" onClick={handleSubmit} loading={submitting}>
                  提交举报
                </Button>
              </div>
            </div>
          ) : loading ? (
            <Skeleton type="list" count={5} animation="wave" />
          ) : reports.length === 0 ? (
            <div className="report-empty">
              <div className="empty-icon">📭</div>
              <h3 className="empty-text">还没有举报记录</h3>
              <p className="empty-tip">发现违规内容?点击上方提交举报吧!</p>
            </div>
          ) : (
            <div className="report-list">
              {reports.map((item) => (
                <div key={item.id} className="report-item">
                  <div className="report-item__header">
                    <span className="report-item__type">{formatType(item.targetType)}</span>
                    <span
                      className="report-item__status"
                      style={{ color: formatStatus(item.status).color }}
                    >
                      {formatStatus(item.status).text}
                    </span>
                  </div>
                  <div className="report-item__content">
                    <div className="report-item__reason">{item.reason}</div>
                    {item.handleResult && (
                      <div className="report-item__result">
                        <strong>处理结果:</strong> {item.handleResult}
                      </div>
                    )}
                  </div>
                  <div className="report-item__footer">
                    <span>目标ID: {item.targetId}</span>
                    <span>{new Date(item.createdAt).toLocaleString('zh-CN')}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Report;
