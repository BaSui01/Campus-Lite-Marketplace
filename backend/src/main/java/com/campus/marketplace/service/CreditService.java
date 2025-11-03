package com.campus.marketplace.service;

import com.campus.marketplace.common.enums.CreditLevel;

/**
 * 信用服务接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface CreditService {

    /**
     * 计算用户信用等级
     * 
     * @param userId 用户ID
     * @return 信用等级
     */
    CreditLevel calculateCreditLevel(Long userId);

    /**
     * 获取用户信用等级描述
     * 
     * @param userId 用户ID
     * @return 信用等级描述
     */
    String getCreditLevelDescription(Long userId);

    /**
     * 计算平均响应时间（分钟）
     * 
     * @param sellerId 卖家ID
     * @return 平均响应时间
     */
    Integer calculateAvgResponseTime(Long sellerId);
}
