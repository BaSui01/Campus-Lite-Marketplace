# Spec #7: 评价系统完善 - 架构设计文档

> **功能名称**: 评价系统完善 (Review System Enhancement)
> **设计版本**: v1.0
> **创建时间**: 2025-11-03
> **作者**: BaSui 😎
> **架构原则**: SOLID | KISS | DRY | 模块化 | NLP集成

---

## 📐 架构设计概览

### 🎯 设计目标

1. **扩展现有Review实体**：复用不重复造轮子
2. **NLP智能化**：标签提取+情感分析
3. **高性能**：评价提交≤1.5s，查询≤300ms
4. **激励机制**：积分奖励+优质认证

### 🏗️ 架构分层

```
Controller 层
├─ ReviewController (扩展)
├─ ReviewReplyController (新增)
└─ ReviewMediaController (新增)

Service 层
├─ ReviewService (扩展现有)
├─ ReviewReplyService (新增)
├─ ReviewMediaService (新增)
├─ ReviewInteractionService (新增：点赞/举报)
├─ ReviewTagService (新增：标签提取)
├─ ReviewSentimentService (新增：情感分析)
├─ ReviewAuditService (新增：审核过滤)
└─ ReviewIncentiveService (新增：激励机制)

Repository 层
├─ ReviewRepository (扩展)
├─ ReviewReplyRepository (新增)
├─ ReviewMediaRepository (新增)
├─ ReviewLikeRepository (新增)
├─ ReviewTagRepository (新增)
└─ ReviewSentimentRepository (新增)

Entity 层
├─ Review (扩展字段：多维度评分、情感分析结果)
├─ ReviewReply (新增：回复和追评)
├─ ReviewMedia (新增：图片/视频)
├─ ReviewLike (新增：点赞记录)
├─ ReviewTag (新增：标签)
└─ ReviewSentiment (新增：情感分析)
```

---

## 🗂️ 数据模型设计

### 1️⃣ Review 实体扩展（复用现有）

```java
@Entity
@Table(name = "t_review")
public class Review extends BaseEntity {
    // ========== 现有字段（保留不变）==========
    private Long orderId;
    private Long buyerId;
    private Long sellerId;
    private Integer rating; // 保留作为综合评分

    // ========== 新增字段（扩展）==========
    /**
     * 三维评分
     */
    @Column(name = "goods_quality_score")
    private Integer goodsQualityScore; // 商品质量评分 (1-5星)

    @Column(name = "service_score")
    private Integer serviceScore; // 服务态度评分 (1-5星)

    @Column(name = "logistics_score")
    private Integer logisticsScore; // 物流速度评分 (1-5星)

    /**
     * 媒体统计
     */
    @Column(name = "has_media")
    @Builder.Default
    private Boolean hasMedia = false; // 是否有图片/视频

    @Column(name = "media_count")
    @Builder.Default
    private Integer mediaCount = 0; // 媒体数量

    /**
     * 互动数据
     */
    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0; // 点赞数

    @Column(name = "is_quality_review")
    @Builder.Default
    private Boolean isQualityReview = false; // 是否优质评价

    /**
     * 情感分析结果
     */
    @Column(name = "sentiment_score")
    private Double sentimentScore; // 情感得分 (0-1)

    @Column(name = "sentiment_type")
    @Enumerated(EnumType.STRING)
    private SentimentType sentimentType; // 好评/中评/差评

    /**
     * 追评
     */
    @Column(name = "has_additional_review")
    @Builder.Default
    private Boolean hasAdditionalReview = false; // 是否有追评

    @Column(name = "additional_review_at")
    private LocalDateTime additionalReviewAt; // 追评时间
}
```

### 2️⃣ ReviewReply 实体（新增）

