package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.DataBackup;
import com.campus.marketplace.repository.DataBackupRepository;
import com.campus.marketplace.service.impl.DataBackupServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 数据备份服务测试 - TDD第1步：定义预期行为
 * 
 * 测试策略：
 * 1. 数据备份创建：验证备份数据完整性和过期时间设置
 * 2. 备份数据查询：验证根据实体类型和ID查询最新备份
 * 3. 备份数据恢复：验证备份数据的反序列化和完整性验证
 * 4. 备份生命周期：验证过期备份的自动清理机制
 * 
 * @author BaSui  
 * @date 2025-11-03
 */
@DisplayName("数据备份服务测试")
@ExtendWith(MockitoExtension.class)
class DataBackupServiceTest {

    @Mock
    private DataBackupRepository dataBackupRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DataBackupServiceImpl dataBackupService;

    @Test
    @DisplayName("应该成功创建数据备份")
    void shouldCreateDataBackupSuccessfully() {
        // Given - 准备测试数据
        String entityType = "Goods";
        Long entityId = 1001L;
        String backupData = "{\"id\":1001,\"name\":\"测试商品\",\"price\":99.99}";
        int expireDays = 30;

        DataBackup expectedBackup = DataBackup.builder()
                .entityType(entityType)
                .entityId(entityId)
                .backupData(backupData)
                .backupVersion(1)
                .expireAt(LocalDateTime.now().plusDays(expireDays))
                .isActive(true)
                .build();

        when(dataBackupRepository.findMaxVersionByEntityTypeAndId(entityType, entityId))
                .thenReturn(Optional.empty());
        when(dataBackupRepository.save(any(DataBackup.class))).thenReturn(expectedBackup);

        // When - 执行备份创建
        DataBackup actualBackup = dataBackupService.createBackup(entityType, entityId, backupData, expireDays);

        // Then - 验证结果
        assertThat(actualBackup).isNotNull();
        assertThat(actualBackup.getEntityType()).isEqualTo(entityType);
        assertThat(actualBackup.getEntityId()).isEqualTo(entityId);
        assertThat(actualBackup.getBackupData()).isEqualTo(backupData);
        assertThat(actualBackup.getIsActive()).isTrue();
        verify(dataBackupRepository, times(1)).save(any(DataBackup.class));
    }

    @Test
    @DisplayName("应该根据实体类型和ID查询最新备份")
    void shouldFindLatestBackupByEntityTypeAndId() {
        // Given
        String entityType = "Order";
        Long entityId = 2001L;
        DataBackup expectedBackup = DataBackup.builder()
                .entityType(entityType)
                .entityId(entityId)
                .backupData("{\"id\":2001,\"status\":\"PAID\"}")
                .backupVersion(3)
                .isActive(true)
                .build();
        expectedBackup.setId(100L); // 手动设置ID

        when(dataBackupRepository.findLatestByEntityTypeAndId(entityType, entityId))
                .thenReturn(Optional.of(expectedBackup));

        // When
        Optional<DataBackup> actualBackup = dataBackupService.findLatestBackup(entityType, entityId);

        // Then
        assertThat(actualBackup).isPresent();
        assertThat(actualBackup.get().getEntityType()).isEqualTo(entityType);
        assertThat(actualBackup.get().getEntityId()).isEqualTo(entityId);
        assertThat(actualBackup.get().getBackupVersion()).isEqualTo(3);
        verify(dataBackupRepository, times(1)).findLatestByEntityTypeAndId(entityType, entityId);
    }

    @Test
    @DisplayName("应该验证备份数据完整性")
    void shouldValidateBackupDataIntegrity() throws Exception {
        // Given
        String backupData = "{\"id\":1001,\"name\":\"商品\",\"checksum\":\"abc123\"}";
        when(objectMapper.readTree(backupData)).thenReturn(null); // Mock JSON解析成功
        
        // When
        boolean isValid = dataBackupService.validateBackupIntegrity(backupData);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("应该清理过期的备份数据")
    void shouldCleanupExpiredBackups() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        when(dataBackupRepository.countByExpireAtBeforeAndIsActiveTrue(any(LocalDateTime.class)))
                .thenReturn(5);
        when(dataBackupRepository.deleteByExpireAtBeforeAndIsActiveTrue(any(LocalDateTime.class)))
                .thenReturn(5);

        // When
        int deletedCount = dataBackupService.cleanupExpiredBackups();

        // Then
        assertThat(deletedCount).isEqualTo(5);
        verify(dataBackupRepository, times(1)).deleteByExpireAtBeforeAndIsActiveTrue(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("应该获取下一个备份版本号")
    void shouldGetNextBackupVersion() {
        // Given
        String entityType = "User";
        Long entityId = 3001L;
        when(dataBackupRepository.findMaxVersionByEntityTypeAndId(entityType, entityId))
                .thenReturn(Optional.of(5));

        // When
        int nextVersion = dataBackupService.getNextBackupVersion(entityType, entityId);

        // Then
        assertThat(nextVersion).isEqualTo(6);
        verify(dataBackupRepository, times(1)).findMaxVersionByEntityTypeAndId(entityType, entityId);
    }

    @Test
    @DisplayName("首次备份版本号应该为1")
    void shouldReturnVersionOneForFirstBackup() {
        // Given
        String entityType = "Goods";
        Long entityId = 4001L;
        when(dataBackupRepository.findMaxVersionByEntityTypeAndId(entityType, entityId))
                .thenReturn(Optional.empty());

        // When
        int nextVersion = dataBackupService.getNextBackupVersion(entityType, entityId);

        // Then
        assertThat(nextVersion).isEqualTo(1);
    }
}
