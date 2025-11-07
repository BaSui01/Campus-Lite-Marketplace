# 🔧 服务层批量重构计划

## 📊 优先级分类

### P0 - 高频核心服务（4个文件）⭐⭐⭐
**特点**：用户高频使用、影响核心业务流程
1. `user.ts` - 用户服务（登录、注册、个人信息）
2. `message.ts` - 消息服务（即时通讯）
3. `post.ts` - 帖子服务（论坛社区）
4. `refund.ts` - 退款服务（交易相关）

### P1 - 管理员服务（10个文件）⭐⭐
**特点**：管理后台功能、使用频率中等
5. `adminUser.ts` - 管理员用户管理
6. `appeal.ts` - 申诉管理
7. `campus.ts` - 校区管理
8. `category.ts` - 分类管理
9. `compliance.ts` - 合规审核
10. `dispute.ts` - 争议处理
11. `disputeStatistics.ts` - 争议统计
12. `featureFlag.ts` - 功能开关
13. `role.ts` - 角色权限
14. `report.ts` - 举报管理

### P2 - 辅助服务（13个文件）⭐
**特点**：功能支持、低频使用
15. `blacklist.ts` - 黑名单
16. `community.ts` - 社区功能
17. `logistics.ts` - 物流查询
18. `monitor.ts` - 系统监控
19. `notificationPreference.ts` - 通知偏好
20. `notificationTemplate.ts` - 通知模板
21. `rateLimit.ts` - 限流管理
22. `recommend.ts` - 推荐算法
23. `softDelete.ts` - 软删除
24. `tag.ts` - 标签管理
25. `task.ts` - 任务管理
26. `topic.ts` - 话题管理
27. `upload.ts` - 文件上传

## 🎯 重构策略

### 分批次执行
- **第1批**：P0（4个文件）- 30分钟
- **第2批**：P1（10个文件）- 1小时
- **第3批**：P2（13个文件）- 1.5小时

### 重构模板
遵循 `order.ts` 的重构模式：
1. 移除 `http` 导入，改用 `getApi()`
2. 替换所有手写路径为 `DefaultApi` 方法
3. 更新类型定义为 OpenAPI 生成的类型
4. 备份原文件为 `.old`

## 📋 执行检查清单
- [ ] 检查 OpenAPI 生成的 DefaultApi 是否包含对应方法
- [ ] 替换所有 `http.get/post/put/delete` 调用
- [ ] 更新类型导入路径
- [ ] 保持方法签名向后兼容（如需）
- [ ] 运行 TypeScript 类型检查
- [ ] 更新重构报告

---

**开始时间**: 2025-11-07  
**预计完成**: 3小时  
**负责人**: BaSui 😎
