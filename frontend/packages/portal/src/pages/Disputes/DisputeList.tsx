/**
 * 纠纷列表页面 - 用户端
 *
 * @author BaSui 😎
 * @description 用户查看自己提交的纠纷列表
 * @date 2025-11-07
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { disputeService, DisputeStatus } from '../../services';
import type { DisputeDTO } from '@campus/shared/api/models';

/**
 * 纠纷状态显示配置
 */
const STATUS_CONFIG = {
  [DisputeStatus.NEGOTIATING]: {
    label: '协商中',
    color: 'text-blue-500',
    bgColor: 'bg-blue-100',
    icon: '💬',
    description: '双方正在协商解决'
  },
  [DisputeStatus.PENDING_ARBITRATION]: {
    label: '待仲裁',
    color: 'text-orange-500',
    bgColor: 'bg-orange-100',
    icon: '⏳',
    description: '等待平台介入仲裁'
  },
  [DisputeStatus.ARBITRATING]: {
    label: '仲裁中',
    color: 'text-purple-500',
    bgColor: 'bg-purple-100',
    icon: '⚖️',
    description: '平台正在仲裁处理'
  },
  [DisputeStatus.RESOLVED]: {
    label: '已解决',
    color: 'text-green-500',
    bgColor: 'bg-green-100',
    icon: '✅',
    description: '纠纷已成功解决'
  },
  [DisputeStatus.CLOSED]: {
    label: '已关闭',
    color: 'text-gray-400',
    bgColor: 'bg-gray-50',
    icon: '🔒',
    description: '纠纷已关闭'
  },
};

/**
 * 纠纷列表页面组件
 */
