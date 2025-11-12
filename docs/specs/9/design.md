# Spec #9: 前端管理端设计文档

> **版本**: v1.0  
> **创建日期**: 2025-11-05  
> **作者**: BaSui 😎  
> **状态**: 📝 规划中

---

## 📋 目录

- [架构设计](#架构设计)
- [技术选型](#技术选型)
- [组件设计](#组件设计)
- [状态管理](#状态管理)
- [路由设计](#路由设计)
- [API集成](#api集成)
- [性能优化](#性能优化)
- [安全设计](#安全设计)

---

## 🏗️ 架构设计

### 整体架构

```
frontend/packages/admin/
├── src/
│   ├── pages/               # 页面组件（9个功能模块）
│   │   ├── Goods/          # 商品管理
│   │   ├── Orders/         # 订单管理
│   │   ├── Appeals/        # 申诉管理
│   │   ├── Disputes/       # 纠纷仲裁
│   │   ├── Reviews/        # 评价管理
│   │   ├── Batch/          # 批量操作
│   │   ├── Logs/           # 日志管理
│   │   └── Users/          # 用户管理（已有）
│   │
│   ├── components/          # 业务组件
│   │   ├── GoodsAuditForm/ # 商品审核表单
│   │   ├── OrderStatusTimeline/ # 订单状态时间线
│   │   ├── DisputeEvidenceViewer/ # 纠纷证据查看器
│   │   ├── BatchTaskProgress/ # 批量任务进度
│   │   └── ...
│   │
│   ├── hooks/              # 自定义Hooks
│   │   ├── useGoodsList.ts # 商品列表Hook
│   │   ├── useOrderDetail.ts # 订单详情Hook
│   │   └── ...
│   │
│   ├── stores/             # 状态管理
│   │   ├── auth.ts         # 认证状态（已有）
│   │   ├── goods.ts        # 商品状态
│   │   ├── orders.ts       # 订单状态
│   │   └── ...
│   │
│   ├── router/             # 路由配置
│   │   └── index.tsx       # 路由定义
│   │
│   └── config/             # 配置文件
│       └── menu.ts         # 菜单配置
│
└── package.json
```

### 分层架构

```
┌─────────────────────────────────────┐
│         Presentation Layer          │  页面组件（Pages）
│   ┌─────────┐  ┌─────────┐         │
│   │ Goods   │  │ Orders  │  ...    │
│   └─────────┘  └─────────┘         │
└──────────────┬──────────────────────┘
               │
┌──────────────┴──────────────────────┐
│       Business Logic Layer          │  业务组件 + Hooks
│   ┌──────────────┐  ┌─────────────┐│
│   │ Components   │  │   Hooks     ││
│   └──────────────┘  └─────────────┘│
└──────────────┬──────────────────────┘
               │
┌──────────────┴──────────────────────┐
│         Data Access Layer           │  Services + API
│   ┌──────────────┐  ┌─────────────┐│
│   │  Services    │  │     API     ││
│   │ (@shared)    │  │  (Auto-gen) ││
│   └──────────────┘  └─────────────┘│
└─────────────────────────────────────┘
```

### 数据流

```
User Action → Component Event → Hook Logic → Service Call → API Request
                                                                ↓
User UI ← Component Update ← Hook State ← Service Response ← API Response
```

---

## 🔧 技术选型

### 核心技术栈

| 技术 | 版本 | 用途 | 理由 |
|------|------|------|------|
| **React** | 18.3.1 | UI框架 | 成熟稳定，生态丰富 |
| **TypeScript** | 5.5.3 | 类型系统 | 类型安全，减少Bug |
| **Vite** | 5.4.1 | 构建工具 | 极速构建，HMR快速 |
| **Ant Design** | 5.27.6 | UI组件库 | 企业级组件，开箱即用 |
| **Zustand** | 4.5.7 | 状态管理 | 轻量简洁，TypeScript友好 |
| **TanStack Query** | 5.51.0 | 数据请求 | 缓存、轮询、乐观更新 |
| **React Router** | 6.26.0 | 路由 | 声明式路由，嵌套路由 |
| **Axios** | 1.7.2 | HTTP客户端 | 请求拦截、错误处理 |
| **ECharts** | 5.4.3 | 图表 | 功能强大，交互友好 |
| **Day.js** | 1.11.12 | 日期处理 | 轻量级，Moment替代品 |

### 工具库

| 工具 | 版本 | 用途 |
|------|------|------|
| **@testing-library/react** | 最新 | 单元测试 |
| **Vitest** | 最新 | 测试运行器 |
| **ESLint** | 9.9.0 | 代码检查 |
| **Prettier** | 最新 | 代码格式化 |

---

## 🎨 组件设计

### 1. 商品管理组件

#### GoodsList - 商品列表页
```tsx
<GoodsList>
  <GoodsSearchBar />          // 搜索栏（关键词、分类、价格、状态）
  <GoodsBatchActions />       // 批量操作按钮
  <GoodsTable>                // 商品表格
    <Pagination />            // 分页器
  </GoodsTable>
</GoodsList>
```

#### GoodsAuditForm - 商品审核表单
```tsx
<GoodsAuditForm goodsId={id}>
  <GoodsInfo />               // 商品信息展示
  <AuditActions>              // 审核操作
    <ApproveButton />         // 批准
    <RejectButton />          // 拒绝
    <AuditCommentInput />     // 审核意见
  </AuditActions>
</GoodsAuditForm>
```

#### GoodsDetail - 商品详情页
```tsx
<GoodsDetail goodsId={id}>
  <GoodsBasicInfo />          // 基本信息卡片
  <GoodsImageGallery />       // 图片轮播
  <SellerInfo />              // 卖家信息卡片
  <AuditHistory />            // 审核记录时间线
  <GoodsActions />            // 操作按钮组
</GoodsDetail>
```

### 2. 订单管理组件

#### OrderList - 订单列表页
```tsx
<OrderList>
  <OrderSearchBar />          // 搜索栏
  <OrderStatusFilter />       // 状态筛选
  <OrderStatCards />          // 统计卡片
  <OrderTable>                // 订单表格
    <Pagination />
  </OrderTable>
</OrderList>
```

#### OrderDetail - 订单详情页
```tsx
<OrderDetail orderNo={orderNo}>
  <OrderInfoCard />           // 订单信息
  <GoodsInfoCard />           // 商品信息
  <BuyerInfoCard />           // 买家信息
  <SellerInfoCard />          // 卖家信息
  <PaymentInfoCard />         // 支付信息
  <LogisticsInfoCard />       // 物流信息
  <OrderStatusTimeline />     // 订单状态时间线
  <OrderActions />            // 操作按钮
</OrderDetail>
```

#### RefundManagement - 退款管理页
```tsx
<RefundManagement>
  <RefundList>                // 退款列表
    <RefundItem>
      <RefundInfo />          // 退款信息
      <RefundAuditForm />     // 审核表单
    </RefundItem>
  </RefundList>
</RefundManagement>
```

### 3. 申诉管理组件

#### AppealList - 申诉列表页
```tsx
<AppealList>
  <AppealSearchBar />         // 搜索栏
  <AppealTypeFilter />        // 类型筛选
  <AppealStatusFilter />      // 状态筛选
  <AppealTable>               // 申诉表格
    <Pagination />
  </AppealTable>
</AppealList>
```

#### AppealDetail - 申诉详情页
```tsx
<AppealDetail appealId={id}>
  <AppealBasicInfo />         // 申诉基本信息
  <AppealerInfo />            // 申诉人信息
  <AppealContent />           // 申诉内容
  <AppealMaterials />         // 申诉材料
  <RelatedInfo />             // 关联信息
  <AppealAuditForm />         // 审核表单
  <AppealHistory />           // 审核历史
</AppealDetail>
```

### 4. 纠纷仲裁组件

#### DisputeList - 纠纷列表页
```tsx
<DisputeList>
  <DisputeSearchBar />        // 搜索栏
  <DisputeStatusFilter />     // 状态筛选
  <DisputeTable>              // 纠纷表格
    <Pagination />
  </DisputeTable>
</DisputeList>
```

#### DisputeDetail - 纠纷详情页
```tsx
<DisputeDetail disputeId={id}>
  <DisputeBasicInfo />        // 纠纷基本信息
  <OrderInfoCard />           // 订单信息
  <BuyerClaimCard />          // 买家申诉
  <SellerReplyCard />         // 卖家回复
  <EvidenceMaterialsCard />   // 证据材料
  <NegotiationTimeline />     // 协商记录时间线
  <ArbitrationDecisionForm /> // 仲裁决策表单
</DisputeDetail>
```

#### DisputeEvidenceViewer - 证据查看器
```tsx
<DisputeEvidenceViewer disputeId={id}>
  <EvidenceList>              // 证据列表
    <EvidenceItem>
      <EvidenceImage />       // 图片预览
      <EvidenceFile />        // 文件下载
      <EvidenceHashVerify />  // 哈希验证
    </EvidenceItem>
  </EvidenceList>
</DisputeEvidenceViewer>
```

#### DisputeStatistics - 纠纷统计页
```tsx
<DisputeStatistics>
  <DisputeTrendChart />       // 纠纷趋势图（折线图）
  <ArbitrationResultChart />  // 仲裁结果分析（饼图）
  <DisputeTypeDistribution /> // 纠纷类型分布（饼图）
  <ArbitratorPerformance />   // 仲裁员绩效（表格）
</DisputeStatistics>
```

### 5. 评价管理组件

#### ReviewList - 评价列表页
```tsx
<ReviewList>
  <ReviewSearchBar />         // 搜索栏
  <ReviewRatingFilter />      // 评分筛选
  <ReviewStatusFilter />      // 状态筛选
  <ReviewTable>               // 评价表格
    <Pagination />
  </ReviewTable>
</ReviewList>
```

#### ReviewDetail - 评价详情页
```tsx
<ReviewDetail reviewId={id}>
  <ReviewBasicInfo />         // 评价基本信息
  <GoodsInfo />               // 商品信息
  <UserInfo />                // 用户信息
  <ReviewContent />           // 评价内容
  <ReviewMediaGallery />      // 图片视频
  <ReviewReplies />           // 回复列表
  <ReviewInteraction />       // 互动数据（点赞、举报）
  <ReviewAuditForm />         // 审核表单
</ReviewDetail>
```

### 6. 批量操作组件

#### BatchTaskList - 批量任务列表页
```tsx
<BatchTaskList>
  <BatchTaskSearchBar />      // 搜索栏
  <BatchTaskStatusFilter />   // 状态筛选
  <BatchTaskTable>            // 任务表格
    <Pagination />
  </BatchTaskTable>
</BatchTaskList>
```

#### BatchTaskProgress - 批量任务进度页
```tsx
<BatchTaskProgress taskId={id}>
  <TaskBasicInfo />           // 任务基本信息
  <TaskProgressBar />         // 进度条（动态更新）
  <TaskStatistics />          // 任务统计（总数、成功、失败）
  <TaskLogTable />            // 任务日志表格
  <TaskActions />             // 操作按钮（取消、重试、导出）
</BatchTaskProgress>
```

#### BatchTaskCreate - 批量任务创建页
```tsx
<BatchTaskCreate>
  <TaskTypeSelector />        // 任务类型选择
  <TargetSelector>            // 目标选择
    <ManualInput />           // 手动输入
    <FileUpload />            // 文件上传
    <ConditionFilter />       // 条件筛选
  </TargetSelector>
  <TaskParamsForm />          // 任务参数配置
  <TaskPreview />             // 任务预览
  <CreateButton />            // 创建按钮
</BatchTaskCreate>
```

### 7. 日志管理组件

#### AuditLogList - 审计日志页
```tsx
<AuditLogList>
  <AuditLogSearchBar />       // 搜索栏
  <TimeRangeFilter />         // 时间范围筛选
  <OperationTypeFilter />     // 操作类型筛选
  <AuditLogTable>             // 日志表格
    <Pagination />
  </AuditLogTable>
  <ExportButton />            // 导出按钮
</AuditLogList>
```

#### OperationLogList - 操作日志页
```tsx
<OperationLogList>
  <LogLevelFilter />          // 日志级别筛选
  <LogSearchBar />            // 搜索栏
  <LogTable>                  // 日志表格
    <HighlightedErrorRow />   // 错误日志高亮
  </LogTable>
</OperationLogList>
```

### 8. 封禁记录组件

#### BannedUserList - 封禁记录页
```tsx
<BannedUserList>
  <BannedStatusFilter />      // 状态筛选
  <BannedSearchBar />         // 搜索栏
  <BannedTable>               // 封禁表格
    <UnbanAction />           // 解封操作
  </BannedTable>
</BannedUserList>
```

### 9. 帖子审核组件

#### PostAuditList - 帖子审核页
```tsx
<PostAuditList>
  <PostStatusFilter />        // 状态筛选
  <PostTable>                 // 帖子表格
    <PostAuditModal />        // 审核弹窗
  </PostTable>
</PostAuditList>
```

---

## 🗄️ 状态管理

### Zustand Store设计

#### goodsStore - 商品状态
```typescript
interface GoodsStore {
  // 状态
  goodsList: GoodsResponse[];
  currentGoods: GoodsDetailResponse | null;
  pendingGoods: GoodsResponse[];
  loading: boolean;
  error: string | null;
  
  // 操作
  fetchGoodsList: (params: GoodsListParams) => Promise<void>;
  fetchGoodsDetail: (id: number) => Promise<void>;
  fetchPendingGoods: () => Promise<void>;
  approveGoods: (id: number, request: ApproveGoodsRequest) => Promise<void>;
  rejectGoods: (id: number, reason: string) => Promise<void>;
  updateGoodsStatus: (id: number, status: string) => Promise<void>;
  deleteGoods: (id: number) => Promise<void>;
  batchUpdateGoods: (ids: number[], action: string) => Promise<void>;
}
```

#### ordersStore - 订单状态
```typescript
interface OrdersStore {
  // 状态
  ordersList: OrderResponse[];
  currentOrder: OrderResponse | null;
  refundList: RefundResponse[];
  loading: boolean;
  
  // 操作
  fetchOrdersList: (params: OrderListParams) => Promise<void>;
  fetchOrderDetail: (orderNo: string) => Promise<void>;
  cancelOrder: (orderNo: string, reason: string) => Promise<void>;
  fetchRefundList: () => Promise<void>;
  approveRefund: (refundId: number, approved: boolean) => Promise<void>;
}
```

#### appealsStore - 申诉状态
```typescript
interface AppealsStore {
  // 状态
  appealsList: Appeal[];
  currentAppeal: AppealDetailResponse | null;
  pendingAppeals: Appeal[];
  
  // 操作
  fetchAppealsList: (params: AppealListParams) => Promise<void>;
  fetchAppealDetail: (id: number) => Promise<void>;
  reviewAppeal: (request: ReviewRequest) => Promise<void>;
  batchReviewAppeals: (request: BatchReviewRequest) => Promise<void>;
}
```

#### disputesStore - 纠纷状态
```typescript
interface DisputesStore {
  // 状态
  disputesList: DisputeDTO[];
  currentDispute: DisputeDetailDTO | null;
  evidenceList: EvidenceDTO[];
  negotiationMessages: NegotiationMessageDTO[];
  statistics: DisputeStatistics | null;
  
  // 操作
  fetchDisputesList: (params: DisputeListParams) => Promise<void>;
  fetchDisputeDetail: (id: number) => Promise<void>;
  assignArbitrator: (disputeId: number, arbitratorId: number) => Promise<void>;
  submitArbitration: (request: ArbitrateDisputeRequest) => Promise<void>;
  fetchEvidenceList: (disputeId: number) => Promise<void>;
  fetchNegotiationMessages: (disputeId: number) => Promise<void>;
  fetchStatistics: () => Promise<void>;
}
```

#### batchStore - 批量操作状态
```typescript
interface BatchStore {
  // 状态
  tasksList: BatchTaskResponse[];
  currentTask: BatchTaskResponse | null;
  taskProgress: BatchTaskProgressResponse | null;
  
  // 操作
  fetchTasksList: (params: BatchTaskListParams) => Promise<void>;
  fetchTaskDetail: (id: number) => Promise<void>;
  fetchTaskProgress: (id: number) => Promise<void>;
  createTask: (request: CreateBatchTaskRequest) => Promise<number>;
  cancelTask: (id: number) => Promise<void>;
  retryTask: (id: number) => Promise<void>;
}
```

### TanStack Query使用

对于需要缓存、轮询、乐观更新的场景，使用TanStack Query：

```typescript
// 商品列表查询（缓存5分钟）
const { data, isLoading, error, refetch } = useQuery({
  queryKey: ['goods', 'list', params],
  queryFn: () => goodsService.listGoods(params),
  staleTime: 5 * 60 * 1000, // 5分钟
});

// 批量任务进度（每3秒轮询）
const { data: progress } = useQuery({
  queryKey: ['batch', 'progress', taskId],
  queryFn: () => batchService.getTaskProgress(taskId),
  refetchInterval: 3000, // 3秒轮询
  enabled: taskStatus === 'RUNNING', // 仅执行中时轮询
});

// 审核商品（乐观更新）
const mutation = useMutation({
  mutationFn: (request: ApproveGoodsRequest) => goodsService.approveGoods(request),
  onMutate: async (request) => {
    // 乐观更新UI
    await queryClient.cancelQueries({ queryKey: ['goods', 'pending'] });
    const previousData = queryClient.getQueryData(['goods', 'pending']);
    queryClient.setQueryData(['goods', 'pending'], (old: any) => {
      return old.filter((g: GoodsResponse) => g.id !== request.goodsId);
    });
    return { previousData };
  },
  onError: (err, variables, context) => {
    // 回滚
    queryClient.setQueryData(['goods', 'pending'], context?.previousData);
  },
  onSuccess: () => {
    // 刷新列表
    queryClient.invalidateQueries({ queryKey: ['goods', 'pending'] });
  },
});
```

---

## 🛣️ 路由设计

### 路由结构

```typescript
const routes = [
  {
    path: '/admin',
    element: <AdminLayout />,
    children: [
      // ===== 仪表盘 =====
      { path: 'dashboard', element: <Dashboard /> },
      
      // ===== 商品管理 =====
      { path: 'goods/list', element: <GoodsList /> },
      { path: 'goods/:id', element: <GoodsDetail /> },
      { path: 'content/goods', element: <GoodsAudit /> }, // 商品审核
      
      // ===== 订单管理 =====
      { path: 'orders/list', element: <OrderList /> },
      { path: 'orders/:orderNo', element: <OrderDetail /> },
      { path: 'orders/refunds', element: <RefundManagement /> },
      
      // ===== 申诉管理 =====
      { path: 'appeals/list', element: <AppealList /> },
      { path: 'appeals/:id', element: <AppealDetail /> },
      
      // ===== 纠纷仲裁 =====
      { path: 'disputes/list', element: <DisputeList /> },
      { path: 'disputes/:id', element: <DisputeDetail /> },
      { path: 'disputes/:id/evidence', element: <DisputeEvidence /> },
      { path: 'disputes/statistics', element: <DisputeStatistics /> },
      
      // ===== 评价管理 =====
      { path: 'reviews/list', element: <ReviewList /> },
      { path: 'reviews/:id', element: <ReviewDetail /> },
      
      // ===== 批量操作 =====
      { path: 'batch/tasks', element: <BatchTaskList /> },
      { path: 'batch/tasks/:id', element: <BatchTaskProgress /> },
      { path: 'batch/create', element: <BatchTaskCreate /> },
      
      // ===== 日志管理 =====
      { path: 'logs/audit', element: <AuditLogList /> },
      { path: 'logs/operation', element: <OperationLogList /> },
      
      // ===== 用户管理 =====
      { path: 'users/list', element: <UserList /> }, // 已有
      { path: 'users/:id', element: <UserDetail /> }, // 已有
      { path: 'users/banned', element: <BannedUserList /> }, // 新增
      
      // ===== 内容管理 =====
      { path: 'content/reports', element: <ReportList /> }, // 已有
      { path: 'content/posts', element: <PostAudit /> }, // 新增
      
      // ===== 系统管理 =====（已有，不变）
      { path: 'system/rate-limit', element: <RateLimit /> },
      { path: 'system/recycle-bin', element: <RecycleBin /> },
      { path: 'system/notifications', element: <Notifications /> },
      { path: 'system/compliance', element: <Compliance /> },
      { path: 'system/revert', element: <RevertManagement /> },
      
      // ===== 角色权限 =====（已有，不变）
      { path: 'roles', element: <RoleList /> },
    ],
  },
  { path: '/admin/login', element: <Login /> },
];
```

### 权限路由

所有路由都需要权限保护：

```tsx
<PermissionGuard permission={PERMISSION_CODES.SYSTEM_GOODS_VIEW}>
  <GoodsList />
</PermissionGuard>
```

---

## 🔌 API集成

### Service层封装

所有API调用统一封装在 `@campus/shared/services/` 中：

```typescript
// goodsService.ts（扩展）
export const goodsService = {
  // 查询商品列表
  listGoods: async (params: GoodsListParams) => {
    const { data } = await apiClient.get('/api/goods', { params });
    return data.data;
  },
  
  // 查询待审核商品
  listPendingGoods: async (params: PaginationParams) => {
    const { data } = await apiClient.get('/api/goods/pending', { params });
    return data.data;
  },
  
  // 查询商品详情
  getGoodsDetail: async (id: number) => {
    const { data } = await apiClient.get(`/api/goods/${id}`);
    return data.data;
  },
  
  // 审核商品
  approveGoods: async (id: number, request: ApproveGoodsRequest) => {
    const { data } = await apiClient.post(`/api/goods/${id}/approve`, request);
    return data.data;
  },
  
  // 商品上下架
  updateGoodsStatus: async (id: number, status: string) => {
    const { data } = await apiClient.put(`/api/goods/${id}/status`, { status });
    return data.data;
  },
  
  // 删除商品
  deleteGoods: async (id: number) => {
    const { data } = await apiClient.delete(`/api/goods/${id}`);
    return data.data;
  },
  
  // 批量操作
  batchUpdateGoods: async (request: GoodsBatchRequest) => {
    const { data } = await apiClient.post('/api/goods/batch', request);
    return data.data;
  },
};
```

### API Client配置

```typescript
// apiClient.ts（已有，扩展）
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器 - 注入Token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器 - 统一错误处理
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token过期，跳转登录页
      localStorage.removeItem('token');
      window.location.href = '/admin/login';
    } else if (error.response?.status === 403) {
      // 无权限
      message.error('无权限访问');
    } else {
      message.error(error.response?.data?.message || '请求失败');
    }
    return Promise.reject(error);
  }
);
```

---

## ⚡ 性能优化

### 1. 代码分割

使用React.lazy实现路由懒加载：

```typescript
const GoodsList = lazy(() => import('@/pages/Goods/GoodsList'));
const OrderList = lazy(() => import('@/pages/Orders/OrderList'));
// ...
```

### 2. 图片懒加载

使用Ant Design Image组件的懒加载功能：

```tsx
<Image src={url} lazy />
```

### 3. 虚拟滚动

对于长列表，使用Ant Design的虚拟滚动表格：

```tsx
<Table virtual scroll={{ y: 600 }} />
```

### 4. 请求防抖/节流

使用自定义Hooks：

```typescript
const debouncedSearch = useDebounce(searchKeyword, 500);
const throttledScroll = useThrottle(handleScroll, 200);
```

### 5. 数据缓存

使用TanStack Query缓存数据：

```typescript
const { data } = useQuery({
  queryKey: ['goods', 'list'],
  queryFn: () => goodsService.listGoods(),
  staleTime: 5 * 60 * 1000, // 缓存5分钟
});
```

### 6. 分页加载

所有列表页面支持分页，每页20条：

```tsx
<Table
  pagination={{
    current: page + 1,
    pageSize: 20,
    total: total,
    showSizeChanger: true,
    showQuickJumper: true,
  }}
/>
```

---

## 🔒 安全设计

### 1. 权限控制

所有页面和API调用都需要权限验证：

```tsx
// 页面级权限
<PermissionGuard permission={PERMISSION_CODES.SYSTEM_GOODS_VIEW}>
  <GoodsList />
</PermissionGuard>

// 组件级权限
{hasPermission(PERMISSION_CODES.SYSTEM_GOODS_APPROVE) && (
  <Button onClick={handleApprove}>审核</Button>
)}
```

### 2. Token管理

- Token存储在LocalStorage
- 每次请求自动注入Token
- Token过期自动跳转登录页
- 支持Token刷新机制

### 3. XSS防护

- 所有用户输入使用Ant Design组件（自动转义）
- 富文本编辑使用DOMPurify清理
- 禁止使用dangerouslySetInnerHTML

### 4. CSRF防护

- 所有POST/PUT/DELETE请求需要Token验证
- API请求使用HTTPS（生产环境）

### 5. 敏感数据保护

- 密码输入使用Input.Password（自动掩码）
- 手机号、身份证号脱敏显示
- 日志中不记录敏感信息

---

## 📊 监控与日志

### 1. 错误监控

使用Error Boundary捕获React错误：

```tsx
<ErrorBoundary>
  <App />
</ErrorBoundary>
```

### 2. 性能监控

使用Web Vitals监控性能指标：

```typescript
import { getCLS, getFID, getFCP, getLCP, getTTFB } from 'web-vitals';

getCLS(console.log);
getFID(console.log);
getFCP(console.log);
getLCP(console.log);
getTTFB(console.log);
```

### 3. 用户行为追踪

记录关键操作：

```typescript
const trackEvent = (event: string, params: any) => {
  console.log('User Event:', event, params);
  // 发送到后端或第三方平台
};
```

---

## 📖 参考文档

- [Ant Design 组件文档](https://ant.design/components/overview-cn)
- [React Router 文档](https://reactrouter.com/)
- [TanStack Query 文档](https://tanstack.com/query/latest)
- [Zustand 文档](https://github.com/pmndrs/zustand)
- [ECharts 文档](https://echarts.apache.org/)

---

**设计版本**: v1.0  
**创建日期**: 2025-11-05  
**作者**: BaSui 😎
