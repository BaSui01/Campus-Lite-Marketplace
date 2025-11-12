# Spec #11: 前端门户增强开发(复用共享层)

> **编号**: 11  
> **创建日期**: 2025-11-06  
> **负责人**: BaSui 😎  
> **优先级**: P0 (Critical)  
> **预计工期**: 6-8周  
> **依赖**: 
> - 后端门户API已完整实现(145个接口)
> - Shared层组件库已就绪(22个组件)
> - Portal基础架构已完成

---

## 📋 一、需求概述

### 🎯 背景

当前前端用户门户(Portal)已实现核心交易流程,但与后端 **145个门户接口** 相比,接口覆盖率仅约 **40%**。本 Spec 基于已有的 **22个共享层(Shared)组件**,规划门户功能的增强开发,**最大化组件复用率**。

**关键数据**:
- **后端门户接口**: 145个 (不含管理接口40个)
- **已实现接口**: ~58个 (40%覆盖)
- **Shared层组件**: 22个 ✅
- **Portal专属组件**: 2个 (ErrorBoundary, SliderCaptcha)
- **组件复用目标**: ≥90%

### 🚀 目标

1. **提升接口覆盖率**: 40% → 85%+ (新增对接 ~65个接口)
2. **最大化组件复用**: 优先使用Shared层组件,避免重复造轮子
3. **补全核心功能**: 评价、物流、纠纷、社交、推荐
4. **统一代码风格**: 与Admin包保持一致的架构和组件使用方式

### 📊 Shared层组件盘点

#### P0 基础组件 (10个) - 已完成 ✅

| 组件名 | 描述 | Portal使用场景 | Admin使用场景 |
|--------|------|---------------|--------------|
| **Button** | 按钮 | 所有页面按钮 | 所有页面按钮 |
| **Input** | 输入框 | 搜索框、表单 | 筛选器、表单 |
| **Select** | 选择器 | 分类筛选 | 状态筛选 |
| **Empty** | 空状态 | 无数据展示 | 无数据展示 |
| **Loading** | 加载 | 页面加载 | 页面加载 |
| **Skeleton** | 骨架屏 | 列表加载 | 表格加载 |
| **Toast** | 消息提示 | 操作反馈 | 操作反馈 |
| **Modal** | 模态框 | 确认弹窗 | 表单弹窗 |
| **Form/FormItem** | 表单 | 发布商品、评价 | 审核表单 |

#### P1 高级组件 (7个) - 已完成 ✅

| 组件名 | 描述 | Portal使用场景 | Admin使用场景 |
|--------|------|---------------|--------------|
| **Dropdown** | 下拉菜单 | 用户菜单 | 操作菜单 |
| **Table** | 表格 | 订单列表(可选) | 管理列表 |
| **Pagination** | 分页 | 商品列表 | 所有列表 |
| **Card** | 卡片 | 商品卡片容器 | 统计卡片 |
| **Tabs** | 标签页 | 订单tabs、设置tabs | 管理tabs |
| **Badge** | 徽标 | 未读消息数 | 状态徽标 |
| **Avatar** | 头像 | 用户头像 | 用户头像 |
| **Tag** | 标签 | 商品标签 | 状态标签 |

#### P2 业务组件 (5个) - 已完成 ✅

| 组件名 | 描述 | Portal使用场景 | Admin使用场景 |
|--------|------|---------------|--------------|
| **GoodsCard** | 商品卡片 | 商品列表、推荐 | 商品审核 |
| **OrderCard** | 订单卡片 | 订单列表 | 订单管理 |
| **UserAvatar** | 用户头像 | 评论、私信 | 用户管理 |
| **ImageUpload** | 图片上传 | 发布商品、评价 | 商品审核 |
| **RichTextEditor** | 富文本 | 发帖、回复 | 通知模板 |

### 🆕 需要新增的组件

根据门户需求分析,以下组件需要在Shared层新增:

