package com.campus.marketplace.integration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flyway 迁移可用性与可重复性测试。
 * 即使当前无显式迁移脚本，也应能正常初始化并返回 0 次迁移。
 */
class FlywayMigrationIT extends IntegrationTestBase {

    @Autowired(required = false)
    private Flyway flyway;

    @Test
    @DisplayName("Flyway 能够初始化并成功执行迁移（0 或更多）")
    void flyway_migrate_ok() {
        assertThat(flyway).as("Flyway 已自动装配").isNotNull();
        MigrateResult result = flyway.migrate();
        assertThat(result.success).isTrue();
        assertThat(result.migrationsExecuted).isGreaterThanOrEqualTo(0);
        // 再次执行应为可重复（不报错）
        MigrateResult second = flyway.migrate();
        assertThat(second.success).isTrue();
    }
}
