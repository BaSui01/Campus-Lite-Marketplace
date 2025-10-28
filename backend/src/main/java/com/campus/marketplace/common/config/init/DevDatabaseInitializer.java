package com.campus.marketplace.common.config.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 开发环境数据库自动创建初始化器
 * 仅在 active profile 包含 "dev" 时生效。
 *
 * 作用：在 JPA 初始化前尝试连接 postgres 默认库，若目标库不存在则创建之。
 */
@Slf4j
public class DevDatabaseInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment env = applicationContext.getEnvironment();
        String[] profiles = env.getActiveProfiles();
        boolean isDev = false;
        for (String p : profiles) {
            if ("dev".equalsIgnoreCase(p)) { isDev = true; break; }
        }
        if (!isDev) return;

        // 读取连接信息（与 application.yml 中的变量保持一致）
        String host = env.getProperty("DB_HOST", "localhost");
        int port = Integer.parseInt(env.getProperty("DB_PORT", "5432"));
        String dbName = env.getProperty("DB_NAME", "campus_marketplace_dev");
        String username = env.getProperty("DB_USERNAME", "postgres");
        String password = env.getProperty("DB_PASSWORD", "postgres");

        String adminUrl = String.format("jdbc:postgresql://%s:%d/postgres", host, port);

        try (Connection conn = DriverManager.getConnection(adminUrl, username, password)) {
            // 检查数据库是否存在
            String existsSql = "SELECT 1 FROM pg_database WHERE datname = ?";
            try (PreparedStatement ps = conn.prepareStatement(existsSql)) {
                ps.setString(1, dbName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        log.info("开发环境检测：数据库已存在 -> {}", dbName);
                        return;
                    }
                }
            }

            // 创建数据库
            String createSql = "CREATE DATABASE \"" + dbName + "\" ENCODING 'UTF8'";
            try (PreparedStatement ps = conn.prepareStatement(createSql)) {
                ps.executeUpdate();
                log.info("开发环境自动创建数据库成功：{} (host={}, port={})", dbName, host, port);
            }
        } catch (Exception e) {
            // 仅记录日志，不阻断启动（可能 Postgres 未启动，后续仍会因连接失败而报错）
            log.warn("开发环境数据库自检/创建失败：host={}, port={}, db={}, message={}", host, port, dbName, e.getMessage());
        }
    }
}
