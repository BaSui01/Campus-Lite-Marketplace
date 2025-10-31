/**
 * Form 组件 - 表单管理大师！📝
 * @author BaSui 😎
 * @description 通用表单组件，支持表单验证、错误提示
 */

import React, { FormEvent } from 'react';
import './Form.css';

/**
 * 表单布局模式
 * - horizontal: 水平布局（标签在左，输入框在右）
 * - vertical: 垂直布局（标签在上，输入框在下）
 * - inline: 行内布局（紧凑排列）
 */
export type FormLayout = 'horizontal' | 'vertical' | 'inline';

/**
 * Form 组件的 Props 接口
 */
export interface FormProps {
  /**
   * 表单布局模式
   * @default 'vertical'
   */
  layout?: FormLayout;

  /**
   * 表单子元素
   */
  children?: React.ReactNode;

  /**
   * 表单提交回调
   */
  onSubmit?: (event: FormEvent<HTMLFormElement>) => void;

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
 * FormItem 组件的 Props 接口
 */
export interface FormItemProps {
  /**
   * 表单项标签
   */
  label?: React.ReactNode;

  /**
   * 表单项名称（用于表单提交）
   */
  name?: string;

  /**
   * 是否必填
   * @default false
   */
  required?: boolean;

  /**
   * 错误提示信息
   */
  error?: string;

  /**
   * 帮助提示信息
   */
  help?: string;

  /**
   * 表单项内容
   */
  children?: React.ReactNode;

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
 * Form 组件
 *
 * @example
 * ```tsx
 * <Form layout="vertical" onSubmit={handleSubmit}>
 *   <FormItem label="用户名" required>
 *     <Input placeholder="请输入用户名" />
 *   </FormItem>
 *
 *   <FormItem label="密码" required>
 *     <Input type="password" placeholder="请输入密码" />
 *   </FormItem>
 *
 *   <FormItem>
 *     <Button type="primary" htmlType="submit">
 *       提交
 *     </Button>
 *   </FormItem>
 * </Form>
 * ```
 */
export const Form: React.FC<FormProps> = ({
  layout = 'vertical',
  children,
  onSubmit,
  className = '',
  style,
}) => {
  // 处理表单提交
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onSubmit?.(event);
  };

  // 组装 CSS 类名
  const classNames = [
    'campus-form',
    `campus-form--${layout}`,
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <form className={classNames} style={style} onSubmit={handleSubmit}>
      {children}
    </form>
  );
};

/**
 * FormItem 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <FormItem label="用户名" required>
 *   <Input />
 * </FormItem>
 *
 * // 带错误提示
 * <FormItem label="邮箱" required error="邮箱格式不正确">
 *   <Input />
 * </FormItem>
 *
 * // 带帮助提示
 * <FormItem label="密码" help="密码长度至少 8 位">
 *   <Input type="password" />
 * </FormItem>
 * ```
 */
export const FormItem: React.FC<FormItemProps> = ({
  label,
  name,
  required = false,
  error,
  help,
  children,
  className = '',
  style,
}) => {
  // 组装 CSS 类名
  const classNames = [
    'campus-form-item',
    error ? 'campus-form-item--error' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={classNames} style={style}>
      {/* 标签 */}
      {label && (
        <label className="campus-form-item__label">
          {required && <span className="campus-form-item__required">*</span>}
          {label}
        </label>
      )}

      {/* 表单控件 */}
      <div className="campus-form-item__control">
        {/* 给子元素添加 name 属性 */}
        {React.Children.map(children, (child) => {
          if (React.isValidElement(child) && name) {
            return React.cloneElement(child as any, {
              name,
              error: !!error,
            });
          }
          return child;
        })}

        {/* 错误提示 */}
        {error && <div className="campus-form-item__error">{error}</div>}

        {/* 帮助提示 */}
        {!error && help && <div className="campus-form-item__help">{help}</div>}
      </div>
    </div>
  );
};

export default Form;
