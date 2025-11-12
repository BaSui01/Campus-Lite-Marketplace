# Spec #10: 基础管理功能完善 - 设计文档

> **Spec编号**: #10  
> **作者**: BaSui 😎  
> **创建日期**: 2025-11-06  
> **状态**: 📝 规划中  
> **基于**: Spec #9 的成功经验

---

## 🎯 设计目标

### 核心原则

1. **最大化复用** - 基于现有模板（GoodsList），复用率≥90%
2. **统一风格** - 保持与现有24个管理页面一致的UI/UX
3. **高效开发** - 单页面开发时间≤2小时
4. **质量保证** - TypeScript类型安全 + 完整错误处理

### 设计策略

```
现有成功模式
    ↓
复制模板（5分钟）
    ↓
修改业务逻辑（30分钟）
    ↓
扩展Service（15分钟）
    ↓
配置路由菜单（10分钟）
    ↓
测试验证（10分钟）
```

**单页面总时间**: **1小时**  
**9个页面总时间**: **4天**（含测试和优化）

---

## 🏗️ 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────┐
│            前端管理端（Admin Package）              │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌──────────────┐  ┌──────────────┐            │
│  │ 路由层（Router）│  │ 菜单（Menu）  │            │
│  └──────┬───────┘  └──────┬───────┘            │
│         │                 │                    │
│  ┌──────▼─────────────────▼───────┐            │
│  │      页面组件层（Pages）          │            │
│  │  - CampusList                   │            │
│  │  - CategoryList                 │            │
│  │  - TagList                      │            │
│  │  - SystemMonitor                │            │
│  │  - TopicList                    │            │
│  │  - CommunityList                │            │
│  │  - FeatureFlagList              │            │
│  │  - TaskList                     │            │
│  │  - DisputeStatistics            │            │
│  └──────┬──────────────────────────┘            │
│         │                                       │
│  ┌──────▼──────────────────────────┐            │
│  │   通用组件层（Shared Components） │            │
│  │  - PermissionGuard              │            │
│  │  - SearchBar                    │            │
│  │  - StatisticsCard               │            │
│  │  - DataTable                    │            │
│  └──────┬──────────────────────────┘            │
│         │                                       │
│  ┌──────▼──────────────────────────┐            │
│  │   数据层（Services + TanStack）   │            │
│  │  - campusService                │            │
│  │  - categoryService              │            │
│  │  - tagService                   │            │
│  │  - monitorService               │            │
│  │  - topicService                 │            │
│  │  - communityService             │            │
│  │  - featureFlagService           │            │
│  │  - taskService                  │            │
│  │  - statisticsService            │            │
│  └─────────────────────────────────┘            │
│                                                 │
└─────────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────┐
│              后端API（已实现）                     │
├─────────────────────────────────────────────────┤
│  - CampusController        ✅                   │
│  - CategoryController      ✅                   │
│  - TagController           ✅                   │
│  - SystemMonitorController ✅                   │
│  - TopicController         ✅                   │
│  - CommunityController     ✅                   │
│  - FeatureFlagController   ✅                   │
│  - TaskController          ✅                   │
│  - DisputeStatisticsController ✅               │
└─────────────────────────────────────────────────┘
```

---

## 🧩 组件设计

### 1. 基础列表页组件（通用模板）

**模板来源**: `GoodsList.tsx`（已验证成功）

**组件结构**:
```tsx
const BaseListPage = () => {
  // 1. 状态管理
  const [searchParams, setSearchParams] = useState({})
  const [pagination, setPagination] = useState({ current: 1, pageSize: 20 })
  
  // 2. 数据查询（TanStack Query）
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['list', searchParams, pagination],
    queryFn: () => service.list(searchParams, pagination)
  })
  
  // 3. 渲染布局
  return (
    <div>
      {/* 统计卡片 */}
      <StatisticsCards data={statistics} />
      
      {/* 搜索筛选 */}
      <SearchBar onSearch={handleSearch} />
      
      {/* 数据表格 */}
      <Table
        dataSource={data?.list}
        columns={columns}
        pagination={pagination}
        loading={isLoading}
      />
    </div>
  )
}
```

**复用策略**:
- ✅ 统计卡片 - 100%复用
- ✅ 搜索筛选 - 90%复用（调整字段）
- ✅ 数据表格 - 90%复用（调整列定义）
- ✅ 操作按钮 - 100%复用
- ✅ 弹窗表单 - 90%复用（调整表单字段）

---

### 2. 九个页面组件设计

#### 2.1 校园管理（CampusList）

**复用模板**: GoodsList.tsx  
**修改内容**:
- 表格列：名称、代码、地址、电话、状态
- 搜索字段：名称、代码
- 统计卡片：总数、启用数、禁用数
- 操作：添加、编辑、删除、启用/禁用

**特殊功能**:
- 校园统计弹窗（用户数、商品数、订单数）

**预计工时**: 1小时

---

#### 2.2 分类管理（CategoryList）

**复用模板**: GoodsList.tsx  
**修改内容**:
- 树形表格（Ant Design Tree Table）
- 表格列：名称、图标、排序、层级、状态
- 操作：添加、编辑、删除、排序

**特殊功能**:
- 拖拽排序（React DnD Kit）
- 树形展开/收起

**预计工时**: 2小时（树形结构稍复杂）

---

#### 2.3 标签管理（TagList）

**复用模板**: GoodsList.tsx  
**修改内容**:
- 表格列：名称、类型、颜色、热度、状态
- 搜索字段：名称、类型
- 统计卡片：总数、热门标签数

**特殊功能**:
- 热度排行榜（TOP 20）
- 标签合并功能（批量操作）

**预计工时**: 1.5小时

---

#### 2.4 系统监控（SystemMonitor）

**复用模板**: Dashboard.tsx  
**修改内容**:
- 实时数据卡片（CPU、内存、JVM）
- API监控图表（ECharts）
- 慢接口TOP 10表格
- 错误日志列表

**特殊功能**:
- 实时更新（30秒轮询）
- 报警提示（WebSocket推送）
- ECharts图表（折线图、柱状图）

**技术选型**:
- **图表库**: Apache ECharts
- **实时更新**: TanStack Query refetchInterval: 30000
- **报警**: WebSocket + Notification组件

**预计工时**: 3小时（图表较多）

---

#### 2.5 话题管理（TopicList）

**复用模板**: GoodsList.tsx  
**修改内容**:
- 表格列：名称、描述、热度、开始/结束时间、状态
- 搜索字段：名称
- 统计卡片：总数、热门话题数、参与人数

**特殊功能**:
- 话题审核（批准/拒绝）
- 话题统计弹窗（帖子数、参与人数、浏览量）

**预计工时**: 1.5小时

---

#### 2.6 社区管理（CommunityList）

**复用模板**: GoodsList.tsx  
**修改内容**:
- 表格列：名称、类型、成员数、活跃度、状态
- 搜索字段：名称、类型
- 统计卡片：总数、活跃社区数、总成员数

**特殊功能**:
- 成员管理弹窗（成员列表、角色设置、禁言）
- 社区设置（审核开关、权限设置）

**预计工时**: 2小时

---

#### 2.7 功能开关（FeatureFlagList）

**复用模板**: GoodsList.tsx  
**修改内容**:
- 表格列：名称、Key、状态、策略、环境
- 搜索字段：名称、Key
- 统计卡片：总数、启用数、灰度中数

**特殊功能**:
- 灰度策略配置（用户/校园/百分比）
- 使用日志查看

**预计工时**: 1.5小时

---

#### 2.8 任务管理（TaskList）

**复用模板**: BatchTaskList.tsx（已有批量任务页面）  
**修改内容**:
- 表格列：名称、Cron表达式、状态、上次/下次执行时间
- 操作：启动、暂停、手动触发
- 统计卡片：总数、运行中、失败数

**特殊功能**:
- 执行日志抽屉（最近100条）
- 任务统计图表（成功率、耗时）

**预计工时**: 1.5小时

---

#### 2.9 纠纷统计（DisputeStatistics）

**复用模板**: Dashboard.tsx  
**修改内容**:
- 统计概览卡片（总数、处理中、成功率）
- 趋势图（ECharts折线图）
- 分类统计（饼图）
- 仲裁员排行（表格）

**特殊功能**:
- 时间范围选择（日/周/月）
- 数据导出（CSV/Excel）

**预计工时**: 2小时

---

## 📦 Service层设计

### Service扩展模式

**统一模式**（已验证成功）:
```typescript
// @campus/shared/services/campus.ts

