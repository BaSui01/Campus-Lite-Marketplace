/**
 * EmojiDisplay 组件测试
 * @author BaSui 😎
 * @description 测试表情展示组件的功能
 */

import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import { EmojiDisplay, EmojiTextRenderer } from './index';
import type { EmojiMessage } from '@campus/shared/types/emoji';

const mockEmojiMessage: EmojiMessage = {
  type: 'emoji',
  emojiId: 'emoji-1',
  packId: 'system-smileys',
  content: '😊',
  emojiName: '笑脸',
  contentType: 'text',
  packName: '经典笑脸',
};

describe('EmojiDisplay', () => {
  it('renders text emoji correctly', () => {
    render(<EmojiDisplay emoji={mockEmojiMessage} />);

    const emojiElement = screen.getByText('😊');
    expect(emojiElement).toBeInTheDocument();
    expect(emojiElement).toHaveAttribute('title', '笑脸');
  });

  it('renders image emoji correctly', () => {
    const imageEmoji: EmojiMessage = {
      ...mockEmojiMessage,
      contentType: 'image',
      content: 'https://example.com/emoji.png',
    };

    render(<EmojiDisplay emoji={imageEmoji} />);

    const imageElement = screen.getByAltText('笑脸');
    expect(imageElement).toBeInTheDocument();
    expect(imageElement).toHaveAttribute('src', 'https://example.com/emoji.png');
  });

  it('applies correct size classes', () => {
    const { rerender } = render(<EmojiDisplay emoji={mockEmojiMessage} size="small" />);
    expect(screen.getByText('😊')).toHaveStyle({ fontSize: '16px' });

    rerender(<EmojiDisplay emoji={mockEmojiMessage} size="medium" />);
    expect(screen.getByText('😊')).toHaveStyle({ fontSize: '24px' });

    rerender(<EmojiDisplay emoji={mockEmojiMessage} size="large" />);
    expect(screen.getByText('😊')).toHaveStyle({ fontSize: '48px' });
  });

  it('shows preview modal when clicked and clickable is true', () => {
    render(<EmojiDisplay emoji={mockEmojiMessage} clickable={true} />);

    fireEvent.click(screen.getByText('😊'));

    // 检查模态框是否显示
    expect(screen.getByText('笑脸')).toBeInTheDocument();
    expect(screen.getByText('来自：经典笑脸')).toBeInTheDocument();
    expect(screen.getByText('关闭')).toBeInTheDocument();
  });

  it('calls custom onClick when provided', () => {
    const mockOnClick = jest.fn();

    render(<EmojiDisplay emoji={mockEmojiMessage} onClick={mockOnClick} />);

    fireEvent.click(screen.getByText('😊'));

    expect(mockOnClick).toHaveBeenCalledWith(mockEmojiMessage);
  });

  it('does not show preview modal when clickable is false', () => {
    render(<EmojiDisplay emoji={mockEmojiMessage} clickable={false} />);

    expect(screen.getByText('😊')).toBeInTheDocument();
    expect(screen.getByText('😊')).toHaveStyle({ cursor: 'default' });
  });

  it('handles image load error gracefully', () => {
    const imageEmoji: EmojiMessage = {
      ...mockEmojiMessage,
      contentType: 'image',
      content: 'https://example.com/nonexistent.png',
    };

    render(<EmojiDisplay emoji={imageEmoji} />);

    const imageElement = screen.getByAltText('笑脸');
    fireEvent.error(imageElement);

    // 检查是否显示错误占位符
    expect(screen.getByText('🚫')).toBeInTheDocument();
  });

  it('respects maxWidth prop', () => {
    render(<EmojiDisplay emoji={mockEmojiMessage} maxWidth={100} />);

    const container = screen.getByText('😊').parentElement;
    expect(container).toHaveStyle({ maxWidth: '100px' });
  });

  it('applies custom className', () => {
    render(<EmojiDisplay emoji={mockEmojiMessage} className="custom-emoji-class" />);

    const container = screen.getByText('😊').parentElement;
    expect(container).toHaveClass('custom-emoji-class');
  });

  it('closes preview modal when clicking close button', () => {
    render(<EmojiDisplay emoji={mockEmojiMessage} clickable={true} />);

    // 打开预览
    fireEvent.click(screen.getByText('😊'));
    expect(screen.getByText('关闭')).toBeInTheDocument();

    // 点击关闭按钮
    fireEvent.click(screen.getByText('关闭'));

    // 预览应该关闭
    expect(screen.queryByText('关闭')).not.toBeInTheDocument();
  });

  it('closes preview modal when clicking backdrop', () => {
    render(<EmojiDisplay emoji={mockEmojiMessage} clickable={true} />);

    // 打开预览
    fireEvent.click(screen.getByText('😊'));

    // 点击背景
    const backdrop = screen.getByText('😊').closest('[style*="background-color"]')?.parentElement;
    if (backdrop) {
      fireEvent.click(backdrop);
    }

    // 预览应该关闭
    expect(screen.queryByText('关闭')).not.toBeInTheDocument();
  });
});

