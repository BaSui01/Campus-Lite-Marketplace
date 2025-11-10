# CaptchaSelector 组件使用说明

## 功能说明
验证码选择器组件，可以随机选择不同类型的验证码，增强安全性。

## 支持的验证码类型

| 类型 | 说明 | 状态 |
|-----|------|------|
| `image` | 图形验证码（4位数字+字母） | ✅ 已实现 |
| `slider` | 滑块验证码（拼图对齐） | ✅ 已实现 |
| `rotate` | 旋转验证码（旋转图片到正确角度） | 🚧 待实现 |
| `click` | 点选验证码（依次点击指定文字） | 🚧 待实现 |

## 基本使用

### 1. 随机选择验证码（推荐）
```tsx
import { CaptchaSelector } from '@campus/shared/components/CaptchaSelector';

function LoginPage() {
  const [resetKey, setResetKey] = useState(0);

  const handleSuccess = (type, data) => {
    console.log('验证成功:', type, data);
    // type: 'image' | 'slider' | 'rotate' | 'click'
    // data: { captchaId, code } 或 { slideId, xPosition } 等
  };

  return (
    <CaptchaSelector
      onSuccess={handleSuccess}
      reset={resetKey > 0}
    />
  );
}
```

### 2. 指定验证码类型
```tsx
<CaptchaSelector
  forceType="slider"  // 强制使用滑块验证码
  onSuccess={handleSuccess}
/>
```

### 3. 限制验证码类型
```tsx
<CaptchaSelector
  allowedTypes={['image', 'slider']}  // 只允许图形和滑块
  onSuccess={handleSuccess}
/>
```

## 完整示例（登录页面）

```tsx
import React, { useState } from 'react';
import { CaptchaSelector } from '@campus/shared/components/CaptchaSelector';
import { authService } from '@campus/shared/services/auth';

function LoginPage() {
  const [resetKey, setResetKey] = useState(0);
  const [captchaData, setCaptchaData] = useState<any>(null);

  const handleCaptchaSuccess = (type, data) => {
    console.log('验证码验证成功:', type, data);
    setCaptchaData({ type, ...data });
  };

  const handleLogin = async (e) => {
    e.preventDefault();

    if (!captchaData) {
      alert('请先完成验证码验证');
      return;
    }

    try {
      const loginData = {
        username: 'user@example.com',
        password: 'password123',
        // 根据验证码类型传递不同参数
        ...(captchaData.type === 'image' && {
          captchaId: captchaData.captchaId,
          captchaCode: captchaData.code,
        }),
        ...(captchaData.type === 'slider' && {
          slideId: captchaData.slideId,
          slidePosition: captchaData.xPosition,
        }),
      };

      await authService.login(loginData);
      console.log('登录成功！');
    } catch (error) {
      console.error('登录失败:', error);
      setResetKey((prev) => prev + 1); // 重置验证码
      setCaptchaData(null);
    }
  };

  return (
    <form onSubmit={handleLogin}>
      {/* 用户名和密码输入框 */}
      
      {/* 验证码选择器 */}
      <CaptchaSelector
        onSuccess={handleCaptchaSuccess}
        onFail={() => {
          setCaptchaData(null);
          setResetKey((prev) => prev + 1);
        }}
        reset={resetKey > 0}
        allowedTypes={['image', 'slider']}  // 目前只启用这两种
      />

      <button type="submit">登录</button>
    </form>
  );
}
```

## Props 说明

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|--------|------|
| `onSuccess` | `(type, data) => void` | - | 验证成功回调 |
| `onFail` | `() => void` | - | 验证失败回调 |
| `className` | `string` | `''` | 自定义类名 |
| `reset` | `boolean` | `false` | 重置标志（触发刷新验证码） |
| `allowedTypes` | `CaptchaType[]` | `['image', 'slider']` | 允许的验证码类型 |
| `forceType` | `CaptchaType` | - | 强制指定验证码类型（不随机） |

## 验证码数据格式

### 图形验证码
```ts
{
  type: 'image',
  captchaId: 'uuid-xxx',
  code: 'A3B9'
}
```

### 滑块验证码
```ts
{
  type: 'slider',
  slideId: 'uuid-xxx',
  xPosition: 120
}
```

## 注意事项

1. **随机性**：每次组件挂载或 `reset` 变化时，都会重新随机选择验证码类型
2. **兼容性**：目前只实现了图形和滑块验证码，旋转和点选验证码待实现
3. **安全性**：后端已支持所有验证码类型的验证，前端组件待完善
4. **跨包导入**：滑块验证码在 portal 包中，使用时需要注意包依赖

## 后续扩展

- [ ] 实现旋转验证码组件（RotateCaptcha）
- [ ] 实现点选验证码组件（ClickCaptcha）
- [ ] 添加验证码切换动画
- [ ] 支持自定义验证码组件
