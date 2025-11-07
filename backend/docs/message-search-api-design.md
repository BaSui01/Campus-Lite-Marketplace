# 消息搜索API设计方案

> **基于现有 MessageController 扩展**
> **优先级**: 高
> **作者**: BaSui 😎
> **日期**: 2025-11-07

## 📋 需求概述

为纠纷协商聊天功能添加消息搜索能力，支持：
- 关键词搜索
- 消息类型筛选
- 发送者筛选
- 时间范围筛选
- 搜索建议
- 搜索历史

## 🏗️ 架构设计

### 1. 扩展 MessageController

在现有的 `MessageController.java` 中添加以下接口：

```java
/**
 * 搜索协商消息
 * GET /api/messages/search
 */
@GetMapping("/search")
@PreAuthorize("hasRole('USER')")
@Operation(summary = "搜索协商消息", description = "在纠纷协商中搜索聊天消息")
public ApiResponse<Page<MessageSearchResponse>> searchMessages(
    @Parameter(description = "纠纷ID", required = true) @RequestParam Long disputeId,
    @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword,
    @Parameter(description = "消息类型筛选") @RequestParam(required = false) List<String> messageTypes,
    @Parameter(description = "发送者ID筛选") @RequestParam(required = false) List<Long> senderIds,
    @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
    @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
    @Parameter(description = "只搜索自己的消息") @RequestParam(defaultValue = "false") boolean ownMessagesOnly,
    @Parameter(description = "包含已撤回消息") @RequestParam(defaultValue = "false") boolean includeRecalled,
    @Parameter(description = "排序方式") @RequestParam(defaultValue = "relevance") String sortBy,
    @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDirection,
    @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
    @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size
) {
    // 实现逻辑
}

/**
 * 获取搜索建议
 * GET /api/messages/search/suggestions
 */
@GetMapping("/search/suggestions")
@PreAuthorize("hasRole('USER')")
@Operation(summary = "获取消息搜索建议", description = "根据输入提供智能搜索建议")
public ApiResponse<List<MessageSearchSuggestion>> getSearchSuggestions(
    @Parameter(description = "纠纷ID", required = true) @RequestParam Long disputeId,
    @Parameter(description = "关键词前缀") @RequestParam(required = false) String keyword,
    @Parameter(description = "建议类型") @RequestParam(required = false) String suggestionType,
    @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") int limit
) {
    // 实现逻辑
}

/**
 * 获取搜索历史
 * GET /api/messages/search/history
 */
@GetMapping("/search/history")
@PreAuthorize("hasRole('USER')")
@Operation(summary = "获取消息搜索历史", description = "获取用户的消息搜索历史记录")
public ApiResponse<List<MessageSearchHistory>> getSearchHistory(
    @Parameter(description = "纠纷ID", required = true) @RequestParam Long disputeId,
    @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "20") int limit
) {
    // 实现逻辑
}

/**
 * 清空搜索历史
 * DELETE /api/messages/search/history
 */
@DeleteMapping("/search/history")
@PreAuthorize("hasRole('USER')")
@Operation(summary = "清空消息搜索历史", description = "清空用户的消息搜索历史记录")
public ApiResponse<Void> clearSearchHistory(
    @Parameter(description = "纠纷ID", required = true) @RequestParam Long disputeId
) {
    // 实现逻辑
}

/**
 * 获取搜索统计
 * GET /api/messages/search/statistics
 */
@GetMapping("/search/statistics")
@PreAuthorize("hasRole('USER')")
@Operation(summary = "获取消息搜索统计", description = "获取搜索相关的统计信息")
public ApiResponse<MessageSearchStatistics> getSearchStatistics(
    @Parameter(description = "纠纷ID", required = true) @RequestParam Long disputeId
) {
    // 实现逻辑
}
```

### 2. 新增DTO类

#### MessageSearchRequest.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchRequest {
    @NotNull
    private Long disputeId;

    @NotBlank
    private String keyword;

    private List<String> messageTypes;
    private List<Long> senderIds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean ownMessagesOnly = false;
    private boolean includeRecalled = false;
    private String sortBy = "relevance";
    private String sortDirection = "desc";
    private int page = 0;
    private int size = 20;
}
```

#### MessageSearchResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchResponse {
    private Long messageId;
    private String content;
    private String messageType;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private LocalDateTime timestamp;
    private Double relevanceScore;
    private boolean isOwn;
    private boolean isRecalled;
    private List<TextHighlight> highlights;
    private List<String> matchedKeywords;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextHighlight {
    private String text;
    private boolean isMatch;
    private String keyword;
}
```

