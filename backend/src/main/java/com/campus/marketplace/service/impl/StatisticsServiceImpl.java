package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.OrderStatisticsDTO;
import com.campus.marketplace.common.dto.response.RefundStatisticsDTO;
import com.campus.marketplace.common.dto.response.SystemOverviewDTO;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.RefundRequest;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.enums.RefundStatus;
import com.campus.marketplace.repository.*;
import com.campus.marketplace.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
 * @updated 2025-11-10 - 使用强类型 DTO 替代 Map<String, Object> 😎
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UserRepository userRepository;
    private final GoodsRepository goodsRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final RefundRequestRepository refundRequestRepository;

    @Override
    public SystemOverviewDTO getSystemOverview() {
        try {
            // ==================== 总体统计 ====================
            long totalUsers = userRepository.count();
            long totalGoods = goodsRepository.count();
            long totalOrders = orderRepository.count();

            // 计算总收入（已完成订单）
            BigDecimal totalRevenue = orderRepository.findAll().stream()
                    .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                    .map(order -> order.getActualAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // ==================== 今日统计 ====================
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();

            long todayNewUsers = userRepository.findAll().stream()
                    .filter(user -> user.getCreatedAt().isAfter(todayStart))
                    .count();

            long todayNewGoods = goodsRepository.findAll().stream()
                    .filter(goods -> goods.getCreatedAt().isAfter(todayStart))
                    .count();

            long todayNewOrders = orderRepository.findAll().stream()
                    .filter(order -> order.getCreatedAt().isAfter(todayStart))
                    .count();

            // ==================== 活跃统计 ====================
            long activeUsers = userRepository.findAll().stream()
                    .filter(user -> user.isActive())
                    .count();

            long pendingGoods = goodsRepository.findAll().stream()
                    .filter(goods -> goods.getStatus() == GoodsStatus.PENDING)
                    .count();

            // ==================== 构建 DTO ====================
            SystemOverviewDTO overview = SystemOverviewDTO.builder()
                    .totalUsers(totalUsers)
                    .totalGoods(totalGoods)
                    .totalOrders(totalOrders)
                    .totalRevenue(totalRevenue)
                    .todayNewUsers(todayNewUsers)
                    .todayNewGoods(todayNewGoods)
                    .todayNewOrders(todayNewOrders)
                    .activeUsers(activeUsers)
                    .pendingGoods(pendingGoods)
                    .build();

            log.debug("✅ 系统概览统计成功: users={}, goods={}, orders={}, revenue={}",
                    totalUsers, totalGoods, totalOrders, totalRevenue);

            return overview;
        } catch (Exception e) {
            log.error("❌ 系统概览统计失败: {}", e.getMessage());
            // 返回空数据的 DTO
            return SystemOverviewDTO.builder()
                    .totalUsers(0L)
                    .totalGoods(0L)
                    .totalOrders(0L)
                    .totalRevenue(BigDecimal.ZERO)
                    .todayNewUsers(0L)
                    .todayNewGoods(0L)
                    .todayNewOrders(0L)
                    .activeUsers(0L)
                    .pendingGoods(0L)
                    .build();
        }
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

    @Override
    public OrderStatisticsDTO getOrderStatisticsEnhanced(String startDate, String endDate) {
        log.info("📊 获取订单统计数据: startDate={}, endDate={}", startDate, endDate);

        try {
            LocalDateTime start = parseDateTime(startDate, true);
            LocalDateTime end = parseDateTime(endDate, false);

            // 获取所有订单或时间范围内的订单
            List<Order> orders = orderRepository.findAll().stream()
                    .filter(order -> {
                        if (start != null && order.getCreatedAt().isBefore(start)) return false;
                        if (end != null && order.getCreatedAt().isAfter(end)) return false;
                        return true;
                    })
                    .toList();

            // ==================== 总体统计 ====================
            long totalOrders = orders.size();
            long pendingPaymentOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING_PAYMENT).count();
            long paidOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.PAID).count();
            long completedOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
            long cancelledOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
            long refundingOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.REFUNDING).count();
            long refundedOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.REFUNDED).count();

            // ==================== 金额统计 ====================
            BigDecimal totalAmount = orders.stream().map(Order::getActualAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal completedAmount = orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .map(Order::getActualAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal refundedAmount = orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.REFUNDED)
                    .map(Order::getActualAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal averageAmount = totalOrders > 0 ?
                    totalAmount.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            // ==================== 比率统计 ====================
            double completionRate = totalOrders > 0 ? (completedOrders * 100.0 / totalOrders) : 0.0;
            double cancellationRate = totalOrders > 0 ? (cancelledOrders * 100.0 / totalOrders) : 0.0;
            double refundRate = totalOrders > 0 ? (refundedOrders * 100.0 / totalOrders) : 0.0;

            // ==================== 按状态统计 ====================
            Map<String, Long> ordersByStatus = orders.stream()
                    .collect(Collectors.groupingBy(o -> o.getStatus().name(), Collectors.counting()));

            // ==================== 按支付方式统计 ====================
            Map<String, BigDecimal> amountByPaymentMethod = new HashMap<>();
            Map<String, Long> countByPaymentMethod = new HashMap<>();
            orders.stream()
                    .filter(o -> o.getPaymentMethod() != null)
                    .forEach(o -> {
                        String method = o.getPaymentMethod();
                        amountByPaymentMethod.merge(method, o.getActualAmount(), BigDecimal::add);
                        countByPaymentMethod.merge(method, 1L, Long::sum);
                    });

            // ==================== 今日统计 ====================
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            long todayNewOrders = orders.stream().filter(o -> o.getCreatedAt().isAfter(todayStart)).count();
            BigDecimal todayAmount = orders.stream()
                    .filter(o -> o.getCreatedAt().isAfter(todayStart))
                    .map(Order::getActualAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long todayCompletedOrders = orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED && o.getCreatedAt().isAfter(todayStart))
                    .count();

            return OrderStatisticsDTO.builder()
                    .totalOrders(totalOrders)
                    .pendingPaymentOrders(pendingPaymentOrders)
                    .paidOrders(paidOrders)
                    .completedOrders(completedOrders)
                    .cancelledOrders(cancelledOrders)
                    .refundingOrders(refundingOrders)
                    .refundedOrders(refundedOrders)
                    .totalAmount(totalAmount)
                    .completedAmount(completedAmount)
                    .refundedAmount(refundedAmount)
                    .averageAmount(averageAmount)
                    .completionRate(Math.round(completionRate * 10.0) / 10.0)
                    .cancellationRate(Math.round(cancellationRate * 10.0) / 10.0)
                    .refundRate(Math.round(refundRate * 10.0) / 10.0)
                    .ordersByStatus(ordersByStatus)
                    .amountByPaymentMethod(amountByPaymentMethod)
                    .countByPaymentMethod(countByPaymentMethod)
                    .todayNewOrders(todayNewOrders)
                    .todayAmount(todayAmount)
                    .todayCompletedOrders(todayCompletedOrders)
                    .build();

        } catch (Exception e) {
            log.error("❌ 订单统计失败: {}", e.getMessage(), e);
            return OrderStatisticsDTO.builder()
                    .totalOrders(0L)
                    .totalAmount(BigDecimal.ZERO)
                    .build();
        }
    }

    @Override
    public RefundStatisticsDTO getRefundStatistics(String startDate, String endDate) {
        log.info("💰 获取退款统计数据: startDate={}, endDate={}", startDate, endDate);

        try {
            LocalDateTime start = parseDateTime(startDate, true);
            LocalDateTime end = parseDateTime(endDate, false);

            // 获取所有退款或时间范围内的退款
            List<RefundRequest> refunds = refundRequestRepository.findAll().stream()
                    .filter(refund -> {
                        if (start != null && refund.getCreatedAt().isBefore(start)) return false;
                        if (end != null && refund.getCreatedAt().isAfter(end)) return false;
                        return true;
                    })
                    .toList();

            // ==================== 总体统计 ====================
            long totalRefunds = refunds.size();
            long appliedRefunds = refunds.stream().filter(r -> r.getStatus() == RefundStatus.APPLIED).count();
            long approvedRefunds = refunds.stream().filter(r -> r.getStatus() == RefundStatus.APPROVED).count();
            long rejectedRefunds = refunds.stream().filter(r -> r.getStatus() == RefundStatus.REJECTED).count();
            long processingRefunds = refunds.stream().filter(r -> r.getStatus() == RefundStatus.PROCESSING).count();
            long completedRefunds = refunds.stream().filter(r -> r.getStatus() == RefundStatus.REFUNDED).count();
            long failedRefunds = refunds.stream().filter(r -> r.getStatus() == RefundStatus.FAILED).count();

            // ==================== 金额统计 ====================
            BigDecimal totalAmount = refunds.stream().map(RefundRequest::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal completedAmount = refunds.stream()
                    .filter(r -> r.getStatus() == RefundStatus.REFUNDED)
                    .map(RefundRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal processingAmount = refunds.stream()
                    .filter(r -> r.getStatus() == RefundStatus.PROCESSING)
                    .map(RefundRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal averageAmount = totalRefunds > 0 ?
                    totalAmount.divide(BigDecimal.valueOf(totalRefunds), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            // ==================== 比率统计 ====================
            long totalReviewed = approvedRefunds + rejectedRefunds;
            double approvalRate = totalReviewed > 0 ? (approvedRefunds * 100.0 / totalReviewed) : 0.0;
            long totalProcessed = completedRefunds + failedRefunds;
            double successRate = totalProcessed > 0 ? (completedRefunds * 100.0 / totalProcessed) : 0.0;
            double failureRate = totalProcessed > 0 ? (failedRefunds * 100.0 / totalProcessed) : 0.0;

            // ==================== 按状态统计 ====================
            Map<String, Long> refundsByStatus = refunds.stream()
                    .collect(Collectors.groupingBy(r -> r.getStatus().name(), Collectors.counting()));

            // ==================== 按渠道统计 ====================
            Map<String, BigDecimal> amountByChannel = new HashMap<>();
            Map<String, Long> countByChannel = new HashMap<>();
            refunds.stream()
                    .filter(r -> r.getChannel() != null)
                    .forEach(r -> {
                        String channel = r.getChannel();
                        amountByChannel.merge(channel, r.getAmount(), BigDecimal::add);
                        countByChannel.merge(channel, 1L, Long::sum);
                    });

            // ==================== 今日统计 ====================
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            long todayNewRefunds = refunds.stream().filter(r -> r.getCreatedAt().isAfter(todayStart)).count();
            BigDecimal todayAmount = refunds.stream()
                    .filter(r -> r.getCreatedAt().isAfter(todayStart))
                    .map(RefundRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long todayCompletedRefunds = refunds.stream()
                    .filter(r -> r.getStatus() == RefundStatus.REFUNDED && r.getCreatedAt().isAfter(todayStart))
                    .count();

            // ==================== 平均处理时间 ====================
            // 平均审核时间（从申请到批准/拒绝）
            double avgReviewTime = refunds.stream()
                    .filter(r -> r.getStatus() == RefundStatus.APPROVED || r.getStatus() == RefundStatus.REJECTED)
                    .mapToLong(r -> Duration.between(r.getCreatedAt(), r.getUpdatedAt()).toHours())
                    .average()
                    .orElse(0.0);

            // 平均完成时间（从申请到完成）
            double avgCompletionTime = refunds.stream()
                    .filter(r -> r.getStatus() == RefundStatus.REFUNDED)
                    .mapToLong(r -> Duration.between(r.getCreatedAt(), r.getUpdatedAt()).toHours())
                    .average()
                    .orElse(0.0);

            return RefundStatisticsDTO.builder()
                    .totalRefunds(totalRefunds)
                    .appliedRefunds(appliedRefunds)
                    .approvedRefunds(approvedRefunds)
                    .rejectedRefunds(rejectedRefunds)
                    .processingRefunds(processingRefunds)
                    .completedRefunds(completedRefunds)
                    .failedRefunds(failedRefunds)
                    .totalAmount(totalAmount)
                    .completedAmount(completedAmount)
                    .processingAmount(processingAmount)
                    .averageAmount(averageAmount)
                    .approvalRate(Math.round(approvalRate * 10.0) / 10.0)
                    .successRate(Math.round(successRate * 10.0) / 10.0)
                    .failureRate(Math.round(failureRate * 10.0) / 10.0)
                    .refundsByStatus(refundsByStatus)
                    .amountByChannel(amountByChannel)
                    .countByChannel(countByChannel)
                    .todayNewRefunds(todayNewRefunds)
                    .todayAmount(todayAmount)
                    .todayCompletedRefunds(todayCompletedRefunds)
                    .avgReviewTime(Math.round(avgReviewTime * 10.0) / 10.0)
                    .avgCompletionTime(Math.round(avgCompletionTime * 10.0) / 10.0)
                    .build();

        } catch (Exception e) {
            log.error("❌ 退款统计失败: {}", e.getMessage(), e);
            return RefundStatisticsDTO.builder()
                    .totalRefunds(0L)
                    .totalAmount(BigDecimal.ZERO)
                    .build();
        }
    }

    /**
     * 解析日期时间
     */
    private LocalDateTime parseDateTime(String dateStr, boolean isStart) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return isStart ? date.atStartOfDay() : date.atTime(LocalTime.MAX);
        } catch (Exception e) {
            log.warn("日期解析失败: {}", dateStr);
            return null;
        }
    }
}

