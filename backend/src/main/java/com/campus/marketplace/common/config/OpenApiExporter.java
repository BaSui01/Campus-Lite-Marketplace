package com.campus.marketplace.common.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.util.Optional.ofNullable;

/**
 * OpenAPI 导出器：配合 {@code --openapi.export.enabled=true} 启动参数，一次性导出最新的 OpenAPI 文档。
 * <p>
 * 使用方式：
 * <pre>
 * mvn -f backend/pom.xml \
 *     -Dspring-boot.run.arguments="--openapi.export.enabled=true,--openapi.export.path=target/openapi-frontend.json" \
 *     spring-boot:run
 * </pre>
 * 导出完成后应用会自动退出，便于在 CI/脚本中串联执行。
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "openapi.export", name = "enabled", havingValue = "true")
public class OpenApiExporter implements ApplicationListener<ApplicationReadyEvent> {

    private final ObjectMapper objectMapper;
    private final ConfigurableApplicationContext applicationContext;
    private final ServletWebServerApplicationContext webServerApplicationContext;
    private final RestTemplateBuilder restTemplateBuilder;

    private static final String DEFAULT_EXPORT_PATH = "target/openapi/openapi.json";
    private static final String DEFAULT_GROUP = "前台接口";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Environment environment = applicationContext.getEnvironment();
        String group = environment.getProperty("openapi.export.group", DEFAULT_GROUP);
        String pathArg = environment.getProperty("openapi.export.path", DEFAULT_EXPORT_PATH);
        String contextPath = ofNullable(environment.getProperty("server.servlet.context-path")).orElse("");

        try {
            int port = webServerApplicationContext.getWebServer().getPort();
            String basePath = normalizeContextPath(contextPath);
            String endpoint = buildDocsEndpoint(port, basePath, group);

            ResponseEntity<String> response = restTemplateBuilder
                    .setConnectTimeout(CONNECT_TIMEOUT)
                    .setReadTimeout(READ_TIMEOUT)
                    .build()
                    .getForEntity(endpoint, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("获取 OpenAPI 文档失败: status=" + response.getStatusCode());
            }

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            Path exportPath = Path.of(pathArg).toAbsolutePath();
            Files.createDirectories(exportPath.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(exportPath.toFile(), jsonNode);

            log.info("✅ OpenAPI 文档已导出至: {}", exportPath.toAbsolutePath());
            scheduleShutdown(0);
        } catch (Exception ex) {
            log.error("❌ OpenAPI 文档导出失败", ex);
            scheduleShutdown(1);
        }
    }

    private String buildDocsEndpoint(int port, String basePath, String group) {
        StringBuilder url = new StringBuilder("http://127.0.0.1:").append(port).append(basePath).append("/v3/api-docs");
        if (StringUtils.hasText(group)) {
            url.append("?group=").append(URLEncoder.encode(group, StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    private String normalizeContextPath(String contextPath) {
        if (!StringUtils.hasText(contextPath) || "/".equals(contextPath)) {
            return "";
        }
        return contextPath.startsWith("/") ? contextPath : "/" + contextPath;
    }

    private void scheduleShutdown(int exitCode) {
        CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS)
                .execute(() -> SpringApplication.exit(applicationContext, () -> exitCode));
    }
}