| 组件名 | 描述 | 优先级 | 复用度 |
|--------|------|--------|--------|
| **StarRating** | 星级评分 | P0 | Portal/Admin |
| **Timeline** | 时间轴 | P0 | Portal/Admin |
| **FileUploader** | 文件上传 | P1 | Portal/Admin |
| **VideoPlayer** | 视频播放器 | P2 | Portal |
| **Carousel** | 轮播图 | P2 | Portal |

---

## 🏗️ 二、功能需求(按优先级)

### 🔴 P0 - 关键功能 (第1-2周)

#### 需求 11.1: 评价系统 ⚡ Critical

**用户故事**:
> 作为买家,我希望能对购买的商品进行评价,并查看其他买家的评价,以便做出更好的购买决策。

**功能点**:

1. **发布评价页** `/orders/:orderNo/review`
   - **复用组件**:
     - ✅ `Form` - 表单容器
     - ✅ `Button` - 提交/取消按钮
     - ✅ `ImageUpload` - 评价图片上传
     - ✅ `Toast` - 提交成功提示
     - 🆕 `StarRating` - 星级评分(新增)
   - **新增内容**:
     - 文字评价输入框(10-500字,带字数统计)
     - 敏感词过滤提示

2. **商品详情页评价区块**
   - **复用组件**:
     - ✅ `Tabs` - 全部/5星/4星/3星/2星/1星筛选
     - ✅ `Pagination` - 评价分页
     - ✅ `UserAvatar` - 买家头像
     - ✅ `Badge` - 点赞数徽标
     - ✅ `Empty` - 暂无评价
     - ✅ `Loading` - 加载中
     - 🆕 `StarRating` - 星级展示
   - **新增内容**:
     - 评价列表项组件
     - 评价图片网格展示
     - 点赞/回复按钮

3. **我的评价页** `/profile/reviews`
   - **复用组件**:
     - ✅ `Card` - 评价卡片
     - ✅ `Pagination` - 分页
     - ✅ `Button` - 编辑/删除
     - ✅ `Modal` - 确认删除弹窗
     - 🆕 `StarRating` - 星级展示

**后端API对接**:
- ❌ `ReviewLikeController` (5个接口)
- ❌ `ReviewReplyController` (7个接口)
- ❌ `ReviewMediaController` (6个接口)

**验收标准**:
- ✅ 可在订单完成后发布评价(星级+文字+图片)
- ✅ 商品详情页显示评价列表,支持筛选排序
- ✅ 评价点赞和卖家回复功能正常
- ✅ 个人中心可查看/编辑/删除我的评价

---

#### 需求 11.2: 物流信息集成 ⚡ Critical

**用户故事**:
> 作为买家,我希望能实时查看订单的物流状态,知道包裹当前位置和预计送达时间。

**功能点**:

1. **订单详情页物流区块**
   - **复用组件**:
     - ✅ `Card` - 物流卡片
     - ✅ `Button` - 展开/收起
     - ✅ `Loading` - 加载物流信息
     - ✅ `Empty` - 暂无物流信息
     - 🆕 `Timeline` - 物流时间轴(新增)
   - **新增内容**:
     - 物流公司logo
     - 快递单号(可复制)
     - 最新状态高亮

**后端API对接**:
- ❌ `LogisticsController` (7个接口)

**验收标准**:
- ✅ 订单详情页显示物流卡片
- ✅ 物流时间轴展示完整轨迹
- ✅ 可复制快递单号

---

#### 需求 11.3: 推荐系统 ⚡ High

**用户故事**:
> 作为用户,我希望首页能展示个性化推荐的商品,而不是只有固定分类,提升购物效率。

**功能点**:

1. **首页推荐区块**
   - **复用组件**:
     - ✅ `Tabs` - 为你推荐/热门榜单切换
     - ✅ `GoodsCard` - 商品卡片
     - ✅ `Loading` - 加载推荐
     - ✅ `Empty` - 暂无推荐
   - **新增内容**:
     - 横向滑动容器
     - 加载更多按钮

