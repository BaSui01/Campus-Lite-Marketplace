# SmartCaptcha 智能验证码组件

## 🎯 功能说明

SmartCaptcha 是一个智能验证码选择器组件，可以随机选择不同类型的验证码，提升安全性和用户体验。

## ✨ 支持的验证码类型

| 类型 | 说明 | 状态 | 安全性 |
|-----|------|------|--------|
| `image` | 图形验证码（4位数字+字母） | ✅ 完成 | ⭐⭐⭐ |
| `rotate` | 旋转验证码（旋转图片到正确角度） | ✅ 完成 | ⭐⭐⭐⭐ |
| `click` | 点选验证码（依次点击指定文字） | ✅ 完成 | ⭐⭐⭐⭐⭐ |

## 📦 基本使用

### 1. 随机验证码（推荐）

```tsx
import { SmartCaptcha, CaptchaResult } from '@campus/shared/components/SmartCaptcha';

function LoginPage() {
  const handleSuccess = (result: CaptchaResult) => {
    console.log('验证成功:', result);
    
    // 根据类型处理不同的验证结果
    switch (result.type) {
      case 'image':
        console.log('图形验证:', result.captchaId, result.captchaCode);
        break;
      case 'rotate':
        console.log('旋转验证:', result.rotateId, result.rotateAngle);
        break;
      case 'click':
        console.log('点选验证:', result.clickId, result.clickPoints);
        break;
    }
  };

  return (
    <SmartCaptcha
      onSuccess={handleSuccess}
      onFail={() => console.log('验证失败')}
    />
  );
}
```

### 2. 指定验证码类型

```tsx
<SmartCaptcha
  forceType="rotate"  // 强制使用旋转验证码
  onSuccess={handleSuccess}
/>
```

### 3. 完整登录示例

```tsx
import React, { useState } from 'react';
import { SmartCaptcha, CaptchaResult } from '@campus/shared/components/SmartCaptcha';
import { authService } from '@campus/shared/services/auth';

function LoginPage() {
  const [captchaResult, setCaptchaResult] = useState<CaptchaResult | null>(null);
  const [resetKey, setResetKey] = useState(0);

  const handleCaptchaSuccess = (result: CaptchaResult) => {
    console.log('验证码验证成功:', result);
    setCaptchaResult(result);
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!captchaResult) {
      alert('请先完成验证码验证');
      return;
    }

    try {
      const loginData: any = {
        username: 'user@example.com',
        password: 'password123',
      };

      // 根据验证码类型添加对应字段
      switch (captchaResult.type) {
        case 'image':
          loginData.captchaId = captchaResult.captchaId;
          loginData.captchaCode = captchaResult.captchaCode;
          break;
        case 'rotate':
          loginData.rotateId = captchaResult.rotateId;
          loginData.rotateAngle = captchaResult.rotateAngle;
          break;
        case 'click':
          loginData.clickId = captchaResult.clickId;
          loginData.clickPoints = captchaResult.clickPoints;
          break;
      }

      await authService.login(loginData);
      console.log('登录成功！');
    } catch (error) {
      console.error('登录失败:', error);
      // 重置验证码
      setCaptchaResult(null);
      setResetKey((prev) => prev + 1);
    }
  };

  return (
    <form onSubmit={handleLogin}>
      {/* 用户名和密码输入框 */}
      
      {/* 智能验证码 */}
      <SmartCaptcha
        onSuccess={handleCaptchaSuccess}
        onFail={() => {
          setCaptchaResult(null);
          setResetKey((prev) => prev + 1);
        }}
        reset={resetKey > 0}
      />

      <button type="submit">登录</button>
    </form>
  );
}
```

## 📖 API 文档

### Props

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|--------|------|
| `onSuccess` | `(result: CaptchaResult) => void` | - | 验证成功回调 |
| `onFail` | `() => void` | - | 验证失败回调 |
| `className` | `string` | `''` | 自定义类名 |
| `reset` | `boolean` | `false` | 重置标志（触发刷新） |
| `forceType` | `CaptchaType` | - | 强制指定类型（不随机） |

### CaptchaResult 类型

```typescript
interface CaptchaResult {
  type: 'image' | 'rotate' | 'click';
  
  // 图形验证码
  captchaId?: string;
  captchaCode?: string;
  
  // 旋转验证码
  rotateId?: string;
  rotateAngle?: number;
  
  // 点选验证码
  clickId?: string;
  clickPoints?: Array<{x: number; y: number}>;
}
```

## 🎨 验证码类型详解

### 1. 图形验证码 (image)

**特点**: 经典的数字+字母验证码  
**用户操作**: 输入4位字符  
**验证结果**:
```typescript
{
  type: 'image',
  captchaId: 'uuid-xxx',
  captchaCode: 'A3B9'
}
```

### 2. 旋转验证码 (rotate)

**特点**: 旋转图片到正确角度  
**用户操作**: 拖动滑块旋转图片  
**验证结果**:
```typescript
{
  type: 'rotate',
  rotateId: 'uuid-xxx',
  rotateAngle: 90
}
```

### 3. 点选验证码 (click)

**特点**: 依次点击指定文字  
**用户操作**: 按顺序点击文字  
**验证结果**:
```typescript
{
  type: 'click',
  clickId: 'uuid-xxx',
  clickPoints: [
    {x: 50, y: 75},
    {x: 150, y: 120}
  ]
}
```

## 🔧 高级用法

### 限制验证码类型

```tsx
// 只使用图形和旋转验证码（排除点选）
<SmartCaptcha
  allowedTypes={['image', 'rotate']}
  onSuccess={handleSuccess}
/>
```

### 动态切换验证码

```tsx
const [captchaType, setCaptchaType] = useState<CaptchaType>('image');

<SmartCaptcha
  forceType={captchaType}
  onSuccess={handleSuccess}
/>

<button onClick={() => setCaptchaType('rotate')}>
  切换到旋转验证码
</button>
```

## 💡 最佳实践

1. **随机选择**: 默认随机选择验证码类型，增强安全性
2. **错误处理**: 验证失败后自动重置验证码
3. **用户体验**: 提供明确的提示和反馈
4. **移动端适配**: 所有验证码类型都支持触摸操作

## 🐛 常见问题

### Q: 如何在后端验证？

A: 根据不同的验证码类型，调用不同的后端API：

```typescript
// 图形验证码
POST /api/captcha/image/verify
{ captchaId, code }

// 旋转验证码
POST /api/captcha/rotate/verify
{ rotateId, angle }

// 点选验证码
POST /api/captcha/click/verify
{ clickId, clickPoints }
```

### Q: 验证码过期怎么办？

A: 所有验证码默认5分钟过期，过期后会自动提示用户刷新

### Q: 如何自定义样式？

A: 使用 `className` 属性添加自定义类名，然后覆盖CSS

## 📚 相关链接

- [RotateCaptcha 组件](../RotateCaptcha/)
- [ClickCaptcha 组件](../ClickCaptcha/)
- [ImageCaptcha 组件](../ImageCaptcha/)
- [后端API文档](../../../CAPTCHA_INTEGRATION_GUIDE.md)

---

**更新时间**: 2025-11-10  
**作者**: BaSui 😎
