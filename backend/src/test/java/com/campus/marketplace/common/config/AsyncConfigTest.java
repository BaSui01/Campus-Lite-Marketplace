package com.campus.marketplace.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("AsyncConfig 测试")
class AsyncConfigTest {

    private final Executor executor = mock(Executor.class);
    private final AsyncConfig asyncConfig = new AsyncConfig(executor);

    @Test
    @DisplayName("getAsyncExecutor 返回注入的执行器")
    void getAsyncExecutor_shouldReturnInjectedExecutor() {
        assertThat(asyncConfig.getAsyncExecutor()).isSameAs(executor);
    }

    @Test
    @DisplayName("异步异常处理器可安全处理异常")
    void getAsyncUncaughtExceptionHandler_shouldHandle() throws Exception {
        AsyncUncaughtExceptionHandler handler = asyncConfig.getAsyncUncaughtExceptionHandler();
        Method method = AsyncConfigTest.class.getDeclaredMethod("dummyMethod");
        handler.handleUncaughtException(new RuntimeException("error"), method, new Object[]{"arg"});
        // 没有抛出异常即视为通过
    }

    private void dummyMethod() {
        // no-op
    }
}
