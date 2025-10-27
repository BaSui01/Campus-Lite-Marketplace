package com.campus.marketplace.common.aspect;

import com.campus.marketplace.service.ViewLogService;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ViewLogAspectTest {

    @Test
    void logViewGoods_shouldInvokeSaveAsync() {
        ViewLogService viewLogService = mock(ViewLogService.class);
        ViewLogAspect aspect = new ViewLogAspect(viewLogService);

        JoinPoint jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{123L});

        aspect.logViewGoods(jp, null);

        verify(viewLogService, atLeastOnce()).saveAsync(anyString(), eq(123L), anyLong());
    }
}
