# Spec #10: 基础管理功能完善 - 任务分解

> **Spec编号**: #10  
> **作者**: BaSui 😎  
> **创建日期**: 2025-11-06  
> **状态**: 📝 规划中  
> **开发模式**: TDD十步流程法

---

## 🎯 任务概览

### 开发阶段

| 阶段 | 任务数 | 预计工时 | 说明 |
|------|--------|---------|------|
| Phase 1: 基础数据管理 | 4个模块 | 2天 | 校园、分类、标签、功能开关 |
| Phase 2: 系统运维 | 2个模块 | 1天 | 系统监控、任务管理 |
| Phase 3: 社区运营 | 3个模块 | 1天 | 话题、社区、纠纷统计 |
| **总计** | **9个模块** | **4天** | 27个任务 |

### 任务优先级

| 优先级 | 任务数 | 说明 |
|--------|--------|------|
| 🔥 P0（高） | 4个 | 基础数据，必须优先 |
| ⚡ P1（中） | 5个 | 运维和运营支持 |

---

## 📋 任务分解

### Phase 1: 基础数据管理（2天）

---

#### T1: 校园管理功能 🔥

**需求**: 需求121  
**优先级**: P0  
**预计工时**: 6小时  
**依赖**: 无

**子任务**:

**T1.1: 创建Service层（15分钟）**
```bash
# 文件: frontend/packages/shared/src/services/campus.ts
```

**实现内容**:
- [ ] 定义Campus接口类型
- [ ] 定义CampusListParams接口
- [ ] 定义CampusStatistics接口
- [ ] 实现campusService.list()
- [ ] 实现campusService.create()
- [ ] 实现campusService.update()
- [ ] 实现campusService.delete()
- [ ] 实现campusService.statistics()
- [ ] 导出到services/index.ts

**验收标准**:
- ✅ TypeScript类型定义完整
- ✅ 所有方法返回类型正确
- ✅ API路径与后端一致（/api/admin/campuses）

---

**T1.2: 创建CampusList页面（1.5小时）**
```bash
# 文件: frontend/packages/admin/src/pages/System/CampusList.tsx
```

**实现步骤**（基于GoodsList模板）:

1. **复制模板**（5分钟）
```bash
cp frontend/packages/admin/src/pages/Goods/GoodsList.tsx \
   frontend/packages/admin/src/pages/System/CampusList.tsx
```

2. **修改业务逻辑**（40分钟）
- [ ] 更新状态类型定义
- [ ] 修改统计卡片（总数、启用、禁用）
- [ ] 修改搜索字段（名称、代码）
- [ ] 修改表格列定义（名称、代码、地址、电话、状态、操作）
- [ ] 修改Service调用（campusService）
- [ ] 实现添加/编辑表单
- [ ] 实现校园统计弹窗

3. **权限控制**（5分钟）
- [ ] 添加PermissionGuard包裹
- [ ] 配置权限码SYSTEM_CAMPUS_MANAGE

**表格列定义**:
```tsx
const columns: ColumnType<Campus>[] = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '校园名称', dataIndex: 'name', width: 200 },
  { title: '校园代码', dataIndex: 'code', width: 150 },
  { title: '地址', dataIndex: 'address', width: 250 },
  { title: '联系电话', dataIndex: 'phone', width: 150 },
  {
    title: '状态',
    dataIndex: 'status',
    width: 100,
    render: (status) => (
      <Tag color={status === 'ENABLED' ? 'green' : 'red'}>
        {status === 'ENABLED' ? '启用' : '禁用'}
      </Tag>
    )
  },
  {
    title: '创建时间',
    dataIndex: 'createdAt',
    width: 180,
    render: (date) => dayjs(date).format('YYYY-MM-DD HH:mm')
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
    render: (_, record) => (
      <Space>
        <Button size="small" onClick={() => handleView(record)}>统计</Button>
        <Button size="small" onClick={() => handleEdit(record)}>编辑</Button>
        <Button size="small" danger onClick={() => handleDelete(record)}>删除</Button>
      </Space>
    )
  }
]
```

