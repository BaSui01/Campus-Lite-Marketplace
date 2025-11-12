/**
 * TopicSelector 组件 - 话题选择器
 * @author BaSui 😎
 * @description 支持单选/多选、搜索、热门话题推荐的话题选择器
 */

import React, { useState, useEffect } from 'react';
import './TopicSelector.css';

/**
 * 话题数据结构
 */
export interface TopicOption {
  id: number;
  name: string;
  description?: string;
  postCount?: number;
  followerCount?: number;
  hotness?: number;
}

/**
 * TopicSelector 组件 Props
 */
export interface TopicSelectorProps {
  /**
   * 已选中的话题ID（单选时为number，多选时为number[]）
   */
  value?: number | number[];

  /**
   * 默认选中的话题ID
   */
  defaultValue?: number | number[];

  /**
   * 可选话题列表
   */
  options?: TopicOption[];

  /**
   * 热门话题列表
   */
  hotTopics?: TopicOption[];

  /**
   * 是否多选
   * @default false
   */
  multiple?: boolean;

  /**
   * 最多选择数量（多选时有效）
   * @default 5
   */
  maxCount?: number;

  /**
   * 是否支持搜索
   * @default true
   */
  searchable?: boolean;

  /**
   * 是否显示热门话题
   * @default true
   */
  showHotTopics?: boolean;

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
  onChange?: (value: number | number[]) => void;
}

/**
 * TopicSelector 组件
 * 
 * @example
 * ```tsx
 * // 单选模式
 * <TopicSelector
 *   options={topics}
 *   value={selectedTopicId}
 *   onChange={(id) => setSelectedTopicId(id as number)}
 * />
 * 
 * // 多选模式
 * <TopicSelector
 *   multiple
 *   options={topics}
 *   value={selectedTopicIds}
 *   onChange={(ids) => setSelectedTopicIds(ids as number[])}
 * />
 * ```
 */
