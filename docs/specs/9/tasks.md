# Spec #9: 前端管理端开发任务分解

> **版本**: v1.0  
> **创建日期**: 2025-11-05  
> **作者**: BaSui 😎  
> **预计工期**: 21天（2人团队）

---

## 📋 任务总览

| 任务编号 | 任务名称 | 优先级 | 工作量 | 状态 | 负责人 |
|---------|---------|--------|--------|------|-------|
| T1 | 商品管理模块开发 | P0 | 3天 | 待开始 | - |
| T2 | 订单管理模块开发 | P0 | 3天 | 待开始 | - |
| T3 | 申诉管理模块开发 | P1 | 2天 | 待开始 | - |
| T4 | 纠纷仲裁模块开发 | P1 | 4天 | 待开始 | - |
| T5 | 评价管理模块开发 | P1 | 3天 | 待开始 | - |
| T6 | 批量操作模块开发 | P1 | 3天 | 待开始 | - |
| T7 | 日志管理模块开发 | P2 | 1天 | 待开始 | - |
| T8 | 封禁记录管理开发 | P2 | 1天 | 待开始 | - |
| T9 | 帖子审核管理开发 | P2 | 1天 | 待开始 | - |

**总计**: 9个任务，21天工作量

---

## 🎯 开发流程（TDD十步流程）

每个任务都需要遵循以下十步流程：

```
🔍 第0步：复用检查 → 检查@campus/shared中的可复用组件
🔴 第1步：编写测试 → 单元测试+集成测试
🟢 第2步：编写组件 → React组件
🟢 第3步：编写Service → API服务封装（如果shared中没有）
🟢 第4步：编写Hooks → 业务逻辑Hook（如果shared中没有）
🟢 第5步：编写Store → 状态管理（如果需要）
🟢 第6步：配置路由 → 路由配置
🟢 第7步：配置菜单 → 菜单配置
🔵 第8步：运行测试 → 验证测试通过
🔵 第9步：重构优化 → 代码质量提升
```

---

## 📝 任务详情

### T1: 商品管理模块开发 ⚡ P0

**需求编号**: 112  
**工作量**: 3天  
**优先级**: P0（核心功能）

#### 开发任务清单

**第0步：复用检查** ✅
- [x] 检查@campus/shared中的Table、Pagination、Modal等组件
- [x] 检查goodsService是否需要扩展
- [x] 检查useRequest、usePagination等Hooks

**第1步：编写测试** 🔴
- [ ] **GoodsList.test.tsx**: 测试商品列表渲染、搜索、筛选、分页
- [ ] **GoodsAudit.test.tsx**: 测试商品审核表单提交
- [ ] **GoodsDetail.test.tsx**: 测试商品详情展示
- [ ] **goodsService.test.ts**: 测试API调用（Mock）
- [ ] **useGoodsList.test.ts**: 测试Hooks逻辑

**第2步：编写组件** 🟢
- [ ] **GoodsList.tsx**: 商品列表页
  - 搜索栏（关键词、分类、价格区间、状态、标签）
  - 批量操作按钮（批量上架、批量下架、批量删除）
  - 商品表格（20条/页）
  - 操作列（查看详情、编辑、上下架、删除）
- [ ] **GoodsAudit.tsx**: 商品审核页
  - 待审核商品列表
  - 审核表单（批准/拒绝、审核意见）
  - 批量审核功能
- [ ] **GoodsDetail.tsx**: 商品详情页
  - 基本信息卡片（标题、描述、价格、分类、标签、状态）
  - 商品图片轮播（最多10张）
  - 卖家信息卡片（用户名、头像、信用评分）
  - 审核记录时间线
  - 操作按钮（审核、上下架、编辑、删除）
- [ ] **GoodsSearchBar.tsx**: 商品搜索栏组件
- [ ] **GoodsBatchActions.tsx**: 批量操作组件
- [ ] **GoodsAuditForm.tsx**: 审核表单组件
- [ ] **GoodsImageGallery.tsx**: 图片轮播组件

