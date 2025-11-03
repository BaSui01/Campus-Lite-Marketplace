package com.campus.marketplace.service;

/**
 * 内容审核服务接口
 * 
 * 提供内容审核功能：敏感词过滤、广告识别、垃圾内容检测
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface ContentAuditService {

    /**
     * 审核内容（综合检查）
     * 
     * @param content 内容
     * @return 审核结果（true=通过，false=拒绝）
     */
    boolean auditContent(String content);

    /**
     * 过滤敏感词
     * 
     * @param content 内容
     * @return 过滤后的内容
     */
    String filterSensitiveWords(String content);

    /**
     * 检测广告内容
     * 
     * @param content 内容
     * @return 是否包含广告
     */
    boolean detectAdvertisement(String content);

    /**
     * 检测垃圾内容
     * 
     * @param userId 用户ID
     * @param content 内容
     * @return 是否为垃圾内容
     */
    boolean detectSpam(Long userId, String content);
}
