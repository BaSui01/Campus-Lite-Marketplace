package com.campus.marketplace.enums;

import com.campus.marketplace.common.enums.PresenceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PresenceStatus 枚举测试
 *
 * 测试用户在线状态枚举的完整性和正确性
 *
 * @author BaSui
 * @date 2025-11-03
 */
@DisplayName("用户在线状态枚举测试")
class PresenceStatusTest {

    @Test
    @DisplayName("应该包含所有必需的在线状态")
    void shouldContainAllRequiredStatuses() {
        // 验证枚举值完整性
        assertThat(PresenceStatus.values()).hasSize(4);
        assertThat(PresenceStatus.ONLINE).isNotNull();
        assertThat(PresenceStatus.BUSY).isNotNull();
        assertThat(PresenceStatus.AWAY).isNotNull();
        assertThat(PresenceStatus.OFFLINE).isNotNull();
    }

    @Test
    @DisplayName("每个状态应该有正确的显示名称")
    void eachStatusShouldHaveCorrectDisplayName() {
        assertThat(PresenceStatus.ONLINE.getDisplayName()).isEqualTo("在线");
        assertThat(PresenceStatus.BUSY.getDisplayName()).isEqualTo("忙碌");
        assertThat(PresenceStatus.AWAY.getDisplayName()).isEqualTo("离开");
        assertThat(PresenceStatus.OFFLINE.getDisplayName()).isEqualTo("离线");
    }

    @Test
    @DisplayName("应该能够通过名称获取枚举值")
    void shouldBeAbleToGetEnumByName() {
        assertThat(PresenceStatus.valueOf("ONLINE")).isEqualTo(PresenceStatus.ONLINE);
        assertThat(PresenceStatus.valueOf("BUSY")).isEqualTo(PresenceStatus.BUSY);
        assertThat(PresenceStatus.valueOf("AWAY")).isEqualTo(PresenceStatus.AWAY);
        assertThat(PresenceStatus.valueOf("OFFLINE")).isEqualTo(PresenceStatus.OFFLINE);
    }

    @Test
    @DisplayName("ONLINE 状态应该表示用户在线")
    void onlineStatusShouldRepresentUserIsOnline() {
        PresenceStatus status = PresenceStatus.ONLINE;
        assertThat(status.name()).isEqualTo("ONLINE");
        assertThat(status.getDisplayName()).isEqualTo("在线");
    }

    @Test
    @DisplayName("OFFLINE 状态应该表示用户离线")
    void offlineStatusShouldRepresentUserIsOffline() {
        PresenceStatus status = PresenceStatus.OFFLINE;
        assertThat(status.name()).isEqualTo("OFFLINE");
        assertThat(status.getDisplayName()).isEqualTo("离线");
    }
}