2. **商品详情页相似推荐**
   - **复用组件**:
     - ✅ `GoodsCard` - 商品卡片
     - ✅ `Card` - 推荐区块容器

**后端API对接**:
- ❌ `RecommendController` (3个接口)

**验收标准**:
- ✅ 首页显示个性化推荐和热门榜单
- ✅ 商品详情页显示相似商品
- ✅ 推荐数据准确

---

### 🟡 P1 - 重要功能 (第3-4周)

#### 需求 11.4: 纠纷处理系统 ⚡ High

**用户故事**:
> 作为买家/卖家,当交易出现问题时,我希望能发起纠纷,通过平台协商或仲裁解决,保障我的权益。

**功能点**:

1. **纠纷列表页** `/disputes`
   - **复用组件**:
     - ✅ `Table` - 纠纷列表(可选)或自定义卡片列表
     - ✅ `Tabs` - 协商中/仲裁中/已关闭
     - ✅ `Pagination` - 分页
     - ✅ `Badge` - 状态徽标
     - ✅ `Empty` - 暂无纠纷

2. **纠纷详情页** `/disputes/:id`
   - **复用组件**:
     - ✅ `Card` - 纠纷信息卡片
     - ✅ `Button` - 操作按钮
     - ✅ `Modal` - 确认弹窗
     - 🆕 `Timeline` - 协商记录时间轴
     - 🆕 `FileUploader` - 证据上传(新增)
   - **新增内容**:
     - 方案卡片组件
     - 证据列表展示

3. **发起纠纷页** (订单详情页弹窗)
   - **复用组件**:
     - ✅ `Modal` - 弹窗容器
     - ✅ `Form` - 表单
     - ✅ `Select` - 纠纷类型选择
     - ✅ `Input` - 纠纷描述
     - ✅ `ImageUpload` - 证据上传
     - ✅ `Button` - 提交/取消

**后端API对接**:
- ❌ `DisputeController` (6个接口)
- ❌ `DisputeNegotiationController` (6个接口)
- ❌ `DisputeEvidenceController` (8个接口)
- ❌ `DisputeArbitrationController` (2个接口,查询部分)

**验收标准**:
- ✅ 可在订单详情页发起纠纷
- ✅ 纠纷列表和详情页功能完整
- ✅ 协商消息和方案功能正常
- ✅ 可查看仲裁结果

---

#### 需求 11.5: 黑名单管理 ⚡ High

**用户故事**:
> 作为用户,我希望能拉黑骚扰我的用户,避免收到Ta的消息和看到Ta的商品。

**功能点**:

1. **黑名单管理页** `/settings/blacklist`
   - **复用组件**:
     - ✅ `Card` - 页面容器
     - ✅ `UserAvatar` - 用户头像
     - ✅ `Button` - 解除拉黑按钮
     - ✅ `Empty` - 暂无黑名单
     - ✅ `Pagination` - 分页
     - ✅ `Modal` - 确认弹窗
   - **新增内容**:
     - 黑名单卡片组件

2. **拉黑入口**
   - 用户主页: ✅ `Button` - 拉黑按钮
   - 私信界面: ✅ `Dropdown` - 操作菜单中的拉黑选项

**后端API对接**:
- ❌ `BlacklistController` (4个接口)

**验收标准**:
- ✅ 可在用户主页/私信拉黑用户
- ✅ 黑名单列表显示正常,可解除拉黑
- ✅ 拉黑后私信/商品过滤生效

---

#### 需求 11.6: 申诉系统 ⚡ Medium

**用户故事**:
> 作为被封禁用户,我希望能提交申诉,说明情况并提供证据,争取解封。

**功能点**:

1. **申诉列表页** `/appeals`
   - **复用组件**:
     - ✅ `Table` / 卡片列表
     - ✅ `Tabs` - 待审核/已通过/已拒绝
     - ✅ `Badge` - 状态徽标
     - ✅ `Pagination` - 分页
     - ✅ `Empty` - 暂无申诉

