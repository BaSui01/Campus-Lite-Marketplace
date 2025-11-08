/**
 * 申诉创建页面 - 用户端
 *
 * @author BaSui 😎
 * @description 用户提交申诉申请的表单页面
 * @date 2025-11-07
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { appealService } from '../../services';
import { AppealFileUploader } from './components/AppealFileUploader';
import type {
  CreateAppealRequest,
  CreateAppealRequestTargetTypeEnum,
  CreateAppealRequestAppealTypeEnum,
} from '@campus/shared/api';

/**
 * 目标类型配置
 */
const TARGET_TYPE_OPTIONS = [
  { value: 'USER_BAN', label: '账号封禁' },
  { value: 'USER_MUTE', label: '账号禁言' },
  { value: 'GOODS_DELETE', label: '商品删除' },
  { value: 'GOODS_OFFLINE', label: '商品下架' },
  { value: 'POST_DELETE', label: '帖子删除' },
  { value: 'REPLY_DELETE', label: '回复删除' },
  { value: 'ORDER_CANCEL', label: '订单取消' },
  { value: 'REPORT_REJECT', label: '举报驳回' },
];

/**
 * 申诉类型配置
 */
const APPEAL_TYPE_OPTIONS = [
  { value: 'UNJUST_BAN', label: '不当封禁' },
  { value: 'UNJUST_MUTE', label: '不当禁言' },
  { value: 'UNJUST_DELETE', label: '不当删除' },
  { value: 'UNJUST_OFFLINE', label: '不当下架' },
  { value: 'VIOLATION_REPORT', label: '违规举报' },
  { value: 'SYSTEM_ERROR', label: '系统错误' },
  { value: 'OTHER', label: '其他' },
];

/**
 * 申诉创建页面组件
 */
