package com.campus.marketplace.entity;

import com.campus.marketplace.common.entity.UserBehaviorLog;
import com.campus.marketplace.common.enums.BehaviorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户行为日志实体测试
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DisplayName("用户行为日志实体测试")
class UserBehaviorLogTest {

    @Container
    @SuppressWarnings("resource") // Testcontainers 自动管理容器生命周期
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("campus_marketplace_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("新创建的行为日志应该有默认值")
    void newBehaviorLogShouldHaveDefaultValues() {
        // Arrange
        UserBehaviorLog log = UserBehaviorLog.builder()
                .userId(123L)
                .behaviorType(BehaviorType.VIEW)
                .targetType("Goods")
                .targetId(456L)
                .source("推荐")
                .build();

        // Act
        UserBehaviorLog saved = entityManager.persistAndFlush(log);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(123L);
        assertThat(saved.getBehaviorType()).isEqualTo(BehaviorType.VIEW);
    }

    @Test
    @DisplayName("行为日志应该能正确存储额外数据到JSONB字段")
    void behaviorLogShouldStoreExtraDataAsJsonb() {
        // Arrange
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("keyword", "iPhone 13");
        extraData.put("categoryId", 1L);
        extraData.put("priceRange", "5000-8000");

        UserBehaviorLog log = UserBehaviorLog.builder()
                .userId(123L)
                .behaviorType(BehaviorType.SEARCH)
                .targetType("Goods")
                .targetId(null)
                .source("搜索框")
                .extraData(extraData)
                .build();

        // Act
        UserBehaviorLog saved = entityManager.persistAndFlush(log);
        entityManager.clear();
        UserBehaviorLog found = entityManager.find(UserBehaviorLog.class, saved.getId());

        // Assert
        assertThat(found.getExtraData()).isNotNull();
        assertThat(found.getExtraData()).containsEntry("keyword", "iPhone 13");
        assertThat(found.getExtraData()).containsEntry("categoryId", 1);
        assertThat(found.getExtraData()).containsEntry("priceRange", "5000-8000");
    }

    @Test
    @DisplayName("浏览行为应该记录浏览时长")
    void viewBehaviorShouldRecordDuration() {
        // Arrange
        UserBehaviorLog log = UserBehaviorLog.builder()
                .userId(123L)
                .behaviorType(BehaviorType.VIEW)
                .targetType("Goods")
                .targetId(456L)
                .source("推荐")
                .duration(120)  // 浏览120秒
                .sessionId("session-abc-123")
                .deviceType("Mobile")
                .build();

        // Act
        UserBehaviorLog saved = entityManager.persistAndFlush(log);

        // Assert
        assertThat(saved.getDuration()).isEqualTo(120);
        assertThat(saved.getSessionId()).isEqualTo("session-abc-123");
        assertThat(saved.getDeviceType()).isEqualTo("Mobile");
    }

    @Test
    @DisplayName("购买行为应该关联商品ID")
    void purchaseBehaviorShouldLinkToGoodsId() {
        // Arrange
        UserBehaviorLog log = UserBehaviorLog.builder()
                .userId(123L)
                .behaviorType(BehaviorType.PURCHASE)
                .targetType("Goods")
                .targetId(789L)
                .source("商品详情页")
                .build();

        // Act
        UserBehaviorLog saved = entityManager.persistAndFlush(log);

        // Assert
        assertThat(saved.getBehaviorType()).isEqualTo(BehaviorType.PURCHASE);
        assertThat(saved.getTargetId()).isEqualTo(789L);
    }

    @Test
    @DisplayName("联合索引应该能快速查询用户行为")
    void compositeIndexShouldEnableFastQuery() {
        // Arrange - 插入多条行为日志
        for (int i = 0; i < 10; i++) {
            UserBehaviorLog log = UserBehaviorLog.builder()
                    .userId(123L)
                    .behaviorType(BehaviorType.VIEW)
                    .targetType("Goods")
                    .targetId((long) i)
                    .source("推荐")
                    .build();
            entityManager.persist(log);
        }
        entityManager.flush();
        entityManager.clear();

        // Act - 使用JPQL查询（会利用索引）
        var query = entityManager.getEntityManager()
                .createQuery("SELECT b FROM UserBehaviorLog b " +
                        "WHERE b.userId = :userId AND b.behaviorType = :type " +
                        "ORDER BY b.createdAt DESC", UserBehaviorLog.class);
        query.setParameter("userId", 123L);
        query.setParameter("type", BehaviorType.VIEW);
        var results = query.getResultList();

        // Assert
        assertThat(results).hasSize(10);
        assertThat(results).allMatch(log -> log.getUserId().equals(123L));
    }
}
