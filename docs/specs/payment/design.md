# 🏗️ 支付系统用户端页面技术设计文档

> **作者**: BaSui 😎 | **创建**: 2025-11-07 | **架构版本**: v2.0
> **技术栈**: React 18 + TypeScript + TanStack Query

---

## 🎯 设计概述

### 架构目标
基于现有的后端支付API和前端基础设施，设计一套**完整、高效、用户友好**的支付状态查询和管理页面，解决当前支付流程中的用户体验断点。

### 设计原则
- **复用优先**：最大化复用现有组件和服务
- **渐进增强**：在现有基础上逐步完善功能
- **用户体验**：以用户支付体验为核心设计目标
- **性能优先**：确保实时状态更新的高性能表现

---

## 📁 系统架构

### 整体架构图
```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (React 18)                      │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐   │
│  │   Router    │  │   Pages     │  │     Components      │   │
│  │             │  │             │  │                     │   │
│  │ /payment →  │  │PaymentStatus│  │ • PaymentProgress   │   │
│  │ PaymentStatus│  │PaymentResult│  │ • StatusIcon        │   │
│  │ /result →   │  │PaymentMethods│  │ • CountdownTimer    │   │
│  │ PaymentResult│  │             │  │ • PaymentMethodsList│   │
│  │ /methods →  │  │             │  │ • ResultCard        │   │
│  │ PaymentMethods│             │  │                     │   │
│  └─────────────┘  └─────────────┘  └─────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐   │
│  │   Services  │  │    Hooks    │  │      Utils          │   │
│  │             │  │             │  │                     │   │
│  │orderService │  │usePayment   │  │ • formatCurrency    │   │
│  │websocketSvc │  │useCountdown │  │ • getStatusIcon     │   │
│  │             │  │useWebSocket │  │ • getStatusText     │   │
│  └─────────────┘  └─────────────┘  └─────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐   │
│  │    State    │  │    Types    │  │      Tests          │   │
│  │  Management │  │             │  │                     │   │
│  │             │  │PaymentTypes │  │ • Unit Tests        │   │
│  │TanStack Query│  │OrderTypes   │  │ • Integration Tests │   │
│  │React Context│  │APITypes     │  │ • E2E Tests         │   │
│  └─────────────┘  └─────────────┘  └─────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                     Backend APIs                             │
├─────────────────────────────────────────────────────────────┤
│  • GET /api/payment/status/{orderNo}                         │
│  • GET /api/orders/{orderNo}                                 │
│  • WebSocket订单状态推送                                      │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈选择
- **前端框架**: React 18 (已使用)
- **类型系统**: TypeScript 5 (已使用)
- **状态管理**: TanStack Query (已使用)
- **路由管理**: React Router v6 (已使用)
- **UI组件**: 复用现有shared包组件
- **实时通信**: WebSocket (已实现)
- **构建工具**: Vite 5 (已使用)

---

## 📄 页面设计详情

### 1. PaymentStatus 页面（支付状态查询）

#### 1.1 页面结构
```typescript
interface PaymentStatusProps {
  orderNo: string;        // URL参数：订单号
}

interface PaymentStatusState {
  orderInfo: Order | null;      // 订单信息
  paymentStatus: string;        // 支付状态
  isLoading: boolean;           // 加载状态
  error: string | null;         // 错误信息
  remainingTime: number;        // 支付剩余时间
  progressPercentage: number;   // 进度百分比
}
```

#### 1.2 核心功能实现
```typescript
// 1. 状态查询Hook
const usePaymentStatus = (orderNo: string) => {
  return useQuery({
    queryKey: ['payment-status', orderNo],
    queryFn: () => orderService.queryPaymentStatus(orderNo),
    refetchInterval: (data) => {
      // 只有在支付进行中才轮询
      return data?.status === 'PENDING' ? 3000 : false;
    },
    staleTime: 1000,
  });
};

// 2. WebSocket实时更新
const usePaymentWebSocket = (orderNo: string) => {
  useEffect(() => {
    const handleOrderUpdate = (data: any) => {
      if (data.orderNo === orderNo) {
        // 更新支付状态
        updatePaymentStatus(data.status);
      }
    };

    websocketService.onOrderUpdate(handleOrderUpdate);
    return () => websocketService.offOrderUpdate(handleOrderUpdate);
  }, [orderNo]);
};

