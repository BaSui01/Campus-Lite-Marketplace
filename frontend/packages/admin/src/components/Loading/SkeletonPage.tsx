/**
 * 骨架屏页面组件
 * 
 * 功能：
 * - 列表页面骨架屏
 * - 详情页面骨架屏
 * - 表单页面骨架屏
 * - 仪表盘骨架屏
 * - 统一加载体验
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import React from 'react';
import { Card, Skeleton, Row, Col, Space } from 'antd';

/**
 * 骨架屏类型
 */
export type SkeletonType = 'list' | 'detail' | 'form' | 'dashboard';

/**
 * SkeletonPage 组件属性
 */
export interface SkeletonPageProps {
  /** 骨架屏类型 */
  type?: SkeletonType;
  /** 是否显示头部，默认 true */
  showHeader?: boolean;
  /** 是否显示统计卡片，默认 false */
  showStats?: boolean;
  /** 列表行数，默认 5 */
  rows?: number;
}

/**
 * 骨架屏页面组件
 * 
 * @example
 * ```tsx
 * // 列表页骨架屏
 * <SkeletonPage type="list" showStats />
 * 
 * // 详情页骨架屏
 * <SkeletonPage type="detail" />
 * 
 * // 表单页骨架屏
 * <SkeletonPage type="form" />
 * 
 * // 仪表盘骨架屏
 * <SkeletonPage type="dashboard" showStats />
 * ```
 */
export const SkeletonPage: React.FC<SkeletonPageProps> = ({
  type = 'list',
  showHeader = true,
  showStats = false,
  rows = 5,
}) => {
  /**
   * 渲染统计卡片骨架屏
   */
  const renderStats = () => {
    if (!showStats) return null;

    return (
      <Row gutter={16} style={{ marginBottom: 24 }}>
        {[1, 2, 3, 4].map((i) => (
          <Col span={6} key={i}>
            <Card>
              <Skeleton active paragraph={{ rows: 1 }} />
            </Card>
          </Col>
        ))}
      </Row>
    );
  };

  /**
   * 渲染页面头部骨架屏
   */
  const renderHeader = () => {
    if (!showHeader) return null;

    return (
      <div style={{ marginBottom: 24 }}>
        <Skeleton.Input active style={{ width: 200, height: 32 }} />
        <div style={{ marginTop: 8 }}>
          <Skeleton.Input active style={{ width: 300, height: 24 }} />
        </div>
      </div>
    );
  };

  /**
   * 渲染列表骨架屏
   */
  const renderList = () => (
    <Card>
      {/* 搜索栏骨架屏 */}
      <Space style={{ marginBottom: 16, width: '100%' }} direction="vertical">
        <Row gutter={16}>
          <Col span={6}>
            <Skeleton.Input active block />
          </Col>
          <Col span={6}>
            <Skeleton.Input active block />
          </Col>
          <Col span={6}>
            <Skeleton.Input active block />
          </Col>
          <Col span={6}>
            <Skeleton.Button active block />
          </Col>
        </Row>
      </Space>

      {/* 表格骨架屏 */}
      <Skeleton active paragraph={{ rows }} />
      <Skeleton active paragraph={{ rows }} />
    </Card>
  );

  /**
   * 渲染详情骨架屏
   */
  const renderDetail = () => (
    <Card>
      {/* 详情头部 */}
      <Row gutter={24} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <Skeleton active title paragraph={{ rows: 4 }} />
        </Col>
        <Col span={12}>
          <Skeleton active title paragraph={{ rows: 4 }} />
        </Col>
      </Row>

      {/* 详情内容 */}
      <Skeleton active title paragraph={{ rows: 6 }} />
    </Card>
  );

  /**
   * 渲染表单骨架屏
   */
  const renderForm = () => (
    <Card>
      <Space direction="vertical" style={{ width: '100%' }} size="large">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i}>
            <Skeleton.Input active style={{ width: 120, marginBottom: 8 }} />
            <Skeleton.Input active block style={{ height: 40 }} />
          </div>
        ))}
        <div style={{ marginTop: 16 }}>
          <Space>
            <Skeleton.Button active style={{ width: 100 }} />
            <Skeleton.Button active style={{ width: 100 }} />
          </Space>
        </div>
      </Space>
    </Card>
  );

  /**
   * 渲染仪表盘骨架屏
   */
  const renderDashboard = () => (
    <>
      {/* 图表行 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <Card>
            <Skeleton active paragraph={{ rows: 8 }} />
          </Card>
        </Col>
        <Col span={12}>
          <Card>
            <Skeleton active paragraph={{ rows: 8 }} />
          </Card>
        </Col>
      </Row>

      {/* 列表行 */}
      <Row gutter={16}>
        <Col span={24}>
          <Card>
            <Skeleton active paragraph={{ rows: 6 }} />
          </Card>
        </Col>
      </Row>
    </>
  );

  /**
   * 根据类型渲染骨架屏
   */
  const renderContent = () => {
    switch (type) {
      case 'list':
        return renderList();
      case 'detail':
        return renderDetail();
      case 'form':
        return renderForm();
      case 'dashboard':
        return renderDashboard();
      default:
        return renderList();
    }
  };

  return (
    <div style={{ padding: '24px' }}>
      {renderHeader()}
      {renderStats()}
      {renderContent()}
    </div>
  );
};
