/**
 * 纠纷详情页面 - 用户端
 *
 * @author BaSui 😎
 * @description 查看纠纷详细信息、订单、证据、协商记录
 * @date 2025-11-07
 */

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { disputeService, DisputeStatus } from '../../services';
import { ChatInterface } from './components/ChatInterface';
import { useDisputeChat } from './hooks/useDisputeChat';
import SearchPanel from '@/components/SearchPanel';
import type { DisputeDetailDTO } from '@campus/shared/api/models';

/**
 * 纠纷状态显示配置
 */
const STATUS_CONFIG = {
  [DisputeStatus.NEGOTIATING]: {
    label: '协商中',
    color: 'text-blue-500',
    bgColor: 'bg-blue-100',
    icon: '💬',
  },
  [DisputeStatus.PENDING_ARBITRATION]: {
    label: '待仲裁',
    color: 'text-orange-500',
    bgColor: 'bg-orange-100',
    icon: '⏳',
  },
  [DisputeStatus.ARBITRATING]: {
    label: '仲裁中',
    color: 'text-purple-500',
    bgColor: 'bg-purple-100',
    icon: '⚖️',
  },
  [DisputeStatus.RESOLVED]: {
    label: '已解决',
    color: 'text-green-500',
    bgColor: 'bg-green-100',
    icon: '✅',
  },
  [DisputeStatus.CLOSED]: {
    label: '已关闭',
    color: 'text-gray-400',
    bgColor: 'bg-gray-50',
    icon: '🔒',
  },
};

/**
 * 纠纷详情页面组件
 */