export const AppealCreate: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<Partial<CreateAppealRequest>>({
    userId: 0, // 实际应从当前用户上下文获取
    targetType: undefined,
    targetId: undefined,
    appealType: undefined,
    reason: '',
    attachments: [],
    notes: '',
  });

  /**
   * 表单字段更新
   */
  const updateField = <K extends keyof CreateAppealRequest>(
    key: K,
    value: CreateAppealRequest[K]
  ) => {
    setFormData((prev) => ({ ...prev, [key]: value }));
  };

  /**
   * 处理文件上传成功
   */
  const handleFileUploadSuccess = (files: any[]) => {
    // 将上传成功的文件URL添加到表单数据
    const urls = files.filter(file => file.status === 'success' && file.url).map(file => file.url);
    setFormData((prev) => ({
      ...prev,
      attachments: [...(prev.attachments || []), ...urls],
    }));
  };

  /**
   * 处理文件删除
   */
  const handleFileRemove = (fileId: string) => {
    // 从表单数据中移除对应的文件URL
    // 注意：这里需要根据实际的上传组件返回的数据结构来处理
    setFormData((prev) => ({
      ...prev,
      // 暂时保持原逻辑，后续可以根据需要优化
    }));
  };

  /**
   * 表单验证
   */
  const validateForm = (): string | null => {
    if (!formData.targetType) return '请选择目标类型';
    if (!formData.targetId || formData.targetId <= 0) return '请输入有效的目标ID';
    if (!formData.appealType) return '请选择申诉类型';
    if (!formData.reason || formData.reason.trim().length < 10) {
      return '申诉理由至少需要10个字符';
    }
    return null;
  };

  /**
   * 提交申诉
   */
  const handleSubmit = async () => {
    // 表单验证
    const error = validateForm();
    if (error) {
      alert(error);
      return;
    }

    setLoading(true);
    try {
      // TODO: 实际应从当前用户上下文获取 userId
      const request: CreateAppealRequest = {
        userId: 1, // 临时硬编码，实际应动态获取
        targetType: formData.targetType as CreateAppealRequestTargetTypeEnum,
        targetId: formData.targetId!,
        appealType: formData.appealType as CreateAppealRequestAppealTypeEnum,
        reason: formData.reason!,
        attachments: formData.attachments,
        notes: formData.notes,
      };

      const appealId = await appealService.submitAppeal(request);

      alert('申诉提交成功！申诉ID: ' + appealId);
      navigate('/appeals');
    } catch (error: any) {
      console.error('提交申诉失败:', error);
      alert('提交失败: ' + (error.message || '未知错误'));
    } finally {
      setLoading(false);
    }
  };

  /**
   * 返回列表
   */
  const handleCancel = () => {
    if (confirm('确定要取消提交吗？已填写的内容将丢失。')) {
      navigate('/appeals');
    }
  };

  return (
    <div className="max-w-3xl mx-auto p-6">
      {/* 页面头部 */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800">提交申诉</h1>
        <p className="text-sm text-gray-500 mt-1">
          请详细描述您的申诉理由，我们将尽快处理
        </p>
      </div>

      {/* 表单卡片 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6">
        {/* 目标类型 */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            目标类型 <span className="text-red-500">*</span>
          </label>
          <select
            value={formData.targetType || ''}
            onChange={(e) => updateField('targetType', e.target.value as any)}
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">请选择目标类型</option>
            {TARGET_TYPE_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {/* 目标ID */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            目标ID <span className="text-red-500">*</span>
          </label>
          <input
            type="number"
            value={formData.targetId || ''}
            onChange={(e) => updateField('targetId', parseInt(e.target.value) || 0)}
            placeholder="请输入目标ID（如商品ID、订单ID等）"
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <p className="text-xs text-gray-500 mt-1">
            提示：目标ID可在相关页面的URL或详情中找到
          </p>
        </div>

        {/* 申诉类型 */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            申诉类型 <span className="text-red-500">*</span>
          </label>
          <select
            value={formData.appealType || ''}
            onChange={(e) => updateField('appealType', e.target.value as any)}
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">请选择申诉类型</option>
            {APPEAL_TYPE_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {/* 申诉理由 */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            申诉理由 <span className="text-red-500">*</span>
          </label>
          <textarea
            value={formData.reason || ''}
            onChange={(e) => updateField('reason', e.target.value)}
            placeholder="请详细描述您的申诉理由（至少10个字符）"
            rows={6}
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
          />
          <p className="text-xs text-gray-500 mt-1">
            已输入 {formData.reason?.length || 0} 个字符
          </p>
        </div>

        {/* 申诉材料上传 */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            申诉材料（可选）
          </label>
          <div className="text-xs text-gray-500 mb-3">
            支持上传图片、PDF、Word文档作为申诉证据，有助于加快审核进度
          </div>
          <AppealFileUploader
            onUploadSuccess={handleFileUploadSuccess}
            onFileRemove={handleFileRemove}
            disabled={loading}
            className="mb-4"
          />
        </div>

        {/* 备注 */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            备注（可选）
          </label>
          <textarea
            value={formData.notes || ''}
            onChange={(e) => updateField('notes', e.target.value)}
            placeholder="其他需要说明的信息"
            rows={3}
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
          />
        </div>

        {/* 操作按钮 */}
        <div className="flex justify-end space-x-4">
          <button
            type="button"
            onClick={handleCancel}
            className="px-6 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
            disabled={loading}
          >
            取消
          </button>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={loading}
            className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? '提交中...' : '提交申诉'}
          </button>
        </div>
      </div>

      {/* 温馨提示 */}
      <div className="mt-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
        <h3 className="text-sm font-medium text-yellow-800 mb-2">温馨提示：</h3>
        <ul className="text-xs text-yellow-700 space-y-1 list-disc list-inside">
          <li>请确保提供的信息真实准确，虚假申诉将被驳回</li>
          <li>申诉理由请详细描述，有助于加快审核进度</li>
          <li>支持上传图片（JPG、PNG等）、PDF、Word文档作为申诉证据</li>
          <li>文件大小限制：图片≤10MB，文档≤20MB，最多5个文件</li>
          <li>申诉提交后，我们将在3个工作日内完成审核</li>
        </ul>
      </div>
    </div>
  );
};

export default AppealCreate;