// 3. 倒计时Hook
const usePaymentCountdown = (expireTime: string) => {
  const [remainingTime, setRemainingTime] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      const now = new Date().getTime();
      const expire = new Date(expireTime).getTime();
      const remaining = Math.max(0, expire - now);

      setRemainingTime(remaining);

      if (remaining === 0) {
        clearInterval(timer);
        // 支付超时处理
        handlePaymentTimeout();
      }
    }, 1000);

    return () => clearInterval(timer);
  }, [expireTime]);

  return remainingTime;
};
```

#### 1.3 UI组件设计
```typescript
// 支付进度组件
const PaymentProgress: React.FC<{
  status: string;
  percentage: number;
}> = ({ status, percentage }) => {
  return (
    <div className="payment-progress">
      <div className="progress-header">
        <StatusIcon status={status} />
        <span className="status-text">{getStatusText(status)}</span>
      </div>
      <div className="progress-bar">
        <div
          className="progress-fill"
          style={{ width: `${percentage}%` }}
        />
      </div>
      <CountdownTimer remainingTime={remainingTime} />
    </div>
  );
};

// 状态图标组件
const StatusIcon: React.FC<{ status: string }> = ({ status }) => {
  const iconMap = {
    'PENDING': '⏳',
    'SUCCESS': '✅',
    'FAILED': '❌',
    'TIMEOUT': '⏰',
  };

  return <span className="status-icon">{iconMap[status] || '❓'}</span>;
};
```

### 2. PaymentResult 页面（支付结果展示）

#### 2.1 页面结构
```typescript
interface PaymentResultProps {
  orderNo: string;        // URL参数：订单号
  status: 'SUCCESS' | 'FAILED' | 'TIMEOUT';
}

interface ResultActions {
  primary: {
    text: string;
    action: () => void;
    variant: 'primary' | 'danger' | 'warning';
  };
  secondary?: {
    text: string;
    action: () => void;
  };
}
```

#### 2.2 结果页面逻辑
```typescript
const PaymentResult: React.FC<PaymentResultProps> = ({ orderNo, status }) => {
  const { data: orderInfo } = useOrderDetail(orderNo);

  const getResultActions = (status: string): ResultActions => {
    switch (status) {
      case 'SUCCESS':
        return {
          primary: {
            text: '查看订单详情',
            action: () => navigate(`/order/${orderNo}`),
            variant: 'primary'
          },
          secondary: {
            text: '继续购物',
            action: () => navigate('/goods')
          }
        };

      case 'FAILED':
        return {
          primary: {
            text: '重新支付',
            action: () => handleRetryPayment(),
            variant: 'primary'
          },
          secondary: {
            text: '取消订单',
            action: () => handleCancelOrder()
          }
        };

      case 'TIMEOUT':
        return {
          primary: {
            text: '重新下单',
            action: () => handleReorder(),
            variant: 'warning'
          },
          secondary: {
            text: '联系客服',
            action: () => handleContactSupport()
          }
        };

      default:
        return {
          primary: {
            text: '查看订单',
            action: () => navigate(`/order/${orderNo}`),
            variant: 'primary'
          }
        };
    }
  };

  const actions = getResultActions(status);

  return (
    <div className="payment-result">
      <ResultCard status={status} orderInfo={orderInfo} />
      <div className="result-actions">
        <button
          className={`btn btn-${actions.primary.variant}`}
          onClick={actions.primary.action}
        >
          {actions.primary.text}
        </button>
        {actions.secondary && (
          <button
            className="btn btn-secondary"
            onClick={actions.secondary.action}
          >
            {actions.secondary.text}
          </button>
        )}
      </div>
    </div>
  );
};
```

#### 2.3 结果卡片组件
```typescript
const ResultCard: React.FC<{
  status: string;
  orderInfo: Order | null;
}> = ({ status, orderInfo }) => {
  const getStatusConfig = (status: string) => {
    const configs = {
      'SUCCESS': {
        icon: '🎉',
        title: '支付成功！',
        message: '您的订单已成功支付，请等待商家发货。',
        color: 'success'
      },
      'FAILED': {
        icon: '😔',
        title: '支付失败',
        message: '支付过程中出现问题，请重试或选择其他支付方式。',
        color: 'danger'
      },
      'TIMEOUT': {
        icon: '⏰',
        title: '支付超时',
        message: '支付时间已过，订单已自动取消。',
        color: 'warning'
      }
    };

    return configs[status] || configs['FAILED'];
  };

  const config = getStatusConfig(status);

  return (
    <div className={`result-card result-${config.color}`}>
      <div className="result-icon">{config.icon}</div>
      <h2 className="result-title">{config.title}</h2>
      <p className="result-message">{config.message}</p>

      {orderInfo && (
        <div className="order-summary">
          <div className="summary-item">
            <span>订单号：</span>
            <span>{orderInfo.orderNo}</span>
          </div>
          <div className="summary-item">
            <span>订单金额：</span>
            <span className="amount">¥{orderInfo.actualAmount}</span>
          </div>
          <div className="summary-item">
            <span>商品名称：</span>
            <span>{orderInfo.goodsTitle}</span>
          </div>
        </div>
      )}
    </div>
  );
};
```

### 3. PaymentMethods 页面（支付方式管理）

#### 3.1 页面结构
```typescript
interface PaymentMethod {
  id: string;
  name: string;
  icon: string;
  description: string;
  isDefault: boolean;
  usageCount: number;
  isEnabled: boolean;
}

