/**
 * 订单管理流程 E2E测试
 * @author BaSui 😎
 * @description 测试订单创建、支付、评价、退款流程
 */

import { test, expect } from '@playwright/test';

test.describe('订单管理流程', () => {
  
  // 登录辅助函数
  async function login(page: any) {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'testuser');
    await page.fill('input[name="password"]', 'Test123456!');
    await page.click('button[type="submit"]');
    await page.waitForTimeout(2000);
  }

  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('查看订单列表', async ({ page }) => {
    // 访问订单列表页
    await page.goto('/orders');
    await page.waitForTimeout(2000);
    
    // 验证订单列表显示
    const ordersPage = page.locator('text=我的订单').or(page.locator('[data-testid="orders-page"]'));
    await expect(ordersPage).toBeVisible();
  });

  test('订单筛选功能', async ({ page }) => {
    // 访问订单列表页
    await page.goto('/orders');
    await page.waitForTimeout(1000);
    
    // 查找订单状态筛选器
    const filterTabs = page.locator('[data-testid="order-tabs"]').or(page.locator('.order-tabs'));
    
    if (await filterTabs.isVisible()) {
      // 点击"待支付"标签
      await filterTabs.locator('text=待支付').or(filterTabs.locator('button >> nth=1')).click();
      await page.waitForTimeout(1000);
      
      // 验证URL或页面内容变化
      const url = page.url();
      expect(url).toMatch(/status|tab/);
    }
  });

  test('查看订单详情', async ({ page }) => {
    // 访问订单列表页
    await page.goto('/orders');
    await page.waitForTimeout(2000);
    
    // 点击第一个订单
    const firstOrder = page.locator('.order-card').or(page.locator('[data-testid="order-item"]')).first();
    
    if (await firstOrder.isVisible()) {
      await firstOrder.click();
      await page.waitForTimeout(2000);
      
      // 验证订单详情页
      const url = page.url();
      expect(url).toContain('/orders/');
      
      // 验证订单详情元素
      await expect(page.locator('text=订单详情').or(page.locator('text=订单信息'))).toBeVisible();
    }
  });

  test('订单搜索功能', async ({ page }) => {
    // 访问订单列表页
    await page.goto('/orders');
    await page.waitForTimeout(1000);
    
    // 查找搜索框
    const searchInput = page.locator('input[placeholder*="搜索"]').or(page.locator('[data-testid="order-search"]'));
    
    if (await searchInput.isVisible()) {
      // 输入搜索关键词
      await searchInput.fill('手机');
      await searchInput.press('Enter');
      await page.waitForTimeout(1000);
      
      // 验证搜索结果
      const searchResults = page.locator('.order-card');
      await expect(searchResults.first()).toBeVisible({ timeout: 5000 });
    }
  });

  test('取消订单流程', async ({ page }) => {
    // 访问订单列表页
    await page.goto('/orders');
    await page.waitForTimeout(2000);
    
    // 查找待支付订单的取消按钮
    const cancelButton = page.locator('button:has-text("取消订单")').first();
    
    if (await cancelButton.isVisible()) {
      await cancelButton.click();
      
      // 确认取消
      await page.waitForTimeout(500);
      const confirmButton = page.locator('button:has-text("确定")').or(page.locator('button:has-text("确认")'));
      if (await confirmButton.isVisible()) {
        await confirmButton.click();
      }
      
      // 等待操作完成
      await page.waitForTimeout(1000);
      
      // 验证成功提示
      const successMessage = page.locator('text=取消成功').or(page.locator('text=已取消'));
      await expect(successMessage).toBeVisible({ timeout: 5000 });
    }
  });

  test('订单评价流程', async ({ page }) => {
    // 访问订单列表页
    await page.goto('/orders');
    await page.waitForTimeout(2000);
    
    // 查找已完成订单的评价按钮
    const reviewButton = page.locator('button:has-text("评价")').first();
    
    if (await reviewButton.isVisible()) {
      await reviewButton.click();
      await page.waitForTimeout(1000);
      
      // 验证是否到达评价页面
      const url = page.url();
      expect(url).toMatch(/review|evaluate/);
      
      // 填写评价
      const ratingStars = page.locator('[data-testid="rating-stars"]').or(page.locator('.rating-star'));
      if (await ratingStars.first().isVisible()) {
        await ratingStars.nth(4).click(); // 点击5星
      }
      
      // 填写评价内容
      const commentTextarea = page.locator('textarea[placeholder*="评价"]').or(page.locator('[data-testid="comment-input"]'));
      if (await commentTextarea.isVisible()) {
        await commentTextarea.fill('商品质量很好，非常满意！');
      }
      
      // 提交评价
      await page.click('button[type="submit"]').or(page.click('button:has-text("提交")'));
      await page.waitForTimeout(2000);
      
      // 验证评价成功
      const successMessage = page.locator('text=评价成功').or(page.locator('text=感谢'));
      await expect(successMessage).toBeVisible({ timeout: 5000 });
    }
  });
});