#### MessageSearchSuggestion.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchSuggestion {
    private String text;
    private String type; // keyword, user, date
    private String description;
    private String icon;
    private Long userId; // 如果是用户建议
}
```

#### MessageSearchHistory.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchHistory {
    private String id;
    private String keyword;
    private LocalDateTime searchedAt;
    private int resultCount;
    private Map<String, Object> filters;
}
```

#### MessageSearchStatistics.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchStatistics {
    private long totalSearches;
    private List<PopularKeyword> popularKeywords;
    private List<MessageSearchHistory> recentSearches;
    private double successRate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularKeyword {
        private String keyword;
        private long count;
    }
}
```

### 3. 扩展 MessageService

在 `MessageService` 接口中添加：

```java
/**
 * 搜索协商消息
 */
Page<MessageSearchResponse> searchMessages(MessageSearchRequest request, Long currentUserId);

/**
 * 获取搜索建议
 */
List<MessageSearchSuggestion> getSearchSuggestions(Long disputeId, String keyword, String type, int limit, Long currentUserId);

/**
 * 获取搜索历史
 */
List<MessageSearchHistory> getSearchHistory(Long disputeId, int limit, Long currentUserId);

/**
 * 清空搜索历史
 */
void clearSearchHistory(Long disputeId, Long currentUserId);

/**
 * 获取搜索统计
 */
MessageSearchStatistics getSearchStatistics(Long disputeId, Long currentUserId);
```

### 4. 数据库设计

#### 消息搜索历史表
```sql
CREATE TABLE message_search_history (
    id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    dispute_id BIGINT NOT NULL,
    keyword VARCHAR(200) NOT NULL,
    result_count INT DEFAULT 0,
    filters TEXT, -- JSON格式存储筛选条件
    searched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_dispute (user_id, dispute_id),
    INDEX idx_searched_at (searched_at),
    INDEX idx_keyword (keyword)
);
```

#### 消息搜索统计表
```sql
CREATE TABLE message_search_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    dispute_id BIGINT NOT NULL,
    search_date DATE NOT NULL,
    total_searches INT DEFAULT 0,
    successful_searches INT DEFAULT 0,
    popular_keywords TEXT, -- JSON格式存储热门关键词
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_dispute_date (user_id, dispute_id, search_date)
);
```

### 5. 实现要点

#### 5.1 搜索算法
```java
// 相关性得分计算
private double calculateRelevanceScore(Message message, String keyword) {
    double score = 0.0;

    // 完全匹配得分最高
    if (message.getContent().equalsIgnoreCase(keyword)) {
        score += 1.0;
    }
    // 开头匹配得分较高
    else if (message.getContent().toLowerCase().startsWith(keyword.toLowerCase())) {
        score += 0.8;
    }
    // 包含匹配得分
    else if (message.getContent().toLowerCase().contains(keyword.toLowerCase())) {
        score += 0.6;
    }

    // 长度因子（越短越精确）
    double lengthFactor = Math.max(0, 1 - (message.getContent().length() - keyword.length()) / 100.0);
    score += lengthFactor * 0.4;

    return Math.min(score, 1.0);
}
```

#### 5.2 高亮处理
```java
// 文本高亮
private List<TextHighlight> highlightText(String text, String keyword) {
    List<TextHighlight> highlights = new ArrayList<>();
    String lowerKeyword = keyword.toLowerCase();
    String lowerText = text.toLowerCase();

    int index = lowerText.indexOf(lowerKeyword);
    if (index == -1) {
        highlights.add(new TextHighlight(text, false, null));
        return highlights;
    }

    // 添加匹配前的文本
    if (index > 0) {
        highlights.add(new TextHighlight(text.substring(0, index), false, null));
    }

    // 添加匹配的文本
    highlights.add(new TextHighlight(
        text.substring(index, index + keyword.length()),
        true,
        keyword
    ));

    // 添加匹配后的文本
    if (index + keyword.length() < text.length()) {
        highlights.add(new TextHighlight(
            text.substring(index + keyword.length()),
            false,
            null
        ));
    }

    return highlights;
}
```

#### 5.3 搜索建议生成
```java
// 关键词建议
private List<MessageSearchSuggestion> generateKeywordSuggestions(String keyword, Long disputeId, Long userId) {
    // 1. 从搜索历史中提取相似关键词
    List<String> similarKeywords = searchHistoryRepository.findSimilarKeywords(disputeId, userId, keyword, 5);

    // 2. 从消息内容中提取高频词
    List<String> popularWords = messageRepository.findPopularWordsInDispute(disputeId, keyword, 5);

    // 合并并去重
    Set<String> suggestions = new LinkedHashSet<>();
    suggestions.addAll(similarKeywords);
    suggestions.addAll(popularWords);

    return suggestions.stream()
        .limit(10)
        .map(word -> MessageSearchSuggestion.builder()
            .text(word)
            .type("keyword")
            .description("关键词建议")
            .icon("🔍")
            .build())
        .collect(Collectors.toList());
}

