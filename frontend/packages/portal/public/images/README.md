# 📸 轮播图图片说明

> **作者**: BaSui 😎 | **创建日期**: 2025-11-08

---

## 🎯 目录用途

此目录用于存储前端静态图片资源（轮播图、占位图等）。

---

## 📂 图片列表

### 轮播图（Hero Carousel）

| 文件名 | 尺寸 | 用途 | 状态 |
|--------|------|------|------|
| `hero-1.jpg` | 1920x500 | 首页轮播图1 - 校园轻享集市 | ⚠️ 待补充 |
| `hero-2.jpg` | 1920x500 | 首页轮播图2 - 安全交易 | ⚠️ 待补充 |
| `hero-3.jpg` | 1920x500 | 首页轮播图3 - 社区互动 | ⚠️ 待补充 |

---

## 🎨 设计规范

### 轮播图设计要求

- **尺寸**：1920x500 像素（宽x高）
- **格式**：JPG（压缩质量 85%）或 WebP
- **文件大小**：≤ 200KB
- **色调**：温暖、活力、校园风
- **文字**：清晰可读，避免过多文字

### 推荐配色

- **主色调**：#1890ff（蓝色）、#52c41a（绿色）
- **辅助色**：#faad14（橙色）、#f5222d（红色）
- **背景色**：渐变或纯色，避免过于花哨

---

## 🚀 临时解决方案

在正式设计图片之前，可以使用以下临时方案：

### 方案1：使用占位图服务

```typescript
// Hero.tsx
const CAROUSEL_DATA: CarouselItem[] = [
  {
    id: 1,
    title: '校园轻享集市',
    description: '让闲置物品找到新主人，让环保成为生活方式',
    imageUrl: 'https://via.placeholder.com/1920x500/1890ff/ffffff?text=Campus+Marketplace',
  },
  // ...
];
```

### 方案2：使用 Unsplash 免费图片

```typescript
const CAROUSEL_DATA: CarouselItem[] = [
  {
    id: 1,
    title: '校园轻享集市',
    description: '让闲置物品找到新主人，让环保成为生活方式',
    imageUrl: 'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=1920&h=500&fit=crop',
  },
  // ...
];
```

### 方案3：使用纯色背景 + CSS 渐变

```css
.hero__carousel-background {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
```

---

## 📝 TODO

- [ ] 设计并制作 3 张轮播图
- [ ] 优化图片大小（压缩至 ≤ 200KB）
- [ ] 转换为 WebP 格式（更小的文件体积）
- [ ] 添加响应式版本（移动端 750x400）

---

**BaSui 提示：** 在正式图片到位之前，建议先使用占位图或纯色背景，确保页面正常显示！😎
