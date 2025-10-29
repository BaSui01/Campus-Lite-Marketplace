package com.campus.marketplace.common.config;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardThreadExecutor;
import org.apache.coyote.ProtocolHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("VirtualThreadConfig 测试")
class VirtualThreadConfigTest {

    private final VirtualThreadConfig config = new VirtualThreadConfig();

    @Test
    @DisplayName("虚拟线程执行器 Bean 可成功创建")
    void virtualThreadExecutor_shouldCreateExecutor() {
        Executor executor = config.virtualThreadExecutor();
        assertThat(executor).isNotNull();
        executor.execute(() -> {});
    }

    @Test
    @DisplayName("Tomcat 自定义器会注入虚拟线程执行器")
    void protocolHandlerCustomizer_shouldApplyExecutor() {
        Executor executor = command -> {};
        var customizer = config.protocolHandlerVirtualThreadExecutorCustomizer(executor);
        ProtocolHandler protocolHandler = mock(ProtocolHandler.class);

        customizer.customize(protocolHandler);

        verify(protocolHandler).setExecutor(executor);
    }
}
