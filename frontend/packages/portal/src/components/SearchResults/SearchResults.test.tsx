/**
 * SearchResults 组件测试
 * @author BaSui 😎
 * @description 测试搜索结果组件的功能
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { SearchResults } from './index';
import type { SearchResponse, MessageType } from '@campus/shared/types/search';

// Mock 搜索响应
const mockSearchResponse: SearchResponse = {
  results: [
    {
      messageId: '1',
      content: '这是一条测试消息',
      messageType: MessageType.TEXT,
      sender: {
        id: 1,
        name: '张三',
        role: 'buyer',
      },
      timestamp: new Date(Date.now() - 60000).toISOString(),
      disputeId: 1,
      matchedKeywords: ['测试'],
      highlights: [
        { text: '这是一条', isMatch: false },
        { text: '测试', isMatch: true, keyword: '测试' },
        { text: '消息', isMatch: false },
      ],
      score: 0.9,
      isOwn: false,
    },
    {
      messageId: '2',
      content: '我自己的消息内容',
      messageType: MessageType.TEXT,
      sender: {
        id: 2,
        name: '我',
        role: 'seller',
      },
      timestamp: new Date(Date.now() - 120000).toISOString(),
      disputeId: 1,
      matchedKeywords: ['消息'],
      highlights: [
        { text: '我自己的', isMatch: false },
        { text: '消息', isMatch: true, keyword: '消息' },
        { text: '内容', isMatch: false },
      ],
      score: 0.8,
      isOwn: true,
    },
  ],
  pagination: {
    page: 0,
    pageSize: 20,
    total: 2,
    totalPages: 1,
    hasNext: false,
    hasPrev: false,
  },
  statistics: {
    totalResults: 2,
    searchTime: 150,
    matchedKeywords: ['测试', '消息'],
  },
};

describe('SearchResults', () => {
  const mockOnJumpToMessage = jest.fn();
  const currentUserId = 2;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders search results correctly', () => {
    render(
      <SearchResults
        searchResponse={mockSearchResponse}
        keyword="测试"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
      />
    );

    expect(screen.getByText('找到 2 条相关消息')).toBeInTheDocument();
    expect(screen.getByText('这是一条测试消息')).toBeInTheDocument();
    expect(screen.getByText('我自己的消息内容')).toBeInTheDocument();
    expect(screen.getByText('张三')).toBeInTheDocument();
    expect(screen.getByText('我')).toBeInTheDocument();
  });

  it('highlights matched keywords', () => {
    render(
      <SearchResults
        searchResponse={mockSearchResponse}
        keyword="测试"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
      />
    );

    const highlightedText = screen.getByText('测试');
    expect(highlightedText).toHaveClass('bg-yellow-200', 'text-yellow-900', 'font-medium');
  });

  it('shows loading state correctly', () => {
    render(
      <SearchResults
        searchResponse={null}
        loading={true}
        keyword="测试"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
      />
    );

    expect(screen.getByText('搜索中...')).toBeInTheDocument();
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('shows error state correctly', () => {
    render(
      <SearchResults
        searchResponse={null}
        loading={false}
        error="网络错误"
        keyword="测试"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
      />
    );

    expect(screen.getByText('搜索出错')).toBeInTheDocument();
    expect(screen.getByText('网络错误')).toBeInTheDocument();
  });

  it('shows empty state when no results', () => {
    render(
      <SearchResults
        searchResponse={{
          results: [],
          pagination: {
            page: 0,
            pageSize: 20,
            total: 0,
            totalPages: 0,
            hasNext: false,
            hasPrev: false,
          },
          statistics: {
            totalResults: 0,
            searchTime: 100,
            matchedKeywords: [],
          },
        }}
        keyword="不存在的关键词"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
      />
    );

    expect(screen.getByText('未找到相关消息')).toBeInTheDocument();
    expect(screen.getByText('尝试使用其他关键词或调整筛选条件')).toBeInTheDocument();
  });

  it('calls onJumpToMessage when result item is clicked', async () => {
    render(
      <SearchResults
        searchResponse={mockSearchResponse}
        keyword="测试"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
      />
    );

    const resultItem = screen.getByText('这是一条测试消息').closest('.cursor-pointer');
    fireEvent.click(resultItem!);

    await waitFor(() => {
      expect(mockOnJumpToMessage).toHaveBeenCalledWith('1', mockSearchResponse.results[0].timestamp);
    });
  });

  it('displays message type icons correctly', () => {
    const responseWithImage: SearchResponse = {
      ...mockSearchResponse,
      results: [
        {
          ...mockSearchResponse.results[0],
          messageType: MessageType.IMAGE,
        },
      ],
    };

    render(
      <SearchResults
        searchResponse={responseWithImage}
        keyword="测试"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
      />
    );

    const imageIcon = screen.getByText('🖼️');
    expect(imageIcon).toBeInTheDocument();
  });

  it('shows different styles for own and other messages', () => {
    render(
      <SearchResults
        searchResponse={mockSearchResponse}
        keyword="测试"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
      />
    );

    const otherMessageItem = screen.getByText('这是一条测试消息').closest('.bg-white');
    const ownMessageItem = screen.getByText('我自己的消息内容').closest('.bg-blue-50');

    expect(otherMessageItem).toBeInTheDocument();
    expect(ownMessageItem).toBeInTheDocument();
  });

  it('displays sender role badges correctly', () => {
    render(
      <SearchResults
        searchResponse={mockSearchResponse}
        keyword="测试"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
      />
    );

    const buyerBadge = screen.getByText('张三').closest('.bg-blue-100');
    expect(buyerBadge).toBeInTheDocument();
    expect(buyerBadge).toHaveClass('text-blue-800');
  });

  it('shows pagination information when multiple pages exist', () => {
    const multiPageResponse: SearchResponse = {
      ...mockSearchResponse,
      pagination: {
        page: 1,
        pageSize: 20,
        total: 50,
        totalPages: 3,
        hasNext: true,
        hasPrev: true,
      },
    };

    render(
      <SearchResults
        searchResponse={multiPageResponse}
        keyword="测试"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
      />
    );

    expect(screen.getByText('第 2 页，共 3 页')).toBeInTheDocument();
    expect(screen.getByText('共 50 条结果')).toBeInTheDocument();
  });

  it('displays matched keywords correctly', () => {
    render(
      <SearchResults
        searchResponse={mockSearchResponse}
        keyword="测试"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
      />
    );

    expect(screen.getByText('测试')).toBeInTheDocument();
    expect(screen.getByText('消息')).toBeInTheDocument();

    const testKeywordBadge = screen.getByText('测试').closest('.bg-green-100');
    const messageKeywordBadge = screen.getByText('消息').closest('.bg-green-100');

    expect(testKeywordBadge).toHaveClass('text-green-800');
    expect(messageKeywordBadge).toHaveClass('text-green-800');
  });

  it('shows search statistics correctly', () => {
    render(
      <SearchResults
        searchResponse={mockSearchResponse}
        keyword="测试"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
        showStatistics={true}
      />
    );

    expect(screen.getByText('找到 2 条相关消息')).toBeInTheDocument();
    expect(screen.getByText('耗时 150ms')).toBeInTheDocument();
    expect(screen.getByText('匹配关键词:')).toBeInTheDocument();
  });

  it('hides statistics when showStatistics is false', () => {
    render(
      <SearchResults
        searchResponse={mockSearchResponse}
        keyword="测试"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
        showStatistics={false}
      />
    );

    expect(screen.queryByText('找到 2 条相关消息')).not.toBeInTheDocument();
    expect(screen.queryByText('耗时 150ms')).not.toBeInTheDocument();
  });

  it('displays relevance scores', () => {
    render(
      <SearchResults
        searchResponse={mockSearchResponse}
        keyword="测试"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
      />
    );

    const scores = screen.getAllByText(/\d+%/);
    expect(scores).toHaveLength(2);
    expect(scores[0]).toHaveTextContent('90%'); // 0.9 * 100
    expect(scores[1]).toHaveTextContent('80%'); // 0.8 * 100
  });

  it('applies custom className correctly', () => {
    render(
      <SearchResults
        searchResponse={mockSearchResponse}
        keyword="测试"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
        className="custom-results-container"
      />
    );

    const container = document.querySelector('.custom-results-container');
    expect(container).toBeInTheDocument();
  });

  it('handles long message content truncation', () => {
    const longMessageResponse: SearchResponse = {
      ...mockSearchResponse,
      results: [
        {
          ...mockSearchResponse.results[0],
          content: '这是一条非常长的消息内容，应该被截断显示以避免界面布局问题',
        },
      ],
    };

    render(
      <SearchResults
        searchResponse={longMessageResponse}
        keyword="长"
        currentUserId={currentUserId}
        onJumpToMessage={mockOnJumpToMessage}
      />
    );

    const messageContent = screen.getByText(/这是一条非常长的消息内容/);
    expect(messageContent).toBeInTheDocument();
    expect(messageContent).toHaveClass('break-words');
  });
});