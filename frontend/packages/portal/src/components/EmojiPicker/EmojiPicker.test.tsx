/**
 * EmojiPicker 组件测试
 * @author BaSui 😎
 * @description 测试表情选择器组件的功能
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { EmojiPicker } from './index';
import type { EmojiItem } from '@campus/shared/types/emoji';

// Mock emojiService
jest.mock('../../../services/emoji', () => ({
  emojiService: {
    getEmojiPacks: jest.fn(),
    getRecentlyUsedEmojis: jest.fn(),
    getFavoriteEmojis: jest.fn(),
    searchEmojis: jest.fn(),
    recordEmojiUsage: jest.fn(),
    toggleEmojiFavorite: jest.fn(),
  },
}));

import { emojiService } from '../../../services/emoji';

const mockEmojiItem: EmojiItem = {
  id: 'emoji-1',
  name: '笑脸',
  content: '😊',
  contentType: 'text',
  category: 'SMILEYS',
  packId: 'system-smileys',
  sortOrder: 1,
  isFavorite: false,
  useCount: 100,
  createdAt: '2025-01-01',
  updatedAt: '2025-01-01',
};

describe('EmojiPicker', () => {
  const mockOnEmojiSelect = jest.fn();
  const mockOnClose = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    // Mock API responses
    (emojiService.getEmojiPacks as jest.Mock).mockResolvedValue({
      packs: [
        {
          id: 'system-smileys',
          name: '经典笑脸',
          type: 'SYSTEM',
          status: 'ACTIVE',
          emojis: [mockEmojiItem],
          isBuiltIn: true,
          downloadCount: 1000,
          favoriteCount: 50,
          sortOrder: 1,
          createdAt: '2025-01-01',
          updatedAt: '2025-01-01',
        }
      ],
      total: 1,
      page: 0,
      size: 10,
      totalPages: 1,
    });

    (emojiService.getRecentlyUsedEmojis as jest.Mock).mockResolvedValue([mockEmojiItem]);
    (emojiService.getFavoriteEmojis as jest.Mock).mockResolvedValue([]);
    (emojiService.searchEmojis as jest.Mock).mockResolvedValue([mockEmojiItem]);
    (emojiService.recordEmojiUsage as jest.Mock).mockResolvedValue(true);
    (emojiService.toggleEmojiFavorite as jest.Mock).mockResolvedValue(true);
  });

  it('renders emoji picker when visible', () => {
    render(
      <EmojiPicker
        visible={true}
        onEmojiSelect={mockOnEmojiSelect}
        onClose={mockOnClose}
      />
    );

    expect(screen.getByPlaceholderText('搜索表情...')).toBeInTheDocument();
    expect(screen.getByText('最近')).toBeInTheDocument();
    expect(screen.getByText('收藏')).toBeInTheDocument();
    expect(screen.getByText('笑脸')).toBeInTheDocument();
  });

  it('does not render when not visible', () => {
    render(
      <EmojiPicker
        visible={false}
        onEmojiSelect={mockOnEmojiSelect}
        onClose={mockOnClose}
      />
    );

    expect(screen.queryByPlaceholderText('搜索表情...')).not.toBeInTheDocument();
  });

  it('calls onEmojiSelect when emoji is clicked', async () => {
    render(
      <EmojiPicker
        visible={true}
        onEmojiSelect={mockOnEmojiSelect}
        onClose={mockOnClose}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('😊')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('😊'));

    await waitFor(() => {
      expect(emojiService.recordEmojiUsage).toHaveBeenCalledWith('emoji-1', 'chat');
      expect(mockOnEmojiSelect).toHaveBeenCalledWith(mockEmojiItem);
    });
  });

  it('calls onClose when clicking outside', () => {
    render(
      <div>
        <EmojiPicker
          visible={true}
          onEmojiSelect={mockOnEmojiSelect}
          onClose={mockOnClose}
        />
        <div data-testid="outside-element">Outside</div>
      </div>
    );

    fireEvent.click(screen.getByTestId('outside-element'));

    expect(mockOnClose).toHaveBeenCalled();
  });

  it('searches emojis when typing in search box', async () => {
    render(
      <EmojiPicker
        visible={true}
        onEmojiSelect={mockOnEmojiSelect}
        onClose={mockOnClose}
      />
    );

    const searchInput = screen.getByPlaceholderText('搜索表情...');
    fireEvent.change(searchInput, { target: { value: '笑脸' } });

    await waitFor(() => {
      expect(emojiService.searchEmojis).toHaveBeenCalledWith('笑脸', undefined);
    });
  });

  it('toggles emoji favorite on right click', async () => {
    render(
      <EmojiPicker
        visible={true}
        onEmojiSelect={mockOnEmojiSelect}
        onClose={mockOnClose}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('😊')).toBeInTheDocument();
    });

    // 右键点击表情
    fireEvent.contextMenu(screen.getByText('😊'));

    await waitFor(() => {
      expect(emojiService.toggleEmojiFavorite).toHaveBeenCalledWith('emoji-1', true);
    });
  });

  it('switches tabs correctly', async () => {
    render(
      <EmojiPicker
        visible={true}
        onEmojiSelect={mockOnEmojiSelect}
        onClose={mockOnClose}
      />
    );

    // 默认应该显示"最近"标签
    expect(screen.getByText('最近')).toHaveStyle({ 'background-color': expect.any(String) });

    // 点击收藏标签
    fireEvent.click(screen.getByText('收藏'));
    expect(screen.getByText('收藏')).toHaveStyle({ 'background-color': expect.any(String) });
    expect(emojiService.getFavoriteEmojis).toHaveBeenCalled();
  });

  it('applies custom configuration', () => {
    const customConfig = {
      showFavoriteTab: false,
      showSearch: false,
      emojisPerRow: 6,
      maxRows: 4,
      theme: 'dark' as const,
    };

    render(
      <EmojiPicker
        visible={true}
        onEmojiSelect={mockOnEmojiSelect}
        onClose={mockOnClose}
        config={customConfig}
      />
    );

    expect(screen.queryByPlaceholderText('搜索表情...')).not.toBeInTheDocument();
    expect(screen.queryByText('收藏')).not.toBeInTheDocument();
  });

  it('shows loading state while loading', () => {
    // Mock loading state
    (emojiService.getEmojiPacks as jest.Mock).mockImplementation(() => new Promise(() => {}));

    render(
      <EmojiPicker
        visible={true}
        onEmojiSelect={mockOnEmojiSelect}
        onClose={mockOnClose}
      />
    );

    expect(screen.getByText('加载中...')).toBeInTheDocument();
  });

  it('shows empty state when no emojis', async () => {
    (emojiService.getEmojiPacks as jest.Mock).mockResolvedValue({
      packs: [],
      total: 0,
      page: 0,
      size: 10,
      totalPages: 0,
    });

    render(
      <EmojiPicker
        visible={true}
        onEmojiSelect={mockOnEmojiSelect}
        onClose={mockOnClose}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('暂无表情')).toBeInTheDocument();
    });
  });

  it('shows no results when search returns empty', async () => {
    (emojiService.searchEmojis as jest.Mock).mockResolvedValue([]);

    render(
      <EmojiPicker
        visible={true}
        onEmojiSelect={mockOnEmojiSelect}
        onClose={mockOnClose}
      />
    );

    const searchInput = screen.getByPlaceholderText('搜索表情...');
    fireEvent.change(searchInput, { target: { value: '不存在的表情' } });

    await waitFor(() => {
      expect(screen.getByText('没有找到相关表情')).toBeInTheDocument();
    });
  });
});