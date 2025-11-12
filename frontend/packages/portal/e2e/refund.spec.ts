/**
 * 退款申请流程 E2E测试
 * @author BaSui 😎
 * @description 测试退款申请、退款详情、退款列表流程
 */

import { test, expect } from '@playwright/test';

test.describe('退款申请流程', () => {
  
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

  test('访问退款列表页', async ({ page }) => {
    // 访问退款列表页
    await page.goto('/refunds');
    await page.waitForTimeout(2000);
    
    // 验证退款列表页显示
    const refundsPage = page.locator('text=退款').or(page.locator('[data-testid="refunds-page"]'));
    await expect(refundsPage).toBeVisible();
  });

  test('申请退款流程', async ({ page }) => {
    // 访问订单列表页
    await page.goto('/orders');
    await page.waitForTimeout(2000);
    
    // 查找已支付订单的退款按钮
    const refundButton = page.locator('button:has-text("退款")').or(page.locator('button:has-text("申请退款")'));
    const firstRefundButton = refundButton.first();
    
    if (await firstRefundButton.isVisible()) {
      await firstRefundButton.click();
      await page.waitForTimeout(1000);
      
      // 验证是否到达退款申请页面
      const url = page.url();
      expect(url).toMatch(/refund|申请/);
      
      // 选择退款原因
      const reasonSelect = page.locator('select[name="reason"]').or(page.locator('[data-testid="refund-reason"]'));
      if (await reasonSelect.isVisible()) {
        await reasonSelect.selectOption({ index: 1 });
      }
      
      // 填写退款说明
      const descriptionTextarea = page.locator('textarea[name="description"]').or(page.locator('[data-testid="refund-description"]'));
      if (await descriptionTextarea.isVisible()) {
        await descriptionTextarea.fill('商品与描述不符，申请退款');
      }
      
      // 提交退款申请
      await page.click('button[type="submit"]').or(page.click('button:has-text("提交")'));
      await page.waitForTimeout(2000);
      
      // 验证申请成功
      const successMessage = page.locator('text=申请成功').or(page.locator('text=已提交'));
      await expect(successMessage).toBeVisible({ timeout: 5000 });
    }
  });

  test('查看退款详情', async ({ page }) => {
    // 访问退款列表页
    await page.goto('/refunds');
    await page.waitForTimeout(2000);
    
    // 点击第一个退款记录
    const firstRefund = page.locator('.refund-card').or(page.locator('[data-testid="refund-item"]')).first();
    
    if (await firstRefund.isVisible()) {
      await firstRefund.click();
      await page.waitForTimeout(2000);
      
      // 验证退款详情页
      const url = page.url();
      expect(url).toContain('/refund');
      
      // 验证退款详情元素
      await expect(page.locator('text=退款详情').or(page.locator('text=退款信息'))).toBeVisible();
    }
  });

  test('撤销退款申请', async ({ page }) => {
    // 访问退款列表页
    await page.goto('/refunds');
    await page.waitForTimeout(2000);
    
    // 查找撤销按钮（仅对待审核的退款）
    const cancelButton = page.locator('button:has-text("撤销")').first();
    
    if (await cancelButton.isVisible()) {
      await cancelButton.click();
      
      // 确认撤销
      await page.waitForTimeout(500);
      const confirmButton = page.locator('button:has-text("确定")').or(page.locator('button:has-text("确认")'));
      if (await confirmButton.isVisible()) {
        await confirmButton.click();
      }
      
      // 等待操作完成
      await page.waitForTimeout(1000);
      
      // 验证成功提示
      const successMessage = page.locator('text=撤销成功').or(page.locator('text=已撤销'));
      await expect(successMessage).toBeVisible({ timeout: 5000 });
    }
  });

  test('退款状态筛选', async ({ page }) => {
    // 访问退款列表页
    await page.goto('/refunds');
    await page.waitForTimeout(1000);
    
    // 查找状态筛选器
    const filterTabs = page.locator('[data-testid="refund-tabs"]').or(page.locator('.refund-tabs'));
    
    if (await filterTabs.isVisible()) {
      // 点击"退款中"标签
      await filterTabs.locator('text=退款中').or(filterTabs.locator('button >> nth=1')).click();
      await page.waitForTimeout(1000);
      
      // 验证页面内容变化
      const url = page.url();
      expect(url).toMatch(/status|tab/);
    }
  });
});
