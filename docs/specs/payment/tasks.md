# 📋 支付系统用户端页面任务分解文档

> **作者**: BaSui 😎 | **创建**: 2025-11-07 | **工期**: 3天
> **开发方法**: TDD十步流程 | **测试覆盖率**: ≥85%

---

## 🎯 任务总览

### 项目目标
完成最后一个P0级功能 - 支付系统用户端页面，包括支付状态查询、支付结果展示和支付方式管理三个核心页面。

### 开发方法论
严格遵循 **TDD十步流程**：
1. 🔍 复用检查
2. 🔴 编写测试
3. 🟢 编写实体
4. 🟢 编写DTO
5. 🟢 编写Mapper
6. 🟢 编写Service接口
7. 🟢 编写Service实现
8. 🟢 编写Controller
9. 🔵 运行测试
10. 🔵 重构优化

### 任务清单概览
| 阶段 | 任务 | 预估工时 | 负责人 | 状态 |
|------|------|----------|--------|------|
| **Day 1** | 复用检查 + PaymentStatus页面 | 6小时 | BaSui | 📋 待开始 |
| **Day 2** | PaymentResult页面 | 6小时 | BaSui | 📋 待开始 |
| **Day 3** | PaymentMethods页面 + 集成测试 | 6小时 | BaSui | 📋 待开始 |

---

## 📅 详细任务分解

### 🔄 第0步：复用检查（30分钟）

**目标**: 最大化复用现有组件和服务，避免重复开发

**任务内容**:
- [ ] 搜索现有支付相关组件和服务
- [ ] 分析API接口复用可能性
- [ ] 检查UI组件库可用组件
- [ ] 评估工具函数复用度

**具体执行**:
```bash
# 1. 搜索现有支付相关代码
find frontend/packages -name "*.ts*" -exec grep -l "payment\|Payment" {} \;

# 2. 搜索订单相关组件
find frontend/packages -name "*.tsx" -exec grep -l "order\|Order" {} \;

# 3. 检查API服务
ls -la frontend/packages/shared/src/services/
```

**预期结果**:
- ✅ 复用 `orderService.queryPaymentStatus()` API
- ✅ 复用 WebSocket 订单状态更新
- ✅ 复用 PaymentMethod 枚举和类型定义
- ✅ 复用现有 Button、Card 等 UI 组件

---

## 🚀 Day 1: PaymentStatus 页面开发（6小时）

### 🔍 复用检查（已完成）

### 🔴 第1步：编写PaymentStatus测试（1小时）

**文件**: `frontend/packages/portal/src/pages/Payment/__tests__/PaymentStatus.test.tsx`

**测试内容**:
```typescript
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import PaymentStatus from '../PaymentStatus';

// Mock现有服务
jest.mock('../../../../shared/src/services/order', () => ({
  orderService: {
    queryPaymentStatus: jest.fn(),
    getOrderDetail: jest.fn(),
  },
}));

jest.mock('../../../../shared/src/services/websocket', () => ({
  websocketService: {
    onOrderUpdate: jest.fn(),
    offOrderUpdate: jest.fn(),
    isConnected: jest.fn(() => true),
  },
}));

describe('PaymentStatus', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });
  });

  // 测试1: 正常渲染
  it('应该正常渲染支付状态页面', async () => {
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/payment?orderNo=O202511070001']}>
          <PaymentStatus />
        </MemoryRouter>
      </QueryClientProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('支付状态')).toBeInTheDocument();
    });
  });

  // 测试2: 支付成功状态
  it('应该显示支付成功状态', async () => {
    const mockOrderService = require('../../../../shared/src/services/order').orderService;
    mockOrderService.queryPaymentStatus.mockResolvedValue('SUCCESS');
    mockOrderService.getOrderDetail.mockResolvedValue({
      orderNo: 'O202511070001',
      status: 'PAID',
      actualAmount: 100.00,
    });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/payment?orderNo=O202511070001']}>
          <PaymentStatus />
        </MemoryRouter>
      </QueryClientProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('支付成功')).toBeInTheDocument();
      expect(screen.getByText('¥100.00')).toBeInTheDocument();
    });
  });

  // 测试3: 支付失败状态
  it('应该显示支付失败状态并提供重试选项', async () => {
    const mockOrderService = require('../../../../shared/src/services/order').orderService;
    mockOrderService.queryPaymentStatus.mockResolvedValue('FAILED');
    mockOrderService.getOrderDetail.mockResolvedValue({
      orderNo: 'O202511070001',
      status: 'PENDING_PAYMENT',
      actualAmount: 100.00,
    });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/payment?orderNo=O202511070001']}>
          <PaymentStatus />
        </MemoryRouter>
      </QueryClientProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('支付失败')).toBeInTheDocument();
      expect(screen.getByText('重新支付')).toBeInTheDocument();
    });
  });

  // 测试4: 实时状态更新
  it('应该支持WebSocket实时状态更新', async () => {
    const mockWebSocket = require('../../../../shared/src/services/websocket').websocketService;

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/payment?orderNo=O202511070001']}>
          <PaymentStatus />
        </MemoryRouter>
      </QueryClientProvider>
    );

    await waitFor(() => {
      expect(mockWebSocket.onOrderUpdate).toHaveBeenCalled();
    });
  });

  // 测试5: 倒计时功能
  it('应该显示支付倒计时', async () => {
    const mockOrderService = require('../../../../shared/src/services/order').orderService;
    mockOrderService.getOrderDetail.mockResolvedValue({
      orderNo: 'O202511070001',
      status: 'PENDING_PAYMENT',
      actualAmount: 100.00,
      expireTime: new Date(Date.now() + 30 * 60 * 1000).toISOString(), // 30分钟后过期
    });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/payment?orderNo=O202511070001']}>
          <PaymentStatus />
        </MemoryRouter>
      </QueryClientProvider>
    );

    await waitFor(() => {
      expect(screen.getByText(/支付剩余时间/)).toBeInTheDocument();
    });
  });
});
```

