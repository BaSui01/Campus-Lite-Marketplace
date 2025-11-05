package com.campus.marketplace.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis缓存配置
 *
 * 配置缓存过期时间、序列化方式等
 *
 * @author BaSui
 * @date 2025-11-04
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    private final CacheProperties cacheProperties;

    public RedisCacheConfig(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    /**
     * 配置Redis缓存管理器
     */
    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // 创建Jackson序列化器
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(cacheProperties.getDefaultTtl())
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)))
                .disableCachingNullValues();

        // 为不同缓存空间配置不同的过期时间
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 用户缓存 - 15分钟
        cacheConfigurations.put("user", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // 商品列表缓存 - 5分钟
        cacheConfigurations.put("goods:list", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 商品详情缓存 - 30分钟
        cacheConfigurations.put("goods:detail", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 分类缓存 - 1小时
        cacheConfigurations.put("category", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 校区缓存 - 1小时
        cacheConfigurations.put("campus", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 通知模板缓存 - 2小时
        cacheConfigurations.put("notification:template", defaultConfig.entryTtl(Duration.ofHours(2)));

        // 应用配置文件中的自定义配置
        cacheProperties.getTtl().forEach((cacheName, ttl) -> {
            cacheConfigurations.put(cacheName, defaultConfig.entryTtl(ttl));
        });

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}