**验收标准**:
- ✅ 页面正常渲染
- ✅ 列表数据正常展示
- ✅ 搜索筛选功能正常
- ✅ 添加/编辑/删除功能正常
- ✅ 统计弹窗正常展示

---

**T1.3: 配置路由和菜单（10分钟）**

1. **路由配置**
```tsx
// frontend/packages/admin/src/router/index.tsx
{
  path: 'system/campuses',
  element: (
    <PermissionGuard permission={PERMISSION_CODES.SYSTEM_CAMPUS_MANAGE}>
      <CampusList />
    </PermissionGuard>
  ),
}
```

2. **菜单配置**
```tsx
// frontend/packages/admin/src/config/menu.ts
{
  key: 'system-campuses',
  label: '校园管理',
  path: '/admin/system/campuses',
  permission: PERMISSION_CODES.SYSTEM_CAMPUS_MANAGE,
}
```

**验收标准**:
- ✅ 路由跳转正常
- ✅ 菜单显示正常
- ✅ 权限控制生效

---

**T1.4: 测试验证（10分钟）**
```bash
pnpm dev
```

**测试清单**:
- [ ] 访问 http://localhost:5173/admin/system/campuses
- [ ] 测试列表加载
- [ ] 测试搜索功能
- [ ] 测试添加校园
- [ ] 测试编辑校园
- [ ] 测试删除校园
- [ ] 测试统计查看
- [ ] 测试权限控制

**验收标准**:
- ✅ 所有功能正常运行
- ✅ 无TypeScript错误
- ✅ 无控制台错误

---

#### T2: 分类管理功能 🔥

**需求**: 需求122  
**优先级**: P0  
**预计工时**: 8小时  
**依赖**: 无

**子任务**:

**T2.1: 创建Service层（20分钟）**
```bash
# 文件: frontend/packages/shared/src/services/category.ts
```

**实现内容**:
- [ ] 定义Category接口（含parentId、level、children）
- [ ] 定义CategoryListParams接口
- [ ] 实现categoryService.tree() - 获取分类树
- [ ] 实现categoryService.create()
- [ ] 实现categoryService.update()
- [ ] 实现categoryService.delete()
- [ ] 实现categoryService.sort() - 批量排序
- [ ] 导出到services/index.ts

---

**T2.2: 创建CategoryList页面（2.5小时）**
```bash
# 文件: frontend/packages/admin/src/pages/System/CategoryList.tsx
```

**实现步骤**:

1. **使用树形表格**（1小时）
```tsx
import { Table } from 'antd'

<Table
  columns={columns}
  dataSource={treeData}
  expandable={{
    defaultExpandAllRows: false,
    indentSize: 24
  }}
  pagination={false}
/>
```

2. **实现拖拽排序**（1小时）
```tsx
import { DndContext, closestCenter } from '@dnd-kit/core'
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable'

// 拖拽排序逻辑
const handleDragEnd = (event) => {
  // 更新排序
  categoryService.sort(newOrder)
}
```

3. **添加/编辑表单**（30分钟）
- [ ] 父分类选择（Select树形结构）
- [ ] 分类名称输入
- [ ] 图标URL输入
- [ ] 排序权重输入
- [ ] 状态选择

**验收标准**:
- ✅ 树形结构正常展示
- ✅ 拖拽排序功能正常
- ✅ 添加/编辑/删除功能正常
- ✅ 层级关系正确

---

**T2.3: 配置路由和菜单（10分钟）**

**验收标准**:
- ✅ 路由跳转正常
- ✅ 菜单显示正常

---

**T2.4: 测试验证（10分钟）**

**测试清单**:
- [ ] 测试树形展开/收起
- [ ] 测试拖拽排序
- [ ] 测试添加子分类
- [ ] 测试编辑分类
- [ ] 测试删除保护（有子分类不能删除）

