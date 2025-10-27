# 📋 Specs 工作流规范 - BaSui 专业版

> **适用于**: 校园轻享集市系统
> **作者**: BaSui 😎
> **更新**: 2025-10-27
> **优先级**: 🔥 最高（所有需求开发必须遵守）

---

## 🎯 核心理念

**需求驱动开发 (RDD - Requirements Driven Development)**：
```
需求分析 → 设计方案 → 任务拆解 → TDD 实现 → 代码审查
```

**三大原则**：
1. 📝 **需求先行** - 没有需求文档不准写代码！
2. 🔢 **编号唯一** - 全局需求编号绝不重复！
3. ✅ **可验收** - 每个需求都有明确验收标准！

---

## 📂 目录结构规范

### 1. Specs 文件组织

```
docs/
└── specs/
    ├── 1/                          # Spec #1: 基础架构
    │   ├── requirements.md         # 需求分析（需求 1-52）
    │   ├── design.md               # 设计方案
    │   └── tasks.md                # 任务拆解
    ├── 2/                          # Spec #2: 新功能模块
    │   ├── requirements.md         # 需求分析（需求 53-N）
    │   ├── design.md               # 设计方案
    │   └── tasks.md                # 任务拆解
    └── SPEC_INDEX.md               # 需求索引（防止编号重复）
```

### 2. 文件命名约定

- **requirements.md** - 需求分析文档（MUST）
- **design.md** - 设计方案文档（MUST）
- **tasks.md** - 任务拆解文档（MUST）
- **SPEC_INDEX.md** - 全局需求索引（MUST，根目录唯一）

---

## 🔢 需求编号管理（防重复）

### 1. 全局需求编号规则

**格式**：`需求 N`（N 为全局唯一递增整数）

**示例**：
```markdown
### 需求 1：分层架构设计
### 需求 2：用户认证与授权
### 需求 53：数据导出功能
```

**规则**：
- ✅ 编号从 1 开始，全局唯一递增
- ✅ 不同 Spec 文件夹之间**不能重复编号**
- ✅ 废弃的需求保留编号，标记 `[已废弃]`
- ❌ 禁止跳号（如 1, 2, 5, 6）
- ❌ 禁止重用已分配的编号

### 2. SPEC_INDEX.md 索引格式

**作用**：全局需求编号分配器，防止重复

**示例**：
```markdown
# Specs 需求索引

> **最后更新**: 2025-10-27
> **下一个可用编号**: 53

## 需求编号分配记录

| 编号范围 | Spec 编号 | 功能模块 | 状态 | 更新日期 |
|---------|----------|---------|------|---------|
| 1-52    | #1       | 基础架构 | ✅ 已完成 | 2025-10-25 |
| 53-65   | #2       | 数据导出 | 🚧 开发中 | 2025-10-27 |
| 66-80   | #3       | 用户画像 | 📝 规划中 | 2025-10-28 |

## 已废弃需求

| 编号 | 原功能 | 废弃原因 | 废弃日期 |
|-----|--------|---------|---------|
| 42  | 优惠券系统 | 业务调整 | 2025-10-26 |
```

---

## 📝 工作流程（强制执行）

### 阶段 1：需求分析 - requirements.md

**目标**：明确做什么、为什么做、怎么验收

**步骤**：

1️⃣ **读取 SPEC_INDEX.md，获取下一个可用编号**
```bash
# 示例：下一个可用编号 = 53
```

2️⃣ **创建需求文档**
```markdown
### 需求 53：数据导出功能

**用户故事**: 作为系统管理员，我希望导出用户数据为 Excel，以便进行数据分析。

#### 验收标准

1. WHEN Admin 请求导出用户数据时，THE System SHALL 验证管理员拥有 system:export:user 权限
2. WHEN Admin 请求导出时，THE System SHALL 生成包含所有用户信息的 Excel 文件
3. THE System SHALL 在 Excel 中包含以下字段：用户名、邮箱、注册时间、状态
4. THE System SHALL 对敏感信息脱敏（如手机号）
5. THE System SHALL 记录导出日志（操作人、导出时间、导出数据量）
```

3️⃣ **更新 SPEC_INDEX.md**
```markdown
| 53-65   | #2       | 数据导出 | 🚧 开发中 | 2025-10-27 |
```

**检查清单**：
- [ ] 需求编号从 SPEC_INDEX.md 获取？
- [ ] 需求有明确的用户故事？
- [ ] 验收标准使用 SHALL/WHEN/IF/THEN？
- [ ] 验收标准可测试、可验证？
- [ ] 已更新 SPEC_INDEX.md？

---

### 阶段 2：设计方案 - design.md

**目标**：明确怎么做、用什么技术、怎么实现

**步骤**：

1️⃣ **创建设计文档**
```markdown
# 数据导出功能 - 设计方案

## 1. 架构设计

### 1.1 技术选型

- **Excel 库**：Apache POI 5.x
- **异步处理**：Spring @Async
- **文件存储**：本地临时目录 + 定时清理

### 1.2 核心组件

- `ExportService` - 导出服务
- `ExcelExporter` - Excel 生成器
- `ExportController` - 导出接口

## 2. 数据库设计

### 2.1 实体设计

- `ExportLog` - 导出日志表

### 2.2 字段定义

```java
@Entity
@Table(name = "export_logs")
public class ExportLog {
    @Id private Long id;
    private Long userId;        // 操作人
    private String exportType;  // 导出类型
    private Integer recordCount;// 导出数据量
    private LocalDateTime createdAt;
}
```

## 3. API 设计

### 3.1 接口定义

```java
GET /api/admin/export/users
Response: { "code": 200, "data": "file-url" }
```
```

