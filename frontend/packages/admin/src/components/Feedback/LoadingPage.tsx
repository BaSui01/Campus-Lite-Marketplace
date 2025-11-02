/**
 * 全屏加载页面组件
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React from 'react';
import { Spin, Typography, Space, Progress } from 'antd';
import { Center } from '../Layout';

const { Title, Text } = Typography;

interface LoadingPageProps {
  title?: string;
  subtitle?: string;
  progress?: number;
  showLogo?: boolean;
}

const LoadingPage: React.FC<LoadingPageProps> = ({
  title = '系统加载中...',
  subtitle,
  progress,
  showLogo = true,
}) => {
  return (
    <div style={{
      height: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    }}>
      <Center>
        <Space direction="vertical" size="large" align="center">
          {showLogo && (
            <div style={{
              width: 80,
              height: 80,
              borderRadius: '50%',
              background: 'rgba(255,255,255,0.1)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              backdropFilter: 'blur(10px)',
            }}>
              <div style={{
                width: 40,
                height: 40,
                borderRadius: '50%',
                background: 'white',
                animation: 'pulse 2s infinite',
              }} />
            </div>
          )}
          
          <Spin size="large">
            <div style={{ textAlign: 'center', padding: '40px' }}>
              <Title level={3} style={{ color: 'white', margin: 0 }}>
                {title}
              </Title>
              {subtitle && (
                <Text style={{ color: 'rgba(255,255,255,0.8)', fontSize: 16 }}>
                  {subtitle}
                </Text>
              )}
            </div>
          </Spin>

          {progress !== undefined && (
            <div style={{ width: 200 }}>
              <Progress
                percent={progress}
                strokeColor={{
                  '0%': '#ffffff',
                  '100%': '#ffffff',
                }}
                trailColor="rgba(255,255,255,0.2)"
                strokeWidth={6}
              />
            </div>
          )}
        </Space>
      </Center>

      <style jsx>{`
        @keyframes pulse {
          0% {
            transform: scale(0.8);
            opacity: 0.8;
          }
          50% {
            transform: scale(1);
            opacity: 1;
          }
          100% {
            transform: scale(0.8);
            opacity: 0.8;
          }
        }
      `}</style>
    </div>
  );
};

export default LoadingPage;
