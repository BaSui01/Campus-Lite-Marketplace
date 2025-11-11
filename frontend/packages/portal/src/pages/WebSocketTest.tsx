/**
 * WebSocket 测试页面
 * @author BaSui 😎
 * @description 验证 MessageSessionManager 和 DisputeSessionManager 独立运行
 */

import React, { useState, useEffect } from 'react';
import { WebSocketClient } from '@campus/shared/utils/websocket';
import { getAccessToken } from '@campus/shared';

const WebSocketTest: React.FC = () => {
  const [messageStatus, setMessageStatus] = useState<string>('未连接');
  const [disputeStatus, setDisputeStatus] = useState<string>('未连接');
  const [messageLog, setMessageLog] = useState<string[]>([]);
  const [disputeLog, setDisputeLog] = useState<string[]>([]);
  const [messageClient, setMessageClient] = useState<WebSocketClient | null>(null);
  const [disputeClient, setDisputeClient] = useState<WebSocketClient | null>(null);

  // 添加日志
  const addMessageLog = (msg: string) => {
    setMessageLog(prev => [...prev.slice(-9), `[${new Date().toLocaleTimeString()}] ${msg}`]);
  };

  const addDisputeLog = (msg: string) => {
    setDisputeLog(prev => [...prev.slice(-9), `[${new Date().toLocaleTimeString()}] ${msg}`]);
  };

  // 连接消息端点
  const connectMessage = () => {
    const token = getAccessToken();
    if (!token) {
      addMessageLog('❌ 未登录，无法连接');
      return;
    }

    const client = new WebSocketClient({
      url: `ws://localhost:8200/api/ws/message`,
      heartbeatInterval: 30000,
      onOpen: () => {
        setMessageStatus('已连接');
        addMessageLog('✅ 连接成功');
      },
      onClose: () => {
        setMessageStatus('已断开');
        addMessageLog('⚠️ 连接断开');
      },
      onError: () => {
        setMessageStatus('连接错误');
        addMessageLog('❌ 连接错误');
      },
      onMessage: (msg) => {
        addMessageLog(`📨 收到消息: ${JSON.stringify(msg)}`);
      },
    });

    client.connect();
    setMessageClient(client);
    addMessageLog('🔌 正在连接...');
  };

  // 连接纠纷端点
  const connectDispute = () => {
    const token = getAccessToken();
    if (!token) {
      addDisputeLog('❌ 未登录，无法连接');
      return;
    }

    const client = new WebSocketClient({
      url: `ws://localhost:8200/api/ws/dispute`,
      heartbeatInterval: 30000,
      onOpen: () => {
        setDisputeStatus('已连接');
        addDisputeLog('✅ 连接成功');
      },
      onClose: () => {
        setDisputeStatus('已断开');
        addDisputeLog('⚠️ 连接断开');
      },
      onError: () => {
        setDisputeStatus('连接错误');
        addDisputeLog('❌ 连接错误');
      },
      onMessage: (msg) => {
        addDisputeLog(`📨 收到消息: ${JSON.stringify(msg)}`);
      },
    });

    client.connect();
    setDisputeClient(client);
    addDisputeLog('🔌 正在连接...');
  };

  // 断开连接
  const disconnectMessage = () => {
    if (messageClient) {
      messageClient.disconnect();
      setMessageClient(null);
      addMessageLog('🔌 主动断开');
    }
  };

  const disconnectDispute = () => {
    if (disputeClient) {
      disputeClient.disconnect();
      setDisputeClient(null);
      addDisputeLog('🔌 主动断开');
    }
  };

  // 组件卸载时断开连接
  useEffect(() => {
    return () => {
      messageClient?.disconnect();
      disputeClient?.disconnect();
    };
  }, [messageClient, disputeClient]);

  return (
    <div style={{ padding: '20px', fontFamily: 'monospace' }}>
      <h1>🧪 WebSocket 测试页面</h1>
      <p>验证 MessageSessionManager 和 DisputeSessionManager 独立运行</p>

      <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
        {/* 消息端点 */}
        <div style={{ flex: 1, border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
          <h2>📨 消息端点 (/ws/message)</h2>
          <p>状态: <strong style={{ color: messageStatus === '已连接' ? 'green' : 'gray' }}>{messageStatus}</strong></p>
          <div style={{ marginBottom: '10px' }}>
            <button onClick={connectMessage} disabled={messageStatus === '已连接'} style={{ marginRight: '10px' }}>
              连接
            </button>
            <button onClick={disconnectMessage} disabled={messageStatus !== '已连接'}>
              断开
            </button>
          </div>
          <div style={{ 
            background: '#f5f5f5', 
            padding: '10px', 
            borderRadius: '4px', 
            height: '200px', 
            overflow: 'auto',
            fontSize: '12px'
          }}>
            {messageLog.length === 0 ? (
              <div style={{ color: '#999' }}>暂无日志</div>
            ) : (
              messageLog.map((log, index) => <div key={index}>{log}</div>)
            )}
          </div>
        </div>

        {/* 纠纷端点 */}
        <div style={{ flex: 1, border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
          <h2>⚖️ 纠纷端点 (/ws/dispute)</h2>
          <p>状态: <strong style={{ color: disputeStatus === '已连接' ? 'green' : 'gray' }}>{disputeStatus}</strong></p>
          <div style={{ marginBottom: '10px' }}>
            <button onClick={connectDispute} disabled={disputeStatus === '已连接'} style={{ marginRight: '10px' }}>
              连接
            </button>
            <button onClick={disconnectDispute} disabled={disputeStatus !== '已连接'}>
              断开
            </button>
          </div>
          <div style={{ 
            background: '#f5f5f5', 
            padding: '10px', 
            borderRadius: '4px', 
            height: '200px', 
            overflow: 'auto',
            fontSize: '12px'
          }}>
            {disputeLog.length === 0 ? (
              <div style={{ color: '#999' }}>暂无日志</div>
            ) : (
              disputeLog.map((log, index) => <div key={index}>{log}</div>)
            )}
          </div>
        </div>
      </div>

      <div style={{ marginTop: '20px', padding: '15px', background: '#e8f5e9', borderRadius: '8px' }}>
        <h3>✅ 测试步骤</h3>
        <ol>
          <li>确保后端已启动（http://localhost:8200）</li>
          <li>点击"连接"按钮连接两个端点</li>
          <li>观察两个端点是否都能成功连接</li>
          <li>检查后端日志，应该看到两个独立的连接记录</li>
          <li>如果都显示"已连接"，说明修复成功！🎉</li>
        </ol>
      </div>

      <div style={{ marginTop: '20px', padding: '15px', background: '#fff3cd', borderRadius: '8px' }}>
        <h3>⚠️ 预期行为</h3>
        <ul>
          <li><strong>修复前</strong>：连接第二个端点时，第一个会被强制断开</li>
          <li><strong>修复后</strong>：两个端点可以同时连接，互不影响</li>
        </ul>
      </div>
    </div>
  );
};

export default WebSocketTest;
