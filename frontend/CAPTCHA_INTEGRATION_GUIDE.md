# 验证码集成指南 📚

## 🎯 快速开始

### 后端已实现的验证码类型

| 验证码类型 | 状态 | API端点 | 说明 |
|----------|------|---------|------|
| 图形验证码 | ✅ 完成 | `/api/captcha/image` | 4位数字+字母，带干扰线 |
| 滑块验证码 | ✅ 完成 | `/api/captcha/slide/image` | 真实拼图形状，轨迹分析 |
| 旋转验证码 | ✅ 完成 | `/api/captcha/rotate` | 旋转图片到正确角度 |
| 点选验证码 | ✅ 完成 | `/api/captcha/click` | 依次点击指定文字 |

### 前端已实现的组件

| 组件 | 状态 | 位置 | 说明 |
|-----|------|------|------|
| ImageCaptcha | ✅ 完成 | `packages/shared/src/components/ImageCaptcha` | 图形验证码 |
| SliderCaptcha | ✅ 完成 | `packages/portal/src/components/SliderCaptcha` | 滑块验证码 |
| SmartCaptcha | ✅ 完成 | `packages/shared/src/components/SmartCaptcha` | 智能选择器（暂时只支持图形） |
| RotateCaptcha | 🚧 待实现 | - | 旋转验证码 |
| ClickCaptcha | 🚧 待实现 | - | 点选验证码 |

---

## 📖 使用方法

### 方法1：使用SmartCaptcha（推荐）

在登录/注册页面使用智能验证码组件：

```tsx
import { SmartCaptcha, CaptchaResult } from '@campus/shared/components/SmartCaptcha';

function LoginPage() {
  const [resetKey, setResetKey] = useState(0);

  const handleCaptchaSuccess = (result: CaptchaResult) => {
    console.log('验证成功:', result);
    // result.type: 'image' | 'slider'
    // result.captchaId, result.captchaCode (图形验证码)
    // result.slideId, result.slidePosition (滑块验证码)
  };

  return (
    <SmartCaptcha
      onSuccess={handleCaptchaSuccess}
      onFail={() => setResetKey((prev) => prev + 1)}
      reset={resetKey > 0}
    />
  );
}
```

### 方法2：直接使用单个验证码组件

#### 图形验证码
```tsx
import { ImageCaptcha } from '@campus/shared/components/ImageCaptcha';

<ImageCaptcha
  onSuccess={(captchaId, code) => {
    console.log('验证成功:', captchaId, code);
  }}
  reset={resetKey > 0}
/>
```

#### 滑块验证码
```tsx
import SliderCaptcha from '@campus/portal/components/SliderCaptcha';

<SliderCaptcha
  onSuccess={(slideId, xPosition) => {
    console.log('验证成功:', slideId, xPosition);
  }}
  text="拖动滑块完成拼图"
/>
```

---

## 🔧 后端API使用

### 1. 图形验证码

**生成验证码**
```http
GET /api/captcha/image

Response:
{
  "code": 0,
  "message": "验证码生成成功",
  "data": {
    "captchaId": "uuid-xxx",
    "imageBase64": "data:image/png;base64,iVBORw0KGgo...",
    "expiresIn": 300
  }
}
```

**验证验证码**
```http
POST /api/captcha/image/verify
Content-Type: application/json

{
  "captchaId": "uuid-xxx",
  "code": "A3B9"
}
```

### 2. 滑块验证码

**生成验证码**
```http
GET /api/captcha/slide/image

Response:
{
  "code": 0,
  "data": {
    "slideId": "uuid-xxx",
    "backgroundImage": "data:image/png;base64,...",
    "sliderImage": "data:image/png;base64,...",
    "yposition": 75,
    "expiresIn": 300
  }
}
```

**验证验证码**
```http
POST /api/captcha/slide/verify/track
Content-Type: application/json

{
  "slideId": "uuid-xxx",
  "xposition": 120,
  "track": [
    {"x": 0, "y": 0, "t": 0},
    {"x": 10, "y": 1, "t": 100},
    {"x": 120, "y": 2, "t": 500}
  ]
}
```

### 3. 旋转验证码

**生成验证码**
```http
GET /api/captcha/rotate

Response:
{
  "code": 0,
  "data": {
    "rotateId": "uuid-xxx",
    "originalImage": "data:image/png;base64,...",
    "rotatedImage": "data:image/png;base64,...",
    "expiresIn": 300
  }
}
```

**验证验证码**
```http
POST /api/captcha/rotate/verify
Content-Type: application/json

{
  "rotateId": "uuid-xxx",
  "angle": 90
}
```

### 4. 点选验证码

**生成验证码**
```http
GET /api/captcha/click

Response:
{
  "code": 0,
  "data": {
    "clickId": "uuid-xxx",
    "backgroundImage": "data:image/png;base64,...",
    "targetWords": ["春", "天"],
    "hint": "请依次点击【春】【天】",
    "expiresIn": 300
  }
}
```

**验证验证码**
```http
POST /api/captcha/click/verify
Content-Type: application/json

{
  "clickId": "uuid-xxx",
  "clickPoints": [
    {"x": 50, "y": 75},
    {"x": 150, "y": 120}
  ]
}
```

---

## 🎨 前端实现TODO

### 优先级高
- [ ] 整合SliderCaptcha到SmartCaptcha
- [ ] 实现RotateCaptcha组件
- [ ] 实现ClickCaptcha组件

### 优先级中
- [ ] 添加验证码切换动画
- [ ] 优化移动端体验
- [ ] 添加验证码预加载

### 优先级低
- [ ] 添加验证码统计
- [ ] 支持自定义验证码样式
- [ ] 添加无障碍支持

---

## 🧪 测试步骤

1. **启动后端服务**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **启动前端开发服务器**
   ```bash
   cd frontend/packages/portal
   pnpm dev
   ```

3. **测试验证码**
   - 访问 `http://localhost:5173`
   - 打开登录页面
   - 测试各种验证码类型

---

## 📝 开发笔记

### 滑块验证码修复
- ✅ 修复了拼图对齐问题
- ✅ 使用Path2D生成真实拼图形状
- ✅ 滑块和缺口形状完全匹配

### 新增验证码类型
- ✅ 旋转验证码（后端完成）
- ✅ 点选验证码（后端完成）
- 🚧 前端组件待实现

---

## 🔗 相关链接

- [后端验证码服务](../backend/src/main/java/com/campus/marketplace/service/impl/CaptchaServiceImpl.java)
- [验证码Controller](../backend/src/main/java/com/campus/marketplace/controller/CaptchaController.java)
- [前端ImageCaptcha组件](./packages/shared/src/components/ImageCaptcha/)
- [前端SliderCaptcha组件](./packages/portal/src/components/SliderCaptcha/)

---

**更新时间**: 2025-11-10  
**作者**: BaSui 😎
