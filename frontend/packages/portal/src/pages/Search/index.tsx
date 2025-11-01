/**
 * 搜索结果页面 - 快速找到你想要的！🔍
 * @author BaSui 😎
 * @description 支持商品搜索、用户搜索、筛选排序
 */

import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Input, Button, Skeleton, Tabs } from '@campus/shared/components';
import { goodsService } from '@campus/shared/services';
import { useNotificationStore } from '../../store';
import type { GoodsResponse } from '@campus/shared/api/models';
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
      const response = await goodsService.searchGoods({
        keyword: keyword.trim(),
        page,
        size: pageSize,
        sort: sortType === 'newest' ? 'createdAt,desc' : sortType === 'price_asc' ? 'price,asc' : sortType === 'price_desc' ? 'price,desc' : undefined,
      });

      setGoodsResults(response.data?.content || []);
      setGoodsTotal(response.data?.totalElements || 0);
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
  }, [searchType, sortType, page]);

  // ==================== 事件处理 ====================

  /**
   * 处理搜索输入
   */
  const handleSearch = () => {
    setPage(1);
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
              prefix={<span>🔍</span>}
              allowClear
            />
            <Button type="primary" size="large" onClick={handleSearch} loading={isLoading}>
              搜索
            </Button>
          </div>
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
                    <h3 className="goods-result-card__title">{goods.title}</h3>
                    <p className="goods-result-card__desc">{goods.description}</p>
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
    </div>
  );
};

export default Search;
