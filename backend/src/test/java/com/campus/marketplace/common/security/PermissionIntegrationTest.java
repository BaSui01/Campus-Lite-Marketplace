package com.campus.marketplace.common.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 权限验证集成测试 - 测试权限体系在实际场景中的有效性
 * 
 * 这个测试验证权限编码、角色定义和权限使用方式的一致性
 * 
 * @author BaSui 😎
 * @date 2025-11-02
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("权限验证集成测试")
class PermissionIntegrationTest {

    @Test
    @DisplayName("超级管理员应该拥有所有权限")
    void superAdminShouldHaveAllPermissions() {
        // Arrange
        Set<String> superAdminPermissions = RoleDefinition.SUPER_ADMIN.getPermissions();
        Set<String> allSystemPermissions = PermissionCodes.allCodes();
        
        // Act & Assert
        assertThat(superAdminPermissions)
            .describedAs("超级管理员应该拥有所有系统权限")
            .containsAll(allSystemPermissions);
            
        assertThat(superAdminPermissions.size())
            .describedAs("超级管理员权限数量应该等于系统总权限数量")
            .isEqualTo(allSystemPermissions.size());
    }

    @Test
    @DisplayName("系统管理员权限应该是超级管理员权限的子集")
    void adminPermissionsShouldBeSubsetOfSuperAdmin() {
        // Arrange
        Set<String> adminPermissions = RoleDefinition.ADMIN.getPermissions();
        Set<String> superAdminPermissions = RoleDefinition.SUPER_ADMIN.getPermissions();
        
        // Act & Assert
        assertThat(superAdminPermissions)
            .describedAs("超级管理员权限应该包含所有管理员权限")
            .containsAll(adminPermissions);
    }

    @Test
    @DisplayName("申诉专员权限应该遵循最小权限原则")
    void appealManagerShouldFollowLeastPrivilegePrinciple() {
        // Arrange
        Set<String> appealManagerPermissions = RoleDefinition.APPEAL_MANAGER.getPermissions();
        
        // Act & Assert
        // 申诉专员应该有必要的权限
        assertThat(appealManagerPermissions)
            .describedAs("申诉专员应该有申诉处理权限")
            .contains(PermissionCodes.SYSTEM_USER_APPEAL_HANDLE);
            
        assertThat(appealManagerPermissions)
            .describedAs("申诉专员应该有纠纷仲裁权限")
            .contains(PermissionCodes.SYSTEM_DISPUTE_HANDLE);
            
        // 申诉专员不应该有不相关的危险权限
        assertThat(appealManagerPermissions)
            .describedAs("申诉专员不应该有用户封禁权限")
            .doesNotContain(PermissionCodes.SYSTEM_USER_BAN);
            
        assertThat(appealManagerPermissions)
            .describedAs("申诉专员不应该有批量删除权限")
            .doesNotContain(PermissionCodes.SYSTEM_BATCH_GOODS_DELETE);
    }

    @Test
    @DisplayName("批量管理员权限应该集中在批量操作")
    void batchManagerShouldHaveFocusedBatchPermissions() {
        // Arrange
        Set<String> batchManagerPermissions = RoleDefinition.BATCH_MANAGER.getPermissions();
        
        // Act & Assert
        // 批量管理员应该有批量操作的权限
        assertThat(batchManagerPermissions)
            .describedAs("批量管理员应该有批量下架权限")
            .contains(PermissionCodes.SYSTEM_BATCH_GOODS_OFFLINE);
            
        assertThat(batchManagerPermissions)
            .describedAs("批量管理员应该有批量更新权限")
            .contains(PermissionCodes.SYSTEM_BATCH_GOODS_UPDATE);
            
        // 批量管理员不应该有申诉处理权限
        assertThat(batchManagerPermissions)
            .describedAs("批量管理员不应该有申诉处理权限")
            .doesNotContain(PermissionCodes.SYSTEM_USER_APPEAL_HANDLE);
    }

    @Test
    @DisplayName("数据管理员权限应该专注于数据和审计")
    void dataManagerShouldFocusOnDataAndAudit() {
        // Arrange
        Set<String> dataManagerPermissions = RoleDefinition.DATA_MANAGER.getPermissions();
        
        // Act & Assert
        assertThat(dataManagerPermissions)
            .describedAs("数据管理员应该有数据追踪权限")
            .contains(PermissionCodes.SYSTEM_DATA_TRACK);
            
        assertThat(dataManagerPermissions)
            .describedAs("数据管理员应该有审计查看权限")
            .contains(PermissionCodes.SYSTEM_AUDIT_VIEW);
            
        // 数据管理员不应该有业务操作权限
        assertThat(dataManagerPermissions)
            .describedAs("数据管理员不应该有商品审核权限")
            .doesNotContain(PermissionCodes.SYSTEM_GOODS_APPROVE);
    }

    @Test
    @DisplayName("推荐算法师权限应该专注于推荐和统计")
    void recommendationEngineerShouldFocusOnRecommendationAndStats() {
        // Arrange
        Set<String> recommendationPermissions = RoleDefinition.RECOMMENDATION_ENGINEER.getPermissions();
        
        // Act & Assert
        assertThat(recommendationPermissions)
            .describedAs("推荐算法师应该有推荐算法管理权限")
            .contains(PermissionCodes.SYSTEM_RECOMMENDATION_MANAGE);
            
        assertThat(recommendationPermissions)
            .describedAs("推荐算法师应该有统计查看权限")
            .contains(PermissionCodes.SYSTEM_STATISTICS_VIEW);
            
        // 推荐算法师不应该有用户管理权限
        assertThat(recommendationPermissions)
            .describedAs("推荐算法师不应该有用户封禁权限")
            .doesNotContain(PermissionCodes.SYSTEM_USER_BAN);
    }

