package com.campus.marketplace.common.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Notification Dispatcher
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${notifications.queue.key:notifications:queue}")
    private String queueKey;

    public void enqueueTemplate(Long receiverId,
                                String templateCode,
                                Map<String, Object> params,
                                String type,
                                Long relatedId,
                                String relatedType,
                                String link) {
        try {
            Job job = new Job(receiverId, templateCode, params, type, relatedId, relatedType, link, 0);
            String json = objectMapper.writeValueAsString(job);
            redisTemplate.opsForList().leftPush(queueKey, json);
        } catch (Exception e) {
            log.error("入队通知失败: userId={}, tpl={}", receiverId, templateCode, e);
        }
    }

    @Data
    @AllArgsConstructor
    public static class Job {
        private Long receiverId;
        private String templateCode;
        private Map<String, Object> params;
        private String type;
        private Long relatedId;
        private String relatedType;
        private String link;
        private int attempts;
    }
}
