package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.NotificationResponse;
import com.campus.marketplace.common.enums.NotificationStatus;
import com.campus.marketplace.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器 - 站内消息查询和管理
 *
 * @author BaSui 😎
 * @since 2025-10-27
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "站内通知查询、标记已读、删除等操作")
public class NotificationController {

    private final NotificationService notificationService;

        @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询通知列表", description = "分页查询当前用户的通知列表")
    public ApiResponse<Page<NotificationResponse>> listNotifications(
            @Parameter(description = "通知状态", example = "UNREAD") @RequestParam(required = false) NotificationStatus status,
            @Parameter(description = "页码（从0开始）", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        log.info("查询通知列表: status={}, page={}, size={}", status, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> result = notificationService.listNotifications(status, pageable);

        return ApiResponse.success(result);
    }

        @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询未读数量", description = "获取当前用户的未读通知数量")
    public ApiResponse<Long> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ApiResponse.success(count);
    }

        @PutMapping("/mark-read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "标记通知为已读", description = "批量标记指定的通知为已读")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = java.util.List.class),
                    examples = @ExampleObject(name = "请求示例", value = "[101,102,103]")
            )
    )
    public ApiResponse<String> markAsRead(@RequestBody List<Long> notificationIds) {
        log.info("标记通知为已读: ids={}", notificationIds);

        notificationService.markAsRead(notificationIds);

        return ApiResponse.success("标记成功");
    }

        @PutMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "全部标记为已读", description = "标记当前用户所有未读通知为已读")
    public ApiResponse<String> markAllAsRead() {
        log.info("全部标记为已读");

        notificationService.markAllAsRead();

        return ApiResponse.success("全部已读");
    }

        @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除通知", description = "批量删除指定的通知（软删除）")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = java.util.List.class),
                    examples = @ExampleObject(name = "请求示例", value = "[201,202]")
            )
    )
    public ApiResponse<String> deleteNotifications(@RequestBody List<Long> notificationIds) {
        log.info("删除通知: ids={}", notificationIds);

        notificationService.deleteNotifications(notificationIds);

        return ApiResponse.success("删除成功");
    }
}