**第3步：编写Service** 🟢
- [ ] 扩展 **goodsService.ts**（@campus/shared/services/）
  - `listGoods(params)` - 商品列表查询
  - `listPendingGoods(params)` - 待审核商品
  - `getGoodsDetail(id)` - 商品详情
  - `approveGoods(id, request)` - 审核商品
  - `updateGoodsStatus(id, status)` - 上下架
  - `deleteGoods(id)` - 删除商品
  - `batchUpdateGoods(request)` - 批量操作

**第4步：编写Hooks** 🟢
- [ ] **useGoodsList.ts**: 商品列表Hook（分页、搜索、筛选）
- [ ] **useGoodsAudit.ts**: 商品审核Hook（批准、拒绝）
- [ ] **useGoodsDetail.ts**: 商品详情Hook（查询、操作）

**第5步：编写Store** 🟢（可选，使用TanStack Query代替）
- [ ] 使用 `useQuery` 管理商品列表
- [ ] 使用 `useMutation` 管理商品审核、上下架等操作

**第6步：配置路由** 🟢
- [ ] 添加路由：`/admin/goods/list` → GoodsList
- [ ] 添加路由：`/admin/content/goods` → GoodsAudit
- [ ] 添加路由：`/admin/goods/:id` → GoodsDetail
- [ ] 添加权限守卫：`SYSTEM_GOODS_VIEW`、`SYSTEM_GOODS_APPROVE`

**第7步：配置菜单** 🟢
- [ ] 添加菜单项：商品管理 → 商品列表、商品审核
- [ ] 关联权限：`SYSTEM_GOODS_VIEW`、`SYSTEM_GOODS_APPROVE`

**第8步：运行测试** 🔵
- [ ] 运行单元测试：`npm test`
- [ ] 验证测试覆盖率 ≥85%
- [ ] 运行E2E测试（手动）

**第9步：重构优化** 🔵
- [ ] 提取通用组件（如果有重复代码）
- [ ] 优化性能（图片懒加载、虚拟滚动）
- [ ] 代码审查、ESLint检查

---

### T2: 订单管理模块开发 ⚡ P0

**需求编号**: 113  
**工作量**: 3天  
**优先级**: P0（核心功能）

#### 开发任务清单

**第0步：复用检查** ✅
- [x] 检查orderService是否需要扩展
- [x] 检查refundService是否需要扩展
- [x] 检查logisticsService是否存在

**第1步：编写测试** 🔴
- [ ] **OrderList.test.tsx**: 测试订单列表、搜索、筛选
- [ ] **OrderDetail.test.tsx**: 测试订单详情展示
- [ ] **RefundManagement.test.tsx**: 测试退款管理
- [ ] **orderService.test.ts**: 测试API调用
- [ ] **useOrderList.test.ts**: 测试Hooks逻辑

**第2步：编写组件** 🟢
- [ ] **OrderList.tsx**: 订单列表页
  - 搜索栏（订单号、商品名、买家/卖家）
  - 状态筛选（8种状态）
  - 时间筛选（今日、近7天、近30天、自定义）
  - 统计卡片（总订单、待支付、已完成、退款中）
  - 订单表格（20条/页）
- [ ] **OrderDetail.tsx**: 订单详情页
  - 订单信息卡片（订单号、状态、时间）
  - 商品信息卡片（图片、标题、价格）
  - 买家信息卡片（用户名、联系方式、收货地址）
  - 卖家信息卡片（用户名、联系方式）
  - 支付信息卡片（支付方式、金额、时间、流水号）
  - 物流信息卡片（物流公司、运单号、物流状态、轨迹时间线）
  - 订单状态时间线（创建→支付→发货→完成）
  - 操作按钮（取消订单、强制完成、查看退款）
- [ ] **RefundManagement.tsx**: 退款管理页
  - 退款列表（退款单号、订单号、金额、原因、状态）
  - 退款审核表单（批准/拒绝、审核意见）
  - 批量审核功能
- [ ] **OrderSearchBar.tsx**: 订单搜索栏组件
- [ ] **OrderStatusFilter.tsx**: 状态筛选组件
- [ ] **OrderStatCards.tsx**: 统计卡片组件
- [ ] **OrderStatusTimeline.tsx**: 订单状态时间线组件
- [ ] **LogisticsInfo.tsx**: 物流信息组件
- [ ] **RefundAuditForm.tsx**: 退款审核表单组件

