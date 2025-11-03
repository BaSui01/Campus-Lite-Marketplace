package com.campus.marketplace.service;

import java.time.LocalDate;
import java.util.Map;

/**
 * 商家数据看板服务接口
 *
 * @author BaSui 😎
 * @since 2025-11-04
 */
public interface MerchantDashboardService {

    /**
     * 获取商家今日数据概览
     */
    Map<String, Object> getTodayOverview(Long merchantId);

    /**
     * 获取商家销售趋势（近N天）
     */
    Map<String, Object> getSalesTrend(Long merchantId, int days);

    /**
     * 获取商家商品排行榜
     */
    Map<String, Object> getGoodsRanking(Long merchantId);

    /**
     * 获取商家访客分析
     */
    Map<String, Object> getVisitorAnalysis(Long merchantId);

    /**
     * 生成商家日报（定时任务）
     */
    void generateDailyReport(LocalDate date);
}
