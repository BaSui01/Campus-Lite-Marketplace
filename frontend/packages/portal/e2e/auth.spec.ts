/**
 * 用户认证流程 E2E测试
 * @author BaSui 😎
 * @description 测试用户注册、登录、忘记密码流程
 */

import { test, expect } from '@playwright/test';

test.describe('用户认证流程', () => {
  
  test.beforeEach(async ({ page }) => {
    // 每个测试前访问首页
    await page.goto('/');
  });

  test('访问首页应该显示登录按钮', async ({ page }) => {
    // 等待页面加载
    await page.waitForLoadState('networkidle');
    
    // 检查登录按钮是否存在
    const loginButton = page.locator('text=登录');
    await expect(loginButton).toBeVisible();
  });

  test('用户注册流程', async ({ page }) => {
    // 点击登录按钮
    await page.click('text=登录');
    
    // 等待导航到登录页
    await expect(page).toHaveURL(/.*login/);
    
    // 点击注册链接
    await page.click('text=注册');
    
    // 等待导航到注册页
    await expect(page).toHaveURL(/.*register/);
    
    // 生成随机用户信息
    const timestamp = Date.now();
    const username = `testuser${timestamp}`;
    const email = `test${timestamp}@example.com`;
    const password = 'Test123456!';
    
    // 填写注册表单
    await page.fill('input[name="username"]', username);
    await page.fill('input[name="email"]', email);
    await page.fill('input[name="password"]', password);
    await page.fill('input[name="confirmPassword"]', password);
    
    // 提交表单
    await page.click('button[type="submit"]');
    
    // 等待注册成功提示或跳转
    await page.waitForTimeout(2000);
    
    // 验证是否跳转到首页或显示成功消息
    const url = page.url();
    expect(url).toMatch(/\/(home)?$/);
  });

  test('用户登录流程', async ({ page }) => {
    // 点击登录按钮
    await page.click('text=登录');
    
    // 等待导航到登录页
    await expect(page).toHaveURL(/.*login/);
    
    // 使用测试账号登录
    await page.fill('input[name="username"]', 'testuser');
    await page.fill('input[name="password"]', 'Test123456!');
    
    // 提交登录表单
    await page.click('button[type="submit"]');
    
    // 等待登录成功
    await page.waitForTimeout(2000);
    
    // 验证是否跳转到首页
    await expect(page).toHaveURL(/\/(home)?$/);
    
    // 验证用户菜单是否可见
    const userMenu = page.locator('[data-testid="user-menu"]').or(page.locator('text=个人中心'));
    await expect(userMenu).toBeVisible({ timeout: 5000 });
  });

  test('忘记密码流程', async ({ page }) => {
    // 点击登录按钮
    await page.click('text=登录');
    
    // 等待导航到登录页
    await expect(page).toHaveURL(/.*login/);
    
    // 点击忘记密码链接
    await page.click('text=忘记密码');
    
    // 等待导航到忘记密码页
    await expect(page).toHaveURL(/.*forgot-password/);
    
    // 填写邮箱
    await page.fill('input[name="email"]', 'test@example.com');
    
    // 提交表单
    await page.click('button[type="submit"]');
    
    // 等待提示消息
    await page.waitForTimeout(1000);
    
    // 验证是否显示成功提示
    const successMessage = page.locator('text=已发送').or(page.locator('text=邮件'));
    await expect(successMessage).toBeVisible({ timeout: 5000 });
  });

  test('登出流程', async ({ page }) => {
    // 先登录
    await page.goto('/login');
    await page.fill('input[name="username"]', 'testuser');
    await page.fill('input[name="password"]', 'Test123456!');
    await page.click('button[type="submit"]');
    await page.waitForTimeout(2000);
    
    // 点击用户菜单
    await page.click('[data-testid="user-menu"]').catch(() => page.click('text=个人中心'));
    
    // 点击退出登录
    await page.click('text=退出').or(page.click('text=登出'));
    
    // 等待跳转
    await page.waitForTimeout(1000);
    
    // 验证是否跳转到首页或登录页
    const url = page.url();
    expect(url).toMatch(/\/(home|login)?$/);
  });
});
