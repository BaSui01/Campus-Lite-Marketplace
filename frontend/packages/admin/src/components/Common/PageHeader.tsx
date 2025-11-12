/**
 * 页面头部组件
 * 
 * 功能：
 * - 页面标题
 * - 返回按钮
 * - 面包屑导航
 * - 操作按钮区
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { PageHeader as AntPageHeader, Breadcrumb, type PageHeaderProps as AntPageHeaderProps } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';

/**
 * 面包屑项
 */
export interface BreadcrumbItem {
  /** 标题 */
  title: string;
  /** 路径 */
  path?: string;
}

/**
 * PageHeader 组件属性
 */
export interface PageHeaderProps extends Omit<AntPageHeaderProps, 'onBack' | 'breadcrumb'> {
  /** 页面标题 */
  title: string;
  /** 子标题 */
  subTitle?: string;
  /** 是否显示返回按钮，默认 false */
  showBack?: boolean;
  /** 返回按钮点击事件，默认返回上一页 */
  onBack?: () => void;
  /** 面包屑导航 */
  breadcrumb?: BreadcrumbItem[];
  /** 额外操作区 */
  extra?: React.ReactNode;
  /** 底部内容 */
  footer?: React.ReactNode;
}

/**
 * 页面头部组件
 * 
 * @example
 * ```tsx
 * <PageHeader
 *   title="商品详情"
 *   subTitle="查看商品详细信息"
 *   showBack
 *   breadcrumb={[
 *     { title: '商品管理', path: '/goods/list' },
 *     { title: '商品详情' },
 *   ]}
 *   extra={
 *     <Space>
 *       <Button>编辑</Button>
 *       <Button type="primary">保存</Button>
 *     </Space>
 *   }
 * />
 * ```
 */
export const PageHeader: React.FC<PageHeaderProps> = ({
  title,
  subTitle,
  showBack = false,
  onBack,
  breadcrumb,
  extra,
  footer,
  ...restProps
}) => {
  const navigate = useNavigate();

  /**
   * 默认返回事件
   */
  const handleBack = () => {
    if (onBack) {
      onBack();
    } else {
      navigate(-1);
    }
  };

  /**
   * 渲染面包屑
   */
  const renderBreadcrumb = () => {
    if (!breadcrumb || breadcrumb.length === 0) {
      return undefined;
    }

    return {
      items: breadcrumb.map((item) => ({
        title: item.path ? (
          <a onClick={() => navigate(item.path!)}>{item.title}</a>
        ) : (
          item.title
        ),
      })),
    };
  };

  return (
    <div style={{ marginBottom: 24 }}>
      <AntPageHeader
        title={title}
        subTitle={subTitle}
        onBack={showBack ? handleBack : undefined}
        backIcon={showBack ? <ArrowLeftOutlined /> : false}
        breadcrumb={renderBreadcrumb()}
        extra={extra}
        footer={footer}
        style={{
          padding: '16px 24px',
          backgroundColor: '#fff',
          borderRadius: 6,
        }}
        {...restProps}
      />
    </div>
  );
};