export interface Campus {
  id: number
  name: string
  code: string
  address?: string
  phone?: string
  status: 'ENABLED' | 'DISABLED'
  createdAt: string
}

export interface CampusListParams {
  keyword?: string
  status?: string
  page: number
  size: number
}

export const campusService = {
  // 列表查询
  async list(params: CampusListParams) {
    return apiClient.get<PageResponse<Campus>>('/api/admin/campuses', { params })
  },
  
  // 添加
  async create(data: Omit<Campus, 'id' | 'createdAt'>) {
    return apiClient.post<Campus>('/api/admin/campuses', data)
  },
  
  // 编辑
  async update(id: number, data: Partial<Campus>) {
    return apiClient.put<Campus>(`/api/admin/campuses/${id}`, data)
  },
  
  // 删除
  async delete(id: number) {
    return apiClient.delete(`/api/admin/campuses/${id}`)
  },
  
  // 统计
  async statistics(id: number) {
    return apiClient.get<CampusStatistics>(`/api/admin/campuses/${id}/statistics`)
  }
}
```

### 需要创建的Service（9个）

| Service | 方法数 | 预计时间 |
|---------|--------|---------|
| campusService | 5个 | 15分钟 |
| categoryService | 6个（含排序） | 20分钟 |
| tagService | 6个（含合并） | 20分钟 |
| monitorService | 6个 | 20分钟 |
| topicService | 6个 | 20分钟 |
| communityService | 8个（含成员管理） | 30分钟 |
| featureFlagService | 5个 | 15分钟 |
| taskService | 7个 | 25分钟 |
| statisticsService | 5个 | 15分钟 |

**总计**: **3小时**（包含类型定义）

---

## 🎨 UI设计规范

### 统一布局模式

```tsx
<PageContainer>
  {/* 1. 页面标题 */}
  <PageHeader title="模块名称" />
  
  {/* 2. 统计卡片（4个） */}
  <Row gutter={16}>
    <Col span={6}><StatisticCard title="总数" value={100} /></Col>
    <Col span={6}><StatisticCard title="状态1" value={50} /></Col>
    <Col span={6}><StatisticCard title="状态2" value={30} /></Col>
    <Col span={6}><StatisticCard title="状态3" value={20} /></Col>
  </Row>
  
  {/* 3. 搜索筛选区 */}
  <Card style={{ marginTop: 16 }}>
    <Form layout="inline">
      <Form.Item label="搜索">
        <Input placeholder="请输入关键词" />
      </Form.Item>
      <Form.Item label="状态">
        <Select options={statusOptions} />
      </Form.Item>
      <Form.Item>
        <Button type="primary">查询</Button>
        <Button>重置</Button>
      </Form.Item>
    </Form>
  </Card>
  
  {/* 4. 操作按钮区 */}
  <div style={{ marginTop: 16, marginBottom: 16 }}>
    <Button type="primary" icon={<PlusOutlined />}>添加</Button>
    <Button>批量操作</Button>
  </div>
  
  {/* 5. 数据表格 */}
  <Table
    dataSource={data}
    columns={columns}
    pagination={{ current: 1, pageSize: 20, total: 100 }}
    loading={isLoading}
  />
