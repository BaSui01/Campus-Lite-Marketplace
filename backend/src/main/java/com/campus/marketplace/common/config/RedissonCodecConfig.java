package com.campus.marketplace.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 序列化配置，统一开启 Java Time 模块与多态支持。
 */
@Configuration
@ConditionalOnClass(JsonJacksonCodec.class)
public class RedissonCodecConfig {

    @Bean
    public Codec redissonJsonJacksonCodec(ObjectMapper objectMapper) {
        ObjectMapper mapper = objectMapper.copy();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new JsonJacksonCodec(mapper);
    }
}
