/**
 * 表情包服务 - 聊天表情管理专家！😎
 *
 * @author BaSui 😎
 * @description 表情包的获取、管理、使用统计等功能
 * @date 2025-11-07
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type {
  EmojiPack,
  EmojiItem,
  EmojiPackQuery,
  EmojiPackListResponse,
  CreateEmojiPackRequest,
  UpdateEmojiPackRequest,
  AddEmojiRequest,
  EmojiUsage,
  EmojiStatistics,
  EmojiMessage,
  EmojiPackType,
  EmojiCategory,
} from '@campus/shared/types/emoji';

/**
 * 表情包服务类
 */
export class EmojiService {
  private api = getApi();

  // 本地缓存
  private emojiPacksCache: EmojiPack[] | null = null;
  private recentlyUsedCache: EmojiItem[] | null = null;
  private favoriteCache: EmojiItem[] | null = null;

  /**
   * 获取表情包列表 📦
   *
   * @param params 查询参数
   * @returns 表情包列表
   */
  async getEmojiPacks(params?: EmojiPackQuery): Promise<EmojiPackListResponse> {
    try {
      // 如果没有查询参数，使用缓存
      if (!params && this.emojiPacksCache) {
        return {
          packs: this.emojiPacksCache,
          total: this.emojiPacksCache.length,
          page: 0,
          size: this.emojiPacksCache.length,
          totalPages: 1,
        };
      }

      // 模拟API调用（实际需要后端接口支持）
      const mockResponse = await this.mockGetEmojiPacks(params);

      // 缓存结果
      if (!params) {
        this.emojiPacksCache = mockResponse.packs;
      }

      return mockResponse;
    } catch (error) {
      console.error('获取表情包列表失败:', error);
      throw new Error('获取表情包列表失败');
    }
  }

  /**
   * 获取单个表情包详情 🔍
   *
   * @param packId 表情包ID
   * @returns 表情包详情
   */
  async getEmojiPackById(packId: string): Promise<EmojiPack | null> {
    try {
      // 先从缓存中查找
      if (this.emojiPacksCache) {
        const pack = this.emojiPacksCache.find(p => p.id === packId);
        if (pack) return pack;
      }

      // 模拟API调用
      return await this.mockGetEmojiPackById(packId);
    } catch (error) {
      console.error('获取表情包详情失败:', error);
      return null;
    }
  }

  /**
   * 创建自定义表情包 ➕
   *
   * @param data 创建请求
   * @returns 创建的表情包
   */
  async createEmojiPack(data: CreateEmojiPackRequest): Promise<EmojiPack> {
    try {
      // 模拟API调用
      const newPack = await this.mockCreateEmojiPack(data);

      // 清除缓存
      this.emojiPacksCache = null;

      return newPack;
    } catch (error) {
      console.error('创建表情包失败:', error);
      throw new Error('创建表情包失败');
    }
  }

  /**
   * 更新表情包信息 ✏️
   *
   * @param packId 表情包ID
   * @param data 更新请求
   * @returns 更新结果
   */
  async updateEmojiPack(packId: string, data: UpdateEmojiPackRequest): Promise<boolean> {
    try {
      // 模拟API调用
      const success = await this.mockUpdateEmojiPack(packId, data);

      if (success) {
        // 清除缓存
        this.emojiPacksCache = null;
      }

      return success;
    } catch (error) {
      console.error('更新表情包失败:', error);
      return false;
    }
  }

  /**
   * 删除表情包 🗑️
   *
   * @param packId 表情包ID
   * @returns 删除结果
   */
  async deleteEmojiPack(packId: string): Promise<boolean> {
    try {
      // 模拟API调用
      const success = await this.mockDeleteEmojiPack(packId);

      if (success) {
        // 清除缓存
        this.emojiPacksCache = null;
      }

      return success;
    } catch (error) {
      console.error('删除表情包失败:', error);
      return false;
    }
  }

  /**
   * 添加表情到表情包 ➕
   *
   * @param packId 表情包ID
   * @param data 添加表情请求
   * @returns 添加的表情
   */
  async addEmojiToPack(packId: string, data: AddEmojiRequest): Promise<EmojiItem> {
    try {
      // 模拟API调用
      const newEmoji = await this.mockAddEmojiToPack(packId, data);

      // 清除缓存
      this.emojiPacksCache = null;

      return newEmoji;
    } catch (error) {
      console.error('添加表情失败:', error);
      throw new Error('添加表情失败');
    }
  }

