package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.UserBehaviorLogDTO;
import com.campus.marketplace.common.dto.UserPersonaDTO;
import com.campus.marketplace.common.enums.BehaviorType;

import java.util.List;
import java.util.Map;

/**
 * 行为分析服务接口
 *
 * 提供用户行为追踪、画像构建、行为分析等功能
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
public interface BehaviorAnalysisService {

    /**
     * 记录用户行为
     *
     * @param userId       用户ID
     * @param behaviorType 行为类型
     * @param targetType   目标类型（Goods/Post/User）
     * @param targetId     目标ID
     * @param source       来源（搜索/推荐/直接访问）
     * @param duration     浏览时长（秒），可选
     * @param extraData    额外数据，可选
     * @return 行为日志DTO
     */
    UserBehaviorLogDTO recordBehavior(
            Long userId,
            BehaviorType behaviorType,
            String targetType,
            Long targetId,
            String source,
            Integer duration,
            Map<String, Object> extraData
    );

    /**
     * 获取用户行为日志列表
     *
     * @param userId 用户ID
     * @param limit  返回数量限制
     * @return 行为日志列表
     */
    List<UserBehaviorLogDTO> getUserBehaviors(Long userId, Integer limit);

    /**
     * 获取用户画像
     *
     * @param userId 用户ID
     * @return 用户画像DTO，如果不存在则返回null
     */
    UserPersonaDTO getUserPersona(Long userId);

    /**
     * 构建或更新用户画像
     *
     * @param userId 用户ID
     * @return 更新后的用户画像DTO
     */
    UserPersonaDTO buildUserPersona(Long userId);

    /**
     * 批量更新用户画像（定时任务）
     *
     * 每天更新距离上次更新超过24小时的画像
     */
    void batchUpdateUserPersonas();

    /**
     * 分析用户兴趣标签
     *
     * @param userId 用户ID
     * @return 兴趣标签及权重
     */
    Map<String, Double> analyzeInterestTags(Long userId);

    /**
     * 分析用户价格偏好
     *
     * @param userId 用户ID
     * @return 价格偏好信息
     */
    Map<String, Object> analyzePricePreference(Long userId);

    /**
     * 分析用户活跃时段
     *
     * @param userId 用户ID
     * @return 活跃时段列表
     */
    List<String> analyzeActiveTimeSlots(Long userId);

    /**
     * 确定用户分群
     *
     * @param userId 用户ID
     * @return 用户分群类型
     */
    String determineUserSegment(Long userId);

    /**
     * 获取行为热力图数据（管理员）
     *
     * @return 行为热力图数据
     */
    Map<String, Object> getBehaviorHeatmap();

    /**
     * 归档旧的行为日志（定时任务）
     *
     * 删除90天前的行为日志
     */
    void archiveOldBehaviorLogs();
}