describe('EmojiTextRenderer', () => {
  it('renders plain text correctly', () => {
    render(<EmojiTextRenderer text="Hello world!" />);

    expect(screen.getByText('Hello world!')).toBeInTheDocument();
  });

  it('renders text with emojis correctly', () => {
    const emojiMap: Record<string, EmojiMessage> = {
      '😊': mockEmojiMessage,
    };

    render(
      <EmojiTextRenderer
        text="Hello 😊 world!"
        emojiMap={emojiMap}
      />
    );

    expect(screen.getByText('Hello ')).toBeInTheDocument();
    expect(screen.getByText(' world!')).toBeInTheDocument();
    // 应该有 EmojiDisplay 组件渲染的表情
    expect(screen.getByText('😊')).toBeInTheDocument();
  });

  it('renders multiple emojis in text', () => {
    const emojiMap: Record<string, EmojiMessage> = {
      '😊': mockEmojiMessage,
      '👍': {
        ...mockEmojiMessage,
        emojiId: 'emoji-2',
        content: '👍',
        emojiName: '点赞',
      },
    };

    render(
      <EmojiTextRenderer
        text="😊 Good job! 👍"
        emojiMap={emojiMap}
      />
    );

    expect(screen.getByText('😊')).toBeInTheDocument();
    expect(screen.getByText(' Good job! ')).toBeInTheDocument();
    expect(screen.getByText('👍')).toBeInTheDocument();
  });

  it('handles overlapping emoji patterns correctly', () => {
    const emojiMap: Record<string, EmojiMessage> = {
      '😊': mockEmojiMessage,
      '😊😊': {
        ...mockEmojiMessage,
        emojiId: 'emoji-double',
        content: '😊😊',
        emojiName: '双笑脸',
      },
    };

    render(
      <EmojiTextRenderer
        text="😊😊 single 😊"
        emojiMap={emojiMap}
      />
    );

    // 应该优先匹配更长的表情
    expect(screen.getByText('😊😊')).toBeInTheDocument();
    expect(screen.getByText(' single ')).toBeInTheDocument();
    expect(screen.getByText('😊')).toBeInTheDocument();
  });

  it('applies correct emoji size', () => {
    const emojiMap: Record<string, EmojiMessage> = {
      '😊': mockEmojiMessage,
    };

    const { rerender } = render(
      <EmojiTextRenderer
        text="Hello 😊"
        emojiMap={emojiMap}
        emojiSize="small"
      />
    );

    expect(screen.getByText('😊')).toHaveStyle({ fontSize: '16px' });

    rerender(
      <EmojiTextRenderer
        text="Hello 😊"
        emojiMap={emojiMap}
        emojiSize="large"
      />
    );

    expect(screen.getByText('😊')).toHaveStyle({ fontSize: '48px' });
  });

  it('respects clickable prop', () => {
    const emojiMap: Record<string, EmojiMessage> = {
      '😊': mockEmojiMessage,
    };

    render(
      <EmojiTextRenderer
        text="Hello 😊"
        emojiMap={emojiMap}
        clickable={false}
      />
    );

    const emojiElement = screen.getByText('😊');
    expect(emojiElement.closest('.emoji-display')).toHaveStyle({ cursor: 'default' });
  });

  it('applies custom className', () => {
    const emojiMap: Record<string, EmojiMessage> = {
      '😊': mockEmojiMessage,
    };

    render(
      <EmojiTextRenderer
        text="Hello 😊"
        emojiMap={emojiMap}
        className="custom-text-renderer"
      />
    );

    const container = screen.getByText('Hello 😊').parentElement;
    expect(container).toHaveClass('custom-text-renderer');
  });

  it('handles empty text', () => {
    render(<EmojiTextRenderer text="" />);

    expect(screen.getByText('')).toBeInTheDocument();
  });

  it('handles text with no matching emojis', () => {
    const emojiMap: Record<string, EmojiMessage> = {
      '😊': mockEmojiMessage,
    };

    render(
      <EmojiTextRenderer
        text="Hello world!"
        emojiMap={emojiMap}
      />
    );

    expect(screen.getByText('Hello world!')).toBeInTheDocument();
    expect(screen.queryByText('😊')).not.toBeInTheDocument();
  });
});