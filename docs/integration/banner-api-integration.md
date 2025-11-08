# 🎨 轮播图 API 集成方案

> **作者**: BaSui 😎 | **创建日期**: 2025-11-08

---

## 🎯 方案概述

将轮播图从**硬编码**改为**后端 API 动态获取**，支持通过管理后台配置。

---

## 📋 后端实现（已完成）

### 1. 数据库表结构

```sql
CREATE TABLE t_banner (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    image_url VARCHAR(500) NOT NULL,
    link_url VARCHAR(500),
    sort_order INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    click_count INTEGER NOT NULL DEFAULT 0,
    view_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_banner_status ON t_banner(status);
CREATE INDEX idx_banner_sort_order ON t_banner(sort_order);
```

### 2. API 接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取启用的轮播图 | GET | `/api/banners/active` | 前台使用 |
| 记录点击 | POST | `/api/banners/{id}/click` | 统计点击 |
| 记录展示 | POST | `/api/banners/{id}/view` | 统计展示 |

### 3. 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "校园轻享集市",
      "description": "让闲置物品找到新主人，让环保成为生活方式",
      "imageUrl": "/uploads/images/banners/hero-1.jpg",
      "linkUrl": "/goods",
      "sortOrder": 1,
      "status": "ENABLED"
    },
    {
      "id": 2,
      "title": "安全交易，放心购物",
      "description": "实名认证，交易保障，让每一笔交易都安心",
      "imageUrl": "/uploads/images/banners/hero-2.jpg",
      "linkUrl": "/about",
      "sortOrder": 2,
      "status": "ENABLED"
    }
  ]
}
```

---

## 🎨 前端改造

### 1. 创建 API 服务

```typescript
// frontend/packages/shared/src/api/banner.ts
import { request } from './request';

export interface Banner {
  id: number;
  title: string;
  description: string;
  imageUrl: string;
  linkUrl?: string;
  sortOrder: number;
  status: string;
}

/**
 * 获取启用的轮播图
 */
export const getActiveBanners = async (): Promise<Banner[]> => {
  const response = await request.get<Banner[]>('/banners/active');
  return response.data;
};

/**
 * 记录轮播图点击
 */
export const recordBannerClick = async (id: number): Promise<void> => {
  await request.post(`/banners/${id}/click`);
};

/**
 * 记录轮播图展示
 */
export const recordBannerView = async (id: number): Promise<void> => {
  await request.post(`/banners/${id}/view`);
};
```

### 2. 改造 Hero 组件

```typescript
// frontend/packages/portal/src/pages/Home/Hero.tsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input } from '@campus/shared/components';
import { getActiveBanners, recordBannerClick, recordBannerView, Banner } from '@campus/shared/api';

