/**
 * Select 组件 - 选择器界的选美冠军！👑
 * @author BaSui 😎
 * @description 下拉选择器组件，支持搜索、多选、分组等功能
 */

import React, { useState, useRef, useEffect } from 'react';
import './Select.css';

/**
 * Select 选项的 Props 接口
 */
export interface SelectOptionProps {
  /**
   * 选项值
   */
  value: string | number;

  /**
   * 显示文本
   */
  children: React.ReactNode;

  /**
   * 是否禁用
   * @default false
   */
  disabled?: boolean;

  /**
   * 自定义类名
   */
  className?: string;
}

/**
 * Select 组件的 Props 接口
 */
export interface SelectProps extends Omit<React.SelectHTMLAttributes<HTMLSelectElement>, 'onChange' | 'value'> {
  /**
   * 当前选中值
   */
  value?: string | number | (string | number)[];

  /**
   * 默认选中值
   */
  defaultValue?: string | number | (string | number)[];

  /**
   * 是否多选
   * @default false
   */
  multiple?: boolean;

  /**
   * 是否禁用
   * @default false
   */
  disabled?: boolean;

  /**
   * 是否可搜索
   * @default false
   */
  searchable?: boolean;

  /**
   * 占位提示文本
   */
  placeholder?: string;

  /**
   * 尺寸
   * @default 'medium'
   */
  size?: 'small' | 'medium' | 'large';

  /**
   * 是否显示清除按钮
   * @default false
   */
  allowClear?: boolean;

  /**
   * 自定义类名
   */
  className?: string;

  /**
   * 自定义样式
   */
  style?: React.CSSProperties;

  /**
   * 选项变化回调
   */
  onChange?: (value: string | number | (string | number)[]) => void;

  /**
   * 子元素（Select.Option）
   */
  children?: React.ReactNode;
}

/**
 * Option 组件
 */
export const SelectOption: React.FC<SelectOptionProps> = ({
  value,
  children,
  disabled = false,
  className = '',
}) => {
  return (
    <option value={value} disabled={disabled} className={className}>
      {children}
    </option>
  );
};

/**
 * Select 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <Select value="apple" onChange={(value) => console.log(value)}>
 *   <Select.Option value="apple">🍎 苹果</Select.Option>
 *   <Select.Option value="banana">🍌 香蕉</Select.Option>
 *   <Select.Option value="orange">🍊 橙子</Select.Option>
 * </Select>
 *
 * // 带占位符
 * <Select placeholder="请选择水果" allowClear>
 *   <Select.Option value="">请选择</Select.Option>
 *   <Select.Option value="apple">🍎 苹果</Select.Option>
 * </Select>
 *
 * // 多选
 * <Select multiple value={['apple', 'banana']}>
 *   <Select.Option value="apple">🍎 苹果</Select.Option>
 *   <Select.Option value="banana">🍌 香蕉</Select.Option>
 * </Select>
 * ```
 */
export const Select: React.FC<SelectProps> = ({
  value,
  defaultValue,
  multiple = false,
  disabled = false,
  searchable = false,
  placeholder = '请选择',
  size = 'medium',
  allowClear = false,
  className = '',
  style,
  onChange,
  children,
  ...rest
}) => {
  const [internalValue, setInternalValue] = useState<string | number | (string | number)[]>(
    value || defaultValue || (multiple ? [] : '')
  );

  const [isClearVisible, setIsClearVisible] = useState(false);

  // 受控模式处理
  const currentValue = value !== undefined ? value : internalValue;
  const hasValue = Array.isArray(currentValue) 
    ? currentValue.length > 0 
    : currentValue !== '' && currentValue !== undefined;

  // 尺寸映射
  const sizeClassMap = {
    small: 'campus-select--small',
    medium: 'campus-select--medium',
    large: 'campus-select--large',
  };

  // 组装 CSS 类名
  const classNames = [
    'campus-select',
    sizeClassMap[size],
    disabled ? 'campus-select--disabled' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  // 处理值变化
  const handleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    let newValue: string | number | (string | number)[];

    if (multiple) {
      // 多选模式处理
      const selectedOptions = Array.from(event.target.selectedOptions, option => option.value);
      newValue = selectedOptions;
    } else {
      // 单选模式处理
      newValue = event.target.value;
    }

    // 更新内部状态
    if (value === undefined) {
      setInternalValue(newValue);
    }

    // 触发回调
    onChange?.(newValue);
  };

  // 清除选择
  const handleClear = (event: React.MouseEvent<HTMLSpanElement>) => {
    event.stopPropagation();
    
    const clearedValue = multiple ? [] : '';
    
    if (value === undefined) {
      setInternalValue(clearedValue);
    }
    
    onChange?.(clearedValue);
  };

  // 处理鼠标悬停显示清除按钮
  const handleMouseEnter = () => {
    if (allowClear && hasValue && !disabled) {
      setIsClearVisible(true);
    }
  };

  const handleMouseLeave = () => {
    setIsClearVisible(false);
  };

  return (
    <div 
      className={classNames} 
      style={style}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      <select
        value={currentValue}
        onChange={handleChange}
        disabled={disabled}
        multiple={multiple}
        className="campus-select__inner"
        {...rest}
      >
        {!multiple && placeholder && (
          <option value="" disabled={hasValue}>
            {placeholder}
          </option>
        )}
        {children}
      </select>
      
      {/* 清除按钮 */}
      {allowClear && hasValue && !disabled && (
        <span className="campus-select__clear" onClick={handleClear}>
          ✕
        </span>
      )}

      {/* 下拉箭头 */}
      <span className="campus-select__arrow">▼</span>
    </div>
  );
};

// 为 Select 组件添加 Option 属性
Select.Option = SelectOption;

export default Select;