### 🟢 第2步：创建PaymentStatus组件目录（15分钟）

**目录结构**:
```
frontend/packages/portal/src/pages/Payment/
├── PaymentStatus.tsx
├── PaymentResult.tsx
├── PaymentMethods.tsx
├── components/
│   ├── PaymentProgress.tsx
│   ├── StatusIcon.tsx
│   ├── CountdownTimer.tsx
│   └── ResultCard.tsx
├── hooks/
│   ├── usePayment.ts
│   ├── useCountdown.ts
│   └── usePaymentWebSocket.ts
├── utils/
│   ├── paymentUtils.ts
│   └── formatUtils.ts
├── __tests__/
│   ├── PaymentStatus.test.tsx
│   ├── PaymentResult.test.tsx
│   └── PaymentMethods.test.tsx
└── index.tsx
```

**执行命令**:
```bash
mkdir -p frontend/packages/portal/src/pages/Payment/{components,hooks,utils,__tests__}
touch frontend/packages/portal/src/pages/Payment/{PaymentStatus,PaymentResult,PaymentMethods,index}.tsx
touch frontend/packages/portal/src/pages/Payment/components/{PaymentProgress,StatusIcon,CountdownTimer,ResultCard}.tsx
touch frontend/packages/portal/src/pages/Payment/hooks/{usePayment,useCountdown,usePaymentWebSocket}.ts
touch frontend/packages/portal/src/pages/Payment/utils/{paymentUtils,formatUtils}.ts
touch frontend/packages/portal/src/pages/Payment/__tests__/{PaymentStatus,PaymentResult,PaymentMethods}.test.tsx
```

### 🟢 第3步：实现工具函数（45分钟）

**文件**: `frontend/packages/portal/src/pages/Payment/utils/paymentUtils.ts`

