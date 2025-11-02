/**
 * 退款详情页面 - 查看退款详细信息！💰
 * @author BaSui 😎
 * @description 查看退款申请的详细信息、进度、凭证等
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button } from '@campus/shared/components';
import { refundService, RefundStatus, type RefundRequest } from '@campus/shared/services';
import { useNotificationStore } from '../../store';
import './RefundDetail.css';

// ==================== 类型定义 ====================

/**
 * 退款状态标签配置
 */
const STATUS_CONFIG: Record<RefundStatus, { text: string; color: string; emoji: string; description: string }> = {
  [RefundStatus.APPLIED]: { text: '待审核', color: '#F59E0B', emoji: '⏳', description: '你的退款申请已提交，等待平台审核' },
  [RefundStatus.APPROVED]: { text: '已通过', color: '#10B981', emoji: '✅', description: '审核已通过，即将发起退款' },
  [RefundStatus.REJECTED]: { text: '已拒绝', color: '#EF4444', emoji: '❌', description: '抱歉，你的退款申请未通过审核' },
  [RefundStatus.PROCESSING]: { text: '退款中', color: '#3B82F6', emoji: '⚡', description: '正在处理退款，请耐心等待' },
  [RefundStatus.REFUNDED]: { text: '已退款', color: '#22C55E', emoji: '🎉', description: '退款已完成，款项将在 1-3 个工作日内到账' },
  [RefundStatus.FAILED]: { text: '退款失败', color: '#DC2626', emoji: '⚠️', description: '退款处理失败，请联系客服' },
};

/**
 * 退款详情页面组件
 */
const RefundDetail: React.FC = () => {
  const navigate = useNavigate();
  const { refundNo } = useParams<{ refundNo: string }>();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [refund, setRefund] = useState<RefundRequest | null>(null);
  const [loading, setLoading] = useState(true);

  // ==================== 数据加载 ====================

  /**
   * 加载退款详情
   */
  const loadRefundDetail = async () => {
    if (!refundNo) {
      toast.error('退款单号不能为空！😭');
      navigate('/refunds');
      return;
    }

    setLoading(true);

    try {
      const response = await refundService.getMyRefund(refundNo);

      if (response.code === 200 && response.data) {
        setRefund(response.data);
      } else {
        toast.error(response.message || '退款详情不存在！😭');
        navigate('/refunds');
      }
    } catch (err: any) {
      console.error('加载退款详情失败:', err);
      toast.error(err.response?.data?.message || '加载退款详情失败！😭');
      navigate('/refunds');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRefundDetail();
  }, [refundNo]);

  // ==================== 事件处理 ====================

  /**
   * 格式化价格
   */
  const formatPrice = (amount?: number) => {
    if (!amount) return '¥0.00';
    return `¥${amount.toFixed(2)}`;
  };

  /**
   * 格式化日期
   */
  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString('zh-CN');
  };

  /**
   * 渲染凭证（图片或其他）
   */
  const renderEvidence = (evidence?: Record<string, any>) => {
    if (!evidence || Object.keys(evidence).length === 0) {
      return <div className="evidence-empty">暂无凭证</div>;
    }

    return (
      <div className="evidence-list">
        {evidence.images && Array.isArray(evidence.images) && (
          <div className="evidence-images">
            <label>图片凭证：</label>
            <div className="image-grid">
              {evidence.images.map((url: string, index: number) => (
                <img key={index} src={url} alt={`凭证${index + 1}`} className="evidence-image" />
              ))}
            </div>
          </div>
        )}
        {evidence.note && (
          <div className="evidence-note">
            <label>文字说明：</label>
            <div className="note-content">{evidence.note}</div>
          </div>
        )}
      </div>
    );
  };

  // ==================== 渲染 ====================

  if (loading) {
    return (
      <div className="refund-detail-page">
        <div className="refund-detail-container">
          <div className="loading-text">加载中...</div>
        </div>
      </div>
    );
  }

  if (!refund) {
    return null;
  }

  const statusConfig = STATUS_CONFIG[refund.status];

  return (
    <div className="refund-detail-page">
      <div className="refund-detail-container">
        {/* ==================== 头部 ==================== */}
        <div className="refund-detail-header">
          <Button type="default" size="small" onClick={() => navigate('/refunds')}>
            ← 返回列表
          </Button>
        </div>

        {/* ==================== 状态卡片 ==================== */}
        <div className="refund-status-card" style={{ borderColor: statusConfig.color }}>
          <div className="status-icon" style={{ color: statusConfig.color }}>
            {statusConfig.emoji}
          </div>
          <div className="status-content">
            <h2 className="status-title" style={{ color: statusConfig.color }}>
              {statusConfig.text}
            </h2>
            <p className="status-description">{statusConfig.description}</p>
          </div>
        </div>

        {/* ==================== 退款信息 ==================== */}
        <div className="refund-info-section">
          <h3 className="section-title">💰 退款信息</h3>
          <div className="info-card">
            <div className="info-item">
              <span className="label">退款单号：</span>
              <span className="value">{refund.refundNo}</span>
            </div>
            <div className="info-item">
              <span className="label">订单号：</span>
              <span className="value">{refund.orderNo}</span>
            </div>
            <div className="info-item">
              <span className="label">退款金额：</span>
              <span className="value price">{formatPrice(refund.amount)}</span>
            </div>
            <div className="info-item">
              <span className="label">支付渠道：</span>
              <span className="value">{refund.channel === 'ALIPAY' ? '支付宝' : refund.channel === 'WECHAT' ? '微信支付' : '未知'}</span>
            </div>
            <div className="info-item">
              <span className="label">申请时间：</span>
              <span className="value">{formatDate(refund.createdAt)}</span>
            </div>
            <div className="info-item">
              <span className="label">更新时间：</span>
              <span className="value">{formatDate(refund.updatedAt)}</span>
            </div>
          </div>
        </div>

        {/* ==================== 退款理由 ==================== */}
        <div className="refund-reason-section">
          <h3 className="section-title">📝 退款理由</h3>
          <div className="reason-card">
            <p>{refund.reason}</p>
          </div>
        </div>

        {/* ==================== 退款凭证 ==================== */}
        <div className="refund-evidence-section">
          <h3 className="section-title">📎 退款凭证</h3>
          <div className="evidence-card">{renderEvidence(refund.evidence)}</div>
        </div>

        {/* ==================== 错误信息（如果有） ==================== */}
        {refund.lastError && (
          <div className="refund-error-section">
            <h3 className="section-title">⚠️ 错误信息</h3>
            <div className="error-card">
              <p>{refund.lastError}</p>
              {refund.retryCount && refund.retryCount > 0 && <p className="retry-info">已重试次数：{refund.retryCount}</p>}
            </div>
          </div>
        )}

        {/* ==================== 操作按钮 ==================== */}
        <div className="refund-actions">
          <Button type="primary" size="large" onClick={() => navigate('/refunds')}>
            返回退款列表
          </Button>
        </div>
      </div>
    </div>
  );
};

export default RefundDetail;
