package com.campus.marketplace.entity;

import com.campus.marketplace.common.entity.Logistics;
import com.campus.marketplace.common.entity.LogisticsTrackRecord;
import com.campus.marketplace.common.enums.LogisticsCompany;
import com.campus.marketplace.common.enums.LogisticsStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Logistics实体测试类
 *
 * 使用H2内存数据库进行测试（配置在 application-test.yml）
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@DataJpaTest
@EnableJpaAuditing  // 启用 JPA Auditing，自动填充 createdAt 和 updatedAt 字段
@ActiveProfiles("test")
class LogisticsTest {

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
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("物流轨迹应该能正确序列化为JSON")
    void trackRecordsShouldBeSerializedToJson() {
        // Arrange
        LogisticsTrackRecord record1 = new LogisticsTrackRecord();
        record1.setTime(LocalDateTime.now().minusHours(2));
        record1.setLocation("北京市朝阳区");
        record1.setStatusDesc("快件已揽收");
        record1.setOperatorName("顺丰快递员-张三");

        LogisticsTrackRecord record2 = new LogisticsTrackRecord();
        record2.setTime(LocalDateTime.now().minusHours(1));
        record2.setLocation("北京市海淀区");
        record2.setStatusDesc("快件运输中");
        record2.setOperatorName("顺丰快递员-李四");

        List<LogisticsTrackRecord> records = new ArrayList<>();
        records.add(record1);
        records.add(record2);

        Logistics logistics = Logistics.builder()
                .orderId(123L)
                .trackingNumber("SF1234567890")
                .logisticsCompany(LogisticsCompany.SHUNFENG)
                .status(LogisticsStatus.PICKED_UP)
                .trackRecords(records)
                .build();

        // Act
        Logistics saved = entityManager.persistAndFlush(logistics);
        entityManager.clear();
        Logistics found = entityManager.find(Logistics.class, saved.getId());

        // Assert
        assertThat(found.getTrackRecords()).hasSize(2);
        assertThat(found.getTrackRecords().get(0).getLocation()).isEqualTo("北京市朝阳区");
        assertThat(found.getTrackRecords().get(0).getStatusDesc()).isEqualTo("快件已揽收");
        assertThat(found.getTrackRecords().get(1).getLocation()).isEqualTo("北京市海淀区");
        assertThat(found.getTrackRecords().get(1).getStatusDesc()).isEqualTo("快件运输中");
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

        // 等待至少1毫秒，确保时间戳变化
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        saved.setStatus(LogisticsStatus.PICKED_UP);
        entityManager.persistAndFlush(saved);
        entityManager.clear();
        Logistics updated = entityManager.find(Logistics.class, saved.getId());

        // Assert
        assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("物流记录应该支持软删除")
    void logisticsShouldSupportSoftDelete() {
        // Arrange
        Logistics logistics = Logistics.builder()
                .orderId(123L)
                .trackingNumber("SF1234567890")
                .logisticsCompany(LogisticsCompany.SHUNFENG)
                .status(LogisticsStatus.PENDING)
                .build();
        Logistics saved = entityManager.persistAndFlush(logistics);
        Long logisticsId = saved.getId();

        // Act
        saved.markDeleted();
        entityManager.persistAndFlush(saved);
        entityManager.clear();
        Logistics found = entityManager.find(Logistics.class, logisticsId);

        // Assert
        // 由于使用了 @SQLRestriction("deleted = false")，软删除的记录不应被查询到
        // 但在测试中，我们直接使用find方法，所以还是能查到，但deleted字段应该为true
        // find方法会应用@SQLRestriction，所以软删除的记录查不到
        assertThat(found).isNull();  // 软删除后find方法查不到
    }

    @Test
    @DisplayName("同步计数应该正确递增")
    void syncCountShouldIncrementCorrectly() {
        // Arrange
        Logistics logistics = Logistics.builder()
                .orderId(123L)
                .trackingNumber("SF1234567890")
                .logisticsCompany(LogisticsCompany.SHUNFENG)
                .status(LogisticsStatus.PENDING)
                .syncCount(0)
                .build();
        Logistics saved = entityManager.persistAndFlush(logistics);

        // Act
        saved.setSyncCount(saved.getSyncCount() + 1);
        entityManager.persistAndFlush(saved);
        entityManager.clear();
        Logistics updated = entityManager.find(Logistics.class, saved.getId());

        // Assert
        assertThat(updated.getSyncCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("超时标记应该能正确设置")
    void overtimeFlagShouldBeSetCorrectly() {
        // Arrange
        Logistics logistics = Logistics.builder()
                .orderId(123L)
                .trackingNumber("SF1234567890")
                .logisticsCompany(LogisticsCompany.SHUNFENG)
                .status(LogisticsStatus.DELIVERING)
                .estimatedDeliveryTime(LocalDateTime.now().minusDays(1))  // 预计昨天送达
                .isOvertime(false)
                .build();
        Logistics saved = entityManager.persistAndFlush(logistics);

        // Act
        saved.setIsOvertime(true);
        entityManager.persistAndFlush(saved);
        entityManager.clear();
        Logistics updated = entityManager.find(Logistics.class, saved.getId());

        // Assert
        assertThat(updated.getIsOvertime()).isTrue();
    }

    @Test
    @DisplayName("空的物流轨迹列表应该能正确保存")
    void emptyTrackRecordsShouldBeSavedCorrectly() {
        // Arrange
        Logistics logistics = Logistics.builder()
                .orderId(123L)
                .trackingNumber("SF1234567890")
                .logisticsCompany(LogisticsCompany.SHUNFENG)
                .status(LogisticsStatus.PENDING)
                .trackRecords(new ArrayList<>())
                .build();

        // Act
        Logistics saved = entityManager.persistAndFlush(logistics);
        entityManager.clear();
        Logistics found = entityManager.find(Logistics.class, saved.getId());

        // Assert
        assertThat(found.getTrackRecords()).isEmpty();
    }

    @Test
    @DisplayName("物流记录的所有字段应该能正确保存和读取")
    void allFieldsShouldBeSavedAndLoadedCorrectly() {
        // Arrange
        LocalDateTime estimatedTime = LocalDateTime.now().plusDays(3);
        LocalDateTime actualTime = LocalDateTime.now();
        LocalDateTime lastSyncTime = LocalDateTime.now().minusHours(1);

        Logistics logistics = Logistics.builder()
                .orderId(456L)
                .trackingNumber("ZTO9876543210")
                .logisticsCompany(LogisticsCompany.ZHONGTONG)
                .status(LogisticsStatus.DELIVERED)
                .currentLocation("上海市浦东新区")
                .estimatedDeliveryTime(estimatedTime)
                .actualDeliveryTime(actualTime)
                .isOvertime(false)
                .syncCount(5)
                .lastSyncTime(lastSyncTime)
                .build();

        // Act
        Logistics saved = entityManager.persistAndFlush(logistics);
        entityManager.clear();
        Logistics found = entityManager.find(Logistics.class, saved.getId());

        // Assert
        assertThat(found.getOrderId()).isEqualTo(456L);
        assertThat(found.getTrackingNumber()).isEqualTo("ZTO9876543210");
        assertThat(found.getLogisticsCompany()).isEqualTo(LogisticsCompany.ZHONGTONG);
        assertThat(found.getStatus()).isEqualTo(LogisticsStatus.DELIVERED);
        assertThat(found.getCurrentLocation()).isEqualTo("上海市浦东新区");
        assertThat(found.getEstimatedDeliveryTime()).isEqualToIgnoringNanos(estimatedTime);
        assertThat(found.getActualDeliveryTime()).isEqualToIgnoringNanos(actualTime);
        assertThat(found.getIsOvertime()).isFalse();
        assertThat(found.getSyncCount()).isEqualTo(5);
        assertThat(found.getLastSyncTime()).isEqualToIgnoringNanos(lastSyncTime);
    }
}
