# 🌐 自动生成的 API 客户端

> **自动生成 - 请勿手动修改！**  
> 本目录的所有文件由 OpenAPI Generator 根据后端 API 自动生成

---

## 📋 目录结构

```
api/
├── api/                    # API 接口方法类
│   ├── default-api.ts     # 主要 API 接口
│   └── dispute-statistics-api.ts  # 纠纷统计 API
├── models/                 # TypeScript 类型定义（252个DTO）
│   ├── user.ts
│   ├── goods.ts
│   └── ...
├── base.ts                 # Axios 基础配置
├── common.ts               # 通用工具函数
├── configuration.ts        # API 配置类
└── index.ts                # 统一导出入口
```

---

## 🚀 快速使用

### 基础配置

```typescript
import { Configuration, DefaultApi } from '@campus/shared/api';

// 创建配置（可选，设置baseURL和token）
const config = new Configuration({
  basePath: 'http://localhost:8200/api',
  accessToken: 'your-jwt-token-here'
});

// 创建 API 实例
const api = new DefaultApi(config);
```

### 认证相关

```typescript
// 用户注册
const registerResponse = await api.register({
  registerRequest: {
    username: 'testuser',
    password: 'password123',
    email: 'test@example.com',
    phoneNumber: '13800138000',
    campusId: 1
  }
});

// 用户登录
const loginResponse = await api.login({
  loginRequest: {
    username: 'testuser',
    password: 'password123'
  }
});

const token = loginResponse.data.data;
console.log('JWT Token:', token);

// 获取当前用户信息
const profileResponse = await api.getUserProfile();
console.log('User Profile:', profileResponse.data.data);
```

### 商品相关

```typescript
// 获取商品列表（分页）
const goodsListResponse = await api.listGoods({
  page: 0,
  size: 20,
  campusId: 1,
  categoryId: 1,
  status: 'ON_SALE'
});

console.log('商品列表:', goodsListResponse.data.data);

// 获取商品详情
const goodsDetailResponse = await api.getGoodsDetail({
  id: 1
});

console.log('商品详情:', goodsDetailResponse.data.data);

// 发布商品
const createGoodsResponse = await api.createGoods({
  createGoodsRequest: {
    title: '全新iPhone 15',
    description: '未拆封，原价转让',
    price: 5999,
    categoryId: 1,
    images: ['https://example.com/image1.jpg']
  }
});
```

### 订单相关

```typescript
// 创建订单
const createOrderResponse = await api.createOrder({
  createOrderRequest: {
    goodsId: 1,
    buyerMessage: '请尽快发货'
  }
});

// 获取我的订单列表
const myOrdersResponse = await api.listMyOrders({
  page: 0,
  size: 20
});
```

---

## 🎯 高级用法

### 使用 React Query（推荐）

```typescript
import { useQuery, useMutation } from '@tanstack/react-query';
import { DefaultApi } from '@campus/shared/api';

const api = new DefaultApi();

// 查询商品列表
function useGoodsList(campusId: number) {
  return useQuery({
    queryKey: ['goods', 'list', campusId],
    queryFn: () => api.listGoods({ campusId, page: 0, size: 20 })
  });
}

// 发布商品
function useCreateGoods() {
  return useMutation({
    mutationFn: (data: CreateGoodsRequest) =>
      api.createGoods({ createGoodsRequest: data }),
    onSuccess: () => {
      console.log('商品发布成功！');
    }
  });
}

// 在组件中使用
function GoodsListPage() {
  const { data, isLoading, error } = useGoodsList(1);
  const createGoods = useCreateGoods();

  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Error: {error.message}</div>;

  return (
    <div>
      <h1>商品列表</h1>
      {data?.data.data?.map(goods => (
        <div key={goods.id}>{goods.title}</div>
      ))}
    </div>
  );
}
```

### 自定义 Axios 配置

```typescript
import axios from 'axios';
import { DefaultApi, Configuration } from '@campus/shared/api';

// 创建自定义 axios 实例
const axiosInstance = axios.create({
  baseURL: 'http://localhost:8200/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 添加请求拦截器（自动添加 Token）
axiosInstance.interceptors.request.use(config => {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 添加响应拦截器（统一错误处理）
axiosInstance.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // 跳转到登录页
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// 使用自定义 axios 实例
const config = new Configuration();
const api = new DefaultApi(config, undefined, axiosInstance);
```

---

## 🔄 重新生成

当后端 API 变更后，重新生成前端客户端：

### 方式 1：自动监听（推荐）
```bash
cd frontend
pnpm api:watch
```

### 方式 2：手动生成
```bash
cd frontend
pnpm api:generate
```

### 方式 3：清理重建
```bash
cd frontend
pnpm api:clean
pnpm api:generate
```

---

## 📝 类型定义示例

所有 DTO 都有完整的 TypeScript 类型定义：

```typescript
import type {
  ApiResponse,
  User,
  Goods,
  Order,
  CreateGoodsRequest,
  UpdateGoodsRequest
} from '@campus/shared/api';

// 类型安全的函数参数
function processGoods(goods: Goods): void {
  console.log(goods.id);        // ✅ 类型提示
  console.log(goods.title);     // ✅ 类型提示
  // console.log(goods.invalid); // ❌ 编译错误！
}

// 类型安全的 API 响应
async function fetchGoods(id: number): Promise<ApiResponse<Goods>> {
  const response = await api.getGoodsDetail({ id });
  return response.data; // ApiResponse<Goods>
}
```

---

## 🐛 常见问题

### Q: 为什么我的 API 调用没有返回数据？
```typescript
// ❌ 错误：没有从 response.data 中提取数据
const goods = await api.getGoodsDetail({ id: 1 });
console.log(goods); // AxiosResponse 对象

// ✅ 正确：从 response.data.data 中提取
const goods = await api.getGoodsDetail({ id: 1 });
console.log(goods.data.data); // 实际的商品数据
```

### Q: 如何处理 Token 过期？
```typescript
// 在 axios 拦截器中统一处理
axiosInstance.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // 清除本地 Token
      localStorage.removeItem('jwt_token');
      // 跳转到登录页
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### Q: 如何在多个环境中切换 baseURL？
```typescript
const baseURL = process.env.REACT_APP_API_URL || 'http://localhost:8200/api';

const config = new Configuration({
  basePath: baseURL
});
```

---

## 📚 参考资源

- [OpenAPI Generator 文档](https://openapi-generator.tech/)
- [Axios 文档](https://axios-http.com/)
- [React Query 文档](https://tanstack.com/query/latest)
- [项目 API 文档](http://localhost:8200/api/swagger-ui/index.html)

---

**最后更新**: 2025-11-05  
**生成工具**: OpenAPI Generator 7.6.0  
**后端版本**: 1.0.0-SNAPSHOT
