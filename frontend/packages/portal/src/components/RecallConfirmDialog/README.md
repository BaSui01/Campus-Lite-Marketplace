# 消息撤回功能说明文档

> **作者**: BaSui 😎
> **更新**: 2025-11-07
> **功能**: 聊天消息撤回系统

## 📋 功能概述

消息撤回功能允许用户撤回已发送的消息，提供安全、友好的撤回体验。

### ✨ 核心特性

1. **时间限制**: 消息发送后5分钟内可撤回
2. **确认机制**: 撤回前需要用户确认，防止误操作
3. **实时倒计时**: 显示剩余可撤回时间
4. **视觉反馈**: 撤回后的消息有特殊显示样式
5. **双向同步**: 撤回操作通过WebSocket实时同步

## 🎯 使用方法

### 基本用法

```typescript
import RecallConfirmDialog from '@/components/RecallConfirmDialog';

<RecallConfirmDialog
  visible={showDialog}
  messagePreview="要撤回的消息内容"
  messageTime="10:30"
  onConfirm={handleRecall}
  onCancel={handleCancel}
  loading={recalling}
  remainingTime={120}
  timeLimit={5}
/>
```

### 参数说明

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| visible | boolean | ✅ | 对话框是否可见 |
| messagePreview | string | ✅ | 消息内容预览 |
| messageTime | string | ✅ | 消息发送时间 |
| onConfirm | () => void | ✅ | 确认撤回回调 |
| onCancel | () => void | ✅ | 取消撤回回调 |
| loading | boolean | ❌ | 是否正在撤回中 |
| remainingTime | number | ❌ | 剩余可撤回时间（秒） |
| timeLimit | number | ❌ | 撤回时间限制（分钟） |
| className | string | ❌ | 自定义样式类名 |

## 🔧 集成到聊天系统

### 1. 在MessageList组件中使用

```typescript
// MessageItem组件中
const [showRecallDialog, setShowRecallDialog] = useState(false);
const [recalling, setRecalling] = useState(false);

// 检查是否可以撤回
const canRecall = () => {
  const messageTime = new Date(message.timestamp).getTime();
  const now = Date.now();
  const timeDiff = (now - messageTime) / (1000 * 60);
  return isOwn && timeDiff <= 5;
};

// 处理撤回
const handleRecallMessage = async () => {
  setRecalling(true);
  try {
    await messageService.recallMessage(parseInt(message.id));
    setShowRecallDialog(false);
    onMessageRecall?.(message.id);
  } catch (error) {
    console.error('撤回失败:', error);
  } finally {
    setRecalling(false);
  }
};

// 撤回按钮
{canRecall() && (
  <button onClick={() => setShowRecallDialog(true)}>
    撤回
  </button>
)}

// 确认对话框
<RecallConfirmDialog
  visible={showRecallDialog}
  messagePreview={message.content}
  messageTime={formatTime(message.timestamp)}
  onConfirm={handleRecallMessage}
  onCancel={() => setShowRecallDialog(false)}
  loading={recalling}
  remainingTime={getRemainingRecallTime()}
/>
```

### 2. 在useDisputeChat Hook中使用

```typescript
const recallMessage = useCallback(async (messageId: string): Promise<boolean> => {
  try {
    // 调用API撤回消息
    const success = await messageService.recallMessage(parseInt(messageId));

    if (success) {
      // 发送WebSocket通知
      if (readyState === WebSocketReadyState.OPEN) {
        send({
          type: 'message_recall',
          payload: { messageId, senderId: currentUserId }
        });
      }

      // 更新本地状态
      setMessages(prev => prev.map(msg =>
        msg.id === messageId
          ? { ...msg, isRecalled: true, content: '[消息已撤回]' }
          : msg
      ));
    }

    return success;
  } catch (error) {
    onError?.('撤回消息失败');
    return false;
  }
}, [readyState, send, currentUserId, onError]);
```

## 🎨 UI/UX 设计

### 对话框设计原则

1. **清晰的视觉层次**: 使用橙色作为主色调，表示警示性操作
2. **信息完整**: 显示消息预览、时间、剩余时间等关键信息
3. **操作明确**: 确认和取消按钮有明确的视觉区分
4. **状态反馈**: 加载状态、禁用状态有明确的视觉提示

### 撤回消息显示

```typescript
// 撤回后的消息样式
{message.isRecalled && (
  <div className="bg-gray-100 text-gray-500 border border-gray-200 p-3 rounded-lg">
    <div className="flex items-center space-x-2">
      <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
        <path fillRule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a1 1 0 001 1h12a1 1 0 001-1V6a1 1 0 00-1-1h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clipRule="evenodd"/>
      </svg>
      <span className="italic">[消息已撤回]</span>
    </div>
  </div>
)}
```

## ⚡ 性能优化

1. **时间计算优化**: 使用时间戳差值计算，避免频繁的日期计算
2. **状态管理**: 使用React的useState和useCallback优化渲染
3. **内存管理**: 及时清理定时器和事件监听器

## 🔒 安全考虑

1. **权限验证**: 只有消息发送者才能撤回自己的消息
2. **时间限制**: 严格的5分钟撤回时间限制
3. **操作确认**: 双重确认机制防止误操作
4. **日志记录**: 记录撤回操作便于审计

## 🧪 测试

### 组件测试

```bash
# 运行组件测试
npm test RecallConfirmDialog.test.tsx
```

### 测试覆盖

- ✅ 对话框显示/隐藏
- ✅ 确认/取消操作
- ✅ 加载状态
- ✅ 时间限制处理
- ✅ 键盘事件
- ✅ 样式应用

## 🔄 API 集成

### 后端接口

```typescript
// 撤回消息接口
POST /api/messages/{messageId}/recall
```

### WebSocket 事件

```typescript
// 撤回消息事件
{
  type: 'message_recall',
  payload: {
    messageId: string,
    senderId: number,
    timestamp: string
  }
}
```

## 📱 移动端适配

- 响应式设计，适配不同屏幕尺寸
- 触摸友好的按钮尺寸
- 适配移动端的键盘事件

## 🎯 最佳实践

1. **用户体验**: 提供清晰的视觉反馈和操作指引
2. **错误处理**: 友好的错误提示和恢复机制
3. **性能**: 避免不必要的重渲染和网络请求
4. **可访问性**: 支持键盘导航和屏幕阅读器

---

## 📞 技术支持

如有问题，请联系开发团队或查看相关文档。