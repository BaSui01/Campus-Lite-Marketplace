package com.campus.marketplace.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 配置类
 *
 * 配置 Redis 序列化、缓存管理器和 RedisTemplate
 *
 * @author BaSui
 * @date 2025-10-25
 */
@Configuration
@ConditionalOnProperty(name = "app.redis.mode", havingValue = "redis", matchIfMissing = true)
public class RedisConfig {

    /**
     * 配置 RedisTemplate
     * 使用 Jackson2JsonRedisSerializer 进行序列化
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 配置 ObjectMapper
        ObjectMapper mapper = createRedisObjectMapper();

        // 使用 Jackson2JsonRedisSerializer 来序列化和反序列化 redis 的 value 值
        // 使用新的构造函数,直接传入 ObjectMapper
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        // 使用 StringRedisSerializer 来序列化和反序列化 redis 的 key 值
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // key 采用 String 的序列化方式
        template.setKeySerializer(stringSerializer);
        // hash 的 key 也采用 String 的序列化方式
        template.setHashKeySerializer(stringSerializer);
        // value 序列化方式采用 jackson
        template.setValueSerializer(serializer);
        // hash 的 value 序列化方式采用 jackson
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置缓存管理器
     * 设置默认缓存过期时间为 30 分钟
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 配置 ObjectMapper
        ObjectMapper mapper = createRedisObjectMapper();

        // 配置序列化(使用新的构造函数)
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        // 配置缓存
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))  // 默认缓存 30 分钟
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();  // 不缓存 null 值

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * 构建 Redis 专用 ObjectMapper,支持 Java Time 类型序列化。
     * 🛠️ 配置 PageImpl 等 Spring Data 类型的混入,防止反序列化失败
     */
    private ObjectMapper createRedisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 🔧 修复 PageImpl 反序列化问题: 添加 PageImpl 的 MixIn
        mapper.addMixIn(org.springframework.data.domain.PageImpl.class, PageImplMixin.class);
        // 🔧 修复 PageRequest 反序列化问题: 添加 PageRequest 的 MixIn
        mapper.addMixIn(org.springframework.data.domain.PageRequest.class, PageRequestMixin.class);
        // 🔧 修复 Sort 反序列化问题: 添加 Sort 的 MixIn
        mapper.addMixIn(org.springframework.data.domain.Sort.class, SortMixin.class);

        return mapper;
    }

    /**
     * PageImpl 的 Jackson MixIn 类,提供反序列化所需的构造函数信息
     * 🎯 解决 "Cannot construct instance of PageImpl (no Creators)" 错误
     */
    private abstract static class PageImplMixin {
        @com.fasterxml.jackson.annotation.JsonCreator
        PageImplMixin(
                @com.fasterxml.jackson.annotation.JsonProperty("content") java.util.List<?> content,
                @com.fasterxml.jackson.annotation.JsonProperty("pageable") org.springframework.data.domain.Pageable pageable,
                @com.fasterxml.jackson.annotation.JsonProperty("total") long total) {
        }
    }

    /**
     * PageRequest 的 Jackson MixIn 类,提供反序列化所需的构造函数信息
     * 🎯 解决 "Cannot construct instance of PageRequest (no Creators)" 错误
     */
    private abstract static class PageRequestMixin {
        @com.fasterxml.jackson.annotation.JsonCreator
        PageRequestMixin(
                @com.fasterxml.jackson.annotation.JsonProperty("page") int page,
                @com.fasterxml.jackson.annotation.JsonProperty("size") int size,
                @com.fasterxml.jackson.annotation.JsonProperty("sort") org.springframework.data.domain.Sort sort) {
        }
    }

    /**
     * Sort 的 Jackson MixIn 类,提供反序列化所需的构造函数信息
     * 🎯 解决 "Cannot construct instance of Sort (no Creators)" 错误
     */
    private abstract static class SortMixin {
        @com.fasterxml.jackson.annotation.JsonCreator
        SortMixin(
                @com.fasterxml.jackson.annotation.JsonProperty("orders") java.util.List<org.springframework.data.domain.Sort.Order> orders) {
        }
    }
}
