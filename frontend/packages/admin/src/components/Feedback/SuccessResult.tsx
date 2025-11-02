/**
 * 操作成功结果组件
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React from 'react';
import { Result, Button, Typography, Space } from 'antd';
import { CheckCircleOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { Center } from '../Layout';

const { Paragraph, Title } = Typography;

interface SuccessResultProps {
  title?: string;
  subtitle?: string;
  actionText?: string;
  onAction?: () => void;
  extra?: React.ReactNode;
  showBack?: boolean;
  backText?: string;
  onBack?: () => void;
}

const SuccessResult: React.FC<SuccessResultProps> = ({
  title = '操作成功',
  subtitle = '您的操作已成功完成',
  actionText = '继续操作',
  onAction,
  extra,
  showBack = true,
  backText = '返回',
  onBack,
}) => {
  const buttonStyle = {
    borderRadius: 6,
    height: 40,
    fontSize: 14,
    fontWeight: 500,
  };

  return (
    <Center style={{ padding: '60px 20px' }}>
      <Result
        status="success"
        icon={<CheckCircleOutlined style={{ fontSize: 64, color: '#52c41a' }} />}
        title={title}
        extra={
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Paragraph style={{ textAlign: 'center', color: '#666', fontSize: 16 }}>
              {subtitle}
            </Paragraph>
            
            <Space>
              {showBack && (
                <Button 
                  style={buttonStyle}
                  icon={<ArrowLeftOutlined />}
                  onClick={onBack}
                >
                  {backText}
                </Button>
              )}
              
              <Button 
                type="primary" 
                style={{ ...buttonStyle, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', border: 'none' }}
                onClick={onAction}
              >
                {actionText}
              </Button>
            </Space>
            
            {extra}
          </Space>
        }
      />
    </Center>
  );
};

// 预设成功状态
export const CreateSuccess = (props: Omit<SuccessResultProps, 'title' | 'subtitle'>) => (
  <SuccessResult
    title="创建成功"
    subtitle="数据已成功创建，您可以继续进行下一步操作"
    actionText="继续添加"
    {...props}
  />
);

export const UpdateSuccess = (props: Omit<SuccessResultProps, 'title' | 'subtitle'>) => (
  <SuccessResult
    title="更新成功"
    subtitle="数据已成功更新，信息已即时生效"
    actionText="返回列表"
    {...props}
  />
);

export const DeleteSuccess = (props: Omit<SuccessResultProps, 'title' | 'subtitle'>) => (
  <SuccessResult
    title="删除成功"
    subtitle="数据已成功删除，此操作不可恢复"
    actionText="返回列表"
    {...props}
  />
);

export const LoginSuccess = (props: Omit<SuccessResultProps, 'title' | 'subtitle' | 'showBack'>) => (
  <SuccessResult
    title="登录成功"
    subtitle="欢迎回来！正在为您跳转到管理页面..."
    actionText="进入系统"
    showBack={false}
    {...props}
  />
);

export const SubmitSuccess = (props: Omit<SuccessResultProps, 'title' | 'subtitle'>) => (
  <SuccessResult
    title="提交成功"
    subtitle="您的请求已处理，我们会尽快完成相关操作"
    actionText="查看结果"
    {...props}
  />
);

export default SuccessResult;