**第3步：编写Service** 🟢
- [ ] 扩展 **orderService.ts**（@campus/shared/services/）
  - `listOrdersAdmin(params)` - 订单列表（管理员视角）
  - `getOrderDetail(orderNo)` - 订单详情
  - `cancelOrder(orderNo, reason)` - 取消订单
- [ ] 扩展 **refundService.ts**（@campus/shared/services/）
  - `listRefunds(params)` - 退款列表
  - `getRefundDetail(refundId)` - 退款详情
  - `approveRefund(refundId, approved, reason)` - 退款审核
- [ ] 扩展 **logisticsService.ts**（@campus/shared/services/）
  - `getLogisticsInfo(orderNo)` - 物流详情

**第4步：编写Hooks** 🟢
- [ ] **useOrderList.ts**: 订单列表Hook
- [ ] **useOrderDetail.ts**: 订单详情Hook
- [ ] **useRefundList.ts**: 退款列表Hook
- [ ] **useLogisticsInfo.ts**: 物流信息Hook

**第5步：编写Store** 🟢（可选）
- [ ] 使用 `useQuery` 管理订单列表、订单详情
- [ ] 使用 `useMutation` 管理取消订单、退款审核

**第6步：配置路由** 🟢
- [ ] 添加路由：`/admin/orders/list` → OrderList
- [ ] 添加路由：`/admin/orders/:orderNo` → OrderDetail
- [ ] 添加路由：`/admin/orders/refunds` → RefundManagement
- [ ] 添加权限守卫：`SYSTEM_ORDER_VIEW`、`SYSTEM_ORDER_MANAGE`

**第7步：配置菜单** 🟢
- [ ] 添加菜单项：订单管理 → 订单列表、退款管理
- [ ] 关联权限：`SYSTEM_ORDER_VIEW`、`SYSTEM_ORDER_MANAGE`

**第8步：运行测试** 🔵
- [ ] 运行单元测试：`npm test`
- [ ] 验证测试覆盖率 ≥85%
- [ ] 运行E2E测试（手动）

**第9步：重构优化** 🔵
- [ ] 提取通用组件
- [ ] 优化性能（图片懒加载、数据缓存）
- [ ] 代码审查

---

### T3: 申诉管理模块开发 ⚡ P1

**需求编号**: 114  
**工作量**: 2天  
**优先级**: P1（重要功能）

#### 开发任务清单

**第0步：复用检查** ✅
- [x] 检查appealService是否存在（@campus/shared/services/）
- [x] 检查Appeal相关TypeScript类型定义

**第1步：编写测试** 🔴
- [ ] **AppealList.test.tsx**: 测试申诉列表、筛选
- [ ] **AppealDetail.test.tsx**: 测试申诉详情、审核
- [ ] **appealService.test.ts**: 测试API调用
- [ ] **useAppealList.test.ts**: 测试Hooks逻辑

**第2步：编写组件** 🟢
- [ ] **AppealList.tsx**: 申诉列表页
  - 状态筛选（待审核、已批准、已拒绝、已过期）
  - 类型筛选（封禁、商品、订单、其他）
  - 搜索功能（申诉ID、用户名）
  - 统计卡片（总数、待审核、已批准、已拒绝）
  - 申诉表格（20条/页）
- [ ] **AppealDetail.tsx**: 申诉详情页
  - 申诉基本信息（ID、类型、状态、时间）
  - 申诉人信息（用户名、头像、信用评分、历史申诉次数）
  - 申诉内容（标题、描述、材料）
  - 关联信息（封禁记录/商品信息/订单信息）
  - 审核表单（批准/拒绝、审核意见）
  - 审核历史
- [ ] **AppealMaterials.tsx**: 申诉材料组件（图片预览、文件下载）
- [ ] **AppealAuditForm.tsx**: 审核表单组件
- [ ] **AppealBatchActions.tsx**: 批量审核组件

**第3步：编写Service** 🟢
- [ ] 创建 **appealService.ts**（@campus/shared/services/）（如果不存在）
  - `listAppeals(params)` - 申诉列表
  - `getAppealDetail(id)` - 申诉详情
  - `listPendingAppeals(params)` - 待审核申诉
  - `reviewAppeal(request)` - 审核申诉
  - `batchReviewAppeals(request)` - 批量审核
  - `getAppealStatistics()` - 申诉统计
  - `getAppealMaterials(appealId)` - 申诉材料

