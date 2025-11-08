/**
 * SearchBar 组件测试
 * @author BaSui 😎
 * @description 测试搜索栏组件的功能
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import SearchBar from './index';
import type { SearchFilters, SearchState, SearchHistory, SearchSuggestion, QuickSearch } from '@campus/shared/types/search';

// Mock 搜索状态
const mockSearchState: SearchState = {
  searching: false,
  currentKeyword: '',
  currentFilters: {
    keyword: '',
    messageTypes: [],
    senders: [],
    dateRange: null,
    ownMessagesOnly: false,
    includeRecalled: false,
  },
  results: [],
  error: null,
  suggestions: [],
  showAdvancedFilters: false,
};

// Mock 搜索历史
const mockSearchHistory: SearchHistory[] = [
  {
    id: '1',
    keyword: '测试关键词',
    searchedAt: new Date(Date.now() - 60000).toISOString(),
    resultCount: 5,
    filters: {},
  },
  {
    id: '2',
    keyword: '另一个搜索',
    searchedAt: new Date(Date.now() - 120000).toISOString(),
    resultCount: 3,
    filters: {},
  },
];

// Mock 搜索建议
const mockSuggestions: SearchSuggestion[] = [
  {
    text: '测试建议',
    type: 'keyword',
    description: '搜索建议',
    icon: '💡',
  },
  {
    text: '张三',
    type: 'user',
    description: '买家',
    icon: '👤',
  },
];

describe('SearchBar', () => {
  const mockOnSearch = jest.fn();
  const mockOnClear = jest.fn();
  const mockOnQuickSearch = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders search input correctly', () => {
    render(
      <SearchBar
        searchState={mockSearchState}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
      />
    );

    const searchInput = screen.getByPlaceholderText('搜索聊天记录...');
    expect(searchInput).toBeInTheDocument();
    expect(searchInput).toHaveValue('');
  });

  it('displays current keyword from search state', () => {
    const searchStateWithKeyword = {
      ...mockSearchState,
      currentKeyword: '当前搜索',
    };

    render(
      <SearchBar
        searchState={searchStateWithKeyword}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
      />
    );

    const searchInput = screen.getByDisplayValue('当前搜索');
    expect(searchInput).toBeInTheDocument();
  });

  it('calls onSearch when Enter key is pressed', async () => {
    render(
      <SearchBar
        searchState={mockSearchState}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
      />
    );

    const searchInput = screen.getByPlaceholderText('搜索聊天记录...');
    fireEvent.change(searchInput, { target: { value: '测试搜索' } });
    fireEvent.keyDown(searchInput, { key: 'Enter' });

    await waitFor(() => {
      expect(mockOnSearch).toHaveBeenCalledWith('测试搜索', expect.any(Object));
    });
  });

  it('calls onClear when clear button is clicked', () => {
    const searchStateWithValue = {
      ...mockSearchState,
      currentKeyword: '测试搜索',
    };

    render(
      <SearchBar
        searchState={searchStateWithValue}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
      />
    );

    const clearButton = screen.getByTitle('清除搜索');
    fireEvent.click(clearButton);

    expect(mockOnClear).toHaveBeenCalledTimes(1);
  });

  it('shows search history when input is focused and empty', async () => {
    render(
      <SearchBar
        searchState={mockSearchState}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
      />
    );

    const searchInput = screen.getByPlaceholderText('搜索聊天记录...');
    fireEvent.focus(searchInput);

    await waitFor(() => {
      expect(screen.getByText('搜索历史')).toBeInTheDocument();
      expect(screen.getByText('测试关键词')).toBeInTheDocument();
      expect(screen.getByText('另一个搜索')).toBeInTheDocument();
    });
  });

  it('shows search suggestions when typing', async () => {
    render(
      <SearchBar
        searchState={mockSearchState}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
      />
    );

    const searchInput = screen.getByPlaceholderText('搜索聊天记录...');
    fireEvent.change(searchInput, { target: { value: '测试' } });
    fireEvent.focus(searchInput);

    await waitFor(() => {
      expect(screen.getByText('测试建议')).toBeInTheDocument();
      expect(screen.getByText('张三')).toBeInTheDocument();
    });
  });

  it('calls onSearch when suggestion is clicked', async () => {
    render(
      <SearchBar
        searchState={mockSearchState}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
      />
    );

    const searchInput = screen.getByPlaceholderText('搜索聊天记录...');
    fireEvent.change(searchInput, { target: { value: '测试' } });
    fireEvent.focus(searchInput);

    await waitFor(() => {
      const suggestion = screen.getByText('测试建议');
      fireEvent.click(suggestion);
    });

    await waitFor(() => {
      expect(mockOnSearch).toHaveBeenCalledWith('测试建议', expect.any(Object));
    });
  });

  it('calls onSearch when history item is clicked', async () => {
    render(
      <SearchBar
        searchState={mockSearchState}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
      />
    );

    const searchInput = screen.getByPlaceholderText('搜索聊天记录...');
    fireEvent.focus(searchInput);

    await waitFor(() => {
      const historyItem = screen.getByText('测试关键词');
      fireEvent.click(historyItem);
    });

    await waitFor(() => {
      expect(mockOnSearch).toHaveBeenCalledWith('测试关键词', expect.any(Object));
    });
  });

  it('shows loading state when searching', () => {
    const searchingState = {
      ...mockSearchState,
      searching: true,
    };

    render(
      <SearchBar
        searchState={searchingState}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
      />
    );

    const searchIcon = document.querySelector('.animate-spin');
    expect(searchIcon).toBeInTheDocument();
  });

  it('shows error state when search fails', () => {
    const errorState = {
      ...mockSearchState,
      error: '搜索失败',
    };

    render(
      <SearchBar
        searchState={errorState}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
      />
    );

    expect(screen.getByText('搜索出错：搜索失败')).toBeInTheDocument();
  });

  it('calls onQuickSearch when quick search button is clicked', async () => {
    render(
      <SearchBar
        searchState={mockSearchState}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
        showQuickSearch={true}
      />
    );

    // 找到"我的消息"快捷搜索按钮
    const quickSearchButton = screen.getByTitle('我的消息');
    fireEvent.click(quickSearchButton);

    await waitFor(() => {
      expect(mockOnQuickSearch).toHaveBeenCalledWith(expect.objectContaining({
        name: '我的消息',
        icon: '📤',
      }));
    });
  });

  it('navigates suggestions with arrow keys', async () => {
    render(
      <SearchBar
        searchState={mockSearchState}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
      />
    );

    const searchInput = screen.getByPlaceholderText('搜索聊天记录...');
    fireEvent.change(searchInput, { target: { value: '测试' } });
    fireEvent.focus(searchInput);

    await waitFor(() => {
      expect(screen.getByText('测试建议')).toBeInTheDocument();
    });

    // 按下箭头键
    fireEvent.keyDown(searchInput, { key: 'ArrowDown' });
    fireEvent.keyDown(searchInput, { key: 'Enter' });

    await waitFor(() => {
      expect(mockOnSearch).toHaveBeenCalledWith('测试建议', expect.any(Object));
    });
  });

  it('closes suggestions when Escape key is pressed', async () => {
    render(
      <SearchBar
        searchState={mockSearchState}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
      />
    );

    const searchInput = screen.getByPlaceholderText('搜索聊天记录...');
    fireEvent.change(searchInput, { target: { value: '测试' } });
    fireEvent.focus(searchInput);

    await waitFor(() => {
      expect(screen.getByText('测试建议')).toBeInTheDocument();
    });

    fireEvent.keyDown(searchInput, { key: 'Escape' });

    await waitFor(() => {
      expect(screen.queryByText('测试建议')).not.toBeInTheDocument();
    });
  });

  it('applies custom className', () => {
    render(
      <SearchBar
        searchState={mockSearchState}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
        className="custom-search-bar"
      />
    );

    const searchBarContainer = document.querySelector('.custom-search-bar');
    expect(searchBarContainer).toBeInTheDocument();
  });

  it('is disabled when disabled prop is true', () => {
    render(
      <SearchBar
        searchState={mockSearchState}
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onQuickSearch={mockOnQuickSearch}
        searchHistory={mockSearchHistory}
        suggestions={mockSuggestions}
        disabled={true}
      />
    );

    const searchInput = screen.getByPlaceholderText('搜索已禁用');
    expect(searchInput).toBeDisabled();
    expect(searchInput).toHaveClass('bg-gray-100', 'cursor-not-allowed');
  });
});