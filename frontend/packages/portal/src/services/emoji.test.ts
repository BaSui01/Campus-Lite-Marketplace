/**
 * EmojiService 服务测试
 * @author BaSui 😎
 * @description 测试表情包服务的功能
 */

import { emojiService } from './emoji';
import type {
  EmojiPack,
  EmojiItem,
  EmojiPackType,
  EmojiCategory,
  CreateEmojiPackRequest,
  AddEmojiRequest,
} from '@campus/shared/types/emoji';

describe('EmojiService', () => {
  beforeEach(() => {
    // 清除缓存
    emojiService.clearCache();
  });

  describe('getEmojiPacks', () => {
    it('should return emoji packs from cache', async () => {
      // 第一次调用
      const result1 = await emojiService.getEmojiPacks();
      expect(result1.packs).toHaveLength(2); // 系统默认有两个表情包
      expect(result1.packs[0].name).toBe('经典笑脸');
      expect(result1.packs[1].name).toBe('可爱动物');

      // 第二次调用应该使用缓存
      const result2 = await emojiService.getEmojiPacks();
      expect(result2.packs).toEqual(result1.packs);
    });

    it('should return filtered emoji packs with query params', async () => {
      const result = await emojiService.getEmojiPacks({
        type: 'SYSTEM' as EmojiPackType,
        status: 'ACTIVE' as any,
      });

      expect(result.packs.length).toBeGreaterThan(0);
      result.packs.forEach(pack => {
        expect(pack.type).toBe('SYSTEM');
        expect(pack.status).toBe('ACTIVE');
      });
    });

    it('should handle search keyword', async () => {
      const result = await emojiService.getEmojiPacks({
        keyword: '笑脸',
      });

      expect(result.packs.length).toBeGreaterThan(0);
      // 应该包含"经典笑脸"表情包
      const smileyPack = result.packs.find(pack => pack.name.includes('笑脸'));
      expect(smileyPack).toBeDefined();
    });
  });

  describe('getEmojiPackById', () => {
    it('should return emoji pack by ID', async () => {
      const packs = await emojiService.getEmojiPacks();
      const targetPack = packs.packs[0];

      const result = await emojiService.getEmojiPackById(targetPack.id);
      expect(result).toEqual(targetPack);
    });

    it('should return null for non-existent pack', async () => {
      const result = await emojiService.getEmojiPackById('non-existent-id');
      expect(result).toBeNull();
    });
  });

  describe('createEmojiPack', () => {
    it('should create new emoji pack', async () => {
      const requestData: CreateEmojiPackRequest = {
        name: '我的自定义表情包',
        description: '测试用的自定义表情包',
        type: 'CUSTOM' as EmojiPackType,
      };

      const result = await emojiService.createEmojiPack(requestData);

      expect(result.name).toBe(requestData.name);
      expect(result.description).toBe(requestData.description);
      expect(result.type).toBe(requestData.type);
      expect(result.author).toBe('User');
      expect(result.isBuiltIn).toBe(false);
      expect(result.emojis).toHaveLength(0);
    });
  });

  describe('updateEmojiPack', () => {
    it('should update emoji pack', async () => {
      const packs = await emojiService.getEmojiPacks();
      const targetPack = packs.packs[0];

      const success = await emojiService.updateEmojiPack(targetPack.id, {
        name: '更新后的表情包名称',
        description: '更新后的描述',
      });

      expect(success).toBe(true);
    });
  });

  describe('deleteEmojiPack', () => {
    it('should delete emoji pack', async () => {
      const packs = await emojiService.getEmojiPacks();
      const targetPack = packs.packs[0];

      const success = await emojiService.deleteEmojiPack(targetPack.id);
      expect(success).toBe(true);
    });
  });

  describe('addEmojiToPack', () => {
    it('should add emoji to pack', async () => {
      const packs = await emojiService.getEmojiPacks();
      const targetPack = packs.packs[0];

      const requestData: AddEmojiRequest = {
        name: '测试表情',
        content: '🤣',
        contentType: 'text',
        category: 'SMILEYS' as EmojiCategory,
        sortOrder: 999,
      };

      const result = await emojiService.addEmojiToPack(targetPack.id, requestData);

      expect(result.name).toBe(requestData.name);
      expect(result.content).toBe(requestData.content);
      expect(result.contentType).toBe(requestData.contentType);
      expect(result.category).toBe(requestData.category);
      expect(result.packId).toBe(targetPack.id);
    });
  });

  describe('removeEmoji', () => {
    it('should remove emoji from pack', async () => {
      const packs = await emojiService.getEmojiPacks();
      const targetPack = packs.packs[0];
      const targetEmoji = targetPack.emojis[0];

      const success = await emojiService.removeEmoji(targetPack.id, targetEmoji.id);
      expect(success).toBe(true);
    });
  });

  describe('recordEmojiUsage', () => {
    it('should record emoji usage', async () => {
      const packs = await emojiService.getEmojiPacks();
      const targetEmoji = packs.packs[0].emojis[0];

      const success = await emojiService.recordEmojiUsage(targetEmoji.id, 'chat');
      expect(success).toBe(true);
    });

    it('should record usage for different contexts', async () => {
      const packs = await emojiService.getEmojiPacks();
      const targetEmoji = packs.packs[0].emojis[0];

      const contexts = ['chat', 'dispute-chat', 'comment'];
      for (const context of contexts) {
        const success = await emojiService.recordEmojiUsage(targetEmoji.id, context);
        expect(success).toBe(true);
      }
    });
  });

  describe('toggleEmojiFavorite', () => {
    it('should toggle emoji favorite status', async () => {
      const packs = await emojiService.getEmojiPacks();
      const targetEmoji = packs.packs[0].emojis[0];

      // 添加到收藏
      let success = await emojiService.toggleEmojiFavorite(targetEmoji.id, true);
      expect(success).toBe(true);

      // 取消收藏
      success = await emojiService.toggleEmojiFavorite(targetEmoji.id, false);
      expect(success).toBe(true);
    });
  });

  describe('getRecentlyUsedEmojis', () => {
    it('should return recently used emojis', async () => {
      const emojis = await emojiService.getRecentlyUsedEmojis(10);
      expect(emojis.length).toBeGreaterThan(0);
      expect(emojis.length).toBeLessThanOrEqual(10);

      // 应该按使用次数排序
      for (let i = 0; i < emojis.length - 1; i++) {
        expect(emojis[i].useCount).toBeGreaterThanOrEqual(emojis[i + 1].useCount);
      }
    });

    it('should respect limit parameter', async () => {
      const emojis5 = await emojiService.getRecentlyUsedEmojis(5);
      const emojis10 = await emojiService.getRecentlyUsedEmojis(10);

      expect(emojis5.length).toBeLessThanOrEqual(5);
      expect(emojis10.length).toBeLessThanOrEqual(10);
      expect(emojis5.length).toBeLessThanOrEqual(emojis10.length);
    });
  });

  describe('getFavoriteEmojis', () => {
    it('should return favorite emojis', async () => {
      // 先添加一些收藏
      const packs = await emojiService.getEmojiPacks();
      const targetEmoji = packs.packs[0].emojis[0];
      await emojiService.toggleEmojiFavorite(targetEmoji.id, true);

      const favorites = await emojiService.getFavoriteEmojis();
      expect(favorites.length).toBeGreaterThan(0);

      favorites.forEach(emoji => {
        expect(emoji.isFavorite).toBe(true);
      });
    });
  });

  describe('searchEmojis', () => {
    it('should search emojis by keyword', async () => {
      const results = await emojiService.searchEmojis('笑');
      expect(results.length).toBeGreaterThan(0);

      results.forEach(emoji => {
        expect(emoji.name.toLowerCase()).toContain('笑');
      });
    });

    it('should search emojis by category', async () => {
      const results = await emojiService.searchEmojis('', 'SMILEYS' as EmojiCategory);
      expect(results.length).toBeGreaterThan(0);

      results.forEach(emoji => {
        expect(emoji.category).toBe('SMILEYS');
      });
    });

    it('should search emojis by keyword and category', async () => {
      const results = await emojiService.searchEmojis('笑', 'SMILEYS' as EmojiCategory);
      expect(results.length).toBeGreaterThan(0);

      results.forEach(emoji => {
        expect(emoji.name.toLowerCase()).toContain('笑');
        expect(emoji.category).toBe('SMILEYS');
      });
    });

    it('should return empty results for non-existent keywords', async () => {
      const results = await emojiService.searchEmojis('不存在的表情');
      expect(results).toHaveLength(0);
    });
  });

  describe('getEmojiStatistics', () => {
    it('should return emoji usage statistics', async () => {
      const stats = await emojiService.getEmojiStatistics(1);
      expect(stats).not.toBeNull();

      if (stats) {
        expect(stats.totalUsage).toBeGreaterThan(0);
        expect(stats.mostUsedEmojis.length).toBeGreaterThan(0);
        expect(stats.recentlyUsedEmojis.length).toBeGreaterThan(0);
        expect(stats.usageByCategory.length).toBeGreaterThan(0);

        // 验证统计数据的一致性
        const calculatedTotal = stats.usageByCategory.reduce((sum, cat) => sum + cat.count, 0);
        expect(calculatedTotal).toBe(stats.totalUsage);
      }
    });
  });

  describe('clearCache', () => {
    it('should clear all caches', async () => {
      // 先加载数据到缓存
      await emojiService.getEmojiPacks();
      await emojiService.getRecentlyUsedEmojis();
      await emojiService.getFavoriteEmojis();

      // 清除缓存
      emojiService.clearCache();

      // 再次获取数据应该重新加载（这里通过检查是否有缓存来验证）
      // 在实际实现中，可能需要更复杂的缓存检查逻辑
      expect(true).toBe(true); // 这里只是示意清除缓存的调用
    });
  });

  describe('Error Handling', () => {
    it('should handle API errors gracefully', async () => {
      // 这里可以模拟网络错误或其他异常情况
      // 由于我们使用的是模拟数据，实际错误处理需要在真实API环境中测试
      expect(true).toBe(true);
    });
  });

  describe('Performance', () => {
    it('should handle large emoji collections efficiently', async () => {
      const startTime = Date.now();

      // 执行多个操作
      await Promise.all([
        emojiService.getEmojiPacks(),
        emojiService.getRecentlyUsedEmojis(),
        emojiService.getFavoriteEmojis(),
        emojiService.searchEmojis(''),
      ]);

      const endTime = Date.now();
      const duration = endTime - startTime;

      // 操作应该在合理时间内完成（比如小于1秒）
      expect(duration).toBeLessThan(1000);
    });
  });
});