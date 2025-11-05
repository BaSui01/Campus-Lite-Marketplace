/**
 * Categories 分类导航组件 📂
 * @author BaSui 😎
 * @description 一级分类导航，快速跳转到商品列表
 */

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Skeleton, Empty } from '@campus/shared/components';
import { goodsService } from '@campus/shared/services';

// 分类图标映射（根据分类名称）
const CATEGORY_ICONS: Record<string, string> = {
  '图书教材': '📚',
  '电子产品': '💻',
  '生活用品': '🏠',
  '服装鞋帽': '👔',
  '运动器材': '⚽',
  '美妆护肤': '💄',
  '食品饮料': '🍔',
  '其他': '📦',
};

export const Categories: React.FC = () => {
  const navigate = useNavigate();

  // 获取分类树（只显示一级分类）
  const { data: categories, isLoading } = useQuery({
    queryKey: ['categories', 'tree'],
    queryFn: async () => {
      const response = await goodsService.getCategoryTree();
      return response;
    },
    staleTime: 30 * 60 * 1000, // 30分钟缓存（分类不常变）
  });

  // 处理分类点击
  const handleCategoryClick = (categoryId: number, categoryName: string) => {
    navigate(`/goods?categoryId=${categoryId}&categoryName=${encodeURIComponent(categoryName)}`);
  };

  return (
    <section className="categories">
      <div className="categories__header">
        <h2 className="categories__title">📂 商品分类</h2>
      </div>

      {/* Loading状态 */}
      {isLoading && (
        <div className="categories__grid">
          {Array.from({ length: 8 }).map((_, index) => (
            <Skeleton key={index} type="avatar" animation="wave" />
          ))}
        </div>
      )}

      {/* 分类列表 */}
      {!isLoading && categories && categories.length > 0 && (
        <div className="categories__grid">
          {categories.map((category) => (
            <button
              key={category.id}
              className="categories__item"
              onClick={() => handleCategoryClick(category.id!, category.name!)}
            >
              <div className="categories__item-icon">
                {CATEGORY_ICONS[category.name!] || '📦'}
              </div>
              <div className="categories__item-name">{category.name}</div>
              {category.goodsCount !== undefined && category.goodsCount > 0 && (
                <div className="categories__item-count">
                  {category.goodsCount} 件
                </div>
              )}
            </button>
          ))}
        </div>
      )}

      {/* 空状态 */}
      {!isLoading && (!categories || categories.length === 0) && (
        <Empty
          icon="📂"
          title="暂无分类"
          description="还没有创建商品分类"
        />
      )}
    </section>
  );
};

export default Categories;
