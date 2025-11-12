/**
 * 法律页面通用布局组件
 * @author BaSui 😎
 * @description 用于关于我们、隐私政策、服务条款等静态页面的通用布局
 * @date 2025-11-08
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import './LegalPage.css';

interface LegalPageProps {
  /** 页面标题 */
  title: string;
  /** 最后更新时间 */
  lastUpdated?: string;
  /** 页面内容 */
  children: React.ReactNode;
  /** 显示目录导航 */
  showToc?: boolean;
}

/**
 * 法律页面布局组件
 */
export const LegalPage: React.FC<LegalPageProps> = ({
  title,
  lastUpdated,
  children,
  showToc = false,
}) => {
  const navigate = useNavigate();

  return (
    <div className="legal-page">
      {/* 顶部导航 */}
      <div className="legal-page__header">
        <button
          className="legal-page__back-btn"
          onClick={() => navigate(-1)}
          aria-label="返回"
        >
          ← 返回
        </button>
        <div className="legal-page__breadcrumb">
          <span className="legal-page__breadcrumb-item" onClick={() => navigate('/')}>
            首页
          </span>
          <span className="legal-page__breadcrumb-divider">/</span>
          <span className="legal-page__breadcrumb-item legal-page__breadcrumb-item--active">
            {title}
          </span>
        </div>
      </div>

      {/* 主内容区 */}
      <div className="legal-page__container">
        <article className="legal-page__content">
          {/* 页面标题 */}
          <header className="legal-page__title-section">
            <h1 className="legal-page__title">{title}</h1>
            {lastUpdated && (
              <p className="legal-page__last-updated">
                最后更新：{lastUpdated}
              </p>
            )}
          </header>

          {/* 页面内容 */}
          <div className="legal-page__body">
            {children}
          </div>
        </article>

        {/* 侧边目录（可选） */}
        {showToc && (
          <aside className="legal-page__toc">
            <div className="legal-page__toc-title">目录</div>
            {/* 这里可以根据需要动态生成目录 */}
          </aside>
        )}
      </div>
    </div>
  );
};

export default LegalPage;
