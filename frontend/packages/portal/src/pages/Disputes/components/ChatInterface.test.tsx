/**
 * ChatInterface 组件测试
 * @author BaSui 😎
 * @description 测试纠纷协商沟通界面组件
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { ChatInterface } from './ChatInterface';

// Mock WebSocket
jest.mock('@campus/shared/hooks', () => ({
  useWebSocket: jest.fn(),
}));

import { useWebSocket } from '@campus/shared/hooks';

describe('ChatInterface', () => {
  const mockSend = jest.fn();
  const mockLastMessage = null;
  const mockReadyState = 1; // OPEN

  beforeEach(() => {
    (useWebSocket as jest.Mock).mockReturnValue({
      lastMessage: mockLastMessage,
      send: mockSend,
      readyState: mockReadyState,
      connect: jest.fn(),
      disconnect: jest.fn(),
      reconnectCount: 0,
    });

    mockSend.mockClear();
  });

  const defaultProps = {
    disputeId: 1,
    currentUserId: 1,
    currentUserRole: 'seller' as const,
    otherUser: {
      id: 2,
      name: '买家',
      role: 'buyer' as const,
    },
    disputeStatus: 'NEGOTIATING',
  };

  it('renders chat interface with correct elements', () => {
    render(<ChatInterface {...defaultProps} />);

    // 检查对方用户信息
    expect(screen.getByText('买家')).toBeInTheDocument();
    expect(screen.getByText('卖家')).toBeInTheDocument();

    // 检查连接状态
    expect(screen.getByText('已连接')).toBeInTheDocument();

    // 检查输入框
    expect(screen.getByPlaceholderText('输入消息...')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /发送/ })).toBeInTheDocument();
  });

  it('displays typing indicator when other user is typing', () => {
    render(<ChatInterface {...defaultProps} />);

    // 模拟正在输入状态
    // 这里需要通过props或状态来控制
  });

  it('disables input when connection is lost', () => {
    (useWebSocket as jest.Mock).mockReturnValue({
      lastMessage: mockLastMessage,
      send: mockSend,
      readyState: 3, // CLOSED
      connect: jest.fn(),
      disconnect: jest.fn(),
      reconnectCount: 1,
    });

    render(<ChatInterface {...defaultProps} />);

    expect(screen.getByPlaceholderText('连接中，请稍候...')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /发送/ })).toBeDisabled();
  });

  it('shows correct status for different dispute statuses', () => {
    const { rerender } = render(<ChatInterface {...defaultProps} />);

    // 协商中状态应该显示聊天界面
    expect(screen.getByText('协商沟通')).toBeInTheDocument();

    // 测试其他状态
    rerender(<ChatInterface {...defaultProps} disputeStatus="PENDING_ARBITRATION" />);
    expect(screen.getByText('纠纷已升级为仲裁')).toBeInTheDocument();

    rerender(<ChatInterface {...defaultProps} disputeStatus="RESOLVED" />);
    expect(screen.getByText('纠纷已解决')).toBeInTheDocument();
  });

  it('handles file upload', async () => {
    render(<ChatInterface {...defaultProps} />);

    const fileInput = screen.getByLabelText(/发送文件/);
    expect(fileInput).toBeInTheDocument();

    const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });

    fireEvent.change(fileInput, { target: { files: [file] } });

    await waitFor(() => {
      expect(mockSend).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'message',
          payload: expect.objectContaining({
            messageType: 'image',
            fileName: 'test.jpg',
          }),
        })
      );
    });
  });

  it('sends message on form submit', async () => {
    render(<ChatInterface {...defaultProps} />);

    const input = screen.getByPlaceholderText('输入消息...');
    const sendButton = screen.getByRole('button', { name: /发送/ });

    // 输入消息
    fireEvent.change(input, { target: { value: 'Hello, this is a test message' } });

    // 点击发送按钮
    fireEvent.click(sendButton);

    await waitFor(() => {
      expect(mockSend).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'message',
          payload: expect.objectContaining({
            content: 'Hello, this is a test message',
            messageType: 'text',
          }),
        })
      );
    });
  });

  it('sends message on Enter key press', async () => {
    render(<ChatInterface {...defaultProps} />);

    const input = screen.getByPlaceholderText('输入消息...');

    // 输入消息并按Enter
    fireEvent.change(input, { target: { value: 'Test message' } });
    fireEvent.keyDown(input, { key: 'Enter' });

    await waitFor(() => {
      expect(mockSend).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'message',
          payload: expect.objectContaining({
            content: 'Test message',
            messageType: 'text',
          }),
        })
      );
    });

    // 输入框应该被清空
    expect(input).toHaveValue('');
  });

  it('does not send message with Shift+Enter', () => {
    render(<ChatInterface {...defaultProps} />);

    const input = screen.getByPlaceholderText('输入消息...');

    // 输入消息并按Shift+Enter
    fireEvent.change(input, { target: { value: 'Test message\nwith new line' } });
    fireEvent.keyDown(input, { key: 'Enter', shiftKey: true });

    // 应该不发送消息
    expect(mockSend).not.toHaveBeenCalled();

    // 消息应该保留在输入框中
    expect(input).toHaveValue('Test message\nwith new line');
  });

  it('disables chat for arbitrator role', () => {
    render(<ChatInterface {...defaultProps} currentUserRole="arbitrator" />);

    expect(screen.getByText('仲裁员无法参与协商沟通')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('输入消息...')).toBeDisabled();
    expect(screen.getByRole('button', { name: /发送/ })).toBeDisabled();
  });

  it('disables chat when dispute is not in negotiating status', () => {
    render(<ChatInterface {...defaultProps} disputeStatus="RESOLVED" />);

    expect(screen.getByText('纠纷已解决')).toBeInTheDocument();
    expect(screen.queryByPlaceholderText('输入消息...')).not.toBeInTheDocument();
  });
});