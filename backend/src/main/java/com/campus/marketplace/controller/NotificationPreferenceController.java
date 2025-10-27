package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.enums.NotificationChannel;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications/preferences")
@RequiredArgsConstructor
@Tag(name = "通知偏好", description = "用户通知偏好与退订管理")
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @PostMapping("/channel/{channel}/enabled/{enabled}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "设置渠道开关")
    public ApiResponse<Void> setChannelEnabled(@PathVariable NotificationChannel channel,
                                               @PathVariable boolean enabled) {
        Long userId = com.campus.marketplace.common.utils.SecurityUtil.getCurrentUserId();
        preferenceService.setChannelEnabled(userId, channel, enabled);
        return ApiResponse.success();
    }

    @PostMapping("/channel/{channel}/quiet-hours")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "设置静默时段")
    public ApiResponse<Void> setQuietHours(@PathVariable NotificationChannel channel,
                                           @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime start,
                                           @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime end) {
        Long userId = SecurityUtil.getCurrentUserId();
        preferenceService.setQuietHours(userId, channel, start, end);
        return ApiResponse.success();
    }

    @PostMapping("/unsubscribe/{channel}/{templateCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "退订模板")
    public ApiResponse<Void> unsubscribe(@PathVariable NotificationChannel channel,
                                         @PathVariable String templateCode) {
        Long userId = SecurityUtil.getCurrentUserId();
        preferenceService.unsubscribe(userId, templateCode, channel);
        return ApiResponse.success();
    }

    @DeleteMapping("/unsubscribe/{channel}/{templateCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "取消退订")
    public ApiResponse<Void> resubscribe(@PathVariable NotificationChannel channel,
                                         @PathVariable String templateCode) {
        Long userId = SecurityUtil.getCurrentUserId();
        preferenceService.resubscribe(userId, templateCode, channel);
        return ApiResponse.success();
    }

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查看开关与静默生效状态")
    public ApiResponse<Map<String, Object>> status() {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean emailEnabled = preferenceService.isChannelEnabled(userId, NotificationChannel.EMAIL);
        boolean webpushEnabled = preferenceService.isChannelEnabled(userId, NotificationChannel.WEB_PUSH);
        boolean emailQuiet = preferenceService.isInQuietHours(userId, NotificationChannel.EMAIL, LocalTime.now());
        boolean webpushQuiet = preferenceService.isInQuietHours(userId, NotificationChannel.WEB_PUSH, LocalTime.now());
        return ApiResponse.success(Map.of(
                "emailEnabled", emailEnabled,
                "webpushEnabled", webpushEnabled,
                "emailQuietNow", emailQuiet,
                "webpushQuietNow", webpushQuiet
        ));
    }
}
