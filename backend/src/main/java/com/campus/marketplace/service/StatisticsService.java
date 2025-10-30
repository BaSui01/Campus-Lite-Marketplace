package com.campus.marketplace.service;

import java.util.List;
import java.util.Map;

/**
 * 数据统计服务接口
 * 
 * 功能：
 * 1. 系统概览统计
 * 2. 用户统计
 * 3. 物品统计
 * 4. 订单统计
 * 5. 趋势分析
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public interface StatisticsService {

    /**
     * 获取系统概览统计
     * 
     * @return 系统统计数据
     */
    Map<String, Object> getSystemOverview();

    /**
     * 获取用户统计数据
     * 
     * @return 用户统计数据
     */
    Map<String, Object> getUserStatistics();

    /**
     * 获取物品统计数据
     * 
     * @return 物品统计数据
     */
    Map<String, Object> getGoodsStatistics();

    /**
     * 获取订单统计数据
     * 
     * @return 订单统计数据
     */
    Map<String, Object> getOrderStatistics();

    /**
     * 获取今日统计数据
     * 
     * @return 今日统计数据
     */
    Map<String, Object> getTodayStatistics();

    /**
     * 获取分类统计数据
     * 
     * @return 分类统计数据
     */
    Map<String, Long> getCategoryStatistics();

    /**
     * 获取趋势数据
     * 
     * @param days 天数
     * @return 趋势数据
     */
    Map<String, Object> getTrendData(int days);

    /**
     * 获取热门物品排行榜
     * 
     * @param limit 数量限制
     * @return 热门物品列表
     */
    List<Map<String, Object>> getTopGoods(int limit);

    /**
     * 获取活跃用户排行榜
     * 
     * @param limit 数量限制
     * @return 活跃用户列表
     */
    List<Map<String, Object>> getTopUsers(int limit);

    /**
     * 获取收入统计（按月）
     * 
     * @param months 月数
     * @return 收入统计数据
     */
    Map<String, Object> getRevenueByMonth(int months);
}
