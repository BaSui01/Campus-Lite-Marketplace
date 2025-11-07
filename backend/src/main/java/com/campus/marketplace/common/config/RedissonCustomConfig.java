package com.campus.marketplace.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

/**
 * Redisson 自定义配置类
 *
 * 🎯 解决 Redis 空密码认证问题！
 *
 * 当 REDIS_PASSWORD 为空字符串时，不设置 password 字段，避免 Redisson 执行 AUTH 命令
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */
@Configuration
@ConditionalOnProperty(name = "app.redis.mode", havingValue = "redis", matchIfMissing = true)
public class RedissonCustomConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.database:0}")
    private int database;

    /**
     * 自定义 Redisson 客户端配置
     *
     * 🔧 核心修复：只有当密码非空时才设置 password，避免空密码认证失败
     */
    @Bean(destroyMethod = "shutdown")
    @Primary  // 🎯 优先使用我们的配置！
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + host + ":" + port;

        // 配置单机模式
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                .setConnectionPoolSize(50)
                .setConnectionMinimumIdleSize(10)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(10000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        // 🎯 关键修复：只有当密码非空时才设置 password！
        if (StringUtils.hasText(password)) {
            config.useSingleServer().setPassword(password);
        }

        return Redisson.create(config);
    }

    /**
     * Redisson 连接工厂
     */
    @Bean
    @Primary  // 🎯 优先使用我们的连接工厂！
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redissonClient) {
        return new RedissonConnectionFactory(redissonClient);
    }
}