  /**
   * 删除表情 🗑️
   *
   * @param packId 表情包ID
   * @param emojiId 表情ID
   * @returns 删除结果
   */
  async removeEmoji(packId: string, emojiId: string): Promise<boolean> {
    try {
      // 模拟API调用
      const success = await this.mockRemoveEmoji(packId, emojiId);

      if (success) {
        // 清除缓存
        this.emojiPacksCache = null;
        this.recentlyUsedCache = null;
        this.favoriteCache = null;
      }

      return success;
    } catch (error) {
      console.error('删除表情失败:', error);
      return false;
    }
  }

  /**
   * 记录表情使用 📊
   *
   * @param emojiId 表情ID
   * @param context 使用场景
   * @returns 记录结果
   */
  async recordEmojiUsage(emojiId: string, context: string = 'chat'): Promise<boolean> {
    try {
      // 模拟API调用
      const success = await this.mockRecordEmojiUsage(emojiId, context);

      if (success) {
        // 清除相关缓存
        this.recentlyUsedCache = null;
      }

      return success;
    } catch (error) {
      console.error('记录表情使用失败:', error);
      return false;
    }
  }

  /**
   * 收藏/取消收藏表情 ⭐
   *
   * @param emojiId 表情ID
   * @param isFavorite 是否收藏
   * @returns 操作结果
   */
  async toggleEmojiFavorite(emojiId: string, isFavorite: boolean): Promise<boolean> {
    try {
      // 模拟API调用
      const success = await this.mockToggleEmojiFavorite(emojiId, isFavorite);

      if (success) {
        // 清除收藏缓存
        this.favoriteCache = null;

        // 更新主缓存中的收藏状态
        if (this.emojiPacksCache) {
          this.emojiPacksCache = this.emojiPacksCache.map(pack => ({
            ...pack,
            emojis: pack.emojis.map(emoji =>
              emoji.id === emojiId
                ? { ...emoji, isFavorite }
                : emoji
            )
          }));
        }
      }

      return success;
    } catch (error) {
      console.error('更新表情收藏状态失败:', error);
      return false;
    }
  }

  /**
   * 获取最近使用的表情 ⏰
   *
   * @param limit 返回数量限制
   * @returns 最近使用的表情列表
   */
  async getRecentlyUsedEmojis(limit: number = 20): Promise<EmojiItem[]> {
    try {
      if (this.recentlyUsedCache) {
        return this.recentlyUsedCache.slice(0, limit);
      }

      // 模拟API调用
      this.recentlyUsedCache = await this.mockGetRecentlyUsedEmojis(limit);
      return this.recentlyUsedCache;
    } catch (error) {
      console.error('获取最近使用表情失败:', error);
      return [];
    }
  }

  /**
   * 获取收藏的表情 ⭐
   *
   * @param limit 返回数量限制
   * @returns 收藏的表情列表
   */
  async getFavoriteEmojis(limit: number = 50): Promise<EmojiItem[]> {
    try {
      if (this.favoriteCache) {
        return this.favoriteCache.slice(0, limit);
      }

      // 模拟API调用
      this.favoriteCache = await this.mockGetFavoriteEmojis(limit);
      return this.favoriteCache;
    } catch (error) {
      console.error('获取收藏表情失败:', error);
      return [];
    }
  }

  /**
   * 搜索表情 🔍
   *
   * @param keyword 搜索关键词
   * @param category 搜索分类
   * @returns 搜索结果
   */
  async searchEmojis(keyword: string, category?: EmojiCategory): Promise<EmojiItem[]> {
    try {
      // 从缓存中搜索
      if (this.emojiPacksCache) {
        let results: EmojiItem[] = [];

        this.emojiPacksCache.forEach(pack => {
          pack.emojis.forEach(emoji => {
            const matchesKeyword = !keyword ||
              emoji.name.toLowerCase().includes(keyword.toLowerCase());
            const matchesCategory = !category || emoji.category === category;

            if (matchesKeyword && matchesCategory) {
              results.push(emoji);
            }
          });
        });

        return results.sort((a, b) => b.useCount - a.useCount);
      }

      // 模拟API调用
      return await this.mockSearchEmojis(keyword, category);
    } catch (error) {
      console.error('搜索表情失败:', error);
      return [];
    }
  }

