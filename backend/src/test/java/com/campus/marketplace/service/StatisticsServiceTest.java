package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.SystemOverviewDTO;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.StatisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 数据统计服务测试类
 *
 * TDD 开发：先写测试，定义数据统计的预期行为
 *
 * @author BaSui
 * @date 2025-10-27
 * @updated 2025-11-10 - 使用强类型 DTO 替代 Map<String, Object> 😎
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("数据统计服务测试")
class StatisticsServiceTest {

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GoodsRepository goodsRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // 每个测试前准备（移除全局 Mock，按需在测试中设置）
    }

    @Test
    @DisplayName("应该获取系统概览统计")
    void shouldGetSystemOverview() {
        // Given: Mock Repository 返回数据
        when(userRepository.count()).thenReturn(0L);
        when(goodsRepository.count()).thenReturn(0L);
        when(orderRepository.count()).thenReturn(0L);
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(goodsRepository.findAll()).thenReturn(Collections.emptyList());

        // When: 获取系统概览统计（强类型 DTO）
        SystemOverviewDTO overview = statisticsService.getSystemOverview();

        // Then: 返回系统统计数据
        assertNotNull(overview);
        assertNotNull(overview.getTotalUsers());
        assertNotNull(overview.getTotalGoods());
        assertNotNull(overview.getTotalOrders());
        assertNotNull(overview.getTotalRevenue());
        assertNotNull(overview.getTodayNewUsers());
        assertNotNull(overview.getTodayNewGoods());
        assertNotNull(overview.getTodayNewOrders());
        assertNotNull(overview.getActiveUsers());
        assertNotNull(overview.getPendingGoods());

        // 验证数据值（应该为 0）
        assertEquals(0L, overview.getTotalUsers());
        assertEquals(0L, overview.getTotalGoods());
        assertEquals(0L, overview.getTotalOrders());
    }

    @Test
    @DisplayName("应该获取用户统计数据")
    void shouldGetUserStatistics() {
        // Given: Mock Repository
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        
        // When: 获取用户统计
        Map<String, Object> userStats = statisticsService.getUserStatistics();

        // Then: 返回用户统计数据
        assertNotNull(userStats);
        assertTrue(userStats.containsKey("totalUsers"));
        assertTrue(userStats.containsKey("activeUsers"));
        assertTrue(userStats.containsKey("bannedUsers"));
        assertTrue(userStats.containsKey("newUsersToday"));
    }

    @Test
    @DisplayName("应该获取物品统计数据")
    void shouldGetGoodsStatistics() {
        // Given: Mock Repository
        when(goodsRepository.count()).thenReturn(0L);
        when(goodsRepository.findAll()).thenReturn(Collections.emptyList());
        
        // When: 获取物品统计
        Map<String, Object> goodsStats = statisticsService.getGoodsStatistics();

        // Then: 返回物品统计数据
        assertNotNull(goodsStats);
        assertTrue(goodsStats.containsKey("totalGoods"));
        assertTrue(goodsStats.containsKey("onSaleGoods"));
        assertTrue(goodsStats.containsKey("soldGoods"));
        assertTrue(goodsStats.containsKey("pendingApprovalGoods"));
    }

    @Test
    @DisplayName("应该获取订单统计数据")
    void shouldGetOrderStatistics() {
        // Given: Mock Repository
        when(orderRepository.count()).thenReturn(0L);
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());
        
        // When: 获取订单统计
        Map<String, Object> orderStats = statisticsService.getOrderStatistics();

        // Then: 返回订单统计数据
        assertNotNull(orderStats);
        assertTrue(orderStats.containsKey("totalOrders"));
        assertTrue(orderStats.containsKey("completedOrders"));
        assertTrue(orderStats.containsKey("cancelledOrders"));
        assertTrue(orderStats.containsKey("totalRevenue"));
    }

    @Test
    @DisplayName("应该获取今日统计数据")
    void shouldGetTodayStatistics() {
        // Given: Mock Repository
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(goodsRepository.findAll()).thenReturn(Collections.emptyList());
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());
        
        // When: 获取今日统计
        Map<String, Object> todayStats = statisticsService.getTodayStatistics();

        // Then: 返回今日统计数据
        assertNotNull(todayStats);
        assertTrue(todayStats.containsKey("newUsers"));
        assertTrue(todayStats.containsKey("newGoods"));
        assertTrue(todayStats.containsKey("newOrders"));
        assertTrue(todayStats.containsKey("todayRevenue"));
    }

    @Test
    @DisplayName("应该获取分类统计数据")
    void shouldGetCategoryStatistics() {
        // Given: Mock Repository
        when(goodsRepository.findAll()).thenReturn(Collections.emptyList());
        
        // When: 获取分类统计
        var categoryStats = statisticsService.getCategoryStatistics();

        // Then: 返回分类统计数据（Map<分类名称, 物品数量>）
        assertNotNull(categoryStats);
        // 至少应该是一个 Map
        assertTrue(categoryStats instanceof Map);
    }

    @Test
    @DisplayName("应该获取趋势数据（最近7天）")
    void shouldGetTrendData() {
        // Given: Mock Repository
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(goodsRepository.findAll()).thenReturn(Collections.emptyList());
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());
        
        // When: 获取趋势数据
        Map<String, Object> trendData = statisticsService.getTrendData(7);

        // Then: 返回趋势数据
        assertNotNull(trendData);
        assertTrue(trendData.containsKey("dates"));
        assertTrue(trendData.containsKey("userCounts"));
        assertTrue(trendData.containsKey("goodsCounts"));
        assertTrue(trendData.containsKey("orderCounts"));
    }

    @Test
    @DisplayName("应该获取热门物品排行榜")
    void shouldGetTopGoods() {
        // Given: Mock Repository
        when(goodsRepository.findAll()).thenReturn(Collections.emptyList());
        
        // When: 获取热门物品排行榜（前10）
        var topGoods = statisticsService.getTopGoods(10);

        // Then: 返回热门物品列表
        assertNotNull(topGoods);
        assertTrue(topGoods instanceof java.util.List);
        // 列表可能为空（如果数据库没有数据）
    }

    @Test
    @DisplayName("应该获取活跃用户排行榜")
    void shouldGetTopUsers() {
        // Given: Mock Repository
        when(goodsRepository.findAll()).thenReturn(Collections.emptyList());
        
        // When: 获取活跃用户排行榜（前10）
        var topUsers = statisticsService.getTopUsers(10);

        // Then: 返回活跃用户列表
        assertNotNull(topUsers);
        assertTrue(topUsers instanceof java.util.List);
    }

    @Test
    @DisplayName("应该获取收入统计（按月）")
    void shouldGetRevenueByMonth() {
        // Given: Mock Repository
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());
        
        // When: 获取最近12个月的收入统计
        Map<String, Object> revenueData = statisticsService.getRevenueByMonth(12);

        // Then: 返回收入统计数据
        assertNotNull(revenueData);
        assertTrue(revenueData.containsKey("months"));
        assertTrue(revenueData.containsKey("revenues"));
    }
}
