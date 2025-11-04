package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.MerchantDashboard;
import com.campus.marketplace.repository.MerchantDashboardRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.service.MerchantDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商家数据看板服务实现
 *
 * @author BaSui 😎
 * @since 2025-11-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantDashboardServiceImpl implements MerchantDashboardService {

    private final MerchantDashboardRepository merchantDashboardRepository;
    private final OrderRepository orderRepository;
    private final GoodsRepository goodsRepository;

    @Override
    public Map<String, Object> getTodayOverview(Long merchantId) {
        log.debug("获取商家{}今日数据概览", merchantId);
        
        LocalDate today = LocalDate.now();
        
        // 查询今日看板数据
        MerchantDashboard dashboard = merchantDashboardRepository
            .findByMerchantIdAndStatDate(merchantId, today)
            .orElse(null);
        
        Map<String, Object> overview = new HashMap<>();
        
        if (dashboard != null) {
            overview.put("date", today);
            overview.put("salesAmount", dashboard.getSalesAmount());
            overview.put("orderCount", dashboard.getOrderCount());
            overview.put("visitorCount", dashboard.getVisitorCount());
            overview.put("newVisitorCount", dashboard.getNewVisitorCount());
            overview.put("pageViewCount", dashboard.getPageViewCount());
            overview.put("conversionRate", dashboard.getConversionRate());
            overview.put("topSellingGoods", dashboard.getTopSellingGoods());
            overview.put("visitorSources", dashboard.getVisitorSources());
        } else {
            // 如果今日数据未生成，返回空数据
            overview.put("date", today);
            overview.put("salesAmount", BigDecimal.ZERO);
            overview.put("orderCount", 0);
            overview.put("visitorCount", 0);
            overview.put("newVisitorCount", 0);
            overview.put("pageViewCount", 0);
            overview.put("conversionRate", BigDecimal.ZERO);
            overview.put("topSellingGoods", List.of());
            overview.put("visitorSources", Map.of());
        }
        
        return overview;
    }

    @Override
    public Map<String, Object> getSalesTrend(Long merchantId, int days) {
        log.debug("获取商家{}近{}天销售趋势", merchantId, days);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        
        // 查询时间范围内的数据
        List<MerchantDashboard> dashboards = merchantDashboardRepository
            .findByMerchantIdAndDateRange(merchantId, startDate, endDate);
        
        Map<String, Object> trend = new HashMap<>();
        
        // 按日期排序
        List<Map<String, Object>> dailyData = dashboards.stream()
            .sorted(Comparator.comparing(MerchantDashboard::getStatDate))
            .map(dashboard -> {
                Map<String, Object> data = new HashMap<>();
                data.put("date", dashboard.getStatDate());
                data.put("salesAmount", dashboard.getSalesAmount());
                data.put("orderCount", dashboard.getOrderCount());
                data.put("visitorCount", dashboard.getVisitorCount());
                data.put("conversionRate", dashboard.getConversionRate());
                return data;
            })
            .collect(Collectors.toList());
        
        trend.put("dailyData", dailyData);
        
        // 计算总计和平均值
        BigDecimal totalSales = dashboards.stream()
            .map(MerchantDashboard::getSalesAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalOrders = dashboards.stream()
            .mapToInt(MerchantDashboard::getOrderCount)
            .sum();
        
        int totalVisitors = dashboards.stream()
            .mapToInt(MerchantDashboard::getVisitorCount)
            .sum();
        
        trend.put("totalSales", totalSales);
        trend.put("totalOrders", totalOrders);
        trend.put("totalVisitors", totalVisitors);
        trend.put("averageSales", dashboards.isEmpty() ? BigDecimal.ZERO :
            totalSales.divide(BigDecimal.valueOf(dashboards.size()), 2, java.math.RoundingMode.HALF_UP));
        trend.put("averageOrders", dashboards.isEmpty() ? 0 : totalOrders / dashboards.size());
        
        return trend;
    }

    @Override
    public Map<String, Object> getGoodsRanking(Long merchantId) {
        log.debug("获取商家{}商品排行榜", merchantId);
        
        LocalDate today = LocalDate.now();
        
        // 查询今日看板数据
        MerchantDashboard dashboard = merchantDashboardRepository
            .findByMerchantIdAndStatDate(merchantId, today)
            .orElse(null);
        
        Map<String, Object> ranking = new HashMap<>();
        
        if (dashboard != null && dashboard.getTopSellingGoods() != null) {
            List<Long> topGoodsIds = dashboard.getTopSellingGoods();
            
            // 查询商品详情
            List<Map<String, Object>> topGoods = topGoodsIds.stream()
                .limit(10)
                .map(goodsId -> goodsRepository.findById(goodsId).orElse(null))
                .filter(Objects::nonNull)
                .map(goods -> {
                    Map<String, Object> goodsInfo = new HashMap<>();
                    goodsInfo.put("id", goods.getId());
                    goodsInfo.put("title", goods.getTitle());
                    goodsInfo.put("price", goods.getPrice());
                    // 获取第一张图片作为封面
                    String coverImage = goods.getImages() != null && goods.getImages().length > 0 ? 
                        goods.getImages()[0] : null;
                    goodsInfo.put("coverImage", coverImage);
                    return goodsInfo;
                })
                .collect(Collectors.toList());
            
            ranking.put("topSellingGoods", topGoods);
        } else {
            ranking.put("topSellingGoods", List.of());
        }
        
        return ranking;
    }

    @Override
    public Map<String, Object> getVisitorAnalysis(Long merchantId) {
        log.debug("获取商家{}访客分析", merchantId);
        
        LocalDate today = LocalDate.now();
        
        // 查询今日看板数据
        MerchantDashboard dashboard = merchantDashboardRepository
            .findByMerchantIdAndStatDate(merchantId, today)
            .orElse(null);
        
        Map<String, Object> analysis = new HashMap<>();
        
        if (dashboard != null) {
            analysis.put("visitorCount", dashboard.getVisitorCount());
            analysis.put("newVisitorCount", dashboard.getNewVisitorCount());
            analysis.put("pageViewCount", dashboard.getPageViewCount());
            analysis.put("visitorSources", dashboard.getVisitorSources() != null ? 
                dashboard.getVisitorSources() : Map.of());
            
            // 计算老访客数
            int returningVisitors = dashboard.getVisitorCount() - dashboard.getNewVisitorCount();
            analysis.put("returningVisitorCount", Math.max(0, returningVisitors));
            
            // 计算人均浏览量
            double avgPageViews = dashboard.getVisitorCount() > 0 ? 
                (double) dashboard.getPageViewCount() / dashboard.getVisitorCount() : 0.0;
            analysis.put("avgPageViews", String.format("%.2f", avgPageViews));
        } else {
            analysis.put("visitorCount", 0);
            analysis.put("newVisitorCount", 0);
            analysis.put("pageViewCount", 0);
            analysis.put("returningVisitorCount", 0);
            analysis.put("avgPageViews", "0.00");
            analysis.put("visitorSources", Map.of());
        }
        
        return analysis;
    }

    @Override
    @Transactional
    public void generateDailyReport(LocalDate date) {
        log.info("开始生成{}的商家日报", date);
        
        // 获取所有商家列表
        List<Long> merchantIds = goodsRepository.findAll().stream()
            .map(goods -> goods.getSellerId())
            .distinct()
            .collect(Collectors.toList());
        
        log.info("需要生成日报的商家数量: {}", merchantIds.size());
        
        int generatedCount = 0;
        for (Long merchantId : merchantIds) {
            try {
                generateMerchantDailyReport(merchantId, date);
                generatedCount++;
            } catch (Exception e) {
                log.error("生成商家{}的日报失败: {}", merchantId, e.getMessage(), e);
            }
        }
        
        log.info("商家日报生成完成，共生成{}条记录", generatedCount);
    }
    
    /**
     * 生成单个商家的日报
     */
    private void generateMerchantDailyReport(Long merchantId, LocalDate date) {
        // 检查是否已存在
        if (merchantDashboardRepository.existsByMerchantIdAndStatDate(merchantId, date)) {
            log.debug("商家{}的{}日报已存在，跳过生成", merchantId, date);
            return;
        }
        
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.atTime(LocalTime.MAX);
        
        // 统计销售数据
        List<com.campus.marketplace.common.entity.Order> orders = orderRepository.findAll().stream()
            .filter(order -> order.getCreatedAt().isAfter(startTime) && 
                           order.getCreatedAt().isBefore(endTime))
            .filter(order -> {
                // 根据订单中的商品判断是否属于该商家
                // 简化处理：这里需要关联查询订单商品表
                return true; // 待完善
            })
            .collect(Collectors.toList());
        
        BigDecimal salesAmount = orders.stream()
            .map(com.campus.marketplace.common.entity.Order::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int orderCount = orders.size();
        
        // 统计访客数据（简化处理）
        int visitorCount = 0;
        int newVisitorCount = 0;
        int pageViewCount = 0;
        
        // 计算转化率
        BigDecimal conversionRate = visitorCount > 0 ?
            BigDecimal.valueOf(orderCount).divide(BigDecimal.valueOf(visitorCount), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
        
        // 构建看板数据
        MerchantDashboard dashboard = MerchantDashboard.builder()
            .merchantId(merchantId)
            .statDate(date)
            .salesAmount(salesAmount)
            .orderCount(orderCount)
            .visitorCount(visitorCount)
            .newVisitorCount(newVisitorCount)
            .pageViewCount(pageViewCount)
            .conversionRate(conversionRate)
            .visitorSources(Map.of("搜索", 0, "推荐", 0, "直接访问", 0)) // 简化处理
            .topSellingGoods(List.of()) // 待完善
            .build();
        
        merchantDashboardRepository.save(dashboard);
        log.debug("商家{}的{}日报生成成功", merchantId, date);
    }
}