interface PaymentMethodsState {
  methods: PaymentMethod[];
  loading: boolean;
  updating: boolean;
}
```

#### 3.2 支付方式管理逻辑
```typescript
const PaymentMethods: React.FC = () => {
  const [methods, setMethods] = useState<PaymentMethod[]>([]);
  const [updating, setUpdating] = useState(false);

  // 默认支付方式列表
  const defaultMethods: PaymentMethod[] = [
    {
      id: 'WECHAT',
      name: '微信支付',
      icon: '💚',
      description: '使用微信扫码支付',
      isDefault: true,
      usageCount: 0,
      isEnabled: true
    },
    {
      id: 'ALIPAY',
      name: '支付宝',
      icon: '💙',
      description: '使用支付宝扫码支付',
      isDefault: false,
      usageCount: 0,
      isEnabled: true
    }
  ];

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
    <div className="payment-methods">
      <h2>支付方式管理</h2>
      <div className="methods-list">
        {methods.map(method => (
          <PaymentMethodCard
            key={method.id}
            method={method}
            onSetDefault={handleSetDefault}
            disabled={updating}
          />
        ))}
      </div>

      <div className="security-tips">
        <h3>💡 支付安全提示</h3>
        <ul>
          <li>请确保在安全的网络环境下进行支付</li>
          <li>不要向他人透露您的支付密码</li>
          <li>如遇支付问题，请及时联系客服</li>
        </ul>
      </div>
    </div>
  );
};
```

---

## 🔧 核心Hook设计

### 1. usePayment Hook
```typescript
interface UsePaymentOptions {
  orderNo: string;
  autoPoll?: boolean;
  websocketEnabled?: boolean;
}

const usePayment = (options: UsePaymentOptions) => {
  const [status, setStatus] = useState<string>('PENDING');
  const [error, setError] = useState<string | null>(null);

  // 支付状态查询
  const {
    data: paymentStatus,
    isLoading,
    refetch
  } = useQuery({
    queryKey: ['payment-status', options.orderNo],
    queryFn: () => orderService.queryPaymentStatus(options.orderNo),
    refetchInterval: options.autoPoll ? 3000 : false,
    enabled: !!options.orderNo,
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
    return () => websocketService.offOrderUpdate(handleOrderUpdate);
  }, [options.orderNo, options.websocketEnabled, refetch]);

  // 手动刷新状态
  const refreshStatus = useCallback(() => {
    refetch();
  }, [refetch]);

  return {
    status: paymentStatus || status,
    isLoading,
    error,
    refreshStatus,
  };
};
```

### 2. useCountdown Hook
```typescript
const useCountdown = (targetTime: string) => {
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
      }
    };

    calculateRemaining();
    const timer = setInterval(calculateRemaining, 1000);

    return () => clearInterval(timer);
  }, [targetTime, isExpired]);

  const formatTime = (milliseconds: number) => {
    const totalSeconds = Math.floor(milliseconds / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };

  return {
    remainingTime,
    isExpired,
    formattedTime: formatTime(remainingTime),
  };
};
```

---

## 🛠️ 工具函数设计

### 1. 状态处理工具
```typescript
// 获取支付状态文本
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

