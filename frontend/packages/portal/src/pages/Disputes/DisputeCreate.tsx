/**
 * 纠纷创建页面 - 用户端
 *
 * @author BaSui 😎
 * @description 用户提交订单纠纷的表单页面
 * @date 2025-11-07
 */

import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { disputeService } from '../../services';
import type { CreateDisputeRequest, CreateDisputeRequestTypeEnum } from '@campus/shared/api';

/**
 * 纠纷类型配置
 */
const DISPUTE_TYPE_OPTIONS = [
  { value: 'REFUND', label: '退款纠纷', icon: '💰', description: '商品退款相关问题' },
  { value: 'QUALITY', label: '质量问题', icon: '❌', description: '商品质量不符合描述' },
  { value: 'SHIPPING', label: '物流问题', icon: '📦', description: '物流配送相关问题' },
  { value: 'SERVICE', label: '服务纠纷', icon: '💼', description: '卖家服务态度问题' },
  { value: 'OTHER', label: '其他', icon: '📝', description: '其他类型纠纷' },
];

/**
 * 纠纷创建页面组件
 */
export const DisputeCreate: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const orderNo = searchParams.get('orderNo') || ''; // 从URL获取订单编号

  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<Partial<CreateDisputeRequest>>({
    orderNo: orderNo,
    type: undefined,
    title: '',
    description: '',
    amount: undefined,
    evidence: [],
  });

  /**
   * 表单字段更新
   */
  const updateField = <K extends keyof CreateDisputeRequest>(
    key: K,
    value: CreateDisputeRequest[K]
  ) => {
    setFormData((prev) => ({ ...prev, [key]: value }));
  };

  /**
   * 证据URL添加
   */
  const addEvidence = () => {
    const url = prompt('请输入证据URL（图片或文件链接）：');
    if (url && url.trim()) {
      setFormData((prev) => ({
        ...prev,
        evidence: [...(prev.evidence || []), url.trim()],
      }));
    }
  };

  /**
   * 移除证据
   */
  const removeEvidence = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      evidence: prev.evidence?.filter((_, i) => i !== index),
    }));
  };

  /**
   * 表单验证
   */
  const validateForm = (): string | null => {
    if (!formData.orderNo || formData.orderNo.trim() === '') {
      return '请输入订单编号';
    }
    if (!formData.type) {
      return '请选择纠纷类型';
    }
    if (!formData.title || formData.title.trim().length < 5) {
      return '纠纷标题至少需要5个字符';
    }
    if (!formData.description || formData.description.trim().length < 20) {
      return '纠纷描述至少需要20个字符';
    }
    if (formData.amount !== undefined && formData.amount <= 0) {
      return '纠纷金额必须大于0';
    }
    return null;
  };

  /**
   * 提交纠纷
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
      const request: CreateDisputeRequest = {
        orderNo: formData.orderNo!,
        type: formData.type as CreateDisputeRequestTypeEnum,
        title: formData.title!,
        description: formData.description!,
        amount: formData.amount,
        evidence: formData.evidence,
      };

      const disputeId = await disputeService.submitDispute(request);

      alert('纠纷提交成功！纠纷ID: ' + disputeId);
      navigate('/disputes');
    } catch (error: any) {
      console.error('提交纠纷失败:', error);
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
      navigate('/disputes');
    }
  };

  /**
   * 渲染纠纷类型选择卡片
   */
  const renderTypeCard = (option: typeof DISPUTE_TYPE_OPTIONS[0]) => {
    const isSelected = formData.type === option.value;
    return (
      <div
        key={option.value}
        onClick={() => updateField('type', option.value as any)}
        className={`border-2 rounded-lg p-4 cursor-pointer transition-all ${
          isSelected
            ? 'border-blue-600 bg-blue-50'
            : 'border-gray-200 hover:border-blue-300 bg-white'
        }`}
      >
        <div className="flex items-center space-x-3">
          <span className="text-3xl">{option.icon}</span>
          <div className="flex-1">
            <h3 className={`text-sm font-semibold ${isSelected ? 'text-blue-600' : 'text-gray-800'}`}>
              {option.label}
            </h3>
            <p className="text-xs text-gray-500 mt-1">{option.description}</p>
          </div>
          {isSelected && (
            <div className="flex-shrink-0">
              <svg className="w-6 h-6 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fillRule="evenodd"
                  d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                  clipRule="evenodd"
                />
              </svg>
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <div className="max-w-3xl mx-auto p-6">
      {/* 页面头部 */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800">提交纠纷</h1>
        <p className="text-sm text-gray-500 mt-1">
          请详细描述您的纠纷情况，我们将尽快处理
        </p>
      </div>

      {/* 表单卡片 */}
      <div className="bg-white border border-gray-200 rounded-lg p-6">
        {/* 订单编号 */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            订单编号 <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            value={formData.orderNo || ''}
            onChange={(e) => updateField('orderNo', e.target.value)}
            placeholder="请输入订单编号（如 ORD20250107001）"
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <p className="text-xs text-gray-500 mt-1">
            提示：订单编号可在订单详情页找到
          </p>
        </div>

        {/* 纠纷类型 */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            纠纷类型 <span className="text-red-500">*</span>
          </label>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {DISPUTE_TYPE_OPTIONS.map(renderTypeCard)}
          </div>
        </div>

        {/* 纠纷标题 */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            纠纷标题 <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            value={formData.title || ''}
            onChange={(e) => updateField('title', e.target.value)}
            placeholder="请简明扼要地描述纠纷问题（至少5个字符）"
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <p className="text-xs text-gray-500 mt-1">
            已输入 {formData.title?.length || 0} 个字符
          </p>
        </div>

        {/* 纠纷描述 */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            纠纷描述 <span className="text-red-500">*</span>
          </label>
          <textarea
            value={formData.description || ''}
            onChange={(e) => updateField('description', e.target.value)}
            placeholder="请详细描述纠纷的具体情况、原因及您的诉求（至少20个字符）"
            rows={6}
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
          />
          <p className="text-xs text-gray-500 mt-1">
            已输入 {formData.description?.length || 0} 个字符
          </p>
        </div>

        {/* 纠纷金额 */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            纠纷金额（可选）
          </label>
          <div className="relative">
            <span className="absolute left-4 top-2 text-gray-500">¥</span>
            <input
              type="number"
              value={formData.amount || ''}
              onChange={(e) => updateField('amount', parseFloat(e.target.value) || undefined)}
              placeholder="0.00"
              min="0"
              step="0.01"
              className="w-full pl-8 pr-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <p className="text-xs text-gray-500 mt-1">
            如涉及退款，请填写纠纷金额
          </p>
        </div>

        {/* 证据附件 */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            证据附件（可选）
          </label>
          {formData.evidence && formData.evidence.length > 0 && (
            <ul className="mb-2 space-y-2">
              {formData.evidence.map((url, index) => (
                <li
                  key={index}
                  className="flex items-center justify-between p-2 bg-gray-50 rounded-md"
                >
                  <span className="text-sm text-gray-600 truncate">{url}</span>
                  <button
                    type="button"
                    onClick={() => removeEvidence(index)}
                    className="ml-2 text-red-500 hover:text-red-700"
                  >
                    删除
                  </button>
                </li>
              ))}
            </ul>
          )}
          <button
            type="button"
            onClick={addEvidence}
            className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            + 添加证据
          </button>
          <p className="text-xs text-gray-500 mt-1">
            可上传聊天记录截图、商品照片等作为证据
          </p>
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
            {loading ? '提交中...' : '提交纠纷'}
          </button>
        </div>
      </div>

      {/* 温馨提示 */}
      <div className="mt-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
        <h3 className="text-sm font-medium text-yellow-800 mb-2">温馨提示：</h3>
        <ul className="text-xs text-yellow-700 space-y-1 list-disc list-inside">
          <li>请确保提供的信息真实准确，虚假纠纷将被驳回</li>
          <li>纠纷描述请详细具体，有助于加快处理进度</li>
          <li>证据附件可以提供相关截图或照片作为证明</li>
          <li>纠纷提交后，双方将进入协商阶段</li>
          <li>如协商无果，可申请平台介入仲裁</li>
        </ul>
      </div>
    </div>
  );
};

export default DisputeCreate;
