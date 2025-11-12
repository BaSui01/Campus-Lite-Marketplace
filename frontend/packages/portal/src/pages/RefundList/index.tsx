/**
 * 退款列表页面 - 查看我的退款申请！💰
 * @author BaSui 😎
 * @description 用户查看自己的退款列表、筛选、查看详情
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Pagination, Select, Empty } from '@campus/shared/components';
import { refundService, RefundStatus, type RefundRequest } from '@campus/shared/services';;
import { useNotificationStore } from '../../store';
import './RefundList.css';

// ==================== 类型定义 ====================

/**
 * 退款状态标签配置
 */
const STATUS_CONFIG: Record<RefundStatus, { text: string; color: string; emoji: string }> = {
  [RefundStatus.APPLIED]: { text: '待审核', color: '#F59E0B', emoji: '⏳' },
  [RefundStatus.APPROVED]: { text: '已通过', color: '#10B981', emoji: '✅' },
  [RefundStatus.REJECTED]: { text: '已拒绝', color: '#EF4444', emoji: '❌' },
  [RefundStatus.PROCESSING]: { text: '退款中', color: '#3B82F6', emoji: '⚡' },
  [RefundStatus.REFUNDED]: { text: '已退款', color: '#22C55E', emoji: '🎉' },
  [RefundStatus.FAILED]: { text: '退款失败', color: '#DC2626', emoji: '⚠️' },
};

/**
 * 退款列表页面组件
 */
const RefundList: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [refunds, setRefunds] = useState<RefundRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(10);

  // 筛选条件
  const [statusFilter, setStatusFilter] = useState<RefundStatus | undefined>(undefined);

  // ==================== 数据加载 ====================

  /**
   * 加载退款列表
   */
  const loadRefunds = async () => {
    setLoading(true);

    try {
      const response = await refundService.listMyRefunds({
        page: currentPage - 1, // 后端从0开始
        size: pageSize,
        status: statusFilter,
      });

      if (response.code === 200 && response.data) {
        setRefunds(response.data.content || []);
        setTotalPages(response.data.totalPages || 0);
      } else {
        toast.error(response.message || '加载退款列表失败！😭');
      }
    } catch (err: any) {
      console.error('加载退款列表失败:', err);
      toast.error(err.response?.data?.message || '加载退款列表失败！😭');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRefunds();
  }, [currentPage, statusFilter]);

  // ==================== 事件处理 ====================

  /**
   * 查看退款详情
   */
  const handleViewDetail = (refundNo: string) => {
    navigate(`/refunds/${refundNo}`);
  };

  /**
   * 状态筛选变化
   */
  const handleStatusFilterChange = (value: string) => {
    setStatusFilter(value ? (value as RefundStatus) : undefined);
    setCurrentPage(1); // 重置到第一页
  };

  /**
   * 分页变化
   */
  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

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
   * 渲染状态标签
   */
  const renderStatusBadge = (status: RefundStatus) => {
    const config = STATUS_CONFIG[status];
    return (
      <span className="refund-status-badge" style={{ color: config.color, borderColor: config.color }}>
        {config.emoji} {config.text}
      </span>
    );
  };

  // ==================== 渲染 ====================

  return (
    <div className="refund-list-page">
      <div className="refund-list-container">
        {/* ==================== 头部 ==================== */}
        <div className="refund-list-header">
          <h1 className="refund-list-header__title">💰 我的退款</h1>
          <p className="refund-list-header__subtitle">查看退款申请进度</p>
        </div>

        {/* ==================== 筛选栏 ==================== */}
        <div className="refund-list-filters">
          <div className="filter-item">
            <label>退款状态：</label>
            <Select
              value={statusFilter || ''}
              onChange={handleStatusFilterChange}
              placeholder="全部状态"
              style={{ width: 180 }}
            >
              <Select.Option value="">全部状态</Select.Option>
              <Select.Option value={RefundStatus.APPLIED}>⏳ 待审核</Select.Option>
              <Select.Option value={RefundStatus.PROCESSING}>⚡ 退款中</Select.Option>
              <Select.Option value={RefundStatus.REFUNDED}>🎉 已退款</Select.Option>
              <Select.Option value={RefundStatus.REJECTED}>❌ 已拒绝</Select.Option>
              <Select.Option value={RefundStatus.FAILED}>⚠️ 退款失败</Select.Option>
            </Select>
          </div>
        </div>

        {/* ==================== 退款列表 ==================== */}
        {loading ? (
          <div className="refund-list-loading">
            <div className="loading-text">加载中...</div>
          </div>
        ) : refunds.length === 0 ? (
          <Empty description="暂无退款记录" />
        ) : (
          <>
            <div className="refund-list">
              {refunds.map((refund) => (
                <div key={refund.refundNo} className="refund-card">
                  <div className="refund-card-header">
                    <div className="refund-card-title">
                      <span className="refund-no">退款单号：{refund.refundNo}</span>
                      {renderStatusBadge(refund.status)}
                    </div>
                    <div className="refund-card-time">{formatDate(refund.createdAt)}</div>
                  </div>

                  <div className="refund-card-body">
                    <div className="refund-info-item">
                      <span className="label">订单号：</span>
                      <span className="value">{refund.orderNo}</span>
                    </div>
                    <div className="refund-info-item">
                      <span className="label">退款金额：</span>
                      <span className="value price">{formatPrice(refund.amount)}</span>
                    </div>
                    <div className="refund-info-item">
                      <span className="label">退款理由：</span>
                      <span className="value">{refund.reason}</span>
                    </div>
                    {refund.lastError && (
                      <div className="refund-info-item error">
                        <span className="label">错误信息：</span>
                        <span className="value">{refund.lastError}</span>
                      </div>
                    )}
                  </div>

                  <div className="refund-card-footer">
                    <Button type="primary" size="small" onClick={() => handleViewDetail(refund.refundNo)}>
                      查看详情
                    </Button>
                  </div>
                </div>
              ))}
            </div>

            {/* ==================== 分页 ==================== */}
            {totalPages > 1 && (
              <div className="refund-list-pagination">
                <Pagination current={currentPage} total={totalPages * pageSize} pageSize={pageSize} onChange={handlePageChange} />
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default RefundList;
