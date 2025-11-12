/**
 * 基本信息步骤 📝
 * @author BaSui 😎
 * @description 商品标题、描述、价格、分类、成色等信息
 */

import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Input, Select, Skeleton } from '@campus/shared/components';
import { categoryService, tagService } from '@campus/shared/services';;
import type { GoodsFormData } from './index';
import './BasicInfoStep.css';

interface BasicInfoStepProps {
  formData: GoodsFormData;
  onUpdate: (data: Partial<GoodsFormData>) => void;
  onNext: () => void;
  onCancel: () => void;
}

const CONDITION_OPTIONS = [
  { value: 'BRAND_NEW', label: '全新' },
  { value: 'LIKE_NEW', label: '几乎全新' },
  { value: 'LIGHTLY_USED', label: '轻微使用痕迹' },
  { value: 'WELL_USED', label: '明显使用痕迹' },
  { value: 'HEAVILY_USED', label: '重度使用痕迹' },
];

const DELIVERY_OPTIONS = [
  { value: 'MEET', label: '校园面交' },
  { value: 'MAIL', label: '快递邮寄' },
];

export const BasicInfoStep: React.FC<BasicInfoStepProps> = ({
  formData,
  onUpdate,
  onNext,
  onCancel,
}) => {
  const [tagInput, setTagInput] = useState('');
  const [showTagSuggestions, setShowTagSuggestions] = useState(false);

  // 获取分类树
  const { data: categories, isLoading: categoriesLoading } = useQuery({
    queryKey: ['categories', 'tree'],
    queryFn: async () => {
      const response = await categoryService.tree();
      return response;
    },
    staleTime: 30 * 60 * 1000,
  });

  // 获取标签列表
  const { data: tags, isLoading: tagsLoading } = useQuery({
    queryKey: ['tags', 'list'],
    queryFn: async () => {
      const response = await tagService.list();
      return response;
    },
    staleTime: 30 * 60 * 1000,
  });

  // 过滤标签建议（根据用户输入）
  const tagSuggestions = React.useMemo(() => {
    if (!tags || !tagInput.trim()) return [];
    
    const input = tagInput.toLowerCase().trim();
    return tags
      .filter(tag => 
        tag.name?.toLowerCase().includes(input) && 
        !formData.tags.includes(tag.name || '')
      )
      .slice(0, 5) // 最多显示5个建议
      .map(tag => tag.name || '');
  }, [tags, tagInput, formData.tags]);

  // 构建分类选项（展平二级分类）
  const categoryOptions = React.useMemo(() => {
    if (!categories) return [];
    
    const options: { value: number; label: string }[] = [];
    
    categories.forEach((category) => {
      // 一级分类
      if (category.id) {
        options.push({
          value: category.id,
          label: category.name || '',
        });
      }
      
      // 二级分类
      if (category.children) {
        category.children.forEach((child) => {
          if (child.id) {
            options.push({
              value: child.id,
              label: `${category.name} / ${child.name}`,
            });
          }
        });
      }
    });
    
    return options;
  }, [categories]);

  // 处理交易方式切换
  const handleDeliveryToggle = (value: string) => {
    const newDeliveryMethod = formData.deliveryMethod.includes(value)
      ? formData.deliveryMethod.filter((m) => m !== value)
      : [...formData.deliveryMethod, value];
    
    onUpdate({ deliveryMethod: newDeliveryMethod });
  };

  // 添加标签
  const handleAddTag = (tagName?: string) => {
    const tag = (tagName || tagInput).trim();
    if (tag && !formData.tags.includes(tag)) {
      onUpdate({ tags: [...formData.tags, tag] });
      setTagInput('');
      setShowTagSuggestions(false);
    }
  };

  // 选择标签建议
  const handleSelectTagSuggestion = (tag: string) => {
    handleAddTag(tag);
  };

  // 删除标签
  const handleRemoveTag = (tag: string) => {
    onUpdate({ tags: formData.tags.filter((t) => t !== tag) });
  };

  // 验证并进入下一步
  const handleNext = () => {
    if (!formData.title.trim()) {
      alert('请输入商品标题');
      return;
    }
    if (!formData.description.trim()) {
      alert('请输入商品描述');
      return;
    }
    if (formData.price <= 0) {
      alert('请输入有效的价格');
      return;
    }
    if (formData.deliveryMethod.length === 0) {
      alert('请至少选择一种交易方式');
      return;
    }
    
    onNext();
  };

  return (
    <div className="basic-info-step">
      <h2 className="basic-info-step__title">📝 填写商品基本信息</h2>

      <div className="basic-info-step__form">
        {/* 商品标题 */}
        <div className="form-field">
          <label className="form-field__label">
            商品标题 <span className="form-field__required">*</span>
          </label>
          <Input
            size="large"
            placeholder="请输入商品标题（建议20字以内）"
            value={formData.title}
            onChange={(e) => onUpdate({ title: e.target.value })}
            maxLength={50}
          />
          <div className="form-field__hint">
            {formData.title.length}/50
          </div>
        </div>

        {/* 商品描述 */}
        <div className="form-field">
          <label className="form-field__label">
            商品描述 <span className="form-field__required">*</span>
          </label>
          <textarea
            className="form-field__textarea"
            placeholder="请详细描述商品情况，如购买时间、使用情况、转让原因等"
            value={formData.description}
            onChange={(e) => onUpdate({ description: e.target.value })}
            maxLength={500}
            rows={6}
          />
          <div className="form-field__hint">
            {formData.description.length}/500
          </div>
        </div>

        {/* 价格 */}
        <div className="form-field-group">
          <div className="form-field">
            <label className="form-field__label">
              出售价格（元） <span className="form-field__required">*</span>
            </label>
            <Input
              type="number"
              size="large"
              placeholder="0.00"
              value={formData.price || ''}
              onChange={(e) => onUpdate({ price: parseFloat(e.target.value) || 0 })}
              min="0"
              step="0.01"
              prefix={<span>¥</span>}
            />
          </div>

          <div className="form-field">
            <label className="form-field__label">
              原价（元）<span className="form-field__optional">选填</span>
            </label>
            <Input
              type="number"
              size="large"
              placeholder="0.00"
              value={formData.originalPrice || ''}
              onChange={(e) => onUpdate({ originalPrice: parseFloat(e.target.value) || undefined })}
              min="0"
              step="0.01"
              prefix={<span>¥</span>}
            />
          </div>
        </div>

        {/* 分类选择 */}
        <div className="form-field">
          <label className="form-field__label">
            商品分类 <span className="form-field__optional">选填</span>
          </label>
          {categoriesLoading ? (
            <Skeleton type="input" />
          ) : (
            <Select
              size="large"
              placeholder="请选择商品分类"
              value={formData.categoryId}
              onChange={(value) => onUpdate({ categoryId: value as number })}
              options={categoryOptions}
            />
          )}
        </div>

        {/* 成色选择 */}
        <div className="form-field">
          <label className="form-field__label">
            商品成色 <span className="form-field__required">*</span>
          </label>
          <div className="form-field__radio-group">
            {CONDITION_OPTIONS.map((option) => (
              <label
                key={option.value}
                className={`form-field__radio ${formData.condition === option.value ? 'active' : ''}`}
              >
                <input
                  type="radio"
                  name="condition"
                  value={option.value}
                  checked={formData.condition === option.value}
                  onChange={(e) => onUpdate({ condition: e.target.value })}
                />
                <span>{option.label}</span>
              </label>
            ))}
          </div>
        </div>

        {/* 交易方式 */}
        <div className="form-field">
          <label className="form-field__label">
            交易方式 <span className="form-field__required">*</span>
          </label>
          <div className="form-field__checkbox-group">
            {DELIVERY_OPTIONS.map((option) => (
              <label
                key={option.value}
                className={`form-field__checkbox ${formData.deliveryMethod.includes(option.value) ? 'active' : ''}`}
              >
                <input
                  type="checkbox"
                  value={option.value}
                  checked={formData.deliveryMethod.includes(option.value)}
                  onChange={() => handleDeliveryToggle(option.value)}
                />
                <span>{option.label}</span>
              </label>
            ))}
          </div>
        </div>

        {/* 标签 */}
        <div className="form-field">
          <label className="form-field__label">
            商品标签 <span className="form-field__optional">选填</span>
          </label>
          <div className="form-field__tag-input" style={{ position: 'relative' }}>
            <Input
              placeholder="输入标签，按回车添加（支持从已有标签选择）"
              value={tagInput}
              onChange={(e) => {
                setTagInput(e.target.value);
                setShowTagSuggestions(e.target.value.trim().length > 0);
              }}
              onPressEnter={() => handleAddTag()}
              onFocus={() => setShowTagSuggestions(tagInput.trim().length > 0)}
              onBlur={() => setTimeout(() => setShowTagSuggestions(false), 200)}
              disabled={tagsLoading}
            />
            <button
              type="button"
              className="form-field__tag-add-btn"
              onClick={() => handleAddTag()}
              disabled={tagsLoading}
            >
              添加
            </button>
            
            {/* 标签建议下拉框 */}
            {showTagSuggestions && tagSuggestions.length > 0 && (
              <div 
                className="form-field__tag-suggestions"
                style={{
                  position: 'absolute',
                  top: '100%',
                  left: 0,
                  right: 0,
                  zIndex: 1000,
                  backgroundColor: '#fff',
                  border: '1px solid #e0e0e0',
                  borderRadius: '4px',
                  marginTop: '4px',
                  maxHeight: '200px',
                  overflowY: 'auto',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
                }}
              >
                {tagSuggestions.map((tag) => (
                  <div
                    key={tag}
                    className="form-field__tag-suggestion-item"
                    style={{
                      padding: '8px 12px',
                      cursor: 'pointer',
                      transition: 'background-color 0.2s'
                    }}
                    onClick={() => handleSelectTagSuggestion(tag)}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f5f5f5'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#fff'}
                  >
                    #{tag}
                  </div>
                ))}
              </div>
            )}
          </div>
          
          {formData.tags.length > 0 && (
            <div className="form-field__tags">
              {formData.tags.map((tag) => (
                <span key={tag} className="form-field__tag">
                  #{tag}
                  <button
                    type="button"
                    className="form-field__tag-remove"
                    onClick={() => handleRemoveTag(tag)}
                  >
                    ×
                  </button>
                </span>
              ))}
            </div>
          )}
          <div className="form-field__hint">
            {tagsLoading ? '正在加载标签...' : `已选 ${formData.tags.length} 个标签`}
          </div>
        </div>
      </div>

      {/* 操作按钮 */}
      <div className="basic-info-step__actions">
        <button
          type="button"
          className="basic-info-step__btn basic-info-step__btn--cancel"
          onClick={onCancel}
        >
          取消
        </button>
        <button
          type="button"
          className="basic-info-step__btn basic-info-step__btn--next"
          onClick={handleNext}
        >
          下一步：上传图片 →
        </button>
      </div>
    </div>
  );
};

export default BasicInfoStep;