2. **提交申诉页** `/appeals/create`
   - **复用组件**:
     - ✅ `Form` - 表单
     - ✅ `Select` - 申诉类型
     - ✅ `Input` - 申诉原因
     - ✅ `ImageUpload` / 🆕 `FileUploader` - 证据上传
     - ✅ `Button` - 提交/取消

3. **申诉详情页** `/appeals/:id`
   - **复用组件**:
     - ✅ `Card` - 详情卡片
     - 🆕 `Timeline` - 审核进度
     - ✅ `Badge` - 状态
     - ✅ `Button` - 取消申诉

**后端API对接**:
- ⚠️ `AppealController` (5个接口) - 部分已实现
- ❌ `AppealMaterialController` (11个接口)

**验收标准**:
- ✅ 封禁后可提交申诉
- ✅ 申诉列表和详情功能完整
- ✅ 审核结果正常展示

---

### 🟢 P2 - 优化功能 (第5-6周)

#### 需求 11.7: 社交功能完善

**7.1 回复管理**

**用户故事**:
> 作为用户,我希望能回复帖子的评论,形成讨论串,增强互动。

**功能点**:
- **复用组件**:
  - ✅ `Card` - 回复卡片
  - ✅ `UserAvatar` - 回复者头像
  - ✅ `Button` - 回复/删除按钮
  - ✅ `Input` - 回复输入框
  - ✅ `Modal` - 确认删除
- **新增内容**:
  - 树状回复组件
  - @用户提及功能

**后端API对接**:
- ❌ `ReplyController` (4个接口)

---

**7.2 话题系统**

**用户故事**:
> 作为用户,我希望能关注感兴趣的话题,查看话题下的所有帖子。

**功能点**:

1. **话题详情页** `/topics/:id`
   - **复用组件**:
     - ✅ `Card` - 话题信息卡片
     - ✅ `Button` - 关注/取关
     - ✅ `Badge` - 关注数
     - ✅ `Pagination` - 帖子分页
     - ✅ `Empty` - 暂无帖子

2. **我的关注话题** `/topics/following`
   - **复用组件**:
     - ✅ `Card` - 话题卡片
     - ✅ `Button` - 取关
     - ✅ `Pagination` - 分页
     - ✅ `Empty` - 暂无关注

**后端API对接**:
- ❌ `TopicController` (10个接口)

---

**7.3 用户关注**

**用户故事**:
> 作为用户,我希望能关注其他用户,查看Ta的动态和作品。

**功能点**:

1. **关注列表** `/users/:id/following`
   - **复用组件**:
     - ✅ `UserAvatar` - 用户卡片
     - ✅ `Button` - 取关按钮
     - ✅ `Pagination` - 分页
     - ✅ `Empty` - 暂无关注

2. **粉丝列表** `/users/:id/followers`
   - **复用组件**:
     - ✅ `UserAvatar` - 用户卡片
     - ✅ `Button` - 关注回粉
     - ✅ `Pagination` - 分页
     - ✅ `Empty` - 暂无粉丝

**后端API对接**:
- ❌ `UserFollowController` (7个接口)

---

#### 需求 11.8: 隐私与合规

**用户故事**:
> 作为用户,我希望能导出我的个人数据,或者注销账号,保障我的隐私权。

**功能点**:

1. **隐私设置页** `/settings/privacy`
   - **复用组件**:
     - ✅ `Card` - 设置区块
     - ✅ `Button` - 导出/注销按钮
     - ✅ `Modal` - 确认弹窗
     - ✅ `Loading` - 处理中
     - ✅ `Toast` - 操作反馈
   - **新增内容**:
     - 数据导出状态展示
     - 注销账号风险提示

**后端API对接**:
- ❌ `PrivacyController` (4个接口)

---