export const DisputeDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [detail, setDetail] = useState<DisputeDetailDTO | null>(null);
  const [escalating, setEscalating] = useState(false);
  const [showSearchPanel, setShowSearchPanel] = useState(false);

  // 聊天相关状态 - 临时数据，实际应该从用户上下文获取
  const [currentUserId] = useState(1); // 临时硬编码
  const [currentUserRole] = useState<'buyer' | 'seller'>('seller'); // 临时硬编码

  // 获取对方用户信息
  const getOtherUser = () => {
    if (!detail?.dispute) return null;

    // 临时逻辑：根据当前用户角色推断对方信息
    // 实际应该从detail中获取准确的用户信息
    return {
      id: currentUserRole === 'buyer' ? 2 : 1,
      name: currentUserRole === 'buyer' ? '卖家张三' : '买家李四',
      role: currentUserRole === 'buyer' ? 'seller' : 'buyer',
      avatar: undefined,
    };
  };

  // 聊天Hook
  const chatState = useDisputeChat({
    disputeId: parseInt(id!),
    currentUserId,
    onMessage: (message) => {
      console.log('收到新消息:', message);
    },
    onConnectionChange: (isConnected) => {
      console.log('连接状态变化:', isConnected);
    },
    onError: (error) => {
      console.error('聊天错误:', error);
    },
  });

  /**
   * 加载纠纷详情
   */
  const loadDetail = async () => {
    if (!id) {
      alert('纠纷ID无效');
      navigate('/disputes');
      return;
    }

    setLoading(true);
    try {
      const response = await disputeService.getDisputeDetail(parseInt(id));

      if (response.code === 200 && response.data) {
        setDetail(response.data);
      } else {
        alert('加载失败: ' + response.message);
        navigate('/disputes');
      }
    } catch (error: any) {
      console.error('加载纠纷详情异常:', error);
      alert('加载失败: ' + (error.message || '未知错误'));
      navigate('/disputes');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 升级为仲裁
   */
  const handleEscalate = async () => {
    if (!id || !detail) return;

    if (!confirm('确定要升级为仲裁吗？平台将介入处理此纠纷。')) {
      return;
    }

    setEscalating(true);
    try {
      await disputeService.escalateToArbitration(parseInt(id));
      alert('已升级为仲裁，平台将尽快处理');
      loadDetail(); // 重新加载详情
    } catch (error: any) {
      console.error('升级纠纷失败:', error);
      alert('升级失败: ' + (error.message || '未知错误'));
    } finally {
      setEscalating(false);
    }
  };

  /**
   * 页面加载时获取数据
   */
  useEffect(() => {
    loadDetail();
  }, [id]);

  /**
   * 处理键盘快捷键
   */
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      // Ctrl+F 或 Cmd+F 打开搜索
      if ((event.ctrlKey || event.metaKey) && event.key === 'f') {
        event.preventDefault();
        setShowSearchPanel(true);
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, []);

  /**
   * 渲染纠纷状态标签
   */
  const renderStatus = (status: string) => {
    const config = STATUS_CONFIG[status as DisputeStatus] || STATUS_CONFIG[DisputeStatus.NEGOTIATING];
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
   * 渲染证据列表
   */
  const renderEvidence = () => {
    if (!detail?.dispute?.evidence || detail.dispute.evidence.length === 0) {
      return (
        <div className="text-center py-8 text-gray-500">
          <p>暂无证据材料</p>
        </div>
      );
    }

    return (
      <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
        {detail.dispute.evidence.map((url, index) => (
          <div
            key={index}
            className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
          >
            {url.match(/\.(jpg|jpeg|png|gif|webp)$/i) ? (
              <img
                src={url}
                alt={`证据${index + 1}`}
                className="w-full h-32 object-cover rounded-md mb-2"
              />
            ) : (
              <div className="w-full h-32 bg-gray-100 rounded-md flex items-center justify-center mb-2">
                <span className="text-4xl">📄</span>
              </div>
            )}
            <a
              href={url}
              target="_blank"
              rel="noopener noreferrer"
              className="text-xs text-blue-600 hover:underline block truncate"
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
    if (!detail?.dispute) return null;

    const events = [
      {
        time: detail.dispute.createdAt,
        title: '纠纷提交',
        description: '纠纷已成功提交，双方进入协商阶段',
        icon: '📝',
      },
    ];

    if (detail.dispute.status === DisputeStatus.PENDING_ARBITRATION || detail.dispute.status === DisputeStatus.ARBITRATING) {
      events.push({
        time: new Date().toISOString(), // 临时使用当前时间
        title: '升级仲裁',
        description: '已升级为仲裁，平台将介入处理',
        icon: '⚖️',
      });
    }

    if (detail.dispute.status === DisputeStatus.RESOLVED) {
      events.push({
        time: detail.dispute.resolvedAt || new Date().toISOString(),
        title: '纠纷解决',
        description: detail.dispute.resolution || '纠纷已成功解决',
        icon: '✅',
      });
    }

    if (detail.dispute.status === DisputeStatus.CLOSED) {
      events.push({
        time: detail.dispute.closedAt || new Date().toISOString(),
        title: '纠纷关闭',
        description: detail.dispute.closeReason || '纠纷已关闭',
        icon: '🔒',
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
  if (!detail || !detail.dispute) {
    return (
      <div className="max-w-4xl mx-auto p-6">
        <div className="text-center py-12">
          <p className="text-gray-500">纠纷不存在</p>
          <button
            onClick={() => navigate('/disputes')}
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            返回列表
          </button>
        </div>
      </div>
    );
  }

  const { dispute, order } = detail;

  return (
    <div className="max-w-4xl mx-auto p-6">
      {/* 页面头部 */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">纠纷详情</h1>
          <p className="text-sm text-gray-500 mt-1">纠纷ID：{dispute.id}</p>
        </div>
        <button
          onClick={() => navigate('/disputes')}
          className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          ← 返回列表
        </button>
      </div>

      {/* 状态卡片 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
        <div className="flex justify-between items-center">
          {renderStatus(dispute.status)}
          {dispute.status === DisputeStatus.NEGOTIATING && (
            <button
              onClick={handleEscalate}
              disabled={escalating}
              className="px-4 py-2 bg-orange-600 text-white rounded-md hover:bg-orange-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {escalating ? '升级中...' : '升级为仲裁'}
            </button>
          )}
        </div>
      </div>

      {/* 订单信息 */}
      {order && (
        <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-800 mb-4">关联订单</h2>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span className="text-gray-500">订单编号：</span>
              <span className="text-gray-800 font-medium">{order.orderNo}</span>
            </div>
            <div>
              <span className="text-gray-500">订单金额：</span>
              <span className="text-gray-800 font-medium">¥{order.totalAmount}</span>
            </div>
            <div>
              <span className="text-gray-500">商品名称：</span>
              <span className="text-gray-800 font-medium">{order.goodsTitle || 'N/A'}</span>
            </div>
            <div>
              <span className="text-gray-500">卖家：</span>
              <span className="text-gray-800 font-medium">{order.sellerName || 'N/A'}</span>
            </div>
          </div>
        </div>
      )}

      {/* 基本信息 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">纠纷信息</h2>
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <span className="text-gray-500">纠纷类型：</span>
            <span className="text-gray-800 font-medium">
              {dispute.type === 'REFUND' ? '退款纠纷' : dispute.type === 'QUALITY' ? '质量问题' : '其他'}
            </span>
          </div>
          <div>
            <span className="text-gray-500">提交时间：</span>
            <span className="text-gray-800 font-medium">
              {new Date(dispute.createdAt).toLocaleString()}
            </span>
          </div>
          <div>
            <span className="text-gray-500">纠纷金额：</span>
            <span className="text-gray-800 font-medium">
              {dispute.amount ? `¥${dispute.amount}` : 'N/A'}
            </span>
          </div>
        </div>
      </div>

      {/* 纠纷标题 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">纠纷标题</h2>
        <p className="text-gray-700">{dispute.title}</p>
      </div>

      {/* 纠纷描述 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">纠纷描述</h2>
        <p className="text-gray-700 whitespace-pre-wrap">{dispute.description}</p>
      </div>

      {/* 协商沟通 */}
      {dispute.status === DisputeStatus.NEGOTIATING && (
        <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold text-gray-800">协商沟通</h2>
            <button
              onClick={() => setShowSearchPanel(true)}
              className="flex items-center space-x-2 px-3 py-2 text-sm bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 transition-colors"
              title="搜索聊天记录 (Ctrl+F)"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
              <span>搜索记录</span>
            </button>
          </div>
          <div className="h-96 border border-gray-200 rounded-lg">
            {getOtherUser() ? (
              <ChatInterface
                disputeId={dispute.id}
                currentUserId={currentUserId}
                currentUserRole={currentUserRole}
                otherUser={getOtherUser()!}
                disputeStatus={dispute.status}
                onMessageSent={(message) => {
                  console.log('消息已发送:', message);
                }}
              />
            ) : (
              <div className="flex items-center justify-center h-full text-gray-500">
                <p>正在获取用户信息...</p>
              </div>
            )}
          </div>

          {/* 聊天状态提示 */}
          <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
            <div className="flex items-center space-x-2">
              <svg className="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0z" clipRule="evenodd" />
                <path d="M14 7.5a.5.5 0 00-.5-.5v-1a.5.5 0 00-.5-.5h-1a.5.5 0 00-.5.5v1a.5.5 0 00.5.5h1z" />
              </svg>
              <div className="text-sm text-blue-800">
                <p className="font-medium">协商沟通提示</p>
                <p className="text-xs mt-1">
                  请保持友好协商，理性沟通。所有对话记录将作为仲裁参考。
                  {chatState.isConnected ? '连接正常' : '连接异常，请刷新页面'}
                  按 <kbd className="px-1 py-0.5 bg-gray-100 rounded text-xs">Ctrl+F</kbd> 搜索聊天记录
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 非协商状态的提示 */}
      {dispute.status !== DisputeStatus.NEGOTIATING && (
        <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-800 mb-4">协商沟通</h2>
          <div className="text-center py-8 text-gray-500">
            <svg className="w-12 h-12 text-gray-300 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77.833-1.928.833-3.468 1.732L2.268 8.5c-.77.833-1.192.833-2.732 1.732L11.268 9.5c.77-.833 1.698-.833 3.468 1.732L18.732 8.5c.77-.833 1.192-1.928 1.732-3.468L14.732 4z" />
            </svg>
            <p className="text-lg font-medium mb-2">
              {dispute.status === DisputeStatus.PENDING_ARBITRATION && '纠纷已升级为仲裁'}
              {dispute.status === DisputeStatus.ARBITRATING && '仲裁进行中'}
              {dispute.status === DisputeStatus.RESOLVED && '纠纷已解决'}
              {dispute.status === DisputeStatus.CLOSED && '纠纷已关闭'}
            </p>
            <p className="text-sm">
              {dispute.status === DisputeStatus.PENDING_ARBITRATION && '请等待仲裁员处理'}
              {dispute.status === DisputeStatus.ARBITRATING && '仲裁员正在处理此纠纷'}
              {dispute.status === DisputeStatus.RESOLVED && '纠纷已成功解决'}
              {dispute.status === DisputeStatus.CLOSED && '此纠纷已关闭'}
            </p>
          </div>
        </div>
      )}

      {/* 证据材料 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">证据材料</h2>
        {renderEvidence()}
      </div>

      {/* 处理结果 */}
      {dispute.resolution && (
        <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-800 mb-4">处理结果</h2>
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="text-sm text-gray-700 whitespace-pre-wrap">{dispute.resolution}</p>
          </div>
        </div>
      )}

      {/* 时间轴 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">处理进度</h2>
        {renderTimeline()}
      </div>

      {/* 搜索面板 */}
      <SearchPanel
        visible={showSearchPanel}
        onClose={() => setShowSearchPanel(false)}
        currentUserId={currentUserId}
        disputeId={dispute.id}
        showAdvancedFilters={true}
        placeholder="搜索协商聊天记录..."
      />
    </div>
  );
};

export default DisputeDetail;