    @Test
    @DisplayName("权限编码应该都有对应的描述")
    void allPermissionCodesShouldHaveDescriptions() {
        // Arrange
        Set<String> allPermissionCodes = PermissionCodes.allCodes();
        
        // Act & Assert - 验证所有权限编码都有描述
        for (String permissionCode : allPermissionCodes) {
            assertThat(PermissionCodes.descriptionOf(permissionCode))
                .describedAs("权限编码 %s 应该有描述", permissionCode)
                .isNotNull();
        }
    }

    @Test
    @DisplayName("管理员默认权限应该覆盖核心管理功能")
    void adminDefaultPermissionsShouldCoverCoreManagementFunctions() {
        // Arrange
        Set<String> adminDefaults = PermissionCodes.adminDefaultCodes();
        
        // Act & Assert - 验证管理员有核心权限
        assertThat(adminDefaults)
            .describedAs("管理员应该有用户查看权限")
            .contains(PermissionCodes.SYSTEM_USER_VIEW);
            
        assertThat(adminDefaults)
            .describedAs("管理员应该有配置查看权限")
            .contains(PermissionCodes.SYSTEM_CONFIG_VIEW);
            
        assertThat(adminDefaults)
            .describedAs("管理员应该有统计查看权限")
            .contains(PermissionCodes.SYSTEM_STATISTICS_VIEW);
    }

    @Test
    @DisplayName("普通用户角色应该没有任何权限")
    void normalUserRolesShouldHaveNoPermissions() {
        // Arrange
        Set<String> userPermissions = RoleDefinition.USER.getPermissions();
        Set<String> studentPermissions = RoleDefinition.STUDENT.getPermissions();
        
        // Act & Assert
        assertThat(userPermissions)
            .describedAs("普通用户不应该有任何系统权限")
            .isEmpty();
            
        assertThat(studentPermissions)
            .describedAs("学生用户不应该有任何系统权限")
            .isEmpty();
    }

    @Test
    @DisplayName("角色权限设计应该避免权限重叠和冲突")
    void rolePermissionsShouldAvoidOverlapsAndConflicts() {
        // Arrange
        Set<String> appealManagerPermissions = RoleDefinition.APPEAL_MANAGER.getPermissions();
        Set<String> batchManagerPermissions = RoleDefinition.BATCH_MANAGER.getPermissions();
        Set<String> dataManagerPermissions = RoleDefinition.DATA_MANAGER.getPermissions();
        
        // Act & Assert - 验证角色权限边界清晰
        assertThat(appealManagerPermissions)
            .describedAs("申诉专员和批量管理员权限应该不重叠")
            .doesNotContainAnyElementsOf(batchManagerPermissions);
            
        assertThat(batchManagerPermissions)
            .describedAs("批量管理员和数据管理员权限应该不重叠")
            .doesNotContainAnyElementsOf(dataManagerPermissions);
    }

    @Test
    @DisplayName("权限编码命名应该遵循规范格式")
    void permissionCodeNamingShouldFollowStandardFormat() {
        // Arrange
        Set<String> allPermissionCodes = PermissionCodes.allCodes();
        
        // Act & Assert - 验证权限编码格式规范
        for (String permissionCode : allPermissionCodes) {
            assertThat(permissionCode)
                .describedAs("权限编码 %s 应该使用冒号分隔的格式", permissionCode)
                .contains(":");
                
            String[] parts = permissionCode.split(":");
            assertThat(parts.length)
                .describedAs("权限编码 %s 应该由2-4个部分组成", permissionCode)
                .isBetween(2, 4);
                
            assertThat(parts[0])
                .describedAs("权限编码第一部分应该是系统/模块名称")
                .isIn("system", "market");
        }
    }

    @Test
    @DisplayName("新功能权限应该被正确分配给相关角色")
    void newFeaturePermissionsShouldBeAssignedCorrectly() {
        // Arrange
        Set<String> superAdminPermissions = RoleDefinition.SUPER_ADMIN.getPermissions();
        Set<String> adminPermissions = RoleDefinition.ADMIN.getPermissions();
        
        // Act & Assert - 验证新增权限的分配情况
        // 申诉相关权限应该分配给管理员
        assertThat(adminPermissions)
            .describedAs("管理员应该有申诉处理权限")
            .contains(PermissionCodes.SYSTEM_USER_APPEAL_HANDLE);
            
        // 批量操作权限应该分配给管理员
        assertThat(adminPermissions)
            .describedAs("管理员应该有批量操作权限")
            .contains(PermissionCodes.SYSTEM_BATCH_GOODS_OFFLINE);
            
        // 新的权限都应该被超级管理员拥有
        assertThat(superAdminPermissions)
            .describedAs("超级管理员应该拥有所有新权限")
            .contains(PermissionCodes.SYSTEM_USER_APPEAL)
            .contains(PermissionCodes.SYSTEM_BATCH_USERS_NOTIFY)
            .contains(PermissionCodes.SYSTEM_DATA_TRACK)
            .contains(PermissionCodes.MARKET_SELLER_CENTER);
    }
}