#### 需求 11.9: 通知偏好设置

**用户故事**:
> 作为用户,我希望能自定义通知渠道和时段,避免打扰。

**功能点**:

1. **通知偏好页** `/settings/notifications`
   - **复用组件**:
     - ✅ `Card` - 设置区块
     - ✅ `Select` - 渠道选择
     - ✅ `Button` - 保存按钮
     - ✅ `Toast` - 保存成功
   - **新增内容**:
     - 时段选择器

**后端API对接**:
- ❌ `NotificationPreferenceController` (5个接口)

---

#### 需求 11.10: 订阅管理增强

**功能点**:
- **复用组件**:
  - ✅ `Card` - 订阅卡片
  - ✅ `Input` - 关键词输入
  - ✅ `Button` - 添加/删除订阅
  - ✅ `Empty` - 暂无订阅
  - ✅ `Pagination` - 分页

**后端API对接**:
- ⚠️ `SubscriptionController` (3个接口) - 部分已实现

---

### 🔵 P3 - 锦上添花 (第7-8周)

#### 需求 11.11: 商品批量操作(卖家端)

**用户故事**:
> 作为卖家,我希望能批量上下架商品、修改价格,提升管理效率。

**功能点**:
- **复用组件**:
  - ✅ `Table` - 商品列表(带选择)
  - ✅ `Button` - 批量操作按钮
  - ✅ `Modal` - 批量修改表单
  - ✅ `Toast` - 操作结果

**后端API对接**:
- ❌ `GoodsBatchController` (5个接口)

---

## 🎨 三、组件新增与优化

### 新增Shared层组件

#### 1. StarRating - 星级评分组件 ⚡ P0

**功能**:
- 支持 1-5 星评分
- 只读/可编辑两种模式
- 半星支持(可选)
- 自定义颜色/尺寸

**使用场景**:
- Portal: 评价发布/评价展示
- Admin: 商品审核(查看评分)

**API设计**:
```typescript
interface StarRatingProps {
  value: number;              // 星级值 (0-5)
  onChange?: (value: number) => void;  // 评分变化回调
  readonly?: boolean;         // 只读模式
  size?: 'small' | 'medium' | 'large';
  color?: string;             // 星星颜色
  allowHalf?: boolean;        // 是否支持半星
}
```

---

#### 2. Timeline - 时间轴组件 ⚡ P0

**功能**:
- 垂直/水平布局
- 时间节点高亮
- 支持图标/状态点
- 响应式适配

**使用场景**:
- Portal: 物流轨迹/纠纷协商/申诉进度
- Admin: 审核记录/操作日志

**API设计**:
```typescript
interface TimelineProps {
  items: TimelineItem[];
  direction?: 'vertical' | 'horizontal';
  activeIndex?: number;       // 高亮节点
}

interface TimelineItem {
  time: string;
  title: string;
  description?: string;
  icon?: React.ReactNode;
  status?: 'pending' | 'processing' | 'success' | 'error';
}
```

---

#### 3. FileUploader - 文件上传组件 ⚡ P1

**功能**:
- 支持多种文件类型(图片/视频/PDF)
- 拖拽上传
- 预览/删除
- 进度显示
- 文件大小限制

**使用场景**:
- Portal: 申诉材料/纠纷证据上传
- Admin: 批量导入

**API设计**:
```typescript
interface FileUploaderProps {
  accept?: string;            // 接受的文件类型
  maxSize?: number;           // 最大文件大小(MB)
  maxCount?: number;          // 最多上传数量
  value?: UploadedFile[];
  onChange?: (files: UploadedFile[]) => void;
  onError?: (error: string) => void;
}
```

---

### 优化现有Shared层组件

#### ImageUpload 增强
- 新增视频上传支持
- 新增裁剪功能(可选)
- 优化预览体验

#### Form 增强
- 新增自动保存(草稿箱)
- 新增字段联动验证
- 优化错误提示样式

---

