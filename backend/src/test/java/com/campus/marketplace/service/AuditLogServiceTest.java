package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.repository.AuditLogRepository;
import com.campus.marketplace.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("审计日志服务测试")
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @BeforeEach
    void setUp() {
        // 准备工作
    }

    @Test
    @DisplayName("记录审计日志成功")
    void logAction_Success() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(100L);
            return log;
        });

        auditLogService.logAction(
                1L, 
                "testuser", 
                AuditActionType.USER_LOGIN, 
                "User", 
                1L, 
                "用户登录成功", 
                "SUCCESS",
                "127.0.0.1",
                "Mozilla/5.0"
        );

        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());
        AuditLog savedLog = logCaptor.getValue();
        
        assertThat(savedLog.getOperatorId()).isEqualTo(1L);
        assertThat(savedLog.getOperatorName()).isEqualTo("testuser");
        assertThat(savedLog.getActionType()).isEqualTo(AuditActionType.USER_LOGIN);
        assertThat(savedLog.getTargetType()).isEqualTo("User");
        assertThat(savedLog.getTargetId()).isEqualTo(1L);
        assertThat(savedLog.getDetails()).isEqualTo("用户登录成功");
        assertThat(savedLog.getResult()).isEqualTo("SUCCESS");
        assertThat(savedLog.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(savedLog.getUserAgent()).isEqualTo("Mozilla/5.0");
    }

    @Test
    @DisplayName("记录审计日志-匿名操作")
    void logAction_AnonymousUser() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(100L);
            return log;
        });

        auditLogService.logAction(
                null, 
                "匿名用户", 
                AuditActionType.USER_REGISTER, 
                "User", 
                null, 
                "用户注册", 
                "SUCCESS",
                null,
                null
        );

        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());
        AuditLog savedLog = logCaptor.getValue();
        
        assertThat(savedLog.getOperatorId()).isNull();
        assertThat(savedLog.getOperatorName()).isEqualTo("匿名用户");
        assertThat(savedLog.getIpAddress()).isNull();
    }

    @Test
    @DisplayName("异步记录审计日志不影响业务")
    void logActionAsync_DoesNotAffectBusiness() {
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

        assertThatCode(() -> auditLogService.logActionAsync(
                1L, 
                "testuser", 
                AuditActionType.GOODS_CREATE, 
                "Goods", 
                100L, 
                "发布物品", 
                "SUCCESS",
                "127.0.0.1",
                null
        )).doesNotThrowAnyException();
    }
}
