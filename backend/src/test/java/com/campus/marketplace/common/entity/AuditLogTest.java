package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.common.enums.AuditEntityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("审计日志实体测试")
class AuditLogTest {

    @Test
    @DisplayName("新创建的审计日志应该有正确的默认值")
    void newAuditLogShouldHaveCorrectDefaults() {
        // Arrange
        AuditLog auditLog = AuditLog.builder()
            .operatorId(123L)
            .operatorName("testuser")
            .actionType(AuditActionType.USER_LOGIN)
            .targetType("User")
            .targetId(456L)
            .details("用户登录成功")
            .result("SUCCESS")
            .ipAddress("127.0.0.1")
            .userAgent("Mozilla/5.0")
            .build();
        
        // Assert
        assertThat(auditLog.getOperatorId()).isEqualTo(123L);
        assertThat(auditLog.getOperatorName()).isEqualTo("testuser");
        assertThat(auditLog.getActionType()).isEqualTo(AuditActionType.USER_LOGIN);
        assertThat(auditLog.getTargetType()).isEqualTo("User");
        assertThat(auditLog.getTargetId()).isEqualTo(456L);
        assertThat(auditLog.getDetails()).isEqualTo("用户登录成功");
        assertThat(auditLog.getResult()).isEqualTo("SUCCESS");
        assertThat(auditLog.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(auditLog.getUserAgent()).isEqualTo("Mozilla/5.0");
    }

    @Test
    @DisplayName("应该支持存储实体变更前后的数据")
    void shouldSupportEntityChangeTracking() {
        // 这个测试会失败，因为当前的AuditLog实体缺少oldValue和newValue字段
        // 根据TDD原则，我们先写失败的测试，然后再实现功能
        
        // Arrange & Act
        AuditLog auditLog = AuditLog.builder()
            .operatorId(123L)
            .actionType(AuditActionType.USER_BAN)
            .targetType("User")
            .targetId(456L)
            .oldValue("{\"status\":\"ACTIVE\"}")
            .newValue("{\"status\":\"BANNED\"}")
            .entityName("User")
            .entityType(AuditEntityType.USER)
            .isReversible(true)
            .build();
        
        // Assert
        assertThat(auditLog.getOldValue()).isEqualTo("{\"status\":\"ACTIVE\"}");
        assertThat(auditLog.getNewValue()).isEqualTo("{\"status\":\"BANNED\"}");
        assertThat(auditLog.getEntityName()).isEqualTo("User");
        assertThat(auditLog.getEntityType()).isEqualTo(AuditEntityType.USER);
        assertThat(auditLog.getIsReversible()).isTrue();
    }

    @Test
    @DisplayName("可撤销操作应该正确标记")
    void reversibleOperationShouldBeMarkedCorrectly() {
        // 这个测试会失败，因为当前缺少isReversible字段
        
        // Arrange & Act
        AuditLog reversibleLog = AuditLog.builder()
            .actionType(AuditActionType.USER_BAN)
            .isReversible(true)
            .build();
            
        AuditLog irreversibleLog = AuditLog.builder()
            .actionType(AuditActionType.USER_LOGIN)
            .isReversible(false)
            .build();
        
        // Assert
        assertThat(reversibleLog.getIsReversible()).isTrue();
        assertThat(irreversibleLog.getIsReversible()).isFalse();
    }

    @Test
    @DisplayName("审计日志应该包含完整的实体信息")
    void shouldContainCompleteEntityInfo() {
        // Act
        AuditLog auditLog = AuditLog.builder()
            .operatorId(123L)
            .actionType(AuditActionType.USER_LOGIN)
            .targetType("User")
            .targetId(456L)
            .build();
        
        // Assert
        assertThat(auditLog.getOperatorId()).isEqualTo(123L);
        assertThat(auditLog.getActionType()).isEqualTo(AuditActionType.USER_LOGIN);
        assertThat(auditLog.getTargetType()).isEqualTo("User");
        assertThat(auditLog.getTargetId()).isEqualTo(456L);
    }

    @Test
    @DisplayName("应该支持批量操作的审计记录")
    void shouldSupportBatchOperationAudit() {
        // Arrange & Act
        AuditLog batchLog = AuditLog.builder()
            .operatorId(123L)
            .actionType(AuditActionType.GOODS_DELETE)
            .targetType("Goods")
            .targetIds("100,101,102,103")  // 批量操作的ID列表
            .details("批量删除4个商品")
            .result("SUCCESS")
            .isReversible(false)
            .build();
        
        // Assert
        assertThat(batchLog.getTargetIds()).isEqualTo("100,101,102,103");
        assertThat(batchLog.getDetails()).contains("4个商品");
        assertThat(batchLog.getIsReversible()).isFalse();
    }
}
