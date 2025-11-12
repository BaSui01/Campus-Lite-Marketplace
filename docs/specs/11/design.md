# Spec #11: 前端门户增强开发 - 技术设计

> **编号**: 11  
> **创建日期**: 2025-11-06  
> **负责人**: BaSui 😎  
> **依赖**: [requirements.md](./requirements.md)

---

## 📋 目录

- [一、架构设计](#一架构设计)
- [二、组件设计](#二组件设计)
- [三、服务层设计](#三服务层设计)
- [四、状态管理设计](#四状态管理设计)
- [五、路由设计](#五路由设计)
- [六、数据流设计](#六数据流设计)
- [七、性能优化设计](#七性能优化设计)
- [八、错误处理设计](#八错误处理设计)

---

## 一、架构设计

### 1.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        Portal 用户端                          │
├─────────────────────────────────────────────────────────────┤
│                         表现层 (Pages)                        │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐   │
│  │  评价页  │  纠纷页  │  话题页  │  申诉页  │  设置页  │   │
│  └─────┬────┴────┬─────┴────┬─────┴────┬─────┴────┬─────┘   │
│        │         │          │          │          │          │
├────────┼─────────┼──────────┼──────────┼──────────┼─────────┤
│                    业务逻辑层 (Services)                      │
│  ┌─────┴─────┬───┴───┬──────┴────┬─────┴────┬────┴──────┐   │
│  │ review.ts │dispute│ topic.ts  │appeal.ts │privacy.ts │   │
│  └───────────┴───────┴───────────┴──────────┴───────────┘   │
│        ↓         ↓          ↓          ↓          ↓          │
├─────────────────────────────────────────────────────────────┤
│                    状态管理层 (Stores)                        │
│  ┌──────────────┬──────────────┬──────────────┬──────────┐  │
│  │ useReviewStr │ useDisputeSt │ useFollowStr │useBlackli│  │
│  └──────────────┴──────────────┴──────────────┴──────────┘  │
├─────────────────────────────────────────────────────────────┤
│                     共享层 (@campus/shared)                   │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ 组件库(22+3) │ 服务层(20+14) │ 工具库 │ 类型定义 │    │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            ↓ HTTP/WebSocket
┌─────────────────────────────────────────────────────────────┐
│                    后端 API (Spring Boot)                    │
│              145个门户接口 (31个Controller)                   │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 技术栈选型

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **UI框架** | React | 18.3.1 | 并发模式 + Suspense |
| **路由** | React Router | 6.26.0 | 客户端路由 |
| **状态管理** | Zustand | 4.5.7 | 轻量级全局状态 |
| **数据请求** | TanStack Query | 5.51.0 | 服务端状态管理 |
| **样式方案** | Tailwind CSS | 3.4.7 | 原子化CSS |
| **HTTP客户端** | Axios | 1.7.2 | 请求拦截 + 错误处理 |
| **表单验证** | Zod | - | TypeScript-first 验证 |
| **日期处理** | Day.js | 1.11.12 | 轻量级日期库 |
| **构建工具** | Vite | 5.4.1 | ESM + HMR |
| **类型检查** | TypeScript | 5.4.5 | 严格模式 |

### 1.3 目录结构设计

```
frontend/packages/portal/src/
├── pages/                         # 页面组件
│   ├── Review/                   # ✅ 新增
│   │   ├── index.tsx            # 评价列表页
│   │   ├── Create.tsx           # 发布评价页
│   │   └── MyReviews.tsx        # 我的评价页
│   ├── Dispute/                  # ✅ 新增
│   │   ├── index.tsx            # 纠纷列表页
│   │   ├── Detail.tsx           # 纠纷详情页
│   │   └── Negotiate.tsx        # 协商页
│   ├── Appeal/                   # ✅ 新增
│   │   ├── index.tsx            # 申诉列表页
│   │   ├── Create.tsx           # 提交申诉页
│   │   └── Detail.tsx           # 申诉详情页
│   ├── Topic/                    # ✅ 新增
│   │   ├── Detail.tsx           # 话题详情页
│   │   └── Following.tsx        # 关注话题页
│   ├── Follow/                   # ✅ 新增
│   │   ├── Following.tsx        # 关注列表页
│   │   └── Followers.tsx        # 粉丝列表页
│   └── Settings/
│       ├── Blacklist.tsx        # ✅ 新增 黑名单页
│       ├── Privacy.tsx          # ✅ 新增 隐私设置页
│       └── Notifications.tsx    # ✅ 新增 通知偏好页
│
├── components/                    # 页面级组件(Portal专属)
│   ├── ErrorBoundary/           # ✅ 已有
│   ├── SliderCaptcha/           # ✅ 已有
│   ├── ReviewCard/              # ✅ 新增 评价卡片
│   ├── DisputeTimeline/         # ✅ 新增 纠纷时间轴
│   ├── LogisticsCard/           # ✅ 新增 物流卡片
│   └── TopicCard/               # ✅ 新增 话题卡片
│
├── store/                         # Zustand 状态管理
│   ├── useAuthStore.ts          # ✅ 已有
│   ├── useNotificationStore.ts  # ✅ 已有
│   ├── useReviewStore.ts        # ✅ 新增
│   ├── useDisputeStore.ts       # ✅ 新增
│   ├── useFollowStore.ts        # ✅ 新增
│   ├── useBlacklistStore.ts     # ✅ 新增
│   └── index.ts
│
├── hooks/                         # 自定义 Hooks
│   ├── useReviewQuery.ts        # ✅ 新增 评价查询
│   ├── useDisputeQuery.ts       # ✅ 新增 纠纷查询
│   ├── useInfiniteScroll.ts     # ✅ 新增 无限滚动
│   └── useDebounce.ts           # ✅ 新增 防抖
│
├── layouts/                       # 布局组件
│   ├── MainLayout/              # ✅ 已有
│   └── AuthLayout/              # ✅ 已有
│
├── router/                        # 路由配置
│   └── index.tsx                # ✅ 需扩展
│
├── types/                         # TypeScript 类型定义
│   ├── api.d.ts                 # ✅ 自动生成(openapi-typescript)
│   └── models.ts                # ✅ 补充自定义类型
│
└── utils/                         # 工具函数
    ├── format.ts                # 格式化工具
    ├── validator.ts             # 验证工具
    └── constants.ts             # 常量定义
```

---

## 二、组件设计

### 2.1 新增Shared层组件

#### 2.1.1 StarRating - 星级评分组件 ⭐

**文件位置**: `frontend/packages/shared/src/components/StarRating/`

**接口设计**:

```typescript
// StarRating.tsx
interface StarRatingProps {
  /** 星级值 (0-5) */
  value: number;
  
  /** 评分变化回调 */
  onChange?: (value: number) => void;
  
  /** 只读模式 */
  readonly?: boolean;
  
  /** 尺寸 */
  size?: 'small' | 'medium' | 'large';
  
  /** 星星颜色 */
  color?: string;
  
  /** 是否支持半星 */
  allowHalf?: boolean;
  
  /** 是否显示数字 */
  showValue?: boolean;
  
  /** 自定义类名 */
  className?: string;
}

export const StarRating: React.FC<StarRatingProps> = ({
  value,
  onChange,
  readonly = false,
  size = 'medium',
  color = '#fadb14',
  allowHalf = false,
  showValue = false,
  className = ''
}) => {
  // 实现逻辑...
};
```

**尺寸映射**:
```typescript
const sizeMap = {
  small: '16px',
  medium: '20px',
  large: '28px'
};
```

**使用示例**:
```tsx
// 只读展示
<StarRating value={4.5} readonly showValue />

// 可编辑
<StarRating 
  value={rating} 
  onChange={setRating} 
  allowHalf 
  size="large" 
/>
```

---

#### 2.1.2 Timeline - 时间轴组件 📅

**文件位置**: `frontend/packages/shared/src/components/Timeline/`

**接口设计**:

```typescript
// Timeline.tsx
interface TimelineItem {
  /** 时间 */
  time: string;
  
  /** 标题 */
  title: string;
  
  /** 描述 */
  description?: string;
  
  /** 图标 */
  icon?: React.ReactNode;
  
  /** 状态 */
  status?: 'pending' | 'processing' | 'success' | 'error' | 'default';
  
  /** 自定义内容 */
  content?: React.ReactNode;
}

interface TimelineProps {
  /** 时间轴数据 */
  items: TimelineItem[];
  
  /** 方向 */
  direction?: 'vertical' | 'horizontal';
  
  /** 当前高亮节点索引 */
  activeIndex?: number;
  
  /** 是否显示时间 */
  showTime?: boolean;
  
  /** 自定义类名 */
  className?: string;
}

export const Timeline: React.FC<TimelineProps> = ({
  items,
  direction = 'vertical',
  activeIndex = -1,
  showTime = true,
  className = ''
}) => {
  // 实现逻辑...
};
```

**状态颜色映射**:
```typescript
const statusColorMap = {
  pending: '#d9d9d9',
  processing: '#1890ff',
  success: '#52c41a',
  error: '#ff4d4f',
  default: '#1890ff'
};
```

**使用示例**:
```tsx
// 物流轨迹
<Timeline 
  items={logistics} 
  activeIndex={0} 
  direction="vertical" 
/>

// 纠纷协商记录
<Timeline 
  items={negotiations} 
  showTime 
/>
```

---

#### 2.1.3 FileUploader - 文件上传组件 📎

**文件位置**: `frontend/packages/shared/src/components/FileUploader/`

**接口设计**:

```typescript
// FileUploader.tsx
interface UploadedFile {
  id: string;
  name: string;
  size: number;
  type: string;
  url: string;
  thumbUrl?: string;
  status: 'uploading' | 'success' | 'error';
  progress?: number;
  error?: string;
}

interface FileUploaderProps {
  /** 接受的文件类型 */
  accept?: string;
  
  /** 最大文件大小(MB) */
  maxSize?: number;
  
  /** 最多上传数量 */
  maxCount?: number;
  
  /** 已上传文件列表 */
  value?: UploadedFile[];
  
  /** 上传变化回调 */
  onChange?: (files: UploadedFile[]) => void;
  
  /** 上传失败回调 */
  onError?: (error: string) => void;
  
  /** 是否支持拖拽 */
  draggable?: boolean;
  
  /** 是否支持多选 */
  multiple?: boolean;
  
  /** 自定义上传函数 */
  customUpload?: (file: File) => Promise<string>;
  
  /** 自定义类名 */
  className?: string;
}

export const FileUploader: React.FC<FileUploaderProps> = ({
  accept = '*',
  maxSize = 10,
  maxCount = 5,
  value = [],
  onChange,
  onError,
  draggable = true,
  multiple = true,
  customUpload,
  className = ''
}) => {
  // 实现逻辑...
};
```

**使用示例**:
```tsx
// 纠纷证据上传
<FileUploader
  accept="image/*,video/*,.pdf"
  maxSize={50}
  maxCount={10}
  value={evidences}
  onChange={setEvidences}
  draggable
  multiple
/>
```

---

### 2.2 新增Portal专属组件

#### 2.2.1 ReviewCard - 评价卡片 💬

**文件位置**: `frontend/packages/portal/src/components/ReviewCard/`

**Props设计**:
```typescript
interface ReviewCardProps {
  review: {
    id: number;
    rating: number;
    content: string;
    images?: string[];
    createdAt: string;
    buyer: {
      id: number;
      nickname: string;
      avatar: string;
    };
    likes: number;
    isLiked: boolean;
    reply?: {
      content: string;
      createdAt: string;
    };
  };
  onLike?: (reviewId: number) => void;
  onReply?: (reviewId: number) => void;
}
```

**复用组件**:
- ✅ `StarRating` - 星级展示
- ✅ `UserAvatar` - 买家头像
- ✅ `Badge` - 点赞数
- ✅ `Button` - 点赞/回复按钮

---

#### 2.2.2 DisputeTimeline - 纠纷时间轴 ⚖️

**Props设计**:
```typescript
interface DisputeTimelineProps {
  records: {
    id: number;
    type: 'message' | 'proposal' | 'evidence' | 'system';
    time: string;
    actor: string;
    content: string;
    proposal?: {
      type: string;
      amount?: number;
      status: 'pending' | 'accepted' | 'rejected';
    };
    evidences?: {
      id: number;
      url: string;
      type: string;
    }[];
  }[];
}
```

**复用组件**:
- ✅ `Timeline` - 基础时间轴
- ✅ `Card` - 方案卡片
- ✅ `Badge` - 状态徽标

---

#### 2.2.3 LogisticsCard - 物流卡片 🚚

**Props设计**:
```typescript
interface LogisticsCardProps {
  logistics: {
    company: string;
    trackingNumber: string;
    status: string;
    currentLocation?: string;
    estimatedDelivery?: string;
    tracks: {
      time: string;
      status: string;
      location: string;
      description: string;
    }[];
  };
  collapsed?: boolean;
  onToggle?: () => void;
}
```

**复用组件**:
- ✅ `Card` - 卡片容器
- ✅ `Timeline` - 物流轨迹
- ✅ `Button` - 展开/收起

---

## 三、服务层设计

### 3.1 服务层架构重组

```
frontend/packages/shared/src/services/
├── auth/                    # 认证模块
│   ├── auth.ts             ✅ 已有
│   └── types.ts
│
├── goods/                   # 商品模块
│   ├── goods.ts            ✅ 已有
│   ├── category.ts         ✅ 新独立
│   ├── review.ts           ✅ 新增
│   ├── favorite.ts         ✅ 新独立
│   └── types.ts
│
├── order/                   # 订单模块
│   ├── order.ts            ✅ 已有
│   ├── refund.ts           ✅ 已有
│   ├── logistics.ts        ✅ 新增
│   └── types.ts
│
├── social/                  # 社交模块
│   ├── message.ts          ✅ 已有
│   ├── post.ts             ✅ 已有
│   ├── reply.ts            ✅ 新增
│   ├── topic.ts            ✅ 新增
│   ├── follow.ts           ✅ 新增
│   ├── community.ts        ✅ 新独立
│   └── types.ts
│
├── dispute/                 # 纠纷模块
│   ├── dispute.ts          ✅ 新增
│   ├── negotiation.ts      ✅ 新增
│   ├── evidence.ts         ✅ 新增
│   ├── arbitration.ts      ✅ 新增
│   └── types.ts
│
├── system/                  # 系统模块
│   ├── notification.ts     ✅ 新独立
│   ├── notificationPref.ts ✅ 新增
│   ├── privacy.ts          ✅ 新增
│   ├── blacklist.ts        ✅ 新增
│   ├── appeal.ts           ⚠️ 补全
│   ├── subscription.ts     ✅ 新独立
│   └── types.ts
│
├── recommend/               # 推荐模块
│   ├── recommend.ts        ✅ 新增
│   └── types.ts
│
├── search/                  # 搜索模块
│   ├── search.ts           ✅ 新独立
│   └── types.ts
│
└── index.ts                 # 统一导出
```

### 3.2 服务层接口设计示例

#### 3.2.1 Review Service

```typescript
// services/goods/review.ts

import { http } from '@campus/shared/utils/http';
import type { 
  Review, 
  ReviewCreateRequest, 
  ReviewListParams, 
  ReviewListResponse 
} from './types';

export interface ReviewService {
  /** 发布评价 */
  createReview(request: ReviewCreateRequest): Promise<number>;
  
  /** 查询商品评价列表 */
  listReviews(goodsId: number, params: ReviewListParams): Promise<ReviewListResponse>;
  
  /** 点赞评价 */
  likeReview(reviewId: number): Promise<void>;
  
  /** 取消点赞 */
  unlikeReview(reviewId: number): Promise<void>;
  
  /** 回复评价(卖家) */
  replyReview(reviewId: number, content: string): Promise<void>;
  
  /** 上传评价图片 */
  uploadReviewMedia(reviewId: number, files: File[]): Promise<string[]>;
  
  /** 获取我的评价 */
  getMyReviews(params: { page: number; size: number }): Promise<ReviewListResponse>;
  
  /** 删除评价 */
  deleteReview(reviewId: number): Promise<void>;
}

class ReviewServiceImpl implements ReviewService {
  async createReview(request: ReviewCreateRequest): Promise<number> {
    const response = await http.post<number>('/api/reviews', request);
    return response.data;
  }
  
  async listReviews(goodsId: number, params: ReviewListParams): Promise<ReviewListResponse> {
    const response = await http.get<ReviewListResponse>(`/api/goods/${goodsId}/reviews`, { params });
    return response.data;
  }
  
  async likeReview(reviewId: number): Promise<void> {
    await http.post(`/api/reviews/${reviewId}/like`);
  }
  
  async unlikeReview(reviewId: number): Promise<void> {
    await http.delete(`/api/reviews/${reviewId}/like`);
  }
  
  // ... 其他方法实现
}

export const reviewService = new ReviewServiceImpl();
```

#### 3.2.2 Dispute Service

```typescript
// services/dispute/dispute.ts

export interface DisputeService {
  /** 发起纠纷 */
  createDispute(orderNo: string, request: CreateDisputeRequest): Promise<number>;
  
  /** 查询我的纠纷列表 */
  listMyDisputes(params: DisputeListParams): Promise<DisputeListResponse>;
  
  /** 查询纠纷详情 */
  getDisputeDetail(disputeId: number): Promise<DisputeDetail>;
  
  /** 关闭纠纷 */
  closeDispute(disputeId: number): Promise<void>;
  
  /** 升级到仲裁 */
  escalateToArbitration(disputeId: number): Promise<void>;
  
  // 协商相关
  negotiation: {
    sendMessage(disputeId: number, message: string): Promise<void>;
    proposeResolution(disputeId: number, proposal: ProposalRequest): Promise<void>;
    respondToProposal(proposalId: number, accept: boolean): Promise<void>;
    getHistory(disputeId: number): Promise<NegotiationRecord[]>;
  };
  
  // 证据相关
  evidence: {
    upload(disputeId: number, files: File[]): Promise<string[]>;
    list(disputeId: number): Promise<Evidence[]>;
    delete(evidenceId: number): Promise<void>;
  };
}
```

---

## 四、状态管理设计

### 4.1 新增Zustand Store

#### 4.1.1 useReviewStore

```typescript
// store/useReviewStore.ts

import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { reviewService } from '@campus/shared/services';

interface ReviewState {
  // 状态
  myReviews: Review[];
  loading: boolean;
  error: string | null;
  
  // 操作
  fetchMyReviews: () => Promise<void>;
  createReview: (request: ReviewCreateRequest) => Promise<void>;
  deleteReview: (reviewId: number) => Promise<void>;
  likeReview: (reviewId: number) => Promise<void>;
  reset: () => void;
}

export const useReviewStore = create<ReviewState>()(
  devtools(
    (set, get) => ({
      // 初始状态
      myReviews: [],
      loading: false,
      error: null,
      
      // 获取我的评价
      fetchMyReviews: async () => {
        set({ loading: true, error: null });
        try {
          const response = await reviewService.getMyReviews({ page: 0, size: 20 });
          set({ myReviews: response.content, loading: false });
        } catch (error) {
          set({ error: (error as Error).message, loading: false });
        }
      },
      
      // 创建评价
      createReview: async (request) => {
        await reviewService.createReview(request);
        await get().fetchMyReviews();
      },
      
      // 删除评价
      deleteReview: async (reviewId) => {
        await reviewService.deleteReview(reviewId);
        set({ myReviews: get().myReviews.filter(r => r.id !== reviewId) });
      },
      
      // 点赞评价
      likeReview: async (reviewId) => {
        await reviewService.likeReview(reviewId);
        // 更新本地状态...
      },
      
      // 重置
      reset: () => set({ myReviews: [], loading: false, error: null })
    }),
    { name: 'ReviewStore' }
  )
);
```

#### 4.1.2 useDisputeStore

```typescript
// store/useDisputeStore.ts

interface DisputeState {
  // 状态
  disputes: Dispute[];
  currentDispute: DisputeDetail | null;
  unreadCount: number;
  
  // 操作
  fetchDisputes: (status?: string) => Promise<void>;
  fetchDisputeDetail: (disputeId: number) => Promise<void>;
  createDispute: (orderNo: string, request: CreateDisputeRequest) => Promise<void>;
  sendMessage: (disputeId: number, message: string) => Promise<void>;
  closeDispute: (disputeId: number) => Promise<void>;
}
```

#### 4.1.3 useFollowStore

```typescript
// store/useFollowStore.ts

interface FollowState {
  // 状态
  following: User[];
  followers: User[];
  followingIds: Set<number>;
  
  // 操作
  fetchFollowing: () => Promise<void>;
  fetchFollowers: () => Promise<void>;
  followUser: (userId: number) => Promise<void>;
  unfollowUser: (userId: number) => Promise<void>;
  isFollowing: (userId: number) => boolean;
}
```

---

## 五、路由设计

### 5.1 新增路由配置

```typescript
// router/index.tsx

// 新增路由
const newRoutes = [
  // 评价模块
  {
    path: 'orders/:orderNo/review',
    element: (
      <RequireAuth>
        <LazyLoadWrapper>
          <ReviewCreate />
        </LazyLoadWrapper>
      </RequireAuth>
    )
  },
  {
    path: 'profile/reviews',
    element: (
      <RequireAuth>
        <LazyLoadWrapper>
          <MyReviews />
        </LazyLoadWrapper>
      </RequireAuth>
    )
  },
  
  // 纠纷模块
  {
    path: 'disputes',
    element: (
      <RequireAuth>
        <LazyLoadWrapper>
          <DisputeList />
        </LazyLoadWrapper>
      </RequireAuth>
    )
  },
  {
    path: 'disputes/:id',
    element: (
      <RequireAuth>
        <LazyLoadWrapper>
          <DisputeDetail />
        </LazyLoadWrapper>
      </RequireAuth>
    )
  },
  
  // 申诉模块
  {
    path: 'appeals',
    element: (
      <RequireAuth>
        <LazyLoadWrapper>
          <AppealList />
        </LazyLoadWrapper>
      </RequireAuth>
    )
  },
  {
    path: 'appeals/create',
    element: (
      <RequireAuth>
        <LazyLoadWrapper>
          <AppealCreate />
        </LazyLoadWrapper>
      </RequireAuth>
    )
  },
  
  // 话题模块
  {
    path: 'topics/:id',
    element: (
      <LazyLoadWrapper>
        <TopicDetail />
      </LazyLoadWrapper>
    )
  },
  
  // 用户关注
  {
    path: 'users/:id/following',
    element: (
      <LazyLoadWrapper>
        <FollowingList />
      </LazyLoadWrapper>
    )
  },
  {
    path: 'users/:id/followers',
    element: (
      <LazyLoadWrapper>
        <FollowersList />
      </LazyLoadWrapper>
    )
  },
  
  // 设置页面
  {
    path: 'settings/blacklist',
    element: (
      <RequireAuth>
        <LazyLoadWrapper>
          <BlacklistSettings />
        </LazyLoadWrapper>
      </RequireAuth>
    )
  },
  {
    path: 'settings/privacy',
    element: (
      <RequireAuth>
        <LazyLoadWrapper>
          <PrivacySettings />
        </LazyLoadWrapper>
      </RequireAuth>
    )
  },
  {
    path: 'settings/notifications',
    element: (
      <RequireAuth>
        <LazyLoadWrapper>
          <NotificationSettings />
        </LazyLoadWrapper>
      </RequireAuth>
    )
  }
];
```

---

## 六、数据流设计

### 6.1 评价系统数据流

```
用户操作                组件层              状态层              服务层              后端API
   │                    │                  │                  │                   │
   ├─ 发布评价 ────────► ReviewCreate ────► useReviewStore ──► reviewService ────► POST /api/reviews
   │                    │                  │   .createReview   │                   │
   │                    │                  │                  │                   │
   ├─ 查看评价 ────────► GoodsDetail ─────► TanStack Query ──► reviewService ────► GET /api/goods/{id}/reviews
   │                    │  (ReviewCard)    │   useQuery        │                   │
   │                    │                  │                  │                   │
   ├─ 点赞评价 ────────► ReviewCard ──────► useReviewStore ──► reviewService ────► POST /api/reviews/{id}/like
   │                    │                  │   .likeReview     │                   │
   │                    │                  │                  │                   │
   └─ 删除评价 ────────► MyReviews ───────► useReviewStore ──► reviewService ────► DELETE /api/reviews/{id}
                         │                  │   .deleteReview   │                   │
```

### 6.2 纠纷处理数据流

```
用户操作                组件层              状态层              服务层              后端API
   │                    │                  │                  │                   │
   ├─ 发起纠纷 ────────► OrderDetail ─────► useDisputeStore ─► disputeService ───► POST /api/disputes
   │                    │  (Modal)         │   .createDispute  │                   │
   │                    │                  │                  │                   │
   ├─ 发送消息 ────────► DisputeDetail ───► useDisputeStore ─► disputeService ───► POST /api/disputes/negotiations/messages
   │                    │  (DisputeTimeline)│   .sendMessage   │                   │
   │                    │                  │                  │                   │
   ├─ 上传证据 ────────► DisputeDetail ───► useDisputeStore ─► disputeService ───► POST /api/disputes/evidence
   │                    │  (FileUploader)  │                  │                   │
   │                    │                  │                  │                   │
   └─ 查看详情 ────────► DisputeDetail ───► TanStack Query ──► disputeService ───► GET /api/disputes/{id}
                         │                  │   useQuery       │                   │
```

---

## 七、性能优化设计

### 7.1 代码分割策略

```typescript
// 路由级代码分割
const ReviewCreate = lazy(() => import('./pages/Review/Create'));
const DisputeDetail = lazy(() => import('./pages/Dispute/Detail'));

// 组件级代码分割
const StarRating = lazy(() => import('@campus/shared/components/StarRating'));

// 按功能模块分包
const reviewModules = () => import('./modules/review');
const disputeModules = () => import('./modules/dispute');
```

### 7.2 图片优化

```typescript
// 懒加载
<img 
  src={image} 
  loading="lazy" 
  alt="评价图片" 
/>

// 响应式图片
<picture>
  <source srcset={image.webp} type="image/webp" />
  <source srcset={image.jpg} type="image/jpeg" />
  <img src={image.jpg} alt="评价图片" />
</picture>

// 缩略图预加载
<img 
  src={thumbUrl} 
  data-full-src={fullUrl} 
  onClick={handleImageClick} 
/>
```

### 7.3 列表虚拟化

```typescript
// 使用 react-window 虚拟化长列表
import { FixedSizeList } from 'react-window';

<FixedSizeList
  height={600}
  itemCount={reviews.length}
  itemSize={150}
  width="100%"
>
  {({ index, style }) => (
    <div style={style}>
      <ReviewCard review={reviews[index]} />
    </div>
  )}
</FixedSizeList>
```

### 7.4 缓存策略

```typescript
// TanStack Query 缓存配置
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,    // 5分钟
      cacheTime: 10 * 60 * 1000,   // 10分钟
      refetchOnWindowFocus: false,
      retry: 1
    }
  }
});

// 评价列表缓存
const { data: reviews } = useQuery({
  queryKey: ['reviews', goodsId, params],
  queryFn: () => reviewService.listReviews(goodsId, params),
  staleTime: 5 * 60 * 1000
});
```

---

## 八、错误处理设计

### 8.1 全局错误处理

```typescript
// utils/errorHandler.ts

export class AppError extends Error {
  constructor(
    public code: string,
    public message: string,
    public status?: number
  ) {
    super(message);
  }
}

export const errorHandler = (error: unknown) => {
  if (error instanceof AppError) {
    // 业务错误
    toast.error(error.message);
  } else if (axios.isAxiosError(error)) {
    // HTTP错误
    const status = error.response?.status;
    const message = error.response?.data?.message || '网络错误';
    
    if (status === 401) {
      // 未授权,跳转登录
      window.location.href = '/login';
    } else if (status === 403) {
      toast.error('无权限访问');
    } else {
      toast.error(message);
    }
  } else {
    // 未知错误
    console.error(error);
    toast.error('系统错误,请稍后重试');
  }
};
```

### 8.2 组件级错误边界

```typescript
// components/ErrorBoundary/index.tsx

export class ErrorBoundary extends React.Component<Props, State> {
  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error };
  }
  
  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('组件错误:', error, errorInfo);
    // 上报错误到监控系统
  }
  
  render() {
    if (this.state.hasError) {
      return (
        <div className="error-fallback">
          <h2>页面加载失败</h2>
          <button onClick={this.handleReset}>重试</button>
        </div>
      );
    }
    
    return this.props.children;
  }
}
```

### 8.3 网络请求错误处理

```typescript
// utils/http.ts

http.interceptors.response.use(
  response => response,
  error => {
    const { response } = error;
    
    // 错误码映射
    const errorMap: Record<number, string> = {
      400: '请求参数错误',
      401: '未登录或登录已过期',
      403: '无权限访问',
      404: '请求的资源不存在',
      500: '服务器错误',
      502: '网关错误',
      503: '服务暂时不可用'
    };
    
    const message = errorMap[response?.status] || '网络错误';
    toast.error(message);
    
    return Promise.reject(error);
  }
);
```

---

## 附录

### A. 类型定义自动生成

```bash
# 使用 openapi-typescript 自动生成类型
npx openapi-typescript http://localhost:8080/v3/api-docs -o src/types/api.d.ts

# 在 package.json 中添加脚本
{
  "scripts": {
    "gen:types": "openapi-typescript http://localhost:8080/v3/api-docs -o src/types/api.d.ts"
  }
}
```

### B. 组件文档模板

每个新增组件需包含:
- README.md - 组件说明
- index.ts - 导出文件
- Component.tsx - 组件实现
- Component.test.tsx - 单元测试
- types.ts - 类型定义

### C. 测试覆盖率要求

- 组件测试: ≥70%
- 服务层测试: ≥85%
- Hooks测试: ≥80%
- E2E测试: 关键流程全覆盖

---

**文档版本**: v1.0  
**最后更新**: 2025-11-06  
**下一步**: 基于本设计文档生成详细的任务分解(tasks.md)
