/**
 * 搜索栏组件
 * 
 * 功能：
 * - 关键词搜索
 * - 下拉筛选
 * - 日期范围选择
 * - 搜索/重置按钮
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import React from 'react';
import { Form, Input, Select, DatePicker, Button, Space, Row, Col } from 'antd';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import type { FormInstance } from 'antd';

const { Option } = Select;
const { RangePicker } = DatePicker;

/**
 * 搜索字段类型
 */
export type SearchFieldType = 'input' | 'select' | 'dateRange' | 'custom';

/**
 * 搜索字段配置
 */
export interface SearchField {
  /** 字段名 */
  name: string;
  /** 字段标签 */
  label: string;
  /** 字段类型 */
  type: SearchFieldType;
  /** 占位文本 */
  placeholder?: string;
  /** 下拉选项（type 为 select 时） */
  options?: Array<{ label: string; value: string | number }>;
  /** 自定义渲染（type 为 custom 时） */
  render?: () => React.ReactNode;
  /** 列宽，默认 6 */
  span?: number;
}

/**
 * SearchBar 组件属性
 */
export interface SearchBarProps {
  /** 搜索字段配置 */
  fields: SearchField[];
  /** 搜索事件 */
  onSearch: (values: Record<string, any>) => void;
  /** 重置事件 */
  onReset?: () => void;
  /** 表单实例 */
  form?: FormInstance;
  /** 是否加载中 */
  loading?: boolean;
}

/**
 * 搜索栏组件
 * 
 * @example
 * ```tsx
 * const [form] = Form.useForm();
 * 
 * <SearchBar
 *   form={form}
 *   fields={[
 *     {
 *       name: 'keyword',
 *       label: '关键词',
 *       type: 'input',
 *       placeholder: '请输入关键词',
 *     },
 *     {
 *       name: 'status',
 *       label: '状态',
 *       type: 'select',
 *       options: [
 *         { label: '全部', value: '' },
 *         { label: '启用', value: 'ACTIVE' },
 *         { label: '禁用', value: 'DISABLED' },
 *       ],
 *     },
 *     {
 *       name: 'dateRange',
 *       label: '日期',
 *       type: 'dateRange',
 *     },
 *   ]}
 *   onSearch={(values) => {
 *     console.log('搜索参数:', values);
 *   }}
 *   onReset={() => {
 *     console.log('重置搜索');
 *   }}
 * />
 * ```
 */
export const SearchBar: React.FC<SearchBarProps> = ({
  fields,
  onSearch,
  onReset,
  form: externalForm,
  loading = false,
}) => {
  const [internalForm] = Form.useForm();
  const form = externalForm || internalForm;

  /**
   * 处理搜索
   */
  const handleSearch = () => {
    const values = form.getFieldsValue();
    onSearch(values);
  };

  /**
   * 处理重置
   */
  const handleReset = () => {
    form.resetFields();
    onReset?.();
  };

  /**
   * 渲染字段
   */
  const renderField = (field: SearchField) => {
    switch (field.type) {
      case 'input':
        return (
          <Input
            placeholder={field.placeholder || `请输入${field.label}`}
            allowClear
            onPressEnter={handleSearch}
          />
        );

      case 'select':
        return (
          <Select
            placeholder={field.placeholder || `请选择${field.label}`}
            allowClear
          >
            {field.options?.map((option) => (
              <Option key={option.value} value={option.value}>
                {option.label}
              </Option>
            ))}
          </Select>
        );

      case 'dateRange':
        return (
          <RangePicker
            style={{ width: '100%' }}
            placeholder={['开始日期', '结束日期']}
          />
        );

      case 'custom':
        return field.render?.();

      default:
        return null;
    }
  };

  return (
    <div style={{ marginBottom: 16, padding: '16px 24px', backgroundColor: '#fff', borderRadius: 6 }}>
      <Form form={form} layout="vertical">
        <Row gutter={16}>
          {fields.map((field) => (
            <Col key={field.name} span={field.span || 6}>
              <Form.Item label={field.label} name={field.name}>
                {renderField(field)}
              </Form.Item>
            </Col>
          ))}

          <Col span={24}>
            <Space>
              <Button
                type="primary"
                icon={<SearchOutlined />}
                onClick={handleSearch}
                loading={loading}
              >
                搜索
              </Button>
              <Button icon={<ReloadOutlined />} onClick={handleReset}>
                重置
              </Button>
            </Space>
          </Col>
        </Row>
      </Form>
    </div>
  );
};