</PageContainer>
```

### 统一色彩规范

| 类型 | 颜色 | 说明 |
|------|------|------|
| 主色调 | #1890ff | Ant Design蓝色 |
| 成功色 | #52c41a | 绿色 |
| 警告色 | #faad14 | 橙色 |
| 错误色 | #f5222d | 红色 |
| 灰色 | #8c8c8c | 次要文本 |

### 统一字体规范

| 类型 | 字号 | 说明 |
|------|------|------|
| 页面标题 | 24px | 加粗 |
| 卡片标题 | 16px | 加粗 |
| 正文 | 14px | 常规 |
| 辅助文本 | 12px | 灰色 |

---

## 🔄 数据流设计

### TanStack Query数据流

```
用户操作
    ↓
触发Query/Mutation
    ↓
调用Service方法
    ↓
发起HTTP请求
    ↓
后端API处理
    ↓
返回响应数据
    ↓
TanStack自动缓存
    ↓
组件自动更新
```

### 缓存策略

| 数据类型 | 缓存时间 | 刷新策略 |
|---------|---------|---------|
| 列表数据 | 2分钟 | 手动刷新 / 修改后自动 |
| 详情数据 | 5分钟 | 手动刷新 / 修改后自动 |
| 统计数据 | 30秒 | 自动轮询 |
| 监控数据 | 30秒 | 自动轮询 |

### 错误处理

```tsx
const { data, isLoading, error } = useQuery({
  queryKey: ['list'],
  queryFn: service.list,
  onError: (err) => {
    message.error(`查询失败: ${err.message}`)
  }
})

