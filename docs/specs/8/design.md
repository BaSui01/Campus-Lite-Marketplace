# Spec #8: 用户体验全面提升系统 - 设计文档

> **功能名称**: 用户体验全面提升系统 (User Experience Enhancement System)  
> **设计版本**: v1.0  
> **创建时间**: 2025-11-03  
> **作者**: BaSui 😎  
> **遵循原则**: SOLID | KISS | DRY | YAGNI | 复用优先

---

## 📋 目录

- [系统架构设计](#系统架构设计)
- [技术选型与复用分析](#技术选型与复用分析)
- [数据模型设计](#数据模型设计)
- [核心服务设计](#核心服务设计)
- [API接口设计](#api接口设计)
- [缓存策略设计](#缓存策略设计)
- [性能优化方案](#性能优化方案)
- [安全设计方案](#安全设计方案)
- [监控告警设计](#监控告警设计)

---

## 🏗️ 系统架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         前端层 (Frontend)                        │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐ │
│  │   管理端(Admin)   │  │   用户端(Portal)  │  │ WebSocket客户端 │ │
│  └──────────────────┘  └──────────────────┘  └───────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                  ↓
┌─────────────────────────────────────────────────────────────────┐
│                         接入层 (Gateway)                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐ │
│  │   JWT认证     │  │    限流防刷   │  │   WebSocket网关       │ │
│  └──────────────┘  └──────────────┘  └──────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                  ↓
┌─────────────────────────────────────────────────────────────────┐
│                         业务层 (Business)                        │
│ ┌────────────────────────────────────────────────────────────┐ │
│ │ 物流服务 | 聊天服务 | 信用服务 | 推荐服务 | 数据看板服务    │ │
│ │ 营销服务 | 自动化服务 | 社区服务 | 搜索服务 | 监控服务     │ │
│ └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                  ↓
┌─────────────────────────────────────────────────────────────────┐
│                         数据层 (Data)                            │
│ ┌──────────────┐  ┌──────────────┐  ┌───────────────────────┐ │
│ │  PostgreSQL   │  │    Redis      │  │  外部API(快递/支付)   │ │
│ │  (主数据库)    │  │  (缓存+锁)    │  │                       │ │
│ └──────────────┘  └──────────────┘  └───────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 分层架构设计

**遵循现有分层架构**（参考 `structure.md`）：

```
Controller → Service → Repository → Entity
    ↓          ↓          ↓
   DTO    ← Mapper →    Entity
```

**新增服务层组件**：

1. **物流服务层**
   - `LogisticsController` → `LogisticsService` → `LogisticsRepository`
   - 外部快递API集成：`LogisticsProviderFactory`

2. **聊天服务层**
   - `WebSocketController` → `ChatService` → `ChatRepository`
   - WebSocket增强：`UserPresenceService`

3. **信用服务层**
   - `CreditController` → `CreditService` → `CreditRepository`
   - 信用计算引擎：`CreditCalculationService`

4. **推荐服务层**
   - `RecommendationController` → `RecommendationService`
   - 推荐算法引擎：`RecommendationAlgorithmService`

5. **数据看板服务层**
   - `MerchantDashboardController` → `DashboardService` → `VisitorLogRepository`

6. **营销服务层**
   - `CampaignController` → `CampaignService` → `CampaignRepository`

7. **自动化服务层**
   - 定时任务：`OrderAutomationTask`

8. **社区服务层**
   - `CommunityController` → `CommunityService` → `TopicRepository`

---

## 🔧 技术选型与复用分析

### 复用现有组件清单

**严格遵循复用优先原则**（参考 `CLAUDE.md`）：

| 现有组件 | 复用场景 | 是否需要扩展 |
|---------|---------|-------------|
| **AuditLogService** | 所有操作审计日志记录 | ❌ 不需要扩展，直接复用 |
| **权限系统(PermissionCodes/RoleDefinition)** | 功能权限控制 | ✅ 需要新增25个权限编码 |
| **FileService** | 图片/视频上传和存储 | ❌ 不需要扩展，直接复用 |
| **NotificationService** | 消息通知推送 | ❌ 不需要扩展，直接复用 |
| **Order/Goods/User实体** | 关联核心业务数据 | ❌ 不需要扩展，直接复用 |
| **CacheService(Redis)** | 缓存管理 | ❌ 不需要扩展，直接复用 |
| **StatisticsService** | 基础统计服务 | ✅ 需要扩展商家数据看板功能 |
| **WebSocketSessionManager** | WebSocket基础框架 | ✅ 需要增强在线状态、消息已读功能 |
| **RecommendService** | 推荐服务 | ✅ 需要重构推荐算法 |
| **CouponService** | 优惠券服务 | ✅ 需要扩展营销活动功能 |
| **TaskService** | 定时任务服务 | ✅ 需要扩展订单自动化任务 |
| **SearchService** | 搜索服务 | ✅ 需要增强搜索功能 |
| **PostService/ReplyService** | 论坛功能 | ✅ 需要扩展社区广场功能 |

### 新增技术栈

| 技术 | 版本 | 用途 | 说明 |
|------|------|------|------|
| **快递100 API** | - | 物流查询 | 支持主流快递公司查询 |
| **Apache Mahout** | 14.1 | 推荐算法 | 协同过滤算法实现 |
| **Spring Task** | 内置 | 定时任务 | 订单自动化流程 |
| **Spring WebSocket** | 内置 | 实时通讯 | WebSocket增强 |
| **ECharts** | 5.4.3 | 数据可视化 | 商家数据看板（前端已有依赖） |

**复用决策原则**：
- ✅ **能直接复用的组件** - 不做任何修改
- ✅ **能扩展的组件** - 通过继承或组合扩展
- ✅ **能组合的组件** - 组合多个现有组件实现新功能
- ⚠️ **必须新建的组件** - 功能完全不存在，且无法复用

---

## 📊 数据模型设计

### 新增实体清单

根据**复用优先原则**，仅新增必要实体：

#### 1. Logistics 实体 - 物流信息

```java
@Entity
@Table(name = "t_logistics")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Logistics extends BaseEntity {
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;  // 关联订单ID
    
    @Column(name = "tracking_number", nullable = false, length = 50)
    private String trackingNumber;  // 快递单号
    
    @Enumerated(EnumType.STRING)
    @Column(name = "logistics_company", nullable = false, length = 20)
    private LogisticsCompany logisticsCompany;  // 快递公司（顺丰/中通/圆通等）
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LogisticsStatus status;  // 物流状态（已揽件/运输中/派送中/已签收）
    
    @Column(name = "current_location", length = 200)
    private String currentLocation;  // 当前位置
    
    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;  // 预计送达时间
    
    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;  // 实际送达时间
    
    @Column(name = "is_overtime")
    @Builder.Default
    private Boolean isOvertime = false;  // 是否超时
    
    @Column(name = "track_records", columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private List<LogisticsTrackRecord> trackRecords;  // 物流轨迹（JSON存储）
    
    @Column(name = "sync_count")
    @Builder.Default
    private Integer syncCount = 0;  // 同步次数
    
    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;  // 最后同步时间
}

// 物流轨迹记录（嵌套类，存储为JSON）
@Data
public class LogisticsTrackRecord {
    private LocalDateTime time;       // 时间
    private String location;          // 地点
    private String statusDesc;        // 状态描述
    private String operatorName;      // 操作员（可选）
}

// 物流公司枚举
public enum LogisticsCompany {
    SHUNFENG("顺丰速运"),
    ZHONGTONG("中通快递"),
    YUANTONG("圆通速递"),
    YUNDA("韵达快递"),
    EMS("邮政EMS"),
    JINGDONG("京东物流"),
    DEBANG("德邦物流"),
    SHENTONG("申通快递");
    
    private final String displayName;
}

// 物流状态枚举
public enum LogisticsStatus {
    PENDING("待发货"),
    PICKED_UP("已揽件"),
    IN_TRANSIT("运输中"),
    DELIVERING("派送中"),
    DELIVERED("已签收"),
    REJECTED("已拒签"),
    LOST("疑似丢失");
    
    private final String displayName;
}
```

**设计要点**：
- 使用 `JSONB` 存储物流轨迹，支持灵活查询
- 记录同步次数和时间，避免频繁调用外部API
- `isOvertime` 字段快速标记超时订单

---

#### 2. UserPresence 实体 - 用户在线状态

```java
@Entity
@Table(name = "t_user_presence")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserPresence extends BaseEntity {
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PresenceStatus status = PresenceStatus.OFFLINE;  // 在线状态
    
    @Column(name = "last_active_time", nullable = false)
    private LocalDateTime lastActiveTime;  // 最后活跃时间
    
    @Column(name = "device_type", length = 20)
    private String deviceType;  // 设备类型（PC/Mobile/Tablet）
    
    @Column(name = "device_id", length = 100)
    private String deviceId;  // 设备ID（用于多端同步）
    
    @Column(name = "websocket_session_id", length = 100)
    private String websocketSessionId;  // WebSocket会话ID
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;  // IP地址
    
    @Column(name = "online_duration")
    @Builder.Default
    private Long onlineDuration = 0L;  // 在线时长（秒）
}

// 在线状态枚举
public enum PresenceStatus {
    ONLINE("在线"),
    BUSY("忙碌"),
    AWAY("离开"),
    OFFLINE("离线");
    
    private final String displayName;
}
```

**设计要点**：
- 使用 `userId` 唯一索引，快速查询在线状态
- 记录 `websocketSessionId` 用于消息推送
- `onlineDuration` 统计用户活跃度

---

#### 3. UserCreditScore 实体 - 用户信用分

```java
@Entity
@Table(name = "t_user_credit_score")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserCreditScore extends BaseEntity {
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "credit_score", nullable = false)
    @Builder.Default
    private Integer creditScore = 100;  // 信用分（0-1000）
    
    @Enumerated(EnumType.STRING)
    @Column(name = "credit_level", nullable = false, length = 20)
    private CreditLevel creditLevel;  // 信用等级（青铜/白银/黄金/铂金/钻石）
    
    @Column(name = "good_review_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal goodReviewRate = BigDecimal.ZERO;  // 好评率（0-100%）
    
    @Column(name = "transaction_count")
    @Builder.Default
    private Integer transactionCount = 0;  // 成交量
    
    @Column(name = "refund_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal refundRate = BigDecimal.ZERO;  // 退款率（0-100%）
    
    @Column(name = "dispute_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal disputeRate = BigDecimal.ZERO;  // 纠纷率（0-100%）
    
    @Column(name = "active_days")
    @Builder.Default
    private Integer activeDays = 0;  // 活跃天数
    
    @Column(name = "violation_count")
    @Builder.Default
    private Integer violationCount = 0;  // 违规次数
    
    @Column(name = "credit_tags", columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private List<String> creditTags;  // 信用标签（诚信商家、及时发货等）
    
    @Column(name = "score_history", columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private List<CreditScoreHistory> scoreHistory;  // 信用分历史（近30天）
    
    @Column(name = "last_calculated_time")
    private LocalDateTime lastCalculatedTime;  // 最后计算时间
}

// 信用等级枚举
public enum CreditLevel {
    BRONZE("青铜", 0, 199),
    SILVER("白银", 200, 399),
    GOLD("黄金", 400, 599),
    PLATINUM("铂金", 600, 799),
    DIAMOND("钻石", 800, 1000);
    
    private final String displayName;
    private final int minScore;
    private final int maxScore;
    
    // 根据信用分获取等级
    public static CreditLevel fromScore(int score) {
        for (CreditLevel level : values()) {
            if (score >= level.minScore && score <= level.maxScore) {
                return level;
            }
        }
        return BRONZE;
    }
}

// 信用分历史记录（嵌套类）
@Data
public class CreditScoreHistory {
    private LocalDate date;      // 日期
    private Integer score;       // 当天信用分
    private String changeReason; // 变化原因
}
```

**设计要点**：
- 信用分范围 0-1000，5个等级区间清晰
- 多维度数据：好评率、成交量、退款率、纠纷率、活跃度
- 使用 `JSONB` 存储信用标签和历史记录，灵活扩展
- `lastCalculatedTime` 避免频繁重复计算

---

#### 4. UserPersona 实体 - 用户画像

```java
@Entity
@Table(name = "t_user_persona")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserPersona extends BaseEntity {
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "interest_tags", columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private Map<String, Double> interestTags;  // 兴趣标签（标签→权重）
    
    @Column(name = "price_preference", columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private PricePreference pricePreference;  // 价格偏好
    
    @Column(name = "active_time_slots", columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private List<String> activeTimeSlots;  // 活跃时段（如"08:00-12:00"）
    
    @Column(name = "campus_preference", length = 50)
    private String campusPreference;  // 校区偏好
    
    @Column(name = "favorite_categories", columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private List<String> favoriteCategories;  // 偏好分类
    
    @Column(name = "favorite_brands", columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private List<String> favoriteBrands;  // 偏好品牌
    
    @Column(name = "user_segment", length = 50)
    private String userSegment;  // 用户分群（高价值/活跃/沉睡/新用户）
    
    @Column(name = "last_updated_time")
    private LocalDateTime lastUpdatedTime;  // 最后更新时间
}

// 价格偏好（嵌套类）
@Data
public class PricePreference {
    private String preferredRange;  // 偏好区间（如"50-200"）
    private Integer avgSpending;    // 平均消费金额
    private Integer maxSpending;    // 最高消费金额
}
```

**设计要点**：
- 使用 Map 存储兴趣标签及权重（动态调整）
- `userSegment` 字段快速查询不同用户群体
- 数据每天更新一次，减少计算开销

---

#### 5. UserBehaviorLog 实体 - 用户行为日志

```java
@Entity
@Table(name = "t_user_behavior_log", indexes = {
    @Index(name = "idx_user_behavior", columnList = "user_id,behavior_type,created_at"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserBehaviorLog extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "behavior_type", nullable = false, length = 20)
    private BehaviorType behaviorType;  // 行为类型
    
    @Column(name = "target_type", length = 20)
    private String targetType;  // 目标类型（Goods/Post/User）
    
    @Column(name = "target_id")
    private Long targetId;  // 目标ID
    
    @Column(name = "source", length = 50)
    private String source;  // 来源（搜索/推荐/直接访问）
    
    @Column(name = "duration")
    private Integer duration;  // 浏览时长（秒）
    
    @Column(name = "extra_data", columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private Map<String, Object> extraData;  // 额外数据（如搜索关键词）
    
    @Column(name = "session_id", length = 100)
    private String sessionId;  // 会话ID
    
    @Column(name = "device_type", length = 20)
    private String deviceType;  // 设备类型
}

// 行为类型枚举
public enum BehaviorType {
    VIEW("浏览"),
    SEARCH("搜索"),
    FAVORITE("收藏"),
    PURCHASE("购买"),
    CLICK("点击"),
    SHARE("分享"),
    COMMENT("评论"),
    LIKE("点赞");
    
    private final String displayName;
}
```

**设计要点**：
- 使用联合索引 `(user_id, behavior_type, created_at)` 加速查询
- `extraData` 存储扩展数据，避免频繁修改表结构
- 数据保留90天，超期归档到冷存储

---

#### 6. MerchantDashboard 实体 - 商家数据看板

```java
@Entity
@Table(name = "t_merchant_dashboard")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MerchantDashboard extends BaseEntity {
    
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;
    
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;  // 统计日期
    
    @Column(name = "sales_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal salesAmount = BigDecimal.ZERO;  // 销售额
    
    @Column(name = "order_count")
    @Builder.Default
    private Integer orderCount = 0;  // 订单数
    
    @Column(name = "visitor_count")
    @Builder.Default
    private Integer visitorCount = 0;  // 访客数
    
    @Column(name = "new_visitor_count")
    @Builder.Default
    private Integer newVisitorCount = 0;  // 新访客数
    
    @Column(name = "page_view_count")
    @Builder.Default
    private Integer pageViewCount = 0;  // 浏览量
    
    @Column(name = "conversion_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal conversionRate = BigDecimal.ZERO;  // 转化率
    
    @Column(name = "visitor_sources", columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private Map<String, Integer> visitorSources;  // 访客来源（搜索/推荐/直接访问→数量）
    
    @Column(name = "top_selling_goods", columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private List<Long> topSellingGoods;  // 热销商品ID列表
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
}
```

**设计要点**：
- 按日期统计，方便生成趋势图
- 使用联合唯一索引 `(merchant_id, stat_date)` 避免重复统计
- 数据每天凌晨生成一次（定时任务）

---

#### 7. VisitorLog 实体 - 访客日志

```java
@Entity
@Table(name = "t_visitor_log", indexes = {
    @Index(name = "idx_merchant_visitor", columnList = "merchant_id,created_at"),
    @Index(name = "idx_goods_visitor", columnList = "goods_id,created_at")
})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VisitorLog extends BaseEntity {
    
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;  // 商家ID
    
    @Column(name = "goods_id")
    private Long goodsId;  // 商品ID（可选）
    
    @Column(name = "visitor_id")
    private Long visitorId;  // 访客ID（脱敏，使用哈希ID）
    
    @Column(name = "source", length = 50)
    private String source;  // 来源（搜索/推荐/直接访问）
    
    @Column(name = "duration")
    private Integer duration;  // 停留时长（秒）
    
    @Column(name = "device_type", length = 20)
    private String deviceType;  // 设备类型
    
    @Column(name = "is_new_visitor")
    @Builder.Default
    private Boolean isNewVisitor = false;  // 是否新访客
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
}
```

**设计要点**：
- 使用哈希ID脱敏访客信息，保护隐私
- 索引优化：按商家ID和时间范围快速统计
- 数据保留30天，超期自动清理

---

#### 8. MarketingCampaign 实体 - 营销活动

```java
@Entity
@Table(name = "t_marketing_campaign")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MarketingCampaign extends BaseEntity {
    
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;
    
    @Column(name = "campaign_name", nullable = false, length = 100)
    private String campaignName;  // 活动名称
    
    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_type", nullable = false, length = 20)
    private CampaignType campaignType;  // 活动类型
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;  // 开始时间
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;  // 结束时间
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.PENDING;  // 活动状态
    
    @Column(name = "discount_config", columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private DiscountConfig discountConfig;  // 折扣配置
    
    @Column(name = "goods_ids", columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private List<Long> goodsIds;  // 参与商品ID列表
    
    @Column(name = "stock_limit")
    private Integer stockLimit;  // 库存限制
    
    @Column(name = "stock_remaining")
    private Integer stockRemaining;  // 剩余库存
    
    @Column(name = "participation_count")
    @Builder.Default
    private Integer participationCount = 0;  // 参与人数
    
    @Column(name = "sales_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal salesAmount = BigDecimal.ZERO;  // 活动销售额
    
    @Column(name = "created_by")
    private Long createdBy;  // 创建人ID
}

// 活动类型枚举
public enum CampaignType {
    DISCOUNT("限时折扣"),
    FULL_REDUCTION("满减活动"),
    FLASH_SALE("秒杀活动"),
    GROUP_BUYING("拼团活动"),
    NEW_USER("新人专享");
    
    private final String displayName;
}

// 活动状态枚举
public enum CampaignStatus {
    PENDING("待审核"),
    APPROVED("已通过"),
    RUNNING("进行中"),
    PAUSED("已暂停"),
    ENDED("已结束"),
    REJECTED("已拒绝");
    
    private final String displayName;
}

// 折扣配置（嵌套类）
@Data
public class DiscountConfig {
    private String discountType;  // 折扣类型（PERCENTAGE/FIXED_AMOUNT/FULL_REDUCTION）
    private BigDecimal discountValue;  // 折扣值（8折=0.8 / 满减金额）
    private BigDecimal threshold;  // 满减阈值（如满100）
}
```

**设计要点**：
- 使用 `JSONB` 存储折扣配置，支持多种活动类型
- `stockRemaining` 实时扣减，防止超卖
- 状态机管理活动生命周期（待审核→进行中→已结束）

---

#### 9. Topic 实体 - 社区话题

```java
@Entity
@Table(name = "t_topic")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Topic extends BaseEntity {
    
    @Column(name = "topic_name", nullable = false, unique = true, length = 50)
    private String topicName;  // 话题名称（如"#数码评测"）
    
    @Column(name = "topic_desc", length = 200)
    private String topicDesc;  // 话题描述
    
    @Column(name = "cover_image", length = 500)
    private String coverImage;  // 封面图片
    
    @Column(name = "post_count")
    @Builder.Default
    private Integer postCount = 0;  // 帖子数量
    
    @Column(name = "follower_count")
    @Builder.Default
    private Integer followerCount = 0;  // 关注人数
    
    @Column(name = "heat_score")
    @Builder.Default
    private Integer heatScore = 0;  // 热度分数（综合帖子数、参与人数、讨论量计算）
    
    @Column(name = "is_hot")
    @Builder.Default
    private Boolean isHot = false;  // 是否热门话题
    
    @Column(name = "created_by")
    private Long createdBy;  // 创建人ID
}
```

**设计要点**：
- `topicName` 唯一索引，防止重复创建
- `heatScore` 热度分数，用于热门话题排序
- `isHot` 快速筛选热门话题

---

#### 10. UserFeed 实体 - 用户动态流

```java
@Entity
@Table(name = "t_user_feed", indexes = {
    @Index(name = "idx_user_created", columnList = "user_id,created_at")
})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserFeed extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private Long userId;  // 接收动态的用户ID
    
    @Column(name = "actor_id", nullable = false)
    private Long actorId;  // 产生动态的用户ID
    
    @Enumerated(EnumType.STRING)
    @Column(name = "feed_type", nullable = false, length = 20)
    private FeedType feedType;  // 动态类型
    
    @Column(name = "target_type", length = 20)
    private String targetType;  // 目标类型（Post/Goods/Review）
    
    @Column(name = "target_id")
    private Long targetId;  // 目标ID
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;  // 动态内容摘要
    
    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;  // 是否已读
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
}

// 动态类型枚举
public enum FeedType {
    POST_CREATED("发布了帖子"),
    GOODS_PUBLISHED("发布了商品"),
    REVIEW_POSTED("发表了评价"),
    GOODS_FAVORITED("收藏了商品");
    
    private final String displayName;
}
```

**设计要点**：
- 使用联合索引 `(user_id, created_at)` 快速查询用户动态流
- `isRead` 字段标记已读状态
- 数据保留30天，超期自动清理

---

### 数据库迁移脚本

**Flyway迁移脚本**（遵循现有命名规范）：

```sql
-- V100__create_logistics_table.sql
CREATE TABLE t_logistics (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    tracking_number VARCHAR(50) NOT NULL,
    logistics_company VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_location VARCHAR(200),
    estimated_delivery_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    is_overtime BOOLEAN DEFAULT FALSE,
    track_records JSONB,
    sync_count INTEGER DEFAULT 0,
    last_sync_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_logistics_order ON t_logistics(order_id);
CREATE INDEX idx_logistics_tracking ON t_logistics(tracking_number);

-- V101__create_user_presence_table.sql
CREATE TABLE t_user_presence (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    last_active_time TIMESTAMP NOT NULL,
    device_type VARCHAR(20),
    device_id VARCHAR(100),
    websocket_session_id VARCHAR(100),
    ip_address VARCHAR(50),
    online_duration BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_presence_user ON t_user_presence(user_id);
CREATE INDEX idx_user_presence_status ON t_user_presence(status);

-- ... 其他迁移脚本 V102-V111
```

---

## 🎯 核心服务设计

### 1. LogisticsService - 物流服务

**接口定义**：

```java
public interface LogisticsService {
    
    /**
     * 查询物流信息
     */
    LogisticsDTO queryLogistics(Long orderId);
    
    /**
     * 同步物流信息（从快递API）
     */
    void syncLogistics(Long logisticsId);
    
    /**
     * 批量同步物流信息（定时任务）
     */
    void batchSyncLogistics();
    
    /**
     * 检测物流超时
     */
    List<Long> detectOvertimeLogistics();
    
    /**
     * 获取物流统计（管理员）
     */
    LogisticsStatisticsDTO getLogisticsStatistics(LocalDate startDate, LocalDate endDate);
}
```

**服务实现**：

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class LogisticsServiceImpl implements LogisticsService {
    
    private final LogisticsRepository logisticsRepository;
    private final LogisticsProviderFactory providerFactory;
    private final CacheService cacheService;
    private final NotificationService notificationService;
    
    @Override
    @Cacheable(value = "logistics", key = "#orderId")
    public LogisticsDTO queryLogistics(Long orderId) {
        Logistics logistics = logisticsRepository.findByOrderId(orderId)
            .orElseThrow(() -> new EntityNotFoundException("物流信息不存在"));
        
        // 判断是否需要同步（距离上次同步超过2小时）
        if (shouldSync(logistics.getLastSyncTime())) {
            syncLogistics(logistics.getId());
            logistics = logisticsRepository.findById(logistics.getId()).orElseThrow();
        }
        
        return LogisticsMapper.INSTANCE.toDTO(logistics);
    }
    
    @Override
    @Transactional
    public void syncLogistics(Long logisticsId) {
        Logistics logistics = logisticsRepository.findById(logisticsId)
            .orElseThrow(() -> new EntityNotFoundException("物流信息不存在"));
        
        try {
            // 调用快递公司API查询物流
            LogisticsProvider provider = providerFactory.getProvider(logistics.getLogisticsCompany());
            LogisticsTrackResult result = provider.queryTrack(logistics.getTrackingNumber());
            
            // 更新物流信息
            logistics.setStatus(result.getStatus());
            logistics.setCurrentLocation(result.getCurrentLocation());
            logistics.setTrackRecords(result.getTrackRecords());
            logistics.setSyncCount(logistics.getSyncCount() + 1);
            logistics.setLastSyncTime(LocalDateTime.now());
            
            // 检测超时
            if (result.getStatus() == LogisticsStatus.DELIVERING 
                && LocalDateTime.now().isAfter(logistics.getEstimatedDeliveryTime())) {
                logistics.setIsOvertime(true);
                // 发送超时通知
                notificationService.sendOvertimeNotification(logistics.getOrderId());
            }
            
            logisticsRepository.save(logistics);
            
            // 更新缓存
            cacheService.delete("logistics::" + logistics.getOrderId());
            
        } catch (Exception e) {
            log.error("同步物流信息失败: logisticsId={}", logisticsId, e);
        }
    }
    
    @Override
    @Scheduled(fixedRate = 7200000) // 每2小时执行一次
    public void batchSyncLogistics() {
        // 查询最近7天且状态未完成的物流
        List<Logistics> pendingLogistics = logisticsRepository.findPendingLogistics(
            LocalDateTime.now().minusDays(7),
            List.of(LogisticsStatus.IN_TRANSIT, LogisticsStatus.DELIVERING)
        );
        
        pendingLogistics.forEach(logistics -> syncLogistics(logistics.getId()));
        log.info("批量同步物流完成，共{}条", pendingLogistics.size());
    }
    
    private boolean shouldSync(LocalDateTime lastSyncTime) {
        return lastSyncTime == null || 
               lastSyncTime.plusHours(2).isBefore(LocalDateTime.now());
    }
}
```

**快递公司API集成**（策略模式）：

```java
// 快递服务提供商接口
public interface LogisticsProvider {
    LogisticsTrackResult queryTrack(String trackingNumber);
    LogisticsCompany getCompany();
}

// 快递100 API实现（示例）
@Component
public class Kuaidi100Provider implements LogisticsProvider {
    
    @Value("${logistics.kuaidi100.api-key}")
    private String apiKey;
    
    @Override
    public LogisticsTrackResult queryTrack(String trackingNumber) {
        // 调用快递100 API
        String url = "https://poll.kuaidi100.com/poll/query.do";
        // ... API调用逻辑
    }
    
    @Override
    public LogisticsCompany getCompany() {
        return LogisticsCompany.ZHONGTONG;
    }
}

// 快递服务工厂
@Component
@RequiredArgsConstructor
public class LogisticsProviderFactory {
    
    private final List<LogisticsProvider> providers;
    
    public LogisticsProvider getProvider(LogisticsCompany company) {
        return providers.stream()
            .filter(p -> p.getCompany() == company)
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException("不支持的快递公司"));
    }
}
```

---

### 2. ChatService - 聊天服务（WebSocket增强）

**接口定义**：

```java
public interface ChatService {
    
    /**
     * 更新用户在线状态
     */
    void updateUserPresence(Long userId, PresenceStatus status, String sessionId);
    
    /**
     * 查询用户在线状态
     */
    UserPresenceDTO getUserPresence(Long userId);
    
    /**
     * 获取在线用户列表
     */
    List<UserPresenceDTO> getOnlineUsers();
    
    /**
     * 标记消息已读
     */
    void markMessageAsRead(Long messageId, Long userId);
    
    /**
     * 发送输入状态
     */
    void sendTypingIndicator(Long fromUserId, Long toUserId);
    
    /**
     * 撤回消息
     */
    void recallMessage(Long messageId, Long userId);
    
    /**
     * 获取未读消息数
     */
    Integer getUnreadCount(Long userId);
}
```

**服务实现**：

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    
    private final UserPresenceRepository userPresenceRepository;
    private final MessageRepository messageRepository;
    private final WebSocketSessionManager sessionManager;
    private final CacheService cacheService;
    
    @Override
    @Transactional
    public void updateUserPresence(Long userId, PresenceStatus status, String sessionId) {
        UserPresence presence = userPresenceRepository.findByUserId(userId)
            .orElse(UserPresence.builder()
                .userId(userId)
                .status(status)
                .build());
        
        presence.setStatus(status);
        presence.setLastActiveTime(LocalDateTime.now());
        presence.setWebsocketSessionId(sessionId);
        
        // 更新在线时长
        if (status == PresenceStatus.ONLINE) {
            presence.setOnlineDuration(presence.getOnlineDuration() + 60); // 每分钟累加
        }
        
        userPresenceRepository.save(presence);
        
        // 更新缓存（在线状态缓存5分钟）
        cacheService.set("user:presence:" + userId, presence, Duration.ofMinutes(5));
        
        // 推送在线状态变更给好友
        pushPresenceChange(userId, status);
    }
    
    @Override
    public UserPresenceDTO getUserPresence(Long userId) {
        // 优先从缓存读取
        UserPresence presence = (UserPresence) cacheService.get("user:presence:" + userId);
        if (presence == null) {
            presence = userPresenceRepository.findByUserId(userId)
                .orElse(UserPresence.builder()
                    .userId(userId)
                    .status(PresenceStatus.OFFLINE)
                    .build());
            cacheService.set("user:presence:" + userId, presence, Duration.ofMinutes(5));
        }
        
        return UserPresenceMapper.INSTANCE.toDTO(presence);
    }
    
    @Override
    @Transactional
    public void markMessageAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException("消息不存在"));
        
        if (!message.getReceiverId().equals(userId)) {
            throw new UnauthorizedException("无权标记该消息为已读");
        }
        
        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());
        messageRepository.save(message);
        
        // 推送已读回执给发送方
        pushReadReceipt(message.getSenderId(), messageId);
    }
    
    @Override
    public void recallMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException("消息不存在"));
        
        // 校验权限（只能撤回自己的消息）
        if (!message.getSenderId().equals(userId)) {
            throw new UnauthorizedException("无权撤回该消息");
        }
        
        // 校验时效（5分钟内）
        if (message.getCreatedAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
            throw new BusinessException("消息超过5分钟，无法撤回");
        }
        
        message.setContent("[消息已撤回]");
        message.setIsRecalled(true);
        messageRepository.save(message);
        
        // 推送撤回通知
        pushRecallNotification(message.getReceiverId(), messageId);
    }
    
    private void pushPresenceChange(Long userId, PresenceStatus status) {
        // 通过WebSocket推送在线状态变更
        sessionManager.sendToUser(userId, new PresenceChangeMessage(userId, status));
    }
    
    private void pushReadReceipt(Long userId, Long messageId) {
        sessionManager.sendToUser(userId, new ReadReceiptMessage(messageId));
    }
    
    private void pushRecallNotification(Long userId, Long messageId) {
        sessionManager.sendToUser(userId, new RecallNotificationMessage(messageId));
    }
}
```

---

### 3. CreditCalculationService - 信用计算服务

**接口定义**：

```java
public interface CreditCalculationService {
    
    /**
     * 计算用户信用分
     */
    Integer calculateCreditScore(Long userId);
    
    /**
     * 批量更新信用分（定时任务）
     */
    void batchUpdateCreditScores();
    
    /**
     * 更新信用标签
     */
    void updateCreditTags(Long userId);
    
    /**
     * 记录信用分变化
     */
    void recordCreditChange(Long userId, Integer oldScore, Integer newScore, String reason);
}
```

**服务实现**：

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditCalculationServiceImpl implements CreditCalculationService {
    
    private final UserCreditScoreRepository creditRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final DisputeRepository disputeRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public Integer calculateCreditScore(Long userId) {
        // 获取用户数据
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        // 计算各维度数据
        BigDecimal goodReviewRate = calculateGoodReviewRate(userId);  // 好评率
        Integer transactionCount = orderRepository.countByUserIdAndStatus(userId, OrderStatus.COMPLETED);  // 成交量
        BigDecimal refundRate = calculateRefundRate(userId);  // 退款率
        BigDecimal disputeRate = calculateDisputeRate(userId);  // 纠纷率
        Integer activeDays = calculateActiveDays(user.getCreatedAt());  // 活跃天数
        Integer violationCount = countViolations(userId);  // 违规次数
        
        // 信用分计算公式
        double baseScore = 100.0;  // 基础分
        double reviewScore = goodReviewRate.doubleValue() * 300;  // 好评率权重30%
        double transactionScore = Math.min(transactionCount * 2, 200);  // 成交量权重20%
        double refundPenalty = refundRate.doubleValue() * 250;  // 退款率扣分25%
        double disputePenalty = disputeRate.doubleValue() * 250;  // 纠纷率扣分25%
        double activeBonus = Math.min(activeDays / 10.0, 100);  // 活跃度奖励10%
        double violationPenalty = violationCount * 50;  // 违规扣分
        
        int finalScore = (int) Math.max(0, Math.min(1000, 
            baseScore + reviewScore + transactionScore + activeBonus - refundPenalty - disputePenalty - violationPenalty
        ));
        
        // 更新或创建信用分记录
        UserCreditScore creditScore = creditRepository.findByUserId(userId)
            .orElse(UserCreditScore.builder()
                .userId(userId)
                .build());
        
        int oldScore = creditScore.getCreditScore();
        creditScore.setCreditScore(finalScore);
        creditScore.setCreditLevel(CreditLevel.fromScore(finalScore));
        creditScore.setGoodReviewRate(goodReviewRate);
        creditScore.setTransactionCount(transactionCount);
        creditScore.setRefundRate(refundRate);
        creditScore.setDisputeRate(disputeRate);
        creditScore.setActiveDays(activeDays);
        creditScore.setViolationCount(violationCount);
        creditScore.setLastCalculatedTime(LocalDateTime.now());
        
        creditRepository.save(creditScore);
        
        // 记录信用分历史
        if (oldScore != finalScore) {
            recordCreditChange(userId, oldScore, finalScore, "定时更新");
        }
        
        // 更新信用标签
        updateCreditTags(userId);
        
        return finalScore;
    }
    
    @Override
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void batchUpdateCreditScores() {
        List<User> users = userRepository.findAll();
        users.forEach(user -> {
            try {
                calculateCreditScore(user.getId());
            } catch (Exception e) {
                log.error("计算用户信用分失败: userId={}", user.getId(), e);
            }
        });
        log.info("批量更新信用分完成，共{}个用户", users.size());
    }
    
    @Override
    @Transactional
    public void updateCreditTags(Long userId) {
        UserCreditScore creditScore = creditRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("信用记录不存在"));
        
        List<String> tags = new ArrayList<>();
        
        // 诚信商家：0退款0纠纷
        if (creditScore.getRefundRate().compareTo(BigDecimal.ZERO) == 0 
            && creditScore.getDisputeRate().compareTo(BigDecimal.ZERO) == 0) {
            tags.add("诚信商家");
        }
        
        // 及时发货：平均24小时内发货
        if (isTimelyShipper(userId)) {
            tags.add("及时发货");
        }
        
        // 沟通顺畅：平均回复时间<30分钟
        if (isResponsive(userId)) {
            tags.add("沟通顺畅");
        }
        
        // 好评如潮：好评率>95%
        if (creditScore.getGoodReviewRate().compareTo(new BigDecimal("95")) > 0) {
            tags.add("好评如潮");
        }
        
        // 金牌卖家：成交量>50单/月
        if (isGoldSeller(userId)) {
            tags.add("金牌卖家");
        }
        
        creditScore.setCreditTags(tags);
        creditRepository.save(creditScore);
    }
    
    private BigDecimal calculateGoodReviewRate(Long userId) {
        long totalReviews = reviewRepository.countByRevieweeId(userId);
        if (totalReviews == 0) return BigDecimal.ZERO;
        
        long goodReviews = reviewRepository.countByRevieweeIdAndRatingGreaterThanEqual(userId, 4);
        return BigDecimal.valueOf(goodReviews * 100.0 / totalReviews);
    }
    
    private BigDecimal calculateRefundRate(Long userId) {
        long totalOrders = orderRepository.countBySellerIdAndStatusIn(
            userId, List.of(OrderStatus.COMPLETED, OrderStatus.REFUNDED));
        if (totalOrders == 0) return BigDecimal.ZERO;
        
        long refundedOrders = orderRepository.countBySellerIdAndStatus(userId, OrderStatus.REFUNDED);
        return BigDecimal.valueOf(refundedOrders * 100.0 / totalOrders);
    }
    
    private BigDecimal calculateDisputeRate(Long userId) {
        long totalOrders = orderRepository.countBySellerId(userId);
        if (totalOrders == 0) return BigDecimal.ZERO;
        
        long disputedOrders = disputeRepository.countByDefendantId(userId);
        return BigDecimal.valueOf(disputedOrders * 100.0 / totalOrders);
    }
    
    private Integer calculateActiveDays(LocalDateTime registeredAt) {
        return (int) ChronoUnit.DAYS.between(registeredAt.toLocalDate(), LocalDate.now());
    }
    
    private Integer countViolations(Long userId) {
        // 统计违规次数（从审计日志中查询）
        return 0; // TODO: 实现违规统计逻辑
    }
    
    private boolean isTimelyShipper(Long userId) {
        // 查询最近30天订单平均发货时长
        return true; // TODO: 实现及时发货判断
    }
    
    private boolean isResponsive(Long userId) {
        // 查询平均消息回复时间
        return true; // TODO: 实现响应速度判断
    }
    
    private boolean isGoldSeller(Long userId) {
        // 查询最近30天成交量
        long recentOrders = orderRepository.countBySellerIdAndCreatedAtAfter(
            userId, LocalDateTime.now().minusDays(30));
        return recentOrders > 50;
    }
}
```

---

由于篇幅限制，design.md 文档还有其他章节（推荐算法服务、API设计、缓存策略、性能优化等）。现在让我继续完成 design.md 的剩余内容...

让我继续完成 design.md：

```markdown
(续上文)

### 4. RecommendationAlgorithmService - 推荐算法服务

**接口定义**：

```java
public interface RecommendationAlgorithmService {
    
    /**
     * 个性化推荐（协同过滤）
     */
    List<GoodsDTO> getPersonalRecommendations(Long userId, int limit);
    
    /**
     * 相似商品推荐（基于内容）
     */
    List<GoodsDTO> getSimilarGoods(Long goodsId, int limit);
    
    /**
     * 热度推荐
     */
    List<GoodsDTO> getPopularGoods(int limit);
    
    /**
     * 更新用户行为追踪
     */
    void updateBehaviorTrace(Long userId, BehaviorType type, Long targetId);
    
    /**
     * 预计算推荐结果（定时任务）
     */
    void precomputeRecommendations();
}
```

**关键算法逻辑**：

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationAlgorithmServiceImpl implements RecommendationAlgorithmService {
    
    private final UserPersonaRepository personaRepository;
    private final UserBehaviorLogRepository behaviorLogRepository;
    private final GoodsRepository goodsRepository;
    private final CacheService cacheService;
    
    @Override
    @Cacheable(value = "recommendations:personal", key = "#userId")
    public List<GoodsDTO> getPersonalRecommendations(Long userId, int limit) {
        // 1. 获取用户画像
        UserPersona persona = personaRepository.findByUserId(userId).orElse(null);
        if (persona == null) {
            return getPopularGoods(limit); // 降级到热度推荐
        }
        
        // 2. 协同过滤：找相似用户
        List<Long> similarUserIds = findSimilarUsers(userId, 50);
        
        // 3. 推荐相似用户购买但当前用户未接触的商品
        List<Goods> candidateGoods = goodsRepository.findByUserIdsExcluding(similarUserIds, userId);
        
        // 4. 结合用户偏好排序
        return candidateGoods.stream()
            .sorted((g1, g2) -> compareByPreference(g1, g2, persona))
            .limit(limit)
            .map(GoodsMapper.INSTANCE::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "recommendations:similar", key = "#goodsId")
    public List<GoodsDTO> getSimilarGoods(Long goodsId, int limit) {
        Goods targetGoods = goodsRepository.findById(goodsId)
            .orElseThrow(() -> new EntityNotFoundException("商品不存在"));
        
        // 基于内容的推荐：相同类别+相似价格+相同标签
        return goodsRepository.findSimilar(
            targetGoods.getCategoryId(),
            targetGoods.getPrice().multiply(BigDecimal.valueOf(0.8)),
            targetGoods.getPrice().multiply(BigDecimal.valueOf(1.2)),
            targetGoods.getTags()
        ).stream()
            .filter(g -> !g.getId().equals(goodsId))
            .limit(limit)
            .map(GoodsMapper.INSTANCE::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "recommendations:popular", key = "#limit")
    public List<GoodsDTO> getPopularGoods(int limit) {
        // 热度计算：浏览量30% + 成交量40% + 收藏量30%
        return goodsRepository.findTopByHeatScore(limit).stream()
            .map(GoodsMapper.INSTANCE::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Async
    public void updateBehaviorTrace(Long userId, BehaviorType type, Long targetId) {
        UserBehaviorLog log = UserBehaviorLog.builder()
            .userId(userId)
            .behaviorType(type)
            .targetId(targetId)
            .build();
        behaviorLogRepository.save(log);
        
        // 异步更新用户画像
        updateUserPersona(userId);
    }
    
    @Override
    @Scheduled(cron = "0 30 */2 * * ?") // 每2小时的30分执行
    public void precomputeRecommendations() {
        // 预计算活跃用户的推荐结果，写入缓存
        List<Long> activeUserIds = userRepository.findActiveUsers(LocalDateTime.now().minusDays(7));
        activeUserIds.forEach(userId -> {
            try {
                List<GoodsDTO> recommendations = getPersonalRecommendations(userId, 20);
                cacheService.set("recommendations:personal::" + userId, recommendations, Duration.ofHours(2));
            } catch (Exception e) {
                log.error("预计算推荐失败: userId={}", userId, e);
            }
        });
        log.info("预计算推荐完成，共{}个用户", activeUserIds.size());
    }
    
    /**
     * 查找相似用户（基于用户行为的余弦相似度）
     */
    private List<Long> findSimilarUsers(Long userId, int limit) {
        // 获取用户的行为向量
        List<UserBehaviorLog> userBehaviors = behaviorLogRepository.findByUserId(userId);
        
        // 计算与其他用户的相似度
        // TODO: 使用Apache Mahout实现协同过滤
        
        return List.of(); // 临时返回空列表
    }
    
    /**
     * 根据用户偏好比较商品
     */
    private int compareByPreference(Goods g1, Goods g2, UserPersona persona) {
        // 计算商品与用户偏好的匹配度
        double score1 = calculateMatchScore(g1, persona);
        double score2 = calculateMatchScore(g2, persona);
        return Double.compare(score2, score1);
    }
    
    private double calculateMatchScore(Goods goods, UserPersona persona) {
        double score = 0.0;
        
        // 类别匹配
        if (persona.getFavoriteCategories().contains(goods.getCategoryId().toString())) {
            score += 40;
        }
        
        // 价格匹配
        if (isInPriceRange(goods.getPrice(), persona.getPricePreference())) {
            score += 30;
        }
        
        // 品牌匹配
        if (persona.getFavoriteBrands().contains(goods.getBrand())) {
            score += 20;
        }
        
        // 热度加成
        score += Math.min(goods.getViewCount() / 100.0, 10);
        
        return score;
    }
    
    private boolean isInPriceRange(BigDecimal price, PricePreference preference) {
        // 判断商品价格是否在用户偏好区间
        return true; // TODO: 实现价格区间判断
    }
    
    private void updateUserPersona(Long userId) {
        // 异步更新用户画像
        // TODO: 分析用户行为日志，更新兴趣标签和偏好
    }
}
```

---

## 🌐 API接口设计

### RESTful API 规范

**遵循现有API设计规范**（参考 `structure.md`）：

- **统一响应格式**：`ApiResponse<T>`
- **统一异常处理**：`@RestControllerAdvice`
- **统一权限控制**：`@PreAuthorize`
- **统一参数校验**：`@Valid`

### 关键接口列表

#### 1. 物流跟踪接口

```java
@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
@Tag(name = "物流跟踪", description = "物流信息查询和管理")
public class LogisticsController {
    
    private final LogisticsService logisticsService;
    
    /**
     * 查询物流信息
     */
    @GetMapping("/query/{orderId}")
    @Operation(summary = "查询物流信息")
    public ApiResponse<LogisticsDTO> queryLogistics(@PathVariable Long orderId) {
        return ApiResponse.success(logisticsService.queryLogistics(orderId));
    }
    
    /**
     * 手动同步物流信息
     */
    @PostMapping("/sync/{logisticsId}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).LOGISTICS_SYNC)")
    @Operation(summary = "手动同步物流")
    public ApiResponse<Void> syncLogistics(@PathVariable Long logisticsId) {
        logisticsService.syncLogistics(logisticsId);
        return ApiResponse.success();
    }
    
    /**
     * 获取物流统计（管理员）
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_LOGISTICS_VIEW)")
    @Operation(summary = "物流统计")
    public ApiResponse<LogisticsStatisticsDTO> getStatistics(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.success(logisticsService.getLogisticsStatistics(startDate, endDate));
    }
}
```

#### 2. 实时聊天接口

```java
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "实时聊天", description = "WebSocket聊天功能增强")
public class ChatController {
    
    private final ChatService chatService;
    
    /**
     * 获取在线用户列表
     */
    @GetMapping("/online-users")
    @Operation(summary = "在线用户列表")
    public ApiResponse<List<UserPresenceDTO>> getOnlineUsers() {
        return ApiResponse.success(chatService.getOnlineUsers());
    }
    
    /**
     * 查询用户在线状态
     */
    @GetMapping("/presence/{userId}")
    @Operation(summary = "用户在线状态")
    public ApiResponse<UserPresenceDTO> getUserPresence(@PathVariable Long userId) {
        return ApiResponse.success(chatService.getUserPresence(userId));
    }
    
    /**
     * 标记消息已读
     */
    @PostMapping("/messages/{messageId}/read")
    @Operation(summary = "标记消息已读")
    public ApiResponse<Void> markAsRead(@PathVariable Long messageId) {
        Long currentUserId = SecurityContextHolder.getCurrentUserId();
        chatService.markMessageAsRead(messageId, currentUserId);
        return ApiResponse.success();
    }
    
    /**
     * 撤回消息
     */
    @PostMapping("/messages/{messageId}/recall")
    @Operation(summary = "撤回消息")
    public ApiResponse<Void> recallMessage(@PathVariable Long messageId) {
        Long currentUserId = SecurityContextHolder.getCurrentUserId();
        chatService.recallMessage(messageId, currentUserId);
        return ApiResponse.success();
    }
    
    /**
     * 获取未读消息数
     */
    @GetMapping("/unread-count")
    @Operation(summary = "未读消息数")
    public ApiResponse<Integer> getUnreadCount() {
        Long currentUserId = SecurityContextHolder.getCurrentUserId();
        return ApiResponse.success(chatService.getUnreadCount(currentUserId));
    }
}
```

#### 3. 信用评级接口

```java
@RestController
@RequestMapping("/api/credit")
@RequiredArgsConstructor
@Tag(name = "信用评级", description = "用户信用分和信用等级管理")
public class CreditController {
    
    private final CreditService creditService;
    
    /**
     * 查询用户信用信息
     */
    @GetMapping("/score/{userId}")
    @Operation(summary = "查询信用信息")
    public ApiResponse<UserCreditScoreDTO> getCreditScore(@PathVariable Long userId) {
        return ApiResponse.success(creditService.getCreditScore(userId));
    }
    
    /**
     * 获取信用报告
     */
    @GetMapping("/report/{userId}")
    @Operation(summary = "信用报告")
    public ApiResponse<CreditReportDTO> getCreditReport(@PathVariable Long userId) {
        return ApiResponse.success(creditService.getCreditReport(userId));
    }
    
    /**
     * 手动重新计算信用分（管理员）
     */
    @PostMapping("/recalculate/{userId}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CREDIT_MANAGE)")
    @Operation(summary = "重新计算信用分")
    public ApiResponse<Integer> recalculateCreditScore(@PathVariable Long userId) {
        return ApiResponse.success(creditService.recalculateCreditScore(userId));
    }
}
```

#### 4. 推荐系统接口

```java
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "个性化推荐", description = "智能推荐算法")
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    /**
     * 个性化推荐
     */
    @GetMapping("/personal")
    @Operation(summary = "个性化推荐")
    public ApiResponse<List<GoodsDTO>> getPersonalRecommendations(
        @RequestParam(defaultValue = "10") int limit
    ) {
        Long currentUserId = SecurityContextHolder.getCurrentUserId();
        return ApiResponse.success(recommendationService.getPersonalRecommendations(currentUserId, limit));
    }
    
    /**
     * 相似商品推荐
     */
    @GetMapping("/similar/{goodsId}")
    @Operation(summary = "相似商品推荐")
    public ApiResponse<List<GoodsDTO>> getSimilarGoods(
        @PathVariable Long goodsId,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ApiResponse.success(recommendationService.getSimilarGoods(goodsId, limit));
    }
    
    /**
     * 热门推荐
     */
    @GetMapping("/popular")
    @Operation(summary = "热门推荐")
    public ApiResponse<List<GoodsDTO>> getPopularGoods(
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ApiResponse.success(recommendationService.getPopularGoods(limit));
    }
}
```

（其他接口设计省略，遵循相同规范...）

---

## 💾 缓存策略设计

### 缓存层次架构

```
┌─────────────────────────────────────────┐
│          本地缓存 (Caffeine)             │  ← 热点数据（如Feature Flags）
├─────────────────────────────────────────┤
│          分布式缓存 (Redis)              │  ← 业务数据
├─────────────────────────────────────────┤
│          数据库 (PostgreSQL)             │  ← 持久化存储
└─────────────────────────────────────────┘
```

### Redis缓存键命名规范

```
{业务模块}:{实体}:{标识}:{附加信息}

示例：
- logistics::123456                  # 物流信息（订单ID）
- user:presence:789                  # 用户在线状态（用户ID）
- recommendations:personal::456      # 个性化推荐（用户ID）
- credit:score:789                   # 信用分（用户ID）
- dashboard:merchant:123:2025-11-03  # 商家数据看板（商家ID+日期）
```

### 缓存过期时间策略

| 数据类型 | 过期时间 | 更新策略 | 说明 |
|---------|---------|---------|------|
| 物流信息 | 24小时 | 定时同步（2小时） | 减少API调用成本 |
| 在线状态 | 5分钟 | 实时更新 | 保证实时性 |
| 信用分 | 1小时 | 定时计算（每天凌晨2点） | 计算成本高，缓存降低负载 |
| 推荐结果 | 1小时 | 定时预计算（每2小时） | 算法复杂，预计算提升性能 |
| 数据看板 | 5分钟 | 实时统计 | 商家需要实时数据 |
| 搜索建议 | 1小时 | 定时更新 | 热词变化不频繁 |

### 缓存更新策略

**1. Cache-Aside（旁路缓存）**
```java
// 读取
public LogisticsDTO queryLogistics(Long orderId) {
    // 1. 先查缓存
    LogisticsDTO cached = cacheService.get("logistics::" + orderId);
    if (cached != null) {
        return cached;
    }
    
    // 2. 缓存未命中，查数据库
    Logistics logistics = logisticsRepository.findByOrderId(orderId).orElseThrow();
    LogisticsDTO dto = LogisticsMapper.INSTANCE.toDTO(logistics);
    
    // 3. 写入缓存
    cacheService.set("logistics::" + orderId, dto, Duration.ofHours(24));
    
    return dto;
}

// 更新
public void syncLogistics(Long logisticsId) {
    // 1. 更新数据库
    logisticsRepository.save(logistics);
    
    // 2. 删除缓存
    cacheService.delete("logistics::" + logistics.getOrderId());
}
```

**2. Write-Through（直写缓存）**
```java
public void updateUserPresence(Long userId, PresenceStatus status) {
    // 1. 更新数据库
    userPresenceRepository.save(presence);
    
    // 2. 同步更新缓存
    cacheService.set("user:presence:" + userId, presence, Duration.ofMinutes(5));
}
```

**3. Refresh-Ahead（预刷新缓存）**
```java
@Scheduled(cron = "0 30 */2 * * ?")
public void precomputeRecommendations() {
    // 定时预计算推荐结果，主动刷新缓存
    activeUserIds.forEach(userId -> {
        List<GoodsDTO> recommendations = calculateRecommendations(userId);
        cacheService.set("recommendations:personal::" + userId, recommendations, Duration.ofHours(2));
    });
}
```

### 缓存雪崩防护

**1. 随机过期时间**
```java
// 避免大量缓存同时过期
int randomSeconds = ThreadLocalRandom.current().nextInt(60, 300);  // 1-5分钟随机
cacheService.set(key, value, Duration.ofSeconds(baseSeconds + randomSeconds));
```

**2. 缓存预热**
```java
@PostConstruct
public void warmupCache() {
    // 应用启动时预热热点数据
    log.info("开始缓存预热...");
    
    // 预热热门商品
    List<Goods> popularGoods = goodsRepository.findTopByHeatScore(100);
    popularGoods.forEach(goods -> {
        cacheService.set("goods::" + goods.getId(), goods, Duration.ofHours(1));
    });
    
    log.info("缓存预热完成");
}
```

**3. 降级策略**
```java
public List<GoodsDTO> getPersonalRecommendations(Long userId, int limit) {
    try {
        // 尝试从缓存获取
        List<GoodsDTO> cached = cacheService.get("recommendations:personal::" + userId);
        if (cached != null) {
            return cached;
        }
        
        // 缓存未命中，执行推荐算法
        return calculateRecommendations(userId, limit);
        
    } catch (Exception e) {
        log.error("推荐算法失败，降级到热度推荐", e);
        return getPopularGoods(limit);  // 降级策略
    }
}
```

---

## ⚡ 性能优化方案

### 1. 数据库优化

**索引策略**：
```sql
-- 物流查询优化
CREATE INDEX idx_logistics_order ON t_logistics(order_id);
CREATE INDEX idx_logistics_tracking ON t_logistics(tracking_number);
CREATE INDEX idx_logistics_status_time ON t_logistics(status, last_sync_time);

-- 用户行为日志优化（联合索引）
CREATE INDEX idx_behavior_user_type_time ON t_user_behavior_log(user_id, behavior_type, created_at);

-- 商家数据看板优化（唯一联合索引）
CREATE UNIQUE INDEX idx_dashboard_merchant_date ON t_merchant_dashboard(merchant_id, stat_date);

-- 访客日志优化（分区表）
CREATE TABLE t_visitor_log_2025_11 PARTITION OF t_visitor_log
    FOR VALUES FROM ('2025-11-01') TO ('2025-12-01');
```

**查询优化**：
```java
// 批量查询，避免N+1问题
@Query("SELECT g FROM Goods g LEFT JOIN FETCH g.images WHERE g.id IN :ids")
List<Goods> findByIdsWithImages(@Param("ids") List<Long> ids);

// 使用JOIN FETCH优化关联查询
@Query("SELECT u FROM UserCreditScore u LEFT JOIN FETCH u.user WHERE u.userId = :userId")
Optional<UserCreditScore> findByUserIdWithUser(@Param("userId") Long userId);
```

**连接池优化**（application.yml）：
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20        # 最大连接数
      minimum-idle: 5              # 最小空闲连接
      connection-timeout: 30000    # 连接超时30s
      idle-timeout: 600000         # 空闲超时10分钟
      max-lifetime: 1800000        # 连接最大存活30分钟
      connection-test-query: SELECT 1
```

### 2. Redis优化

**连接池配置**：
```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 20    # 最大连接数
          max-idle: 10      # 最大空闲连接
          min-idle: 5       # 最小空闲连接
          max-wait: 5000    # 最大等待时间5s
```

**批量操作优化**：
```java
// 使用Pipeline批量操作
public void batchUpdateCache(Map<String, Object> data) {
    redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
        data.forEach((key, value) -> {
            connection.set(key.getBytes(), serialize(value));
        });
        return null;
    });
}
```

**大Key拆分**：
```java
// 避免存储大对象，拆分为多个小Key
// ❌ 错误：存储整个推荐列表（可能上千个商品）
cacheService.set("recommendations::" + userId, allRecommendations);

// ✅ 正确：只存储商品ID列表
List<Long> goodsIds = recommendations.stream().map(GoodsDTO::getId).collect(Collectors.toList());
cacheService.set("recommendations:ids::" + userId, goodsIds);
```

### 3. 异步处理

**使用@Async异步执行**：
```java
@Async("taskExecutor")
public CompletableFuture<Void> updateBehaviorTrace(Long userId, BehaviorType type, Long targetId) {
    // 记录行为日志（异步，不阻塞主流程）
    behaviorLogRepository.save(log);
    
    // 异步更新用户画像
    updateUserPersona(userId);
    
    return CompletableFuture.completedFuture(null);
}

// 线程池配置
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

### 4. 分页优化

**深度分页优化**：
```java
// ❌ 错误：深度分页性能差（OFFSET 10000）
@Query("SELECT g FROM Goods g ORDER BY g.createdAt DESC")
Page<Goods> findAll(Pageable pageable);

// ✅ 正确：使用ID范围查询（ID > lastId）
@Query("SELECT g FROM Goods g WHERE g.id > :lastId ORDER BY g.id ASC")
List<Goods> findByIdGreaterThan(@Param("lastId") Long lastId, Pageable pageable);
```

### 5. 并发控制

**秒杀活动库存扣减**（使用Redisson分布式锁）：
```java
@Service
@RequiredArgsConstructor
public class FlashSaleService {
    
    private final RedissonClient redissonClient;
    
    public boolean purchaseFlashSale(Long campaignId, Long userId) {
        String lockKey = "flash_sale:lock:" + campaignId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // 尝试加锁（等待3秒，锁定10秒）
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                // 检查库存
                Integer stock = getStockFromRedis(campaignId);
                if (stock <= 0) {
                    return false;
                }
                
                // 扣减库存
                decrStockInRedis(campaignId);
                
                // 创建订单
                createOrder(campaignId, userId);
                
                return true;
            } else {
                return false;  // 获取锁失败
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    private void decrStockInRedis(Long campaignId) {
        String stockKey = "flash_sale:stock:" + campaignId;
        redisTemplate.opsForValue().decrement(stockKey);
    }
}
```

---

## 🔒 安全设计方案

### 1. 权限控制

**新增权限编码**（遵循现有权限系统）：

```java
// PermissionCodes.java - 新增25个权限编码
public class PermissionCodes {
    // ... 现有权限编码
    
    // 物流管理 (2个)
    public static final String SYSTEM_LOGISTICS_VIEW = "system:logistics:view";
    public static final String SYSTEM_LOGISTICS_SYNC = "system:logistics:sync";
    
    // 信用管理 (2个)
    public static final String SYSTEM_CREDIT_VIEW = "system:credit:view";
    public static final String SYSTEM_CREDIT_MANAGE = "system:credit:manage";
    
    // 推荐系统 (2个)
    public static final String SYSTEM_RECOMMENDATION_VIEW = "system:recommendation:view";
    public static final String SYSTEM_RECOMMENDATION_MANAGE = "system:recommendation:manage";
    
    // 数据看板 (2个)
    public static final String DASHBOARD_MERCHANT_VIEW = "dashboard:merchant:view";
    public static final String DASHBOARD_MERCHANT_EXPORT = "dashboard:merchant:export";
    
    // 营销活动 (3个)
    public static final String CAMPAIGN_CREATE = "campaign:create";
    public static final String CAMPAIGN_MANAGE = "campaign:manage";
    public static final String CAMPAIGN_APPROVE = "campaign:approve";
    
    // 社区管理 (2个)
    public static final String COMMUNITY_POST = "community:post";
    public static final String COMMUNITY_MANAGE = "community:manage";
    
    // WebSocket (1个)
    public static final String WEBSOCKET_CONNECT = "websocket:connect";
    
    // 搜索功能 (1个)
    public static final String SEARCH_ADVANCED = "search:advanced";
    
    // 监控系统 (2个)
    public static final String SYSTEM_MONITOR_VIEW = "system:monitor:view";
    public static final String SYSTEM_MONITOR_MANAGE = "system:monitor:manage";
}
```

### 2. 数据脱敏

**用户隐私保护**：

```java
@Service
public class PrivacyService {
    
    /**
     * 脱敏用户ID（使用哈希）
     */
    public String maskUserId(Long userId) {
        return DigestUtils.sha256Hex(userId.toString()).substring(0, 16);
    }
    
    /**
     * 脱敏手机号（保留前3后4位）
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    
    /**
     * 脱敏地址（保留省市，隐藏详细地址）
     */
    public String maskAddress(String address) {
        // 实现地址脱敏逻辑
        return address.substring(0, Math.min(address.length(), 10)) + "...";
    }
}
```

### 3. API限流

**使用Redis实现滑动窗口限流**：

```java
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = generateKey(joinPoint);
        int limit = rateLimit.limit();
        int period = rateLimit.period();
        
        // 滑动窗口计数
        long currentTime = System.currentTimeMillis();
        String windowKey = key + ":" + (currentTime / (period * 1000));
        
        Long count = redisTemplate.opsForValue().increment(windowKey);
        if (count == 1) {
            redisTemplate.expire(windowKey, period, TimeUnit.SECONDS);
        }
        
        if (count > limit) {
            throw new RateLimitException("请求过于频繁，请稍后再试");
        }
        
        return joinPoint.proceed();
    }
}

// 使用示例
@RateLimit(limit = 100, period = 60)  // 每分钟最多100次
@GetMapping("/query/{orderId}")
public ApiResponse<LogisticsDTO> queryLogistics(@PathVariable Long orderId) {
    return ApiResponse.success(logisticsService.queryLogistics(orderId));
}
```

### 4. 敏感信息加密

**使用Jasypt加密配置文件**：

```yaml
# application.yml
spring:
  datasource:
    password: ENC(加密后的密码)
  
logistics:
  kuaidi100:
    api-key: ENC(加密后的API密钥)
```

---

## 📈 监控告警设计

### 1. 性能监控

**使用Micrometer + Prometheus**：

```java
@Service
@RequiredArgsConstructor
public class MetricsService {
    
    private final MeterRegistry meterRegistry;
    
    /**
     * 记录API性能指标
     */
    public void recordApiMetric(String endpoint, long responseTime, boolean success) {
        // 记录响应时间
        Timer.builder("api.response.time")
            .tag("endpoint", endpoint)
            .tag("status", success ? "success" : "failure")
            .register(meterRegistry)
            .record(responseTime, TimeUnit.MILLISECONDS);
        
        // 记录API调用次数
        Counter.builder("api.request.count")
            .tag("endpoint", endpoint)
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * 记录业务指标
     */
    public void recordBusinessMetric(String metricName, double value, Map<String, String> tags) {
        Gauge.builder(metricName, () -> value)
            .tags(Tags.of(tags))
            .register(meterRegistry);
    }
}
```

### 2. 告警规则

**Prometheus 告警配置**：

```yaml
groups:
  - name: campus-marketplace-alerts
    rules:
      # API响应时间告警
      - alert: ApiResponseTimeSlow
        expr: histogram_quantile(0.95, rate(api_response_time_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API响应时间过慢"
          description: "{{ $labels.endpoint }} P95响应时间超过1秒"
      
      # 错误率告警
      - alert: ApiErrorRateHigh
        expr: (rate(api_request_count{status="failure"}[5m]) / rate(api_request_count[5m])) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "API错误率过高"
          description: "{{ $labels.endpoint }} 错误率超过5%"
      
      # Redis缓存命中率告警
      - alert: CacheHitRateLow
        expr: cache_hit_rate < 0.6
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "缓存命中率过低"
          description: "缓存命中率低于60%，请检查缓存策略"
```

---

## 🎯 设计总结

### 复用现有组件统计

| 类型 | 复用组件数 | 新增组件数 | 扩展组件数 | 复用率 |
|------|-----------|-----------|-----------|--------|
| **实体** | 5个 (Order/Goods/User/Review/Post) | 10个 | 0个 | 33% |
| **Service** | 7个 (FileService/NotificationService/CacheService等) | 8个 | 5个 | 35% |
| **Controller** | 3个 (基础Controller模式) | 9个 | 0个 | 25% |
| **工具类** | 10个 (RedisUtil/SecurityUtil等) | 3个 | 0个 | 77% |
| **总计** | 25个 | 30个 | 5个 | 42% |

### 技术亮点

1. **复用优先设计** - 最大化复用现有组件，降低开发成本
2. **分层清晰** - 严格遵循Controller→Service→Repository架构
3. **性能优化** - 多级缓存、异步处理、批量操作
4. **安全可靠** - 权限控制、数据脱敏、限流防刷
5. **可扩展性** - 策略模式、工厂模式、易于扩展

---

**BaSui的设计原则总结**：
> **复用第一** - 能复用的绝不新建！  
> **性能至上** - 缓存、异步、批量，样样不落！  
> **安全可靠** - 权限、加密、限流，层层把关！  
> **架构清晰** - 分层明确，职责单一，易于维护！💪✨

需要我继续完善 design.md 的其他章节，或者开始创建 tasks.md 吗？😎
