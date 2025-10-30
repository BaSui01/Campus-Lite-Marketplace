package com.campus.marketplace.common.component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流规则管理器（内存版）
 *
 * - 支持启用/禁用限流
 * - 支持用户/IP 白名单（豁免限流）
 * - 支持 IP 黑名单（直接拒绝）
 * - 规则可通过管理接口动态更新（重启不丢失可后续扩展为持久化）
 *
 * 说明：为满足 Spec #2 阶段三的“规则管理与动态刷新、黑白名单”要求，
 * 先提供内存实现，后续可替换为 DB/Redis 持久化。
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Component
public class RateLimitRuleManager {

    @Getter
    private volatile boolean enabled = true;

    private final Set<Long> userWhitelist = ConcurrentHashMap.newKeySet();
    private final Set<String> ipWhitelist = ConcurrentHashMap.newKeySet();
    private final Set<String> ipBlacklist = ConcurrentHashMap.newKeySet();

    public boolean isWhitelisted(Long userId, String ip) {
        if (userId != null && userWhitelist.contains(userId)) return true;
        if (ip != null && ipWhitelist.contains(ip)) return true;
        return false;
    }

    public boolean isBlacklisted(String ip) {
        return ip != null && ipBlacklist.contains(ip);
    }

    // -------- 管理操作 --------
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        log.info("RateLimit 总开关已{}", enabled ? "开启" : "关闭");
    }

    public void addUserWhitelist(Long userId) { if (userId != null) userWhitelist.add(userId); }
    public void removeUserWhitelist(Long userId) { if (userId != null) userWhitelist.remove(userId); }
    public void addIpWhitelist(String ip) { if (ip != null) ipWhitelist.add(ip); }
    public void removeIpWhitelist(String ip) { if (ip != null) ipWhitelist.remove(ip); }
    public void addIpBlacklist(String ip) { if (ip != null) ipBlacklist.add(ip); }
    public void removeIpBlacklist(String ip) { if (ip != null) ipBlacklist.remove(ip); }

    public Set<Long> getUserWhitelist() { return Collections.unmodifiableSet(userWhitelist); }
    public Set<String> getIpWhitelist() { return Collections.unmodifiableSet(ipWhitelist); }
    public Set<String> getIpBlacklist() { return Collections.unmodifiableSet(ipBlacklist); }
}
