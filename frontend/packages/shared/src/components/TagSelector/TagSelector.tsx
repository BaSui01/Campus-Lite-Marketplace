/**
 * TagSelector 组件 - 标签选择器
 * @author BaSui 😎
 * @description 支持多选、搜索、热门标签推荐的标签选择器
 */

import React, { useState, useEffect } from 'react';
import './TagSelector.css';

/**
 * 标签数据结构
 */
export interface TagOption {
  id: number;
  name: string;
  usageCount?: number;
  enabled?: boolean;
}

/**
 * TagSelector 组件 Props
 */
export interface TagSelectorProps {
  /**
   * 已选中的标签ID列表
   */
  value?: number[];

  /**
   * 默认选中的标签ID列表
   */
  defaultValue?: number[];

  /**
   * 可选标签列表
   */
  options?: TagOption[];

  /**
   * 热门标签列表
   */
  hotTags?: TagOption[];

  /**
   * 最多选择数量
   * @default 10
   */
  maxCount?: number;

  /**
   * 是否支持搜索
   * @default true
   */
  searchable?: boolean;

  /**
   * 是否显示热门标签
   * @default true
   */
  showHotTags?: boolean;

  /**
   * 占位提示文本
   */
  placeholder?: string;

  /**
   * 是否禁用
   */
  disabled?: boolean;

  /**
   * 自定义类名
   */
  className?: string;

  /**
   * 选择变化回调
   */
  onChange?: (value: number[]) => void;
}

/**
 * TagSelector 组件
 * 
 * @example
 * ```tsx
 * // 基础用法
 * <TagSelector
 *   options={tags}
 *   value={selectedTagIds}
 *   onChange={(ids) => setSelectedTagIds(ids)}
 * />
 * 
 * // 带热门标签
 * <TagSelector
 *   options={tags}
 *   hotTags={hotTags}
 *   showHotTags={true}
 *   maxCount={5}
 *   onChange={(ids) => console.log(ids)}
 * />
 * ```
 */
export const TagSelector: React.FC<TagSelectorProps> = ({
  value,
  defaultValue = [],
  options = [],
  hotTags = [],
  maxCount = 10,
  searchable = true,
  showHotTags = true,
  placeholder = '搜索标签...',
  disabled = false,
  className = '',
  onChange,
}) => {
  // 内部状态管理
  const [selectedIds, setSelectedIds] = useState<number[]>(value || defaultValue);
  const [searchKeyword, setSearchKeyword] = useState<string>('');
  const [filteredOptions, setFilteredOptions] = useState<TagOption[]>(options);

  // 受控模式处理
  const currentValue = value !== undefined ? value : selectedIds;

  // 监听 options 变化，更新过滤列表
  useEffect(() => {
    if (searchKeyword.trim() === '') {
      setFilteredOptions(options);
    } else {
      const keyword = searchKeyword.toLowerCase();
      const filtered = options.filter((tag) =>
        tag.name.toLowerCase().includes(keyword)
      );
      setFilteredOptions(filtered);
    }
  }, [searchKeyword, options]);

  // 处理标签选择/取消选择
  const handleTagClick = (tagId: number) => {
    if (disabled) return;

    let newValue: number[];
    
    if (currentValue.includes(tagId)) {
      // 取消选择
      newValue = currentValue.filter((id) => id !== tagId);
    } else {
      // 选择标签
      if (currentValue.length >= maxCount) {
        alert(`最多只能选择 ${maxCount} 个标签！`);
        return;
      }
      newValue = [...currentValue, tagId];
    }

    // 更新内部状态
    if (value === undefined) {
      setSelectedIds(newValue);
    }

    // 触发回调
    onChange?.(newValue);
  };

  // 清空所有选择
  const handleClearAll = () => {
    if (disabled) return;

    const newValue: number[] = [];
    
    if (value === undefined) {
      setSelectedIds(newValue);
    }

    onChange?.(newValue);
  };

  // 处理搜索输入
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchKeyword(e.target.value);
  };

  // 获取已选中的标签对象
  const selectedTags = options.filter((tag) => currentValue.includes(tag.id));

  return (
    <div className={`campus-tag-selector ${disabled ? 'campus-tag-selector--disabled' : ''} ${className}`}>
      {/* 搜索框 */}
      {searchable && (
        <div className="campus-tag-selector__search">
          <input
            type="text"
            value={searchKeyword}
            onChange={handleSearchChange}
            placeholder={placeholder}
            disabled={disabled}
            className="campus-tag-selector__search-input"
          />
        </div>
      )}

      {/* 已选标签 */}
      {currentValue.length > 0 && (
        <div className="campus-tag-selector__selected">
          <div className="campus-tag-selector__selected-header">
            <span className="campus-tag-selector__selected-title">
              已选 ({currentValue.length}/{maxCount})
            </span>
            {!disabled && (
              <button
                className="campus-tag-selector__clear-btn"
                onClick={handleClearAll}
              >
                清空
              </button>
            )}
          </div>
          <div className="campus-tag-selector__selected-list">
            {selectedTags.map((tag) => (
              <div
                key={tag.id}
                className="campus-tag-selector__tag campus-tag-selector__tag--selected"
                onClick={() => handleTagClick(tag.id)}
              >
                {tag.name}
                {!disabled && <span className="campus-tag-selector__tag-remove">×</span>}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 热门标签 */}
      {showHotTags && hotTags.length > 0 && (
        <div className="campus-tag-selector__hot">
          <div className="campus-tag-selector__hot-title">🔥 热门标签</div>
          <div className="campus-tag-selector__hot-list">
            {hotTags.map((tag) => (
              <div
                key={tag.id}
                className={`campus-tag-selector__tag ${currentValue.includes(tag.id) ? 'campus-tag-selector__tag--active' : ''}`}
                onClick={() => handleTagClick(tag.id)}
              >
                {tag.name}
                {tag.usageCount && <span className="campus-tag-selector__tag-count">({tag.usageCount})</span>}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 所有标签 */}
      <div className="campus-tag-selector__options">
        <div className="campus-tag-selector__options-title">所有标签</div>
        <div className="campus-tag-selector__options-list">
          {filteredOptions.length === 0 ? (
            <div className="campus-tag-selector__empty">暂无标签</div>
          ) : (
            filteredOptions.map((tag) => (
              <div
                key={tag.id}
                className={`campus-tag-selector__tag ${currentValue.includes(tag.id) ? 'campus-tag-selector__tag--active' : ''}`}
                onClick={() => handleTagClick(tag.id)}
              >
                {tag.name}
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default TagSelector;