```java
@Entity
@Table(name = "t_review_reply")
@Data @Builder
public class ReviewReply extends BaseEntity {
    @Column(name = "review_id", nullable = false)
    private Long reviewId; // 关联评价ID

    @Column(name = "reply_type")
    @Enumerated(EnumType.STRING)
    private ReplyType replyType; // SELLER_REPLY(卖家回复) / BUYER_ADDITIONAL(买家追评)

    @Column(name = "replier_id", nullable = false)
    private Long replierId; // 回复者ID

    @Column(name = "replier_name", length = 50)
    private String replierName; // 回复者名称

    @Column(name = "content", nullable = false, length = 500)
    private String content; // 回复内容 (10-200字)

    @Column(name = "media_urls", columnDefinition = "TEXT")
    private String mediaUrls; // 追评图片URL列表（JSON格式，最多3张）

    @Column(name = "can_revoke")
    @Builder.Default
    private Boolean canRevoke = true; // 是否可撤回（3分钟内）

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt; // 撤回时间
}
```

### 3️⃣ ReviewMedia 实体（新增）

```java
@Entity
@Table(name = "t_review_media")
@Data @Builder
public class ReviewMedia extends BaseEntity {
    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "media_type")
    @Enumerated(EnumType.STRING)
    private MediaType mediaType; // IMAGE / VIDEO

    @Column(name = "media_url", nullable = false, length = 500)
    private String mediaUrl; // OSS存储路径

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl; // 缩略图URL（200x200）

    @Column(name = "file_size")
    private Long fileSize; // 文件大小（字节）

    @Column(name = "width")
    private Integer width; // 图片宽度

    @Column(name = "height")
    private Integer height; // 图片高度

    @Column(name = "duration")
    private Integer duration; // 视频时长（秒）
}
```

### 4️⃣ ReviewTag 实体（新增）

```java
@Entity
@Table(name = "t_review_tag")
@Data @Builder
public class ReviewTag extends BaseEntity {
    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "tag_name", nullable = false, length = 20)
    private String tagName; // 标签名称（如"物美价廉"）

    @Column(name = "tag_type")
    @Enumerated(EnumType.STRING)
    private TagType tagType; // POSITIVE(正面) / NEGATIVE(负面) / NEUTRAL(中性)

    @Column(name = "source")
    @Enumerated(EnumType.STRING)
    private TagSource source; // USER_SELECTED(用户选择) / SYSTEM_EXTRACTED(系统提取)
}
```

### 5️⃣ ReviewSentiment 实体（新增）

```java
@Entity
@Table(name = "t_review_sentiment")
@Data @Builder
public class ReviewSentiment extends BaseEntity {
    @Column(name = "review_id", nullable = false, unique = true)
    private Long reviewId;

    @Column(name = "sentiment_score", nullable = false)
    private Double sentimentScore; // 情感得分 (0-1)

    @Column(name = "sentiment_type")
    @Enumerated(EnumType.STRING)
    private SentimentType sentimentType; // POSITIVE(好评≥0.6) / NEUTRAL(中评0.4-0.6) / NEGATIVE(差评<0.4)

    @Column(name = "analysis_method", length = 50)
    private String analysisMethod; // 分析方法（SnowNLP / BaiduAI）

    @Column(name = "analysis_at")
    private LocalDateTime analysisAt; // 分析时间
}
```

### 🔢 枚举类型设计

```java
// SentimentType.java - 情感类型
public enum SentimentType {
    POSITIVE("好评"),   // ≥0.6
    NEUTRAL("中评"),    // 0.4-0.6
    NEGATIVE("差评");   // <0.4
}

// MediaType.java - 媒体类型
public enum MediaType {
    IMAGE("图片"),
    VIDEO("视频");
}

// ReplyType.java - 回复类型
public enum ReplyType {
    SELLER_REPLY("卖家回复"),
    BUYER_ADDITIONAL("买家追评");
}

// TagType.java - 标签类型
public enum TagType {
    POSITIVE("正面"),   // 质量好、态度好等
    NEGATIVE("负面"),   // 质量差、态度恶劣等
    NEUTRAL("中性");    // 包装完好等
}

// TagSource.java - 标签来源
public enum TagSource {
    USER_SELECTED("用户选择"),
    SYSTEM_EXTRACTED("系统提取");
}
```

