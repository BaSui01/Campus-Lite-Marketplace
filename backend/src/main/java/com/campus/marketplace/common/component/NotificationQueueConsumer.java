package com.campus.marketplace.common.component;

import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.service.NotificationService;
import com.campus.marketplace.service.AuditLogService;
import com.campus.marketplace.common.enums.AuditActionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationQueueConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Value("${notifications.queue.key:notifications:queue}")
    private String queueKey;

    @Value("${notifications.retry.key:notifications:retry}")
    private String retryKey;

    @Value("${notifications.batch.size:50}")
    private int batchSize;

    @Value("${notifications.retry.maxAttempts:5}")
    private int maxAttempts;

    // 轮询队列
    @Scheduled(fixedDelayString = "${notifications.consumer.poll-interval-ms:2000}")
    public void consume() {
        try {
            List<String> entries = new ArrayList<>();
            for (int i = 0; i < batchSize; i++) {
                Object v = redisTemplate.opsForList().rightPop(queueKey);
                if (v == null) break;
                entries.add(v.toString());
            }
            for (String json : entries) {
                handle(json);
            }
        } catch (Exception e) {
            log.error("通知队列消费失败: {}", e.getMessage(), e);
        }
    }

    // 到期重试回填
    @Scheduled(fixedDelayString = "${notifications.consumer.retry-interval-ms:5000}")
    public void scheduleRetry() {
        try {
            long now = Instant.now().toEpochMilli();
            var due = redisTemplate.opsForZSet().rangeByScore(retryKey, 0, now);
            if (due == null || due.isEmpty()) return;
            for (Object json : due) {
                redisTemplate.opsForList().leftPush(queueKey, json.toString());
                redisTemplate.opsForZSet().remove(retryKey, json);
            }
        } catch (Exception e) {
            log.error("通知重试回填失败: {}", e.getMessage(), e);
        }
    }

    private void handle(String json) {
        try {
            NotificationDispatcher.Job job = objectMapper.readValue(json, NotificationDispatcher.Job.class);
            notificationService.sendTemplateNotification(job.getReceiverId(), job.getTemplateCode(), job.getParams(),
                    NotificationType.valueOf(job.getType()), job.getRelatedId(), job.getRelatedType(), job.getLink());
        } catch (Exception e) {
            log.warn("通知发送失败，准备重试: {}", e.getMessage());
            enqueueRetry(json);
        }
    }

    private void enqueueRetry(String json) {
        try {
            NotificationDispatcher.Job job = objectMapper.readValue(json, NotificationDispatcher.Job.class);
            int attempts = job.getAttempts() + 1;
            if (attempts > maxAttempts) {
                log.error("通知重试超限，丢弃: userId={}, tpl={}", job.getReceiverId(), job.getTemplateCode());
                try {
                    auditLogService.logActionAsync(null, "system", AuditActionType.NOTIFICATION_FAIL,
                            "NOTIFICATION", job.getRelatedId(),
                            "template=" + job.getTemplateCode() + ", type=" + job.getType(),
                            "FAILED", null, null);
                } catch (Exception ignored) {}
                return;
            }
            job.setAttempts(attempts);
            long delay = (long) Math.min(60000, Math.pow(2, attempts) * 1000L);
            long when = Instant.now().toEpochMilli() + delay;
            String updated = objectMapper.writeValueAsString(job);
            redisTemplate.opsForZSet().add(retryKey, updated, when);
        } catch (Exception ex) {
            log.error("加入重试队列失败: {}", ex.getMessage(), ex);
        }
    }
}
