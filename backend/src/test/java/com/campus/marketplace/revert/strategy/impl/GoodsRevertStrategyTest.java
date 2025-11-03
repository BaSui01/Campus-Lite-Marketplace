package com.campus.marketplace.revert.strategy.impl;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.entity.DataBackup;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.common.enums.AuditEntityType;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.revert.dto.RevertExecutionResult;
import com.campus.marketplace.revert.dto.RevertValidationResult;
import com.campus.marketplace.service.CacheService;
import com.campus.marketplace.service.DataBackupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 商品撤销策略测试 - TDD第1步
 *
 * 测试场景：
 * 1. 验证撤销时限（30天内）
 * 2. 验证删除操作撤销（从备份恢复）
 * 3. 验证更新操作撤销（回滚到旧版本）
 * 4. 验证已撤销操作拒绝
 * 5. 验证备份数据不存在时拒绝
 * 6. 验证恢复后商品状态设为下线
 *
 * @author BaSui 😎
 * @date 2025-11-03
 */
@DisplayName("商品撤销策略测试")
@ExtendWith(MockitoExtension.class)
class GoodsRevertStrategyTest {

    @Mock
    private GoodsRepository goodsRepository;

    @Mock
    private DataBackupService dataBackupService;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private GoodsRevertStrategy goodsRevertStrategy;

    private AuditLog auditLog;
    private Goods goods;
    private DataBackup dataBackup;

    @BeforeEach
    void setUp() {
        // 创建测试用审计日志
        auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setEntityType(AuditEntityType.GOODS);
        auditLog.setEntityId(100L);
        auditLog.setActionType(AuditActionType.DELETE);
        auditLog.setOperatorId(1L);
        auditLog.setRevertDeadline(LocalDateTime.now().plusDays(15)); // 还剩15天
        auditLog.setIsReversible(true);
        auditLog.setRevertedByLogId(null);

        // 创建测试用商品
        goods = new Goods();
        goods.setId(100L);
        goods.setTitle("测试商品");
        goods.setPrice(new BigDecimal("99.99"));
        goods.setStatus(GoodsStatus.APPROVED);

        // 创建测试用备份数据
        dataBackup = new DataBackup();
        dataBackup.setId(1L);
        dataBackup.setEntityType("Goods");
        dataBackup.setEntityId(100L);
        dataBackup.setBackupData("{\"id\":100,\"title\":\"测试商品\",\"price\":99.99}");
    }

    @Test
    @DisplayName("getSupportedEntityType应该返回GOODS")
    void getSupportedEntityType_ShouldReturnGoods() {
        // Act
        String entityType = goodsRevertStrategy.getSupportedEntityType();

        // Assert
        assertThat(entityType).isEqualTo("GOODS");
    }

    // ============ 验证测试 ============