```typescript
import { PaymentStatus } from '../../../../shared/src/types/enum';

/**
 * 获取支付状态文本
 */
export const getPaymentStatusText = (status: string): string => {
  const statusMap = {
    'PENDING': '等待支付',
    'SUCCESS': '支付成功',
    'FAILED': '支付失败',
    'TIMEOUT': '支付超时',
    'CANCELLED': '已取消',
    'REFUNDED': '已退款',
  };

  return statusMap[status] || '未知状态';
};

/**
 * 获取支付状态图标
 */
export const getPaymentStatusIcon = (status: string): string => {
  const iconMap = {
    'PENDING': '⏳',
    'SUCCESS': '✅',
    'FAILED': '❌',
    'TIMEOUT': '⏰',
    'CANCELLED': '🚫',
    'REFUNDED': '💰',
  };

  return iconMap[status] || '❓';
};

/**
 * 计算支付进度百分比
 */
export const calculatePaymentProgress = (
  status: string,
  createTime: string,
  expireTime: string
): number => {
  if (status === 'SUCCESS') return 100;
  if (status === 'FAILED' || status === 'CANCELLED') return 0;

  const now = new Date().getTime();
  const create = new Date(createTime).getTime();
  const expire = new Date(expireTime).getTime();

  if (now >= expire) return 100;

  const total = expire - create;
  const elapsed = now - create;

  return Math.min(100, Math.max(0, (elapsed / total) * 100));
};

/**
 * 判断是否需要轮询
 */
export const shouldPollPayment = (status: string): boolean => {
  return ['PENDING'].includes(status);
};

/**
 * 验证订单号格式
 */
export const validateOrderNo = (orderNo: string): boolean => {
  const pattern = /^O\d{8}\d{4}$/; // O + 年月日 + 序号
  return pattern.test(orderNo);
};
```

**文件**: `frontend/packages/portal/src/pages/Payment/utils/formatUtils.ts`

```typescript
/**
 * 格式化货币显示
 */
export const formatCurrency = (amount: number | string): string => {
  const num = typeof amount === 'string' ? parseFloat(amount) : amount;
  return `¥${num.toFixed(2)}`;
};

/**
 * 格式化时间显示
 */
export const formatDateTime = (dateTime: string): string => {
  const date = new Date(dateTime);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

/**
 * 格式化倒计时显示
 */
export const formatCountdown = (milliseconds: number): string => {
  const totalSeconds = Math.floor(milliseconds / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;

  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
};

/**
 * 格式化相对时间
 */
export const formatRelativeTime = (dateTime: string): string => {
  const date = new Date(dateTime);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  const minutes = Math.floor(diff / (1000 * 60));
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;

  return formatDateTime(dateTime);
};
```

### 🟢 第4步：实现核心Hook（1小时）

**文件**: `frontend/packages/portal/src/pages/Payment/hooks/usePayment.ts`

```typescript
import { useState, useEffect, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { orderService } from '../../../../shared/src/services/order';
import { websocketService } from '../../../../shared/src/services/websocket';

interface UsePaymentOptions {
  orderNo: string;
  autoPoll?: boolean;
  websocketEnabled?: boolean;
}

export const usePayment = (options: UsePaymentOptions) => {
  const [status, setStatus] = useState<string>('PENDING');
  const [error, setError] = useState<string | null>(null);

  // 支付状态查询
  const {
    data: paymentStatus,
    isLoading,
    refetch,
    error: queryError
  } = useQuery({
    queryKey: ['payment-status', options.orderNo],
    queryFn: () => orderService.queryPaymentStatus(options.orderNo),
    refetchInterval: options.autoPoll ? 3000 : false,
    enabled: !!options.orderNo && validateOrderNo(options.orderNo),
    staleTime: 1000,
  });

  // WebSocket实时更新
  useEffect(() => {
    if (!options.websocketEnabled) return;

    const handleOrderUpdate = (data: any) => {
      if (data.orderNo === options.orderNo) {
        setStatus(data.status);
        refetch();
      }
    };

    websocketService.onOrderUpdate(handleOrderUpdate);

    // 确保WebSocket已连接
    if (!websocketService.isConnected()) {
      websocketService.connect();
    }

    return () => {
      websocketService.offOrderUpdate(handleOrderUpdate);
    };
  }, [options.orderNo, options.websocketEnabled, refetch]);

  // 错误处理
  useEffect(() => {
    if (queryError) {
      setError(queryError.message || '查询支付状态失败');
    }
  }, [queryError]);

  // 手动刷新状态
  const refreshStatus = useCallback(() => {
    setError(null);
    refetch();
  }, [refetch]);

  return {
    status: paymentStatus || status,
    isLoading,
    error,
    refreshStatus,
  };
};

// 订单号验证函数
const validateOrderNo = (orderNo: string): boolean => {
  const pattern = /^O\d{8}\d{4}$/;
  return pattern.test(orderNo);
};
```

**文件**: `frontend/packages/portal/src/pages/Payment/hooks/useCountdown.ts`