---

#### T3: 标签管理功能 🔥

**需求**: 需求123  
**优先级**: P0  
**预计工时**: 6小时  
**依赖**: 无

**子任务**:

**T3.1: 创建Service层（20分钟）**
```bash
# 文件: frontend/packages/shared/src/services/tag.ts
```

**实现内容**:
- [ ] 定义Tag接口（含type、color、hotCount）
- [ ] 定义TagListParams接口
- [ ] 实现tagService.list()
- [ ] 实现tagService.create()
- [ ] 实现tagService.update()
- [ ] 实现tagService.delete()
- [ ] 实现tagService.merge() - 标签合并
- [ ] 实现tagService.hot() - 热门标签
- [ ] 导出到services/index.ts

---

**T3.2: 创建TagList页面（1.5小时）**
```bash
# 文件: frontend/packages/admin/src/pages/System/TagList.tsx
```

**实现内容**:
- [ ] 基础列表（复用GoodsList模板）
- [ ] 标签类型筛选（商品/帖子/通用）
- [ ] 热度排行榜（TOP 20，侧边栏卡片）
- [ ] 标签合并功能（批量选择→合并）
- [ ] 标签颜色选择（ColorPicker）

**表格列定义**:
```tsx
{
  title: '标签名',
  dataIndex: 'name',
  render: (name, record) => (
    <Tag color={record.color}>{name}</Tag>
  )
},
{
  title: '类型',
  dataIndex: 'type',
  render: (type) => {
    const typeMap = {
      GOODS: '商品',
      POST: '帖子',
      COMMON: '通用'
    }
    return typeMap[type]
  }
},
{
  title: '热度',
  dataIndex: 'hotCount',
  sorter: true
}
```

**验收标准**:
- ✅ 列表正常展示
- ✅ 类型筛选正常
- ✅ 热度排行正常
- ✅ 标签合并功能正常

---

**T3.3: 配置路由和菜单（10分钟）**

---

**T3.4: 测试验证（10分钟）**

---

#### T4: 功能开关管理 ⚡

**需求**: 需求127  
**优先级**: P1  
**预计工时**: 6小时  
**依赖**: 无

**子任务**:

**T4.1: 创建Service层（15分钟）**
```bash
# 文件: frontend/packages/shared/src/services/featureFlag.ts
```

**实现内容**:
- [ ] 定义FeatureFlag接口（含status、strategy、grayRule）
- [ ] 定义FeatureFlagListParams接口
- [ ] 实现featureFlagService.list()
- [ ] 实现featureFlagService.create()
- [ ] 实现featureFlagService.update()
- [ ] 实现featureFlagService.delete()
- [ ] 实现featureFlagService.logs() - 使用日志
- [ ] 导出到services/index.ts

---

**T4.2: 创建FeatureFlagList页面（1.5小时）**
```bash
# 文件: frontend/packages/admin/src/pages/System/FeatureFlagList.tsx
```

**实现内容**:
- [ ] 基础列表（复用GoodsList模板）
- [ ] 状态筛选（启用/禁用/灰度）
- [ ] 环境筛选（开发/测试/生产）
- [ ] 灰度策略配置（按用户/校园/百分比）
- [ ] 使用日志抽屉

**灰度策略配置表单**:
```tsx
<Form.Item label="灰度策略" name="strategy">
  <Select>
    <Option value="USER">按用户ID</Option>
    <Option value="CAMPUS">按校园ID</Option>
    <Option value="PERCENTAGE">按百分比</Option>
  </Select>
</Form.Item>

{strategy === 'USER' && (
  <Form.Item label="用户ID列表" name="userIds">
    <Select mode="tags" placeholder="输入用户ID" />
  </Form.Item>
)}

{strategy === 'PERCENTAGE' && (
  <Form.Item label="灰度百分比" name="percentage">
    <Slider min={0} max={100} marks={{ 0: '0%', 50: '50%', 100: '100%' }} />
  </Form.Item>
)}
```

