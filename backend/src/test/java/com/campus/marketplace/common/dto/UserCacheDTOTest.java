package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.entity.Role;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserCacheDTO 单元测试
 *
 * 测试目标：
 * 1. ✅ 验证从 User 实体正确转换为 DTO
 * 2. ✅ 验证懒加载字段的安全访问（不会抛出异常）
 * 3. ✅ 验证敏感字段过滤（密码不缓存）
 * 4. ✅ 验证 null 值处理
 * 5. ✅ 验证业务方法（isBanned、isActive、isDeleted、isAdmin）
 *
 * @author BaSui 😎
 * @date 2025-10-31
 */
@DisplayName("用户缓存 DTO 测试")
class UserCacheDTOTest {

    @Test
    @DisplayName("应该能从完整的 User 实体转换为 DTO")
    void shouldConvertFromCompleteUser() {
        // 🔴 Arrange：准备测试数据
        Campus campus = Campus.builder()
                .name("上海校区")
                .build();
        campus.setId(5L);

        Role adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .description("管理员")
                .build();
        adminRole.setId(1L);

        Role userRole = Role.builder()
                .name("ROLE_USER")
                .description("普通用户")
                .build();
        userRole.setId(2L);

        User user = User.builder()
                .username("testuser")
                .password("encrypted_password_123")  // ⚠️ 密码字段
                .email("test@example.com")
                .phone("13800138000")
                .avatar("https://avatar.url")
                .nickname("测试用户")
                .points(100)
                .creditScore(95)
                .campusId(5L)
                .campus(campus)
                .studentId("2021001")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(adminRole, userRole))
                .build();

        user.setId(1L);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 🟢 Act：执行转换
        UserCacheDTO dto = UserCacheDTO.from(user);

        // 🔵 Assert：验证结果
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("testuser");
        assertThat(dto.getEmail()).isEqualTo("test@example.com");
        assertThat(dto.getPhone()).isEqualTo("13800138000");
        assertThat(dto.getAvatar()).isEqualTo("https://avatar.url");
        assertThat(dto.getNickname()).isEqualTo("测试用户");
        assertThat(dto.getPoints()).isEqualTo(100);
        assertThat(dto.getCreditScore()).isEqualTo(95);

        // 验证关联字段的 ID 和名称
        assertThat(dto.getCampusId()).isEqualTo(5L);
        assertThat(dto.getCampusName()).isEqualTo("上海校区");
        assertThat(dto.getStudentId()).isEqualTo("2021001");
        assertThat(dto.getStatus()).isEqualTo(UserStatus.ACTIVE);

