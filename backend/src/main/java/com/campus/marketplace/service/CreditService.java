package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.CreditHistoryResponse;
import com.campus.marketplace.common.dto.response.UserCreditInfoResponse;
import com.campus.marketplace.common.enums.CreditLevel;
import org.springframework.data.domain.Page;

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

    /**
     * 获取当前用户的信用信息
     * 
     * @param userId 用户ID
     * @return 信用信息
     */
    UserCreditInfoResponse getMyCredit(Long userId);

    /**
     * 获取指定用户的信用信息
     * 
     * @param userId 用户ID
     * @return 信用信息
     */
    UserCreditInfoResponse getUserCredit(Long userId);

    /**
     * 获取信用历史记录
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 信用历史记录分页
     */
    Page<CreditHistoryResponse> getCreditHistory(Long userId, int page, int size);
}
