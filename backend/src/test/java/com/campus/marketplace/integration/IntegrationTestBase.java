package com.campus.marketplace.integration;

import org.junit.jupiter.api.BeforeEach;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;

import java.util.stream.Stream;

/**
 * 集成测试基类
 * 
 * 功能：
 * 1. 使用 Testcontainers 启动 PostgreSQL 和 Redis 容器
 * 2. 提供 MockMvc 用于 HTTP 请求测试
 * 3. 每个测试方法使用事务，测试后自动回滚
 * 
 * 使用方式：
 * 继承此类即可获得完整的集成测试环境
 * 
 * @author BaSui 😎
 * @date 2025-10-27
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = com.campus.marketplace.MarketplaceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test-ci")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
@Import(TestCiOverrides.class)
@EntityScan(basePackages = "com.campus.marketplace.common.entity")
@org.junit.jupiter.api.TestInstance(org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired(required = false)
    private org.flywaydb.core.Flyway flyway;

    private static final java.util.concurrent.atomic.AtomicBoolean MIGRATED = new java.util.concurrent.atomic.AtomicBoolean(false);

    @org.junit.jupiter.api.BeforeAll
    void ensureSchema() {
        if (flyway != null && MIGRATED.compareAndSet(false, true)) {
            startInfraIfNecessary();
            flyway.migrate();
        }
    }

    /**
     * PostgreSQL 容器（使用最新的 16 版本）
     */
    @SuppressWarnings("resource")
    @Container
    protected static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("campus_marketplace_test")
            .withUsername("test")
            .withPassword("test")
            .withExposedPorts(5432)
            .withCreateContainerCmdModifier(cmd -> {
                HostConfig hostConfig = cmd.getHostConfig();
                if (hostConfig == null) {
                    hostConfig = new HostConfig();
                }
                hostConfig.withPortBindings(new PortBinding(Ports.Binding.bindPort(54321), new ExposedPort(5432)));
                cmd.withHostConfig(hostConfig);
            }); // 固定映射：容器 5432 -> 宿主 54321，避免与开发库冲突

    /**
     * Redis 容器（使用最新的 7.x 版本）
     */
    @SuppressWarnings("resource")
    @Container
    protected static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            ;

static {
    startInfraIfNecessary();
}

    /**
     * 动态配置数据源和 Redis 连接
     * 
     * 从 Testcontainers 获取容器的动态端口和地址，
     * 覆盖 application-test.yml 中的配置
     */
@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    startInfraIfNecessary();
    registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgresContainer::getUsername);
    registry.add("spring.datasource.password", postgresContainer::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.test.database.replace", () -> "NONE");
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "5");
        // Redis 在 TestCiOverrides 中禁用自动配置
    }

    /**
     * 每个测试方法执行前的初始化
     * 
     * 子类可以覆盖此方法添加自定义初始化逻辑
     */
    @BeforeEach
    protected void setUp() {
        // 子类可选择性覆盖
    }

    /**
     * 确保底层容器已启动，避免属性绑定阶段访问端口映射时报错
     */
    private static void startInfraIfNecessary() {
        Startables.deepStart(
                Stream.of(postgresContainer, redisContainer)
                        .filter(container -> !container.isRunning())
        ).join();
    }
}

@org.springframework.boot.test.context.TestConfiguration
@org.springframework.boot.autoconfigure.ImportAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class
})
class TestCiOverrides {
    @org.springframework.context.annotation.Bean
    @org.springframework.context.annotation.Primary
    public com.campus.marketplace.service.SmsService testSmsService() {
        return (phone, templateCode, params) -> { };
    }

    /**
     * Mock PaymentService for testing 💳
     *
     * 在测试环境下，退款操作直接返回成功，避免调用真实支付宝沙箱API
     */
    @org.springframework.context.annotation.Bean
    @org.springframework.context.annotation.Primary
    public com.campus.marketplace.service.PaymentService testPaymentService(
            com.campus.marketplace.service.impl.AlipayPaymentService alipayPaymentService) {
        return new com.campus.marketplace.service.PaymentService() {
            @Override
            public com.campus.marketplace.common.dto.response.PaymentResponse createPayment(
                    com.campus.marketplace.common.entity.Order order,
                    com.campus.marketplace.common.enums.PaymentMethod paymentMethod) {
                // 使用真实的支付宝沙箱创建支付
                return alipayPaymentService.createPayment(order);
            }

            @Override
            public boolean refund(
                    com.campus.marketplace.common.entity.Order order,
                    java.math.BigDecimal amount,
                    com.campus.marketplace.common.enums.PaymentMethod paymentMethod) {
                // ✅ 测试环境退款直接返回成功（避免调用真实沙箱API）
                return true;
            }
        };
    }

    @org.springframework.context.annotation.Bean
    @org.springframework.core.annotation.Order(0)
    public org.springframework.security.web.SecurityFilterChain testSecurityChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
