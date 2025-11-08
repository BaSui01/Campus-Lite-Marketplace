/**
 * 确认按钮组件
 * 
 * 功能：
 * - 点击后弹出确认对话框
 * - 支持自定义确认文案
 * - 支持异步操作
 * - 支持危险操作样式
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import React, { useState } from 'react';
import { Button, Modal } from 'antd';
import { ExclamationCircleOutlined } from '@ant-design/icons';
import type { ButtonProps } from 'antd';

/**
 * ConfirmButton 组件属性
 */
export interface ConfirmButtonProps extends Omit<ButtonProps, 'onClick'> {
  /** 确认标题 */
  title?: string;
  /** 确认内容 */
  content?: string;
  /** 确认回调 */
  onConfirm: () => void | Promise<void>;
  /** 取消回调 */
  onCancel?: () => void;
  /** 是否危险操作，默认 true */
  isDanger?: boolean;
}

/**
 * 确认按钮组件
 * 
 * @example
 * ```tsx
 * <ConfirmButton
 *   title="删除确认"
 *   content="确定要删除这条记录吗？删除后无法恢复！"
 *   onConfirm={async () => {
 *     await api.delete(id);
 *     message.success('删除成功');
 *   }}
 *   danger
 * >
 *   删除
 * </ConfirmButton>
 * ```
 */
export const ConfirmButton: React.FC<ConfirmButtonProps> = ({
  title = '操作确认',
  content = '确定要执行此操作吗？',
  onConfirm,
  onCancel,
  isDanger = true,
  children,
  ...buttonProps
}) => {
  const [loading, setLoading] = useState(false);

  /**
   * 处理点击
   */
  const handleClick = () => {
    Modal.confirm({
      title,
      content,
      icon: <ExclamationCircleOutlined />,
      okText: '确定',
      cancelText: '取消',
      okButtonProps: {
        danger: isDanger,
      },
      onOk: async () => {
        setLoading(true);
        try {
          await onConfirm();
        } finally {
          setLoading(false);
        }
      },
      onCancel,
    });
  };

  return (
    <Button {...buttonProps} onClick={handleClick} loading={loading}>
      {children}
    </Button>
  );
};
