/**
 * Input 组件 - 输入框界的全能王！⌨️
 * @author BaSui 😎
 * @description 通用输入框组件，支持文本、密码、数字、搜索等多种类型
 */

import React, { useState } from 'react';
import './Input.css';

/**
 * 输入框类型枚举
 * - text: 文本输入（最常用）
 * - password: 密码输入（带眼睛图标）
 * - number: 数字输入（只能输数字）
 * - search: 搜索输入（带搜索图标）
 * - email: 邮箱输入（带邮箱格式验证）
 * - tel: 电话输入（带电话格式）
 */
export type InputType = 'text' | 'password' | 'number' | 'search' | 'email' | 'tel';

/**
 * 输入框尺寸枚举
 */
export type InputSize = 'large' | 'medium' | 'small';

/**
 * Input 组件的 Props 接口
 */
export interface InputProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'size' | 'prefix'> {
  /**
   * 输入框类型
   * @default 'text'
   */
  type?: InputType;

  /**
   * 输入框尺寸
   * @default 'medium'
   */
  size?: InputSize;

  /**
   * 是否禁用
   * @default false
   */
  disabled?: boolean;

  /**
   * 是否只读
   * @default false
   */
  readOnly?: boolean;

  /**
   * 输入框的值
   */
  value?: string | number;

  /**
   * 默认值
   */
  defaultValue?: string | number;

  /**
   * 占位符文本
   */
  placeholder?: string;

  /**
   * 最大长度
   */
  maxLength?: number;

  /**
   * 是否显示清除按钮
   * @default false
   */
  allowClear?: boolean;

  /**
   * 前缀图标
   */
  prefix?: React.ReactNode;

  /**
   * 后缀图标
   */
  suffix?: React.ReactNode;

  /**
   * 是否有错误状态
   * @default false
   */
  error?: boolean;

  /**
   * 错误提示信息
   */
  errorMessage?: string;

  /**
   * 值变化回调
   */
  onChange?: (event: React.ChangeEvent<HTMLInputElement>) => void;

  /**
   * 获得焦点回调
   */
  onFocus?: (event: React.FocusEvent<HTMLInputElement>) => void;

  /**
   * 失去焦点回调
   */
  onBlur?: (event: React.FocusEvent<HTMLInputElement>) => void;

  /**
   * 按下回车键回调
   */
  onPressEnter?: (event: React.KeyboardEvent<HTMLInputElement>) => void;

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
 * Input 组件
 *
 * @example
 * ```tsx
 * // 文本输入框
 * <Input placeholder="请输入用户名" />
 *
 * // 密码输入框
 * <Input type="password" placeholder="请输入密码" />
 *
 * // 带前缀图标的输入框
 * <Input prefix={<UserIcon />} placeholder="用户名" />
 *
 * // 带清除按钮的输入框
 * <Input allowClear placeholder="可清除" />
 *
 * // 错误状态的输入框
 * <Input error errorMessage="用户名不能为空！" />
 * ```
 */
export const Input: React.FC<InputProps> = ({
  type = 'text',
  size = 'medium',
  disabled = false,
  readOnly = false,
  value,
  defaultValue,
  placeholder,
  maxLength,
  allowClear = false,
  prefix,
  suffix,
  error = false,
  errorMessage,
  onChange,
  onFocus,
  onBlur,
  onPressEnter,
  className = '',
  style,
  ...rest
}) => {
  // 内部状态：是否显示密码
  const [showPassword, setShowPassword] = useState(false);

  // 内部状态：是否有焦点
  const [isFocused, setIsFocused] = useState(false);

  // 组装 CSS 类名
  const wrapperClassNames = [
    'campus-input-wrapper',
    `campus-input-wrapper--${size}`,
    isFocused ? 'campus-input-wrapper--focused' : '',
    error ? 'campus-input-wrapper--error' : '',
    disabled ? 'campus-input-wrapper--disabled' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  // 处理键盘事件
  const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      onPressEnter?.(event);
    }
  };

  // 处理焦点事件
  const handleFocus = (event: React.FocusEvent<HTMLInputElement>) => {
    setIsFocused(true);
    onFocus?.(event);
  };

  const handleBlur = (event: React.FocusEvent<HTMLInputElement>) => {
    setIsFocused(false);
    onBlur?.(event);
  };

  // 处理清除按钮点击
  const handleClear = () => {
    const nativeInputValueSetter = Object.getOwnPropertyDescriptor(
      window.HTMLInputElement.prototype,
      'value'
    )?.set;
    const input = document.querySelector(`.${wrapperClassNames.split(' ')[0]} input`) as HTMLInputElement;
    if (input && nativeInputValueSetter) {
      nativeInputValueSetter.call(input, '');
      const event = new Event('input', { bubbles: true });
      input.dispatchEvent(event);
    }
  };

  // 切换密码显示/隐藏
  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  // 确定实际的 input type
  const actualType = type === 'password' && showPassword ? 'text' : type;

  return (
    <div className="campus-input-container">
      <div className={wrapperClassNames} style={style}>
        {/* 前缀图标 */}
        {prefix && <span className="campus-input__prefix">{prefix}</span>}

        {/* 输入框 */}
        <input
          className="campus-input"
          type={actualType}
          value={value}
          defaultValue={defaultValue}
          placeholder={placeholder}
          maxLength={maxLength}
          disabled={disabled}
          readOnly={readOnly}
          onChange={onChange}
          onFocus={handleFocus}
          onBlur={handleBlur}
          onKeyDown={handleKeyDown}
          {...rest}
        />

        {/* 清除按钮 */}
        {allowClear && value && !disabled && !readOnly && (
          <span className="campus-input__clear" onClick={handleClear}>
            ✕
          </span>
        )}

        {/* 密码显示/隐藏按钮 */}
        {type === 'password' && (
          <span className="campus-input__password-toggle" onClick={togglePasswordVisibility}>
            {showPassword ? '👁️' : '🙈'}
          </span>
        )}

        {/* 后缀图标 */}
        {suffix && <span className="campus-input__suffix">{suffix}</span>}
      </div>

      {/* 错误提示信息 */}
      {error && errorMessage && <div className="campus-input__error-message">{errorMessage}</div>}
    </div>
  );
};

export default Input;
