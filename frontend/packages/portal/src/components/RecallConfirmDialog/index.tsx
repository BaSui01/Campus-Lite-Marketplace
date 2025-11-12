/**
 * 消息撤回确认对话框组件 - 安全撤回专家！🔒
 *
 * @author BaSui 😎
 * @description 确认是否撤回消息的对话框组件
 * @date 2025-11-07
 */

import React from 'react';

/**
 * 撤回确认对话框属性
 */
export interface RecallConfirmDialogProps {
  /** 是否显示对话框 */
  visible: boolean;
  /** 消息内容预览 */
  messagePreview: string;
  /** 消息发送时间 */
  messageTime: string;
  /** 确认撤回回调 */
  onConfirm: () => void;
  /** 取消撤回回调 */
  onCancel: () => void;
  /** 是否正在撤回中 */
  loading?: boolean;
  /** 撤回时间限制（分钟） */
  timeLimit?: number;
  /** 剩余可撤回时间（秒） */
  remainingTime?: number;
  /** 自定义样式类名 */
  className?: string;
}

/**
 * 撤回确认对话框组件
 */
export const RecallConfirmDialog: React.FC<RecallConfirmDialogProps> = ({
  visible,
  messagePreview,
  messageTime,
  onConfirm,
  onCancel,
  loading = false,
  timeLimit = 5, // 默认5分钟内可撤回
  remainingTime,
  className = '',
}) => {
  if (!visible) return null;

  // 格式化消息预览（截取过长内容）
  const formatPreview = (text: string, maxLength: number = 50) => {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
  };

  // 检查是否可以撤回
  const canRecall = remainingTime === undefined || remainingTime > 0;

  // 格式化剩余时间
  const formatRemainingTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return minutes > 0 ? `${minutes}分${secs}秒` : `${secs}秒`;
  };

  return (
    <div className={`recall-confirm-dialog-overlay ${className}`}>
      {/* 背景遮罩 */}
      <div
        className="fixed inset-0 bg-black bg-opacity-50 z-40 flex items-center justify-center"
        onClick={onCancel}
      >
        {/* 对话框主体 */}
        <div
          className="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4 z-50"
          onClick={(e) => e.stopPropagation()}
        >
          {/* 对话框标题 */}
          <div className="flex items-center mb-4">
            <div className="w-10 h-10 bg-orange-100 rounded-full flex items-center justify-center mr-3">
              <svg className="w-6 h-6 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77.833-1.928.833-3.468 1.732L2.268 8.5c-.77.833-1.192.833-2.732 1.732L11.268 9.5c.77-.833 1.698-.833 3.468 1.732L18.732 8.5c.77-.833 1.192-1.928 1.732-3.468L14.732 4z" />
              </svg>
            </div>
            <div>
              <h3 className="text-lg font-semibold text-gray-900">撤回消息</h3>
              <p className="text-sm text-gray-600">确认要撤回这条消息吗？</p>
            </div>
          </div>

          {/* 消息预览 */}
          <div className="mb-4 p-3 bg-gray-50 rounded-lg border border-gray-200">
            <div className="flex items-center mb-2">
              <span className="text-xs text-gray-500">{messageTime}</span>
            </div>
            <p className="text-sm text-gray-800 break-words">
              {formatPreview(messagePreview)}
            </p>
          </div>

          {/* 时间限制提示 */}
          {remainingTime !== undefined && (
            <div className="mb-4 p-3 bg-blue-50 rounded-lg border border-blue-200">
              <div className="flex items-center">
                <svg className="w-4 h-4 text-blue-600 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clipRule="evenodd" />
                </svg>
                <span className="text-sm text-blue-800">
                  {canRecall
                    ? `还可撤回 ${formatRemainingTime(remainingTime)}`
                    : '已超过撤回时间限制'
                  }
                </span>
              </div>
              <div className="mt-2">
                <div className="w-full bg-blue-200 rounded-full h-2">
                  <div
                    className="bg-blue-600 h-2 rounded-full transition-all duration-1000"
                    style={{
                      width: `${Math.max(0, (remainingTime / (timeLimit * 60)) * 100)}%`
                    }}
                  />
                </div>
              </div>
            </div>
          )}

          {/* 通用提示 */}
          <div className="mb-6 p-3 bg-yellow-50 rounded-lg border border-yellow-200">
            <div className="flex items-start">
              <svg className="w-4 h-4 text-yellow-600 mr-2 mt-0.5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
              </svg>
              <div className="text-sm text-yellow-800">
                <p className="font-medium mb-1">撤回须知：</p>
                <ul className="list-disc list-inside space-y-1">
                  <li>撤回后，消息将从聊天记录中移除</li>
                  <li>对方将看到"消息已撤回"的提示</li>
                  <li>撤回操作不可恢复</li>
                  <li>消息发送后{timeLimit}分钟内可撤回</li>
                </ul>
              </div>
            </div>
          </div>

          {/* 操作按钮 */}
          <div className="flex space-x-3">
            <button
              onClick={onCancel}
              disabled={loading}
              className="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              取消
            </button>
            <button
              onClick={onConfirm}
              disabled={loading || !canRecall}
              className="flex-1 px-4 py-2 bg-orange-600 text-white rounded-lg hover:bg-orange-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
            >
              {loading ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  撤回中...
                </>
              ) : (
                '确认撤回'
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RecallConfirmDialog;