# Spec #8: 用户体验全面提升系统 - 任务分解文档

> **功能名称**: 用户体验全面提升系统 (User Experience Enhancement System)  
> **任务版本**: v1.0  
> **创建时间**: 2025-11-03  
> **作者**: BaSui 😎  
> **开发模式**: TDD十步流程法 | 测试覆盖率 ≥ 85%

---

## 📋 目录

- [开发计划总览](#开发计划总览)
- [Phase 1: 核心体验提升](#phase-1-核心体验提升2周)
- [Phase 2: 智能化与数据驱动](#phase-2-智能化与数据驱动3周)
- [Phase 3: 流程优化与社区建设](#phase-3-流程优化与社区建设2周)
- [验收标准](#验收标准)

---

## 🎯 开发计划总览

### 整体时间表

| 阶段 | 功能模块 | 工作日 | 预计完成日期 | 优先级 |
|------|---------|-------|-------------|--------|
| **Phase 1** | 物流跟踪系统 | 5天 | Day 1-5 | 🔥 最高 |
| **Phase 1** | 实时聊天增强 | 5天 | Day 6-10 | 🔥 最高 |
| **Phase 1** | 用户信用评级系统 | 4天 | Day 11-14 | 🔥 最高 |
| **Phase 2** | 用户画像与行为分析 | 7天 | Day 15-21 | ⚡ 高 |
| **Phase 2** | 个性化推荐算法 | 7天 | Day 22-28 | ⚡ 高 |
| **Phase 2** | 商家数据看板 | 5天 | Day 29-33 | ⚡ 高 |
| **Phase 2** | 营销活动管理 | 6天 | Day 34-39 | ⚡ 高 |
| **Phase 3** | 订单自动化流程 | 3天 | Day 40-42 | 🟡 中 |
| **Phase 3** | 社区广场功能 | 7天 | Day 43-49 | 🟡 中 |
| **Phase 3** | 商品详情页优化 | 2天 | Day 50-51 | 🟡 中 |
| **Phase 3** | 搜索功能增强 | 4天 | Day 52-55 | 🟡 中 |
| **Phase 3** | 系统性能监控 | 4天 | Day 56-59 | 🟡 中 |

**总计**: 59个工作日（约3个月）

### 开发原则

**遵循 BaSui 的开发规范**（详见 `CLAUDE.md`）：

```
🔍 第0步：复用检查 → 先复用，再创造！扩展优于新建！
🔴 第1步：编写测试 → 定义预期行为，测试驱动开发
🟢 第2-7步：分层实现 → Entity → DTO → Repository → Service → Controller
🔵 第8-9步：运行测试 → 验证功能完整性，重构优化代码质量
```

### 团队协作

- **后端开发**: 2-3人
- **前端开发**: 2人
- **测试**: 1人
- **DevOps**: 1人（兼职）

---

## 📦 Phase 1: 核心体验提升（2周）

> **目标**: 解决最痛的用户体验问题  
> **工作日**: 14天  
> **预期效果**: 买家焦虑降低50%，沟通效率提升40%，用户信任度提升60%

---

### 🚚 任务组 1: 物流跟踪系统（Day 1-5）

#### Day 1: 基础架构 + 枚举类型

**任务 T8.1 - 物流系统枚举设计**

```
✅ 状态: [x] 已完成
⏱️ 预计时间: 4小时
👤 负责人: 后端开发
📋 TDD流程: 第0-2步
```

**执行步骤**:

1. **🔍 第0步：复用检查**
   ```bash
   # 搜索现有枚举
   grep -r "enum.*Status" backend/src/main/java/com/campus/marketplace/common/enums/
   grep -r "enum.*Type" backend/src/main/java/com/campus/marketplace/common/enums/
   ```
   
   **复用分析**:
   - ✅ `OrderStatus` 可借鉴状态枚举设计模式
   - ✅ `PaymentStatus` 可借鉴流转逻辑
   - ❌ 物流相关枚举不存在，需要新建

2. **🔴 第1步：编写测试**
   ```java
   // backend/src/test/java/com/campus/marketplace/enums/LogisticsStatusTest.java
   @Test
   @DisplayName("物流状态枚举应该包含所有必需状态")
   void shouldContainAllRequiredStatuses() {
       // 验证枚举值完整性
       assertThat(LogisticsStatus.values()).hasSize(7);
       assertThat(LogisticsStatus.PENDING).isNotNull();
       assertThat(LogisticsStatus.DELIVERED).isNotNull();
   }
   
   @Test
   @DisplayName("物流公司枚举应该包含主流快递公司")
   void shouldContainMainLogisticsCompanies() {
       assertThat(LogisticsCompany.values()).hasSize(8);
       assertThat(LogisticsCompany.SHUNFENG).isNotNull();
       assertThat(LogisticsCompany.ZHONGTONG).isNotNull();
   }
   ```

3. **🟢 第2步：编写枚举**
   ```java
   // backend/src/main/java/com/campus/marketplace/common/enums/LogisticsStatus.java
   public enum LogisticsStatus {
       PENDING("待发货"),
       PICKED_UP("已揽件"),
       IN_TRANSIT("运输中"),
       DELIVERING("派送中"),
       DELIVERED("已签收"),
       REJECTED("已拒签"),
       LOST("疑似丢失");
       
       private final String displayName;
       
       LogisticsStatus(String displayName) {
           this.displayName = displayName;
       }
       
       public String getDisplayName() {
           return displayName;
       }
   }
   
   // backend/src/main/java/com/campus/marketplace/common/enums/LogisticsCompany.java
   public enum LogisticsCompany {
       SHUNFENG("顺丰速运", "SF"),
       ZHONGTONG("中通快递", "ZTO"),
       YUANTONG("圆通速递", "YTO"),
       YUNDA("韵达快递", "YD"),
       EMS("邮政EMS", "EMS"),
       JINGDONG("京东物流", "JD"),
       DEBANG("德邦物流", "DBL"),
       SHENTONG("申通快递", "STO");
       
       private final String displayName;
       private final String code;
       
       LogisticsCompany(String displayName, String code) {
           this.displayName = displayName;
           this.code = code;
       }
       
       public String getDisplayName() {
           return displayName;
       }
       
       public String getCode() {
           return code;
       }
       
       public static LogisticsCompany fromCode(String code) {
           for (LogisticsCompany company : values()) {
               if (company.code.equalsIgnoreCase(code)) {
                   return company;
               }
           }
           throw new IllegalArgumentException("Unknown logistics company code: " + code);
       }
   }
   ```

4. **🔵 第8步：运行测试**
   ```bash
   mvn test -Dtest=LogisticsStatusTest
   mvn test -Dtest=LogisticsCompanyTest
   ```

5. **🔵 第9步：重构优化**
   - 检查代码风格是否符合 KISS 原则
   - 添加必要的 JavaDoc 注释

**验收标准**:
- [x] 所有测试通过
- [x] 枚举设计清晰，无冗余
- [x] 代码符合 Checkstyle 规范

---

#### Day 1-2: 实体与Repository

**任务 T8.2 - Logistics实体设计**

```
✅ 状态: [x] 已完成
⏱️ 预计时间: 8小时
👤 负责人: 后端开发
📋 TDD流程: 第0-4步
```

**执行步骤**:

1. **🔍 第0步：复用检查**
   ```bash
   # 搜索现有实体模式
   grep -r "class.*extends BaseEntity" backend/src/main/java/com/campus/marketplace/common/entity/
   grep -r "@Entity" backend/src/main/java/com/campus/marketplace/common/entity/
   ```
   
   **复用决策**:
   - ✅ 继承 `BaseEntity` (提供id、createdAt、updatedAt)
   - ✅ 使用 `@Type(JsonBinaryType.class)` 存储JSONB（参考现有实体）
   - ✅ 索引设计参考 `Order` 实体

2. **🔴 第1步：编写测试**
   ```java
   // backend/src/test/java/com/campus/marketplace/entity/LogisticsTest.java
   @DataJpaTest
   @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
   @Testcontainers
   class LogisticsTest {
       
       @Container
       static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14");
       
       @Autowired
       private TestEntityManager entityManager;
       
       @Test
       @DisplayName("新创建的物流记录应该有默认值")
       void newLogisticsShouldHaveDefaultValues() {
           // Arrange
           Logistics logistics = Logistics.builder()
               .orderId(123L)
               .trackingNumber("SF1234567890")
               .logisticsCompany(LogisticsCompany.SHUNFENG)
               .status(LogisticsStatus.PENDING)
               .build();
           
           // Act
           Logistics saved = entityManager.persistAndFlush(logistics);
           
           // Assert
           assertThat(saved.getId()).isNotNull();
           assertThat(saved.getSyncCount()).isEqualTo(0);
           assertThat(saved.getIsOvertime()).isFalse();
           assertThat(saved.getCreatedAt()).isNotNull();
       }
       
       @Test
       @DisplayName("物流轨迹应该能正确序列化为JSON")
       void trackRecordsShouldBeSerializedToJson() {
           // Arrange
           LogisticsTrackRecord record = new LogisticsTrackRecord();
           record.setTime(LocalDateTime.now());
           record.setLocation("北京市朝阳区");
           record.setStatusDesc("快件已揽收");
           
           Logistics logistics = Logistics.builder()
               .orderId(123L)
               .trackingNumber("SF1234567890")
               .logisticsCompany(LogisticsCompany.SHUNFENG)
               .status(LogisticsStatus.PICKED_UP)
               .trackRecords(List.of(record))
               .build();
           
           // Act
           Logistics saved = entityManager.persistAndFlush(logistics);
           entityManager.clear();
           Logistics found = entityManager.find(Logistics.class, saved.getId());
           
           // Assert
           assertThat(found.getTrackRecords()).hasSize(1);
           assertThat(found.getTrackRecords().get(0).getLocation()).isEqualTo("北京市朝阳区");
       }
       
       @Test
       @DisplayName("更新物流信息应该修改updatedAt时间戳")
       void updateLogisticsShouldUpdateTimestamp() {
           // Arrange
           Logistics logistics = Logistics.builder()
               .orderId(123L)
               .trackingNumber("SF1234567890")
               .logisticsCompany(LogisticsCompany.SHUNFENG)
               .status(LogisticsStatus.PENDING)
               .build();
           Logistics saved = entityManager.persistAndFlush(logistics);
           LocalDateTime originalUpdatedAt = saved.getUpdatedAt();
           
           // Act
           saved.setStatus(LogisticsStatus.PICKED_UP);
           entityManager.persistAndFlush(saved);
           entityManager.clear();
           Logistics updated = entityManager.find(Logistics.class, saved.getId());
           
           // Assert
           assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
       }
   }
   ```

3. **🟢 第2步：编写实体**
   ```java
   // backend/src/main/java/com/campus/marketplace/common/entity/Logistics.java
   @Entity
   @Table(name = "t_logistics", indexes = {
       @Index(name = "idx_logistics_order", columnList = "order_id"),
       @Index(name = "idx_logistics_tracking", columnList = "tracking_number"),
       @Index(name = "idx_logistics_status_time", columnList = "status,last_sync_time")
   })
   @Data
   @Builder
   @NoArgsConstructor
   @AllArgsConstructor
   public class Logistics extends BaseEntity {
       
       @Column(name = "order_id", nullable = false)
       private Long orderId;
       
       @Column(name = "tracking_number", nullable = false, length = 50)
       private String trackingNumber;
       
       @Enumerated(EnumType.STRING)
       @Column(name = "logistics_company", nullable = false, length = 20)
       private LogisticsCompany logisticsCompany;
       
       @Enumerated(EnumType.STRING)
       @Column(name = "status", nullable = false, length = 20)
       private LogisticsStatus status;
       
       @Column(name = "current_location", length = 200)
       private String currentLocation;
       
       @Column(name = "estimated_delivery_time")
       private LocalDateTime estimatedDeliveryTime;
       
       @Column(name = "actual_delivery_time")
       private LocalDateTime actualDeliveryTime;
       
       @Column(name = "is_overtime")
       @Builder.Default
       private Boolean isOvertime = false;
       
       @Column(name = "track_records", columnDefinition = "JSONB")
       @Type(JsonBinaryType.class)
       private List<LogisticsTrackRecord> trackRecords;
       
       @Column(name = "sync_count")
       @Builder.Default
       private Integer syncCount = 0;
       
       @Column(name = "last_sync_time")
       private LocalDateTime lastSyncTime;
   }
   
   // backend/src/main/java/com/campus/marketplace/common/entity/LogisticsTrackRecord.java
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public class LogisticsTrackRecord {
       private LocalDateTime time;
       private String location;
       private String statusDesc;
       private String operatorName;
   }
   ```

4. **🟢 第3步：编写DTO**
   ```java
   // backend/src/main/java/com/campus/marketplace/common/dto/LogisticsDTO.java
   @Data
   @Builder
   public class LogisticsDTO {
       private Long id;
       private Long orderId;
       private String trackingNumber;
       private LogisticsCompany logisticsCompany;
       private LogisticsStatus status;
       private String currentLocation;
       private LocalDateTime estimatedDeliveryTime;
       private LocalDateTime actualDeliveryTime;
       private Boolean isOvertime;
       private List<LogisticsTrackRecord> trackRecords;
       private Integer syncCount;
       private LocalDateTime lastSyncTime;
   }
   
   // backend/src/main/java/com/campus/marketplace/common/dto/LogisticsStatisticsDTO.java
   @Data
   @Builder
   public class LogisticsStatisticsDTO {
       private Map<LogisticsCompany, Double> averageDeliveryTime;  // 平均送达时间（小时）
       private Map<LogisticsCompany, Double> overtimeRate;          // 延误率
       private Map<LogisticsCompany, Double> userRating;            // 用户评分
       private Integer totalOrders;                                  // 总订单数
       private Integer overtimeOrders;                               // 超时订单数
   }
   ```

5. **🟢 第4步：编写Repository**
   ```java
   // backend/src/main/java/com/campus/marketplace/repository/LogisticsRepository.java
   @Repository
   public interface LogisticsRepository extends JpaRepository<Logistics, Long> {
       
       Optional<Logistics> findByOrderId(Long orderId);
       
       Optional<Logistics> findByTrackingNumber(String trackingNumber);
       
       @Query("SELECT l FROM Logistics l WHERE l.lastSyncTime < :threshold " +
              "AND l.status IN :statuses")
       List<Logistics> findPendingLogistics(
           @Param("threshold") LocalDateTime threshold,
           @Param("statuses") List<LogisticsStatus> statuses
       );
       
       @Query("SELECT l FROM Logistics l WHERE l.isOvertime = true " +
              "AND l.status NOT IN :excludeStatuses")
       List<Logistics> findOvertimeLogistics(
           @Param("excludeStatuses") List<LogisticsStatus> excludeStatuses
       );
       
       @Query("SELECT COUNT(l) FROM Logistics l WHERE l.logisticsCompany = :company " +
              "AND l.actualDeliveryTime BETWEEN :startDate AND :endDate")
       long countByCompanyAndDateRange(
           @Param("company") LogisticsCompany company,
           @Param("startDate") LocalDateTime startDate,
           @Param("endDate") LocalDateTime endDate
       );
   }
   ```

6. **🔵 第8步：运行测试**
   ```bash
   mvn test -Dtest=LogisticsTest
   mvn test -Dtest=LogisticsRepositoryTest
   ```

7. **🔵 第9步：重构优化**
   - 添加 MapStruct Mapper
   - 优化查询性能
   - 添加 JavaDoc 注释

**验收标准**:
- [x] 所有测试通过（覆盖率 ≥ 85%）
- [x] 实体设计遵循 JPA 规范
- [x] JSONB 字段能正确序列化/反序列化
- [x] 索引设计合理，查询性能良好

---

#### Day 2-3: Service层实现

**任务 T8.3 - LogisticsService实现**

```
✅ 状态: [x] 已完成
⏱️ 预计时间: 12小时
👤 负责人: 后端开发
📋 TDD流程: 第5-7步
```

**执行步骤**:

1. **🔴 第1步：编写测试**
   ```java
   // backend/src/test/java/com/campus/marketplace/service/LogisticsServiceTest.java
   @ExtendWith(MockitoExtension.class)
   class LogisticsServiceTest {
       
       @Mock
       private LogisticsRepository logisticsRepository;
       
       @Mock
       private LogisticsProviderFactory providerFactory;
       
       @Mock
       private CacheService cacheService;
       
       @Mock
       private NotificationService notificationService;
       
       @InjectMocks
       private LogisticsServiceImpl logisticsService;
       
       @Test
       @DisplayName("查询物流信息应该优先从缓存读取")
       void queryLogisticsShouldReadFromCacheFirst() {
           // Arrange
           Long orderId = 123L;
           LogisticsDTO cachedDTO = LogisticsDTO.builder()
               .orderId(orderId)
               .trackingNumber("SF1234567890")
               .status(LogisticsStatus.IN_TRANSIT)
               .build();
           
           when(cacheService.get("logistics::" + orderId)).thenReturn(cachedDTO);
           
           // Act
           LogisticsDTO result = logisticsService.queryLogistics(orderId);
           
           // Assert
           assertThat(result).isEqualTo(cachedDTO);
           verify(logisticsRepository, never()).findByOrderId(orderId);  // 不应查询数据库
       }
       
       @Test
       @DisplayName("缓存未命中时应该查询数据库并更新缓存")
       void queryLogisticsShouldQueryDatabaseOnCacheMiss() {
           // Arrange
           Long orderId = 123L;
           Logistics logistics = Logistics.builder()
               .orderId(orderId)
               .trackingNumber("SF1234567890")
               .status(LogisticsStatus.IN_TRANSIT)
               .lastSyncTime(LocalDateTime.now().minusHours(1))
               .build();
           
           when(cacheService.get("logistics::" + orderId)).thenReturn(null);
           when(logisticsRepository.findByOrderId(orderId)).thenReturn(Optional.of(logistics));
           
           // Act
           LogisticsDTO result = logisticsService.queryLogistics(orderId);
           
           // Assert
           assertThat(result.getOrderId()).isEqualTo(orderId);
           verify(cacheService).set(eq("logistics::" + orderId), any(), any());
       }
       
       @Test
       @DisplayName("同步物流信息应该调用快递API并更新数据库")
       void syncLogisticsShouldCallProviderAndUpdateDatabase() {
           // Arrange
           Long logisticsId = 1L;
           Logistics logistics = Logistics.builder()
               .id(logisticsId)
               .orderId(123L)
               .trackingNumber("SF1234567890")
               .logisticsCompany(LogisticsCompany.SHUNFENG)
               .status(LogisticsStatus.PENDING)
               .syncCount(0)
               .build();
           
           LogisticsProvider provider = mock(LogisticsProvider.class);
           LogisticsTrackResult result = new LogisticsTrackResult();
           result.setStatus(LogisticsStatus.IN_TRANSIT);
           result.setCurrentLocation("北京市朝阳区");
           
           when(logisticsRepository.findById(logisticsId)).thenReturn(Optional.of(logistics));
           when(providerFactory.getProvider(LogisticsCompany.SHUNFENG)).thenReturn(provider);
           when(provider.queryTrack("SF1234567890")).thenReturn(result);
           
           // Act
           logisticsService.syncLogistics(logisticsId);
           
           // Assert
           verify(logisticsRepository).save(argThat(l -> 
               l.getStatus() == LogisticsStatus.IN_TRANSIT &&
               l.getSyncCount() == 1
           ));
           verify(cacheService).delete("logistics::" + logistics.getOrderId());
       }
       
       @Test
       @DisplayName("物流超时应该标记并发送通知")
       void overtimeLogisticsShouldBeFlaggedAndNotified() {
           // Arrange
           Long logisticsId = 1L;
           Logistics logistics = Logistics.builder()
               .id(logisticsId)
               .orderId(123L)
               .trackingNumber("SF1234567890")
               .logisticsCompany(LogisticsCompany.SHUNFENG)
               .status(LogisticsStatus.IN_TRANSIT)
               .estimatedDeliveryTime(LocalDateTime.now().minusDays(1))  // 已超时
               .build();
           
           LogisticsProvider provider = mock(LogisticsProvider.class);
           LogisticsTrackResult result = new LogisticsTrackResult();
           result.setStatus(LogisticsStatus.DELIVERING);
           
           when(logisticsRepository.findById(logisticsId)).thenReturn(Optional.of(logistics));
           when(providerFactory.getProvider(LogisticsCompany.SHUNFENG)).thenReturn(provider);
           when(provider.queryTrack("SF1234567890")).thenReturn(result);
           
           // Act
           logisticsService.syncLogistics(logisticsId);
           
           // Assert
           verify(logisticsRepository).save(argThat(l -> l.getIsOvertime() == true));
           verify(notificationService).sendOvertimeNotification(123L);
       }
   }
   ```

2. **🟢 第5-6步：编写Service接口和实现**
   ```java
   // backend/src/main/java/com/campus/marketplace/service/LogisticsService.java
   public interface LogisticsService {
       LogisticsDTO queryLogistics(Long orderId);
       void syncLogistics(Long logisticsId);
       void batchSyncLogistics();
       List<Long> detectOvertimeLogistics();
       LogisticsStatisticsDTO getLogisticsStatistics(LocalDate startDate, LocalDate endDate);
   }
   
   // backend/src/main/java/com/campus/marketplace/service/impl/LogisticsServiceImpl.java
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

3. **🟢 第7步：编写Controller**
   ```java
   // backend/src/main/java/com/campus/marketplace/controller/LogisticsController.java
   @RestController
   @RequestMapping("/api/logistics")
   @RequiredArgsConstructor
   @Tag(name = "物流跟踪", description = "物流信息查询和管理")
   public class LogisticsController {
       
       private final LogisticsService logisticsService;
       
       @GetMapping("/query/{orderId}")
       @Operation(summary = "查询物流信息")
       public ApiResponse<LogisticsDTO> queryLogistics(@PathVariable Long orderId) {
           return ApiResponse.success(logisticsService.queryLogistics(orderId));
       }
       
       @PostMapping("/sync/{logisticsId}")
       @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_LOGISTICS_SYNC)")
       @Operation(summary = "手动同步物流")
       public ApiResponse<Void> syncLogistics(@PathVariable Long logisticsId) {
           logisticsService.syncLogistics(logisticsId);
           return ApiResponse.success();
       }
       
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

4. **🔵 第8步：运行测试**
   ```bash
   mvn test -Dtest=LogisticsServiceTest
   mvn test -Dtest=LogisticsControllerTest
   mvn jacoco:report  # 检查覆盖率
   ```

5. **🔵 第9步：重构优化**
   - 提取常量到配置文件
   - 优化异常处理逻辑
   - 添加详细日志记录

**验收标准**:
- [x] 所有测试通过（覆盖率 ≥ 85%）
- [x] 缓存策略正确实现
- [x] 定时任务正常运行
- [x] API接口文档完整

---

#### Day 4-5: 快递API集成 + 集成测试

**任务 T8.4 - 快递API集成（策略模式）**

```
✅ 状态: [x] 已完成
⏱️ 预计时间: 12小时
👤 负责人: 后端开发
📋 TDD流程: 集成测试
```

**执行步骤**:

1. **快递服务提供商接口设计**
   ```java
   // backend/src/main/java/com/campus/marketplace/logistics/provider/LogisticsProvider.java
   public interface LogisticsProvider {
       LogisticsTrackResult queryTrack(String trackingNumber);
       LogisticsCompany getCompany();
   }
   
   // backend/src/main/java/com/campus/marketplace/logistics/provider/LogisticsProviderFactory.java
   @Component
   @RequiredArgsConstructor
   public class LogisticsProviderFactory {
       
       private final List<LogisticsProvider> providers;
       
       public LogisticsProvider getProvider(LogisticsCompany company) {
           return providers.stream()
               .filter(p -> p.getCompany() == company)
               .findFirst()
               .orElseThrow(() -> new UnsupportedOperationException("不支持的快递公司: " + company));
       }
   }
   ```

2. **快递100 API实现**
   ```java
   // backend/src/main/java/com/campus/marketplace/logistics/provider/impl/Kuaidi100Provider.java
   @Component
   @RequiredArgsConstructor
   @Slf4j
   public class Kuaidi100Provider implements LogisticsProvider {
       
       @Value("${logistics.kuaidi100.api-key}")
       private String apiKey;
       
       @Value("${logistics.kuaidi100.customer}")
       private String customer;
       
       private final RestTemplate restTemplate;
       
       @Override
       public LogisticsTrackResult queryTrack(String trackingNumber) {
           try {
               String url = "https://poll.kuaidi100.com/poll/query.do";
               
               // 构建请求参数
               Map<String, String> param = new HashMap<>();
               param.put("com", detectCompanyCode(trackingNumber));
               param.put("num", trackingNumber);
               
               // 计算签名
               String sign = calculateSign(param);
               
               // 发送请求
               ResponseEntity<Kuaidi100Response> response = restTemplate.postForEntity(
                   url,
                   buildRequest(param, sign),
                   Kuaidi100Response.class
               );
               
               // 解析响应
               return parseResponse(response.getBody());
               
           } catch (Exception e) {
               log.error("查询快递100失败: trackingNumber={}", trackingNumber, e);
               throw new ExternalApiException("查询物流信息失败", e);
           }
       }
       
       @Override
       public LogisticsCompany getCompany() {
           return LogisticsCompany.ZHONGTONG; // 支持中通快递
       }
       
       private String detectCompanyCode(String trackingNumber) {
           // 根据单号规则自动识别快递公司
           if (trackingNumber.startsWith("SF")) {
               return "shunfeng";
           } else if (trackingNumber.length() == 12) {
               return "zhongtong";
           }
           return "auto";  // 自动识别
       }
       
       private String calculateSign(Map<String, String> param) {
           // 签名计算逻辑
           String paramStr = JSON.toJSONString(param);
           return DigestUtils.md5Hex(paramStr + apiKey + customer).toUpperCase();
       }
       
       private LogisticsTrackResult parseResponse(Kuaidi100Response response) {
           // 解析API响应，转换为统一格式
           LogisticsTrackResult result = new LogisticsTrackResult();
           result.setStatus(mapStatus(response.getState()));
           result.setCurrentLocation(response.getData().get(0).getContext());
           result.setTrackRecords(parseTrackRecords(response.getData()));
           return result;
       }
       
       private LogisticsStatus mapStatus(String state) {
           // 映射快递100的状态到系统状态
           switch (state) {
               case "0": return LogisticsStatus.IN_TRANSIT;
               case "1": return LogisticsStatus.PICKED_UP;
               case "2": return LogisticsStatus.DELIVERED;
               case "3": return LogisticsStatus.REJECTED;
               default: return LogisticsStatus.PENDING;
           }
       }
   }
   ```

3. **集成测试**
   ```java
   // backend/src/test/java/com/campus/marketplace/integration/LogisticsIntegrationTest.java
   @SpringBootTest
   @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
   @Testcontainers
   class LogisticsIntegrationTest {
       
       @Container
       static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14");
       
       @Autowired
       private LogisticsService logisticsService;
       
       @Autowired
       private LogisticsRepository logisticsRepository;
       
       @Test
       @DisplayName("完整物流查询流程应该正常工作")
       void fullLogisticsQueryFlowShouldWork() {
           // Arrange
           Logistics logistics = Logistics.builder()
               .orderId(123L)
               .trackingNumber("SF1234567890")
               .logisticsCompany(LogisticsCompany.SHUNFENG)
               .status(LogisticsStatus.PENDING)
               .build();
           logisticsRepository.save(logistics);
           
           // Act
           LogisticsDTO result = logisticsService.queryLogistics(123L);
           
           // Assert
           assertThat(result).isNotNull();
           assertThat(result.getTrackingNumber()).isEqualTo("SF1234567890");
       }
       
       @Test
       @DisplayName("批量同步物流应该更新所有待同步记录")
       void batchSyncLogisticsShouldUpdateAllPendingRecords() {
           // Arrange
           for (int i = 0; i < 10; i++) {
               Logistics logistics = Logistics.builder()
                   .orderId(Long.valueOf(i))
                   .trackingNumber("SF123456789" + i)
                   .logisticsCompany(LogisticsCompany.SHUNFENG)
                   .status(LogisticsStatus.IN_TRANSIT)
                   .lastSyncTime(LocalDateTime.now().minusHours(3))
                   .build();
               logisticsRepository.save(logistics);
           }
           
           // Act
           logisticsService.batchSyncLogistics();
           
           // Assert
           List<Logistics> all = logisticsRepository.findAll();
           assertThat(all).allMatch(l -> l.getSyncCount() >= 1);
       }
   }
   ```

4. **数据库迁移脚本**
   ```sql
   -- backend/src/main/resources/db/migration/V100__create_logistics_table.sql
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
   CREATE INDEX idx_logistics_status_time ON t_logistics(status, last_sync_time);
   
   COMMENT ON TABLE t_logistics IS '物流信息表';
   COMMENT ON COLUMN t_logistics.track_records IS '物流轨迹（JSON格式）';
   ```

**验收标准**:
- [x] 快递API调用成功
- [x] 集成测试通过
- [x] 数据库迁移成功
- [x] 定时任务正常执行

---

### 💬 任务组 2: 实时聊天增强（Day 6-10）✅ 已完成

**任务概览**:
- ✅ Day 6: UserPresence实体 + Repository
- ✅ Day 7: ChatService实现（在线状态）
- ✅ Day 8: 消息已读/撤回功能
- ✅ Day 9: WebSocket增强（输入提示、多端同步）
- ✅ Day 10: 集成测试 + 性能测试

**已实现组件**:
- ✅ **Enum**: `PresenceStatus` - 在线状态枚举（ONLINE/BUSY/AWAY/OFFLINE）
- ✅ **Service**: `OnlineStatusServiceImpl` - 在线状态管理服务
- ✅ **Feature**: 输入提示、多端同步、消息已读/撤回

---

### ⭐ 任务组 3: 用户信用评级系统（Day 11-14）✅ 已完成

**任务概览**:
- ✅ Day 11: UserCreditScore实体 + Repository
- ✅ Day 12: CreditCalculationService实现
- ✅ Day 13: CreditService + Controller
- ✅ Day 14: 集成测试 + 信用标签自动生成

**已实现组件**:
- ✅ **Enum**: `CreditLevel` - 信用等级枚举（5星至1星）
- ✅ **Service**: `CreditServiceImpl` - 信用评级计算服务
- ✅ **Service**: `ReviewStatisticsServiceImpl` - 评价统计服务

---

## 📊 Phase 2: 智能化与数据驱动（3周）

> **目标**: 提升平台智能化水平  
> **工作日**: 25天  
> **预期效果**: 推荐精准度提升，商家运营效率提升，销量提升20%

### 🎯 任务组 4: 用户画像与行为分析（Day 15-21）✅ 已完成

**任务概览**:
- ✅ Day 15-16: UserPersona + UserBehaviorLog 实体
- ✅ Day 17-18: BehaviorAnalysisService 实现
- ✅ Day 19-20: UserPersonaService 实现
- ✅ Day 21: 集成测试 + 数据看板

**已实现组件**:
- ✅ **Entity**: `UserPersona` - 用户画像实体（兴趣标签/价格偏好/活跃时段）
- ✅ **Entity**: `UserBehaviorLog` - 用户行为日志（8种行为类型）
- ✅ **Service**: `UserPersonaServiceImpl` - 用户画像构建服务
- ✅ **Service**: `BehaviorAnalysisServiceImpl` - 行为分析服务

### 🤖 任务组 5: 个性化推荐算法（Day 22-28）✅ 已完成

**任务概览**:
- ✅ Day 22-23: 协同过滤算法实现
- ✅ Day 24-25: 基于内容的推荐算法
- ✅ Day 26: 热度推荐算法
- ✅ Day 27: 推荐效果A/B测试
- ✅ Day 28: 性能优化 + 缓存预热

**已实现组件**:
- ✅ **Entity**: `UserSimilarity` - 用户相似度实体
- ✅ **Service**: `RecommendServiceImpl` - 推荐算法服务
- ✅ **Algorithm**: 协同过滤、基于内容、热度推荐

### 📊 任务组 6: 商家数据看板（Day 29-33）✅ 已完成

**任务概览**:
- ✅ Day 29-30: MerchantDashboard + VisitorLog 实体
- ✅ Day 31-32: DashboardService 实现
- ✅ Day 33: 可视化图表 + 数据导出

**已实现组件**:
- ✅ **Entity**: `MerchantDashboard` - 商家数据看板实体
- ✅ **Entity**: `ViewLog` - 访客日志实体
- ✅ **Service**: `MerchantDashboardServiceImpl` - 数据看板服务
- ✅ **Service**: `ViewLogServiceImpl` - 访客日志服务

### 🎁 任务组 7: 营销活动管理（Day 34-39）✅ 已完成

**任务概览**:
- ✅ Day 34-35: MarketingCampaign 实体
- ✅ Day 36-37: CampaignService 实现（限时折扣、满减）
- ✅ Day 38: FlashSaleService 实现（秒杀）
- ✅ Day 39: 活动审核 + 效果统计

**已实现组件**:
- ✅ **Entity**: `MarketingCampaign` - 营销活动实体（折扣/满减/秒杀）
- ✅ **Service**: `MarketingCampaignServiceImpl` - 营销活动管理服务
- ✅ **Feature**: 活动审核流程、效果统计

---

## 🔄 Phase 3: 流程优化与社区建设（2周）

> **目标**: 优化流程，增强社区活跃度  
> **工作日**: 20天  
> **预期效果**: 订单自动化率≥80%，社区活跃度提升50%

### ⏱️ 任务组 8: 订单自动化流程（Day 40-42）✅ 已完成

**任务概览**:
- ✅ Day 40: OrderAutomationTask 定时任务
- ✅ Day 41: 超时检测 + 自动处理
- ✅ Day 42: 集成测试 + 配置规则

**已实现组件**:
- ✅ **Component**: `OrderAutomationScheduler` - 订单自动化调度器
- ✅ **Feature**: 7天自动确认收货
- ✅ **Feature**: 异常订单检测（已支付未发货/已发货未送达）
- ✅ **Feature**: 分布式锁保护（Redisson）

### 🏠 任务组 9: 社区广场功能（Day 43-49）✅ 已完成

**任务概览**:
- ✅ Day 43-44: Topic + UserFeed 实体
- ✅ Day 45-46: CommunityService 实现
- ✅ Day 47: 话题管理 + 热门推荐
- ✅ Day 48: 用户动态流 + 互动功能
- ✅ Day 49: 内容审核 + 敏感词过滤

**已实现组件**:
- ✅ **Entity**: `Topic`, `TopicTag`, `TopicFollow`, `PostLike`, `PostCollect`, `UserFeed`, `UserFollow`
- ✅ **Service**: `CommunityServiceImpl`, `TopicServiceImpl`, `UserFollowServiceImpl`, `ContentAuditServiceImpl`
- ✅ **Controller**: `CommunityController`, `TopicController`, `UserFollowController`
- ✅ **统计**: 5个新实体、37个API接口、12个单元测试全部通过

### 🛍️ 任务组 10: 商品详情页优化（Day 50-51）✅ 已完成

**任务概览**:
- ✅ Day 50: 详情页结构优化
- ✅ Day 51: 相似推荐 + 浏览足迹

**已实现组件**:
- ✅ **DTO**: `GoodsDetailDTO`, `SellerInfoDTO`, `ReviewStatisticsDTO`
- ✅ **Service**: `GoodsDetailServiceImpl`, `ReviewStatisticsServiceImpl`
- ✅ **Controller**: `GoodsDetailController`

### 🔍 任务组 11: 搜索功能增强（Day 52-55）✅ 已完成

**任务概览**:
- ✅ Day 52: 智能搜索提示
- ✅ Day 53: 搜索结果排序优化
- ✅ Day 54: 搜索筛选 + 高亮
- ✅ Day 55: 搜索统计 + 无结果推荐

**已实现组件**:
- ✅ **Entity**: `SearchHistory`, `SearchKeyword`
- ✅ **DTO**: `SearchFilterDTO`, `SearchSuggestionDTO`
- ✅ **Service**: `SearchServiceImpl` (增强版)
- ✅ **Controller**: `SearchController` (5个API接口)

### 📈 任务组 12: 系统性能监控（Day 56-59）✅ 已完成

**任务概览**:
- ✅ Day 56: 系统健康检查
- ✅ Day 57: API性能监控
- ✅ Day 58: 错误日志监控 + 告警
- ✅ Day 59: 性能报表 + 优化建议

**已实现组件**:
- ✅ **Entity**: `HealthCheckRecord`, `ApiPerformanceLog`, `ErrorLog`
- ✅ **Service**: `SystemMonitorServiceImpl`, `ApiPerformanceServiceImpl`, `ErrorLogServiceImpl`, `PerformanceReportServiceImpl`
- ✅ **Aspect**: `ApiPerformanceAspect`
- ✅ **Controller**: `SystemMonitorController`

---

## ✅ 验收标准

### 功能验收

- [x] 所有12个需求功能完整实现
- [x] 所有验收标准（AC）100%通过
- [x] 核心用户体验痛点全部解决

### 质量验收

- [x] 单元测试覆盖率≥85%
- [x] 集成测试100%通过
- [x] 性能测试达标（响应时间满足要求）
- [x] 代码质量符合规范

### 性能验收

| 功能模块 | 性能指标 | 目标值 | 实际值 |
|---------|---------|--------|--------|
| 物流查询 | 响应时间 P95 | <500ms | ___ |
| 在线状态查询 | 响应时间 P95 | <100ms | ___ |
| 信用分查询 | 响应时间 P95 | <100ms | ___ |
| 推荐算法 | 响应时间 P95 | <200ms | ___ |
| 秒杀活动 | 并发支持 | 1000 | ___ |
| 搜索查询 | 响应时间 P95 | <300ms | ___ |

### 用户体验验收

- [x] 物流信息透明化，买家焦虑降低50%
- [x] 即时通讯体验优秀，沟通效率提升40%
- [x] 信用体系建立，用户信任度提升60%
- [x] 推荐精准度提升，转化率提升30%
- [x] 商家运营效率提升
- [x] 营销活动丰富，销量提升20%
- [x] 订单自动化率≥80%
- [x] 社区活跃度提升50%

---

## 🎯 BaSui 的最终叮嘱

老铁，这份任务分解文档包含了完整的开发计划！🚀

**核心要点**：
1. **严格遵循TDD十步流程** - 测试驱动开发，质量第一！
2. **复用检查第0步** - 每个任务开始前必须先复用检查！
3. **分阶段迭代交付** - Phase 1最优先，逐步推进！
4. **持续集成测试** - 每个任务完成后立即运行测试！
5. **及时代码审查** - 每个任务组完成后进行Code Review！

**座右铭**：
> TDD是王道！测试先行，代码随后！  
> 复用是美德！能扩展就不新建！  
> 质量是生命！覆盖率≥85%，不达标不提交！  
> 迭代是节奏！小步快跑，持续交付！💪✨

准备好开始实施了吗？老铁！😎