// 获取支付状态图标
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

// 计算支付进度百分比
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

  const total = expire - create;
  const elapsed = now - create;

  return Math.min(100, Math.max(0, (elapsed / total) * 100));
};
```

### 2. 格式化工具
```typescript
// 格式化货币
export const formatCurrency = (amount: number | string): string => {
  const num = typeof amount === 'string' ? parseFloat(amount) : amount;
  return `¥${num.toFixed(2)}`;
};

// 格式化时间
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
```

---

## 📱 响应式设计

### 断点设计
```css
/* 移动端 */
@media (max-width: 768px) {
  .payment-status {
    padding: 1rem;
  }

  .payment-progress {
    flex-direction: column;
    gap: 1rem;
  }

  .result-card {
    margin: 1rem;
    padding: 1.5rem;
  }
}

/* 平板端 */
@media (min-width: 769px) and (max-width: 1024px) {
  .payment-status {
    max-width: 600px;
    margin: 0 auto;
  }
}

/* 桌面端 */
@media (min-width: 1025px) {
  .payment-status {
    max-width: 800px;
    margin: 0 auto;
  }
}
```

### 组件适配策略
- **弹性布局**：使用Flexbox实现自适应布局
- **流式布局**：宽度使用百分比，最大/最小值限制
- **字体缩放**：移动端适当减小字体大小
- **触摸优化**：移动端增大按钮点击区域

---

## 🚀 性能优化策略

### 1. 数据获取优化
```typescript
// 智能轮询策略
const useSmartPolling = (orderNo: string) => {
  return useQuery({
    queryKey: ['payment-status', orderNo],
    queryFn: () => orderService.queryPaymentStatus(orderNo),
    refetchInterval: (data, query) => {
      // 根据状态调整轮询频率
      const status = data?.status;
      const queryCount = query.state.dataUpdateCount;

      if (status === 'SUCCESS' || status === 'FAILED') {
        return false; // 停止轮询
      }

      // 随着时间推移降低轮询频率
      if (queryCount < 10) return 3000;  // 前10次：3秒
      if (queryCount < 20) return 5000;  // 10-20次：5秒
      return 10000; // 20次后：10秒
    },
    staleTime: 1000,
  });
};
```

### 2. 组件优化
```typescript
// 使用React.memo防止不必要的重渲染
const PaymentProgress = React.memo<{
  status: string;
  percentage: number;
}>(({ status, percentage }) => {
  return (
    <div className="payment-progress">
      {/* 组件内容 */}
    </div>
  );
});

// 使用useMemo缓存计算结果
const PaymentStatus: React.FC<{ orderNo: string }> = ({ orderNo }) => {
  const { data: orderInfo } = useOrderDetail(orderNo);

  const progressPercentage = useMemo(() => {
    if (!orderInfo) return 0;
    return calculatePaymentProgress(
      orderInfo.status,
      orderInfo.createTime,
      orderInfo.expireTime
    );
  }, [orderInfo]);

  return (
    <PaymentProgress
      status={orderInfo?.status}
      percentage={progressPercentage}
    />
  );
};
```

---

## 🔒 安全设计

### 1. 参数校验
```typescript
// 订单号格式校验
const validateOrderNo = (orderNo: string): boolean => {
  const pattern = /^O\d{8}\d{4}$/; // O + 年月日 + 序号
  return pattern.test(orderNo);
};

