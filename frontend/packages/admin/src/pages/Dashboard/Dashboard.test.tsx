/**
 * Dashboard 简化测试版本
 * 用于排查渲染问题
 */

import React from 'react';
import { Card, Typography } from 'antd';

const { Title } = Typography;

const DashboardTest: React.FC = () => {
  console.log('🔍 Dashboard Test 组件已渲染');
  
  return (
    <div style={{ padding: '24px' }}>
      <Title level={2}>📊 数据看板 - 测试版</Title>
      <Card>
        <p>✅ Dashboard 组件渲染成功！</p>
        <p>🔍 请检查浏览器控制台是否有请求发出</p>
      </Card>
    </div>
  );
};

export default DashboardTest;
