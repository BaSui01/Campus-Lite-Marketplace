/**
 * 性能监控面板组件
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React from 'react';
import { Card, Progress, Typography, Row, Col, Tag } from 'antd';
import { MonitorOutlined } from '@ant-design/icons';
import { useMemoryMonitor } from '@/utils/performance';

const { Text } = Typography;

interface PerformancePanelProps {
  visible: boolean;
}

const PerformancePanel: React.FC<PerformancePanelProps> = ({ visible }) => {
  const { used, total, percentage } = useMemoryMonitor();

  if (!visible) return null;

  const formatBytes = (bytes: number) => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const getMemoryStatusColor = (percentage: number) => {
    if (percentage < 50) return '#52c41a';
    if (percentage < 80) return '#faad14';
    return '#f5222d';
  };

  const getMemoryStatusText = (percentage: number) => {
    if (percentage < 50) return '正常';
    if (percentage < 80) return '警告';
    return '严重';
  };

  return (
    <Card
      size="small"
      title={
        <Text>
          <MonitorOutlined style={{ marginRight: 8 }} />
          性能监控
        </Text>
      }
      style={{ position: 'fixed', top: 16, right: 16, width: 300, zIndex: 9999 }}
      bodyStyle={{ padding: '16px' }}
    >
      <Row gutter={[8, 8]}>
        <Col span={24}>
          <div>
            <Text type="secondary">内存使用率</Text>
            <Progress
              percent={Math.round(percentage)}
              strokeColor={getMemoryStatusColor(percentage)}
              size="small"
            />
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}>
              <Text type="secondary">{formatBytes(used)}</Text>
              <Text type="secondary">{formatBytes(total)}</Text>
            </div>
          </div>
        </Col>
        <Col span={24}>
          <div style={{ textAlign: 'center' }}>
            <Tag color={getMemoryStatusColor(percentage)}>
              {getMemoryStatusText(percentage)}
            </Tag>
          </div>
        </Col>
      </Row>
    </Card>
  );
};

export default PerformancePanel;
