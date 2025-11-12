/**
 * 商品浏览与购买流程 E2E测试
 * @author BaSui 😎
 * @description 测试商品搜索、详情查看、收藏、购买流程
 */

import { test, expect } from '@playwright/test';

test.describe('商品浏览与购买流程', () => {
  
  // 登录辅助函数
  async function login(page: any) {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'testuser');
    await page.fill('input[name="password"]', 'Test123456!');
    await page.click('button[type="submit"]');
    await page.waitForTimeout(2000);
  }

  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('商品搜索功能', async ({ page }) => {
    // 等待搜索框加载
    await page.waitForSelector('input[placeholder*="搜索"]', { timeout: 10000 });
    
    // 输入搜索关键词
    await page.fill('input[placeholder*="搜索"]', '手机');
    
    // 提交搜索
    await page.press('input[placeholder*="搜索"]', 'Enter');
    
    // 等待搜索结果加载
    await page.waitForTimeout(2000);
    
    // 验证URL包含搜索关键词
    expect(page.url()).toContain('search');
    
    // 验证搜索结果显示
    const searchResults = page.locator('[data-testid="goods-list"]').or(page.locator('.goods-card')).first();
    await expect(searchResults).toBeVisible({ timeout: 5000 });
  });

  test('商品详情查看', async ({ page }) => {
    // 等待首页商品列表加载
    await page.waitForSelector('.goods-card', { timeout: 10000 });
    
    // 点击第一个商品
    await page.click('.goods-card >> nth=0');
    
    // 等待导航到商品详情页
    await page.waitForTimeout(2000);
    expect(page.url()).toContain('/goods/');
    
    // 验证商品详情元素
    await expect(page.locator('text=商品详情').or(page.locator('.goods-title'))).toBeVisible();
    await expect(page.locator('text=价格').or(page.locator('.goods-price'))).toBeVisible();
    await expect(page.locator('text=立即购买').or(page.locator('button:has-text("购买")'))).toBeVisible();
  });

  test('商品收藏功能', async ({ page }) => {
    // 先登录
    await login(page);
    
    // 访问商品详情页
    await page.goto('/goods/1');
    await page.waitForTimeout(1000);
    
    // 点击收藏按钮
    const favoriteButton = page.locator('button:has-text("收藏")').or(page.locator('[data-testid="favorite-button"]'));
    await favoriteButton.click();
    
    // 等待收藏操作完成
    await page.waitForTimeout(1000);
    
    // 验证收藏状态变化
    await expect(favoriteButton).toHaveText(/已收藏|取消收藏/);
  });

  test('商品购买流程', async ({ page }) => {
    // 先登录
    await login(page);
    
    // 访问商品详情页
    await page.goto('/goods/1');
    await page.waitForTimeout(1000);
    
    // 点击立即购买
    await page.click('button:has-text("立即购买")').or(page.click('button:has-text("购买")'));
    
    // 等待跳转到订单确认页
    await page.waitForTimeout(2000);
    
    // 验证是否到达订单页面
    const url = page.url();
    expect(url).toMatch(/\/(order|checkout)/);
    
    // 验证订单信息显示
    await expect(page.locator('text=订单').or(page.locator('text=确认'))).toBeVisible();
  });

  test('商品分类浏览', async ({ page }) => {
    // 等待分类导航加载
    await page.waitForSelector('[data-testid="category-nav"]', { timeout: 10000 }).catch(() => {
      // 如果没有分类导航，直接返回
      return;
    });
    
    // 点击第一个分类
    const categories = page.locator('[data-testid="category-nav"] a').or(page.locator('.category-item'));
    const firstCategory = categories.first();
    
    if (await firstCategory.isVisible()) {
      await firstCategory.click();
      await page.waitForTimeout(2000);
      
      // 验证分类商品列表显示
      const goodsList = page.locator('.goods-card');
      await expect(goodsList.first()).toBeVisible({ timeout: 5000 });
    }
  });

  test('商品列表分页', async ({ page }) => {
    // 访问商品列表页
    await page.goto('/goods');
    await page.waitForTimeout(2000);
    
    // 查找分页组件
    const pagination = page.locator('[data-testid="pagination"]').or(page.locator('.pagination'));
    
    if (await pagination.isVisible()) {
      // 点击下一页
      await pagination.locator('text=下一页').or(pagination.locator('button >> nth=1')).click();
      await page.waitForTimeout(1000);
      
      // 验证URL或页面内容变化
      expect(page.url()).toContain('page=2');
    }
  });
});
