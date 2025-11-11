/**
 * 筛选字段组件
 * @author BaSui 😎
 * @description 根据配置渲染不同类型的筛选字段
 */

import React from 'react';
import { Input, Select, DatePicker, InputNumber, Cascader, Radio, Checkbox } from 'antd';
import type { FilterFieldProps } from '../../types/filter';

const { RangePicker } = DatePicker;

/**
 * 筛选字段组件
 */
export const FilterField: React.FC<FilterFieldProps> = ({ config, value, onChange }) => {
  const {
    type,
    placeholder,
    options,
    allowClear = true,
    width,
    min,
    max,
    step,
    prefix,
    suffix,
    format,
    showTime,
  } = config;

  const baseStyle = { width: width || 200 };

  switch (type) {
    case 'input':
      return (
        <Input
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={typeof placeholder === 'string' ? placeholder : undefined}
          allowClear={allowClear}
          style={baseStyle}
        />
      );

    case 'select':
      return (
        <Select
          value={value}
          onChange={onChange}
          options={options}
          placeholder={typeof placeholder === 'string' ? placeholder : undefined}
          allowClear={allowClear}
          style={baseStyle}
        />
      );

    case 'multiSelect':
      return (
        <Select
          mode="multiple"
          value={value}
          onChange={onChange}
          options={options}
          placeholder={typeof placeholder === 'string' ? placeholder : undefined}
          allowClear={allowClear}
          style={baseStyle}
        />
      );

    case 'numberRange':
      return (
        <Input.Group compact>
          <InputNumber
            style={{ width: '45%' }}
            placeholder="最小值"
            min={min}
            max={max}
            step={step}
            prefix={prefix}
            suffix={suffix}
            value={value?.min}
            onChange={(val) => onChange({ ...value, min: val })}
          />
          <Input
            style={{ width: '10%', textAlign: 'center', pointerEvents: 'none' }}
            placeholder="~"
            disabled
          />
          <InputNumber
            style={{ width: '45%' }}
            placeholder="最大值"
            min={min}
            max={max}
            step={step}
            prefix={prefix}
            suffix={suffix}
            value={value?.max}
            onChange={(val) => onChange({ ...value, max: val })}
          />
        </Input.Group>
      );

    case 'dateRange':
      return (
        <RangePicker
          value={value}
          onChange={onChange}
          format={format}
          showTime={showTime}
          placeholder={
            Array.isArray(placeholder) 
              ? (placeholder as [string, string]) 
              : ['开始日期', '结束日期']
          }
          allowClear={allowClear}
          style={{ width: width || 200 }}
        />
      );

    case 'cascader':
      return (
        <Cascader
          options={options}
          value={value}
          onChange={onChange}
          placeholder={typeof placeholder === 'string' ? placeholder : undefined}
          allowClear={allowClear}
          style={baseStyle}
        />
      );

    case 'radio':
      return (
        <Radio.Group
          options={options}
          value={value}
          onChange={(e) => onChange(e.target.value)}
        />
      );

    case 'checkbox':
      return (
        <Checkbox.Group
          options={options}
          value={value}
          onChange={onChange}
        />
      );

    default:
      return null;
  }
};