**第4步：编写Hooks** 🟢
- [ ] **useAppealList.ts**: 申诉列表Hook
- [ ] **useAppealDetail.ts**: 申诉详情Hook
- [ ] **useAppealAudit.ts**: 申诉审核Hook

**第5步：编写Store** 🟢（可选）
- [ ] 使用 `useQuery` 管理申诉列表、申诉详情
- [ ] 使用 `useMutation` 管理申诉审核

**第6步：配置路由** 🟢
- [ ] 添加路由：`/admin/appeals/list` → AppealList
- [ ] 添加路由：`/admin/appeals/:id` → AppealDetail
- [ ] 添加权限守卫：`SYSTEM_USER_APPEAL_HANDLE`

**第7步：配置菜单** 🟢
- [ ] 添加菜单项：申诉管理 → 申诉列表
- [ ] 关联权限：`SYSTEM_USER_APPEAL_HANDLE`

**第8步：运行测试** 🔵
- [ ] 运行单元测试：`npm test`
- [ ] 验证测试覆盖率 ≥85%

**第9步：重构优化** 🔵
- [ ] 提取通用组件
- [ ] 优化性能
- [ ] 代码审查

---

### T4: 纠纷仲裁模块开发 ⚡ P1

**需求编号**: 115  
**工作量**: 4天  
**优先级**: P1（重要功能）

#### 开发任务清单

**第0步：复用检查** ✅
- [x] 检查disputeService是否存在
- [x] 检查Dispute相关TypeScript类型定义
- [x] 检查ECharts图表组件

**第1步：编写测试** 🔴
- [ ] **DisputeList.test.tsx**: 测试纠纷列表
- [ ] **DisputeDetail.test.tsx**: 测试纠纷详情、仲裁
- [ ] **DisputeEvidence.test.tsx**: 测试证据管理
- [ ] **DisputeStatistics.test.tsx**: 测试统计图表
- [ ] **disputeService.test.ts**: 测试API调用

**第2步：编写组件** 🟢
- [ ] **DisputeList.tsx**: 纠纷列表页
  - 状态筛选（协商中、待仲裁、仲裁中、已完成）
  - 类型筛选（商品、服务、物流、其他）
  - 搜索功能（纠纷ID、订单号）
  - 统计卡片（总数、协商中、待仲裁、今日完成）
  - 纠纷表格（20条/页）
- [ ] **DisputeDetail.tsx**: 纠纷详情页
  - 纠纷基本信息（ID、类型、状态、阶段、仲裁员）
  - 订单信息卡片（订单号、商品、买卖家）
  - 买家申诉卡片（申诉内容、诉求）
  - 卖家回复卡片（回复内容、态度）
  - 证据材料卡片（买家证据、卖家证据、哈希验证）
  - 协商记录时间线（买家↔卖家消息往来）
  - 仲裁决策表单（仲裁结果、理由、处理措施）
- [ ] **DisputeEvidence.tsx**: 证据管理页
  - 证据列表（ID、提交人、类型、时间、哈希）
  - 证据详情（图片预览、文件下载、哈希验证）
  - 证据上传（管理员补充）
- [ ] **DisputeStatistics.tsx**: 纠纷统计页
  - 纠纷趋势图（折线图，ECharts）
  - 仲裁结果分析（饼图，ECharts）
  - 纠纷类型分布（饼图，ECharts）
  - 仲裁员绩效统计（表格）
- [ ] **DisputeEvidenceViewer.tsx**: 证据查看器组件
- [ ] **NegotiationTimeline.tsx**: 协商记录时间线组件
- [ ] **ArbitrationDecisionForm.tsx**: 仲裁决策表单组件

**第3步：编写Service** 🟢
- [ ] 创建 **disputeService.ts**（@campus/shared/services/）（如果不存在）
  - `listDisputes(params)` - 纠纷列表
  - `getDisputeDetail(id)` - 纠纷详情
  - `assignArbitrator(disputeId, arbitratorId)` - 分配仲裁员
  - `submitArbitration(request)` - 提交仲裁决定
  - `getEvidenceList(disputeId)` - 证据列表
  - `uploadEvidence(request)` - 上传证据
  - `getNegotiationMessages(disputeId)` - 协商记录
  - `getDisputeTrend()` - 纠纷趋势
  - `getArbitrationResult()` - 仲裁结果分析
  - `getDisputeTypeDistribution()` - 类型分布

