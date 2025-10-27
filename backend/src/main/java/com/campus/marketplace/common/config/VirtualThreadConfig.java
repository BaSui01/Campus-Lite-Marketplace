package com.campus.marketplace.common.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.ProtocolHandler;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Java 21 虚拟线程配置
 * 
 * 配置 Tomcat 使用虚拟线程处理请求
 * 配置异步任务使用虚拟线程执行器
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Slf4j
@Configuration
public class VirtualThreadConfig {

    /**
     * 配置虚拟线程执行器（用于异步任务）
     * 
     * 虚拟线程的优势：
     * 1. 轻量级：创建成本极低，可以创建百万级线程
     * 2. 高并发：适合 I/O 密集型任务（数据库查询、HTTP 请求等）
     * 3. 简化代码：使用同步代码风格处理异步任务
     */
    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        log.info("初始化虚拟线程执行器");
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 配置 Tomcat 使用虚拟线程处理 HTTP 请求
     * 
     * 这样每个 HTTP 请求都会在独立的虚拟线程中处理
     * 大幅提升并发处理能力
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return new TomcatProtocolHandlerCustomizer<ProtocolHandler>() {
            @Override
            public void customize(ProtocolHandler protocolHandler) {
                log.info("配置 Tomcat 使用虚拟线程处理请求");
                protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            }
        };
    }
}