**验收标准**:
- ✅ 功能开关列表正常
- ✅ 灰度策略配置正常
- ✅ 使用日志正常

---

**T4.3: 配置路由和菜单（10分钟）**

---

**T4.4: 测试验证（10分钟）**

---

### Phase 2: 系统运维（1天）

---

#### T5: 系统监控功能 🔥

**需求**: 需求124  
**优先级**: P0  
**预计工时**: 12小时  
**依赖**: 无

**子任务**:

**T5.1: 创建Service层（20分钟）**
```bash
# 文件: frontend/packages/shared/src/services/monitor.ts
```

**实现内容**:
- [ ] 定义SystemOverview接口
- [ ] 定义ApiMonitor接口
- [ ] 定义CacheMonitor接口
- [ ] 定义DatabaseMonitor接口
- [ ] 定义ErrorLog接口
- [ ] 实现monitorService.system()
- [ ] 实现monitorService.api()
- [ ] 实现monitorService.cache()
- [ ] 实现monitorService.database()
- [ ] 实现monitorService.errors()
- [ ] 实现monitorService.alerts()
- [ ] 导出到services/index.ts

---

**T5.2: 创建SystemMonitor页面（3小时）**
```bash
# 文件: frontend/packages/admin/src/pages/System/SystemMonitor.tsx
```

**实现步骤**:

1. **系统概览卡片**（30分钟）
```tsx
<Row gutter={16}>
  <Col span={6}>
    <Card>
      <Statistic
        title="CPU使用率"
        value={cpuUsage}
        suffix="%"
        valueStyle={{ color: cpuUsage > 80 ? '#cf1322' : '#3f8600' }}
      />
    </Card>
  </Col>
  <Col span={6}>
    <Card>
      <Statistic
        title="内存使用率"
        value={memoryUsage}
        suffix="%"
      />
    </Card>
  </Col>
  {/* ... 其他卡片 */}
</Row>
```

2. **API监控图表**（1小时）
```tsx
import ReactECharts from 'echarts-for-react'

const apiTrendOption = {
  title: { text: 'API请求趋势' },
  xAxis: { type: 'category', data: timeLabels },
  yAxis: { type: 'value' },
  series: [{
    name: '请求量',
    type: 'line',
    data: requestCounts,
    smooth: true
  }]
}

<ReactECharts option={apiTrendOption} style={{ height: 400 }} />
```

3. **慢接口TOP 10**（30分钟）
```tsx
<Table
  dataSource={slowApis}
  columns={[
    { title: 'API路径', dataIndex: 'path' },
    { title: '平均响应时间', dataIndex: 'avgTime', render: (t) => `${t}ms` },
    { title: '调用次数', dataIndex: 'count' }
  ]}
  pagination={false}
/>
```

4. **实时更新**（30分钟）
```tsx
const { data, isLoading } = useQuery({
  queryKey: ['monitor', 'system'],
  queryFn: monitorService.system,
  refetchInterval: 30000 // 30秒自动刷新
})
```

**验收标准**:
- ✅ 系统概览数据实时更新
- ✅ API监控图表正常渲染
- ✅ 慢接口列表正常展示
- ✅ 错误日志列表正常

---

**T5.3: 配置路由和菜单（10分钟）**

---

**T5.4: 测试验证（10分钟）**

---

#### T6: 任务管理功能 ⚡

**需求**: 需求128  
**优先级**: P1  
**预计工时**: 6小时  
**依赖**: 无

**子任务**:

**T6.1: 创建Service层（25分钟）**
```bash
# 文件: frontend/packages/shared/src/services/task.ts
```