**检查清单**：
- [ ] 技术选型合理且符合项目规范？
- [ ] 数据库设计符合规范化要求？
- [ ] API 设计符合 RESTful 风格？
- [ ] 设计考虑了性能和安全性？

---

### 阶段 3：任务拆解 - tasks.md

**目标**：将设计拆解为可执行的开发任务

**步骤**：

1️⃣ **创建任务列表**
```markdown
# 数据导出功能 - 任务拆解

> **关联需求**: 需求 53-65
> **开始日期**: 2025-10-27
> **预计工期**: 3 天

## 任务列表

### 任务 1：创建 ExportLog 实体 ⏱️ 1h

- [ ] 创建 ExportLog.java
- [ ] 创建 ExportLogRepository.java
- [ ] 创建数据库迁移脚本
- [ ] 编写 ExportLog 单元测试

**验收**：测试覆盖率 ≥85%

---

### 任务 2：实现 ExcelExporter 工具类 ⏱️ 2h

- [ ] 添加 Apache POI 依赖
- [ ] 实现 ExcelExporter.exportUsers()
- [ ] 实现敏感信息脱敏逻辑
- [ ] 编写 ExcelExporter 单元测试

**验收**：导出 Excel 格式正确，脱敏有效

---

### 任务 3：实现 ExportService 服务 ⏱️ 2h

- [ ] 创建 ExportService.java
- [ ] 实现 exportUsers() 方法
- [ ] 实现权限验证
- [ ] 实现导出日志记录
- [ ] 编写 ExportService 单元测试

**验收**：测试覆盖率 ≥85%

---

### 任务 4：实现 ExportController 接口 ⏱️ 1.5h

- [ ] 创建 ExportController.java
- [ ] 实现 GET /api/admin/export/users
- [ ] 添加参数校验
- [ ] 编写集成测试

**验收**：API 测试通过
```

**检查清单**：
- [ ] 任务拆分粒度合理（每个任务 1-3 小时）？
- [ ] 任务有明确的验收标准？
- [ ] 任务包含 TDD 测试编写？
- [ ] 任务工时估算合理？

---

## 🚦 开发流程（TDD 强制）

### 1. 开发前（MUST）

```
✅ 第一步：读取 requirements.md，理解需求
✅ 第二步：读取 design.md，理解设计
✅ 第三步：读取 tasks.md，选择任务
✅ 第四步：更新 tasks.md，标记 [-] 进行中
```

### 2. 开发中（TDD）

```
🔴 红灯：先写失败测试
🟢 绿灯：最小代码让测试通过
🔵 重构：优化代码结构
```

### 3. 开发后（MUST）

```
✅ 第一步：确保测试覆盖率 ≥85%
✅ 第二步：更新 tasks.md，标记 [x] 已完成
✅ 第三步：git commit（使用 /zcf:git-commit）
```

---

## 🔍 需求变更管理

### 1. 新增需求

```
1️⃣ 读取 SPEC_INDEX.md，获取下一个编号
2️⃣ 在对应 Spec 的 requirements.md 中添加需求
3️⃣ 更新 SPEC_INDEX.md 分配记录
4️⃣ 在 design.md 中补充设计
5️⃣ 在 tasks.md 中添加任务
```

### 2. 修改需求

```
1️⃣ 在 requirements.md 中标记变更历史
2️⃣ 更新 design.md 中受影响的部分
3️⃣ 更新 tasks.md 中受影响的任务
4️⃣ 记录变更日志
```

### 3. 废弃需求

```
1️⃣ 在 requirements.md 中标记 [已废弃]
2️⃣ 在 SPEC_INDEX.md 的"已废弃需求"中记录
3️⃣ 不重用该编号
```

---

## ✅ Specs 检查清单

### 创建新 Spec 前

- [ ] 已读取 SPEC_INDEX.md 确认下一个编号？
- [ ] 已确认新 Spec 不与现有需求重复？
- [ ] 已创建对应的 Spec 文件夹（如 docs/specs/3/）？

### 编写 requirements.md 后

- [ ] 需求编号全局唯一且连续？
- [ ] 每个需求有用户故事？
- [ ] 每个需求有验收标准？
- [ ] 验收标准可测试、可验证？
- [ ] 已更新 SPEC_INDEX.md？

### 编写 design.md 后

- [ ] 技术选型合理？
- [ ] 数据库设计规范化？
- [ ] API 设计符合 RESTful？
- [ ] 考虑了性能和安全性？

### 编写 tasks.md 后

- [ ] 任务拆分粒度合理（1-3 小时/任务）？
- [ ] 每个任务有验收标准？
- [ ] 每个任务包含 TDD 测试？
- [ ] 工时估算合理？

---

## 🎨 BaSui 的最后叮嘱

**记住这三条铁律**：

1. 📝 **需求先行** - 没有 requirements.md 不准写代码！
2. 🔢 **编号唯一** - 先读 SPEC_INDEX.md，再分配编号！
3. ✅ **TDD 开发** - 先写测试，再写实现！

**座右铭**：
> 需求清晰，开发不慌！
> 编号唯一，管理不乱！
> TDD 护航，质量不降！💪✨

**违反规则？回炉重造！😤**

---

**本规范是校园轻享集市系统的开发基石，所有 AI 和开发人员必须严格遵守！** 🚀
