# 📁 Uploads 目录说明

> **作者**: BaSui 😎 | **创建日期**: 2025-11-04

---

## 🎯 目录用途

此目录用于存储用户上传的静态文件（图片、文件等）。

---

## 📂 目录结构

```
uploads/
├── dev/              # 开发环境通用上传目录
├── goods/            # 商品图片目录
├── avatars/          # 用户头像目录
├── posts/            # 社区帖子图片目录
└── messages/         # 聊天消息文件目录
```

---

## 📝 各目录说明

### 1️⃣ **dev/** - 开发环境通用上传目录
- **用途**：开发环境的临时文件上传
- **配置**：`FILE_UPLOAD_DIR=./uploads/dev`
- **示例**：测试文件、临时文件

### 2️⃣ **goods/** - 商品图片目录
- **用途**：存储商品图片
- **配置**：`GOODS_IMAGE_UPLOAD_DIR=./uploads/goods`
- **命名规则**：`{商品ID}_{时间戳}_{随机数}.{扩展名}`
- **示例**：`123_1699012345678_abc123.jpg`
- **最大数量**：每个商品最多 9 张图片

### 3️⃣ **avatars/** - 用户头像目录
- **用途**：存储用户头像
- **配置**：`USER_AVATAR_UPLOAD_DIR=./uploads/avatars`
- **命名规则**：`user_{用户ID}.{扩展名}`
- **示例**：`user_123.jpg`
- **最大尺寸**：5 MB

### 4️⃣ **posts/** - 社区帖子图片目录
- **用途**：存储社区帖子中的图片
- **命名规则**：`post_{帖子ID}_{序号}.{扩展名}`
- **示例**：`post_456_1.jpg`
- **最大数量**：每个帖子最多 9 张图片

### 5️⃣ **messages/** - 聊天消息文件目录
- **用途**：存储聊天消息中的图片、语音、视频、文件
- **命名规则**：`msg_{消息ID}_{时间戳}.{扩展名}`
- **示例**：`msg_789_1699012345678.jpg`
- **支持类型**：图片、语音、视频、文件

---

## 🔧 配置说明

### **环境变量配置（.env）**

```env
# 文件上传配置
FILE_UPLOAD_DIR=./uploads/dev
FILE_UPLOAD_ENABLED=true

# 静态资源配置
STATIC_RESOURCE_BASE_URL=http://localhost:8200
GOODS_IMAGE_UPLOAD_DIR=./uploads/goods
USER_AVATAR_UPLOAD_DIR=./uploads/avatars

# 文件限制
FILE_MAX_SIZE=5242880                # 单文件最大 5MB
FILE_MAX_REQUEST_SIZE=20971520       # 单次请求最大 20MB
IMAGE_ALLOWED_EXTENSIONS=jpg,jpeg,png,gif,webp
IMAGE_MAX_SIZE=5242880               # 图片最大 5MB
GOODS_MAX_IMAGES=9                   # 商品最多 9 张图片
```

---

## 🚀 访问方式

### **开发环境**

```
http://localhost:8200/uploads/goods/123_1699012345678_abc123.jpg
http://localhost:8200/uploads/avatars/user_123.jpg
http://localhost:8200/uploads/posts/post_456_1.jpg
http://localhost:8200/uploads/messages/msg_789_1699012345678.jpg
```

### **生产环境**

**建议使用 OSS/CDN：**
- ✅ 阿里云 OSS：`https://your-bucket.oss-cn-hangzhou.aliyuncs.com/goods/123.jpg`
- ✅ 腾讯云 COS：`https://your-bucket.cos.ap-guangzhou.myqcloud.com/goods/123.jpg`
- ✅ 七牛云：`https://your-domain.qiniucdn.com/goods/123.jpg`

---

## ⚠️ 注意事项

### 1️⃣ **安全性**
- ✅ 文件类型验证（只允许图片、文档等安全类型）
- ✅ 文件大小限制（防止恶意上传大文件）
- ✅ 文件名随机化（防止路径遍历攻击）
- ❌ 禁止上传可执行文件（.exe, .sh, .bat 等）

### 2️⃣ **性能优化**
- ✅ 图片压缩（上传时自动压缩）
- ✅ 缩略图生成（商品图片、用户头像）
- ✅ CDN 加速（生产环境使用 CDN）
- ✅ 懒加载（前端按需加载图片）

### 3️⃣ **存储管理**
- ✅ 定期清理（删除过期文件）
- ✅ 备份策略（定期备份重要文件）
- ✅ 磁盘监控（监控磁盘使用率）
- ✅ 日志记录（记录文件上传日志）

### 4️⃣ **Git 管理**
- ✅ `.gitkeep` 文件：保留空目录结构
- ✅ `.gitignore` 规则：忽略上传的文件内容
- ❌ 不要提交用户上传的文件到 Git

---

## 📊 文件命名规范

### **商品图片**
```
格式：{商品ID}_{时间戳}_{随机数}.{扩展名}
示例：123_1699012345678_abc123.jpg
```

### **用户头像**
```
格式：user_{用户ID}.{扩展名}
示例：user_123.jpg
```

### **帖子图片**
```
格式：post_{帖子ID}_{序号}.{扩展名}
示例：post_456_1.jpg
```

### **聊天消息文件**
```
格式：msg_{消息ID}_{时间戳}.{扩展名}
示例：msg_789_1699012345678.jpg
```

---

## 🛠️ 开发指南

### **上传文件示例（Java）**

```java
@PostMapping("/upload")
public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
    // 1. 验证文件类型
    String contentType = file.getContentType();
    if (!isAllowedType(contentType)) {
        throw new BadRequestException("不支持的文件类型");
    }

    // 2. 验证文件大小
    if (file.getSize() > MAX_FILE_SIZE) {
        throw new BadRequestException("文件大小超过限制");
    }

    // 3. 生成文件名
    String fileName = generateFileName(file.getOriginalFilename());

    // 4. 保存文件
    Path filePath = Paths.get(uploadDir, fileName);
    Files.copy(file.getInputStream(), filePath);

    // 5. 返回文件 URL
    String fileUrl = baseUrl + "/uploads/goods/" + fileName;
    return ResponseEntity.ok(fileUrl);
}
```

---

## 🎉 总结

**uploads 目录是用户上传文件的核心存储位置！**

- ✅ **开发环境**：直接存储在本地 `uploads/` 目录
- ✅ **生产环境**：建议使用 OSS/CDN 存储
- ✅ **安全性**：文件类型验证、大小限制、文件名随机化
- ✅ **性能**：图片压缩、缩略图、CDN 加速

**有问题随时找 BaSui！😎✨**
