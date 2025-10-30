package com.campus.marketplace.common.component;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RateLimitRuleManager 测试")
class RateLimitRuleManagerTest {

    private final RateLimitRuleManager manager = new RateLimitRuleManager();

    @Test
    @DisplayName("管理操作会正确维护黑白名单与开关")
    void shouldManageRulesCorrectly() {
        manager.setEnabled(false);
        manager.addUserWhitelist(100L);
        manager.addIpWhitelist("127.0.0.1");
        manager.addIpBlacklist("192.168.1.10");

        assertThat(manager.isEnabled()).isFalse();
        assertThat(manager.isWhitelisted(100L, null)).isTrue();
        assertThat(manager.isWhitelisted(null, "127.0.0.1")).isTrue();
        assertThat(manager.isBlacklisted("192.168.1.10")).isTrue();

        manager.removeUserWhitelist(100L);
        manager.removeIpWhitelist("127.0.0.1");
        manager.removeIpBlacklist("192.168.1.10");

        assertThat(manager.isWhitelisted(100L, "127.0.0.1")).isFalse();
        assertThat(manager.isBlacklisted("192.168.1.10")).isFalse();
    }
}