  /**
   * 获取表情使用统计 📊
   *
   * @param userId 用户ID
   * @returns 使用统计
   */
  async getEmojiStatistics(userId: number): Promise<EmojiStatistics | null> {
    try {
      // 模拟API调用
      return await this.mockGetEmojiStatistics(userId);
    } catch (error) {
      console.error('获取表情统计失败:', error);
      return null;
    }
  }

  /**
   * 清除本地缓存 🧹
   */
  clearCache(): void {
    this.emojiPacksCache = null;
    this.recentlyUsedCache = null;
    this.favoriteCache = null;
  }

  // ==================== 模拟方法 ====================
  // 实际开发中需要替换为真实的API调用

  private async mockGetEmojiPacks(params?: EmojiPackQuery): Promise<EmojiPackListResponse> {
    // 模拟系统内置表情包
    const systemEmojis: EmojiPack[] = [
      {
        id: 'system-smileys',
        name: '经典笑脸',
        description: '最常用的经典表情符号',
        type: 'SYSTEM' as EmojiPackType,
        status: 'ACTIVE' as any,
        coverImage: 'https://via.placeholder.com/100x100?text=😊',
        author: 'System',
        emojis: [
          { id: 'emoji-1', name: '笑脸', content: '😊', contentType: 'text', category: 'SMILEYS' as any, packId: 'system-smileys', sortOrder: 1, isFavorite: false, useCount: 150, createdAt: '2025-01-01', updatedAt: '2025-01-01' },
          { id: 'emoji-2', name: '大笑', content: '😂', contentType: 'text', category: 'SMILEYS' as any, packId: 'system-smileys', sortOrder: 2, isFavorite: true, useCount: 200, createdAt: '2025-01-01', updatedAt: '2025-01-01' },
          { id: 'emoji-3', name: '爱心', content: '❤️', contentType: 'text', category: 'SMILEYS' as any, packId: 'system-smileys', sortOrder: 3, isFavorite: false, useCount: 120, createdAt: '2025-01-01', updatedAt: '2025-01-01' },
          { id: 'emoji-4', name: '点赞', content: '👍', contentType: 'text', category: 'GESTURES' as any, packId: 'system-smileys', sortOrder: 4, isFavorite: true, useCount: 180, createdAt: '2025-01-01', updatedAt: '2025-01-01' },
          { id: 'emoji-5', name: 'OK', content: '👌', contentType: 'text', category: 'GESTURES' as any, packId: 'system-smileys', sortOrder: 5, isFavorite: false, useCount: 90, createdAt: '2025-01-01', updatedAt: '2025-01-01' },
          { id: 'emoji-6', name: '庆祝', content: '🎉', contentType: 'text', category: 'ACTIVITIES' as any, packId: 'system-smileys', sortOrder: 6, isFavorite: false, useCount: 110, createdAt: '2025-01-01', updatedAt: '2025-01-01' },
        ],
        isBuiltIn: true,
        downloadCount: 1000,
        favoriteCount: 50,
        sortOrder: 1,
        createdAt: '2025-01-01',
        updatedAt: '2025-01-01',
      },
      {
        id: 'system-animals',
        name: '可爱动物',
        description: '萌萌的小动物表情',
        type: 'SYSTEM' as EmojiPackType,
        status: 'ACTIVE' as any,
        coverImage: 'https://via.placeholder.com/100x100?text=🐱',
        author: 'System',
        emojis: [
          { id: 'emoji-7', name: '小猫', content: '🐱', contentType: 'text', category: 'ANIMALS' as any, packId: 'system-animals', sortOrder: 1, isFavorite: false, useCount: 80, createdAt: '2025-01-01', updatedAt: '2025-01-01' },
          { id: 'emoji-8', name: '小狗', content: '🐶', contentType: 'text', category: 'ANIMALS' as any, packId: 'system-animals', sortOrder: 2, isFavorite: true, useCount: 95, createdAt: '2025-01-01', updatedAt: '2025-01-01' },
          { id: 'emoji-9', name: '兔子', content: '🐰', contentType: 'text', category: 'ANIMALS' as any, packId: 'system-animals', sortOrder: 3, isFavorite: false, useCount: 70, createdAt: '2025-01-01', updatedAt: '2025-01-01' },
          { id: 'emoji-10', name: '熊猫', content: '🐼', contentType: 'text', category: 'ANIMALS' as any, packId: 'system-animals', sortOrder: 4, isFavorite: true, useCount: 85, createdAt: '2025-01-01', updatedAt: '2025-01-01' },
        ],
        isBuiltIn: true,
        downloadCount: 800,
        favoriteCount: 30,
        sortOrder: 2,
        createdAt: '2025-01-01',
        updatedAt: '2025-01-01',
      }
    ];

    return {
      packs: systemEmojis,
      total: systemEmojis.length,
      page: 0,
      size: systemEmojis.length,
      totalPages: 1,
    };
  }