```typescript
import { useState, useEffect } from 'react';

interface UseCountdownOptions {
  targetTime: string;
  onExpire?: () => void;
}

export const useCountdown = ({ targetTime, onExpire }: UseCountdownOptions) => {
  const [remainingTime, setRemainingTime] = useState(0);
  const [isExpired, setIsExpired] = useState(false);

  useEffect(() => {
    const calculateRemaining = () => {
      const now = new Date().getTime();
      const target = new Date(targetTime).getTime();
      const remaining = Math.max(0, target - now);

      setRemainingTime(remaining);

      if (remaining === 0 && !isExpired) {
        setIsExpired(true);
        onExpire?.();
      }
    };

    calculateRemaining();
    const timer = setInterval(calculateRemaining, 1000);

    return () => clearInterval(timer);
  }, [targetTime, isExpired, onExpire]);

  const formatTime = (milliseconds: number): string => {
    const totalSeconds = Math.floor(milliseconds / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }

    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };

  return {
    remainingTime,
    isExpired,
    formattedTime: formatTime(remainingTime),
  };
};
```

### 🟢 第5步：实现UI组件（1.5小时）

**文件**: `frontend/packages/portal/src/pages/Payment/components/StatusIcon.tsx`

```typescript
import React from 'react';
import { getPaymentStatusIcon } from '../utils/paymentUtils';

interface StatusIconProps {
  status: string;
  size?: 'small' | 'medium' | 'large';
  className?: string;
}

export const StatusIcon: React.FC<StatusIconProps> = ({
  status,
  size = 'medium',
  className = ''
}) => {
  const icon = getPaymentStatusIcon(status);

  const sizeClasses = {
    small: 'text-xl',
    medium: 'text-2xl',
    large: 'text-4xl',
  };

  return (
    <span className={`${sizeClasses[size]} ${className}`}>
      {icon}
    </span>
  );
};
```

**文件**: `frontend/packages/portal/src/pages/Payment/components/CountdownTimer.tsx`

```typescript
import React from 'react';
import { useCountdown } from '../hooks/useCountdown';
import { formatCountdown } from '../utils/formatUtils';

interface CountdownTimerProps {
  expireTime: string;
  onExpire?: () => void;
  className?: string;
}

export const CountdownTimer: React.FC<CountdownTimerProps> = ({
  expireTime,
  onExpire,
  className = ''
}) => {
  const { formattedTime, isExpired } = useCountdown({
    targetTime: expireTime,
    onExpire,
  });

  if (isExpired) {
    return (
      <div className={`text-red-500 ${className}`}>
        ⏰ 支付已超时
      </div>
    );
  }

  return (
    <div className={`text-gray-600 ${className}`}>
      ⏱️ 支付剩余时间：{formattedTime}
    </div>
  );
};
```

**文件**: `frontend/packages/portal/src/pages/Payment/components/PaymentProgress.tsx`

```typescript
import React from 'react';
import { StatusIcon } from './StatusIcon';
import { CountdownTimer } from './CountdownTimer';
import { getPaymentStatusText, calculatePaymentProgress } from '../utils/paymentUtils';

interface PaymentProgressProps {
  status: string;
  orderInfo: any;
  className?: string;
}

export const PaymentProgress: React.FC<PaymentProgressProps> = ({
  status,
  orderInfo,
  className = ''
}) => {
  const progressPercentage = calculatePaymentProgress(
    status,
    orderInfo?.createTime,
    orderInfo?.expireTime
  );

  const statusColor = {
    'PENDING': 'bg-blue-500',
    'SUCCESS': 'bg-green-500',
    'FAILED': 'bg-red-500',
    'TIMEOUT': 'bg-gray-500',
    'CANCELLED': 'bg-gray-500',
  }[status] || 'bg-gray-500';

  return (
    <div className={`bg-white rounded-lg shadow-md p-6 ${className}`}>
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center space-x-3">
          <StatusIcon status={status} size="large" />
          <div>
            <h3 className="text-lg font-semibold">
              {getPaymentStatusText(status)}
            </h3>
            <p className="text-gray-600 text-sm">
              订单号：{orderInfo?.orderNo}
            </p>
          </div>
        </div>

        <div className="text-right">
          <div className="text-2xl font-bold text-blue-600">
            ¥{orderInfo?.actualAmount || '0.00'}
          </div>
          <div className="text-sm text-gray-500">
            订单金额
          </div>
        </div>
      </div>

      {/* 进度条 */}
      <div className="mb-4">
        <div className="flex justify-between text-sm text-gray-600 mb-2">
          <span>支付进度</span>
          <span>{Math.round(progressPercentage)}%</span>
        </div>
        <div className="w-full bg-gray-200 rounded-full h-3">
          <div
            className={`h-3 rounded-full transition-all duration-300 ${statusColor}`}
            style={{ width: `${progressPercentage}%` }}
          />
        </div>
      </div>

      {/* 倒计时 */}
      {orderInfo?.expireTime && (
        <div className="flex justify-center">
          <CountdownTimer
            expireTime={orderInfo.expireTime}
            onExpire={() => {
              // 支付超时处理
              console.log('支付超时');
            }}
          />
        </div>
      )}
    </div>
  );
};
```

