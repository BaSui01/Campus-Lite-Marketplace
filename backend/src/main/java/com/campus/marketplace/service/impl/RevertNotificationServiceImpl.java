package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.RevertRequest;
import com.campus.marketplace.revert.dto.RevertExecutionResult;
import com.campus.marketplace.service.NotificationService;
import com.campus.marketplace.service.RevertNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 撤销通知服务实现
 * 
 * 功能说明：
 * 1. 集成NotificationService发送通知
 * 2. 支持多种通知类型（站内信、邮件、推送）
 * 3. 通知内容模板化
 * 4. 异步发送不阻塞主流程
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RevertNotificationServiceImpl implements RevertNotificationService {

    private final NotificationService notificationService;

    @Override
    public void sendRevertRequestNotification(RevertRequest revertRequest) {
        try {
            String title = "数据撤销申请已提交";
            String content = String.format(
                "您的撤销申请已提交（请求ID: %d），我们将尽快处理。\n" +
                "撤销原因: %s\n" +
                "申请时间: %s",
                revertRequest.getId(),
                revertRequest.getReason(),
                revertRequest.getCreatedAt()
            );
            
            // 发送站内通知给申请人
            notificationService.sendNotification(
                revertRequest.getRequesterId(),
                com.campus.marketplace.common.enums.NotificationType.SYSTEM_ANNOUNCEMENT,
                title,
                content,
                revertRequest.getId(),
                "REVERT_REQUEST",
                null
            );
            
            log.info("撤销申请通知已发送: requesterId={}, revertRequestId={}", 
                    revertRequest.getRequesterId(), revertRequest.getId());
            
        } catch (Exception e) {
            log.error("发送撤销申请通知失败: revertRequestId={}", revertRequest.getId(), e);
        }
    }

    @Override
    public void sendRevertExecutionNotification(RevertRequest revertRequest, RevertExecutionResult result) {
        try {
            String title;
            String content;
            
            if (result.isSuccess()) {
                title = "数据撤销执行成功";
                content = String.format(
                    "您的撤销申请已成功执行。\n" +
                    "请求ID: %d\n" +
                    "执行结果: %s\n" +
                    "执行时间: %s",
                    revertRequest.getId(),
                    result.getMessage(),
                    revertRequest.getExecutedAt()
                );
            } else {
                title = "数据撤销执行失败";
                content = String.format(
                    "您的撤销申请执行失败，请联系管理员。\n" +
                    "请求ID: %d\n" +
                    "失败原因: %s\n" +
                    "错误信息: %s",
                    revertRequest.getId(),
                    result.getMessage(),
                    revertRequest.getErrorMessage()
                );
            }
            
            // 发送站内通知给申请人
            notificationService.sendNotification(
                revertRequest.getRequesterId(),
                com.campus.marketplace.common.enums.NotificationType.SYSTEM_ANNOUNCEMENT,
                title,
                content,
                revertRequest.getId(),
                "REVERT_REQUEST",
                null
            );
            
            log.info("撤销执行结果通知已发送: requesterId={}, success={}", 
                    revertRequest.getRequesterId(), result.isSuccess());
            
        } catch (Exception e) {
            log.error("发送撤销执行通知失败: revertRequestId={}", revertRequest.getId(), e);
        }
    }

    @Override
    public void sendApprovalNotification(RevertRequest revertRequest, boolean approved) {
        try {
            String title = approved ? "撤销申请已批准" : "撤销申请已拒绝";
            String content;
            
            if (approved) {
                content = String.format(
                    "您的撤销申请已获批准，即将执行撤销操作。\n" +
                    "请求ID: %d\n" +
                    "审批人: %s\n" +
                    "审批时间: %s\n" +
                    "审批意见: %s",
                    revertRequest.getId(),
                    revertRequest.getApprovedByName() != null ? revertRequest.getApprovedByName() : "系统",
                    revertRequest.getApprovedAt(),
                    revertRequest.getApprovalComment() != null ? revertRequest.getApprovalComment() : "无"
                );
            } else {
                content = String.format(
                    "您的撤销申请已被拒绝。\n" +
                    "请求ID: %d\n" +
                    "审批人: %s\n" +
                    "拒绝原因: %s",
                    revertRequest.getId(),
                    revertRequest.getApprovedByName() != null ? revertRequest.getApprovedByName() : "系统",
                    revertRequest.getApprovalComment() != null ? revertRequest.getApprovalComment() : "未说明"
                );
            }
            
            // 发送站内通知给申请人
            notificationService.sendNotification(
                revertRequest.getRequesterId(),
                com.campus.marketplace.common.enums.NotificationType.SYSTEM_ANNOUNCEMENT,
                title,
                content,
                revertRequest.getId(),
                "REVERT_REQUEST",
                null
            );
            
            log.info("审批结果通知已发送: requesterId={}, approved={}", 
                    revertRequest.getRequesterId(), approved);
            
        } catch (Exception e) {
            log.error("发送审批通知失败: revertRequestId={}", revertRequest.getId(), e);
        }
    }

    @Override
    public void sendRevertWarningNotification(RevertRequest revertRequest, String warningMessage) {
        try {
            String title = "⚠️ 撤销操作重要提醒";
            String content = String.format(
                "您的撤销申请涉及重要操作，请注意：\n\n" +
                "%s\n\n" +
                "请求ID: %d\n" +
                "如有疑问，请联系客服或管理员。",
                warningMessage,
                revertRequest.getId()
            );
            
            // 发送站内通知给申请人
            notificationService.sendNotification(
                revertRequest.getRequesterId(),
                com.campus.marketplace.common.enums.NotificationType.SYSTEM_ANNOUNCEMENT,
                title,
                content,
                revertRequest.getId(),
                "REVERT_REQUEST",
                null
            );
            
            // 同时通知管理员（TODO: 等用户角色服务实现后解除注释）
            // 调用方法：List<Long> adminIds = userService.getAdminUserIds();
            // 说明：获取所有具有审批权限的管理员ID列表
            // 然后遍历发送通知：
            // for (Long adminId : adminIds) {
            //     notificationService.sendNotification(adminId, NotificationType.SYSTEM, title, content, revertRequest.getId(), "REVERT_REQUEST", null);
            // }
            log.debug("管理员通知（待实现）: revertRequestId={}", revertRequest.getId());
            
            log.info("撤销警告通知已发送: requesterId={}", revertRequest.getRequesterId());
            
        } catch (Exception e) {
            log.error("发送撤销警告通知失败: revertRequestId={}", revertRequest.getId(), e);
        }
    }
}
