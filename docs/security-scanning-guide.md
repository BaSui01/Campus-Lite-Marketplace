# 🔒 安全扫描使用指南

> **作者**: BaSui 😎
> **更新**: 2025-10-29
> **目的**: 保障项目依赖安全，及时发现并修复高危漏洞

---

## 📋 目录

1. [快速开始](#快速开始)
2. [本地扫描](#本地扫描)
3. [CI 自动扫描](#ci-自动扫描)
4. [查看扫描报告](#查看扫描报告)
5. [处理安全漏洞](#处理安全漏洞)
6. [抑制误报](#抑制误报)
7. [常见问题](#常见问题)

---

## 🚀 快速开始

### 1. 本地运行一次完整扫描

```bash
cd backend
mvn clean org.owasp:dependency-check-maven:check
```

**首次运行**会下载 NVD 数据库（约 500MB），需要等待 5-10 分钟。后续运行只需 1-2 分钟。

**扫描完成后**：
- 📄 查看 HTML 报告：`backend/target/dependency-check-report.html`
- 📊 查看 JSON 报告：`backend/target/dependency-check-report.json`

---

## 💻 本地扫描

### 基础命令

```bash
# 完整扫描（推荐）
mvn org.owasp:dependency-check-maven:check

# 仅扫描不失败构建（查看报告用）
mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=10

# 快速扫描（跳过 NVD 更新）
mvn org.owasp:dependency-check-maven:check -DautoUpdate=false

# 指定 CVSS 阈值（默认 7）
mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=8
```

### 高级选项

```bash
# 生成 SARIF 格式报告（用于 GitHub Security）
mvn org.owasp:dependency-check-maven:check -Dformat=SARIF

# 使用自定义抑制文件
mvn org.owasp:dependency-check-maven:check \
  -DsuppressionFile=.owasp/custom-suppressions.xml

# 清理 NVD 缓存（数据库损坏时）
mvn org.owasp:dependency-check-maven:purge
```

---

## 🤖 CI 自动扫描

### 1. 质量门禁扫描（每次 Push/PR）

**触发条件**：
- Push 到 `backend/**` 路径
- 创建/更新 Pull Request

**执行流程**：
```yaml
# .github/workflows/backend-quality-gates.yml
- 单元测试 (Surefire)
- 集成测试 + 覆盖率 (Failsafe + JaCoCo)
- 依赖安全扫描 (OWASP Dependency-Check) ← 这里
- 变异测试 (PIT)
```

**查看结果**：
- GitHub Actions → `Backend Quality Gates` → Artifacts → `dependency-check-report`

---

### 2. 每周定期扫描（自动告警）

**触发时间**：每周一凌晨 2:00 UTC（北京时间 10:00）

**功能特性**：
- ✅ 自动扫描所有依赖
- ✅ 生成 HTML + SARIF 报告
- ✅ 上传到 GitHub Security
- ✅ **发现高危漏洞自动创建 Issue**
- ✅ 报告保留 90 天

**手动触发**：
1. 打开 GitHub → Actions
2. 选择 `Security Weekly Scan`
3. 点击 `Run workflow`
4. 选择 CVSS 阈值（默认 7）
5. 点击 `Run workflow` 确认

**查看告警 Issue**：
- GitHub Issues → 标签 `security` + `dependencies`
- Issue 标题格式：`🔒 安全告警：发现 X 个高危依赖漏洞 (Week YYYY-MM-DD)`

---

## 📊 查看扫描报告

### 本地报告

**HTML 报告**（推荐）：
```bash
# 在浏览器中打开
open backend/target/dependency-check-report.html  # macOS
xdg-open backend/target/dependency-check-report.html  # Linux
start backend/target/dependency-check-report.html  # Windows
```

**报告内容**：
- 📈 漏洞统计（严重/高危/中危/低危）
- 📦 受影响的依赖列表
- 🔍 CVE 详情和 CVSS 分数
- 🔗 NVD/CVE 官方链接

---

### GitHub Security 报告

1. 打开 GitHub 仓库
2. 点击 `Security` 标签
3. 选择 `Code scanning alerts`
4. 筛选 `dependency-check` 类别

**优势**：
- 📊 可视化漏洞趋势
- 🔔 邮件/Slack 通知
- 🔄 自动关联 PR
- 📝 支持批注和讨论

---

## 🛠️ 处理安全漏洞

### 标准流程

```
1️⃣ 分析报告
   └─ 确认漏洞影响范围、CVSS 分数、可利用性

2️⃣ 评估风险
   ├─ 是否影响生产环境？
   ├─ 是否有可用的补丁？
   └─ 升级是否会破坏兼容性？

3️⃣ 制定方案
   ├─ 升级依赖版本（首选）
   ├─ 替换依赖库（次选）
   ├─ 抑制误报（评估后）
   └─ 添加缓解措施（临时）

4️⃣ 执行修复
   ├─ 更新 pom.xml
   ├─ 运行测试 (mvn verify)
   └─ 提交 PR 并关联 Issue

5️⃣ 验证修复
   ├─ 重新扫描确认漏洞消失
   ├─ 更新安全整改计划文档
   └─ 关闭相关 Issue
```

---

### 升级依赖示例

**场景**：发现 `com.example:library` 存在 CVE-2024-12345

```xml
<!-- 1. 在 dependencyManagement 中覆盖版本 -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>library</artifactId>
            <version>2.1.4</version>  <!-- 修复版本 -->
        </dependency>
    </dependencies>
</dependencyManagement>
```

```bash
# 2. 验证升级
mvn dependency:tree | grep com.example:library

# 3. 运行完整测试
mvn clean verify

# 4. 重新扫描确认
mvn org.owasp:dependency-check-maven:check

# 5. 提交更改
git add backend/pom.xml
git commit -m "🔒 security: 升级 com.example:library 至 2.1.4 修复 CVE-2024-12345"
```

---

## 🚫 抑制误报

### 何时使用抑制规则？

- ✅ CVE 不适用于当前使用场景
- ✅ 仅影响测试依赖（不打包到生产）
- ✅ 已评估风险并有缓解措施
- ❌ **不要**因为懒得升级而抑制！

---

### 添加抑制规则

编辑 `backend/.owasp/suppressions.xml`：

```xml
<suppress>
   <notes>
       CVE-2024-12345 仅影响 Windows 环境，本项目仅部署在 Linux 容器中
       评估人：BaSui
       评估日期：2025-10-29
       风险评估：低（不适用）
   </notes>
   <cve>CVE-2024-12345</cve>
</suppress>
```

**重要**：
- ✅ 必须添加详细的 `<notes>` 说明原因
- ✅ 必须注明评估人和评估日期
- ✅ 定期审查抑制规则（建议每季度）

---

### 抑制规则示例

**按 CVE 抑制**：
```xml
<suppress>
   <notes>已评估为低风险</notes>
   <cve>CVE-2024-12345</cve>
</suppress>
```

**按依赖抑制（支持正则）**：
```xml
<suppress>
   <notes>Testcontainers 仅用于测试环境</notes>
   <gav regex="true">^org\.testcontainers:.*</gav>
</suppress>
```

**按 CVSS 分数抑制**：
```xml
<suppress>
   <notes>抑制所有低危漏洞（CVSS < 4.0）</notes>
   <cvssBelow>4.0</cvssBelow>
</suppress>
```

---

## ❓ 常见问题

### Q1: 首次扫描很慢怎么办？

**A**: 首次运行需要下载 NVD 数据库（约 500MB），耐心等待。后续运行会使用缓存，只需 1-2 分钟。

**加速方法**：
```bash
# 使用已有的缓存（如果团队成员已下载）
cp -r ~/.m2/repository/org/owasp/dependency-check-data /path/to/your/.m2/repository/org/owasp/
```

---

### Q2: 扫描失败提示 "Unable to download NVD data"？

**A**: 网络问题导致无法连接 NVD 官方服务器。

**解决方案**：
1. 检查网络连接
2. 配置 HTTP 代理（如果需要）
3. 使用离线模式：`mvn -Dcve.url.modified=file:///path/to/local/nvd`

---

### Q3: 如何查看某个依赖的完整依赖树？

**A**: 使用 Maven 依赖树工具：

```bash
# 查看完整依赖树
mvn dependency:tree

# 查看特定依赖的路径
mvn dependency:tree -Dincludes=com.example:library

# 输出到文件
mvn dependency:tree > dependency-tree.txt
```

---

### Q4: CI 扫描失败但本地扫描通过？

**A**: 可能是 NVD 数据库版本不一致。

**解决方案**：
```bash
# 清理本地缓存
mvn org.owasp:dependency-check-maven:purge

# 重新扫描
mvn org.owasp:dependency-check-maven:check
```

---

### Q5: 如何定期更新 NVD 数据库？

**A**: NVD 数据库会自动更新（如果 `autoUpdate=true`）。

**手动更新**：
```bash
# 强制更新 NVD 数据库
mvn org.owasp:dependency-check-maven:update-only
```

**建议**：每月手动更新一次，确保漏洞信息最新。

---

## 📚 相关文档

- [质量保障计划](quality-assurance-plan.md)
- [OWASP Dependency-Check 官方文档](https://jeremylong.github.io/DependencyCheck/)
- [Maven 依赖管理最佳实践](../backend/pom.xml)
- [GitHub Security 文档](https://docs.github.com/en/code-security)

---

## 💡 最佳实践

1. **定期扫描**：每周至少运行一次完整扫描
2. **及时升级**：发现高危漏洞立即评估和修复
3. **记录决策**：抑制规则必须详细注释原因
4. **团队协作**：安全告警 Issue 及时分配和跟进
5. **持续改进**：定期审查抑制规则和安全策略

---

**🎯 记住 BaSui 的安全口诀**：
> 安全漏洞要堵牢，但过程可以很搞笑！
> 依赖升级不可怕，测试通过笑哈哈！
> 定期扫描养习惯，高危漏洞无处藏！💪✨
