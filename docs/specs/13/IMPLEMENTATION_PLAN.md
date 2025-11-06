# Spec 13 & 14 实施计划

> **功能**: 通知偏好设置 + 黑名单功能  
> **作者**: BaSui 😎  
> **日期**: 2025-11-06  
> **总工时**: 3-5天

---

## 📊 总体规划

| Spec | 功能 | 优先级 | 预计工时 | 状态 |
|------|------|--------|---------|------|
| **Spec 13** | 通知偏好设置 | P1 | 2-3天 | ⏳ 待开发 |
| **Spec 14** | 黑名单功能 | P2 | 1-2天 | ⏳ 待开发 |
| **总计** | - | - | **3-5天** | - |

---

## 🎯 实施顺序

### Phase 1: Spec 13 通知偏好设置（2-3天）

**Day 1（前端服务层）**：
1. ✅ 创建 `notificationPreference.ts` 服务
2. ✅ 实现 API 对接方法
3. ✅ 添加 TypeScript 类型定义
4. ✅ 单元测试（可选）

**Day 2（页面开发）**：
1. ✅ 创建通知设置主页 `/settings/notifications`
2. ✅ 实现通知渠道开关组件
3. ✅ 实现免打扰时段设置组件
4. ✅ 集成保存功能

**Day 3（完善）**：
1. ✅ 创建通知类型管理页面
2. ✅ 实现退订/重新订阅功能
3. ✅ 添加 Toast 提示
4. ✅ 响应式适配
5. ✅ 测试验证

---

### Phase 2: Spec 14 黑名单功能（1-2天）

**Day 1（前端服务 + 列表页）**：
1. ✅ 创建 `blacklist.ts` 服务
2. ✅ 实现 API 对接方法
3. ✅ 创建黑名单列表页 `/settings/blacklist`
4. ✅ 实现搜索和分页功能

**Day 2（交互完善）**：
1. ✅ 在用户主页添加"拉黑"按钮
2. ✅ 实现拉黑/解除拉黑确认对话框
3. ✅ 集成到消息页面
4. ✅ 响应式适配
5. ✅ 测试验证

---

## 📂 文件结构

### 前端服务层

```
frontend/packages/shared/src/services/
├── notificationPreference.ts    # 通知偏好服务 (新建)
├── blacklist.ts                 # 黑名单服务 (新建)
└── index.ts                     # 导出服务
```

### 前端页面层（Portal）

```
frontend/packages/portal/src/pages/
├── Settings/
│   ├── NotificationSettings.tsx         # 通知设置主页 (新建)
│   ├── NotificationSettings.css         # 样式文件
│   ├── NotificationTypes.tsx            # 通知类型管理 (新建)
│   ├── BlacklistSettings.tsx            # 黑名单管理 (新建)
│   └── BlacklistSettings.css            # 样式文件
```

### 组件层

```
frontend/packages/shared/src/components/
├── BlacklistButton/
│   ├── index.tsx                        # 拉黑按钮组件 (新建)
│   └── BlacklistButton.css
└── ConfirmDialog/
    └── index.tsx                        # 确认对话框组件 (可复用)
```

---

## 🔧 技术实施细节

### Spec 13: 通知偏好设置

#### 1. 创建服务 `notificationPreference.ts`

