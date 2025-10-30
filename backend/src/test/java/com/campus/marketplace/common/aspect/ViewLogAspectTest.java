package com.campus.marketplace.common.aspect;

import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.ViewLogService;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ViewLogAspect 单元测试")
class ViewLogAspectTest {

    @Test
    @DisplayName("已登录用户浏览商品时记录用户名并异步写入日志")
    void logViewGoods_authenticatedUserUsesCurrentUsername() {
        ViewLogService viewLogService = mock(ViewLogService.class);
        ViewLogAspect aspect = new ViewLogAspect(viewLogService);

        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{123L});

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isAuthenticated).thenReturn(true);
            securityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("alice");

            aspect.logViewGoods(joinPoint, null);

            verify(viewLogService, atLeastOnce()).saveAsync(eq("alice"), eq(123L), anyLong());
        }
    }

    @Test
    @DisplayName("无物品ID参数时不记录浏览日志")
    void logViewGoods_withoutGoodsIdDoesNothing() {
        ViewLogService viewLogService = mock(ViewLogService.class);
        ViewLogAspect aspect = new ViewLogAspect(viewLogService);

        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        aspect.logViewGoods(joinPoint, null);

        verify(viewLogService, never()).saveAsync(anyString(), anyLong(), anyLong());
    }
}
