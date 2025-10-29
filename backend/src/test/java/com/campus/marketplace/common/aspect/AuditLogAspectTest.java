package com.campus.marketplace.common.aspect;

import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogAspect 单元测试")
class AuditLogAspectTest {

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private AuditLogAspect aspect;

    @AfterEach
    void cleanContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("用户封禁成功时记录审计日志")
    void auditUserBan_successful() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{123L, "违规发布"});
        when(joinPoint.proceed()).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "10.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("admin");

            aspect.auditUserBan(joinPoint);
        }

        verify(auditLogService).logActionAsync(
                isNull(),
                eq("admin"),
                eq(AuditActionType.USER_BAN),
                eq("User"),
                eq(123L),
                eq("封禁用户: 违规发布"),
                eq("SUCCESS"),
                eq("10.0.0.1"),
                eq("JUnit")
        );
    }

    @Test
    @DisplayName("用户封禁失败时记录失败原因并抛出异常")
    void auditUserBan_failure() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{456L, "恶意刷单"});
        doThrow(new RuntimeException("数据库异常")).when(joinPoint).proceed();

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("root");

            assertThatThrownBy(() -> aspect.auditUserBan(joinPoint))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("数据库异常");
        }

        ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditLogService, times(1)).logActionAsync(
                isNull(),
                eq("root"),
                eq(AuditActionType.USER_BAN),
                eq("User"),
                eq(456L),
                detailCaptor.capture(),
                eq("FAILED"),
                eq("127.0.0.1"),
                isNull()
        );
        assertThat(detailCaptor.getValue()).contains("数据库异常");
    }

    @Test
    @DisplayName("物品审核通过时记录审计日志")
    void auditGoodsApprove_successful() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{789L, true});
        when(joinPoint.proceed()).thenReturn(null);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("manager");

            aspect.auditGoodsApprove(joinPoint);
        }

        verify(auditLogService).logActionAsync(
                isNull(),
                eq("manager"),
                eq(AuditActionType.GOODS_APPROVE),
                eq("Goods"),
                eq(789L),
                eq("审核通过"),
                eq("SUCCESS"),
                eq("127.0.0.1"),
                isNull()
        );
    }

    @Test
    @DisplayName("举报处理失败时记录失败详情")
    void auditReportHandle_failure() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{999L, false});
        doThrow(new RuntimeException("外部服务异常")).when(joinPoint).proceed();

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("auditor");

            assertThatThrownBy(() -> aspect.auditReportHandle(joinPoint))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("外部服务异常");
        }

        ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditLogService).logActionAsync(
                isNull(),
                eq("auditor"),
                eq(AuditActionType.REPORT_HANDLE),
                eq("Report"),
                eq(999L),
                detailCaptor.capture(),
                eq("FAILED"),
                eq("127.0.0.1"),
                isNull()
        );
        assertThat(detailCaptor.getValue()).contains("外部服务异常");
    }
}