### 🟢 第6步：实现PaymentStatus主组件（1小时）

**文件**: `frontend/packages/portal/src/pages/Payment/PaymentStatus.tsx`

```typescript
import React, { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { orderService } from '../../../shared/src/services/order';
import { usePayment } from './hooks/usePayment';
import { PaymentProgress } from './components/PaymentProgress';
import { validateOrderNo } from './utils/paymentUtils';

const PaymentStatus: React.FC = () => {
  const { orderNo } = useParams<{ orderNo: string }>();
  const navigate = useNavigate();

  // 参数验证
  useEffect(() => {
    if (!orderNo || !validateOrderNo(orderNo)) {
      toast.error('无效的订单号');
      navigate('/orders');
      return;
    }
  }, [orderNo, navigate]);

  // 获取订单详情
  const {
    data: orderInfo,
    isLoading: orderLoading,
    error: orderError
  } = useQuery({
    queryKey: ['order-detail', orderNo],
    queryFn: () => orderService.getOrderDetail(orderNo!),
    enabled: !!orderNo && validateOrderNo(orderNo),
  });

  // 支付状态管理
  const {
    status: paymentStatus,
    isLoading: paymentLoading,
    error: paymentError,
    refreshStatus,
  } = usePayment({
    orderNo: orderNo!,
    autoPoll: true,
    websocketEnabled: true,
  });

  // 错误处理
  useEffect(() => {
    if (orderError || paymentError) {
      toast.error(orderError?.message || paymentError?.message || '加载失败');
    }
  }, [orderError, paymentError]);

  // 支付成功处理
  useEffect(() => {
    if (paymentStatus === 'SUCCESS' && orderInfo) {
      toast.success('支付成功！🎉');
      // 延迟跳转，让用户看到成功状态
      setTimeout(() => {
        navigate(`/payment/result?orderNo=${orderNo}&status=SUCCESS`);
      }, 2000);
    }
  }, [paymentStatus, orderInfo, navigate, orderNo]);

  // 支付失败处理
  useEffect(() => {
    if (paymentStatus === 'FAILED' && orderInfo) {
      navigate(`/payment/result?orderNo=${orderNo}&status=FAILED`);
    }
  }, [paymentStatus, orderInfo, navigate, orderNo]);

  if (orderLoading || paymentLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">正在查询支付状态...</p>
        </div>
      </div>
    );
  }

  if (!orderInfo) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-500 text-6xl mb-4">😔</div>
          <h2 className="text-xl font-semibold mb-2">订单不存在</h2>
          <p className="text-gray-600 mb-4">请检查订单号是否正确</p>
          <button
            onClick={() => navigate('/orders')}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
          >
            返回订单列表
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            支付状态查询
          </h1>
          <p className="text-gray-600">
            正在实时监控您的支付状态，请稍候...
          </p>
        </div>

        <PaymentProgress
          status={paymentStatus}
          orderInfo={orderInfo}
        />

        {/* 操作按钮 */}
        <div className="mt-6 flex justify-center space-x-4">
          <button
            onClick={refreshStatus}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
          >
            🔄 刷新状态
          </button>

          <button
            onClick={() => navigate(`/order/${orderNo}`)}
            className="bg-gray-600 text-white px-6 py-2 rounded-lg hover:bg-gray-700 transition-colors"
          >
            📋 订单详情
          </button>
        </div>

        {/* 帮助信息 */}
        <div className="mt-8 bg-blue-50 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-blue-900 mb-3">
            💡 支付说明
          </h3>
          <ul className="space-y-2 text-blue-800">
            <li>• 支付成功后会自动跳转到结果页面</li>
            <li>• 如遇支付问题，请尝试重新支付</li>
            <li>• 支付完成后请勿关闭页面，等待状态更新</li>
            <li>• 如有疑问，请联系客服</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default PaymentStatus;
```

