package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.UserBehaviorLogDTO;
import com.campus.marketplace.common.dto.UserPersonaDTO;
import com.campus.marketplace.common.entity.UserBehaviorLog;
import com.campus.marketplace.common.entity.UserPersona;
import com.campus.marketplace.common.enums.BehaviorType;
import com.campus.marketplace.repository.UserBehaviorLogRepository;
import com.campus.marketplace.repository.UserPersonaRepository;
import com.campus.marketplace.service.BehaviorAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 行为分析服务实现
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BehaviorAnalysisServiceImpl implements BehaviorAnalysisService {

    private final UserBehaviorLogRepository behaviorLogRepository;
    private final UserPersonaRepository personaRepository;

    @Override
    @Async
    @Transactional
    public UserBehaviorLogDTO recordBehavior(
            Long userId,
            BehaviorType behaviorType,
            String targetType,
            Long targetId,
            String source,
            Integer duration,
            Map<String, Object> extraData
    ) {
        log.debug("记录用户行为: userId={}, type={}, target={}:{}", userId, behaviorType, targetType, targetId);

        UserBehaviorLog log = UserBehaviorLog.builder()
                .userId(userId)
                .behaviorType(behaviorType)
                .targetType(targetType)
                .targetId(targetId)
                .source(source)
                .duration(duration)
                .extraData(extraData)
                .build();

        UserBehaviorLog saved = behaviorLogRepository.save(log);
        return convertToDTO(saved);
    }

    @Override
    public List<UserBehaviorLogDTO> getUserBehaviors(Long userId, Integer limit) {
        List<UserBehaviorLog> logs = behaviorLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        if (limit != null && limit > 0) {
            logs = logs.stream().limit(limit).collect(Collectors.toList());
        }
        
        return logs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public UserPersonaDTO getUserPersona(Long userId) {
        return personaRepository.findByUserId(userId)
                .map(this::convertPersonaToDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public UserPersonaDTO buildUserPersona(Long userId) {
        log.info("构建用户画像: userId={}", userId);

        UserPersona persona = personaRepository.findByUserId(userId)
                .orElse(UserPersona.builder().userId(userId).build());

        // 分析兴趣标签
        persona.setInterestTags(analyzeInterestTags(userId));

        // 分析价格偏好
        persona.setPricePreference(analyzePricePreference(userId));

        // 分析活跃时段
        persona.setActiveTimeSlots(analyzeActiveTimeSlots(userId));

        // 确定用户分群
        persona.setUserSegment(determineUserSegment(userId));

        persona.setLastUpdatedTime(LocalDateTime.now());

        UserPersona saved = personaRepository.save(persona);
        return convertPersonaToDTO(saved);
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点执行
    @Transactional
    public void batchUpdateUserPersonas() {
        log.info("开始批量更新用户画像...");

        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<UserPersona> pendingPersonas = personaRepository.findPendingUpdate(threshold);

        int count = 0;
        for (UserPersona persona : pendingPersonas) {
            try {
                buildUserPersona(persona.getUserId());
                count++;
            } catch (Exception e) {
                log.error("更新用户画像失败: userId={}", persona.getUserId(), e);
            }
        }

        log.info("批量更新用户画像完成: 总数={}, 成功={}", pendingPersonas.size(), count);
    }

    @Override
    public Map<String, Double> analyzeInterestTags(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<UserBehaviorLog> logs = behaviorLogRepository.findByUserIdAndTimeRange(
                userId, thirtyDaysAgo, LocalDateTime.now());

        Map<String, Double> tags = new HashMap<>();
        
        // 基于行为类型和频次计算兴趣权重
        for (UserBehaviorLog log : logs) {
            if (log.getTargetType() != null) {
                String category = log.getTargetType();
                double weight = log.getBehaviorType().getWeight();
                tags.merge(category, weight, (oldValue, newValue) -> oldValue + newValue);
            }
        }

        // 归一化权重到0-1范围
        double maxWeight = tags.values().stream().max(Double::compareTo).orElse(1.0);
        tags.replaceAll((k, v) -> v / maxWeight);

        return tags;
    }

    @Override
    public Map<String, Object> analyzePricePreference(Long userId) {
        Map<String, Object> preference = new HashMap<>();
        
        // 默认值
        preference.put("preferredRange", "0-100");
        preference.put("avgSpending", 50);
        preference.put("maxSpending", 100);

        return preference;
    }

    @Override
    public List<String> analyzeActiveTimeSlots(Long userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<UserBehaviorLog> logs = behaviorLogRepository.findByUserIdAndTimeRange(
                userId, sevenDaysAgo, LocalDateTime.now());

        Map<Integer, Long> hourCounts = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getCreatedAt().getHour(),
                        Collectors.counting()
                ));

        List<String> activeSlots = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : hourCounts.entrySet()) {
            if (entry.getValue() > 5) {  // 至少5次行为才算活跃时段
                int hour = entry.getKey();
                activeSlots.add(String.format("%02d:00-%02d:00", hour, (hour + 4) % 24));
            }
        }

        return activeSlots;
    }

    @Override
    public String determineUserSegment(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<UserBehaviorLog> logs = behaviorLogRepository.findByUserIdAndTimeRange(
                userId, thirtyDaysAgo, LocalDateTime.now());

        long purchaseCount = logs.stream()
                .filter(log -> log.getBehaviorType() == BehaviorType.PURCHASE)
                .count();

        long totalBehaviorCount = logs.size();

        // 简单的分群逻辑
        if (purchaseCount >= 5) {
            return "高价值用户";
        } else if (totalBehaviorCount >= 50) {
            return "活跃用户";
        } else if (totalBehaviorCount < 5) {
            return "沉睡用户";
        } else {
            return "普通用户";
        }
    }

    @Override
    public Map<String, Object> getBehaviorHeatmap() {
        Map<String, Object> heatmap = new HashMap<>();
        // 热力图数据统计需要：
        // 1. 用户行为日志按时间段（小时/日）聚合
        // 2. 按行为类型分组统计
        // 3. 前端使用热力图库（如 ECharts、Heatmap.js）渲染
        heatmap.put("message", "热力图数据功能待实现，需要前端热力图组件配合");
        return heatmap;
    }

    @Override
    @Scheduled(cron = "0 0 3 * * ?")  // 每天凌晨3点执行
    @Transactional
    public void archiveOldBehaviorLogs() {
        log.info("开始归档旧的行为日志...");

        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        behaviorLogRepository.deleteByCreatedAtBefore(ninetyDaysAgo);

        log.info("归档旧的行为日志完成");
    }

    // ========== 私有方法 ==========

    private UserBehaviorLogDTO convertToDTO(UserBehaviorLog log) {
        if (log == null) {
            return null;
        }

        return UserBehaviorLogDTO.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .behaviorType(log.getBehaviorType())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .source(log.getSource())
                .duration(log.getDuration())
                .extraData(log.getExtraData())
                .sessionId(log.getSessionId())
                .deviceType(log.getDeviceType())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private UserPersonaDTO convertPersonaToDTO(UserPersona persona) {
        if (persona == null) {
            return null;
        }

        return UserPersonaDTO.builder()
                .id(persona.getId())
                .userId(persona.getUserId())
                .interestTags(persona.getInterestTags())
                .pricePreference(persona.getPricePreference())
                .activeTimeSlots(persona.getActiveTimeSlots())
                .campusPreference(persona.getCampusPreference())
                .favoriteCategories(persona.getFavoriteCategories())
                .favoriteBrands(persona.getFavoriteBrands())
                .userSegment(persona.getUserSegment())
                .lastUpdatedTime(persona.getLastUpdatedTime())
                .createdAt(persona.getCreatedAt())
                .updatedAt(persona.getUpdatedAt())
                .build();
    }
}