**实现内容**:
- [ ] 定义Task接口（含cron、lastExecuteTime、nextExecuteTime）
- [ ] 定义TaskLog接口
- [ ] 定义TaskStatistics接口
- [ ] 实现taskService.list()
- [ ] 实现taskService.detail()
- [ ] 实现taskService.start()
- [ ] 实现taskService.pause()
- [ ] 实现taskService.trigger()
- [ ] 实现taskService.logs()
- [ ] 实现taskService.statistics()
- [ ] 导出到services/index.ts

---

**T6.2: 创建TaskList页面（1.5小时）**
```bash
# 文件: frontend/packages/admin/src/pages/System/TaskList.tsx
```

**实现内容**:
- [ ] 任务列表（复用GoodsList模板）
- [ ] 状态筛选（运行中/已暂停/失败）
- [ ] 启动/暂停按钮
- [ ] 手动触发按钮
- [ ] 执行日志抽屉
- [ ] 任务统计图表

**表格列定义**:
```tsx
{
  title: 'Cron表达式',
  dataIndex: 'cron',
  width: 150
},
{
  title: '上次执行',
  dataIndex: 'lastExecuteTime',
  render: (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
},
{
  title: '下次执行',
  dataIndex: 'nextExecuteTime',
  render: (time) => dayjs(time).format('YYYY-MM-DD HH:mm:ss')
},
{
  title: '操作',
  render: (_, record) => (
    <Space>
      <Button size="small" onClick={() => handleToggle(record)}>
        {record.status === 'RUNNING' ? '暂停' : '启动'}
      </Button>
      <Button size="small" onClick={() => handleTrigger(record)}>手动触发</Button>
      <Button size="small" onClick={() => handleViewLogs(record)}>查看日志</Button>
    </Space>
  )
}
```

**验收标准**:
- ✅ 任务列表正常展示
- ✅ 启动/暂停功能正常
- ✅ 手动触发功能正常
- ✅ 执行日志正常展示

---

**T6.3: 配置路由和菜单（10分钟）**

---

**T6.4: 测试验证（10分钟）**

---

### Phase 3: 社区运营（1天）

---

#### T7: 话题管理功能 ⚡

**需求**: 需求125  
**优先级**: P1  
**预计工时**: 6小时  
**依赖**: T3（标签管理）

**子任务**:

**T7.1: 创建Service层（20分钟）**
```bash
# 文件: frontend/packages/shared/src/services/topic.ts
```

---

**T7.2: 创建TopicList页面（1.5小时）**
```bash
# 文件: frontend/packages/admin/src/pages/Community/TopicList.tsx
```

**实现内容**:
- [ ] 话题列表（复用GoodsList模板）
- [ ] 话题审核（批准/拒绝）
- [ ] 话题统计弹窗
- [ ] 热度排行榜

---

**T7.3: 配置路由和菜单（10分钟）**

---

**T7.4: 测试验证（10分钟）**

---

#### T8: 社区管理功能 ⚡

**需求**: 需求126  
**优先级**: P1  
**预计工时**: 8小时  
**依赖**: T1（校园管理）

**子任务**:

**T8.1: 创建Service层（30分钟）**
```bash
# 文件: frontend/packages/shared/src/services/community.ts
```

**实现内容**:
- [ ] 定义Community接口
- [ ] 定义CommunityMember接口
- [ ] 定义CommunitySettings接口
- [ ] 实现communityService.list()
- [ ] 实现communityService.create()
- [ ] 实现communityService.update()
- [ ] 实现communityService.delete()
- [ ] 实现communityService.members() - 成员列表
- [ ] 实现communityService.addMember()
- [ ] 实现communityService.removeMember()
- [ ] 实现communityService.updateSettings()
- [ ] 导出到services/index.ts

---

**T8.2: 创建CommunityList页面（2小时）**
```bash
# 文件: frontend/packages/admin/src/pages/Community/CommunityList.tsx
```

**实现内容**:
- [ ] 社区列表（复用GoodsList模板）
- [ ] 成员管理弹窗（嵌套表格）
- [ ] 社区设置抽屉
- [ ] 活跃度统计

