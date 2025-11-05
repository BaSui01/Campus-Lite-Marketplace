# 🔐 登录重定向功能实现文档

> **实现日期**: 2025-11-05  
> **作者**: BaSui 😎  
> **状态**: ✅ 已完成

---

## 📋 需求说明

### 业务需求

用户在门户（portal）访问时，应该有如下体验：

1. **无感知访问** - 访问公共资源（首页、商品详情、社区等）无需登录
2. **权限拦截** - 点击需要权限的功能（发布商品、我的订单等）时，跳转到登录页
3. **登录回跳** - 登录成功后，自动返回到用户之前访问的页面

### 技术需求

- ✅ 路由守卫：检测未登录用户，保存原路径
- ✅ URL参数：使用 `redirect` 参数传递原路径
- ✅ 登录逻辑：登录成功后读取 `redirect` 参数并跳转
- ✅ API拦截：401错误时自动跳转登录并保存原路径

---

## 🎯 实现方案

### 方案架构

```
┌──────────────┐     未登录      ┌──────────────┐
│ 访问受保护页  │ ──────────────> │ 登录页        │
│ /orders      │                 │ /login?       │
│              │                 │ redirect=/    │
│              │                 │ orders        │
└──────────────┘                 └──────────────┘
                                       │
                                       │ 登录成功
                                       ▼
                                 ┌──────────────┐
                                 │ 返回原页面    │
                                 │ /orders      │
                                 └──────────────┘
```

### 涉及文件

| 文件路径 | 修改内容 | 说明 |
|---------|---------|------|
| `frontend/packages/portal/src/router/index.tsx` | 路由守卫 `RequireAuth` | 保存原路径到 `redirect` 参数 |
| `frontend/packages/portal/src/pages/Login/index.tsx` | 登录逻辑 | 读取 `redirect` 参数并跳转 |
| `frontend/packages/shared/src/utils/apiClient.ts` | Axios拦截器 | 401错误时保存原路径 |

---

## 💻 核心代码实现

### 1. 路由守卫（RequireAuth）

**文件**: `frontend/packages/portal/src/router/index.tsx`

```typescript
/**
 * 需要认证的路由守卫
 */
const RequireAuth = ({ children }: { children: React.ReactNode }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  
  if (!isAuthenticated) {
    // 未登录，保存当前路径并重定向到登录页
    const currentPath = window.location.pathname + window.location.search;
    const loginPath = `/login?redirect=${encodeURIComponent(currentPath)}`;
    
    console.log('[RequireAuth] 未登录，重定向到登录页:', loginPath);
    
    return <Navigate to={loginPath} replace />;
  }

  return <>{children}</>;
};
```

**关键点**：
- ✅ 使用 `window.location.pathname + window.location.search` 获取完整路径
- ✅ 使用 `encodeURIComponent` 编码路径参数
- ✅ 使用 `replace` 避免历史记录堆积

---

### 2. 登录页面（Login）

**文件**: `frontend/packages/portal/src/pages/Login/index.tsx`

```typescript
import { useNavigate, useSearchParams } from 'react-router-dom';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { login } = useAuthStore();
  
  // 获取重定向路径（登录成功后跳转）
  const redirectPath = searchParams.get('redirect') || '/';

  const handleLogin = async () => {
    try {
      await login(formData);
      
      console.log('[Login] ✅ 登录成功，状态已更新');
      console.log('[Login] 📍 跳转到:', redirectPath);
      
      // 跳转到重定向路径或首页
      setTimeout(() => {
        navigate(redirectPath, { replace: true });
      }, 500);
    } catch (error) {
      // 错误处理...
    }
  };
};
```

**关键点**：
- ✅ 使用 `useSearchParams` 读取URL参数
- ✅ 默认跳转到首页 `/` 如果没有 `redirect` 参数
- ✅ 使用 `replace: true` 避免历史记录堆积

---

### 3. Axios拦截器（401处理）

**文件**: `frontend/packages/shared/src/utils/apiClient.ts`

```typescript
// 响应拦截器
instance.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const { response } = error;

    // 🔄 401 Token 过期处理
    if (response?.status === 401) {
      const refreshToken = getRefreshToken();
      if (refreshToken) {
        try {
          // 尝试刷新 Token
          const refreshEndpoint = joinWithBaseUrl(API_BASE_URL, '/api/auth/refresh');
          const { data } = await axios.post(refreshEndpoint, { refreshToken });
          const newAccessToken = data.data?.accessToken;

          if (newAccessToken) {
            setTokens(newAccessToken);
            // 重试原请求
            return instance.request(error.config!);
          }
        } catch (refreshError) {
          console.error('[API Client] ❌ Token 刷新失败:', refreshError);
          clearTokens();
          // 保存当前路径，登录后跳转回来
          const currentPath = window.location.pathname + window.location.search;
          window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
        }
      } else {
        clearTokens();
        // 保存当前路径，登录后跳转回来
        const currentPath = window.location.pathname + window.location.search;
        window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
      }
    }

    return Promise.reject(error);
  }
);
```

