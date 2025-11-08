/**
 * 消息详情页（会话聊天记录）
 * 
 * 功能：
 * - 查看会话聊天记录
 * - 查看参与用户信息
 * - 消息时间线展示
 * - 图片预览
 * - 商品卡片跳转
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  Button,
  Space,
  Spin,
  Alert,
  Avatar,
  List,
  Image,
  Tag,
  Descriptions,
} from 'antd';
import {
  ArrowLeftOutlined,
  UserOutlined,
  MessageOutlined,
  PictureOutlined,
  ShoppingOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { getApi } from '@campus/shared/utils/apiClient';
import type { MessageResponse } from '@campus/shared/api';
import dayjs from 'dayjs';

/**
 * 消息类型映射
 */
const MESSAGE_TYPE_MAP: Record<string, { text: string; color: string; icon: React.ReactNode }> = {
  TEXT: { 
    text: '文本', 
    color: 'blue',
    icon: <MessageOutlined />
  },
  IMAGE: { 
    text: '图片', 
    color: 'green',
    icon: <PictureOutlined />
  },
  GOODS_CARD: { 
    text: '商品', 
    color: 'orange',
    icon: <ShoppingOutlined />
  },
};

export const MessageDetail: React.FC = () => {
  const { conversationId } = useParams<{ conversationId: string }>();
  const navigate = useNavigate();

  // 查询会话聊天记录
  const { data, isLoading, error } = useQuery({
    queryKey: ['messages', 'conversation', conversationId],
    queryFn: async () => {
      const api = getApi();
      const response = await api.listMessagesInConversation(
        Number(conversationId),
        0,
        100 // 加载最近100条消息
      );
      return response.data.data;
    },
    enabled: !!conversationId,
  });

  // 返回列表
  const handleBack = () => {
    navigate('/admin/messages/list');
  };

  if (isLoading) {
    return (
      <div style={{ padding: '24px', textAlign: 'center' }}>
        <Spin size="large" tip="加载中..." />
      </div>
    );
  }

  if (error || !data) {
    return (
      <div style={{ padding: '24px' }}>
        <Alert
          message="加载失败"
          description="无法加载消息详情，请稍后重试"
          type="error"
          showIcon
        />
        <Button
          type="primary"
          icon={<ArrowLeftOutlined />}
          onClick={handleBack}
          style={{ marginTop: 16 }}
        >
          返回列表
        </Button>
      </div>
    );
  }

  const messages = data.content || [];

  return (
    <div style={{ padding: '24px' }}>
      {/* 页面头部 */}
      <Space style={{ marginBottom: 24 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={handleBack}>
          返回
        </Button>
      </Space>

      {/* 会话信息 */}
      <Card title="会话信息" style={{ marginBottom: 24 }}>
        <Descriptions column={2}>
          <Descriptions.Item label="会话ID">{conversationId}</Descriptions.Item>
          <Descriptions.Item label="消息数量">{data.totalElements}</Descriptions.Item>
        </Descriptions>
      </Card>

      {/* 聊天记录 */}
      <Card title={`聊天记录 (${messages.length})`}>
        <List
          itemLayout="horizontal"
          dataSource={messages}
          renderItem={(item: MessageResponse) => {
            const typeConfig = MESSAGE_TYPE_MAP[item.messageType as string] || { 
              text: item.messageType, 
              color: 'default',
              icon: null 
            };

            return (
              <List.Item
                style={{
                  padding: '16px',
                  borderBottom: '1px solid #f0f0f0',
                }}
              >
                <List.Item.Meta
                  avatar={<Avatar icon={<UserOutlined />} />}
                  title={
                    <Space>
                      <span style={{ fontWeight: 'bold' }}>
                        {item.senderUsername || `用户${item.senderId}`}
                      </span>
                      <Tag color={typeConfig.color} icon={typeConfig.icon}>
                        {typeConfig.text}
                      </Tag>
                      {item.isRecalled && (
                        <Tag color="red">已撤回</Tag>
                      )}
                      <span style={{ color: '#8c8c8c', fontSize: 12 }}>
                        {dayjs(item.createdAt).format('YYYY-MM-DD HH:mm:ss')}
                      </span>
                    </Space>
                  }
                  description={
                    <div style={{ marginTop: 8 }}>
                      {item.isRecalled ? (
                        <span style={{ color: '#8c8c8c', fontStyle: 'italic' }}>
                          该消息已被撤回
                        </span>
                      ) : (
                        <>
                          {item.messageType === 'TEXT' && (
                            <div style={{ fontSize: 14 }}>{item.content}</div>
                          )}
                          {item.messageType === 'IMAGE' && (
                            <Image
                              src={item.content}
                              alt="图片消息"
                              width={200}
                              style={{ borderRadius: 8 }}
                            />
                          )}
                          {item.messageType === 'GOODS_CARD' && (
                            <Card
                              size="small"
                              style={{ width: 300, cursor: 'pointer' }}
                              onClick={() => {
                                // 解析商品ID并跳转
                                try {
                                  const goodsData = JSON.parse(item.content || '{}');
                                  if (goodsData.goodsId) {
                                    navigate(`/admin/goods/${goodsData.goodsId}`);
                                  }
                                } catch (e) {
                                  console.error('解析商品卡片失败', e);
                                }
                              }}
                            >
                              <Space>
                                <ShoppingOutlined style={{ fontSize: 24 }} />
                                <div>
                                  <div style={{ fontWeight: 'bold' }}>商品卡片</div>
                                  <div style={{ fontSize: 12, color: '#8c8c8c' }}>
                                    点击查看详情
                                  </div>
                                </div>
                              </Space>
                            </Card>
                          )}
                        </>
                      )}
                    </div>
                  }
                />
              </List.Item>
            );
          }}
        />

        {messages.length === 0 && (
          <div style={{ textAlign: 'center', padding: '40px', color: '#8c8c8c' }}>
            <MessageOutlined style={{ fontSize: 48, marginBottom: 16 }} />
            <div>暂无消息</div>
          </div>
        )}
      </Card>
    </div>
  );
};

export default MessageDetail;