**成员管理弹窗**:
```tsx
<Modal title="成员管理" visible={visible}>
  <Table
    dataSource={members}
    columns={[
      { title: '用户', dataIndex: 'userName' },
      { title: '角色', dataIndex: 'role' },
      { title: '加入时间', dataIndex: 'joinedAt' },
      {
        title: '操作',
        render: (_, record) => (
          <Space>
            <Button size="small">设为管理员</Button>
            <Button size="small">禁言</Button>
            <Button size="small" danger>移除</Button>
          </Space>
        )
      }
    ]}
  />
</Modal>
```

**验收标准**:
- ✅ 社区列表正常展示
- ✅ 成员管理功能正常
- ✅ 社区设置生效

---

**T8.3: 配置路由和菜单（10分钟）**

---

**T8.4: 测试验证（10分钟）**

---

#### T9: 纠纷统计分析 ⚡

**需求**: 需求129  
**优先级**: P1  
**预计工时**: 8小时  
**依赖**: 纠纷模块（已完成）

**子任务**:

**T9.1: 创建Service层（15分钟）**
```bash
# 文件: frontend/packages/shared/src/services/statistics.ts
```

**实现内容**:
- [ ] 定义DisputeStatistics接口
- [ ] 实现statisticsService.overview() - 概览
- [ ] 实现statisticsService.trend() - 趋势
- [ ] 实现statisticsService.category() - 分类统计
- [ ] 实现statisticsService.arbitrator() - 仲裁员统计
- [ ] 实现statisticsService.export() - 数据导出
- [ ] 导出到services/index.ts

---

**T9.2: 创建DisputeStatistics页面（2.5小时）**
```bash
# 文件: frontend/packages/admin/src/pages/Disputes/DisputeStatistics.tsx
```

**实现步骤**:

1. **统计概览**（30分钟）
```tsx
<Row gutter={16}>
  <Col span={6}>
    <StatisticCard title="纠纷总数" value={total} />
  </Col>
  <Col span={6}>
    <StatisticCard title="处理中" value={processing} />
  </Col>
  <Col span={6}>
    <StatisticCard title="处理成功率" value={successRate} suffix="%" />
  </Col>
  <Col span={6}>
    <StatisticCard title="平均处理时长" value={avgTime} suffix="小时" />
  </Col>
</Row>
```

2. **趋势图**（1小时）
```tsx
const trendOption = {
  title: { text: '纠纷数量趋势' },
  xAxis: { type: 'category', data: dates },
  yAxis: { type: 'value' },
  series: [
    { name: '新增纠纷', type: 'line', data: newDisputes },
    { name: '已完成', type: 'line', data: completedDisputes }
  ]
}

<ReactECharts option={trendOption} style={{ height: 400 }} />
```

3. **分类统计饼图**（30分钟）
```tsx
const categoryOption = {
  title: { text: '纠纷类型分布' },
  series: [{
    type: 'pie',
    data: [
      { value: 40, name: '商品质量' },
      { value: 30, name: '描述不符' },
      { value: 20, name: '物流问题' },
      { value: 10, name: '其他' }
    ]
  }]
}

<ReactECharts option={categoryOption} style={{ height: 400 }} />
```

4. **仲裁员排行**（30分钟）
```tsx
<Table
  dataSource={arbitrators}
  columns={[
    { title: '仲裁员', dataIndex: 'name' },
    { title: '处理数量', dataIndex: 'count', sorter: true },
    { title: '成功率', dataIndex: 'successRate', render: (r) => `${r}%` },
    { title: '平均处理时长', dataIndex: 'avgTime', render: (t) => `${t}小时` }
  ]}
/>
```

**验收标准**:
- ✅ 统计概览正常展示
- ✅ 趋势图正常渲染
- ✅ 分类统计正常
- ✅ 仲裁员排行正常
- ✅ 数据导出功能正常

---

**T9.3: 配置路由和菜单（10分钟）**