**第4步：编写Hooks** 🟢
- [ ] **useDisputeList.ts**: 纠纷列表Hook
- [ ] **useDisputeDetail.ts**: 纠纷详情Hook
- [ ] **useEvidenceList.ts**: 证据列表Hook
- [ ] **useDisputeStatistics.ts**: 统计数据Hook

**第5步：编写Store** 🟢（可选）
- [ ] 使用 `useQuery` 管理纠纷数据
- [ ] 使用 `useMutation` 管理仲裁操作

**第6步：配置路由** 🟢
- [ ] 添加路由：`/admin/disputes/list` → DisputeList
- [ ] 添加路由：`/admin/disputes/:id` → DisputeDetail
- [ ] 添加路由：`/admin/disputes/:id/evidence` → DisputeEvidence
- [ ] 添加路由：`/admin/disputes/statistics` → DisputeStatistics
- [ ] 添加权限守卫：`ROLE_ADMIN`

**第7步：配置菜单** 🟢
- [ ] 添加菜单项：纠纷仲裁 → 纠纷列表、证据管理、纠纷统计
- [ ] 关联权限：`ROLE_ADMIN`

**第8步：运行测试** 🔵
- [ ] 运行单元测试：`npm test`
- [ ] 验证测试覆盖率 ≥85%

**第9步：重构优化** 🔵
- [ ] 提取通用组件
- [ ] 优化图表性能
- [ ] 代码审查

---

### T5: 评价管理模块开发 ⚡ P1

**需求编号**: 116  
**工作量**: 3天  
**优先级**: P1（重要功能）

#### 开发任务清单

**第0步：复用检查** ✅
- [x] 检查reviewService是否存在
- [x] 检查Review相关TypeScript类型定义

**第1步：编写测试** 🔴
- [ ] **ReviewList.test.tsx**: 测试评价列表
- [ ] **ReviewDetail.test.tsx**: 测试评价详情
- [ ] **reviewService.test.ts**: 测试API调用

**第2步：编写组件** 🟢
- [ ] **ReviewList.tsx**: 评价列表页
  - 评分筛选（全部、5星、4星、3星、2星、1星）
  - 状态筛选（正常、违规、已删除）
  - 关键词搜索（评价内容、商品名、用户名）
  - 统计卡片（总数、平均评分、违规数、待审核数）
  - 评价表格（20条/页）
- [ ] **ReviewDetail.tsx**: 评价详情页
  - 评价基本信息（ID、评分、状态、时间）
  - 商品信息（标题、图片、价格）
  - 用户信息（用户名、头像、信用评分、历史评价数）
  - 评价内容（文字、图片10张、视频1个）
  - 回复列表（卖家回复、管理员回复、追评）
  - 互动数据（点赞数、举报数、点赞用户、举报原因）
  - 审核操作（标记违规、删除、审核意见）
- [ ] **ReviewMediaGallery.tsx**: 评价图片视频组件
- [ ] **ReviewReplies.tsx**: 回复列表组件
- [ ] **ReviewAuditForm.tsx**: 审核表单组件

**第3步：编写Service** 🟢
- [ ] 扩展 **reviewService.ts**（@campus/shared/services/）
  - `listReviews(params)` - 评价列表（管理员视角）
  - `getReviewDetail(id)` - 评价详情
  - `getReviewMedia(reviewId)` - 评价图片视频
  - `getReviewReplies(reviewId)` - 评价回复列表
  - `reviewReview(id, request)` - 审核评价
  - `updateReviewStatus(id, status)` - 更新评价状态
  - `deleteReview(id)` - 删除评价
  - `getReviewStatistics()` - 评价统计

**第4步：编写Hooks** 🟢
- [ ] **useReviewList.ts**: 评价列表Hook
- [ ] **useReviewDetail.ts**: 评价详情Hook
- [ ] **useReviewAudit.ts**: 评价审核Hook

**第5步：编写Store** 🟢（可选）
- [ ] 使用 `useQuery` 管理评价数据
- [ ] 使用 `useMutation` 管理评价审核

