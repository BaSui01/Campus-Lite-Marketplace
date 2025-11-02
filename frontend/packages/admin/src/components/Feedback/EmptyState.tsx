/**
 * 空状态组件
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React from 'react';
import { Empty, Button, Typography, Space } from 'antd';
import { useNavigate } from 'react-router-dom';
import { Center } from '../Layout';

const { Text } = Typography;

interface EmptyStateProps {
  title?: string;
  subtitle?: string;
  actionText?: string;
  onAction?: () => void;
  icon?: React.ReactNode;
  extra?: React.ReactNode;
}

const EmptyState: React.FC<EmptyStateProps> = ({
  title = '暂无数据',
  subtitle = '当前页面没有任何数据',
  actionText = '刷新页面',
  onAction,
  icon,
  extra,
}) => {
  const navigate = useNavigate();

  const handleDefaultAction = () => {
    if (onAction) {
      onAction();
    } else {
      navigate(-1); // 返回上一页
    }
  };

  return (
    <Center style={{ padding: '60px 20px' }}>
      <Empty
        image={icon || Empty.PRESENTED_IMAGE_SIMPLE}
        imageStyle={{
          height: 120,
          opacity: 0.6,
        }}
      >
        <Space direction="vertical" align="center" size="middle">
          <Text style={{ fontSize: 16, color: '#999', fontWeight: 500 }}>
            {title}
          </Text>
          <Text style={{ fontSize: 14, color: '#bbb' }}>
            {subtitle}
          </Text>
          <Button type="primary" onClick={handleDefaultAction}>
            {actionText}
          </Button>
          {extra}
        </Space>
      </Empty>
    </Center>
  );
};

// 预设空状态变体
export const NoDataEmpty = (props: Omit<EmptyStateProps, 'title' | 'subtitle'>) => (
  <EmptyState
    title="暂无数据"
    subtitle="还没有任何内容，试试其他操作吧"
    {...props}
  />
);

export const NoPermissionEmpty = (props: Omit<EmptyStateProps, 'title' | 'subtitle'>) => (
  <EmptyState
    title="没有权限"
    subtitle="您没有访问该页面的权限，请联系管理员"
    actionText="返回首页"
    icon={<div style={{ fontSize: 48, color: '#ff9800' }}>🚫</div>}
    {...props}
  />
);

export const NotFoundEmpty = (props: Omit<EmptyStateProps, 'title' | 'subtitle'>) => (
  <EmptyState
    title="页面不存在"
    subtitle="您访问的页面不存在，请检查URL是否正确"
    actionText="返回首页"
    icon={<div style={{ fontSize: 48, color: '#2196f3' }}>🤷‍♂️</div>}
    {...props}
  />
);

export const NetworkErrorEmpty = (props: Omit<EmptyStateProps, 'title' | 'subtitle'>) => (
  <EmptyState
    title="网络连接失败"
    subtitle="请检查网络连接，然后重试"
    actionText="重新加载"
    icon={<div style={{ fontSize: 48, color: '#f44336' }}>🔌</div>}
    {...props}
  />
);

export default EmptyState;