const mutation = useMutation({
  mutationFn: service.create,
  onSuccess: () => {
    message.success('添加成功')
    queryClient.invalidateQueries(['list']) // 刷新列表
  },
  onError: (err) => {
    message.error(`添加失败: ${err.message}`)
  }
})
```

---

## ⚡ 性能优化

### 1. 列表分页优化

```tsx
// 分页参数
const pagination = {
  current: 1,
  pageSize: 20,  // 固定20条/页
  showSizeChanger: false,  // 禁用页面大小切换
  showTotal: (total) => `共 ${total} 条`
}
```

### 2. 搜索防抖

```tsx
const debouncedSearch = useMemo(
  () => debounce((value) => {
    setSearchParams({ ...searchParams, keyword: value })
  }, 500),
  [searchParams]
)
```

### 3. 图片懒加载

```tsx
<Image
  src={imageUrl}
  lazy
  placeholder={<Spin />}
/>
```

### 4. 虚拟滚动（长列表）

```tsx
// 使用rc-virtual-list
<VirtualList
  data={longList}
  height={600}
  itemHeight={50}
  itemKey="id"
>
  {(item) => <ListItem data={item} />}
</VirtualList>
```

### 5. ECharts按需引入

```tsx
// 只引入需要的图表类型
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { use } from 'echarts/core'

use([
  LineChart,
  BarChart,
  PieChart,
  GridComponent,
  TooltipComponent,
  LegendComponent
])
```

---

## 🔒 权限控制

### 权限守卫

```tsx
// 所有管理页面都包裹PermissionGuard
<PermissionGuard permission={PERMISSION_CODES.SYSTEM_CAMPUS_MANAGE}>
  <CampusList />
</PermissionGuard>
```

### 权限码定义

```typescript
// @campus/shared/constants/permissions.ts

export const PERMISSION_CODES = {
  // 校园管理
  SYSTEM_CAMPUS_VIEW: 'system:campus:view',
  SYSTEM_CAMPUS_MANAGE: 'system:campus:manage',
  
  // 分类管理
  SYSTEM_CATEGORY_VIEW: 'system:category:view',
  SYSTEM_CATEGORY_MANAGE: 'system:category:manage',
  
  // 标签管理
  SYSTEM_TAG_VIEW: 'system:tag:view',
  SYSTEM_TAG_MANAGE: 'system:tag:manage',
  
  // 系统监控
  SYSTEM_MONITOR_VIEW: 'system:monitor:view',
  
  // 话题管理
  SYSTEM_TOPIC_VIEW: 'system:topic:view',
  SYSTEM_TOPIC_MANAGE: 'system:topic:manage',
  
  // 社区管理
  SYSTEM_COMMUNITY_VIEW: 'system:community:view',
  SYSTEM_COMMUNITY_MANAGE: 'system:community:manage',
  
  // 功能开关
  SYSTEM_FEATURE_MANAGE: 'system:feature:manage',
  
  // 任务管理
  SYSTEM_TASK_VIEW: 'system:task:view',
  SYSTEM_TASK_MANAGE: 'system:task:manage',
  
  // 纠纷统计
  SYSTEM_DISPUTE_STATISTICS: 'system:dispute:statistics',
}
```

---

## 📱 路由和菜单配置

### 路由配置

```tsx
// frontend/packages/admin/src/router/index.tsx

// 校园管理
{
  path: 'system/campuses',
  element: (
    <PermissionGuard permission={PERMISSION_CODES.SYSTEM_CAMPUS_MANAGE}>
      <CampusList />
    </PermissionGuard>
  ),
},

// 分类管理
{
  path: 'system/categories',
  element: (
    <PermissionGuard permission={PERMISSION_CODES.SYSTEM_CATEGORY_MANAGE}>
      <CategoryList />
    </PermissionGuard>
  ),
},

// 标签管理
{
  path: 'system/tags',
  element: (
    <PermissionGuard permission={PERMISSION_CODES.SYSTEM_TAG_MANAGE}>
      <TagList />
    </PermissionGuard>
  ),
},

