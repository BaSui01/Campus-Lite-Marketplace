package com.campus.marketplace.service;

import java.util.List;

/**
 * 管理端软删除数据治理服务
 *
 * @author BaSui
 * @date 2025-10-29
 */

public interface SoftDeleteAdminService {

    /**
     * 恢复指定实体的软删除记录
     *
     * @param entityKey 实体标识（如 post、goods）
     * @param id 主键
     */
    void restore(String entityKey, Long id);

    /**
     * 彻底删除指定实体记录
     *
     * @param entityKey 实体标识
     * @param id 主键
     */
    void purge(String entityKey, Long id);

    /**
     * 支持的实体列表
     */
    List<String> listTargets();
}