```typescript
/**
 * 通知偏好 API 服务
 * @author BaSui 😎
 */
import { http } from '../utils/http';

export interface NotificationPreference {
  userId: number;
  channels: {
    email: boolean;
    sms: boolean;
    inApp: boolean;
  };
  quietHours?: {
    enabled: boolean;
    startTime: string;
    endTime: string;
  };
  unsubscribedTypes: string[];
  updatedAt: string;
}

export class NotificationPreferenceService {
  private BASE_PATH = '/api/notifications/preferences';

  /**
   * 开关通知渠道
   */
  async toggleChannel(channel: 'EMAIL' | 'SMS' | 'IN_APP', enabled: boolean): Promise<void> {
    await http.post(`${this.BASE_PATH}/channel/${channel}/enabled/${enabled}`);
  }

  /**
   * 设置免打扰时段
   */
  async setQuietHours(channel: string, startTime: string, endTime: string): Promise<void> {
    await http.post(`${this.BASE_PATH}/channel/${channel}/quiet-hours`, {
      startTime,
      endTime,
    });
  }

  /**
   * 退订通知
   */
  async unsubscribe(channel: string, templateCode: string): Promise<void> {
    await http.post(`${this.BASE_PATH}/unsubscribe/${channel}/${templateCode}`);
  }

  /**
   * 重新订阅
   */
  async resubscribe(channel: string, templateCode: string): Promise<void> {
    await http.delete(`${this.BASE_PATH}/unsubscribe/${channel}/${templateCode}`);
  }

  /**
   * 查询偏好状态
   */
  async getStatus(): Promise<NotificationPreference> {
    const response = await http.get(`${this.BASE_PATH}/status`);
    return response.data.data;
  }
}

export const notificationPreferenceService = new NotificationPreferenceService();
```

---

#### 2. 创建页面组件

```typescript
/**
 * 通知设置页面
 * @author BaSui 😎
 */
import React, { useState, useEffect } from 'react';
import { Switch, TimePicker, Button, message } from '@campus/shared/components';
import { notificationPreferenceService } from '@campus/shared/services';

export const NotificationSettings: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [preferences, setPreferences] = useState<any>(null);

  // 加载偏好设置
  useEffect(() => {
    loadPreferences();
  }, []);

  const loadPreferences = async () => {
    try {
      const data = await notificationPreferenceService.getStatus();
      setPreferences(data);
    } catch (error) {
      message.error('加载失败');
    }
  };

  // 开关渠道
  const handleToggleChannel = async (channel: string, enabled: boolean) => {
    try {
      await notificationPreferenceService.toggleChannel(channel as any, enabled);
      message.success('设置成功');
      loadPreferences();
    } catch (error) {
      message.error('设置失败');
    }
  };

  // 保存设置
  const handleSave = async () => {
    setLoading(true);
    try {
      // 保存免打扰时段等
      message.success('保存成功！');
    } catch (error) {
      message.error('保存失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="notification-settings">
      <h1>🔔 通知设置</h1>
      
      <section>
        <h2>📧 通知渠道</h2>
        <div className="channel-item">
          <span>邮件通知</span>
          <Switch 
            checked={preferences?.channels?.email}
            onChange={(checked) => handleToggleChannel('EMAIL', checked)}
          />
        </div>
        {/* ... 其他渠道 */}
      </section>

      <section>
        <h2>🌙 免打扰时段</h2>
        {/* ... 免打扰设置 */}
      </section>

      <Button type="primary" onClick={handleSave} loading={loading}>
        保存设置
      </Button>
    </div>
  );
};
```

---

### Spec 14: 黑名单功能

#### 1. 创建服务 `blacklist.ts`

```typescript
/**
 * 黑名单 API 服务
 * @author BaSui 😎
 */
import { http } from '../utils/http';

export interface BlacklistUser {
  id: number;
  blockedUserId: number;
  blockedUserName: string;
  blockedUserAvatar?: string;
  createdAt: string;
}

export class BlacklistService {
  private BASE_PATH = '/api/blacklist';

  /**
   * 拉黑用户
   */
  async block(userId: number): Promise<void> {
    await http.post(`${this.BASE_PATH}/block/${userId}`);
  }

  /**
   * 解除拉黑
   */
  async unblock(userId: number): Promise<void> {
    await http.delete(`${this.BASE_PATH}/unblock/${userId}`);
  }

  /**
   * 查看黑名单
   */
  async list(page: number = 0, size: number = 20): Promise<any> {
    const response = await http.get(`${this.BASE_PATH}/list`, {
      params: { page, size },
    });
    return response.data.data;
  }

  /**
   * 检查是否已拉黑
   */
  async check(userId: number): Promise<boolean> {
    const response = await http.get(`${this.BASE_PATH}/check/${userId}`);
    return response.data.data?.isBlocked || false;
  }
}

export const blacklistService = new BlacklistService();
```

