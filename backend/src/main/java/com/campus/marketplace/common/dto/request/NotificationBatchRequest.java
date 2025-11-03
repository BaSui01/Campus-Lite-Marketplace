package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通知批量发送请求
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationBatchRequest {

    /**
     * 目标用户ID列表
     */
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> targetUserIds;

    /**
     * 通知标题
     */
    @NotNull(message = "通知标题不能为空")
    private String title;

    /**
     * 通知内容
     */
    @NotNull(message = "通知内容不能为空")
    private String content;

    /**
     * 通知类型（SYSTEM, ORDER, GOODS等）
     */
    private String notificationType;

    /**
     * 相关实体ID（可选）
     */
    private Long relatedEntityId;
}
