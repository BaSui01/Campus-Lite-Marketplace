package com.campus.marketplace;

import com.campus.marketplace.common.config.init.DevDatabaseInitializer;
import lombok.extern.slf4j.Slf4j;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 校园轻享集市系统 - 主启动类
 *
 * 基于 Spring Boot 3.x + Java 21 的企业级单体应用
 * 提供二手物品交易、论坛交流、即时通讯等功能
 *
 * @author BaSui
 * @date 2025-11-07
 */
@SpringBootApplication(exclude = {RedissonAutoConfigurationV2.class})  // 🎯 禁用 Redisson 自动配置V2，使用我们的自定义配置！
@Slf4j
@EnableCaching
@EnableAsync
@EnableScheduling
public class MarketplaceApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MarketplaceApplication.class);
        app.addInitializers(new DevDatabaseInitializer());
        app.run(args);
    }

    /**
     * 应用启动完成后打印关键访问信息（端口、上下文路径、Swagger 等）。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String appName = env.getProperty("spring.application.name", "campus-marketplace");
        String[] profiles = env.getActiveProfiles();
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String basePath = (contextPath == null || "/".equals(contextPath)) ? "" : contextPath;

        int port = -1;
        if (event.getApplicationContext() instanceof WebServerApplicationContext wac && wac.getWebServer() != null) {
            port = wac.getWebServer().getPort();
        } else {
            // 回退到 Environment（适用于某些场景，如测试）
            try {
                port = Integer.parseInt(env.getProperty("local.server.port", env.getProperty("server.port", "8080")));
            } catch (NumberFormatException ignored) {
                port = 8080;
            }
        }

        String baseUrl = "http://localhost:" + port + basePath;
        String swaggerUrl = baseUrl + "/swagger-ui/index.html";
        String actuatorBase = env.getProperty("management.endpoints.web.base-path", "/actuator");
        String actuatorUrl = "http://localhost:" + port + actuatorBase;

        log.info("应用已启动 name={} profiles={} port={} contextPath={} baseUrl={} swagger={} actuator={}",
                appName,
                profiles.length == 0 ? "default" : String.join(",", profiles),
                port,
                basePath.isEmpty() ? "/" : basePath,
                baseUrl,
                swaggerUrl,
                actuatorUrl);
    }

    /**
     * Web 服务器初始化完成时，第一时间输出端口（若你只关心端口，这条最稳）。
     */
    @EventListener(WebServerInitializedEvent.class)
    public void onWebServerInitialized(WebServerInitializedEvent event) {
        int port = event.getWebServer() != null ? event.getWebServer().getPort() : -1;
        String contextPath = event.getApplicationContext().getEnvironment().getProperty("server.servlet.context-path", "/");
        log.info("Web 服务器已就绪: port={} contextPath={}", port, (contextPath == null || contextPath.isBlank()) ? "/" : contextPath);
    }
}