### 🔵 第7步：运行测试验证（30分钟）

**执行测试**:
```bash
cd frontend/packages/portal

# 运行PaymentStatus测试
npm test -- --testPathPattern=PaymentStatus.test.tsx --watchAll=false

# 检查测试覆盖率
npm test -- --testPathPattern=PaymentStatus --coverage --watchAll=false
```

**预期结果**:
- ✅ 所有测试通过
- ✅ 测试覆盖率 ≥ 85%
- ✅ 无TypeScript错误
- ✅ 无ESLint错误

### 🔵 第8步：集成路由配置（15分钟）

**文件**: `frontend/packages/portal/src/App.tsx`

```typescript
// 添加支付页面路由
import PaymentStatus from './pages/Payment/PaymentStatus';

// 在路由配置中添加
{
  path: '/payment',
  element: <PaymentStatus />,
},
```

### 🔵 第9步：手动测试验证（30分钟）

**测试清单**:
- [ ] 访问 `/payment?orderNo=O202511070001` 正常显示
- [ ] 支付状态实时更新正常
- [ ] 倒计时功能正常
- [ ] 进度条显示正确
- [ ] 刷新按钮功能正常
- [ ] 跳转订单详情正常
- [ ] 响应式设计正常

---

## 🚀 Day 2: PaymentResult页面开发（6小时）

### 🔴 第1步：编写PaymentResult测试（1小时）

### 🟢 第2-7步：实现PaymentResult组件（4小时）

**文件**: `frontend/packages/portal/src/pages/Payment/PaymentResult.tsx`

```typescript
import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { orderService } from '../../../shared/src/services/order';
import { ResultCard } from './components/ResultCard';
import { getPaymentStatusText } from './utils/paymentUtils';

const PaymentResult: React.FC = () => {
  const { orderNo, status } = useParams<{
    orderNo: string;
    status: 'SUCCESS' | 'FAILED' | 'TIMEOUT';
  }>();
  const navigate = useNavigate();

  // 获取订单详情
  const {
    data: orderInfo,
    isLoading,
    error
  } = useQuery({
    queryKey: ['order-detail', orderNo],
    queryFn: () => orderService.getOrderDetail(orderNo!),
    enabled: !!orderNo,
  });

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">正在加载支付结果...</p>
        </div>
      </div>
    );
  }

  if (error || !orderInfo) {
    toast.error('加载订单信息失败');
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <button
            onClick={() => navigate('/orders')}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
          >
            返回订单列表
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-2xl mx-auto px-4">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            支付结果
          </h1>
          <p className="text-gray-600">
            订单 {orderNo} 的支付{getPaymentStatusText(status || 'FAILED')}
          </p>
        </div>

        <ResultCard
          status={status || 'FAILED'}
          orderInfo={orderInfo}
          onPrimaryAction={() => {
            if (status === 'SUCCESS') {
              navigate(`/order/${orderNo}`);
            } else {
              // 重新支付逻辑
              navigate(`/order/${orderNo}`);
            }
          }}
          onSecondaryAction={() => {
            if (status === 'SUCCESS') {
              navigate('/goods');
            } else {
              // 联系客服逻辑
              window.open('/customer-service', '_blank');
            }
          }}
        />

        {/* 推荐商品区域 */}
        {status === 'SUCCESS' && (
          <div className="mt-8">
            <h2 className="text-xl font-semibold mb-4">为您推荐</h2>
            {/* 推荐商品列表 */}
          </div>
        )}
      </div>
    </div>
  );
};

export default PaymentResult;
```

**文件**: `frontend/packages/portal/src/pages/Payment/components/ResultCard.tsx`