export const TopicSelector: React.FC<TopicSelectorProps> = ({
  value,
  defaultValue,
  options = [],
  hotTopics = [],
  multiple = false,
  maxCount = 5,
  searchable = true,
  showHotTopics = true,
  placeholder = '搜索话题...',
  disabled = false,
  className = '',
  onChange,
}) => {
  // 内部状态管理
  const [selectedValue, setSelectedValue] = useState<number | number[]>(
    value !== undefined ? value : (defaultValue || (multiple ? [] : 0))
  );
  const [searchKeyword, setSearchKeyword] = useState<string>('');
  const [filteredOptions, setFilteredOptions] = useState<TopicOption[]>(options);

  // 受控模式处理
  const currentValue = value !== undefined ? value : selectedValue;

  // 监听 options 变化，更新过滤列表
  useEffect(() => {
    if (searchKeyword.trim() === '') {
      setFilteredOptions(options);
    } else {
      const keyword = searchKeyword.toLowerCase();
      const filtered = options.filter((topic) =>
        topic.name.toLowerCase().includes(keyword) ||
        (topic.description && topic.description.toLowerCase().includes(keyword))
      );
      setFilteredOptions(filtered);
    }
  }, [searchKeyword, options]);

  // 处理话题选择
  const handleTopicClick = (topicId: number) => {
    if (disabled) return;

    let newValue: number | number[];

    if (multiple) {
      // 多选模式
      const currentArray = Array.isArray(currentValue) ? currentValue : [];
      
      if (currentArray.includes(topicId)) {
        // 取消选择
        newValue = currentArray.filter((id) => id !== topicId);
      } else {
        // 选择话题
        if (currentArray.length >= maxCount) {
          alert(`最多只能选择 ${maxCount} 个话题！`);
          return;
        }
        newValue = [...currentArray, topicId];
      }
    } else {
      // 单选模式
      newValue = currentValue === topicId ? 0 : topicId;
    }

    // 更新内部状态
    if (value === undefined) {
      setSelectedValue(newValue);
    }

    // 触发回调
    onChange?.(newValue);
  };

  // 清空所有选择
  const handleClearAll = () => {
    if (disabled) return;

    const newValue = multiple ? [] : 0;
    
    if (value === undefined) {
      setSelectedValue(newValue);
    }

    onChange?.(newValue);
  };

  // 处理搜索输入
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchKeyword(e.target.value);
  };

  // 获取已选中的话题对象
  const getSelectedTopics = (): TopicOption[] => {
    if (multiple) {
      const ids = Array.isArray(currentValue) ? currentValue : [];
      return options.filter((topic) => ids.includes(topic.id));
    } else {
      const id = typeof currentValue === 'number' ? currentValue : 0;
      return id > 0 ? options.filter((topic) => topic.id === id) : [];
    }
  };

  const selectedTopics = getSelectedTopics();
  const hasSelection = selectedTopics.length > 0;

  // 检查话题是否被选中
  const isTopicSelected = (topicId: number): boolean => {
    if (multiple) {
      return Array.isArray(currentValue) && currentValue.includes(topicId);
    } else {
      return currentValue === topicId;
    }
  };

  return (
    <div className={`campus-topic-selector ${disabled ? 'campus-topic-selector--disabled' : ''} ${className}`}>
      {/* 搜索框 */}
      {searchable && (
        <div className="campus-topic-selector__search">
          <input
            type="text"
            value={searchKeyword}
            onChange={handleSearchChange}
            placeholder={placeholder}
            disabled={disabled}
            className="campus-topic-selector__search-input"
          />
        </div>
      )}

      {/* 已选话题 */}
      {hasSelection && (
        <div className="campus-topic-selector__selected">
          <div className="campus-topic-selector__selected-header">
            <span className="campus-topic-selector__selected-title">
              已选 {multiple && `(${selectedTopics.length}/${maxCount})`}
            </span>
            {!disabled && (
              <button
                className="campus-topic-selector__clear-btn"
                onClick={handleClearAll}
              >
                清空
              </button>
            )}
          </div>
          <div className="campus-topic-selector__selected-list">
            {selectedTopics.map((topic) => (
              <div
                key={topic.id}
                className="campus-topic-selector__topic campus-topic-selector__topic--selected"
                onClick={() => handleTopicClick(topic.id)}
              >
                <div className="campus-topic-selector__topic-name">#{topic.name}</div>
                {topic.description && (
                  <div className="campus-topic-selector__topic-desc">{topic.description}</div>
                )}
                {!disabled && <span className="campus-topic-selector__topic-remove">×</span>}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 热门话题 */}
      {showHotTopics && hotTopics.length > 0 && (
        <div className="campus-topic-selector__hot">
          <div className="campus-topic-selector__hot-title">🔥 热门话题</div>
          <div className="campus-topic-selector__hot-list">
            {hotTopics.map((topic) => (
              <div
                key={topic.id}
                className={`campus-topic-selector__topic ${isTopicSelected(topic.id) ? 'campus-topic-selector__topic--active' : ''}`}
                onClick={() => handleTopicClick(topic.id)}
              >
                <div className="campus-topic-selector__topic-name">#{topic.name}</div>
                {topic.description && (
                  <div className="campus-topic-selector__topic-desc">{topic.description}</div>
                )}
                <div className="campus-topic-selector__topic-stats">
                  {topic.postCount && <span>📝 {topic.postCount}</span>}
                  {topic.followerCount && <span>👥 {topic.followerCount}</span>}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 所有话题 */}
      <div className="campus-topic-selector__options">
        <div className="campus-topic-selector__options-title">所有话题</div>
        <div className="campus-topic-selector__options-list">
          {filteredOptions.length === 0 ? (
            <div className="campus-topic-selector__empty">暂无话题</div>
          ) : (
            filteredOptions.map((topic) => (
              <div
                key={topic.id}
                className={`campus-topic-selector__topic ${isTopicSelected(topic.id) ? 'campus-topic-selector__topic--active' : ''}`}
                onClick={() => handleTopicClick(topic.id)}
              >
                <div className="campus-topic-selector__topic-name">#{topic.name}</div>
                {topic.description && (
                  <div className="campus-topic-selector__topic-desc">{topic.description}</div>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default TopicSelector;
