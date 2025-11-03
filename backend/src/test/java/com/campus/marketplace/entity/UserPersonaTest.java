package com.campus.marketplace.entity;

import com.campus.marketplace.common.entity.UserPersona;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户画像实体测试
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DisplayName("用户画像实体测试")
class UserPersonaTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("campus_marketplace_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("新创建的用户画像应该有默认值")
    void newUserPersonaShouldHaveDefaultValues() {
        // Arrange
        UserPersona persona = UserPersona.builder()
                .userId(123L)
                .userSegment("新用户")
                .build();

        // Act
        UserPersona saved = entityManager.persistAndFlush(persona);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(123L);
    }

    @Test
    @DisplayName("用户画像应该能正确存储兴趣标签到JSONB字段")
    void userPersonaShouldStoreInterestTagsAsJsonb() {
        // Arrange
        Map<String, Double> interestTags = new HashMap<>();
        interestTags.put("电子产品", 0.8);
        interestTags.put("图书", 0.6);
        interestTags.put("运动器材", 0.3);

        UserPersona persona = UserPersona.builder()
                .userId(123L)
                .interestTags(interestTags)
                .userSegment("活跃用户")
                .build();

        // Act
        UserPersona saved = entityManager.persistAndFlush(persona);
        entityManager.clear();
        UserPersona found = entityManager.find(UserPersona.class, saved.getId());

        // Assert
        assertThat(found.getInterestTags()).isNotNull();
        assertThat(found.getInterestTags()).hasSize(3);
        assertThat(found.getInterestTags()).containsEntry("电子产品", 0.8);
        assertThat(found.getInterestTags()).containsEntry("图书", 0.6);
    }

    @Test
    @DisplayName("用户画像应该能正确存储价格偏好")
    void userPersonaShouldStorePricePreference() {
        // Arrange
        Map<String, Object> pricePreference = new HashMap<>();
        pricePreference.put("preferredRange", "50-200");
        pricePreference.put("avgSpending", 150);
        pricePreference.put("maxSpending", 500);

        UserPersona persona = UserPersona.builder()
                .userId(123L)
                .pricePreference(pricePreference)
                .userSegment("高价值用户")
                .build();

        // Act
        UserPersona saved = entityManager.persistAndFlush(persona);
        entityManager.clear();
        UserPersona found = entityManager.find(UserPersona.class, saved.getId());

        // Assert
        assertThat(found.getPricePreference()).isNotNull();
        assertThat(found.getPricePreference()).containsEntry("preferredRange", "50-200");
        assertThat(found.getPricePreference()).containsEntry("avgSpending", 150);
        assertThat(found.getPricePreference()).containsEntry("maxSpending", 500);
    }

    @Test
    @DisplayName("用户画像应该能正确存储活跃时段")
    void userPersonaShouldStoreActiveTimeSlots() {
        // Arrange
        List<String> activeTimeSlots = List.of("08:00-12:00", "18:00-22:00");

        UserPersona persona = UserPersona.builder()
                .userId(123L)
                .activeTimeSlots(activeTimeSlots)
                .userSegment("活跃用户")
                .build();

        // Act
        UserPersona saved = entityManager.persistAndFlush(persona);
        entityManager.clear();
        UserPersona found = entityManager.find(UserPersona.class, saved.getId());

        // Assert
        assertThat(found.getActiveTimeSlots()).isNotNull();
        assertThat(found.getActiveTimeSlots()).hasSize(2);
        assertThat(found.getActiveTimeSlots()).contains("08:00-12:00", "18:00-22:00");
    }

    @Test
    @DisplayName("用户画像应该能正确存储偏好分类和品牌")
    void userPersonaShouldStoreFavoriteCategoriesAndBrands() {
        // Arrange
        List<String> favoriteCategories = List.of("电子产品", "图书", "运动器材");
        List<String> favoriteBrands = List.of("Apple", "华为", "小米");

        UserPersona persona = UserPersona.builder()
                .userId(123L)
                .favoriteCategories(favoriteCategories)
                .favoriteBrands(favoriteBrands)
                .campusPreference("本部")
                .userSegment("高价值用户")
                .build();

        // Act
        UserPersona saved = entityManager.persistAndFlush(persona);
        entityManager.clear();
        UserPersona found = entityManager.find(UserPersona.class, saved.getId());

        // Assert
        assertThat(found.getFavoriteCategories()).hasSize(3);
        assertThat(found.getFavoriteCategories()).contains("电子产品", "图书", "运动器材");
        assertThat(found.getFavoriteBrands()).hasSize(3);
        assertThat(found.getFavoriteBrands()).contains("Apple", "华为", "小米");
        assertThat(found.getCampusPreference()).isEqualTo("本部");
    }

    @Test
    @DisplayName("userId应该有唯一约束")
    void userIdShouldHaveUniqueConstraint() {
        // Arrange
        UserPersona persona1 = UserPersona.builder()
                .userId(123L)
                .userSegment("活跃用户")
                .build();
        entityManager.persistAndFlush(persona1);

        UserPersona persona2 = UserPersona.builder()
                .userId(123L)  // 相同的userId
                .userSegment("高价值用户")
                .build();

        // Act & Assert
        try {
            entityManager.persistAndFlush(persona2);
            entityManager.clear();
            // 应该抛出异常（唯一约束冲突）
            assertThat(false).as("应该抛出唯一约束异常").isTrue();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("unique");
        }
    }
}
