/**
 * PageContainer - 通用页面容器组件
 * @author BaSui 😎
 * @description 统一的页面布局组件，包含面包屑、标题、操作区、内容区
 * @example
 * ```tsx
 * <PageContainer 
 *   title="商品管理" 
 *   breadcrumb={['首页', '商品', '商品列表']}
 *   extra={<Button type="primary">新增商品</Button>}
 * >
 *   <GoodsList />
 * </PageContainer>
 * ```
 */

import React from 'react';
import './PageContainer.css';

export interface PageContainerProps {
  /** 页面标题 */
  title?: string;
  /** 面包屑导航 */
  breadcrumb?: Array<{ label: string; href?: string }> | string[];
  /** 标题右侧额外操作区 */
  extra?: React.ReactNode;
  /** 页面内容 */
  children: React.ReactNode;
  /** 是否加载中 */
  loading?: boolean;
  /** 自定义类名 */
  className?: string;
  /** 是否显示卡片背景 */
  card?: boolean;
  /** 内容区域自定义样式 */
  contentStyle?: React.CSSProperties;
}

/**
 * PageContainer 组件 - 通用页面布局容器
 */
export const PageContainer: React.FC<PageContainerProps> = ({
  title,
  breadcrumb,
  extra,
  children,
  loading = false,
  className = '',
  card = true,
  contentStyle,
}) => {
  // 渲染面包屑
  const renderBreadcrumb = () => {
    if (!breadcrumb || breadcrumb.length === 0) return null;

    const items = breadcrumb.map((item) =>
      typeof item === 'string' ? { label: item } : item
    );

    return (
      <nav className="page-container__breadcrumb" aria-label="面包屑导航">
        {items.map((item, index) => (
          <React.Fragment key={index}>
            {item.href ? (
              <a href={item.href} className="page-container__breadcrumb-link">
                {item.label}
              </a>
            ) : (
              <span className="page-container__breadcrumb-text">{item.label}</span>
            )}
            {index < items.length - 1 && (
              <span className="page-container__breadcrumb-separator">/</span>
            )}
          </React.Fragment>
        ))}
      </nav>
    );
  };

  // 渲染页头
  const renderHeader = () => {
    if (!title && !extra) return null;

    return (
      <div className="page-container__header">
        {title && <h1 className="page-container__title">{title}</h1>}
        {extra && <div className="page-container__extra">{extra}</div>}
      </div>
    );
  };

  return (
    <div className={`page-container ${className}`}>
      {renderBreadcrumb()}
      {renderHeader()}
      <div
        className={`page-container__content ${card ? 'page-container__content--card' : ''}`}
        style={contentStyle}
      >
        {loading ? (
          <div className="page-container__loading">
            <div className="page-container__spinner"></div>
            <p>加载中...</p>
          </div>
        ) : (
          children
        )}
      </div>
    </div>
  );
};

export default PageContainer;
