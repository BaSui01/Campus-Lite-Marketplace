package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.FeatureFlagRules;
import com.campus.marketplace.common.entity.FeatureFlag;
import com.campus.marketplace.repository.FeatureFlagRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;


/**
 * Feature Flag Service Impl
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureFlagServiceImpl implements com.campus.marketplace.service.FeatureFlagService {

    private final FeatureFlagRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Cache<String, Cached> localCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(1000)
            .build();

    @Value("${spring.profiles.active:dev}")
    private String activeEnv;

    private record Cached(boolean enabled, FeatureFlagRules rules, LocalDateTime updatedAt) {}

    @Override
    public boolean isEnabled(String key, Long userId, Long campusId, String env) {
        if (env == null || env.isBlank()) env = activeEnv;
        Cached cached = localCache.get(key, k -> loadFromDb(k));
        if (cached == null) return false;
        if (!cached.enabled) return false;
        FeatureFlagRules rules = cached.rules;
        if (rules == null) return true;

        if (rules.getAllowEnvs() != null && !rules.getAllowEnvs().isEmpty()) {
            if (!rules.getAllowEnvs().contains(env)) return false;
        }
        if (userId != null && rules.getAllowUserIds() != null && !rules.getAllowUserIds().isEmpty()) {
            if (rules.getAllowUserIds().contains(userId)) return true; // 用户白名单优先
        }
        if (campusId != null && rules.getAllowCampusIds() != null && !rules.getAllowCampusIds().isEmpty()) {
            return rules.getAllowCampusIds().contains(campusId);
        }
        // 默认允许
        return true;
    }

    @Override
    public void refresh(String key) {
        localCache.invalidate(key);
    }

    @Override
    public void refreshAll() {
        localCache.invalidateAll();
    }

    private Cached loadFromDb(String key) {
        Optional<FeatureFlag> opt = repository.findByKey(key);
        if (opt.isEmpty()) return null;
        FeatureFlag flag = opt.get();
        FeatureFlagRules rules = null;
        try {
            if (flag.getRulesJson() != null && !flag.getRulesJson().isBlank()) {
                rules = objectMapper.readValue(flag.getRulesJson(), FeatureFlagRules.class);
            }
        } catch (Exception e) {
            log.warn("FeatureFlag 规则解析失败: key={}, error={}", key, e.getMessage());
        }
        return new Cached(flag.isEnabled(), rules, flag.getUpdatedAt());
    }
}