// 系统监控
{
  path: 'system/monitor',
  element: (
    <PermissionGuard permission={PERMISSION_CODES.SYSTEM_MONITOR_VIEW}>
      <SystemMonitor />
    </PermissionGuard>
  ),
},

// 话题管理
{
  path: 'community/topics',
  element: (
    <PermissionGuard permission={PERMISSION_CODES.SYSTEM_TOPIC_MANAGE}>
      <TopicList />
    </PermissionGuard>
  ),
},

// 社区管理
{
  path: 'community/list',
  element: (
    <PermissionGuard permission={PERMISSION_CODES.SYSTEM_COMMUNITY_MANAGE}>
      <CommunityList />
    </PermissionGuard>
  ),
},

// 功能开关
{
  path: 'system/features',
  element: (
    <PermissionGuard permission={PERMISSION_CODES.SYSTEM_FEATURE_MANAGE}>
      <FeatureFlagList />
    </PermissionGuard>
  ),
},

// 任务管理
{
  path: 'system/tasks',
  element: (
    <PermissionGuard permission={PERMISSION_CODES.SYSTEM_TASK_MANAGE}>
      <TaskList />
    </PermissionGuard>
  ),
},

// 纠纷统计
{
  path: 'disputes/statistics',
  element: (
    <PermissionGuard permission={PERMISSION_CODES.SYSTEM_DISPUTE_STATISTICS}>
      <DisputeStatistics />
    </PermissionGuard>
  ),
},
```

### 菜单配置

```typescript
// frontend/packages/admin/src/config/menu.ts

export const MENU_ITEMS: MenuItem[] = [
  // ... 现有菜单
  
  {
    key: 'system',
    label: '系统管理',
    icon: 'SettingOutlined',
    children: [
      // ... 现有子菜单
      {
        key: 'system-campuses',
        label: '校园管理',
        path: '/admin/system/campuses',
        permission: PERMISSION_CODES.SYSTEM_CAMPUS_MANAGE,
      },
      {
        key: 'system-categories',
        label: '分类管理',
        path: '/admin/system/categories',
        permission: PERMISSION_CODES.SYSTEM_CATEGORY_MANAGE,
      },
      {
        key: 'system-tags',
        label: '标签管理',
        path: '/admin/system/tags',
        permission: PERMISSION_CODES.SYSTEM_TAG_MANAGE,
      },
      {
        key: 'system-features',
        label: '功能开关',
        path: '/admin/system/features',
        permission: PERMISSION_CODES.SYSTEM_FEATURE_MANAGE,
      },
      {
        key: 'system-tasks',
        label: '任务管理',
        path: '/admin/system/tasks',
        permission: PERMISSION_CODES.SYSTEM_TASK_MANAGE,
      },
      {
        key: 'system-monitor',
        label: '系统监控',
        path: '/admin/system/monitor',
        permission: PERMISSION_CODES.SYSTEM_MONITOR_VIEW,
      },
    ],
  },
  
  {
    key: 'community',
    label: '社区管理',
    icon: 'TeamOutlined',
    children: [
      {
        key: 'community-topics',
        label: '话题管理',
        path: '/admin/community/topics',
        permission: PERMISSION_CODES.SYSTEM_TOPIC_MANAGE,
      },
      {
        key: 'community-list',
        label: '社区列表',
        path: '/admin/community/list',
        permission: PERMISSION_CODES.SYSTEM_COMMUNITY_MANAGE,
      },
    ],
  },
  
  {
    key: 'disputes',
    label: '纠纷仲裁',
    icon: 'SafetyCertificateOutlined',
    children: [
      // ... 现有子菜单
      {
        key: 'disputes-statistics',
        label: '纠纷统计',
        path: '/admin/disputes/statistics',
        permission: PERMISSION_CODES.SYSTEM_DISPUTE_STATISTICS,
      },
    ],
  },
]
```

---

## 🧪 测试策略

### 单元测试

```tsx
// CampusList.test.tsx

