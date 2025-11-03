package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.UserPersonaDTO;

import java.util.Map;

/**
 * 用户画像服务接口
 *
 * 提供用户画像的查询、更新、分群等功能
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
public interface UserPersonaService {

    /**
     * 获取用户画像
     *
     * @param userId 用户ID
     * @return 用户画像DTO
     */
    UserPersonaDTO getUserPersona(Long userId);

    /**
     * 创建用户画像
     *
     * @param userId 用户ID
     * @return 用户画像DTO
     */
    UserPersonaDTO createUserPersona(Long userId);

    /**
     * 更新用户画像
     *
     * @param userId 用户ID
     * @return 更新后的用户画像DTO
     */
    UserPersonaDTO updateUserPersona(Long userId);

    /**
     * 删除用户画像
     *
     * @param userId 用户ID
     */
    void deleteUserPersona(Long userId);

    /**
     * 获取用户分群统计
     *
     * @return 用户分群统计数据
     */
    Map<String, Long> getUserSegmentStatistics();
}
