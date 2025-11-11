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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
        // 🎯 创建支持 PageImpl 的 ObjectMapper
        ObjectMapper objectMapper = createRedisObjectMapper();
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

    /**
     * 🎯 创建 Redis 专用 ObjectMapper，支持 PageImpl 序列化
     *
     * 解决 "Cannot construct instance of PageImpl" 错误
     */
    private ObjectMapper createRedisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // ✅ 关键修复：忽略未知字段，避免旧缓存字段导致反序列化报错（如 Sort.empty/sorted/unsorted 等）
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 🔧 修复 PageImpl 反序列化问题
        mapper.addMixIn(org.springframework.data.domain.PageImpl.class, PageImplMixin.class);
        mapper.addMixIn(org.springframework.data.domain.PageRequest.class, PageRequestMixin.class);
        mapper.addMixIn(org.springframework.data.domain.Sort.class, SortMixin.class);
        // 🎯 修复 Sort.Order 反序列化问题（关键修复！）
        mapper.addMixIn(org.springframework.data.domain.Sort.Order.class, SortOrderMixin.class);

        return mapper;
    }

    /**
     * PageImpl 的 Jackson MixIn 类
     */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private abstract static class PageImplMixin {
        @JsonCreator
        PageImplMixin(
                @JsonProperty("content") java.util.List<?> content,
                @JsonProperty("pageable") org.springframework.data.domain.Pageable pageable,
                @JsonProperty("total") long total) {
        }
    }

    /**
     * PageRequest 的 Jackson MixIn 类
     */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private abstract static class PageRequestMixin {
        @JsonCreator
        PageRequestMixin(
                @JsonProperty("page") int page,
                @JsonProperty("size") int size,
                @JsonProperty("sort") org.springframework.data.domain.Sort sort) {
        }
    }

    /**
     * Sort 的 Jackson MixIn 类
     */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private abstract static class SortMixin {
        @JsonCreator
        SortMixin(
                @JsonProperty("orders") java.util.List<org.springframework.data.domain.Sort.Order> orders) {
        }
    }

    /**
     * Sort.Order 的 Jackson MixIn 类
     * 🎯 解决 "Cannot construct instance of Sort$Order (no Creators)" 错误
     * 🔧 添加 @JsonIgnoreProperties 忽略未知字段（例如旧版本缓存中的 "ascending"）
     */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private abstract static class SortOrderMixin {
        @JsonCreator
        SortOrderMixin(
                @JsonProperty("direction") org.springframework.data.domain.Sort.Direction direction,
                @JsonProperty("property") String property,
                @JsonProperty("ignoreCase") boolean ignoreCase,
                @JsonProperty("nullHandling") org.springframework.data.domain.Sort.NullHandling nullHandling) {
        }
    }
}