## 🔧 四、技术实现方案

### 服务层架构优化

**当前问题**:
- 服务文件散乱,未按模块分组
- 部分接口未抽象到服务层

**解决方案**:

```
frontend/packages/shared/src/services/
├── auth/                    # 认证模块
│   ├── auth.ts             ✅ 已实现
│   └── types.ts
├── goods/                   # 商品模块
│   ├── goods.ts            ✅ 已实现
│   ├── category.ts         ⚠️ 需独立
│   ├── review.ts           ❌ 新增
│   └── types.ts
├── order/                   # 订单模块
│   ├── order.ts            ✅ 已实现
│   ├── refund.ts           ✅ 已实现
│   ├── logistics.ts        ❌ 新增
│   └── types.ts
├── social/                  # 社交模块
│   ├── message.ts          ✅ 已实现
│   ├── post.ts             ✅ 已实现
│   ├── reply.ts            ❌ 新增
│   ├── topic.ts            ❌ 新增
│   ├── follow.ts           ❌ 新增
│   └── types.ts
├── dispute/                 # 纠纷模块
│   ├── dispute.ts          ❌ 新增
│   └── types.ts
├── system/                  # 系统模块
│   ├── notification.ts     ⚠️ 需独立
│   ├── notificationPref.ts ❌ 新增
│   ├── privacy.ts          ❌ 新增
│   ├── blacklist.ts        ❌ 新增
│   ├── appeal.ts           ⚠️ 已有,需补全
│   ├── subscription.ts     ❌ 新增
│   └── types.ts
├── recommend/               # 推荐模块
│   ├── recommend.ts        ❌ 新增
│   └── types.ts
└── index.ts                 # 统一导出
```

**新增服务层文件**: 14个  
**优化现有文件**: 3个

---

### 状态管理增强

**新增Zustand Store**:

```typescript
// 1. useReviewStore.ts
interface ReviewState {
  myReviews: Review[];
  fetchMyReviews: () => Promise<void>;
  deleteReview: (reviewId: number) => Promise<void>;
}

// 2. useDisputeStore.ts
interface DisputeState {
  disputes: Dispute[];
  unreadCount: number;
  fetchDisputes: () => Promise<void>;
}

// 3. useFollowStore.ts
interface FollowState {
  following: User[];
  followers: User[];
  isFollowing: (userId: number) => boolean;
}

// 4. useBlacklistStore.ts
interface BlacklistState {
  blacklist: User[];
  isBlocked: (userId: number) => boolean;
  blockUser: (userId: number) => Promise<void>;
}
```

---

### 类型定义自动化

**方案**: 使用 `openapi-typescript` 自动生成类型

```bash
# 生成类型定义
npx openapi-typescript http://localhost:8080/v3/api-docs -o src/types/api.d.ts
```

**优点**:
- 类型完全同步后端
- 零维护成本
- 避免手动定义错误

---

## ✅ 五、验收标准

### 功能验收

每个功能模块必须通过:

1. **功能完整性**
   - ✅ 所有用户故事场景可走通
   - ✅ 边界情况处理正确

2. **组件复用率**
   - ✅ Shared层组件复用率 ≥90%
   - ✅ 无重复造轮子

3. **UI/UX 一致性**
   - ✅ 与现有页面风格统一
   - ✅ 移动端适配正常

4. **性能要求**
   - ✅ 首屏加载 <1.5s
   - ✅ 交互响应 <300ms

5. **代码质量**
   - ✅ ESLint/Prettier 通过
   - ✅ TypeScript 无 any 类型
   - ✅ 组件拆分合理(<300行)

### 测试覆盖

- ✅ 关键功能有 E2E 测试
- ✅ 服务层有单元测试(覆盖率 >80%)
- ✅ 新增组件有单元测试

---

## 📅 六、开发计划

### 迭代1: 评价与物流 (Week 1-2)

