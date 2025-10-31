/**
 * Modal 组件 - 模态框专家！🪟
 * @author BaSui 😎
 * @description 通用模态框组件，支持标题、内容、底部按钮
 */

import React, { useEffect } from 'react';
import { Button } from '../Button';
import './Modal.css';

/**
 * Modal 尺寸枚举
 */
export type ModalSize = 'small' | 'medium' | 'large' | 'fullscreen';

/**
 * Modal 组件的 Props 接口
 */
export interface ModalProps {
  /**
   * 是否显示
   * @default false
   */
  visible: boolean;

  /**
   * 模态框标题
   */
  title?: React.ReactNode;

  /**
   * 模态框内容
   */
  children?: React.ReactNode;

  /**
   * 模态框尺寸
   * @default 'medium'
   */
  size?: ModalSize;

  /**
   * 是否显示关闭按钮
   * @default true
   */
  closable?: boolean;

  /**
   * 是否显示遮罩层
   * @default true
   */
  mask?: boolean;

  /**
   * 点击遮罩层是否关闭
   * @default true
   */
  maskClosable?: boolean;

  /**
   * 确认按钮文字
   * @default '确定'
   */
  okText?: string;

  /**
   * 取消按钮文字
   * @default '取消'
   */
  cancelText?: string;

  /**
   * 是否显示确认按钮
   * @default true
   */
  showOkButton?: boolean;

  /**
   * 是否显示取消按钮
   * @default true
   */
  showCancelButton?: boolean;

  /**
   * 确认按钮是否加载中
   * @default false
   */
  confirmLoading?: boolean;

  /**
   * 自定义底部内容
   */
  footer?: React.ReactNode | null;

  /**
   * 关闭回调
   */
  onClose?: () => void;

  /**
   * 确认回调
   */
  onOk?: () => void | Promise<void>;

  /**
   * 取消回调
   */
  onCancel?: () => void;

  /**
   * 自定义类名
   */
  className?: string;

  /**
   * 自定义样式
   */
  style?: React.CSSProperties;
}

/**
 * Modal 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <Modal
 *   visible={visible}
 *   title="确认删除"
 *   onOk={() => console.log('确认')}
 *   onCancel={() => setVisible(false)}
 * >
 *   确定要删除这个商品吗？
 * </Modal>
 *
 * // 自定义底部
 * <Modal
 *   visible={visible}
 *   title="自定义底部"
 *   footer={
 *     <>
 *       <Button onClick={handleSave}>保存</Button>
 *       <Button type="primary" onClick={handleSubmit}>提交</Button>
 *     </>
 *   }
 * >
 *   内容
 * </Modal>
 *
 * // 无底部按钮
 * <Modal visible={visible} title="纯展示" footer={null}>
 *   只是看看，不能操作
 * </Modal>
 * ```
 */
export const Modal: React.FC<ModalProps> = ({
  visible,
  title,
  children,
  size = 'medium',
  closable = true,
  mask = true,
  maskClosable = true,
  okText = '确定',
  cancelText = '取消',
  showOkButton = true,
  showCancelButton = true,
  confirmLoading = false,
  footer,
  onClose,
  onOk,
  onCancel,
  className = '',
  style,
}) => {
  // 禁用 body 滚动
  useEffect(() => {
    if (visible) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [visible]);

  // ESC 键关闭
  useEffect(() => {
    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && visible && closable) {
        handleClose();
      }
    };

    if (visible) {
      document.addEventListener('keydown', handleEscape);
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
    };
  }, [visible, closable]);

  // 处理关闭
  const handleClose = () => {
    onClose?.();
    onCancel?.();
  };

  // 处理确认
  const handleOk = async () => {
    try {
      await onOk?.();
    } catch (error) {
      console.error('Modal onOk error:', error);
    }
  };

  // 处理取消
  const handleCancel = () => {
    onCancel?.();
  };

  // 处理遮罩层点击
  const handleMaskClick = () => {
    if (maskClosable) {
      handleClose();
    }
  };

  // 阻止内容区点击冒泡
  const handleContentClick = (e: React.MouseEvent) => {
    e.stopPropagation();
  };

  if (!visible) {
    return null;
  }

  // 默认底部内容
  const defaultFooter = (
    <div className="campus-modal__footer-buttons">
      {showCancelButton && (
        <Button onClick={handleCancel} disabled={confirmLoading}>
          {cancelText}
        </Button>
      )}
      {showOkButton && (
        <Button type="primary" onClick={handleOk} loading={confirmLoading}>
          {okText}
        </Button>
      )}
    </div>
  );

  return (
    <div className="campus-modal-root">
      {/* 遮罩层 */}
      {mask && <div className="campus-modal-mask" onClick={handleMaskClick}></div>}

      {/* Modal 内容 */}
      <div className={`campus-modal-wrapper ${className}`} onClick={handleMaskClick}>
        <div
          className={`campus-modal campus-modal--${size}`}
          style={style}
          onClick={handleContentClick}
        >
          {/* 标题栏 */}
          {(title || closable) && (
            <div className="campus-modal__header">
              {title && <div className="campus-modal__title">{title}</div>}
              {closable && (
                <button className="campus-modal__close" onClick={handleClose}>
                  ✕
                </button>
              )}
            </div>
          )}

          {/* 内容区 */}
          <div className="campus-modal__body">{children}</div>

          {/* 底部区 */}
          {footer !== null && <div className="campus-modal__footer">{footer || defaultFooter}</div>}
        </div>
      </div>
    </div>
  );
};

export default Modal;