**关键点**：
- ✅ 401错误时先尝试刷新Token
- ✅ 刷新失败或无Token时，保存当前路径并跳转登录
- ✅ 使用 `window.location.href` 强制页面跳转（清除状态）

---

## 🚀 使用示例

### 场景1：访问受保护页面

```
1. 用户未登录，访问：http://localhost:8220/orders
2. 路由守卫拦截，跳转到：http://localhost:8220/login?redirect=%2Forders
3. 用户登录成功
4. 自动跳转回：http://localhost:8220/orders
```

### 场景2：API调用返回401

```
1. 用户已登录，访问：http://localhost:8220/profile
2. API调用返回401（Token过期）
3. Axios拦截器尝试刷新Token
4. 刷新失败，跳转到：http://localhost:8220/login?redirect=%2Fprofile
5. 用户重新登录
6. 自动跳转回：http://localhost:8220/profile
```

### 场景3：公共页面无需登录

```
1. 用户未登录，访问：http://localhost:8220/
2. 首页是公共页面，直接展示
3. 用户点击"发布商品"按钮，跳转到：http://localhost:8220/publish
4. 路由守卫拦截，跳转到：http://localhost:8220/login?redirect=%2Fpublish
5. 用户登录成功
6. 自动跳转回：http://localhost:8220/publish
```

---

## 🔍 技术细节

### URL参数编码

使用 `encodeURIComponent` 编码路径参数，避免特殊字符问题：

```typescript
// ✅ 正确
const loginPath = `/login?redirect=${encodeURIComponent('/orders?status=pending')}`;
// 结果: /login?redirect=%2Forders%3Fstatus%3Dpending

// ❌ 错误（特殊字符会破坏URL）
const loginPath = `/login?redirect=/orders?status=pending`;
// 结果: /login?redirect=/orders?status=pending （参数冲突）
```

### 历史记录管理

使用 `replace: true` 避免历史记录堆积：

```typescript
// ✅ 正确：使用 replace，不会在历史记录中留下登录页
navigate(redirectPath, { replace: true });

// ❌ 错误：使用 push，用户点击"后退"会回到登录页
navigate(redirectPath);
```

### 默认跳转路径

当没有 `redirect` 参数时，默认跳转到首页：

```typescript
const redirectPath = searchParams.get('redirect') || '/';
```

---

## 🐛 常见问题

### Q1: 为什么有时候登录后跳转到首页而不是原页面？

**原因**：URL参数丢失或未正确传递。

**解决**：检查以下几点：
1. 确保路由守卫使用了 `encodeURIComponent`
2. 确保登录页使用了 `useSearchParams`
3. 检查浏览器控制台日志，查看 `redirect` 参数

### Q2: 为什么Axios拦截器使用 `window.location.href` 而不是 `navigate`?

**原因**：401错误通常表示认证状态失效，需要清除所有状态并重新加载页面。

**区别**：
- `window.location.href`：强制页面刷新，清除所有状态
- `navigate`：客户端路由跳转，状态可能残留

### Q3: 如何测试登录重定向功能？

**测试步骤**：
1. 清除浏览器LocalStorage（清除Token）
2. 访问受保护页面，如 `/orders`
3. 检查是否跳转到 `/login?redirect=%2Forders`
4. 登录成功后，检查是否跳转回 `/orders`

---

## ✅ 验收标准

### 功能测试

- [x] 未登录访问受保护页面，跳转到登录页并保存原路径
- [x] 登录成功后，自动跳转回原页面
- [x] API调用返回401，跳转到登录页并保存原路径
- [x] 公共页面无需登录，可直接访问
- [x] URL参数正确编码，支持复杂路径（带查询参数）

### 边界测试

- [x] redirect参数为空时，默认跳转首页
- [x] redirect参数被篡改时，不影响正常功能
- [x] 用户手动清除Token，下次访问受保护页面时正常拦截

---

## 📚 相关文档

- [React Router 文档](https://reactrouter.com/)
- [Zustand 状态管理](https://github.com/pmndrs/zustand)
- [Axios 拦截器](https://axios-http.com/docs/interceptors)
- [URL 参数编码](https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Global_Objects/encodeURIComponent)

---

## 🎉 总结

本功能实现了完整的登录重定向体验：

1. ✅ **路由守卫** - 拦截未登录用户，保存原路径
2. ✅ **登录回跳** - 登录成功后返回原页面
3. ✅ **API拦截** - 401错误时自动跳转并保存原路径
4. ✅ **用户体验** - 无感知访问公共资源，权限拦截平滑自然

**最后更新**: 2025-11-05  
**维护者**: BaSui 😎
