/**
 * 表单状态管理 Hook
 * 
 * 功能：
 * - 表单提交状态管理
 * - 表单验证
 * - 错误处理
 * - 成功回调
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import { useState } from 'react';
import { message } from 'antd';
import type { FormInstance } from 'antd';

/**
 * useForm Hook 参数
 */
export interface UseFormOptions<T = any> {
  /** 表单实例 */
  form: FormInstance;
  /** 提交函数 */
  onSubmit: (values: T) => Promise<any>;
  /** 成功回调 */
  onSuccess?: (result?: any) => void;
  /** 失败回调 */
  onError?: (error: any) => void;
  /** 成功提示消息 */
  successMessage?: string;
  /** 失败提示消息 */
  errorMessage?: string;
}

/**
 * useForm Hook 返回值
 */
export interface UseFormResult {
  /** 是否提交中 */
  submitting: boolean;
  /** 处理提交 */
  handleSubmit: () => Promise<void>;
  /** 处理重置 */
  handleReset: () => void;
}

/**
 * 表单状态管理 Hook
 * 
 * @example
 * ```tsx
 * const [form] = Form.useForm();
 * 
 * const { submitting, handleSubmit, handleReset } = useForm({
 *   form,
 *   onSubmit: async (values) => {
 *     return await api.create(values);
 *   },
 *   onSuccess: () => {
 *     navigate('/list');
 *   },
 *   successMessage: '创建成功',
 * });
 * 
 * <Form form={form} onFinish={handleSubmit}>
 *   <Form.Item name="name" rules={[{ required: true }]}>
 *     <Input />
 *   </Form.Item>
 *   <Button type="primary" htmlType="submit" loading={submitting}>
 *     提交
 *   </Button>
 *   <Button onClick={handleReset}>
 *     重置
 *   </Button>
 * </Form>
 * ```
 */
export const useForm = <T = any>(options: UseFormOptions<T>): UseFormResult => {
  const {
    form,
    onSubmit,
    onSuccess,
    onError,
    successMessage = '操作成功',
    errorMessage = '操作失败',
  } = options;

  const [submitting, setSubmitting] = useState(false);

  /**
   * 处理提交
   */
  const handleSubmit = async () => {
    try {
      // 验证表单
      const values = await form.validateFields();

      // 提交中状态
      setSubmitting(true);

      // 执行提交
      const result = await onSubmit(values);

      // 成功提示
      message.success(successMessage);

      // 成功回调
      onSuccess?.(result);
    } catch (error: any) {
      // 表单验证失败
      if (error.errorFields) {
        console.log('表单验证失败:', error.errorFields);
        return;
      }

      // 提交失败
      console.error('表单提交失败:', error);
      message.error(error.message || errorMessage);

      // 失败回调
      onError?.(error);
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * 处理重置
   */
  const handleReset = () => {
    form.resetFields();
  };

  return {
    submitting,
    handleSubmit,
    handleReset,
  };
};