  private async mockGetEmojiPackById(packId: string): Promise<EmojiPack | null> {
    const packs = await this.mockGetEmojiPacks();
    return packs.packs.find(pack => pack.id === packId) || null;
  }

  private async mockCreateEmojiPack(data: CreateEmojiPackRequest): Promise<EmojiPack> {
    // 模拟创建自定义表情包
    const newPack: EmojiPack = {
      id: `custom-${Date.now()}`,
      name: data.name,
      description: data.description,
      type: data.type,
      status: 'ACTIVE' as any,
      coverImage: data.coverImage,
      author: 'User',
      emojis: [],
      isBuiltIn: false,
      downloadCount: 0,
      favoriteCount: 0,
      sortOrder: 999,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    return newPack;
  }

  private async mockUpdateEmojiPack(packId: string, data: UpdateEmojiPackRequest): Promise<boolean> {
    console.log('更新表情包:', packId, data);
    return true;
  }

  private async mockDeleteEmojiPack(packId: string): Promise<boolean> {
    console.log('删除表情包:', packId);
    return true;
  }

  private async mockAddEmojiToPack(packId: string, data: AddEmojiRequest): Promise<EmojiItem> {
    const newEmoji: EmojiItem = {
      id: `emoji-${Date.now()}`,
      name: data.name,
      content: data.content,
      contentType: data.contentType,
      category: data.category,
      packId,
      sortOrder: data.sortOrder || 999,
      isFavorite: false,
      useCount: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    return newEmoji;
  }

  private async mockRemoveEmoji(packId: string, emojiId: string): Promise<boolean> {
    console.log('删除表情:', packId, emojiId);
    return true;
  }

  private async mockRecordEmojiUsage(emojiId: string, context: string): Promise<boolean> {
    console.log('记录表情使用:', emojiId, context);
    return true;
  }

  private async mockToggleEmojiFavorite(emojiId: string, isFavorite: boolean): Promise<boolean> {
    console.log('更新表情收藏状态:', emojiId, isFavorite);
    return true;
  }

  private async mockGetRecentlyUsedEmojis(limit: number): Promise<EmojiItem[]> {
    const packs = await this.mockGetEmojiPacks();
    const allEmojis = packs.packs.flatMap(pack => pack.emojis);
    return allEmojis
      .sort((a, b) => b.useCount - a.useCount)
      .slice(0, limit);
  }

  private async mockGetFavoriteEmojis(limit: number): Promise<EmojiItem[]> {
    const packs = await this.mockGetEmojiPacks();
    const allEmojis = packs.packs.flatMap(pack => pack.emojis);
    return allEmojis
      .filter(emoji => emoji.isFavorite)
      .slice(0, limit);
  }

  private async mockSearchEmojis(keyword: string, category?: EmojiCategory): Promise<EmojiItem[]> {
    const packs = await this.mockGetEmojiPacks();
    const allEmojis = packs.packs.flatMap(pack => pack.emojis);

    return allEmojis.filter(emoji => {
      const matchesKeyword = !keyword ||
        emoji.name.toLowerCase().includes(keyword.toLowerCase());
      const matchesCategory = !category || emoji.category === category;

      return matchesKeyword && matchesCategory;
    });
  }

  private async mockGetEmojiStatistics(userId: number): Promise<EmojiStatistics> {
    const packs = await this.mockGetEmojiPacks();
    const allEmojis = packs.packs.flatMap(pack => pack.emojis);

    return {
      totalUsage: allEmojis.reduce((sum, emoji) => sum + emoji.useCount, 0),
      mostUsedEmojis: allEmojis.sort((a, b) => b.useCount - a.useCount).slice(0, 10),
      recentlyUsedEmojis: allEmojis.sort((a, b) => b.useCount - a.useCount).slice(0, 10),
      favoriteEmojis: allEmojis.filter(emoji => emoji.isFavorite),
      usageByCategory: [
        { category: 'SMILEYS' as any, count: 500 },
        { category: 'GESTURES' as any, count: 270 },
        { category: 'ANIMALS' as any, count: 330 },
      ],
    };
  }
}

/**
 * 导出表情包服务单例
 */
export const emojiService = new EmojiService();
export default emojiService;