---

## 🔧 核心Service设计

### 📋 主要接口

```java
// ReviewReplyService - 回复追评
public interface ReviewReplyService {
    Long replyToReview(Long reviewId, String content); // 卖家回复
    Long addAdditionalReview(Long reviewId, String content, List<String> mediaUrls); // 买家追评
    boolean revokeReply(Long replyId); // 撤回回复（3分钟内）
}

// ReviewMediaService - 媒体管理
public interface ReviewMediaService {
    List<Long> uploadReviewMedia(Long reviewId, List<MultipartFile> files); // 上传图片/视频
    List<ReviewMediaDTO> getReviewMedia(Long reviewId); // 查询评价媒体
}

// ReviewInteractionService - 互动功能
public interface ReviewInteractionService {
    boolean likeReview(Long reviewId, Long userId); // 点赞评价
    boolean unlikeReview(Long reviewId, Long userId); // 取消点赞
    Long reportReview(Long reviewId, String reportReason); // 举报评价
}

// ReviewTagService - 标签管理
public interface ReviewTagService {
    void extractTags(Long reviewId, String content); // 提取评价标签（NLP）
    List<ReviewTagDTO> getTagCloud(Long goodsId); // 获取商品标签云
}

// ReviewSentimentService - 情感分析
public interface ReviewSentimentService {
    void analyzeSentiment(Long reviewId, String content); // 分析情感倾向
    SentimentStatisticsDTO getSentimentStatistics(Long goodsId); // 情感统计
}

// ReviewAuditService - 审核管理
public interface ReviewAuditService {
    boolean filterSensitiveWords(String content); // 敏感词过滤
    boolean detectFakeReview(Long reviewId); // 检测虚假评价
    boolean auditReview(Long reviewId, boolean pass, String reason); // 人工审核
}

// ReviewIncentiveService - 激励机制
public interface ReviewIncentiveService {
    int rewardPoints(Long reviewId); // 评价积分奖励
    boolean certifyQualityReview(Long reviewId); // 优质评价认证
}
```

---

## 🎯 NLP集成设计

### 📦 依赖选型

```xml
<!-- 中文分词 -->
<dependency>
    <groupId>com.huaban</groupId>
    <artifactId>jieba-analysis</artifactId>
    <version>1.0.2</version>
</dependency>

<!-- 情感分析 -->
<dependency>
    <groupId>com.github.javaparser</groupId>
    <artifactId>javaparser-core</artifactId>
    <version>3.25.1</version>
</dependency>
```

### 🔍 标签提取算法

```java
@Service
public class ReviewTagServiceImpl implements ReviewTagService {

    // 预设标签库（正面）
    private static final Map<String, String> POSITIVE_KEYWORDS = Map.of(
        "很好", "质量好",
        "不错", "质量好",
        "物美价廉", "物美价廉",
        "快递神速", "发货快",
        "包装完好", "包装完好"
    );

    // 预设标签库（负面）
    private static final Map<String, String> NEGATIVE_KEYWORDS = Map.of(
        "质量差", "质量差",
        "描述不符", "描述不符",
        "态度恶劣", "态度恶劣"
    );

    @Override
    @Async
    public void extractTags(Long reviewId, String content) {
        // 1. 使用jieba分词
        JiebaSegmenter segmenter = new JiebaSegmenter();
        List<String> words = segmenter.sentenceProcess(content);

        // 2. 匹配预设标签
        Set<String> extractedTags = new HashSet<>();
        for (String word : words) {
            if (POSITIVE_KEYWORDS.containsKey(word)) {
                extractedTags.add(POSITIVE_KEYWORDS.get(word));
            } else if (NEGATIVE_KEYWORDS.containsKey(word)) {
                extractedTags.add(NEGATIVE_KEYWORDS.get(word));
            }
        }

        // 3. 保存标签
        extractedTags.forEach(tagName -> {
            ReviewTag tag = ReviewTag.builder()
                .reviewId(reviewId)
                .tagName(tagName)
                .tagType(determineTagType(tagName))
                .source(TagSource.SYSTEM_EXTRACTED)
                .build();
            reviewTagRepository.save(tag);
        });
    }
}
```