export const DisputeList: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);
  const [disputes, setDisputes] = useState<DisputeDTO[]>([]);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [total, setTotal] = useState(0);
  const [statusFilter, setStatusFilter] = useState<string | undefined>(
    searchParams.get('status') || undefined
  );

  /**
   * 加载纠纷列表
   */
  const loadDisputes = async () => {
    setLoading(true);
    try {
      const response = await disputeService.getUserDisputes({
        status: statusFilter as any,
        page,
        size,
      });

      if (response.code === 200 && response.data) {
        setDisputes(response.data.content || []);
        setTotal(response.data.totalElements || 0);
      } else {
        console.error('加载纠纷列表失败:', response.message);
      }
    } catch (error) {
      console.error('加载纠纷列表异常:', error);
    } finally {
      setLoading(false);
    }
  };

  /**
   * 页面加载时获取数据
   */
  useEffect(() => {
    loadDisputes();
  }, [page, statusFilter]);

  /**
   * 状态筛选变更
   */
  const handleStatusChange = (status: string | undefined) => {
    setStatusFilter(status);
    setPage(0);
    if (status) {
      setSearchParams({ status });
    } else {
      setSearchParams({});
    }
  };

  /**
   * 查看纠纷详情
   */
  const viewDetail = (disputeId: number) => {
    navigate(`/disputes/${disputeId}`);
  };

  /**
   * 创建新纠纷
   */
  const createDispute = () => {
    navigate('/disputes/create');
  };

  /**
   * 渲染纠纷状态标签
   */
  const renderStatus = (status: string) => {
    const config = STATUS_CONFIG[status as DisputeStatus] || STATUS_CONFIG[DisputeStatus.NEGOTIATING];
    return (
      <div className="flex items-center space-x-2">
        <span className="text-xl">{config.icon}</span>
        <span
          className={`px-2 py-1 rounded-full text-xs font-medium ${config.color} ${config.bgColor}`}
          title={config.description}
        >
          {config.label}
        </span>
      </div>
    );
  };

  /**
   * 渲染纠纷卡片
   */
  const renderDisputeCard = (dispute: DisputeDTO) => (
    <div
      key={dispute.id}
      className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
      onClick={() => viewDetail(dispute.id)}
    >
      {/* 头部：订单编号 + 状态 */}
      <div className="flex justify-between items-center mb-2">
        <span className="text-sm text-gray-500">订单编号：{dispute.orderNo}</span>
        {renderStatus(dispute.status)}
      </div>

      {/* 标题 */}
      <h3 className="text-lg font-semibold text-gray-800 mb-2">{dispute.title || '订单纠纷'}</h3>

      {/* 描述 */}
      <p className="text-sm text-gray-600 mb-3 line-clamp-2">{dispute.description}</p>

      {/* 底部：时间 + 纠纷类型 */}
      <div className="flex justify-between items-center text-xs text-gray-500">
        <span>提交时间：{new Date(dispute.createdAt).toLocaleDateString()}</span>
        <span className="px-2 py-1 bg-gray-100 rounded">
          {dispute.type === 'REFUND' ? '退款纠纷' : dispute.type === 'QUALITY' ? '质量问题' : '其他'}
        </span>
      </div>
    </div>
  );

  /**
   * 渲染状态筛选器
   */
  const renderStatusFilter = () => (
    <div className="bg-white border border-gray-200 rounded-lg p-4 mb-4">
      <div className="flex items-center space-x-2 overflow-x-auto">
        <button
          onClick={() => handleStatusChange(undefined)}
          className={`px-4 py-2 rounded-md text-sm font-medium whitespace-nowrap transition-colors ${
            !statusFilter
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          全部
        </button>
        {Object.entries(STATUS_CONFIG).map(([status, config]) => (
          <button
            key={status}
            onClick={() => handleStatusChange(status)}
            className={`px-4 py-2 rounded-md text-sm font-medium whitespace-nowrap transition-colors flex items-center space-x-1 ${
              statusFilter === status
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            <span>{config.icon}</span>
            <span>{config.label}</span>
          </button>
        ))}
      </div>
    </div>
  );

  /**
   * 渲染分页按钮
   */
  const renderPagination = () => {
    const totalPages = Math.ceil(total / size);
    if (totalPages <= 1) return null;

    return (
      <div className="flex justify-center items-center space-x-2 mt-6">
        <button
          onClick={() => setPage(page - 1)}
          disabled={page === 0}
          className="px-4 py-2 bg-white border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          上一页
        </button>
        <span className="text-sm text-gray-600">
          第 {page + 1} / {totalPages} 页
        </span>
        <button
          onClick={() => setPage(page + 1)}
          disabled={page >= totalPages - 1}
          className="px-4 py-2 bg-white border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          下一页
        </button>
      </div>
    );
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      {/* 页面头部 */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">我的纠纷</h1>
          <p className="text-sm text-gray-500 mt-1">查看和管理您提交的纠纷记录</p>
        </div>
        <button
          onClick={createDispute}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
        >
          + 提交纠纷
        </button>
      </div>

      {/* 状态筛选器 */}
      {renderStatusFilter()}

      {/* 加载状态 */}
      {loading && (
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="text-gray-500 mt-2">加载中...</p>
        </div>
      )}

      {/* 纠纷列表 */}
      {!loading && disputes.length > 0 && (
        <div className="space-y-4">
          {disputes.map(renderDisputeCard)}
        </div>
      )}

      {/* 空状态 */}
      {!loading && disputes.length === 0 && (
        <div className="text-center py-12 bg-gray-50 rounded-lg">
          <svg
            className="mx-auto h-12 w-12 text-gray-400"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
            />
          </svg>
          <h3 className="mt-2 text-sm font-medium text-gray-900">
            {statusFilter ? `暂无${STATUS_CONFIG[statusFilter as DisputeStatus]?.label}的纠纷` : '暂无纠纷记录'}
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            {statusFilter ? '试试切换其他状态查看' : '您还没有提交过纠纷'}
          </p>
          {!statusFilter && (
            <button
              onClick={createDispute}
              className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
            >
              提交第一个纠纷
            </button>
          )}
        </div>
      )}

      {/* 分页 */}
      {renderPagination()}
    </div>
  );
};

export default DisputeList;
