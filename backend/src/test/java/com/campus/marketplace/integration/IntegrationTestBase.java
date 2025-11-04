package com.campus.marketplace.integration;

import com.campus.marketplace.common.config.TestSecurityConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 集成测试基类：负责启动 Testcontainers（PostgreSQL + Redis），
 * 并将容器连接信息注入 Spring Boot 测试上下文，提供 {@link MockMvc}。
 */
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestSecurityConfig.class)
@ExtendWith(SpringExtension.class)
public abstract class IntegrationTestBase {

    private static final String POSTGRES_IMAGE = System.getProperty(
            "campus.testcontainers.postgres",
            "postgres:16.4-alpine"
    );

    private static final String REDIS_IMAGE = System.getProperty(
            "campus.testcontainers.redis",
            "redis:7.4.1-alpine"
    );

    @SuppressWarnings("resource") // Testcontainers 自动管理容器生命周期
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(
            DockerImageName.parse(POSTGRES_IMAGE)
                    .asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("campus_marketplace_test")
            .withUsername("campus_test")
            .withPassword("campus_test")
            .withReuse(true);

    @SuppressWarnings("resource") // Testcontainers 自动管理容器生命周期
    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(
            DockerImageName.parse(REDIS_IMAGE)
                    .asCompatibleSubstituteFor("redis")
    )
            .withExposedPorts(6379)
            .withReuse(true);

    private static final Path REDISSON_CONFIG_FILE;
    private static final String REDISSON_CONFIG_URI;

    static {
        POSTGRES_CONTAINER.start();
        REDIS_CONTAINER.start();
        REDISSON_CONFIG_FILE = createRedissonConfigFile();
        REDISSON_CONFIG_URI = REDISSON_CONFIG_FILE.toUri().toString();
    }

    @Autowired
    protected MockMvc mockMvc;

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES_CONTAINER::getDriverClassName);
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> 10);
        registry.add("spring.datasource.hikari.minimum-idle", () -> 2);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("app.init.seed", () -> true);

        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
        registry.add("spring.data.redis.redisson.file", () -> REDISSON_CONFIG_URI);
        registry.add("spring.redis.redisson.file", () -> REDISSON_CONFIG_URI);
        registry.add("notifications.webpush.enabled", () -> false);
        registry.add("sms.provider", () -> "logging");
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> 2525);
        registry.add("spring.mail.username", () -> "test");
        registry.add("spring.mail.password", () -> "test");
    }

    private static Path createRedissonConfigFile() {
        try {
            Path temp = Files.createTempFile("redisson-test-", ".yaml");
            String yaml = """
                    singleServerConfig:
                      address: \"redis://%s:%d\"
                      database: 0
                      connectionPoolSize: 16
                      connectionMinimumIdleSize: 2
                      idleConnectionTimeout: 10000
                      connectTimeout: 10000
                      timeout: 3000
                      retryAttempts: 3
                      retryInterval: 1500
                    threads: 4
                    nettyThreads: 4
                    """.formatted(
                    REDIS_CONTAINER.getHost(),
                    REDIS_CONTAINER.getFirstMappedPort()
            );
            Files.writeString(temp, yaml, StandardCharsets.UTF_8);
            temp.toFile().deleteOnExit();
            return temp;
        } catch (IOException ex) {
            throw new IllegalStateException("无法创建测试环境 Redisson 配置文件", ex);
        }
    }
}
