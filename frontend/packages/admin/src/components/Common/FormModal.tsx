/**
 * 表单弹窗组件
 * 
 * 功能：
 * - 集成 useModal Hook
 * - 新增/编辑模式自动切换
 * - 表单验证
 * - 提交状态管理
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import React, { useEffect } from 'react';
import { Modal, Form, App, type FormInstance, type ModalProps } from 'antd';

/**
 * FormModal 组件属性
 */
export interface FormModalProps<T = any> extends Omit<ModalProps, 'onOk' | 'onCancel'> {
  /** 弹窗是否可见 */
  visible: boolean;
  /** 表单初始值（编辑模式） */
  initialValues?: T;
  /** 表单实例 */
  form?: FormInstance;
  /** 新增标题，默认"新增" */
  createTitle?: string;
  /** 编辑标题，默认"编辑" */
  editTitle?: string;
  /** 提交函数 */
  onSubmit: (values: T) => Promise<any>;
  /** 关闭回调 */
  onClose: () => void;
  /** 成功回调 */
  onSuccess?: () => void;
  /** 表单内容 */
  children: React.ReactNode;
  /** 弹窗宽度，默认 600 */
  width?: number | string;
}

/**
 * 表单弹窗组件
 * 
 * @example
 * ```tsx
 * const [form] = Form.useForm();
 * const { visible, data, open, close } = useModal<User>();
 * 
 * <Button onClick={() => open()}>新增</Button>
 * <Button onClick={() => open(record)}>编辑</Button>
 * 
 * <FormModal
 *   visible={visible}
 *   initialValues={data}
 *   form={form}
 *   createTitle="新增用户"
 *   editTitle="编辑用户"
 *   onSubmit={async (values) => {
 *     if (data) {
 *       return await api.update(data.id, values);
 *     } else {
 *       return await api.create(values);
 *     }
 *   }}
 *   onClose={close}
 *   onSuccess={() => {
 *     refetch();
 *   }}
 * >
 *   <Form.Item name="name" label="姓名" rules={[{ required: true }]}>
 *     <Input />
 *   </Form.Item>
 *   <Form.Item name="email" label="邮箱" rules={[{ required: true, type: 'email' }]}>
 *     <Input />
 *   </Form.Item>
 * </FormModal>
 * ```
 */
export const FormModal = <T extends Record<string, any>>({
  visible,
  initialValues,
  form: externalForm,
  createTitle = '新增',
  editTitle = '编辑',
  onSubmit,
  onClose,
  onSuccess,
  children,
  width = 600,
  ...modalProps
}: FormModalProps<T>) => {
  const { message } = App.useApp(); // ✅ 使用 App 提供的 message 实例
  const [internalForm] = Form.useForm();
  const form = externalForm || internalForm;
  const [submitting, setSubmitting] = React.useState(false);

  // 是否编辑模式
  const isEdit = !!initialValues;
  const title = isEdit ? editTitle : createTitle;

  /**
   * 设置表单初始值
   */
  useEffect(() => {
    if (visible) {
      if (initialValues) {
        form.setFieldsValue(initialValues);
      } else {
        form.resetFields();
      }
    }
  }, [visible, initialValues, form]);

  /**
   * 处理确定
   */
  const handleOk = async () => {
    try {
      // 验证表单
      const values = await form.validateFields();

      // 提交中
      setSubmitting(true);

      // 执行提交
      await onSubmit(values);

      // 成功提示
      message.success(`${title}成功`);

      // 关闭弹窗
      onClose();

      // 成功回调
      onSuccess?.();
    } catch (error: any) {
      // 表单验证失败
      if (error.errorFields) {
        console.log('表单验证失败:', error.errorFields);
        return;
      }

      // 提交失败
      console.error('表单提交失败:', error);
      message.error(error.message || `${title}失败`);
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * 处理取消
   */
  const handleCancel = () => {
    form.resetFields();
    onClose();
  };

  return (
    <Modal
      title={title}
      open={visible}
      onOk={handleOk}
      onCancel={handleCancel}
      confirmLoading={submitting}
      width={width}
      destroyOnClose
      {...modalProps}
    >
      <Form form={form} layout="vertical" preserve={false}>
        {children}
      </Form>
    </Modal>
  );
};
