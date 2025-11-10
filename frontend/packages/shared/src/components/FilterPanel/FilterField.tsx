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

  const commonProps = {
    placeholder,
    allowClear,
    style: { width: width || 200 },
  };

  switch (type) {
    case 'input':
      return (
        <Input
          {...commonProps}
          value={value}
          onChange={(e) => onChange(e.target.value)}
        />
      );

    case 'select':
      return (
        <Select
          {...commonProps}
          value={value}
          onChange={onChange}
          options={options}
        />
      );

    case 'multiSelect':
      return (
        <Select
          {...commonProps}
          mode="multiple"
          value={value}
          onChange={onChange}
          options={options}
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
          {...commonProps}
          value={value}
          onChange={onChange}
          format={format}
          showTime={showTime}
        />
      );

    case 'cascader':
      return (
        <Cascader
          {...commonProps}
          options={options}
          value={value}
          onChange={onChange}
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