```typescript
import React from 'react';
import { StatusIcon } from './StatusIcon';
import { formatCurrency } from '../utils/formatUtils';

interface ResultCardProps {
  status: string;
  orderInfo: any;
  onPrimaryAction: () => void;
  onSecondaryAction: () => void;
}

export const ResultCard: React.FC<ResultCardProps> = ({
  status,
  orderInfo,
  onPrimaryAction,
  onSecondaryAction,
}) => {
  const getStatusConfig = (status: string) => {
    const configs = {
      'SUCCESS': {
        icon: '🎉',
        title: '支付成功！',
        message: '您的订单已成功支付，请等待商家发货。',
        color: 'success',
        primaryText: '查看订单详情',
        secondaryText: '继续购物',
      },
      'FAILED': {
        icon: '😔',
        title: '支付失败',
        message: '支付过程中出现问题，请重试或选择其他支付方式。',
        color: 'danger',
        primaryText: '重新支付',
        secondaryText: '联系客服',
      },
      'TIMEOUT': {
        icon: '⏰',
        title: '支付超时',
        message: '支付时间已过，订单已自动取消。',
        color: 'warning',
        primaryText: '重新下单',
        secondaryText: '联系客服',
      },
    };

    return configs[status] || configs['FAILED'];
  };

  const config = getStatusConfig(status);

  return (
    <div className={`bg-white rounded-lg shadow-lg p-8 ${
      status === 'SUCCESS' ? 'border-green-200' :
      status === 'FAILED' ? 'border-red-200' :
      'border-yellow-200'
    } border-2`}>
      {/* 结果图标和标题 */}
      <div className="text-center mb-6">
        <div className="text-6xl mb-4">{config.icon}</div>
        <h2 className={`text-2xl font-bold mb-2 ${
          status === 'SUCCESS' ? 'text-green-600' :
          status === 'FAILED' ? 'text-red-600' :
          'text-yellow-600'
        }`}>
          {config.title}
        </h2>
        <p className="text-gray-600">{config.message}</p>
      </div>

      {/* 订单信息 */}
      <div className="bg-gray-50 rounded-lg p-6 mb-6">
        <h3 className="font-semibold mb-4">订单信息</h3>
        <div className="space-y-3">
          <div className="flex justify-between">
            <span className="text-gray-600">订单号：</span>
            <span className="font-mono">{orderInfo.orderNo}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">订单金额：</span>
            <span className="font-bold text-lg">
              {formatCurrency(orderInfo.actualAmount)}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">商品名称：</span>
            <span className="text-right max-w-xs truncate">
              {orderInfo.goodsTitle}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">下单时间：</span>
            <span>{new Date(orderInfo.createTime).toLocaleString()}</span>
          </div>
        </div>
      </div>

      {/* 操作按钮 */}
      <div className="flex flex-col sm:flex-row gap-4">
        <button
          onClick={onPrimaryAction}
          className={`flex-1 py-3 px-6 rounded-lg font-semibold transition-colors ${
            status === 'SUCCESS'
              ? 'bg-green-600 hover:bg-green-700 text-white'
              : 'bg-blue-600 hover:bg-blue-700 text-white'
          }`}
        >
          {config.primaryText}
        </button>

        <button
          onClick={onSecondaryAction}
          className="flex-1 py-3 px-6 border-2 border-gray-300 rounded-lg font-semibold hover:bg-gray-50 transition-colors"
        >
          {config.secondaryText}
        </button>
      </div>
    </div>
  );
};
```

### 🔵 第8-9步：测试和集成（1小时）

---

## 🚀 Day 3: PaymentMethods页面和集成测试（6小时）

### 🔴 第1步：编写PaymentMethods测试（1小时）

### 🟢 第2-7步：实现PaymentMethods组件（4小时）

**文件**: `frontend/packages/portal/src/pages/Payment/PaymentMethods.tsx`

