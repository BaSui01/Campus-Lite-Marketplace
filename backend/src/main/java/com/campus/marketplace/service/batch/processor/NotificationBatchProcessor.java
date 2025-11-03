package com.campus.marketplace.service.batch.processor;

import com.campus.marketplace.common.dto.request.NotificationBatchRequest;
import com.campus.marketplace.common.entity.BatchTaskItem;
import com.campus.marketplace.common.enums.BatchType;
import com.campus.marketplace.service.NotificationService;
import com.campus.marketplace.service.batch.BatchProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知批量处理器
 * 处理批量通知发送
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationBatchProcessor implements BatchProcessor {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    public BatchType getSupportedType() {
        return BatchType.NOTIFICATION_BATCH;
    }

    @Override
    @Transactional
    public BatchItemResult processItem(BatchTaskItem item) {
        try {
            // 解析请求数据
            NotificationBatchRequest request = objectMapper.readValue(
                item.getInputData(), 
                NotificationBatchRequest.class
            );

            Long userId = item.getTargetId();

            // 发送通知（站内信）
            notificationService.sendNotification(
                userId,
                com.campus.marketplace.common.enums.NotificationType.SYSTEM_ANNOUNCEMENT,
                request.getTitle(),
                request.getContent(),
                request.getRelatedEntityId(),
                request.getNotificationType(),
                null
            );

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("notificationType", request.getNotificationType());
            result.put("sent", true);

            return new BatchItemResult(true, "通知发送成功", result);

        } catch (Exception e) {
            log.error("发送批量通知失败: itemId={}", item.getId(), e);
            return new BatchItemResult(false, "发送失败: " + e.getMessage(), null);
        }
    }
}
