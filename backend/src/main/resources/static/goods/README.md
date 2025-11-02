# 商品图片静态资源目录 📸

## 📁 目录说明

这个目录存放商品展示图片（开发/测试环境使用）。

## 🎯 文件命名规则

- **真实商品图**: 参考数据库 `V2__seed_data.sql` 中定义的文件名
- **占位图**: 使用 `placeholder-{size}.jpg` 格式

## 📝 需要的图片文件（来自种子数据）

```bash
# 真实商品图片（共 4 个）
ipad-air5-front.jpg         # iPad Air 5 正面图
ipad-air5-package.jpg       # iPad Air 5 包装盒图
cet-materials-cover.jpg     # 四六级资料封面
projector-rental.jpg        # 投影仪图片
hoodie-blue-front.jpg       # 蓝色卫衣正面

# 自动生成的种子图片（共 19 个）
seed-05.jpg
seed-07.jpg
seed-08.jpg
seed-10.jpg
seed-11.jpg
seed-13.jpg
seed-14.jpg
seed-16.jpg
seed-17.jpg
seed-19.jpg
# ... 等等（数据库会自动生成 seed-01 到 seed-19）
```

## 🎨 占位图生成方案

**方案 1：使用在线占位图服务**
```
https://via.placeholder.com/400x300.jpg?text=商品图片
```

**方案 2：使用 ImageMagick 生成本地占位图**
```bash
# 安装 ImageMagick
# Windows: choco install imagemagick
# macOS: brew install imagemagick
# Linux: apt-get install imagemagick

# 生成占位图
magick -size 400x300 -background "#f0f0f0" -fill "#999" \
  -gravity center label:"商品图片" ipad-air5-front.jpg
```

**方案 3：使用 Python Pillow 批量生成**
```python
from PIL import Image, ImageDraw, ImageFont

def create_placeholder(filename, text="商品图片", size=(400, 300)):
    img = Image.new('RGB', size, color='#f0f0f0')
    draw = ImageDraw.Draw(img)

    # 简单文字（不依赖字体文件）
    w, h = size
    text_bbox = draw.textbbox((0, 0), text)
    text_w = text_bbox[2] - text_bbox[0]
    text_h = text_bbox[3] - text_bbox[1]

    position = ((w - text_w) // 2, (h - text_h) // 2)
    draw.text(position, text, fill='#999999')

    img.save(filename, 'JPEG')

# 批量生成
images = [
    'ipad-air5-front.jpg',
    'ipad-air5-package.jpg',
    'cet-materials-cover.jpg',
    'projector-rental.jpg',
    'hoodie-blue-front.jpg',
]

for img_name in images:
    create_placeholder(img_name)

# 生成种子图片
for i in range(5, 20):
    create_placeholder(f'seed-{i:02d}.jpg', f'商品 {i:02d}')
```

## 🚀 快速使用

**开发环境不想生成图片？**

前端可以使用占位图服务作为 fallback：
```typescript
const imageUrl = `/goods/${filename}`;
const fallbackUrl = `https://via.placeholder.com/400x300.jpg?text=${encodeURIComponent('商品图片')}`;

<img src={imageUrl} onError={(e) => e.target.src = fallbackUrl} />
```

## 📦 生产环境

生产环境应使用 CDN 或对象存储服务（OSS）：
- 阿里云 OSS
- 腾讯云 COS
- 七牛云 Kodo
- 又拍云 USS

配置方式：修改 `application.yml` 中的 `STATIC_RESOURCE_BASE_URL` 环境变量。