describe('CampusList', () => {
  it('should render list correctly', () => {
    const { getByText } = render(<CampusList />)
    expect(getByText('校园管理')).toBeInTheDocument()
  })
  
  it('should search correctly', async () => {
    const { getByPlaceholderText, getByText } = render(<CampusList />)
    const searchInput = getByPlaceholderText('请输入校园名称')
    
    fireEvent.change(searchInput, { target: { value: '测试校园' } })
    fireEvent.click(getByText('查询'))
    
    await waitFor(() => {
      expect(campusService.list).toHaveBeenCalledWith({ keyword: '测试校园' })
    })
  })
})
```

### 集成测试

```tsx
// 测试完整流程：添加 → 编辑 → 删除
it('should complete CRUD flow', async () => {
  // 1. 添加
  fireEvent.click(getByText('添加校园'))
  // ... 填写表单
  fireEvent.click(getByText('保存'))
  await waitFor(() => {
    expect(getByText('添加成功')).toBeInTheDocument()
  })
  
  // 2. 编辑
  fireEvent.click(getByText('编辑'))
  // ... 修改表单
  fireEvent.click(getByText('保存'))
  await waitFor(() => {
    expect(getByText('编辑成功')).toBeInTheDocument()
  })
  
  // 3. 删除
  fireEvent.click(getByText('删除'))
  fireEvent.click(getByText('确认'))
  await waitFor(() => {
    expect(getByText('删除成功')).toBeInTheDocument()
  })
})
```

---

## 📊 开发计划

### Phase 1: 基础数据管理（2天）

**Day 1**:
- ✅ 校园管理（1小时）
- ✅ 分类管理（2小时）
- ✅ 标签管理（1.5小时）
- ✅ 功能开关（1.5小时）

**Day 2**:
- ✅ 系统监控（3小时）
- ✅ 任务管理（1.5小时）
- ✅ 测试和优化（1.5小时）

### Phase 2: 社区运营功能（1.5天）

**Day 3**:
- ✅ 话题管理（1.5小时）
- ✅ 社区管理（2小时）
- ✅ 纠纷统计（2小时）
- ✅ 测试和优化（1小时）

**Day 4**:
- ✅ 整体联调测试
- ✅ 性能优化
- ✅ 文档完善

### Phase 3: 验收和发布（0.5天）

**Day 5**:
- ✅ 功能验收
- ✅ 代码审查
- ✅ 部署准备

---

## 📖 技术选型

### 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| React | 18.3.1 | 前端框架 |
| TypeScript | 5.5.3 | 类型安全 |
| Ant Design | 5.21.6 | UI组件库 |
| TanStack Query | 5.59.20 | 数据状态管理 |
| React Router | 6.27.0 | 路由管理 |
| Apache ECharts | 5.5.0 | 图表库 |
| dayjs | 1.11.13 | 日期处理 |

### 开发工具

| 工具 | 版本 | 用途 |
|------|------|------|
| Vite | 5.4.8 | 构建工具 |
| ESLint | 9.13.0 | 代码检查 |
| Prettier | 3.3.3 | 代码格式化 |
| Vitest | 2.1.3 | 单元测试 |

---

## ✅ 验收标准

### 功能完整性

- ✅ 9个管理页面全部实现
- ✅ 所有CRUD操作正常
- ✅ 搜索筛选功能完整
- ✅ 统计数据准确
- ✅ 权限控制生效

### 代码质量

- ✅ TypeScript类型安全（0错误）
- ✅ 统一代码风格（ESLint + Prettier）
- ✅ 组件复用率≥90%
- ✅ 测试覆盖率≥85%

### 性能指标

- ✅ 列表加载时间<500ms
- ✅ 搜索响应时间<300ms
- ✅ 图表渲染时间<1s
- ✅ 首屏加载时间<2s

### 用户体验

- ✅ 界面统一美观
- ✅ 操作流程顺畅
- ✅ 加载状态清晰
- ✅ 错误提示友好

---

## 📖 相关文档

- [需求文档](./requirements.md) - 9个功能需求详细说明
- [任务分解](./tasks.md) - TDD开发任务分解
- [前后端覆盖分析](../../FRONTEND_BACKEND_COVERAGE_ANALYSIS.md) - 详细分析报告
- [前端开发指南](../../FRONTEND_DEV_GUIDE.md) - 5步快速开发法
- [技术栈规范](../tech.md) - 项目技术选型
- [项目结构规范](../structure.md) - 代码组织规范

---

**设计文档版本**: v1.0  
**创建日期**: 2025-11-06  
**作者**: BaSui 😎  
**状态**: 📝 规划中