export const Hero: React.FC = () => {
  const navigate = useNavigate();
  const [currentIndex, setCurrentIndex] = useState(0);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [banners, setBanners] = useState<Banner[]>([]);
  const [loading, setLoading] = useState(true);

  // 🎯 从 API 获取轮播图
  useEffect(() => {
    const fetchBanners = async () => {
      try {
        const data = await getActiveBanners();
        setBanners(data);
        
        // 记录第一张轮播图的展示
        if (data.length > 0) {
          recordBannerView(data[0].id);
        }
      } catch (error) {
        console.error('获取轮播图失败:', error);
        // 降级方案：使用默认轮播图
        setBanners(DEFAULT_BANNERS);
      } finally {
        setLoading(false);
      }
    };

    fetchBanners();
  }, []);

  // 自动轮播（5秒间隔）
  useEffect(() => {
    if (banners.length === 0) return;

    const timer = setInterval(() => {
      setCurrentIndex((prev) => {
        const nextIndex = (prev + 1) % banners.length;
        // 记录新轮播图的展示
        recordBannerView(banners[nextIndex].id);
        return nextIndex;
      });
    }, 5000);

    return () => clearInterval(timer);
  }, [banners]);

  // 处理轮播图点击
  const handleBannerClick = () => {
    const currentBanner = banners[currentIndex];
    if (currentBanner) {
      // 记录点击
      recordBannerClick(currentBanner.id);
      
      // 跳转链接
      if (currentBanner.linkUrl) {
        navigate(currentBanner.linkUrl);
      }
    }
  };

  // 加载中状态
  if (loading) {
    return <div className="hero hero--loading">加载中...</div>;
  }

  // 没有轮播图时的降级方案
  if (banners.length === 0) {
    return <div className="hero hero--empty">暂无轮播图</div>;
  }

  const currentBanner = banners[currentIndex];

  return (
    <section className="hero">
      <div className="hero__carousel">
        {/* 轮播图背景 */}
        <div
          className="hero__carousel-background"
          style={{
            backgroundImage: `linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)), url(${import.meta.env.VITE_STATIC_BASE_URL}${currentBanner.imageUrl})`,
            cursor: currentBanner.linkUrl ? 'pointer' : 'default',
          }}
          onClick={handleBannerClick}
        >
          {/* 轮播内容 */}
          <div className="hero__carousel-content">
            <h1 className="hero__title">{currentBanner.title}</h1>
            <p className="hero__description">{currentBanner.description}</p>

            {/* 搜索框 */}
            <div className="hero__search">
              {/* ... 搜索框代码 ... */}
            </div>
          </div>

          {/* 轮播指示器 */}
          <div className="hero__carousel-dots">
            {banners.map((_, index) => (
              <button
                key={index}
                className={`hero__carousel-dot ${index === currentIndex ? 'active' : ''}`}
                onClick={(e) => {
                  e.stopPropagation();
                  setCurrentIndex(index);
                  recordBannerView(banners[index].id);
                }}
                aria-label={`第${index + 1}张`}
              />
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};

// 降级方案：默认轮播图（API 失败时使用）
const DEFAULT_BANNERS: Banner[] = [
  {
    id: 0,
    title: '校园轻享集市',
    description: '让闲置物品找到新主人，让环保成为生活方式',
    imageUrl: 'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=1920&h=500&fit=crop&q=80',
    sortOrder: 1,
    status: 'ENABLED',
  },
];
```

---

## 🗄️ 数据库初始化

### 插入测试数据

```sql
-- 插入 3 张轮播图
INSERT INTO t_banner (title, description, image_url, link_url, sort_order, status) VALUES
('校园轻享集市', '让闲置物品找到新主人，让环保成为生活方式', '/uploads/images/banners/hero-1.jpg', '/goods', 1, 'ENABLED'),
('安全交易，放心购物', '实名认证，交易保障，让每一笔交易都安心', '/uploads/images/banners/hero-2.jpg', '/about', 2, 'ENABLED'),
('社区互动，分享生活', '不仅是交易平台，更是校园生活的分享社区', '/uploads/images/banners/hero-3.jpg', '/community', 3, 'ENABLED');
```

---

## 🎉 优势对比

| 特性 | 硬编码方案 | API 动态获取 |
|------|-----------|-------------|
| **灵活性** | ❌ 修改需重新部署 | ✅ 后台实时配置 |
| **管理性** | ❌ 需要开发人员 | ✅ 运营人员可管理 |
| **统计** | ❌ 无法统计 | ✅ 点击/展示统计 |
| **定时上下线** | ❌ 不支持 | ✅ 支持定时 |
| **A/B 测试** | ❌ 不支持 | ✅ 可扩展支持 |

---

## 📝 后续扩展

### 1. 管理后台

- [ ] 轮播图列表页
- [ ] 轮播图新增/编辑页
- [ ] 图片上传功能
- [ ] 排序拖拽功能
- [ ] 定时上下线设置

### 2. 高级功能

- [ ] A/B 测试（不同用户看不同轮播图）
- [ ] 个性化推荐（根据用户兴趣）
- [ ] 视频轮播图
- [ ] 动画效果配置

---

**BaSui 提示：** 这才是专业的轮播图实现方式！😎