---

**T9.4: 测试验证（10分钟）**

---

## 📊 任务汇总表

| 任务编号 | 功能模块 | 预计工时 | 优先级 | 状态 |
|---------|---------|---------|--------|------|
| T1 | 校园管理 | 6小时 | 🔥 P0 | ⏳ 待开发 |
| T2 | 分类管理 | 8小时 | 🔥 P0 | ⏳ 待开发 |
| T3 | 标签管理 | 6小时 | 🔥 P0 | ⏳ 待开发 |
| T4 | 功能开关 | 6小时 | ⚡ P1 | ⏳ 待开发 |
| T5 | 系统监控 | 12小时 | 🔥 P0 | ⏳ 待开发 |
| T6 | 任务管理 | 6小时 | ⚡ P1 | ⏳ 待开发 |
| T7 | 话题管理 | 6小时 | ⚡ P1 | ⏳ 待开发 |
| T8 | 社区管理 | 8小时 | ⚡ P1 | ⏳ 待开发 |
| T9 | 纠纷统计 | 8小时 | ⚡ P1 | ⏳ 待开发 |
| **总计** | **9个模块** | **66小时** | - | - |

**实际工作日**: 8小时/天 × 4天 = **32工作小时**  
**预留缓冲**: 66小时 → 32小时（通过模板复用，效率提升2倍）

---

## ✅ 验收清单

### 功能验收

- [ ] 9个管理页面全部完成
- [ ] 所有CRUD操作正常
- [ ] 搜索筛选功能完整
- [ ] 统计数据准确
- [ ] 权限控制生效
- [ ] 所有图表正常渲染

### 代码质量

- [ ] TypeScript零错误
- [ ] ESLint零警告
- [ ] 统一代码风格
- [ ] 组件复用率≥90%
- [ ] 测试覆盖率≥85%

### 性能验收

- [ ] 列表加载时间<500ms
- [ ] 搜索响应时间<300ms
- [ ] 图表渲染时间<1s
- [ ] 首屏加载时间<2s

### 用户体验

- [ ] 界面统一美观
- [ ] 操作流程顺畅
- [ ] 加载状态清晰
- [ ] 错误提示友好

---

## 📖 开发指南

### 开发环境

```bash
# 启动前端
cd frontend/packages/admin
pnpm dev

# 访问地址
http://localhost:5173/admin
```

### 调试工具

- **React DevTools** - 组件调试
- **Redux DevTools** - 状态调试
- **Network** - API调试

### 常见问题

**Q1: Service方法返回类型错误**
```typescript
// ❌ 错误
async list() {
  return apiClient.get('/api/xxx')
}

// ✅ 正确
async list(): Promise<PageResponse<Campus>> {
  return apiClient.get<PageResponse<Campus>>('/api/xxx')
}
```

**Q2: 权限守卫不生效**
```tsx
// ❌ 错误 - 缺少PermissionGuard
<CampusList />

// ✅ 正确
<PermissionGuard permission={PERMISSION_CODES.SYSTEM_CAMPUS_MANAGE}>
  <CampusList />
</PermissionGuard>
```

**Q3: TanStack Query缓存问题**
```tsx
// 修改后刷新列表
const mutation = useMutation({
  mutationFn: service.update,
  onSuccess: () => {
    queryClient.invalidateQueries(['list']) // 刷新列表
  }
})
```

---

## 📖 相关文档

- [需求文档](./requirements.md) - 9个功能需求详细说明
- [设计文档](./design.md) - 前端架构设计和组件复用
- [前端开发指南](../../FRONTEND_DEV_GUIDE.md) - 5步快速开发法
- [前后端覆盖分析](../../FRONTEND_BACKEND_COVERAGE_ANALYSIS.md) - 详细分析报告

---

**任务分解版本**: v1.0  
**创建日期**: 2025-11-06  
**作者**: BaSui 😎  
**状态**: 📝 规划中
