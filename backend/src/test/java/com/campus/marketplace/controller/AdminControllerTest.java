package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.BanUserRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 🧪 BaSui 的 AdminController 单元测试 - 专注用户管理测试！😎
 *
 * 功能覆盖：
 * - 👮 用户管理：封禁/解封用户、自动解封
 *
 * ⚠️ 注意：
 * - 统计相关测试已迁移到 AdminStatisticsControllerTest（待创建）
 *
 * @author BaSui
 * @date 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("管理员用户管理控制器测试 💼")
class AdminControllerTest {

    @Mock
    private UserService userService;

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
}
