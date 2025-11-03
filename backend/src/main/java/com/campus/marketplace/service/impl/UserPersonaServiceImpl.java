package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.UserPersonaDTO;
import com.campus.marketplace.common.entity.UserPersona;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.UserPersonaRepository;
import com.campus.marketplace.service.BehaviorAnalysisService;
import com.campus.marketplace.service.UserPersonaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户画像服务实现
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPersonaServiceImpl implements UserPersonaService {

    private final UserPersonaRepository personaRepository;
    private final BehaviorAnalysisService behaviorAnalysisService;

    @Override
    public UserPersonaDTO getUserPersona(Long userId) {
        UserPersona persona = personaRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户画像不存在"));

        return convertToDTO(persona);
    }

    @Override
    @Transactional
    public UserPersonaDTO createUserPersona(Long userId) {
        // 检查是否已存在
        if (personaRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "用户画像已存在");
        }

        return behaviorAnalysisService.buildUserPersona(userId);
    }

    @Override
    @Transactional
    public UserPersonaDTO updateUserPersona(Long userId) {
        return behaviorAnalysisService.buildUserPersona(userId);
    }

    @Override
    @Transactional
    public void deleteUserPersona(Long userId) {
        personaRepository.deleteByUserId(userId);
        log.info("删除用户画像: userId={}", userId);
    }

    @Override
    public Map<String, Long> getUserSegmentStatistics() {
        List<Object[]> results = personaRepository.countByUserSegment();
        
        Map<String, Long> statistics = new HashMap<>();
        for (Object[] result : results) {
            String segment = (String) result[0];
            Long count = (Long) result[1];
            statistics.put(segment, count);
        }

        return statistics;
    }

    // ========== 私有方法 ==========

    private UserPersonaDTO convertToDTO(UserPersona persona) {
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
