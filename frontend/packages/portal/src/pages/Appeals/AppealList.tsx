/**
 * 申诉列表页面 - 用户端
 *
 * @author BaSui 😎
 * @description 用户查看自己提交的申诉列表
 * @date 2025-11-07
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { appealService } from '../../services';
import type { Appeal } from '@campus/shared/api/models';

/**
 * 申诉状态枚举
 */
enum AppealStatus {
  PENDING = 'PENDING', // 待处理
  REVIEWING = 'REVIEWING', // 审核中
  APPROVED = 'APPROVED', // 已通过
  REJECTED = 'REJECTED', // 已驳回
  EXPIRED = 'EXPIRED', // 已过期
}

/**
 * 申诉状态显示配置
 */
const STATUS_CONFIG = {
  [AppealStatus.PENDING]: { label: '待处理', color: 'text-gray-500', bgColor: 'bg-gray-100' },
  [AppealStatus.REVIEWING]: { label: '审核中', color: 'text-blue-500', bgColor: 'bg-blue-100' },
  [AppealStatus.APPROVED]: { label: '已通过', color: 'text-green-500', bgColor: 'bg-green-100' },
  [AppealStatus.REJECTED]: { label: '已驳回', color: 'text-red-500', bgColor: 'bg-red-100' },
  [AppealStatus.EXPIRED]: { label: '已过期', color: 'text-gray-400', bgColor: 'bg-gray-50' },
};

/**
 * 申诉列表页面组件
 */
export const AppealList: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [appeals, setAppeals] = useState<Appeal[]>([]);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [total, setTotal] = useState(0);

  /**
   * 加载申诉列表
   */
  const loadAppeals = async () => {
    setLoading(true);
    try {
      const response = await appealService.getMyAppeals({ page, size });

      if (response.code === 200 && response.data) {
        setAppeals(response.data.content || []);
        setTotal(response.data.totalElements || 0);
      } else {
        console.error('加载申诉列表失败:', response.message);
      }
    } catch (error) {
      console.error('加载申诉列表异常:', error);
    } finally {
      setLoading(false);
    }
  };

  /**
   * 页面加载时获取数据
   */
  useEffect(() => {
    loadAppeals();
  }, [page]);

  /**
   * 查看申诉详情
   */
  const viewDetail = (appealId: number) => {
    navigate(`/appeals/${appealId}`);
  };

  /**
   * 创建新申诉
   */
  const createAppeal = () => {
    navigate('/appeals/create');
  };

  /**
   * 渲染申诉状态标签
   */
  const renderStatus = (status: string) => {
    const config = STATUS_CONFIG[status as AppealStatus] || STATUS_CONFIG[AppealStatus.PENDING];
    return (
      <span
        className={`px-2 py-1 rounded-full text-xs font-medium ${config.color} ${config.bgColor}`}
      >
        {config.label}
      </span>
    );
  };

  /**
   * 渲染申诉卡片
   */
  const renderAppealCard = (appeal: Appeal) => (
    <div
      key={appeal.id}
      className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
      onClick={() => viewDetail(appeal.id)}
    >
      {/* 头部：编号 + 状态 */}
      <div className="flex justify-between items-center mb-2">
        <span className="text-sm text-gray-500">申诉编号：{appeal.appealNo}</span>
        {renderStatus(appeal.status)}
      </div>

      {/* 标题 */}
      <h3 className="text-lg font-semibold text-gray-800 mb-2">{appeal.title}</h3>

      {/* 描述 */}
      <p className="text-sm text-gray-600 mb-3 line-clamp-2">{appeal.description}</p>

      {/* 底部：时间 + 材料数量 */}
      <div className="flex justify-between items-center text-xs text-gray-500">
        <span>提交时间：{new Date(appeal.createdAt).toLocaleDateString()}</span>
        <span>材料数量：{appeal.materialsCount} 个</span>
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
          <h1 className="text-2xl font-bold text-gray-800">我的申诉</h1>
          <p className="text-sm text-gray-500 mt-1">查看和管理您提交的申诉记录</p>
        </div>
        <button
          onClick={createAppeal}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
        >
          + 提交申诉
        </button>
      </div>

      {/* 加载状态 */}
      {loading && (
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="text-gray-500 mt-2">加载中...</p>
        </div>
      )}

      {/* 申诉列表 */}
      {!loading && appeals.length > 0 && (
        <div className="space-y-4">
          {appeals.map(renderAppealCard)}
        </div>
      )}

      {/* 空状态 */}
      {!loading && appeals.length === 0 && (
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
          <h3 className="mt-2 text-sm font-medium text-gray-900">暂无申诉记录</h3>
          <p className="mt-1 text-sm text-gray-500">您还没有提交过申诉</p>
          <button
            onClick={createAppeal}
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
          >
            提交第一个申诉
          </button>
        </div>
      )}

      {/* 分页 */}
      {renderPagination()}
    </div>
  );
};

export default AppealList;