        // 验证角色（懒加载字段）
        assertThat(dto.getRoleNames()).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");

        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getUpdatedAt()).isNotNull();
        assertThat(dto.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("应该过滤敏感字段（密码不缓存）⚠️")
    void shouldFilterSensitiveFields() {
        // 🔴 Arrange：准备包含密码的用户
        User user = User.builder()
                .username("testuser")
                .password("super_secret_password_123456")  // ⚠️ 敏感字段
                .email("test@example.com")
                .status(UserStatus.ACTIVE)
                .build();

        user.setId(1L);

        // 🟢 Act：执行转换
        UserCacheDTO dto = UserCacheDTO.from(user);

        // 🔵 Assert：验证密码字段不存在
        assertThat(dto).isNotNull();
        assertThat(dto.getUsername()).isEqualTo("testuser");
        assertThat(dto.getEmail()).isEqualTo("test@example.com");

        // ✅ 关键断言：UserCacheDTO 类中没有 password 字段
        // 通过反射验证 DTO 中确实没有 password 字段
        assertThat(dto.getClass().getDeclaredFields())
                .extracting("name")
                .doesNotContain("password");
    }

    @Test
    @DisplayName("应该能安全处理懒加载字段为 null 的情况")
    void shouldHandleNullLazyLoadedFields() {
        // 🔴 Arrange：准备只有 ID 没有关联对象的数据
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .campusId(5L)
                .campus(null)  // ⚠️ 懒加载字段未初始化
                .status(UserStatus.ACTIVE)
                .roles(null)   // ⚠️ 懒加载字段未初始化
                .build();

        user.setId(1L);

        // 🟢 Act：执行转换（不应该抛出异常）
        UserCacheDTO dto = UserCacheDTO.from(user);

        // 🔵 Assert：验证 ID 存在但名称为 null
        assertThat(dto).isNotNull();
        assertThat(dto.getCampusId()).isEqualTo(5L);
        assertThat(dto.getCampusName()).isNull();  // ✅ 安全处理
        assertThat(dto.getRoleNames()).isEmpty();  // ✅ 空集合而不是 null
    }

    @Test
    @DisplayName("应该能处理空角色集合")
    void shouldHandleEmptyRoles() {
        // 🔴 Arrange：准备没有角色的用户
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .status(UserStatus.ACTIVE)
                .roles(Set.of())  // ⚠️ 空角色集合
                .build();

        user.setId(1L);

        // 🟢 Act：执行转换
        UserCacheDTO dto = UserCacheDTO.from(user);

        // 🔵 Assert：验证角色为空集合
        assertThat(dto.getRoleNames()).isNotNull();
        assertThat(dto.getRoleNames()).isEmpty();
    }

    @Test
    @DisplayName("应该能处理 User 为 null 的情况")
    void shouldHandleNullUser() {
        // 🔴 Arrange & 🟢 Act
        UserCacheDTO dto = UserCacheDTO.from(null);

        // 🔵 Assert
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("应该能正确判断用户是否被封禁")
    void shouldCheckIfBanned() {
        // 🔴 Arrange：被封禁的用户
        User bannedUser = User.builder()
                .username("banned_user")
                .password("password")
                .status(UserStatus.BANNED)
                .build();

        UserCacheDTO bannedDto = UserCacheDTO.from(bannedUser);

        // 🟢 Act & 🔵 Assert
        assertThat(bannedDto.isBanned()).isTrue();
        assertThat(bannedDto.isActive()).isFalse();

        // 🔴 Arrange：正常用户
        User activeUser = User.builder()
                .username("active_user")
                .password("password")
                .status(UserStatus.ACTIVE)
                .build();

        UserCacheDTO activeDto = UserCacheDTO.from(activeUser);

        // 🟢 Act & 🔵 Assert
        assertThat(activeDto.isBanned()).isFalse();
        assertThat(activeDto.isActive()).isTrue();
    }

    @Test
    @DisplayName("应该能正确判断用户是否已注销")
    void shouldCheckIfDeleted() {
        // 🔴 Arrange：已注销的用户
        User deletedUser = User.builder()
                .username("deleted_user")
                .password("password")
                .status(UserStatus.DELETED)
                .build();

        deletedUser.setDeletedAt(LocalDateTime.now());

        UserCacheDTO deletedDto = UserCacheDTO.from(deletedUser);

        // 🟢 Act & 🔵 Assert
        assertThat(deletedDto.isDeleted()).isTrue();
        assertThat(deletedDto.isActive()).isFalse();
        assertThat(deletedDto.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("应该能正确判断用户是否为管理员")
    void shouldCheckIfAdmin() {
        // 🔴 Arrange：管理员用户
        Role adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .build();

        User adminUser = User.builder()
                .username("admin_user")
                .password("password")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(adminRole))
                .build();

        UserCacheDTO adminDto = UserCacheDTO.from(adminUser);

        // 🟢 Act & 🔵 Assert
        assertThat(adminDto.isAdmin()).isTrue();

        // 🔴 Arrange：普通用户
        Role userRole = Role.builder()
                .name("ROLE_USER")
                .build();

        User normalUser = User.builder()
                .username("normal_user")
                .password("password")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(userRole))
                .build();

        UserCacheDTO normalDto = UserCacheDTO.from(normalUser);

        // 🟢 Act & 🔵 Assert
        assertThat(normalDto.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("应该能处理没有角色的用户（isAdmin 返回 false）")
    void shouldHandleUserWithoutRolesForAdminCheck() {
        // 🔴 Arrange：没有角色的用户
        User user = User.builder()
                .username("no_role_user")
                .password("password")
                .status(UserStatus.ACTIVE)
                .roles(null)
                .build();

        UserCacheDTO dto = UserCacheDTO.from(user);

        // 🟢 Act & 🔵 Assert
        assertThat(dto.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("应该能序列化和反序列化（验证 Serializable）")
    void shouldBeSerializable() {
        // 🔴 Arrange
        UserCacheDTO dto = UserCacheDTO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .nickname("测试用户")
                .status(UserStatus.ACTIVE)
                .points(100)
                .creditScore(95)
                .build();

        // 🟢 Act & 🔵 Assert：验证对象实现了 Serializable
        assertThat(dto).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("应该能正确转换多个角色")
    void shouldConvertMultipleRoles() {
        // 🔴 Arrange：拥有多个角色的用户
        Role adminRole = Role.builder().name("ROLE_ADMIN").build();
        Role moderatorRole = Role.builder().name("ROLE_MODERATOR").build();
        Role userRole = Role.builder().name("ROLE_USER").build();

        User user = User.builder()
                .username("multi_role_user")
                .password("password")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(adminRole, moderatorRole, userRole))
                .build();

        // 🟢 Act：执行转换
        UserCacheDTO dto = UserCacheDTO.from(user);

        // 🔵 Assert：验证所有角色都被转换
        assertThat(dto.getRoleNames())
                .hasSize(3)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_MODERATOR", "ROLE_USER");
    }
}
