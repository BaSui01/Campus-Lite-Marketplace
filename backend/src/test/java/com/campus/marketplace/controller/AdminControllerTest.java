package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.BanUserRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.StatisticsService;
import com.campus.marketplace.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AdminController 单元测试
 * 
 * 功能覆盖：
 * 1. 用户管理（封禁/解封）
 * 2. 数据统计（系统概览、趋势分析等）
 * 3. 系统管理（自动解封）
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("管理员控制器测试 💼")
class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        // 每个测试前的准备工作（目前无需特殊准备）
    }

    // ========== 用户管理模块测试 ==========

    @Test
    @DisplayName("封禁用户 - 成功")
    void banUser_Success() {
        // Arrange（准备数据）
        Long userId = 100L;
        BanUserRequest request = new BanUserRequest(userId, "发布违规内容", 7);

        doNothing().when(userService).banUser(request);

        // Act（执行操作）
        ApiResponse<Void> response = adminController.banUser(request);

        // Assert（验证结果）
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNull(response.getData());

        // 验证 Service 方法被调用
        verify(userService, times(1)).banUser(request);
    }

    @Test
    @DisplayName("解封用户 - 成功")
    void unbanUser_Success() {
        // Arrange
        Long userId = 100L;
        doNothing().when(userService).unbanUser(userId);

        // Act
        ApiResponse<Void> response = adminController.unbanUser(userId);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNull(response.getData());

        // 验证 Service 方法被调用
        verify(userService, times(1)).unbanUser(userId);
    }

    @Test
    @DisplayName("自动解封过期用户 - 成功解封 3 个用户")
    void autoUnbanExpiredUsers_Success() {
        // Arrange
        when(userService.autoUnbanExpiredUsers()).thenReturn(3);

        // Act
        ApiResponse<Integer> response = adminController.autoUnbanExpiredUsers();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(3, response.getData());

        // 验证 Service 方法被调用
        verify(userService, times(1)).autoUnbanExpiredUsers();
    }

    @Test
    @DisplayName("自动解封过期用户 - 没有需要解封的用户")
    void autoUnbanExpiredUsers_NoUsers() {
        // Arrange
        when(userService.autoUnbanExpiredUsers()).thenReturn(0);

        // Act
        ApiResponse<Integer> response = adminController.autoUnbanExpiredUsers();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(0, response.getData());

        // 验证 Service 方法被调用
        verify(userService, times(1)).autoUnbanExpiredUsers();
    }

    // ========== 数据统计模块测试 ==========

    @Test
    @DisplayName("获取系统概览统计 - 成功")
    void getSystemOverview_Success() {
        // Arrange
        Map<String, Object> mockData = Map.of(
                "totalUsers", 1000,
                "totalGoods", 500,
                "totalOrders", 200,
                "activeUsers", 300
        );
        when(statisticsService.getSystemOverview()).thenReturn(mockData);

        // Act
        ApiResponse<Map<String, Object>> response = adminController.getSystemOverview();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(mockData, response.getData());
        assertEquals(1000, response.getData().get("totalUsers"));
        assertEquals(500, response.getData().get("totalGoods"));

        // 验证 Service 方法被调用
        verify(statisticsService, times(1)).getSystemOverview();
    }

    @Test
    @DisplayName("获取用户统计数据 - 成功")
    void getUserStatistics_Success() {
        // Arrange
        Map<String, Object> mockData = Map.of(
                "newUsersToday", 10,
                "activeUsersToday", 50,
                "bannedUsers", 5
        );
        when(statisticsService.getUserStatistics()).thenReturn(mockData);

        // Act
        ApiResponse<Map<String, Object>> response = adminController.getUserStatistics();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(mockData, response.getData());

        // 验证 Service 方法被调用
        verify(statisticsService, times(1)).getUserStatistics();
    }

    @Test
    @DisplayName("获取物品统计数据 - 成功")
    void getGoodsStatistics_Success() {
        // Arrange
        Map<String, Object> mockData = Map.of(
                "totalGoods", 500,
                "approvedGoods", 450,
                "pendingGoods", 30
        );
        when(statisticsService.getGoodsStatistics()).thenReturn(mockData);

        // Act
        ApiResponse<Map<String, Object>> response = adminController.getGoodsStatistics();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(mockData, response.getData());

        // 验证 Service 方法被调用
        verify(statisticsService, times(1)).getGoodsStatistics();
    }

    @Test
    @DisplayName("获取订单统计数据 - 成功")
    void getOrderStatistics_Success() {
        // Arrange
        Map<String, Object> mockData = Map.of(
                "totalOrders", 200,
                "completedOrders", 150,
                "cancelledOrders", 20
        );
        when(statisticsService.getOrderStatistics()).thenReturn(mockData);

        // Act
        ApiResponse<Map<String, Object>> response = adminController.getOrderStatistics();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(mockData, response.getData());

        // 验证 Service 方法被调用
        verify(statisticsService, times(1)).getOrderStatistics();
    }

    @Test
    @DisplayName("获取今日统计数据 - 成功")
    void getTodayStatistics_Success() {
        // Arrange
        Map<String, Object> mockData = Map.of(
                "newUsersToday", 10,
                "newGoodsToday", 20,
                "newOrdersToday", 15
        );
        when(statisticsService.getTodayStatistics()).thenReturn(mockData);

        // Act
        ApiResponse<Map<String, Object>> response = adminController.getTodayStatistics();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(mockData, response.getData());

        // 验证 Service 方法被调用
        verify(statisticsService, times(1)).getTodayStatistics();
    }

    @Test
    @DisplayName("获取分类统计数据 - 成功")
    void getCategoryStatistics_Success() {
        // Arrange
        Map<String, Long> mockData = Map.of(
                "数码产品", 100L,
                "图书文具", 80L,
                "生活用品", 50L
        );
        when(statisticsService.getCategoryStatistics()).thenReturn(mockData);

        // Act
        ApiResponse<Map<String, Long>> response = adminController.getCategoryStatistics();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(mockData, response.getData());

        // 验证 Service 方法被调用
        verify(statisticsService, times(1)).getCategoryStatistics();
    }

    @Test
    @DisplayName("获取趋势数据 - 最近 7 天")
    void getTrendData_Success_SevenDays() {
        // Arrange
        int days = 7;
        Map<String, Object> mockData = Map.of(
                "dates", List.of("2025-10-21", "2025-10-22", "2025-10-23"),
                "newUsers", List.of(10, 15, 12),
                "newOrders", List.of(20, 25, 18)
        );
        when(statisticsService.getTrendData(days)).thenReturn(mockData);

        // Act
        ApiResponse<Map<String, Object>> response = adminController.getTrendData(days);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(mockData, response.getData());

        // 验证 Service 方法被调用
        verify(statisticsService, times(1)).getTrendData(days);
    }

    @Test
    @DisplayName("获取趋势数据 - 默认 30 天")
    void getTrendData_Success_DefaultThirtyDays() {
        // Arrange
        int defaultDays = 30;
        Map<String, Object> mockData = Map.of(
                "dates", List.of("2025-10-01", "2025-10-02"),
                "newUsers", List.of(50, 60),
                "newOrders", List.of(100, 120)
        );
        when(statisticsService.getTrendData(defaultDays)).thenReturn(mockData);

        // Act
        ApiResponse<Map<String, Object>> response = adminController.getTrendData(defaultDays);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(mockData, response.getData());

        // 验证 Service 方法被调用
        verify(statisticsService, times(1)).getTrendData(defaultDays);
    }

    @Test
    @DisplayName("获取热门物品排行榜 - Top 10")
    void getTopGoods_Success() {
        // Arrange
        int limit = 10;
        List<Map<String, Object>> mockData = List.of(
                Map.of("goodsId", 1L, "goodsName", "iPhone 13", "viewCount", 500),
                Map.of("goodsId", 2L, "goodsName", "MacBook Pro", "viewCount", 300)
        );
        when(statisticsService.getTopGoods(limit)).thenReturn(mockData);

        // Act
        ApiResponse<List<Map<String, Object>>> response = adminController.getTopGoods(limit);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(mockData, response.getData());
        assertEquals(2, response.getData().size());

        // 验证 Service 方法被调用
        verify(statisticsService, times(1)).getTopGoods(limit);
    }

    @Test
    @DisplayName("获取活跃用户排行榜 - Top 20")
    void getTopUsers_Success() {
        // Arrange
        int limit = 20;
        List<Map<String, Object>> mockData = List.of(
                Map.of("userId", 1L, "username", "user1", "points", 500),
                Map.of("userId", 2L, "username", "user2", "points", 300)
        );
        when(statisticsService.getTopUsers(limit)).thenReturn(mockData);

        // Act
        ApiResponse<List<Map<String, Object>>> response = adminController.getTopUsers(limit);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(mockData, response.getData());
        assertEquals(2, response.getData().size());

        // 验证 Service 方法被调用
        verify(statisticsService, times(1)).getTopUsers(limit);
    }

    @Test
    @DisplayName("获取收入统计（按月）- 最近 6 个月")
    void getRevenueByMonth_Success() {
        // Arrange
        int months = 6;
        Map<String, Object> mockData = Map.of(
                "months", List.of("2025-05", "2025-06", "2025-07"),
                "revenue", List.of(10000, 12000, 15000)
        );
        when(statisticsService.getRevenueByMonth(months)).thenReturn(mockData);

        // Act
        ApiResponse<Map<String, Object>> response = adminController.getRevenueByMonth(months);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(mockData, response.getData());

        // 验证 Service 方法被调用
        verify(statisticsService, times(1)).getRevenueByMonth(months);
    }
}