**第6步：配置路由** 🟢
- [ ] 添加路由：`/admin/reviews/list` → ReviewList
- [ ] 添加路由：`/admin/reviews/:id` → ReviewDetail
- [ ] 添加权限守卫：`SYSTEM_REVIEW_MANAGE`

**第7步：配置菜单** 🟢
- [ ] 添加菜单项：评价管理 → 评价列表
- [ ] 关联权限：`SYSTEM_REVIEW_MANAGE`

**第8步：运行测试** 🔵
- [ ] 运行单元测试：`npm test`
- [ ] 验证测试覆盖率 ≥85%

**第9步：重构优化** 🔵
- [ ] 提取通用组件
- [ ] 优化图片视频加载
- [ ] 代码审查

---

### T6: 批量操作管理模块开发 ⚡ P1

**需求编号**: 117  
**工作量**: 3天  
**优先级**: P1（重要功能）

#### 开发任务清单

**第0步：复用检查** ✅
- [x] 检查batchService是否存在
- [x] 检查BatchTask相关TypeScript类型定义

**第1步：编写测试** 🔴
- [ ] **BatchTaskList.test.tsx**: 测试任务列表
- [ ] **BatchTaskProgress.test.tsx**: 测试任务进度
- [ ] **BatchTaskCreate.test.tsx**: 测试任务创建
- [ ] **batchService.test.ts**: 测试API调用

**第2步：编写组件** 🟢
- [ ] **BatchTaskList.tsx**: 批量任务列表页
  - 状态筛选（待执行、执行中、已完成、已失败）
  - 类型筛选（商品上下架、价格调整、库存更新、用户通知）
  - 统计卡片（总数、执行中、今日完成、今日失败）
  - 任务表格（20条/页）
- [ ] **BatchTaskProgress.tsx**: 批量任务进度页
  - 任务基本信息（ID、类型、状态、时间）
  - 进度条（动态更新，每3秒刷新）
  - 任务统计（总数、成功、失败、剩余、进度百分比）
  - 任务日志表格（时间、操作类型、目标ID、结果、失败原因）
  - 操作按钮（取消、重试、导出）
- [ ] **BatchTaskCreate.tsx**: 批量任务创建页
  - 任务类型选择器（单选）
  - 目标选择器（手动输入、文件上传、条件筛选）
  - 任务参数表单（动态表单，根据类型变化）
  - 任务预览（显示目标数量、前10条预览、预估时间）
  - 创建按钮
- [ ] **BatchTaskProgress Component.tsx**: 进度条组件（可复用）
- [ ] **TaskLogTable.tsx**: 任务日志表格组件

**第3步：编写Service** 🟢
- [ ] 创建 **batchService.ts**（@campus/shared/services/）（如果不存在）
  - `listTasks(params)` - 任务列表
  - `getTaskDetail(id)` - 任务详情
  - `getTaskProgress(id)` - 任务进度（轮询）
  - `getTaskLogs(id)` - 任务日志
  - `createTask(request)` - 创建任务
  - `cancelTask(id)` - 取消任务
  - `retryTask(id)` - 重试失败项
  - `deleteTask(id)` - 删除任务

**第4步：编写Hooks** 🟢
- [ ] **useBatchTaskList.ts**: 任务列表Hook
- [ ] **useBatchTaskProgress.ts**: 任务进度Hook（轮询）
- [ ] **useBatchTaskCreate.ts**: 任务创建Hook

**第5步：编写Store** 🟢（可选）
- [ ] 使用 `useQuery` 管理任务数据（轮询进度）
- [ ] 使用 `useMutation` 管理任务创建、取消、重试

**第6步：配置路由** 🟢
- [ ] 添加路由：`/admin/batch/tasks` → BatchTaskList
- [ ] 添加路由：`/admin/batch/tasks/:id` → BatchTaskProgress
- [ ] 添加路由：`/admin/batch/create` → BatchTaskCreate
- [ ] 添加权限守卫：`SYSTEM_BATCH_OPERATION`

**第7步：配置菜单** 🟢
- [ ] 添加菜单项：批量操作 → 任务列表、创建任务
- [ ] 关联权限：`SYSTEM_BATCH_OPERATION`

