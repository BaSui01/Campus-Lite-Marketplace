/**
 * 搜索结果页面 - 快速找到你想要的！🔍
 * @author BaSui 😎
 * @description 支持商品搜索、用户搜索、筛选排序 + 新手引导
 */

import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Input, Button, Skeleton, Tabs } from '@campus/shared/components';
import { goodsService } from '@campus/shared/services';;
import { highlightText } from '@campus/shared/utils/highlight';
import { useNotificationStore } from '../../store';
import { SearchGuide } from '../../components/SearchGuide';
import { useLocalStorage } from '@campus/shared/hooks';
import type { GoodsResponse, CategoryNodeResponse, TagResponse } from '@campus/shared/api/models';
import './Search.css';

// ==================== 类型定义 ====================

type SearchType = 'goods' | 'users' | 'posts';
type SortType = 'relevance' | 'newest' | 'price_asc' | 'price_desc' | 'popular';

interface UserResult {
  userId: string;
  username: string;
  avatar?: string;
  bio?: string;
  followersCount: number;
}

interface PostResult {
  postId: string;
  authorName: string;
  content: string;
  likeCount: number;
  createdAt: string;
}

/**
 * 搜索结果页面组件
 */
const Search: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [searchType, setSearchType] = useState<SearchType>('goods');
  const [keyword, setKeyword] = useState(searchParams.get('q') || '');
  const [sortType, setSortType] = useState<SortType>('relevance');
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | undefined>();
  const [selectedTags, setSelectedTags] = useState<number[]>([]);
  const [minPrice, setMinPrice] = useState<number | undefined>();
  const [maxPrice, setMaxPrice] = useState<number | undefined>();
  const [minPriceInput, setMinPriceInput] = useState<string>('');
  const [maxPriceInput, setMaxPriceInput] = useState<string>('');
  const [categoryTree, setCategoryTree] = useState<CategoryNodeResponse[]>([]);
  const [hotTags, setHotTags] = useState<TagResponse[]>([]);

  // 🌟 搜索历史和热门搜索
  const [searchHistory, setSearchHistory] = useState<string[]>([]);
  const [hotSearches] = useState<string[]>(['二手电脑', '自行车', '教材', '吉他', '台灯']);
  const [showHistory, setShowHistory] = useState(false);

  // 🎯 搜索引导状态
  const { getValue: getGuideCompleted, setValue: setGuideCompleted } = useLocalStorage('search-guide-completed', false);
  const [showSearchGuide, setShowSearchGuide] = useState(false);

  // 商品搜索结果
  const [goodsResults, setGoodsResults] = useState<GoodsResponse[]>([]);
  const [goodsLoading, setGoodsLoading] = useState(false);
  const [goodsTotal, setGoodsTotal] = useState(0);

  // 用户搜索结果
  const [userResults, setUserResults] = useState<UserResult[]>([]);
  const [userLoading, setUserLoading] = useState(false);
  const [userTotal, setUserTotal] = useState(0);

  // 帖子搜索结果
  const [postResults, setPostResults] = useState<PostResult[]>([]);
  const [postLoading, setPostLoading] = useState(false);
  const [postTotal, setPostTotal] = useState(0);

  // 分页
  const [page, setPage] = useState(1);
  const [pageSize] = useState(20);

  // ==================== 数据加载 ====================

  /**
   * 检查是否需要显示搜索引导
   */
  useEffect(() => {
    const shouldShowGuide = !getGuideCompleted() &&
                           !searchParams.get('q') &&
                           !keyword.trim() &&
                           searchHistory.length === 0;

    if (shouldShowGuide) {
      // 延迟显示引导，给用户时间看到页面
      const timer = setTimeout(() => {
        setShowSearchGuide(true);
      }, 1000);

      return () => clearTimeout(timer);
    }
  }, [getGuideCompleted, searchParams, keyword, searchHistory]);

  /**
   * 搜索商品
   */
  const searchGoods = async () => {
    if (!keyword.trim()) {
      setGoodsResults([]);
      setGoodsTotal(0);
      return;
    }

    setGoodsLoading(true);

    try {
      // 🚀 调用真实后端 API 搜索商品
      const response = await goodsService.listGoods({
        keyword: keyword.trim(),
        categoryId: selectedCategoryId,
        tags: selectedTags.length > 0 ? selectedTags : undefined,
        minPrice,
        maxPrice,
        page,
        size: pageSize,
        sortBy: sortType === 'newest' ? 'createdAt' : sortType === 'price_asc' ? 'price' : sortType === 'price_desc' ? 'price' : undefined,
        sortDirection: sortType === 'price_asc' ? 'asc' : 'desc',
      });

      setGoodsResults(response.content || []);
      setGoodsTotal(response.totalElements || 0);
    } catch (err: any) {
      console.error('搜索商品失败：', err);
      toast.error(err.response?.data?.message || '搜索失败！😭');
      setGoodsResults([]);
      setGoodsTotal(0);
    } finally {
      setGoodsLoading(false);
    }
  };

  /**
   * 搜索用户
   */
  const searchUsers = async () => {
    if (!keyword.trim()) {
      setUserResults([]);
      setUserTotal(0);
      return;
    }

    setUserLoading(true);

    try {
      // 🚀 调用真实后端 API 搜索用户
      // TODO: 集成真实 API
      // const response = await userService.searchUsers({ keyword, page, size: pageSize });
      // setUserResults(response.data.content);
      // setUserTotal(response.data.totalElements);

      // 临时模拟数据
      const mockUsers: UserResult[] = [
        {
          userId: '101',
          username: '张三',
          bio: '爱好摄影的大三学生 📷',
          followersCount: 256,
        },
        {
          userId: '102',
          username: '李四',
          bio: '篮球爱好者 🏀',
          followersCount: 128,
        },
      ];

      setUserResults(mockUsers);
      setUserTotal(mockUsers.length);
    } catch (err: any) {
      console.error('搜索用户失败：', err);
      toast.error(err.response?.data?.message || '搜索失败！😭');
      setUserResults([]);
      setUserTotal(0);
    } finally {
      setUserLoading(false);
    }
  };

  /**
   * 搜索帖子
   */
  const searchPosts = async () => {
    if (!keyword.trim()) {
      setPostResults([]);
      setPostTotal(0);
      return;
    }

    setPostLoading(true);

    try {
      // 🚀 调用真实后端 API 搜索帖子
      // TODO: 集成真实 API
      // const response = await communityService.searchPosts({ keyword, page, size: pageSize });
      // setPostResults(response.data.content);
      // setPostTotal(response.data.totalElements);

      // 临时模拟数据
      const mockPosts: PostResult[] = [
        {
          postId: '1',
          authorName: '王五',
          content: '今天在图书馆发现了一本好书，推荐给大家！📚',
          likeCount: 42,
          createdAt: new Date().toISOString(),
        },
      ];

      setPostResults(mockPosts);
      setPostTotal(mockPosts.length);
    } catch (err: any) {
      console.error('搜索帖子失败：', err);
      toast.error(err.response?.data?.message || '搜索失败！😭');
      setPostResults([]);
      setPostTotal(0);
    } finally {
      setPostLoading(false);
    }
  };

  /**
   * 执行搜索
   */
  const performSearch = () => {
    if (!keyword.trim()) {
      toast.warning('请输入搜索关键词！😰');
      return;
    }

    // 更新 URL
    setSearchParams({ q: keyword, type: searchType });

    // 根据类型执行搜索
    if (searchType === 'goods') {
      searchGoods();
    } else if (searchType === 'users') {
      searchUsers();
    } else if (searchType === 'posts') {
      searchPosts();
    }
  };

  /**
   * 📂 加载分类树
   */
  const loadCategoryTree = async () => {
    try {
      console.log('[Search] 📂 加载分类树');
      const response = await goodsService.getCategoryTree();
      console.log('[Search] ✅ 分类树加载成功:', response);
      setCategoryTree(response);
    } catch (error) {
      console.error('[Search] ❌ 加载分类树失败:', error);
    }
  };

  /**
   * 🏷️ 加载热门标签
   */
  const loadHotTags = async () => {
    try {
      console.log('[Search] 🏷️ 加载热门标签');
      const response = await goodsService.getHotTags(20);
      console.log('[Search] ✅ 热门标签加载成功:', response);
      setHotTags(response);
    } catch (error) {
      console.error('[Search] ❌ 加载热门标签失败:', error);
    }
  };

  // 🌟 加载搜索历史 (localStorage 持久化)
  useEffect(() => {
    const savedHistory = localStorage.getItem('campus_search_history');
    if (savedHistory) {
      try {
        const parsed = JSON.parse(savedHistory);
        setSearchHistory(Array.isArray(parsed) ? parsed : []);
      } catch (err) {
        console.error('解析搜索历史失败:', err);
      }
    }

    // 📂 初始加载分类树
    loadCategoryTree();
    loadHotTags();
  }, []);

  // 🌟 点击外部关闭搜索建议
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      const searchHeader = document.querySelector('.search-header');

      if (searchHeader && !searchHeader.contains(target)) {
        setShowHistory(false);
      }
    };

    if (showHistory) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showHistory]);

  useEffect(() => {
    const urlKeyword = searchParams.get('q');
    const urlType = searchParams.get('type') as SearchType;

    if (urlKeyword) {
      setKeyword(urlKeyword);
      if (urlType && ['goods', 'users', 'posts'].includes(urlType)) {
        setSearchType(urlType);
      }
      performSearch();
    }
  }, []);

  useEffect(() => {
    if (keyword.trim()) {
      performSearch();
    }
  }, [searchType, sortType, page, selectedCategoryId, selectedTags, minPrice, maxPrice]);

  // ==================== 事件处理 ====================

  /**
   * 保存搜索到历史记录（去重 + 限制10条）
   */
  const saveToHistory = (searchKeyword: string) => {
    if (!searchKeyword.trim()) return;

    const trimmed = searchKeyword.trim();
    const updatedHistory = [trimmed, ...searchHistory.filter((k) => k !== trimmed)].slice(0, 10);

    setSearchHistory(updatedHistory);
    localStorage.setItem('campus_search_history', JSON.stringify(updatedHistory));
  };

  /**
   * 清空搜索历史
   */
  const handleClearHistory = () => {
    setSearchHistory([]);
    localStorage.removeItem('campus_search_history');
    toast.success('搜索历史已清空！🗑️');
  };

  /**
   * 点击历史记录或热门搜索
   */
  const handleClickHistoryOrHot = (text: string) => {
    setKeyword(text);
    setShowHistory(false);
    setPage(1);
    setSearchParams({ q: text, type: searchType });

    // 触发搜索
    if (searchType === 'goods') {
      searchGoods();
    } else if (searchType === 'users') {
      searchUsers();
    } else if (searchType === 'posts') {
      searchPosts();
    }

    // 保存到历史
    saveToHistory(text);
  };

  /**
   * 处理搜索输入
   */
  const handleSearch = () => {
    setPage(1);
    setShowHistory(false);
    saveToHistory(keyword); // 🌟 保存搜索历史
    performSearch();
  };

  /**
   * 按下回车搜索
   */
  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  /**
   * 切换搜索类型
   */
  const handleTypeChange = (value: string) => {
    setSearchType(value as SearchType);
    setPage(1);
  };

  /**
   * 切换排序方式
   */
  const handleSortChange = (type: SortType) => {
    setSortType(type);
    setPage(1);
  };

  /**
   * 📂 处理分类选择
   */
  const handleCategorySelect = (categoryId?: number) => {
    console.log('[Search] 📂 选择分类:', categoryId);
    setSelectedCategoryId(categoryId);
    setPage(1);
  };

  /**
   * 🏷️ 处理标签点击（多选切换）
   */
  const handleTagToggle = (tagId: number) => {
    setSelectedTags(prev => {
      if (prev.includes(tagId)) {
        // 已选中 → 取消选中
        console.log('[Search] 🏷️ 取消标签:', tagId);
        return prev.filter(id => id !== tagId);
      } else {
        // 未选中 → 选中
        console.log('[Search] 🏷️ 选中标签:', tagId);
        return [...prev, tagId];
      }
    });
    setPage(1);
  };

  /**
   * 💰 应用价格筛选
   */
  const handleApplyPriceFilter = () => {
    const min = minPriceInput.trim() ? parseFloat(minPriceInput) : undefined;
    const max = maxPriceInput.trim() ? parseFloat(maxPriceInput) : undefined;

    // 验证价格输入
    if (min !== undefined && (isNaN(min) || min < 0)) {
      console.warn('[Search] 💰 最低价格无效:', minPriceInput);
      return;
    }
    if (max !== undefined && (isNaN(max) || max < 0)) {
      console.warn('[Search] 💰 最高价格无效:', maxPriceInput);
      return;
    }
    if (min !== undefined && max !== undefined && min > max) {
      console.warn('[Search] 💰 最低价格不能大于最高价格');
      return;
    }

    console.log('[Search] 💰 应用价格筛选:', { min, max });
    setMinPrice(min);
    setMaxPrice(max);
    setPage(1);
  };

  /**
   * 💰 清除价格筛选
   */
  const handleClearPriceFilter = () => {
    console.log('[Search] 💰 清除价格筛选');
    setMinPriceInput('');
    setMaxPriceInput('');
    setMinPrice(undefined);
    setMaxPrice(undefined);
    setPage(1);
  };

  /**
   * 跳转到商品详情
   */
  const handleGoToGoods = (goodsId: string) => {
    navigate(`/goods/${goodsId}`);
  };

  /**
   * 跳转到用户主页
   */
  const handleGoToUser = (userId: string) => {
    navigate(`/profile?id=${userId}`);
  };

  /**
   * 🎯 处理搜索引导完成
   */
  const handleGuideComplete = () => {
    setGuideCompleted(true);
    setShowSearchGuide(false);
    toast.success('搜索引导完成！现在试试搜索功能吧！🎉');
  };

  /**
   * 🎯 手动显示搜索引导
   */
  const handleShowGuide = () => {
    setShowSearchGuide(true);
  };

  /**
   * 格式化价格
   */
  const formatPrice = (price?: number) => {
    if (!price) return '¥0.00';
    return `¥${(price / 100).toFixed(2)}`;
  };

  // ==================== 渲染 ====================

  const isLoading = goodsLoading || userLoading || postLoading;
  const currentTotal = searchType === 'goods' ? goodsTotal : searchType === 'users' ? userTotal : postTotal;

  return (
    <div className="search-page">
      <div className="search-container">
        {/* ==================== 搜索引导帮助按钮 ==================== */}
        <div className="search-help-btn">
          <Button
            type="text"
            size="small"
            onClick={handleShowGuide}
            className="help-guide-btn"
            title="搜索功能帮助"
          >
            🧭 搜索帮助
          </Button>
        </div>

        {/* ==================== 搜索栏 ==================== */}
        <div className="search-header">
          <div className="search-header__input">
            <Input
              type="text"
              size="large"
              placeholder="搜索商品、用户、帖子..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyPress={handleKeyPress}
              onFocus={() => setShowHistory(true)}
              prefix={<span>🔍</span>}
              allowClear
            />
            <Button type="primary" size="large" onClick={handleSearch} loading={isLoading}>
              搜索
            </Button>
          </div>

          {/* 🌟 搜索历史和热门搜索 */}
          {showHistory && !keyword && (
            <div className="search-suggestions">
              {/* 搜索历史 */}
              {searchHistory.length > 0 && (
                <div className="search-history">
                  <div className="search-suggestions__header">
                    <span className="search-suggestions__title">🕒 搜索历史</span>
                    <button className="search-suggestions__clear" onClick={handleClearHistory}>
                      清空
                    </button>
                  </div>
                  <div className="search-suggestions__list">
                    {searchHistory.map((item, index) => (
                      <div
                        key={index}
                        className="search-suggestion-item"
                        onClick={() => handleClickHistoryOrHot(item)}
                      >
                        <span className="suggestion-icon">🔍</span>
                        <span className="suggestion-text">{item}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* 热门搜索 */}
              <div className="search-hot">
                <div className="search-suggestions__header">
                  <span className="search-suggestions__title">🔥 热门搜索</span>
                </div>
                <div className="search-suggestions__list">
                  {hotSearches.map((item, index) => (
                    <div
                      key={index}
                      className="search-suggestion-item hot"
                      onClick={() => handleClickHistoryOrHot(item)}
                    >
                      <span className="suggestion-rank">{index + 1}</span>
                      <span className="suggestion-text">{item}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* ==================== 搜索类型切换 ==================== */}
        <div className="search-tabs">
          <Tabs
            defaultValue="goods"
            value={searchType}
            onChange={handleTypeChange}
            tabs={[
              { label: `📦 商品 ${searchType === 'goods' && goodsTotal > 0 ? `(${goodsTotal})` : ''}`, value: 'goods' },
              { label: `👥 用户 ${searchType === 'users' && userTotal > 0 ? `(${userTotal})` : ''}`, value: 'users' },
              { label: `📝 帖子 ${searchType === 'posts' && postTotal > 0 ? `(${postTotal})` : ''}`, value: 'posts' },
            ]}
          />
        </div>

        {/* ==================== 排序栏（仅商品） ==================== */}
        {searchType === 'goods' && (
          <div className="search-sort">
            {/* 分类筛选 */}
            <div className="search-sort__category">
              <select
                className="category-select"
                value={selectedCategoryId || ''}
                onChange={(e) => handleCategorySelect(e.target.value ? Number(e.target.value) : undefined)}
              >
                <option value="">📂 全部分类</option>
                {categoryTree.map((category) => (
                  <React.Fragment key={category.id}>
                    <option value={category.id}>{category.name}</option>
                    {category.children?.map((child) => (
                      <option key={child.id} value={child.id}>
                        &nbsp;&nbsp;└─ {child.name}
                      </option>
                    ))}
                  </React.Fragment>
                ))}
              </select>
            </div>

            {/* 价格筛选 */}
            <div className="search-sort__price">
              <input
                type="number"
                className="price-input"
                placeholder="最低价"
                value={minPriceInput}
                onChange={(e) => setMinPriceInput(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleApplyPriceFilter()}
                min="0"
                step="0.01"
              />
              <span className="price-separator">-</span>
              <input
                type="number"
                className="price-input"
                placeholder="最高价"
                value={maxPriceInput}
                onChange={(e) => setMaxPriceInput(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleApplyPriceFilter()}
                min="0"
                step="0.01"
              />
              <button className="price-apply-btn" onClick={handleApplyPriceFilter}>
                应用
              </button>
              {(minPrice !== undefined || maxPrice !== undefined) && (
                <button className="price-clear-btn" onClick={handleClearPriceFilter}>
                  清除
                </button>
              )}
            </div>

            {/* 排序选项 */}
            <div className="search-sort__label">排序：</div>
            <div className="search-sort__options">
              {[
                { label: '综合', value: 'relevance' },
                { label: '最新', value: 'newest' },
                { label: '价格从低到高', value: 'price_asc' },
                { label: '价格从高到低', value: 'price_desc' },
              ].map((option) => (
                <button
                  key={option.value}
                  className={`search-sort__option ${sortType === option.value ? 'active' : ''}`}
                  onClick={() => handleSortChange(option.value as SortType)}
                >
                  {option.label}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* ==================== 标签筛选栏（仅商品） ==================== */}
        {searchType === 'goods' && hotTags.length > 0 && (
          <div className="search-tags">
            <div className="search-tags__label">🏷️ 热门标签：</div>
            <div className="search-tags__list">
              {hotTags.map(tag => (
                <button
                  key={tag.id}
                  className={`search-tags__item ${selectedTags.includes(tag.id!) ? 'active' : ''}`}
                  onClick={() => handleTagToggle(tag.id!)}
                >
                  #{tag.name}
                </button>
              ))}
            </div>
            {selectedTags.length > 0 && (
              <button
                className="search-tags__clear"
                onClick={() => setSelectedTags([])}
              >
                清除筛选
              </button>
            )}
          </div>
        )}

        {/* ==================== 搜索结果 ==================== */}
        <div className="search-results">
          {/* 加载中 */}
          {isLoading && (
            <div className="search-loading">
              <Skeleton type="card" count={4} animation="wave" />
            </div>
          )}

          {/* 空状态 */}
          {!isLoading && !keyword.trim() && (
            <div className="search-empty">
              <div className="empty-icon">🔍</div>
              <p className="empty-text">输入关键词开始搜索</p>
              <p className="empty-tip">试试搜索"自行车"、"书籍"等关键词</p>
              {!getGuideCompleted() && (
                <Button
                  type="primary"
                  onClick={handleShowGuide}
                  className="guide-cta-btn"
                >
                  🧭 查看搜索引导
                </Button>
              )}
            </div>
          )}

          {/* 无结果 */}
          {!isLoading && keyword.trim() && currentTotal === 0 && (
            <div className="search-empty">
              <div className="empty-icon">😭</div>
              <p className="empty-text">没有找到相关结果</p>
              <p className="empty-tip">换个关键词试试吧</p>
            </div>
          )}

          {/* 商品结果 */}
          {!isLoading && searchType === 'goods' && goodsResults.length > 0 && (
            <div className="search-goods-list">
              {goodsResults.map((goods) => (
                <div
                  key={goods.id}
                  className="goods-result-card"
                  onClick={() => handleGoToGoods(goods.id!)}
                >
                  <div className="goods-result-card__image">
                    {goods.images?.[0] ? (
                      <img src={goods.images[0]} alt={goods.title} />
                    ) : (
                      <div className="image-placeholder">📦</div>
                    )}
                  </div>
                  <div className="goods-result-card__info">
                    <h3
                      className="goods-result-card__title"
                      dangerouslySetInnerHTML={{
                        __html: highlightText(goods.title || '', keyword),
                      }}
                    />
                    <p
                      className="goods-result-card__desc"
                      dangerouslySetInnerHTML={{
                        __html: highlightText(goods.description || '', keyword),
                      }}
                    />
                    <div className="goods-result-card__footer">
                      <div className="goods-result-card__price">{formatPrice(goods.price)}</div>
                      <div className="goods-result-card__seller">
                        👤 {goods.sellerInfo?.username || '未知卖家'}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* 用户结果 */}
          {!isLoading && searchType === 'users' && userResults.length > 0 && (
            <div className="search-user-list">
              {userResults.map((user) => (
                <div
                  key={user.userId}
                  className="user-result-card"
                  onClick={() => handleGoToUser(user.userId)}
                >
                  <div className="user-result-card__avatar">
                    {user.avatar ? <img src={user.avatar} alt={user.username} /> : <span>👤</span>}
                  </div>
                  <div className="user-result-card__info">
                    <h3 className="user-result-card__name">{user.username}</h3>
                    <p className="user-result-card__bio">{user.bio || '这个人很懒，什么都没写'}</p>
                    <div className="user-result-card__stats">
                      <span>👥 {user.followersCount} 粉丝</span>
                    </div>
                  </div>
                  <Button type="primary" size="small">
                    关注
                  </Button>
                </div>
              ))}
            </div>
          )}

          {/* 帖子结果 */}
          {!isLoading && searchType === 'posts' && postResults.length > 0 && (
            <div className="search-post-list">
              {postResults.map((post) => (
                <div key={post.postId} className="post-result-card">
                  <div className="post-result-card__header">
                    <span className="post-result-card__author">👤 {post.authorName}</span>
                  </div>
                  <div className="post-result-card__content">{post.content}</div>
                  <div className="post-result-card__footer">
                    <span>❤️ {post.likeCount}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* ==================== 搜索引导组件 ==================== */}
      {showSearchGuide && (
        <SearchGuide
          visible={showSearchGuide}
          onComplete={handleGuideComplete}
        />
      )}
    </div>
  );
};

export default Search;