// 用户建议
private List<MessageSearchSuggestion> generateUserSuggestions(String keyword, Long disputeId) {
    List<User> participants = disputeRepository.getParticipants(disputeId);

    return participants.stream()
        .filter(user -> user.getName().toLowerCase().contains(keyword.toLowerCase()))
        .map(user -> MessageSearchSuggestion.builder()
            .text(user.getName())
            .type("user")
            .description(getRoleDisplayName(user.getRole()))
            .icon(getRoleIcon(user.getRole()))
            .userId(user.getId())
            .build())
        .limit(5)
        .collect(Collectors.toList());
}
```

## 🚀 实施步骤

### Phase 1: 基础搜索功能
1. 创建DTO类
2. 扩展MessageController添加搜索接口
3. 实现基础搜索逻辑
4. 添加数据库表

### Phase 2: 高级功能
1. 实现搜索建议
2. 添加搜索历史管理
3. 实现搜索统计
4. 性能优化

### Phase 3: 优化和测试
1. 添加单元测试
2. 性能测试和优化
3. 文档完善
4. 集成测试

## 📊 性能考虑

1. **数据库索引**：
   - 消息内容全文索引
   - 发送者、时间戳复合索引
   - 纠纷ID索引

2. **缓存策略**：
   - 搜索结果缓存（Redis，5分钟）
   - 热门关键词缓存
   - 搜索建议缓存

3. **分页优化**：
   - 默认页大小20
   - 最大页大小100
   - 深度分页优化

## 🔒 安全考虑

1. **权限验证**：
   - 只能搜索自己参与的纠纷
   - 验证用户访问权限

2. **数据脱敏**：
   - 已撤回消息内容处理
   - 敏感信息过滤

3. **防刷机制**：
   - 搜索频率限制
   - 历史记录数量限制

## 📝 API文档示例

### 搜索消息
```http
GET /api/messages/search?disputeId=1&keyword=测试&messageTypes=text&ownMessagesOnly=false&page=0&size=20

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "messageId": 1001,
        "content": "这是一条测试消息",
        "messageType": "text",
        "senderId": 123,
        "senderName": "张三",
        "senderRole": "buyer",
        "timestamp": "2025-11-07T10:30:00",
        "relevanceScore": 0.95,
        "isOwn": false,
        "isRecalled": false,
        "highlights": [
          {
            "text": "这是一条",
            "isMatch": false
          },
          {
            "text": "测试",
            "isMatch": true,
            "keyword": "测试"
          },
          {
            "text": "消息",
            "isMatch": false
          }
        ],
        "matchedKeywords": ["测试"]
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

### 获取搜索建议
```http
GET /api/messages/search/suggestions?disputeId=1&keyword=测试&limit=5

Response:
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "text": "测试消息",
      "type": "keyword",
      "description": "关键词建议",
      "icon": "🔍"
    },
    {
      "text": "张三",
      "type": "user",
      "description": "买家",
      "icon": "👤",
      "userId": 123
    }
  ]
}
```

这个设计方案基于现有的架构，最小化开发成本，同时提供完整的搜索功能！🎯