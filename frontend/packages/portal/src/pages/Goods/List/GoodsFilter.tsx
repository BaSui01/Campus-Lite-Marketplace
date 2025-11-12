/**
 * 商品筛选组件 🔍
 * @author BaSui 😎
 * @description 分类、价格区间、标签筛选
 */

import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { goodsService } from '@campus/shared/services';;
import { Skeleton } from '@campus/shared/components';
import './GoodsFilter.css';

interface GoodsFilterProps {
  filters: {
    keyword: string;
    categoryId?: number;
    minPrice?: number;
    maxPrice?: number;
    tags: number[];
    sortBy: string;
    sortDirection: string;
  };
  onFilterChange: (filters: Partial<GoodsFilterProps['filters']>) => void;
}

export const GoodsFilter: React.FC<GoodsFilterProps> = ({
  filters,
  onFilterChange,
}) => {
  // 价格输入临时状态
  const [minPriceInput, setMinPriceInput] = useState(
    filters.minPrice?.toString() || ''
  );
  const [maxPriceInput, setMaxPriceInput] = useState(
    filters.maxPrice?.toString() || ''
  );

  // 获取分类树
  const { data: categories, isLoading: categoriesLoading } = useQuery({
    queryKey: ['categories', 'tree'],
    queryFn: async () => {
      const response = await goodsService.getCategoryTree();
      return response;
    },
    staleTime: 30 * 60 * 1000, // 30分钟缓存
  });

  // 获取热门标签
  const { data: hotTags, isLoading: tagsLoading } = useQuery({
    queryKey: ['tags', 'hot'],
    queryFn: async () => {
      const response = await goodsService.getHotTags(20);
      return response;
    },
    staleTime: 10 * 60 * 1000, // 10分钟缓存
  });

  // 处理分类选择
  const handleCategoryChange = (categoryId?: number) => {
    onFilterChange({ categoryId });
  };

  // 处理价格筛选应用
  const handleApplyPrice = () => {
    const minInput = minPriceInput.trim();
    const maxInput = maxPriceInput.trim();
    
    // 空输入直接清除价格筛选
    if (!minInput && !maxInput) {
      handleClearPrice();
      return;
    }

    const min = minInput ? parseFloat(minInput) : undefined;
    const max = maxInput ? parseFloat(maxInput) : undefined;

    // 价格验证
    if (min !== undefined && (isNaN(min) || min < 0)) {
      alert('❌ 最低价格必须为非负数！');
      return;
    }
    if (max !== undefined && (isNaN(max) || max < 0)) {
      alert('❌ 最高价格必须为非负数！');
      return;
    }
    if (min !== undefined && max !== undefined && min > max) {
      alert('❌ 最低价格不能大于最高价格！\n请调整价格范围后重试。');
      return;
    }

    // 应用筛选
    onFilterChange({ minPrice: min, maxPrice: max });
  };

  // 处理价格筛选清除
  const handleClearPrice = () => {
    setMinPriceInput('');
    setMaxPriceInput('');
    onFilterChange({ minPrice: undefined, maxPrice: undefined });
  };

  // 处理标签切换
  const handleTagToggle = (tagId: number) => {
    const newTags = filters.tags.includes(tagId)
      ? filters.tags.filter(id => id !== tagId)
      : [...filters.tags, tagId];
    
    onFilterChange({ tags: newTags });
  };

  // 清除所有筛选
  const handleClearAll = () => {
    setMinPriceInput('');
    setMaxPriceInput('');
    onFilterChange({
      categoryId: undefined,
      minPrice: undefined,
      maxPrice: undefined,
      tags: [],
    });
  };

  return (
    <div className="goods-filter">
      {/* 清除筛选按钮 */}
      {(filters.categoryId || filters.minPrice || filters.maxPrice || filters.tags.length > 0) && (
        <button className="goods-filter__clear" onClick={handleClearAll}>
          清除全部筛选
        </button>
      )}

      {/* 分类筛选 */}
      <div className="goods-filter__section">
        <h3 className="goods-filter__title">商品分类</h3>
        
        {categoriesLoading ? (
          <div className="goods-filter__loading">
            <Skeleton type="text" count={5} />
          </div>
        ) : (
          <div className="goods-filter__categories">
            <button
              className={`goods-filter__category ${!filters.categoryId ? 'active' : ''}`}
              onClick={() => handleCategoryChange(undefined)}
            >
              全部分类
            </button>
            
            {categories?.map((category) => (
              <div key={category.id} className="goods-filter__category-group">
                <button
                  className={`goods-filter__category ${filters.categoryId === category.id ? 'active' : ''}`}
                  onClick={() => handleCategoryChange(category.id)}
                >
                  {category.name}
                  {category.goodsCount !== undefined && category.goodsCount > 0 && (
                    <span className="goods-filter__count">({category.goodsCount})</span>
                  )}
                </button>
                
                {/* 二级分类 */}
                {category.children && category.children.length > 0 && (
                  <div className="goods-filter__subcategories">
                    {category.children.map((subCategory) => (
                      <button
                        key={subCategory.id}
                        className={`goods-filter__subcategory ${filters.categoryId === subCategory.id ? 'active' : ''}`}
                        onClick={() => handleCategoryChange(subCategory.id)}
                      >
                        {subCategory.name}
                        {subCategory.goodsCount !== undefined && subCategory.goodsCount > 0 && (
                          <span className="goods-filter__count">({subCategory.goodsCount})</span>
                        )}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* 价格筛选 */}
      <div className="goods-filter__section">
        <h3 className="goods-filter__title">价格区间</h3>
        
        <div className="goods-filter__price">
          <input
            type="number"
            className="goods-filter__price-input"
            placeholder="最低价"
            value={minPriceInput}
            onChange={(e) => setMinPriceInput(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleApplyPrice()}
            min="0"
            step="0.01"
          />
          <span className="goods-filter__price-separator">-</span>
          <input
            type="number"
            className="goods-filter__price-input"
            placeholder="最高价"
            value={maxPriceInput}
            onChange={(e) => setMaxPriceInput(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleApplyPrice()}
            min="0"
            step="0.01"
          />
        </div>

        <div className="goods-filter__price-actions">
          <button
            className="goods-filter__price-btn"
            onClick={handleApplyPrice}
          >
            确定
          </button>
          {(filters.minPrice !== undefined || filters.maxPrice !== undefined) && (
            <button
              className="goods-filter__price-btn goods-filter__price-btn--clear"
              onClick={handleClearPrice}
            >
              清除
            </button>
          )}
        </div>
      </div>

      {/* 热门标签 */}
      <div className="goods-filter__section">
        <h3 className="goods-filter__title">热门标签</h3>
        
        {tagsLoading ? (
          <div className="goods-filter__loading">
            <Skeleton type="text" count={3} />
          </div>
        ) : (
          <div className="goods-filter__tags">
            {hotTags?.map((tag) => (
              <button
                key={tag.id}
                className={`goods-filter__tag ${filters.tags.includes(tag.id!) ? 'active' : ''}`}
                onClick={() => handleTagToggle(tag.id!)}
              >
                #{tag.name}
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default GoodsFilter;