| 任务 | 工期 | 依赖 |
|------|------|------|
| 新增StarRating组件 | 1天 | - |
| 新增Timeline组件 | 1天 | - |
| 创建review服务层 | 2天 | StarRating |
| 评价发布页 | 2天 | review服务 |
| 评价展示功能 | 3天 | review服务 |
| 创建logistics服务层 | 1天 | Timeline |
| 订单详情集成物流 | 2天 | logistics服务 |
| 创建recommend服务层 | 1天 | - |
| 首页推荐区块 | 2天 | recommend服务 |

**里程碑**: 评价、物流、推荐上线

---

### 迭代2: 纠纷与黑名单 (Week 3-4)

| 任务 | 工期 | 依赖 |
|------|------|------|
| 新增FileUploader组件 | 2天 | - |
| 创建dispute服务层 | 3天 | Timeline, FileUploader |
| 纠纷列表页 | 2天 | dispute服务 |
| 纠纷详情页 | 3天 | dispute服务 |
| 协商功能 | 3天 | dispute服务 |
| 创建blacklist服务层 | 1天 | - |
| 黑名单管理页 | 2天 | blacklist服务 |
| 补全appeal服务层 | 2天 | FileUploader |
| 申诉页面 | 3天 | appeal服务 |

**里程碑**: 纠纷、黑名单、申诉上线

---

### 迭代3: 社交功能 (Week 5-6)

| 任务 | 工期 | 依赖 |
|------|------|------|
| 创建reply服务层 | 2天 | - |
| 帖子详情集成回复 | 3天 | reply服务 |
| 创建topic服务层 | 2天 | - |
| 话题详情页 | 3天 | topic服务 |
| 创建follow服务层 | 2天 | - |
| 关注/粉丝列表页 | 2天 | follow服务 |
| 创建privacy服务层 | 2天 | - |
| 隐私设置页 | 3天 | privacy服务 |

**里程碑**: 社交、隐私功能完成

---

### 迭代4: 优化与补充 (Week 7-8)

| 任务 | 工期 | 依赖 |
|------|------|------|
| 创建subscription服务 | 1天 | - |
| 订阅管理增强 | 2天 | - |
| 创建notificationPref服务 | 1天 | - |
| 通知偏好设置 | 2天 | - |
| 创建goodsBatch服务 | 1天 | - |
| 商品批量操作 | 2天 | - |
| 全面测试 | 3天 | - |
| 性能优化 | 2天 | - |

**里程碑**: 全功能完成,接口覆盖率 >85%

---

## 🚨 七、风险与应对

### 技术风险

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|------|----------|
| 组件复用率不达标 | Medium | Low | 代码审查时严格检查,禁止重复造轮子 |
| 后端接口不稳定 | High | Medium | 提前联调,Mock数据兜底 |
| 新组件开发延期 | Medium | Medium | 预留buffer,关键组件优先开发 |
| 性能问题 | Medium | Low | 图片懒加载,虚拟列表,分页优化 |

---

## 📚 八、附录

### A. Shared层组件使用规范

1. **强制使用Shared层组件**
   - Portal/Admin包禁止重复实现Shared已有组件
   - 发现重复立即重构

2. **组件扩展规范**
   - 若Shared组件不满足需求,先尝试扩展Props
   - 确需新组件时,评估是否应放入Shared层

3. **组件文档维护**
   - 新增组件必须更新 `components/index.ts` 导出
   - 添加详细的TSDoc注释和使用示例

### B. 组件复用检查清单

**开发前检查**:
- [ ] 是否有Shared层组件可复用?
- [ ] 是否有Admin包组件可参考?
- [ ] 能否通过扩展现有组件实现?

**代码审查检查**:
- [ ] 组件复用率 ≥90%?
- [ ] 无重复实现?
- [ ] 新增组件是否应提升到Shared层?

---

**文档版本**: v1.0  
**最后更新**: 2025-11-06  
**下一步**: 生成详细的设计文档(design.md)和任务分解(tasks.md)
