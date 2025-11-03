package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.MarketingCampaign;

import java.util.List;

/**
 * 营销活动服务接口
 *
 * @author BaSui 😎
 * @since 2025-11-04
 */
public interface MarketingCampaignService {

    /**
     * 创建营销活动
     */
    MarketingCampaign createCampaign(MarketingCampaign campaign);

    /**
     * 获取商家的所有活动
     */
    List<MarketingCampaign> getMerchantCampaigns(Long merchantId);

    /**
     * 获取进行中的活动
     */
    List<MarketingCampaign> getRunningCampaigns();

    /**
     * 审核活动
     */
    void approveCampaign(Long campaignId);

    /**
     * 拒绝活动
     */
    void rejectCampaign(Long campaignId, String reason);

    /**
     * 暂停活动
     */
    void pauseCampaign(Long campaignId);

    /**
     * 恢复活动
     */
    void resumeCampaign(Long campaignId);

    /**
     * 结束活动
     */
    void endCampaign(Long campaignId);

    /**
     * 自动更新活动状态（定时任务）
     */
    void autoUpdateCampaignStatus();

    /**
     * 扣减活动库存（秒杀）
     */
    boolean deductStock(Long campaignId, int quantity);
}
