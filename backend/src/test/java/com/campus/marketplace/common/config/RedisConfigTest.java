package com.campus.marketplace.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("RedisConfig 测试")
class RedisConfigTest {

    private final RedisConfig redisConfig = new RedisConfig();

    @Test
    @DisplayName("RedisTemplate 使用字符串键和值 JSON 序列化")
    void redisTemplate_shouldConfigureSerializers() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class, Answers.RETURNS_DEEP_STUBS);

        RedisTemplate<String, Object> template = redisConfig.redisTemplate(factory);

        assertThat(template.getConnectionFactory()).isEqualTo(factory);
        assertThat(template.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(template.getHashKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(template.getValueSerializer()).isInstanceOf(Jackson2JsonRedisSerializer.class);
        assertThat(template.getHashValueSerializer()).isInstanceOf(Jackson2JsonRedisSerializer.class);
    }

    @Test
    @DisplayName("缓存管理器默认 TTL 为 30 分钟并启用 JSON 序列化")
    void cacheManager_shouldUseCustomConfig() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class, Answers.RETURNS_DEEP_STUBS);

        RedisCacheManager cacheManager = redisConfig.cacheManager(factory);
        assertThat(cacheManager).isNotNull();

        var cache = cacheManager.getCache("demo");
        assertThat(cache).isInstanceOf(org.springframework.data.redis.cache.RedisCache.class);
        RedisCacheConfiguration redisConfig =
                ((org.springframework.data.redis.cache.RedisCache) cache).getCacheConfiguration();
        assertThat(redisConfig.getTtl()).isEqualTo(java.time.Duration.ofMinutes(30));
        assertThat(redisConfig.getKeySerializationPair()).isNotNull();
        assertThat(redisConfig.getValueSerializationPair()).isNotNull();
    }
}
