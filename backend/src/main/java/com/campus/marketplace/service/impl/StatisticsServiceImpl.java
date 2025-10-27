package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.repository.*;
import com.campus.marketplace.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据统计服务实现类
 * 
 * 功能：系统数据统计和分析
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UserRepository userRepository;
    private final GoodsRepository goodsRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            long totalUsers = userRepository.count();
            long totalGoods = goodsRepository.count();
            long totalOrders = orderRepository.count();
            
            // 计算总收入（已完成订单）
            BigDecimal totalRevenue = orderRepository.findAll().stream()
                    .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                    .map(order -> order.getActualAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            overview.put("totalUsers", totalUsers);
            overview.put("totalGoods", totalGoods);
            overview.put("totalOrders", totalOrders);
            overview.put("totalRevenue", totalRevenue);
            
            log.debug("✅ 系统概览统计成功: users={}, goods={}, orders={}, revenue={}", 
                    totalUsers, totalGoods, totalOrders, totalRevenue);
        } catch (Exception e) {
            log.error("❌ 系统概览统计失败: {}", e.getMessage());
        }
        
        return overview;
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> userStats = new HashMap<>();
        
        try {
            long totalUsers = userRepository.count();
            
            // 活跃用户（状态为 ACTIVE）
            long activeUsers = userRepository.findAll().stream()
                    .filter(user -> user.isActive())
                    .count();
            
            // 被封禁用户
            long bannedUsers = userRepository.findAll().stream()
                    .filter(user -> user.isBanned())
                    .count();
            
            // 今日新增用户
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            long newUsersToday = userRepository.findAll().stream()
                    .filter(user -> user.getCreatedAt().isAfter(todayStart))
                    .count();
            
            userStats.put("totalUsers", totalUsers);
            userStats.put("activeUsers", activeUsers);
            userStats.put("bannedUsers", bannedUsers);
            userStats.put("newUsersToday", newUsersToday);
            
            log.debug("✅ 用户统计成功: total={}, active={}, banned={}, newToday={}", 
                    totalUsers, activeUsers, bannedUsers, newUsersToday);
        } catch (Exception e) {
            log.error("❌ 用户统计失败: {}", e.getMessage());
        }
        
        return userStats;
    }

    @Override
    public Map<String, Object> getGoodsStatistics() {
        Map<String, Object> goodsStats = new HashMap<>();
        
        try {
            long totalGoods = goodsRepository.count();
            
            // 在售物品
            long onSaleGoods = goodsRepository.findAll().stream()
                    .filter(goods -> goods.getStatus() == GoodsStatus.APPROVED)
                    .count();
            
            // 已售出物品
            long soldGoods = goodsRepository.findAll().stream()
                    .filter(goods -> goods.getStatus() == GoodsStatus.SOLD)
                    .count();
            
            // 待审核物品
            long pendingApprovalGoods = goodsRepository.findAll().stream()
                    .filter(goods -> goods.getStatus() == GoodsStatus.PENDING)
                    .count();
            
            goodsStats.put("totalGoods", totalGoods);
            goodsStats.put("onSaleGoods", onSaleGoods);
            goodsStats.put("soldGoods", soldGoods);
            goodsStats.put("pendingApprovalGoods", pendingApprovalGoods);
            
            log.debug("✅ 物品统计成功: total={}, onSale={}, sold={}, pending={}", 
                    totalGoods, onSaleGoods, soldGoods, pendingApprovalGoods);
        } catch (Exception e) {
            log.error("❌ 物品统计失败: {}", e.getMessage());
        }
        
        return goodsStats;
    }

    @Override
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> orderStats = new HashMap<>();
        
        try {
            long totalOrders = orderRepository.count();
            
            // 已完成订单
            long completedOrders = orderRepository.findAll().stream()
                    .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                    .count();
            
            // 已取消订单
            long cancelledOrders = orderRepository.findAll().stream()
                    .filter(order -> order.getStatus() == OrderStatus.CANCELLED)
                    .count();
            
            // 总收入
            BigDecimal totalRevenue = orderRepository.findAll().stream()
                    .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                    .map(order -> order.getActualAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            orderStats.put("totalOrders", totalOrders);
            orderStats.put("completedOrders", completedOrders);
            orderStats.put("cancelledOrders", cancelledOrders);
            orderStats.put("totalRevenue", totalRevenue);
            
            log.debug("✅ 订单统计成功: total={}, completed={}, cancelled={}, revenue={}", 
                    totalOrders, completedOrders, cancelledOrders, totalRevenue);
        } catch (Exception e) {
            log.error("❌ 订单统计失败: {}", e.getMessage());
        }
        
        return orderStats;
    }

    @Override
    public Map<String, Object> getTodayStatistics() {
        Map<String, Object> todayStats = new HashMap<>();
        
        try {
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            
            // 今日新增用户
            long newUsers = userRepository.findAll().stream()
                    .filter(user -> user.getCreatedAt().isAfter(todayStart))
                    .count();
            
            // 今日新增物品
            long newGoods = goodsRepository.findAll().stream()
                    .filter(goods -> goods.getCreatedAt().isAfter(todayStart))
                    .count();
            
            // 今日新增订单
            long newOrders = orderRepository.findAll().stream()
                    .filter(order -> order.getCreatedAt().isAfter(todayStart))
                    .count();
            
            // 今日收入
            BigDecimal todayRevenue = orderRepository.findAll().stream()
                    .filter(order -> order.getStatus() == OrderStatus.COMPLETED && 
                            order.getCreatedAt().isAfter(todayStart))
                    .map(order -> order.getActualAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            todayStats.put("newUsers", newUsers);
            todayStats.put("newGoods", newGoods);
            todayStats.put("newOrders", newOrders);
            todayStats.put("todayRevenue", todayRevenue);
            
            log.debug("✅ 今日统计成功: users={}, goods={}, orders={}, revenue={}", 
                    newUsers, newGoods, newOrders, todayRevenue);
        } catch (Exception e) {
            log.error("❌ 今日统计失败: {}", e.getMessage());
        }
        
        return todayStats;
    }

    @Override
    public Map<String, Long> getCategoryStatistics() {
        Map<String, Long> categoryStats = new HashMap<>();
        
        try {
            // 按分类统计物品数量
            goodsRepository.findAll().stream()
                    .collect(Collectors.groupingBy(goods -> goods.getCategoryId(), Collectors.counting()))
                    .forEach((categoryId, count) -> {
                        categoryRepository.findById(categoryId).ifPresent(category -> {
                            categoryStats.put(category.getName(), count);
                        });
                    });
            
            log.debug("✅ 分类统计成功: categories={}", categoryStats.size());
        } catch (Exception e) {
            log.error("❌ 分类统计失败: {}", e.getMessage());
        }
        
        return categoryStats;
    }

    @Override
    public Map<String, Object> getTrendData(int days) {
        Map<String, Object> trendData = new HashMap<>();
        
        try {
            List<String> dates = new ArrayList<>();
            List<Long> userCounts = new ArrayList<>();
            List<Long> goodsCounts = new ArrayList<>();
            List<Long> orderCounts = new ArrayList<>();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                LocalDateTime dayStart = date.atStartOfDay();
                LocalDateTime dayEnd = dayStart.plusDays(1);
                
                dates.add(date.format(formatter));
                
                // 统计当天新增数据
                long userCount = userRepository.findAll().stream()
                        .filter(user -> user.getCreatedAt().isAfter(dayStart) && 
                                user.getCreatedAt().isBefore(dayEnd))
                        .count();
                
                long goodsCount = goodsRepository.findAll().stream()
                        .filter(goods -> goods.getCreatedAt().isAfter(dayStart) && 
                                goods.getCreatedAt().isBefore(dayEnd))
                        .count();
                
                long orderCount = orderRepository.findAll().stream()
                        .filter(order -> order.getCreatedAt().isAfter(dayStart) && 
                                order.getCreatedAt().isBefore(dayEnd))
                        .count();
                
                userCounts.add(userCount);
                goodsCounts.add(goodsCount);
                orderCounts.add(orderCount);
            }
            
            trendData.put("dates", dates);
            trendData.put("userCounts", userCounts);
            trendData.put("goodsCounts", goodsCounts);
            trendData.put("orderCounts", orderCounts);
            
            log.debug("✅ 趋势数据统计成功: days={}", days);
        } catch (Exception e) {
            log.error("❌ 趋势数据统计失败: {}", e.getMessage());
        }
        
        return trendData;
    }

    @Override
    public List<Map<String, Object>> getTopGoods(int limit) {
        try {
            return goodsRepository.findAll().stream()
                    .sorted((g1, g2) -> Integer.compare(g2.getViewCount(), g1.getViewCount()))
                    .limit(limit)
                    .map(goods -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", goods.getId());
                        item.put("title", goods.getTitle());
                        item.put("viewCount", goods.getViewCount());
                        item.put("favoriteCount", goods.getFavoriteCount());
                        return item;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ 热门物品统计失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getTopUsers(int limit) {
        try {
            // 按发布物品数量排序
            Map<Long, Long> userGoodsCount = goodsRepository.findAll().stream()
                    .collect(Collectors.groupingBy(goods -> goods.getSellerId(), Collectors.counting()));
            
            return userGoodsCount.entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .limit(limit)
                    .map(entry -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("userId", entry.getKey());
                        item.put("goodsCount", entry.getValue());
                        userRepository.findById(entry.getKey()).ifPresent(user -> {
                            item.put("username", user.getUsername());
                        });
                        return item;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ 活跃用户统计失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getRevenueByMonth(int months) {
        Map<String, Object> revenueData = new HashMap<>();
        
        try {
            List<String> monthLabels = new ArrayList<>();
            List<BigDecimal> revenues = new ArrayList<>();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            
            for (int i = months - 1; i >= 0; i--) {
                LocalDate month = LocalDate.now().minusMonths(i);
                LocalDateTime monthStart = month.withDayOfMonth(1).atStartOfDay();
                LocalDateTime monthEnd = monthStart.plusMonths(1);
                
                monthLabels.add(month.format(formatter));
                
                // 统计当月收入
                BigDecimal monthRevenue = orderRepository.findAll().stream()
                        .filter(order -> order.getStatus() == OrderStatus.COMPLETED && 
                                order.getCreatedAt().isAfter(monthStart) && 
                                order.getCreatedAt().isBefore(monthEnd))
                        .map(order -> order.getActualAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                revenues.add(monthRevenue);
            }
            
            revenueData.put("months", monthLabels);
            revenueData.put("revenues", revenues);
            
            log.debug("✅ 收入统计成功: months={}", months);
        } catch (Exception e) {
            log.error("❌ 收入统计失败: {}", e.getMessage());
        }
        
        return revenueData;
    }
}

