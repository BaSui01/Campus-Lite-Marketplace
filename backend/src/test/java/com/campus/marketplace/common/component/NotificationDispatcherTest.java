package com.campus.marketplace.common.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationDispatcher 测试")
class NotificationDispatcherTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ListOperations<String, Object> listOperations;

    private NotificationDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new NotificationDispatcher(redisTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(dispatcher, "queueKey", "notifications:test");
    }

    @Test
    @DisplayName("入队通知时写入 Redis 列表")
    void enqueueTemplate_success() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);

        dispatcher.enqueueTemplate(
                1L,
                "WELCOME",
                Map.of("name", "Alice"),
                "SYSTEM",
                99L,
                "ORDER",
                "/orders/99"
        );

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(listOperations).leftPush(eq("notifications:test"), captor.capture());
        assertThat(captor.getValue()).contains("\"templateCode\":\"WELCOME\"");
    }

    @Test
    @DisplayName("序列化失败时捕获异常不写入 Redis")
    void enqueueTemplate_whenSerializationFails() throws Exception {
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any())).thenThrow(new RuntimeException("boom"));
        dispatcher = new NotificationDispatcher(redisTemplate, mapper);
        ReflectionTestUtils.setField(dispatcher, "queueKey", "notifications:test");

        dispatcher.enqueueTemplate(1L, "FAIL", Map.of(), "SYSTEM", null, null, null);

        verify(listOperations, never()).leftPush(anyString(), any());
    }
}