### 🔍 情感分析算法

```java
@Service
public class ReviewSentimentServiceImpl implements ReviewSentimentService {

    @Override
    @Async
    public void analyzeSentiment(Long reviewId, String content) {
        // 使用SnowNLP分析情感
        SnowNLP nlp = new SnowNLP(content);
        double sentimentScore = nlp.sentiments(); // 返回0-1的情感得分

        // 分类
        SentimentType sentimentType;
        if (sentimentScore >= 0.6) {
            sentimentType = SentimentType.POSITIVE; // 好评
        } else if (sentimentScore >= 0.4) {
            sentimentType = SentimentType.NEUTRAL; // 中评
        } else {
            sentimentType = SentimentType.NEGATIVE; // 差评
        }

        // 保存分析结果
        ReviewSentiment sentiment = ReviewSentiment.builder()
            .reviewId(reviewId)
            .sentimentScore(sentimentScore)
            .sentimentType(sentimentType)
            .analysisMethod("SnowNLP")
            .analysisAt(LocalDateTime.now())
            .build();
        reviewSentimentRepository.save(sentiment);

        // 更新评价表
        reviewRepository.updateSentiment(reviewId, sentimentScore, sentimentType);
    }
}
```

---

## 🚀 性能优化

### 1️⃣ 缓存策略

```java
// 评价详情缓存（10分钟）
@Cacheable(value = "review:detail", key = "#reviewId")
public ReviewDetailDTO getReviewDetail(Long reviewId);

// 评价列表缓存（5分钟）
@Cacheable(value = "review:list", key = "#goodsId + ':' + #pageable.pageNumber")
public Page<ReviewDTO> getGoodsReviews(Long goodsId, Pageable pageable);

// 标签云缓存（30分钟）
@Cacheable(value = "review:tag_cloud", key = "#goodsId")
public List<ReviewTagDTO> getTagCloud(Long goodsId);
```

### 2️⃣ 异步处理

```java
// 情感分析异步执行
@Async
public void analyzeSentiment(Long reviewId, String content);

// 标签提取异步执行
@Async
public void extractTags(Long reviewId, String content);

// 图片压缩异步执行
@Async
public void compressImages(List<Long> mediaIds);
```

### 3️⃣ 数据库索引

```sql
-- 评价表索引
CREATE INDEX idx_review_goods_id ON t_review(goods_id);
CREATE INDEX idx_review_user_id ON t_review(user_id);
CREATE INDEX idx_review_has_media ON t_review(has_media);
CREATE INDEX idx_review_is_quality ON t_review(is_quality_review);
CREATE INDEX idx_review_sentiment ON t_review(sentiment_type);

-- 点赞表唯一索引（防重复点赞）
CREATE UNIQUE INDEX uk_review_like ON t_review_like(review_id, user_id);

-- 标签表索引
CREATE INDEX idx_review_tag_review_id ON t_review_tag(review_id);
CREATE INDEX idx_review_tag_tag_name ON t_review_tag(tag_name);
```

---

## ✅ 复用现有组件

- ✅ **Review实体**：扩展字段，不重复创建
- ✅ **FileService**：图片/视频上传
- ✅ **PointsService**：评价积分奖励
- ✅ **NotificationService**：评价通知
- ✅ **AuditLogService**：审计日志

---

**📝 文档版本**: v1.0
**🗓️ 创建时间**: 2025-11-03
**👨‍💻 作者**: BaSui 😎
**✅ 状态**: Design完成