**第8步：运行测试** 🔵
- [ ] 运行单元测试：`npm test`
- [ ] 验证测试覆盖率 ≥85%

**第9步：重构优化** 🔵
- [ ] 提取通用组件
- [ ] 优化轮询性能
- [ ] 代码审查

---

### T7: 日志管理模块开发 ⚡ P2

**需求编号**: 118  
**工作量**: 1天  
**优先级**: P2（常用功能）

#### 开发任务清单

**第0步：复用检查** ✅
- [x] 检查auditLogService是否存在
- [x] 检查AuditLog相关TypeScript类型定义

**第1步：编写测试** 🔴
- [ ] **AuditLogList.test.tsx**: 测试审计日志列表
- [ ] **OperationLogList.test.tsx**: 测试操作日志列表

**第2步：编写组件** 🟢
- [ ] **AuditLogList.tsx**: 审计日志页
  - 时间范围筛选（今日、近7天、近30天、自定义）
  - 操作类型筛选（登录登出、用户管理、商品管理等）
  - 用户筛选（用户名搜索）
  - 日志表格（20条/页）
  - 导出功能（CSV/Excel）
- [ ] **OperationLogList.tsx**: 操作日志页
  - 日志级别筛选（全部、INFO、WARN、ERROR）
  - 关键词搜索（模糊匹配）
  - 日志表格（ERROR级别红色高亮）

**第3步：编写Service** 🟢
- [ ] 扩展 **auditLogService.ts**（@campus/shared/services/）
  - `listAuditLogs(params)` - 审计日志查询
  - `getAuditLogDetail(id)` - 审计日志详情
  - `exportAuditLogs(params)` - 导出审计日志
- [ ] 创建 **systemLogService.ts**（如果后端提供）
  - `listSystemLogs(params)` - 操作日志查询

**第4步：编写Hooks** 🟢
- [ ] **useAuditLogList.ts**: 审计日志Hook
- [ ] **useOperationLogList.ts**: 操作日志Hook

**第5步：编写Store** 🟢（可选）
- [ ] 使用 `useQuery` 管理日志数据

**第6步：配置路由** 🟢
- [ ] 添加路由：`/admin/logs/audit` → AuditLogList
- [ ] 添加路由：`/admin/logs/operation` → OperationLogList
- [ ] 添加权限守卫：`SYSTEM_AUDIT_VIEW`

**第7步：配置菜单** 🟢
- [ ] 菜单项已存在：日志管理 → 审计日志、操作日志

**第8步：运行测试** 🔵
- [ ] 运行单元测试：`npm test`
- [ ] 验证测试覆盖率 ≥85%

**第9步：重构优化** 🔵
- [ ] 优化导出功能
- [ ] 代码审查

---

### T8: 封禁记录管理开发 ⚡ P2

**需求编号**: 119  
**工作量**: 1天  
**优先级**: P2（常用功能）

#### 开发任务清单

**第0步：复用检查** ✅
- [x] 检查userService中是否有封禁相关API

**第1步：编写测试** 🔴
- [ ] **BannedUserList.test.tsx**: 测试封禁记录列表、解封

**第2步：编写组件** 🟢
- [ ] **BannedUserList.tsx**: 封禁记录页
  - 状态筛选（全部、封禁中、已解封）
  - 用户搜索（用户名）
  - 统计卡片（总数、封禁中、今日解封）
  - 封禁表格（20条/页）
  - 解封操作（弹窗填写解封原因）

**第3步：编写Service** 🟢
- [ ] 扩展 **userService.ts**（@campus/shared/services/）
  - `listBannedUsers(params)` - 封禁记录查询
  - `unbanUser(id, reason)` - 解封用户
  - `getUserBanHistory(id)` - 用户封禁历史

**第4步：编写Hooks** 🟢
- [ ] **useBannedUserList.ts**: 封禁记录Hook

**第5步：编写Store** 🟢（可选）
- [ ] 使用 `useQuery` 管理封禁记录数据

**第6步：配置路由** 🟢
- [ ] 添加路由：`/admin/users/banned` → BannedUserList
- [ ] 添加权限守卫：`SYSTEM_USER_BAN`

**第7步：配置菜单** 🟢
- [ ] 更新菜单：用户管理 → 添加"封禁记录"子项

