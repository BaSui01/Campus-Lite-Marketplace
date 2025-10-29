package com.campus.marketplace.common.component;

import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.service.AuditLogService;
import com.campus.marketplace.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationQueueConsumer 测试")
class NotificationQueueConsumerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ListOperations<String, Object> listOperations;
    @Mock
    private ZSetOperations<String, Object> zSetOperations;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AuditLogService auditLogService;

    private NotificationQueueConsumer consumer;
    private StubObjectMapper objectMapper;

    private static final String QUEUE_KEY = "notifications:test:queue";
    private static final String RETRY_KEY = "notifications:test:retry";
    private static final Map<String, Object> PARAMS = Map.of("foo", "bar");

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        objectMapper = new StubObjectMapper();

        consumer = new NotificationQueueConsumer(redisTemplate, objectMapper, notificationService, auditLogService);
        ReflectionTestUtils.setField(consumer, "queueKey", QUEUE_KEY);
        ReflectionTestUtils.setField(consumer, "retryKey", RETRY_KEY);
        ReflectionTestUtils.setField(consumer, "batchSize", 5);
        ReflectionTestUtils.setField(consumer, "maxAttempts", 3);
    }

    private String registerJob(int attempts) {
        NotificationDispatcher.Job job = new NotificationDispatcher.Job(
                10L,
                "TPL",
                PARAMS,
                NotificationType.SYSTEM_ANNOUNCEMENT.name(),
                99L,
                "ORDER",
                "/orders/99",
                attempts
        );
        return objectMapper.register(job);
    }

    @Test
    @DisplayName("consume 可批量处理通知")
    void consume_shouldProcessEntries() {
        String payload = registerJob(0);
        when(listOperations.rightPop(QUEUE_KEY)).thenReturn(payload, (String) null);

        consumer.consume();

        verify(notificationService).sendTemplateNotification(
                eq(10L),
                eq("TPL"),
                eq(PARAMS),
                eq(NotificationType.SYSTEM_ANNOUNCEMENT),
                eq(99L),
                eq("ORDER"),
                eq("/orders/99")
        );
    }

    @Test
    @DisplayName("发送失败时加入重试集合")
    void handleFailure_shouldEnqueueRetry() {
        ReflectionTestUtils.setField(consumer, "maxAttempts", 5);
        String payload = registerJob(0);
        when(listOperations.rightPop(QUEUE_KEY)).thenReturn(payload, (String) null);
        doThrow(new RuntimeException("boom")).when(notificationService)
                .sendTemplateNotification(anyLong(), anyString(), anyMap(), any(), any(), anyString(), anyString());

        consumer.consume();

        verify(zSetOperations).add(eq(RETRY_KEY), anyString(), anyDouble());
    }

    @Test
    @DisplayName("重试次数超限时记录审计日志并丢弃")
    void enqueueRetry_shouldDropWhenExceedMax() {
        ReflectionTestUtils.setField(consumer, "maxAttempts", 1);
        String payload = registerJob(1);

        ReflectionTestUtils.invokeMethod(consumer, "enqueueRetry", payload);

        verify(zSetOperations, never()).add(anyString(), any(), anyDouble());
        verify(auditLogService).logActionAsync(
                isNull(), eq("system"), eq(AuditActionType.NOTIFICATION_FAIL),
                eq("NOTIFICATION"), eq(99L), contains("TPL"), eq("FAILED"),
                isNull(), isNull()
        );
    }

    @Test
    @DisplayName("scheduleRetry 会将到期任务回填队列")
    void scheduleRetry_shouldMoveDueEntries() {
        String payload = registerJob(2);
        when(zSetOperations.rangeByScore(eq(RETRY_KEY), anyDouble(), anyDouble()))
                .thenReturn(Set.of(payload));

        consumer.scheduleRetry();

        verify(listOperations).leftPush(QUEUE_KEY, payload);
        verify(zSetOperations).remove(RETRY_KEY, payload);
    }

    @Test
    @DisplayName("consume 发生异常时捕获并继续")
    void consume_shouldHandleRedisException() {
        when(listOperations.rightPop(QUEUE_KEY)).thenThrow(new RuntimeException("redis down"));

        consumer.consume();

        verify(notificationService, never()).sendTemplateNotification(anyLong(), anyString(), anyMap(), any(), any(), anyString(), anyString());
    }

    /**
     * 测试专用的 ObjectMapper Stub：利用内存 Map 完成序列化与反序列化。
     */
    private static class StubObjectMapper extends ObjectMapper {
        private final Map<String, NotificationDispatcher.Job> storage = new HashMap<>();
        private int counter = 0;

        @Override
        public <T> T readValue(String content, Class<T> valueType) {
            NotificationDispatcher.Job job = storage.get(content);
            if (job == null) {
                throw new IllegalArgumentException("unknown payload: " + content);
            }
            return valueType.cast(job);
        }

        @Override
        public String writeValueAsString(Object value) {
            NotificationDispatcher.Job job = (NotificationDispatcher.Job) value;
            String token = "payload-" + job.getAttempts() + "-" + counter++;
            storage.put(token, job);
            return token;
        }

        String register(NotificationDispatcher.Job job) {
            String token = "job-" + job.getAttempts() + "-" + counter++;
            storage.put(token, job);
            return token;
        }
    }
}