---

#### 2. 创建拉黑按钮组件

```typescript
/**
 * 拉黑按钮组件
 * @author BaSui 😎
 */
import React, { useState, useEffect } from 'react';
import { Button, Modal, message } from '@campus/shared/components';
import { blacklistService } from '@campus/shared/services';

interface BlacklistButtonProps {
  userId: number;
  userName: string;
}

export const BlacklistButton: React.FC<BlacklistButtonProps> = ({ userId, userName }) => {
  const [isBlocked, setIsBlocked] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    checkStatus();
  }, [userId]);

  const checkStatus = async () => {
    try {
      const blocked = await blacklistService.check(userId);
      setIsBlocked(blocked);
    } catch (error) {
      console.error('检查拉黑状态失败:', error);
    }
  };

  const handleBlock = async () => {
    Modal.confirm({
      title: '⚠️ 确认拉黑',
      content: `确定要拉黑用户 "${userName}" 吗？拉黑后对方无法给你发消息。`,
      onOk: async () => {
        setLoading(true);
        try {
          await blacklistService.block(userId);
          message.success('已拉黑');
          setIsBlocked(true);
        } catch (error) {
          message.error('拉黑失败');
        } finally {
          setLoading(false);
        }
      },
    });
  };

  const handleUnblock = async () => {
    Modal.confirm({
      title: 'ℹ️ 解除拉黑',
      content: `确定要解除对 "${userName}" 的拉黑吗？`,
      onOk: async () => {
        setLoading(true);
        try {
          await blacklistService.unblock(userId);
          message.success('已解除拉黑');
          setIsBlocked(false);
        } catch (error) {
          message.error('解除失败');
        } finally {
          setLoading(false);
        }
      },
    });
  };

  return (
    <Button
      danger={!isBlocked}
      disabled={isBlocked}
      onClick={isBlocked ? handleUnblock : handleBlock}
      loading={loading}
    >
      {isBlocked ? '已拉黑' : '拉黑'}
    </Button>
  );
};
```

---

## ✅ 测试清单

### Spec 13: 通知偏好设置

- [ ] 可以开关每个通知渠道
- [ ] 可以设置免打扰时段
- [ ] 可以退订/重新订阅通知类型
- [ ] 设置保存后立即生效
- [ ] 页面刷新后设置保持
- [ ] 响应式设计正常

### Spec 14: 黑名单功能

- [ ] 可以拉黑用户
- [ ] 可以查看黑名单列表
- [ ] 可以解除拉黑
- [ ] 拉黑后看不到对方消息
- [ ] 搜索功能正常
- [ ] 分页功能正常
- [ ] 响应式设计正常

---

## 🎯 验收标准

### 功能验收
- ✅ 所有核心功能正常工作
- ✅ 所有交互符合设计
- ✅ 所有API调用成功

### 性能验收
- ✅ 页面加载 < 1秒
- ✅ API响应 < 2秒

### 体验验收
- ✅ 界面美观清晰
- ✅ 交互流畅自然
- ✅ 错误提示友好

---

## 📅 时间规划

| 日期 | 任务 | 预计完成 |
|------|------|---------|
| Day 1 | Spec 13 服务层 + 主页 | ✅ |
| Day 2 | Spec 13 类型管理页 | ✅ |
| Day 3 | Spec 14 服务层 + 列表页 | ✅ |
| Day 4 | Spec 14 按钮组件 + 集成 | ✅ |
| Day 5 | 测试 + 完善 | ✅ |

---

**开发者**: BaSui 😎  
**开始日期**: 2025-11-06  
**预计完成**: 2025-11-11