**第8步：运行测试** 🔵
- [ ] 运行单元测试：`npm test`
- [ ] 验证测试覆盖率 ≥85%

**第9步：重构优化** 🔵
- [ ] 代码审查

---

### T9: 帖子审核管理开发 ⚡ P2

**需求编号**: 120  
**工作量**: 1天  
**优先级**: P2（常用功能）

#### 开发任务清单

**第0步：复用检查** ✅
- [x] 检查postService是否存在

**第1步：编写测试** 🔴
- [ ] **PostAuditList.test.tsx**: 测试帖子审核列表

**第2步：编写组件** 🟢
- [ ] **PostAuditList.tsx**: 帖子审核页
  - 状态筛选（全部、待审核、已发布、已拒绝）
  - 统计卡片（总数、待审核、已发布、已拒绝）
  - 帖子表格（20条/页）
  - 帖子详情弹窗（标题、内容、图片、作者）
  - 审核表单（批准/拒绝、审核意见）

**第3步：编写Service** 🟢
- [ ] 扩展 **postService.ts**（@campus/shared/services/）
  - `listPosts(params)` - 帖子列表（管理员视角）
  - `listPendingPosts(params)` - 待审核帖子
  - `getPostDetail(id)` - 帖子详情
  - `approvePost(id, approved, reason)` - 审核帖子

**第4步：编写Hooks** 🟢
- [ ] **usePostAuditList.ts**: 帖子审核Hook

**第5步：编写Store** 🟢（可选）
- [ ] 使用 `useQuery` 管理帖子数据

**第6步：配置路由** 🟢
- [ ] 添加路由：`/admin/content/posts` → PostAuditList
- [ ] 添加权限守卫：`SYSTEM_POST_APPROVE`

**第7步：配置菜单** 🟢
- [ ] 更新菜单：内容管理 → 添加"帖子审核"子项（已存在）

**第8步：运行测试** 🔵
- [ ] 运行单元测试：`npm test`
- [ ] 验证测试覆盖率 ≥85%

**第9步：重构优化** 🔵
- [ ] 代码审查

---

## 📅 开发计划

### Phase 1: 核心业务（10天）⚡ P0

| 周 | 任务 | 负责人 | 工作量 |
|----|------|--------|--------|
| Week 1 | T1 商品管理 + T2 订单管理（部分） | 开发者A + 开发者B | 5天 |
| Week 2 | T2 订单管理（完成） + T3 申诉管理 + T7 日志管理 + T8 封禁记录 + T9 帖子审核 | 开发者A + 开发者B | 5天 |

### Phase 2: 高级功能（11天）⚡ P1

| 周 | 任务 | 负责人 | 工作量 |
|----|------|--------|--------|
| Week 3 | T4 纠纷仲裁 + T5 评价管理（部分） | 开发者A + 开发者B | 5天 |
| Week 4 | T5 评价管理（完成） + T6 批量操作 | 开发者A + 开发者B | 6天 |

### 总计: 21天（2人团队）

---

## ✅ 质量标准

### 测试标准
- ✅ 单元测试覆盖率 ≥85%
- ✅ 所有组件都有单元测试
- ✅ 所有Service都有Mock测试
- ✅ 所有Hooks都有逻辑测试

### 代码标准
- ✅ TypeScript无错误（0 errors）
- ✅ ESLint无警告（0 warnings）
- ✅ 代码格式化（Prettier）
- ✅ 组件复用率 ≥70%

### 性能标准
- ✅ 首屏加载 <2s
- ✅ 页面切换 <500ms
- ✅ API响应 <300ms（P95）
- ✅ 图片懒加载

### 安全标准
- ✅ 所有页面需权限验证
- ✅ 所有API请求需Token认证
- ✅ XSS防护（输入转义）
- ✅ CSRF防护（Token验证）

---

## 📖 参考资源

- [需求文档](./requirements.md)
- [设计文档](./design.md)
- [前端管理端分析报告](../../FRONTEND_ADMIN_ANALYSIS.md)
- [Ant Design 组件](https://ant.design/components/overview-cn)
- [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/)

---

**文档版本**: v1.0  
**创建日期**: 2025-11-05  
**作者**: BaSui 😎