```typescript
import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { PaymentMethodCard } from './components/PaymentMethodCard';

interface PaymentMethod {
  id: string;
  name: string;
  icon: string;
  description: string;
  isDefault: boolean;
  usageCount: number;
  isEnabled: boolean;
}

const PaymentMethods: React.FC = () => {
  const [methods, setMethods] = useState<PaymentMethod[]>([]);
  const [updating, setUpdating] = useState(false);

  // 默认支付方式列表
  useEffect(() => {
    const defaultMethods: PaymentMethod[] = [
      {
        id: 'WECHAT',
        name: '微信支付',
        icon: '💚',
        description: '使用微信扫码支付，安全快捷',
        isDefault: localStorage.getItem('defaultPaymentMethod') === 'WECHAT',
        usageCount: parseInt(localStorage.getItem('wechatUsageCount') || '0'),
        isEnabled: true,
      },
      {
        id: 'ALIPAY',
        name: '支付宝',
        icon: '💙',
        description: '使用支付宝扫码支付，支持余额宝',
        isDefault: localStorage.getItem('defaultPaymentMethod') === 'ALIPAY',
        usageCount: parseInt(localStorage.getItem('alipayUsageCount') || '0'),
        isEnabled: true,
      },
    ];

    setMethods(defaultMethods);
  }, []);

  // 设置默认支付方式
  const handleSetDefault = async (methodId: string) => {
    setUpdating(true);
    try {
      // 更新本地状态
      setMethods(prev => prev.map(method => ({
        ...method,
        isDefault: method.id === methodId
      })));

      // 保存到本地存储
      localStorage.setItem('defaultPaymentMethod', methodId);

      toast.success('默认支付方式已更新');
    } catch (error) {
      toast.error('更新失败，请重试');
    } finally {
      setUpdating(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            支付方式管理
          </h1>
          <p className="text-gray-600">
            选择您偏好的支付方式，提升支付体验
          </p>
        </div>

        {/* 支付方式列表 */}
        <div className="grid gap-4 md:grid-cols-2 mb-8">
          {methods.map(method => (
            <PaymentMethodCard
              key={method.id}
              method={method}
              onSetDefault={handleSetDefault}
              disabled={updating}
            />
          ))}
        </div>

        {/* 安全提示 */}
        <div className="bg-blue-50 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-blue-900 mb-3">
            💡 支付安全提示
          </h3>
          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <h4 className="font-semibold text-blue-800 mb-2">安全建议</h4>
              <ul className="space-y-1 text-blue-700 text-sm">
                <li>• 请确保在安全的网络环境下进行支付</li>
                <li>• 不要向他人透露您的支付密码</li>
                <li>• 定期检查支付账单和交易记录</li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold text-blue-800 mb-2">遇到问题</h4>
              <ul className="space-y-1 text-blue-700 text-sm">
                <li>• 如遇支付问题，请及时联系客服</li>
                <li>• 支付成功但订单状态未更新，请刷新页面</li>
                <li>• 支付失败款项会在1-3个工作日内退还</li>
              </ul>
            </div>
          </div>
        </div>

        {/* 联系客服 */}
        <div className="text-center mt-8">
          <button
            onClick={() => window.open('/customer-service', '_blank')}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
          >
            💬 联系客服
          </button>
        </div>
      </div>
    </div>
  );
};

export default PaymentMethods;
```

### 🔵 第8-9步：集成测试和优化（1小时）

---

## 🧪 测试策略

### 单元测试
- **覆盖率目标**: ≥85%
- **测试框架**: Jest + React Testing Library
- **Mock策略**: Mock外部API和服务

### 集成测试
- **端到端测试**: Playwright
- **API集成测试**: 测试真实API调用
- **WebSocket测试**: 测试实时状态更新

### 性能测试
- **页面加载**: <2秒
- **状态更新延迟**: <3秒
- **内存使用**: 监控内存泄漏

---

## 📊 验收标准

### 功能验收
- [ ] 三个页面功能完整实现
- [ ] 支付状态实时更新正常
- [ ] 路由跳转正常工作
- [ ] 响应式设计适配良好

### 质量验收
- [ ] 测试覆盖率 ≥85%
- [ ] 无TypeScript编译错误
- [ ] 无ESLint警告
- [ ] 代码审查通过

### 性能验收
- [ ] 页面加载速度达标
- [ ] 状态更新及时
- [ ] 无明显性能问题

---

## 🎯 风险控制

### 技术风险
- **API变更**: 使用现有成熟API，风险较低
- **WebSocket连接**: 已有稳定实现，风险较低
- **浏览器兼容性**: 使用成熟技术栈，风险较低

### 进度风险
- **时间控制**: 3天工期较紧张，需严格控制
- **测试时间**: 预留充足测试时间
- **代码审查**: 确保代码质量，避免返工

### 质量风险
- **用户体验**: 严格遵循设计规范
- **性能监控**: 实时监控性能指标
- **错误处理**: 完善的错误处理机制

---

**任务文档版本**: v1.0<br>
**最后更新**: 2025-11-07<br>
**预计完成**: 2025-11-10

---

> 🚀 **BaSui的开发动员**:
> 这是最后一个P0级功能啦！搞完这个我们就能进入用户体验优化阶段！想想都激动！让我们用TDD十步流程完美收官，代码质量要杠杠的！加油加油！💪✨