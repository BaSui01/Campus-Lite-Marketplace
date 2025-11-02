package com.campus.marketplace.event.listener;

import com.campus.marketplace.common.entity.Appeal;
import com.campus.marketplace.common.enums.AppealStatus;
import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.event.AppealCreatedEvent;
import com.campus.marketplace.event.AppealHandledEvent;
import com.campus.marketplace.event.AppealStatusChangedEvent;
import com.campus.marketplace.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 申诉事件监听器
 * 
 * 监听申诉相关事件，触发通知发送
 * 
 * @author BaSui 😎
 * @date 2025-11-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppealEventListener {

    private final NotificationService notificationService;

    /**
     * 处理申诉创建事件
     * 
     * 当用户提交新申诉时，通知管理员
     * 
     * @param event 申诉创建事件
     */
    @Async
    @EventListener
    public void handleAppealCreated(AppealCreatedEvent event) {
        try {
            Appeal appeal = event.getAppeal();
            log.info("处理申诉创建事件: appealId={}, userId={}", appeal.getId(), appeal.getUserId());

            // 通知管理员有新的申诉待处理
            String title = "新申诉待处理";
            String content = String.format(
                "用户提交了新的申诉，申诉类型：%s，申诉原因：%s",
                appeal.getAppealType().getDescription(),
                appeal.getReason()
            );

            // 这里简化处理，实际应该查询管理员ID列表
            // 或者使用系统广播机制通知所有管理员
            notificationService.sendNotification(
                1L, // 管理员ID（简化处理）
                NotificationType.SYSTEM_ANNOUNCEMENT,
                title,
                content,
                appeal.getId(),
                "APPEAL",
                "/admin/appeals/" + appeal.getId()
            );

            log.info("申诉创建通知发送成功: appealId={}", appeal.getId());

        } catch (Exception e) {
            log.error("处理申诉创建事件失败: {}", event, e);
        }
    }

    /**
     * 处理申诉状态变更事件
     * 
     * 当申诉状态变化时，通知申诉人
     * 
     * @param event 申诉状态变更事件
     */
    @Async
    @EventListener
    public void handleStatusChanged(AppealStatusChangedEvent event) {
        try {
            Appeal appeal = event.getAppeal();
            AppealStatus oldStatus = event.getOldStatus();
            AppealStatus newStatus = event.getNewStatus();

            log.info("处理申诉状态变更事件: appealId={}, {} -> {}", 
                appeal.getId(), oldStatus, newStatus);

            // 通知申诉人状态变更
            String title = "申诉状态更新";
            String content = buildStatusChangeContent(appeal, oldStatus, newStatus);

            notificationService.sendNotification(
                appeal.getUserId(),
                NotificationType.SYSTEM_ANNOUNCEMENT,
                title,
                content,
                appeal.getId(),
                "APPEAL",
                "/my/appeals/" + appeal.getId()
            );

            log.info("申诉状态变更通知发送成功: appealId={}, userId={}", 
                appeal.getId(), appeal.getUserId());

        } catch (Exception e) {
            log.error("处理申诉状态变更事件失败: {}", event, e);
        }
    }

    /**
     * 处理申诉处理完成事件
     * 
     * 当管理员处理完申诉时，通知申诉人最终结果
     * 
     * @param event 申诉处理完成事件
     */
    @Async
    @EventListener
    public void handleAppealHandled(AppealHandledEvent event) {
        try {
            Appeal appeal = event.getAppeal();
            Long handlerId = event.getHandlerId();
            String handlerName = event.getHandlerName();

            log.info("处理申诉处理完成事件: appealId={}, handlerId={}, approved={}", 
                appeal.getId(), handlerId, event.isApproved());

            // 通知申诉人最终结果
            String title = event.isApproved() ? "申诉通过" : "申诉驳回";
            String content = buildHandledContent(appeal, event.isApproved(), handlerName);

            notificationService.sendNotification(
                appeal.getUserId(),
                NotificationType.SYSTEM_ANNOUNCEMENT,
                title,
                content,
                appeal.getId(),
                "APPEAL",
                "/my/appeals/" + appeal.getId()
            );

            log.info("申诉处理结果通知发送成功: appealId={}, userId={}, approved={}", 
                appeal.getId(), appeal.getUserId(), event.isApproved());

        } catch (Exception e) {
            log.error("处理申诉处理完成事件失败: {}", event, e);
        }
    }

    /**
     * 构建状态变更通知内容
     * 
     * @param appeal    申诉对象
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @return 通知内容
     */
    private String buildStatusChangeContent(Appeal appeal, AppealStatus oldStatus, AppealStatus newStatus) {
        return String.format(
            "您的申诉状态已更新：%s -> %s。申诉类型：%s",
            oldStatus.getDescription(),
            newStatus.getDescription(),
            appeal.getAppealType().getDescription()
        );
    }

    /**
     * 构建申诉处理结果通知内容
     * 
     * @param appeal      申诉对象
     * @param approved    是否通过
     * @param handlerName 处理人名称
     * @return 通知内容
     */
    private String buildHandledContent(Appeal appeal, boolean approved, String handlerName) {
        StringBuilder content = new StringBuilder();
        
        content.append("您的申诉已被处理。\n\n");
        content.append("申诉类型：").append(appeal.getAppealType().getDescription()).append("\n");
        content.append("处理结果：").append(approved ? "通过" : "驳回").append("\n");
        content.append("处理人：").append(handlerName).append("\n");

        if (appeal.getReviewComment() != null && !appeal.getReviewComment().isEmpty()) {
            content.append("处理意见：").append(appeal.getReviewComment()).append("\n");
        }

        if (approved) {
            content.append("\n您的申诉已被接受，相关处罚已撤销。");
        } else {
            content.append("\n如有疑问，请联系客服或再次提交申诉。");
        }

        return content.toString();
    }
}
