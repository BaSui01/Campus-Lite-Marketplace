/**
 * 申诉详情页面 - 用户端
 *
 * @author BaSui 😎
 * @description 查看申诉详细信息、材料、审核结果
 * @date 2025-11-07
 */

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { appealService } from '../../services';
import type { AppealDetailResponse } from '@campus/shared/api/models';

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
  [AppealStatus.PENDING]: { label: '待处理', color: 'text-gray-500', bgColor: 'bg-gray-100', icon: '⏳' },
  [AppealStatus.REVIEWING]: { label: '审核中', color: 'text-blue-500', bgColor: 'bg-blue-100', icon: '🔍' },
  [AppealStatus.APPROVED]: { label: '已通过', color: 'text-green-500', bgColor: 'bg-green-100', icon: '✅' },
  [AppealStatus.REJECTED]: { label: '已驳回', color: 'text-red-500', bgColor: 'bg-red-100', icon: '❌' },
  [AppealStatus.EXPIRED]: { label: '已过期', color: 'text-gray-400', bgColor: 'bg-gray-50', icon: '⏱️' },
};

/**
 * 申诉详情页面组件
 */
export const AppealDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [detail, setDetail] = useState<AppealDetailResponse | null>(null);
  const [canceling, setCanceling] = useState(false);

  /**
   * 加载申诉详情
   */
  const loadDetail = async () => {
    if (!id) {
      alert('申诉ID无效');
      navigate('/appeals');
      return;
    }

    setLoading(true);
    try {
      const response = await appealService.getAppealDetail(parseInt(id));

      if (response.code === 200 && response.data) {
        setDetail(response.data);
      } else {
        alert('加载失败: ' + response.message);
        navigate('/appeals');
      }
    } catch (error: any) {
      console.error('加载申诉详情异常:', error);
      alert('加载失败: ' + (error.message || '未知错误'));
      navigate('/appeals');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 取消申诉
   */
  const handleCancel = async () => {
    if (!id || !detail) return;

    if (!confirm('确定要取消此申诉吗？此操作不可撤销。')) {
      return;
    }

    setCanceling(true);
    try {
      await appealService.cancelAppeal(parseInt(id));
      alert('申诉已取消');
      navigate('/appeals');
    } catch (error: any) {
      console.error('取消申诉失败:', error);
      alert('取消失败: ' + (error.message || '未知错误'));
    } finally {
      setCanceling(false);
    }
  };

  /**
   * 页面加载时获取数据
   */
  useEffect(() => {
    loadDetail();
  }, [id]);

  /**
   * 渲染申诉状态标签
   */
  const renderStatus = (status: string) => {
    const config = STATUS_CONFIG[status as AppealStatus] || STATUS_CONFIG[AppealStatus.PENDING];
    return (
      <div className="flex items-center space-x-2">
        <span className="text-2xl">{config.icon}</span>
        <span
          className={`px-3 py-1 rounded-full text-sm font-medium ${config.color} ${config.bgColor}`}
        >
          {config.label}
        </span>
      </div>
    );
  };

  /**
   * 渲染材料列表
   */
  const renderMaterials = () => {
    if (!detail?.appeal?.materials || detail.appeal.materials.length === 0) {
      return (
        <div className="text-center py-8 text-gray-500">
          <p>暂无材料</p>
        </div>
      );
    }

    return (
      <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
        {detail.appeal.materials.map((material, index) => (
          <div
            key={index}
            className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
          >
            {material.type === 'IMAGE' ? (
              <img
                src={material.url}
                alt={`材料${index + 1}`}
                className="w-full h-32 object-cover rounded-md mb-2"
              />
            ) : (
              <div className="w-full h-32 bg-gray-100 rounded-md flex items-center justify-center mb-2">
                <span className="text-4xl">📄</span>
              </div>
            )}
            <p className="text-xs text-gray-600 truncate">{material.fileName || '附件'}</p>
            <a
              href={material.url}
              target="_blank"
              rel="noopener noreferrer"
              className="text-xs text-blue-600 hover:underline"
            >
              查看原文件
            </a>
          </div>
        ))}
      </div>
    );
  };

  /**
   * 渲染时间轴
   */
  const renderTimeline = () => {
    if (!detail?.appeal) return null;

    const events = [
      {
        time: detail.appeal.createdAt,
        title: '申诉提交',
        description: '您已成功提交申诉',
        icon: '📝',
      },
    ];

    if (detail.appeal.reviewedAt) {
      events.push({
        time: detail.appeal.reviewedAt,
        title: detail.appeal.status === 'APPROVED' ? '申诉通过' : '申诉驳回',
        description:
          detail.appeal.reviewReason || (detail.appeal.status === 'APPROVED' ? '审核通过' : '审核驳回'),
        icon: detail.appeal.status === 'APPROVED' ? '✅' : '❌',
      });
    }

    return (
      <div className="space-y-4">
        {events.map((event, index) => (
          <div key={index} className="flex">
            <div className="flex flex-col items-center mr-4">
              <div className="flex items-center justify-center w-10 h-10 bg-blue-100 rounded-full">
                <span className="text-xl">{event.icon}</span>
              </div>
              {index < events.length - 1 && (
                <div className="w-0.5 h-16 bg-gray-300 mt-2"></div>
              )}
            </div>
            <div className="flex-1 pb-8">
              <h4 className="text-sm font-semibold text-gray-800">{event.title}</h4>
              <p className="text-xs text-gray-500 mt-1">
                {new Date(event.time).toLocaleString()}
              </p>
              <p className="text-sm text-gray-600 mt-2">{event.description}</p>
            </div>
          </div>
        ))}
      </div>
    );
  };

  // 加载状态
  if (loading) {
    return (
      <div className="max-w-4xl mx-auto p-6">
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <p className="text-gray-500 mt-4">加载中...</p>
        </div>
      </div>
    );
  }

  // 数据为空
  if (!detail || !detail.appeal) {
    return (
      <div className="max-w-4xl mx-auto p-6">
        <div className="text-center py-12">
          <p className="text-gray-500">申诉不存在</p>
          <button
            onClick={() => navigate('/appeals')}
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            返回列表
          </button>
        </div>
      </div>
    );
  }

  const { appeal } = detail;

  return (
    <div className="max-w-4xl mx-auto p-6">
      {/* 页面头部 */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">申诉详情</h1>
          <p className="text-sm text-gray-500 mt-1">申诉编号：{appeal.appealNo}</p>
        </div>
        <button
          onClick={() => navigate('/appeals')}
          className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          ← 返回列表
        </button>
      </div>

      {/* 状态卡片 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
        <div className="flex justify-between items-center">
          {renderStatus(appeal.status)}
          {appeal.status === 'PENDING' && (
            <button
              onClick={handleCancel}
              disabled={canceling}
              className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {canceling ? '取消中...' : '取消申诉'}
            </button>
          )}
        </div>
      </div>

      {/* 基本信息 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">基本信息</h2>
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <span className="text-gray-500">申诉类型：</span>
            <span className="text-gray-800 font-medium">{appeal.type}</span>
          </div>
          <div>
            <span className="text-gray-500">提交时间：</span>
            <span className="text-gray-800 font-medium">
              {new Date(appeal.createdAt).toLocaleString()}
            </span>
          </div>
          <div>
            <span className="text-gray-500">目标ID：</span>
            <span className="text-gray-800 font-medium">{appeal.relatedId || 'N/A'}</span>
          </div>
          {appeal.expireAt && (
            <div>
              <span className="text-gray-500">过期时间：</span>
              <span className="text-gray-800 font-medium">
                {new Date(appeal.expireAt).toLocaleString()}
              </span>
            </div>
          )}
        </div>
      </div>

      {/* 申诉理由 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">申诉理由</h2>
        <p className="text-gray-700 whitespace-pre-wrap">{appeal.description}</p>
      </div>

      {/* 附件材料 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">附件材料</h2>
        {renderMaterials()}
      </div>

      {/* 审核结果 */}
      {appeal.reviewReason && (
        <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-800 mb-4">审核结果</h2>
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="text-sm text-gray-600 mb-2">
              <span className="font-medium">审核人员：</span>
              {appeal.reviewerName || '系统'}
            </p>
            <p className="text-sm text-gray-600 mb-2">
              <span className="font-medium">审核时间：</span>
              {appeal.reviewedAt && new Date(appeal.reviewedAt).toLocaleString()}
            </p>
            <p className="text-sm text-gray-700">
              <span className="font-medium">审核意见：</span>
              {appeal.reviewReason}
            </p>
          </div>
        </div>
      )}

      {/* 时间轴 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">处理进度</h2>
        {renderTimeline()}
      </div>
    </div>
  );
};

export default AppealDetail;
