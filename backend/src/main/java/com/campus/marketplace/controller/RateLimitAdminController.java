package com.campus.marketplace.controller;

import com.campus.marketplace.common.component.RateLimitRuleManager;
import com.campus.marketplace.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Rate Limit Admin Controller
 *
 * @author BaSui
 * @date 2025-10-29
 */

@RestController
@RequestMapping("/api/admin/rate-limit")
@RequiredArgsConstructor
@Tag(name = "限流规则管理", description = "动态维护限流黑白名单与总开关")
public class RateLimitAdminController {

    private final RateLimitRuleManager ruleManager;

    @GetMapping("/rules")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)")
    @Operation(summary = "查看规则", description = "查看当前限流开关与黑白名单")
    public ApiResponse<Map<String, Object>> getRules() {
        Map<String, Object> data = new HashMap<>();
        data.put("enabled", ruleManager.isEnabled());
        data.put("userWhitelist", ruleManager.getUserWhitelist());
        data.put("ipWhitelist", ruleManager.getIpWhitelist());
        data.put("ipBlacklist", ruleManager.getIpBlacklist());
        return ApiResponse.success(data);
    }

    @PostMapping("/enabled/{enabled}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)")
    @Operation(summary = "设置开关", description = "开启或关闭限流")
    public ApiResponse<Void> setEnabled(@PathVariable boolean enabled) {
        ruleManager.setEnabled(enabled);
        return ApiResponse.success();
    }

    @PostMapping("/whitelist/users/{userId}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)")
    public ApiResponse<Void> addUserWhitelist(@PathVariable Long userId) {
        ruleManager.addUserWhitelist(userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/whitelist/users/{userId}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)")
    public ApiResponse<Void> removeUserWhitelist(@PathVariable Long userId) {
        ruleManager.removeUserWhitelist(userId);
        return ApiResponse.success();
    }

    @PostMapping("/whitelist/ips/{ip}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)")
    public ApiResponse<Void> addIpWhitelist(@PathVariable String ip) {
        ruleManager.addIpWhitelist(ip);
        return ApiResponse.success();
    }

    @DeleteMapping("/whitelist/ips/{ip}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)")
    public ApiResponse<Void> removeIpWhitelist(@PathVariable String ip) {
        ruleManager.removeIpWhitelist(ip);
        return ApiResponse.success();
    }

    @PostMapping("/blacklist/ips/{ip}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)")
    public ApiResponse<Void> addIpBlacklist(@PathVariable String ip) {
        ruleManager.addIpBlacklist(ip);
        return ApiResponse.success();
    }

    @DeleteMapping("/blacklist/ips/{ip}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)")
    public ApiResponse<Void> removeIpBlacklist(@PathVariable String ip) {
        ruleManager.removeIpBlacklist(ip);
        return ApiResponse.success();
    }
}
