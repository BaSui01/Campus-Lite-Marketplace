package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.PointsLog;
import com.campus.marketplace.common.enums.PointsType;
import org.springframework.data.domain.Page;

/**
 * 积分服务接口
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public interface PointsService {

    /**
     * 增加积分
     * 
     * @param userId 用户ID
     * @param type 积分类型
     * @param description 描述
     */
    void addPoints(Long userId, PointsType type, String description);

    /**
     * 扣除积分
     * 
     * @param userId 用户ID
     * @param points 积分数量
     * @param description 描述
     */
    void deductPoints(Long userId, Integer points, String description);

    /**
     * 查询积分流水
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 积分流水
     */
    Page<PointsLog> getPointsLog(Long userId, int page, int size);

    /**
     * 查询用户当前积分
     * 
     * @param userId 用户ID
     * @return 当前积分
     */
    Integer getCurrentPoints(Long userId);
}