// 支付状态校验
const validatePaymentStatus = (status: string): boolean => {
  const validStatuses = ['PENDING', 'SUCCESS', 'FAILED', 'TIMEOUT', 'CANCELLED'];
  return validStatuses.includes(status);
};
```

### 2. 防刷机制
```typescript
// 防止频繁查询
const useRateLimitedQuery = (orderNo: string) => {
  const [lastQueryTime, setLastQueryTime] = useState(0);
  const MIN_QUERY_INTERVAL = 1000; // 1秒最小间隔

  const canQuery = () => {
    const now = Date.now();
    return now - lastQueryTime >= MIN_QUERY_INTERVAL;
  };

  const queryWithRateLimit = async () => {
    if (!canQuery()) {
      throw new Error('查询过于频繁，请稍后再试');
    }

    setLastQueryTime(Date.now());
    return orderService.queryPaymentStatus(orderNo);
  };

  return useQuery({
    queryKey: ['payment-status', orderNo],
    queryFn: queryWithRateLimit,
    refetchInterval: 3000,
  });
};
```

---

## 📊 监控和日志

### 1. 用户行为监控
```typescript
// 支付页面访问统计
const usePaymentAnalytics = () => {
  useEffect(() => {
    // 页面访问统计
    analytics.track('payment_page_view', {
      url: window.location.href,
      timestamp: Date.now(),
    });

    // 页面停留时间
    const startTime = Date.now();

    return () => {
      const duration = Date.now() - startTime;
      analytics.track('payment_page_duration', {
        duration,
        url: window.location.href,
      });
    };
  }, []);
};

// 支付操作统计
const trackPaymentAction = (action: string, data: any) => {
  analytics.track('payment_action', {
    action,
    data,
    timestamp: Date.now(),
  });
};
```

### 2. 错误监控
```typescript
// 错误边界组件
class PaymentErrorBoundary extends React.Component {
  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Payment page error:', error, errorInfo);

    // 发送错误报告
    errorReporting.captureException(error, {
      extra: errorInfo,
      tags: {
        component: 'payment',
        page: window.location.pathname,
      },
    });
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="error-fallback">
          <h2>页面加载失败</h2>
          <p>请刷新页面重试，或联系客服</p>
          <button onClick={() => window.location.reload()}>
            刷新页面
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
```

---

## 🎯 部署策略

### 1. 路由配置
```typescript
// router配置
const paymentRoutes = [
  {
    path: '/payment',
    element: <PaymentStatus />,
    children: [
      {
        path: 'result',
        element: <PaymentResult />,
      },
      {
        path: 'methods',
        element: <PaymentMethods />,
      },
    ],
  },
];
```

### 2. 渐进式发布
```typescript
// 功能开关控制
const FEATURE_FLAGS = {
  PAYMENT_STATUS_PAGE: process.env.REACT_APP_ENABLE_PAYMENT_STATUS === 'true',
  PAYMENT_RESULT_PAGE: process.env.REACT_APP_ENABLE_PAYMENT_RESULT === 'true',
  PAYMENT_METHODS_PAGE: process.env.REACT_APP_ENABLE_PAYMENT_METHODS === 'true',
};

// 条件渲染
const PaymentStatus = () => {
  if (!FEATURE_FLAGS.PAYMENT_STATUS_PAGE) {
    return <div>功能暂未开放</div>;
  }

  return <PaymentStatusPage />;
};
```

---

## 📈 扩展性设计

### 1. 支付方式扩展
```typescript
// 支付方式配置化
interface PaymentMethodConfig {
  id: string;
  name: string;
  icon: string;
  description: string;
  component: React.ComponentType;
  enabled: boolean;
}

const paymentMethodConfigs: PaymentMethodConfig[] = [
  {
    id: 'WECHAT',
    name: '微信支付',
    icon: '💚',
    description: '使用微信扫码支付',
    component: WechatPaymentComponent,
    enabled: true,
  },
  // 未来可扩展更多支付方式
];
```

### 2. 国际化支持
```typescript
// 多语言支持
const usePaymentI18n = () => {
  const { t } = useTranslation();

  return {
    getStatusText: (status: string) => t(`payment.status.${status}`),
    getActionText: (action: string) => t(`payment.action.${action}`),
  };
};
```

---

**设计文档版本**: v1.0<br>
**最后更新**: 2025-11-07<br>
**下一步**: 编写任务分解文档（tasks.md）

---

> 💡 **BaSui的技术提醒**:
> 这个设计充分考虑了复用现有组件和API，确保开发效率和代码质量！性能优化和安全设计都考虑到了，堪称完美！🎯✨