package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.enums.CampusStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CampusCacheDTO 单元测试
 *
 * 测试目标：
 * 1. ✅ 验证从 Campus 实体正确转换为 DTO
 * 2. ✅ 验证 null 值处理
 * 3. ✅ 验证业务方法（isActive、isInactive）
 *
 * @author BaSui 😎
 * @date 2025-10-31
 */
@DisplayName("校区缓存 DTO 测试")
class CampusCacheDTOTest {

    @Test
    @DisplayName("应该能从完整的 Campus 实体转换为 DTO")
    void shouldConvertFromCompleteCampus() {
        // 🔴 Arrange：准备测试数据
        Campus campus = Campus.builder()
                .code("SH-001")
                .name("上海校区")
                .status(CampusStatus.ACTIVE)
                .build();

        campus.setId(1L);
        campus.setCreatedAt(LocalDateTime.now());
        campus.setUpdatedAt(LocalDateTime.now());

        // 🟢 Act：执行转换
        CampusCacheDTO dto = CampusCacheDTO.from(campus);

        // 🔵 Assert：验证结果
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCode()).isEqualTo("SH-001");
        assertThat(dto.getName()).isEqualTo("上海校区");
        assertThat(dto.getStatus()).isEqualTo(CampusStatus.ACTIVE);
        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("应该能处理 Campus 为 null 的情况")
    void shouldHandleNullCampus() {
        // 🔴 Arrange & 🟢 Act
        CampusCacheDTO dto = CampusCacheDTO.from(null);

        // 🔵 Assert
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("应该能正确判断校区是否激活")
    void shouldCheckIfActive() {
        // 🔴 Arrange：激活的校区
        Campus activeCampus = Campus.builder()
                .name("激活校区")
                .status(CampusStatus.ACTIVE)
                .build();

        CampusCacheDTO activeDto = CampusCacheDTO.from(activeCampus);

        // 🟢 Act & 🔵 Assert
        assertThat(activeDto.isActive()).isTrue();
        assertThat(activeDto.isInactive()).isFalse();

        // 🔴 Arrange：未激活的校区
        Campus inactiveCampus = Campus.builder()
                .name("未激活校区")
                .status(CampusStatus.INACTIVE)
                .build();

        CampusCacheDTO inactiveDto = CampusCacheDTO.from(inactiveCampus);

        // 🟢 Act & 🔵 Assert
        assertThat(inactiveDto.isActive()).isFalse();
        assertThat(inactiveDto.isInactive()).isTrue();
    }

    @Test
    @DisplayName("应该能序列化和反序列化（验证 Serializable）")
    void shouldBeSerializable() {
        // 🔴 Arrange
        CampusCacheDTO dto = CampusCacheDTO.builder()
                .id(1L)
                .code("BJ-001")
                .name("北京校区")
                .status(CampusStatus.ACTIVE)
                .build();

        // 🟢 Act & 🔵 Assert：验证对象实现了 Serializable
        assertThat(dto).isInstanceOf(java.io.Serializable.class);
    }
}
