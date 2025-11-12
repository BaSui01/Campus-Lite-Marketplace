/**
 * 通用筛选类型定义
 * @author BaSui 😎
 * @description 前端通用筛选组件的类型定义
 */

import type { ReactNode } from 'react';

// ==================== 筛选字段类型 ====================

/**
 * 筛选字段类型枚举
 */
export type FilterFieldType =
  | 'input'          // 文本输入
  | 'select'         // 下拉选择
  | 'multiSelect'    // 多选下拉
  | 'dateRange'      // 日期范围
  | 'numberRange'    // 数字范围
  | 'cascader'       // 级联选择
  | 'radio'          // 单选按钮
  | 'checkbox';      // 复选框

/**
 * 筛选选项
 */
export interface FilterOption<V = any> {
  /** 显示文本 */
  label: string;
  /** 选项值 */
  value: V;
  /** 是否禁用 */
  disabled?: boolean;
  /** 图标 */
  icon?: ReactNode;
  /** 子选项（用于级联选择） */
  children?: FilterOption<V>[];
}

/**
 * 筛选字段配置
 */
export interface FilterConfig<V = any> {
  /** 字段类型 */
  type: FilterFieldType;
  /** 字段名称（对应后端参数） */
  field: string;
  /** 显示标签 */
  label: string;
  /** 占位符（dateRange 类型时需要传 [string, string]） */
  placeholder?: string | [string, string];
  /** 选项列表（select/multiSelect/radio/checkbox 类型必填） */
  options?: FilterOption<V>[];
  /** 默认值 */
  defaultValue?: V;
  /** 是否必填 */
  required?: boolean;
  /** 是否允许清空 */
  allowClear?: boolean;
  /** 自定义渲染 */
  render?: (value: V, onChange: (value: V) => void) => ReactNode;
  /** 字段宽度 */
  width?: number | string;
  /** 最小值（numberRange 类型） */
  min?: number;
  /** 最大值（numberRange 类型） */
  max?: number;
  /** 步长（numberRange 类型） */
  step?: number;
  /** 前缀（numberRange 类型） */
  prefix?: string;
  /** 后缀（numberRange 类型） */
  suffix?: string;
  /** 日期格式（dateRange 类型） */
  format?: string;
  /** 是否显示时间（dateRange 类型） */
  showTime?: boolean;
}

// ==================== 筛选面板配置 ====================

/**
 * 筛选面板配置
 */
export interface FilterPanelConfig {
  /** 筛选字段列表 */
  filters: FilterConfig[];
  /** 是否显示搜索按钮 */
  showSearchButton?: boolean;
  /** 是否显示重置按钮 */
  showResetButton?: boolean;
  /** 搜索按钮文本 */
  searchButtonText?: string;
  /** 重置按钮文本 */
  resetButtonText?: string;
  /** 是否自动搜索（值变化时自动触发） */
  autoSearch?: boolean;
  /** 是否可折叠 */
  collapsible?: boolean;
  /** 默认是否展开 */
  defaultExpanded?: boolean;
  /** 布局方式 */
  layout?: 'horizontal' | 'vertical' | 'inline';
  /** 每行显示的字段数量 */
  columns?: number;
}

// ==================== 筛选值类型 ====================

/**
 * 筛选值（键值对）
 */
export type FilterValues = Record<string, any>;

/**
 * 日期范围值
 */
export interface DateRangeValue {
  startDate?: string;
  endDate?: string;
}

/**
 * 数字范围值
 */
export interface NumberRangeValue {
  min?: number;
  max?: number;
}

// ==================== 筛选面板 Props ====================

/**
 * 筛选面板组件 Props
 */
export interface FilterPanelProps {
  /** 筛选配置 */
  config: FilterPanelConfig;
  /** 当前筛选值 */
  values: FilterValues;
  /** 值变化回调 */
  onChange: (values: FilterValues) => void;
  /** 搜索回调 */
  onSearch?: () => void;
  /** 重置回调 */
  onReset?: () => void;
  /** 自定义类名 */
  className?: string;
  /** 自定义样式 */
  style?: React.CSSProperties;
}

/**
 * 筛选字段组件 Props
 */
export interface FilterFieldProps {
  /** 字段配置 */
  config: FilterConfig;
  /** 当前值 */
  value: any;
  /** 值变化回调 */
  onChange: (value: any) => void;
}

// ==================== 工具函数类型 ====================

/**
 * 筛选值转换函数类型
 */
export type FilterValueTransformer<T = any, R = any> = (value: T) => R;

/**
 * 筛选值验证函数类型
 */
export type FilterValueValidator<T = any> = (value: T) => boolean | string;
