# 消息搜索功能实现指南

> **基于现有架构扩展**
> **优先级**: 高
> **作者**: BaSui 😎
> **日期**: 2025-11-07

## 📋 实施步骤

### ✅ 已完成的工作

1. **DTO设计** - 完成所有搜索相关的数据传输对象
2. **实体类设计** - 完成搜索历史和统计实体
3. **Repository接口** - 完成数据访问层接口
4. **Service接口扩展** - 完成MessageService接口扩展
5. **Controller扩展** - 完成MessageController接口添加
6. **数据库迁移** - 完成建表SQL脚本

### 🔄 待完成的工作

#### 1. Service实现类扩展

需要在 `MessageServiceImpl` 中实现以下方法：

```java
// 在 MessageServiceImpl.java 中添加这些实现

@Override
public Page<MessageSearchResponse> searchMessages(MessageSearchRequest request, Long currentUserId) {
    // 1. 验证用户权限（用户必须参与该纠纷）
    // 2. 构建搜索条件
    // 3. 执行搜索查询
    // 4. 计算相关性得分
    // 5. 生成文本高亮
    // 6. 保存搜索历史
    // 7. 返回分页结果
}

@Override
public List<MessageSearchSuggestion> getSearchSuggestions(Long disputeId, String keyword,
        String type, int limit, Long currentUserId) {
    // 1. 生成关键词建议
    // 2. 生成用户建议
    // 3. 生成日期建议
    // 4. 返回建议列表
}

@Override
public List<MessageSearchHistory> getSearchHistory(Long disputeId, int limit, Long currentUserId) {
    // 1. 查询用户搜索历史
    // 2. 按时间倒序排列
    // 3. 限制返回数量
    // 4. 返回历史记录
}

@Override
public void clearSearchHistory(Long disputeId, Long currentUserId) {
    // 1. 删除用户在该纠纷的搜索历史
    // 2. 更新统计数据
}

@Override
public MessageSearchStatistics getSearchStatistics(Long disputeId, Long currentUserId) {
    // 1. 查询总搜索次数
    // 2. 查询成功搜索次数
    // 3. 计算成功率
    // 4. 获取热门关键词
    // 5. 返回统计信息
}
```

#### 2. 核心算法实现

##### 相关性得分计算
```java
private double calculateRelevanceScore(Message message, String keyword) {
    double score = 0.0;
    String content = message.getContent().toLowerCase();
    String lowerKeyword = keyword.toLowerCase();

    // 完全匹配
    if (content.equals(lowerKeyword)) {
        score += 1.0;
    }
    // 开头匹配
    else if (content.startsWith(lowerKeyword)) {
        score += 0.8;
    }
    // 包含匹配
    else if (content.contains(lowerKeyword)) {
        score += 0.6;
    }

    // 长度因子
    double lengthFactor = Math.max(0, 1 - (content.length() - keyword.length()) / 100.0);
    score += lengthFactor * 0.4;

    return Math.min(score, 1.0);
}
```

##### 文本高亮处理
```java
private List<MessageSearchResponse.TextHighlight> highlightText(String text, String keyword) {
    List<MessageSearchResponse.TextHighlight> highlights = new ArrayList<>();

    // 实现高亮逻辑
    // 1. 找到匹配位置
    // 2. 分割文本
    // 3. 标记匹配部分
    // 4. 返回高亮列表

    return highlights;
}
```

#### 3. 权限验证

```java
private boolean hasAccessToDispute(Long disputeId, Long userId) {
    // 验证用户是否参与该纠纷
    return disputeRepository.isParticipant(disputeId, userId);
}
```

## 🗄️ 数据库操作

### 执行迁移脚本

```sql
-- 在数据库中执行以下脚本
source backend/src/main/resources/db/migration/V20251107__add_message_search_tables.sql
```

### 验证表创建

```sql
-- 检查表是否创建成功
SHOW TABLES LIKE 'message_search%';

-- 检查索引
SHOW INDEX FROM message_search_history;
SHOW INDEX FROM message_search_statistics;
```

## 🧪 测试策略

### 1. 单元测试

创建以下测试类：
- `MessageSearchControllerTest.java` - 控制器测试
- `MessageSearchServiceTest.java` - 服务层测试
- `MessageSearchHistoryRepositoryTest.java` - 数据层测试

### 2. 集成测试

```java
@SpringBootTest
@AutoConfigureTestDatabase
class MessageSearchIntegrationTest {

    @Test
    void testSearchMessages() {
        // 测试消息搜索功能
    }

    @Test
    void testSearchSuggestions() {
        // 测试搜索建议功能
    }

    @Test
    void testSearchHistory() {
        // 测试搜索历史功能
    }
}
```

### 3. API测试

```bash
# 测试搜索接口
curl -X POST http://localhost:8080/api/messages/search \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "disputeId": 1,
    "keyword": "测试",
    "page": 0,
    "size": 20
  }'

# 测试搜索建议接口
curl -X GET "http://localhost:8080/api/messages/search/suggestions?disputeId=1&keyword=测试" \
  -H "Authorization: Bearer ${TOKEN}"
```

## 🔧 配置更新

### application.yml 添加搜索配置

```yaml
# 搜索相关配置
search:
  # 搜索历史最大保存数量
  max-history-size: 50
  # 搜索建议最大数量
  max-suggestions: 10
  # 搜索缓存过期时间（秒）
  cache-expire: 300
  # 最大搜索结果数量
  max-results: 1000
```

## 📊 性能优化建议

### 1. 数据库优化
- 添加全文搜索索引
- 优化查询语句
- 合理使用分页

### 2. 缓存策略
```java
@Cacheable(value = "search:suggestions", key = "#disputeId + ':' + #keyword")
public List<MessageSearchSuggestion> getSearchSuggestions(...) {
    // 实现逻辑
}
```

### 3. 异步处理
```java
@Async
public CompletableFuture<Void> saveSearchHistoryAsync(...) {
    // 异步保存搜索历史
}
```

## 🔒 安全考虑

1. **权限验证**：确保用户只能搜索自己参与的纠纷
2. **数据脱敏**：处理敏感信息
3. **频率限制**：防止恶意搜索
4. **SQL注入防护**：使用参数化查询

## 📝 API文档更新

更新 Swagger 文档，添加新的搜索接口说明。

## 🚀 部署清单

- [ ] 执行数据库迁移
- [ ] 更新应用配置
- [ ] 部署新版本代码
- [ ] 验证接口功能
- [ ] 监控性能指标
- [ ] 更新API文档

---

## 📞 技术支持

如遇问题，请联系开发团队或查看相关文档。