    @Test
    @DisplayName("验证撤销 - 30天内的删除操作应该通过验证")
    void validateRevert_DeleteWithinDeadline_ShouldPass() {
        // Arrange
        when(dataBackupService.findLatestBackup("Goods", 100L))
                .thenReturn(Optional.of(dataBackup));

        // Act
        RevertValidationResult result = goodsRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).contains("验证通过");
        verify(dataBackupService).findLatestBackup("Goods", 100L);
    }

    @Test
    @DisplayName("验证撤销 - 超过30天的操作应该拒绝")
    void validateRevert_ExceedDeadline_ShouldFail() {
        // Arrange
        auditLog.setRevertDeadline(LocalDateTime.now().minusDays(1)); // 已过期

        // Act
        RevertValidationResult result = goodsRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("超过撤销期限");
    }

    @Test
    @DisplayName("验证撤销 - 已经被撤销过的操作应该拒绝")
    void validateRevert_AlreadyReverted_ShouldFail() {
        // Arrange
        auditLog.setRevertedByLogId(999L); // 已被撤销
        auditLog.setRevertedAt(LocalDateTime.now()); // 撤销时间

        // Act
        RevertValidationResult result = goodsRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("已被撤销过");
    }

    @Test
    @DisplayName("验证撤销 - 不支持的操作类型应该拒绝")
    void validateRevert_UnsupportedActionType_ShouldFail() {
        // Arrange
        auditLog.setActionType(AuditActionType.GOODS_APPROVE);

        // Act
        RevertValidationResult result = goodsRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("不支持撤销该类型的操作");
    }

    @Test
    @DisplayName("验证撤销 - 删除操作但备份数据不存在应该拒绝")
    void validateRevert_DeleteButNoBackup_ShouldFail() {
        // Arrange
        when(dataBackupService.findLatestBackup("Goods", 100L))
                .thenReturn(Optional.empty());

        // Act
        RevertValidationResult result = goodsRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("备份数据不存在");
        verify(dataBackupService).findLatestBackup("Goods", 100L);
    }

    @Test
    @DisplayName("验证撤销 - 更新操作但历史数据为空应该拒绝")
    void validateRevert_UpdateButNoOldValue_ShouldFail() {
        // Arrange
        auditLog.setActionType(AuditActionType.UPDATE);
        auditLog.setOldValue(null);

        // Act
        RevertValidationResult result = goodsRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("历史数据不存在");
    }

    @Test
    @DisplayName("验证撤销 - 更新操作且历史数据存在应该通过验证")
    void validateRevert_UpdateWithOldValue_ShouldPass() {
        // Arrange
        auditLog.setActionType(AuditActionType.UPDATE);
        auditLog.setOldValue("{\"title\":\"旧标题\",\"price\":88.88}");

        // Act
        RevertValidationResult result = goodsRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).contains("验证通过");
    }

    // ============ 执行测试 ============

    @Test
    @DisplayName("执行撤销 - 删除操作应该从备份恢复商品")
    void executeRevert_Delete_ShouldRestoreFromBackup() {
        // Arrange
        dataBackup.setBackupVersion(1);

        when(dataBackupService.findLatestBackup("Goods", 100L))
                .thenReturn(Optional.of(dataBackup));
        when(dataBackupService.restoreFromBackup(any(DataBackup.class), eq(Goods.class)))
                .thenReturn(goods);
        when(goodsRepository.save(any(Goods.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RevertExecutionResult result = goodsRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("商品删除已撤销");

        // 验证商品被保存
        verify(goodsRepository).save(argThat(g ->
            g.getId().equals(100L) &&
            g.getStatus() == GoodsStatus.OFFLINE // 恢复后状态为下线
        ));
    }

    @Test
    @DisplayName("执行撤销 - 更新操作应该回滚到旧版本")
    void executeRevert_Update_ShouldRollbackToOldVersion() {
        // Arrange
        auditLog.setActionType(AuditActionType.UPDATE);
        auditLog.setOldValue("{\"title\":\"旧标题\",\"price\":88.88,\"status\":\"APPROVED\"}");

        when(goodsRepository.findById(100L))
                .thenReturn(Optional.of(goods));
        when(goodsRepository.save(any(Goods.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RevertExecutionResult result = goodsRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("商品更新已回滚");

        // 验证商品被保存
        verify(goodsRepository).save(any(Goods.class));
    }

    @Test
    @DisplayName("执行撤销 - 删除操作但备份数据不存在应该失败")
    void executeRevert_DeleteButNoBackup_ShouldFail() {
        // Arrange
        when(dataBackupService.findLatestBackup("Goods", 100L))
                .thenReturn(Optional.empty());

        // Act
        RevertExecutionResult result = goodsRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("备份数据不存在");

        // 验证没有保存操作
        verify(goodsRepository, never()).save(any());
    }

    @Test
    @DisplayName("执行撤销 - 更新操作但商品不存在应该失败")
    void executeRevert_UpdateButGoodsNotFound_ShouldFail() {
        // Arrange
        auditLog.setActionType(AuditActionType.UPDATE);
        auditLog.setOldValue("{\"title\":\"旧标题\"}");

        when(goodsRepository.findById(100L))
                .thenReturn(Optional.empty());

        // Act
        RevertExecutionResult result = goodsRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("商品不存在");

        // 验证没有保存操作
        verify(goodsRepository, never()).save(any());
    }

    @Test
    @DisplayName("执行撤销 - 数据库异常应该返回失败")
    void executeRevert_DatabaseException_ShouldFail() {
        // Arrange
        dataBackup.setBackupVersion(1);

        when(dataBackupService.findLatestBackup("Goods", 100L))
                .thenReturn(Optional.of(dataBackup));
        when(dataBackupService.restoreFromBackup(any(DataBackup.class), eq(Goods.class)))
                .thenReturn(goods);
        when(goodsRepository.save(any(Goods.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // Act
        RevertExecutionResult result = goodsRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("恢复商品失败");
    }

    @Test
    @DisplayName("执行撤销 - 恢复后商品状态应该设为下线")
    void executeRevert_RestoredGoodsShouldBeOffShelf() {
        // Arrange
        dataBackup.setBackupVersion(1);

        when(dataBackupService.findLatestBackup("Goods", 100L))
                .thenReturn(Optional.of(dataBackup));
        when(dataBackupService.restoreFromBackup(any(DataBackup.class), eq(Goods.class)))
                .thenReturn(goods);
        when(goodsRepository.save(any(Goods.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RevertExecutionResult result = goodsRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isTrue();

        // 验证恢复后状态为下线（安全考虑）
        verify(goodsRepository).save(argThat(g ->
            g.getStatus() == GoodsStatus.OFFLINE
        ));
    }

    @Test
    @DisplayName("执行撤销 - 删除操作不再清除缓存（由上层服务处理）")
    void executeRevert_ShouldNotClearCache() {
        // Arrange
        dataBackup.setBackupVersion(1);

        when(dataBackupService.findLatestBackup("Goods", 100L))
                .thenReturn(Optional.of(dataBackup));
        when(dataBackupService.restoreFromBackup(any(DataBackup.class), eq(Goods.class)))
                .thenReturn(goods);
        when(goodsRepository.save(any(Goods.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        goodsRevertStrategy.executeRevert(auditLog, 1L);

        // Assert - 策略层不负责缓存清除，由RevertService处理
        verify(cacheService, never()).delete(anyString());
    }
}
