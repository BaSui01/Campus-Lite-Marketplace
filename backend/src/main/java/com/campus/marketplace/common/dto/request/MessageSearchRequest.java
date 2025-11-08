package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息搜索请求DTO
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchRequest {

    @NotNull(message = "纠纷ID不能为空")
    private Long disputeId;

    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    /**
     * 消息类型筛选
     */
    private List<String> messageTypes;

    /**
     * 发送者ID筛选
     */
    private List<Long> senderIds;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 只搜索自己的消息
     */
    @Builder.Default
    private boolean ownMessagesOnly = false;

    /**
     * 包含已撤回消息
     */
    @Builder.Default
    private boolean includeRecalled = false;

    /**
     * 排序方式: relevance, time, sender
     */
    @Builder.Default
    private String sortBy = "relevance";

    /**
     * 排序方向: asc, desc
     */
    @Builder.Default
    private String sortDirection = "desc";

    /**
     * 页码
     */
    @Builder.Default
    private int page = 0;

    /**
     * 每页大小
     */
    @Builder.Default
    private int size = 20;
}