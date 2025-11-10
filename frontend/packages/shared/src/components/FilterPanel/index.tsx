/**
 * 通用筛选面板组件
 * @author BaSui 😎
 * @description 提供统一的筛选面板，支持多种筛选类型
 */

import React from 'react';
import { Card, Space, Button, Form } from 'antd';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import { FilterField } from './FilterField';
import type { FilterPanelProps } from '../../types/filter';

/**
 * 通用筛选面板组件
 */
export const FilterPanel: React.FC<FilterPanelProps> = ({
  config,
  values,
  onChange,
  onSearch,
  onReset,
  className,
  style,
}) => {
  const {
    filters,
    showSearchButton = true,
    showResetButton = true,
    searchButtonText = '搜索',
    resetButtonText = '重置',
    layout = 'inline',
  } = config;

  /**
   * 处理字段值变化
   */
  const handleFieldChange = (field: string, value: any) => {
    onChange({ ...values, [field]: value });
  };

  /**
   * 处理重置
   */
  const handleReset = () => {
    const resetValues: Record<string, any> = {};
    filters.forEach((filter) => {
      resetValues[filter.field] = filter.defaultValue ?? undefined;
    });
    onChange(resetValues);
    onReset?.();
  };

  /**
   * 处理搜索
   */
  const handleSearch = () => {
    onSearch?.();
  };

  return (
    <Card className={className} style={style}>
      <Form layout={layout}>
        <Space wrap size="middle">
          {filters.map((filter) => (
            <Form.Item
              key={filter.field}
              label={filter.label}
              style={{ marginBottom: 0 }}
            >
              {filter.render ? (
                filter.render(values[filter.field], (value) =>
                  handleFieldChange(filter.field, value)
                )
              ) : (
                <FilterField
                  config={filter}
                  value={values[filter.field]}
                  onChange={(value) => handleFieldChange(filter.field, value)}
                />
              )}
            </Form.Item>
          ))}

          {showSearchButton && (
            <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
              {searchButtonText}
            </Button>
          )}

          {showResetButton && (
            <Button icon={<ReloadOutlined />} onClick={handleReset}>
              {resetButtonText}
            </Button>
          )}
        </Space>
      </Form>
    </Card>
  );
};

// 导出子组件和类型
export { FilterField } from './FilterField';
export * from '../../types/filter';
