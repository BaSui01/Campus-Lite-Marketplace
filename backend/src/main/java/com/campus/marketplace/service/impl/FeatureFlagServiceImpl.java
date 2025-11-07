package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.FeatureFlagRules;
import com.campus.marketplace.common.entity.FeatureFlag;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.FeatureFlagRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
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

    // 🎯 BaSui 新增方法实现（功能开关管理扩展）

    @Override
    @Transactional(readOnly = true)
    public List<FeatureFlag> listAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureFlag getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "功能开关不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureFlag getByKey(String key) {
        return repository.findByKey(key)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "功能开关不存在"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(String key, String description, boolean enabled, String rulesJson) {
        // 检查Key是否已存在
        repository.findByKey(key).ifPresent(existing -> {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "功能开关Key已存在");
        });

        // 校验规则JSON格式
        validateRulesJson(rulesJson);

        FeatureFlag flag = FeatureFlag.builder()
                .key(key)
                .description(description)
                .enabled(enabled)
                .rulesJson(rulesJson)
                .build();

        repository.save(flag);
        log.info("创建功能开关成功 id={}, key={}", flag.getId(), flag.getKey());

        // 刷新缓存
        refresh(key);
        return flag.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, String description, boolean enabled, String rulesJson) {
        FeatureFlag flag = getById(id);

        // 校验规则JSON格式
        validateRulesJson(rulesJson);

        flag.setDescription(description);
        flag.setEnabled(enabled);
        flag.setRulesJson(rulesJson);

        repository.save(flag);
        log.info("更新功能开关成功 id={}, key={}", id, flag.getKey());

        // 刷新缓存
        refresh(flag.getKey());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        FeatureFlag flag = getById(id);
        String key = flag.getKey();

        repository.delete(flag);
        log.info("删除功能开关成功 id={}, key={}", id, key);

        // 刷新缓存
        refresh(key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleEnabled(Long id) {
        FeatureFlag flag = getById(id);
        flag.setEnabled(!flag.isEnabled());

        repository.save(flag);
        log.info("切换功能开关启用状态成功 id={}, key={}, enabled={}", id, flag.getKey(), flag.isEnabled());

        // 刷新缓存
        refresh(flag.getKey());
    }

    // 🔧 私有辅助方法

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

    private void validateRulesJson(String rulesJson) {
        if (rulesJson == null || rulesJson.isBlank()) {
            return; // 允许空规则
        }
        try {
            objectMapper.readValue(rulesJson, FeatureFlagRules.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "规则JSON格式错误: " + e.getMessage());
        }
    }
}
