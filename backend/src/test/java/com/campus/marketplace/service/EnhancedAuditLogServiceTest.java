package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.common.enums.AuditEntityType;
import com.campus.marketplace.repository.AuditLogRepository;
import com.campus.marketplace.service.impl.AuditLogServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("增强审计日志服务测试")
class EnhancedAuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @BeforeEach
    void setUp() {
        // 准备工作
    }

    @Test
    @DisplayName("应该能记录实体变更前后数据")
    void shouldRecordEntityChangeData() throws Exception {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(100L);
            return log;
        });
        
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");
        
        Map<String, Object> oldData = Map.of("status", "ACTIVE");
        Map<String, Object> newData = Map.of("status", "BANNED");
        
        // Act
        auditLogService.logEntityChange(1L, "testuser", AuditActionType.USER_BAN, 
            "User", 1L, oldData, newData);
        
        // Assert
        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());
        AuditLog savedLog = logCaptor.getValue();
        
        assertThat(savedLog.getOperatorId()).isEqualTo(1L);
        assertThat(savedLog.getOperatorName()).isEqualTo("testuser");
        assertThat(savedLog.getActionType()).isEqualTo(AuditActionType.USER_BAN);
        assertThat(savedLog.getEntityName()).isEqualTo("User");
        assertThat(savedLog.getEntityId()).isEqualTo(1L);
        assertThat(savedLog.getEntityType()).isEqualTo(AuditEntityType.USER);
        assertThat(savedLog.getOldValue()).isEqualTo("{\"test\":\"data\"}");
        assertThat(savedLog.getNewValue()).isEqualTo("{\"test\":\"data\"}");
        assertThat(savedLog.getIsReversible()).isTrue();
    }

    @Test
    @DisplayName("应该能批量操作审计记录")
    void shouldRecordBatchOperationAudit() throws Exception {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(100L);
            return log;
        });
        
        // Act
        auditLogService.logBatchOperation(1L, "admin", AuditActionType.GOODS_DELETE,
            "Goods", "100,101,102", "批量删除3个商品", false);
        
        // Assert
        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());
        AuditLog savedLog = logCaptor.getValue();
        
        assertThat(savedLog.getOperatorId()).isEqualTo(1L);
        assertThat(savedLog.getOperatorName()).isEqualTo("admin");
        assertThat(savedLog.getActionType()).isEqualTo(AuditActionType.GOODS_DELETE);
        assertThat(savedLog.getTargetType()).isEqualTo("Goods");
        assertThat(savedLog.getTargetIds()).isEqualTo("100,101,102");
        assertThat(savedLog.getDetails()).isEqualTo("批量删除3个商品");
        assertThat(savedLog.getIsReversible()).isFalse();
    }

    @Test
    @DisplayName("应该能标记可撤销操作")
    void shouldMarkReversibleOperations() throws Exception {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(100L);
            return log;
        });
        
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");
        
        Map<String, Object> oldData = Map.of("status", "ACTIVE");
        Map<String, Object> newData = Map.of("status", "BANNED");
        
        // Act
        auditLogService.logReversibleAction(1L, "admin", AuditActionType.USER_BAN,
            "User", 1L, oldData, newData);
        
        // Assert
        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());
        AuditLog savedLog = logCaptor.getValue();
        
        assertThat(savedLog.getOperatorId()).isEqualTo(1L);
        assertThat(savedLog.getOperatorName()).isEqualTo("admin");
        assertThat(savedLog.getActionType()).isEqualTo(AuditActionType.USER_BAN);
        assertThat(savedLog.getEntityName()).isEqualTo("User");
        assertThat(savedLog.getEntityId()).isEqualTo(1L);
        assertThat(savedLog.getEntityType()).isEqualTo(AuditEntityType.USER);
        assertThat(savedLog.getIsReversible()).isTrue();
        assertThat(savedLog.getDetails()).contains("可撤销操作");
    }
}
