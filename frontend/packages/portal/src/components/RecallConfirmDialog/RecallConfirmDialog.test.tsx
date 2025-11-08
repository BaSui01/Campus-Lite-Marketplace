/**
 * RecallConfirmDialog 组件测试
 * @author BaSui 😎
 * @description 测试消息撤回确认对话框的功能
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import RecallConfirmDialog from './index';

describe('RecallConfirmDialog', () => {
  const mockOnConfirm = jest.fn();
  const mockOnCancel = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders dialog when visible', () => {
    render(
      <RecallConfirmDialog
        visible={true}
        messagePreview="Hello world"
        messageTime="10:30"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
      />
    );

    expect(screen.getByText('撤回消息')).toBeInTheDocument();
    expect(screen.getByText('确认要撤回这条消息吗？')).toBeInTheDocument();
    expect(screen.getByText('Hello world')).toBeInTheDocument();
    expect(screen.getByText('10:30')).toBeInTheDocument();
    expect(screen.getByText('取消')).toBeInTheDocument();
    expect(screen.getByText('确认撤回')).toBeInTheDocument();
  });

  it('does not render when not visible', () => {
    render(
      <RecallConfirmDialog
        visible={false}
        messagePreview="Hello world"
        messageTime="10:30"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
      />
    );

    expect(screen.queryByText('撤回消息')).not.toBeInTheDocument();
  });

  it('calls onConfirm when confirm button is clicked', async () => {
    render(
      <RecallConfirmDialog
        visible={true}
        messagePreview="Hello world"
        messageTime="10:30"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
      />
    );

    fireEvent.click(screen.getByText('确认撤回'));

    await waitFor(() => {
      expect(mockOnConfirm).toHaveBeenCalledTimes(1);
    });
  });

  it('calls onCancel when cancel button is clicked', () => {
    render(
      <RecallConfirmDialog
        visible={true}
        messagePreview="Hello world"
        messageTime="10:30"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
      />
    );

    fireEvent.click(screen.getByText('取消'));

    expect(mockOnCancel).toHaveBeenCalledTimes(1);
  });

  it('calls onCancel when backdrop is clicked', () => {
    render(
      <RecallConfirmDialog
        visible={true}
        messagePreview="Hello world"
        messageTime="10:30"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
      />
    );

    // 点击背景遮罩
    const backdrop = screen.getByText('撤回消息').closest('[style*="background-color"]');
    if (backdrop?.parentElement) {
      fireEvent.click(backdrop.parentElement);
    }

    expect(mockOnCancel).toHaveBeenCalledTimes(1);
  });

  it('does not call onConfirm when dialog content is clicked', () => {
    render(
      <RecallConfirmDialog
        visible={true}
        messagePreview="Hello world"
        messageTime="10:30"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
      />
    );

    // 点击对话框内容区域
    const dialog = screen.getByText('撤回消息').closest('.bg-white');
    if (dialog) {
      fireEvent.click(dialog);
    }

    expect(mockOnConfirm).not.toHaveBeenCalled();
    expect(mockOnCancel).not.toHaveBeenCalled();
  });

  it('shows loading state when recalling', () => {
    render(
      <RecallConfirmDialog
        visible={true}
        messagePreview="Hello world"
        messageTime="10:30"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
        loading={true}
      />
    );

    expect(screen.getByText('撤回中...')).toBeInTheDocument();
    expect(screen.getByText('确认撤回')).toBeDisabled();
    expect(screen.getByText('取消')).not.toBeDisabled();
  });

  it('disables confirm button when cannot recall', () => {
    render(
      <RecallConfirmDialog
        visible={true}
        messagePreview="Hello world"
        messageTime="10:30"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
        remainingTime={0} // 已超时
      />
    );

    expect(screen.getByText('确认撤回')).toBeDisabled();
    expect(screen.getByText('已超过撤回时间限制')).toBeInTheDocument();
  });

  it('shows remaining time countdown', () => {
    render(
      <RecallConfirmDialog
        visible={true}
        messagePreview="Hello world"
        messageTime="10:30"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
        remainingTime={120} // 2分钟
        timeLimit={5}
      />
    );

    expect(screen.getByText('还可撤回 2分0秒')).toBeInTheDocument();

    // 检查进度条
    const progressBar = screen.getByRole('progressbar');
    expect(progressBar).toBeInTheDocument();
  });

  it('truncates long message preview', () => {
    const longMessage = 'This is a very long message that should be truncated when displayed in the preview dialog because it exceeds the maximum length limit';

    render(
      <RecallConfirmDialog
        visible={true}
        messagePreview={longMessage}
        messageTime="10:30"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
      />
    );

    expect(screen.getByText('This is a very long message that should be truncated when...')).toBeInTheDocument();
  });

  it('displays custom time limit', () => {
    render(
      <RecallConfirmDialog
        visible={true}
        messagePreview="Hello world"
        messageTime="10:30"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
        timeLimit={10} // 10分钟
        remainingTime={180} // 3分钟
      />
    );

    expect(screen.getByText('消息发送后10分钟内可撤回')).toBeInTheDocument();
    expect(screen.getByText('还可撤回 3分0秒')).toBeInTheDocument();
  });

  it('handles keyboard events', () => {
    render(
      <RecallConfirmDialog
        visible={true}
        messagePreview="Hello world"
        messageTime="10:30"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
      />
    );

    // 测试 Escape 键
    fireEvent.keyDown(document, { key: 'Escape' });
    expect(mockOnCancel).toHaveBeenCalledTimes(1);
  });

  it('applies custom className', () => {
    render(
      <RecallConfirmDialog
        visible={true}
        messagePreview="Hello world"
        messageTime="10:30"
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
        className="custom-dialog-class"
      />
    );

    const overlay = screen.getByText('撤回消息').closest('.recall-confirm-dialog-overlay');
    expect(overlay).toHaveClass('custom-dialog-class');
  });
});