package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.FeatureFlag;

import java.util.List;

/**
 * Feature Flag Service
 *
 * @author BaSui
 * @date 2025-10-29
 */
public interface FeatureFlagService {

    boolean isEnabled(String key, Long userId, Long campusId, String env);

    void refresh(String key);

    void refreshAll();

    // 🎯 BaSui 新增方法（功能开关管理扩展）
    /**
     * 查询所有功能开关列表
     */
    List<FeatureFlag> listAll();

    /**
     * 根据ID获取功能开关详情
     */
    FeatureFlag getById(Long id);

    /**
     * 根据Key获取功能开关详情
     */
    FeatureFlag getByKey(String key);

    /**
     * 创建功能开关
     */
    Long create(String key, String description, boolean enabled, String rulesJson);

    /**
     * 更新功能开关
     */
    void update(Long id, String description, boolean enabled, String rulesJson);

    /**
     * 删除功能开关
     */
    void delete(Long id);

    /**
     * 切换功能开关启用状态
     */
    void toggleEnabled(Long id);
}
