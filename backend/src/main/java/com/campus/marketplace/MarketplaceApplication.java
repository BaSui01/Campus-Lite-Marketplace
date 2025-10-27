package com.campus.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 校园轻享集市系统 - 主启动类
 * 
 * 基于 Spring Boot 3.x + Java 21 的企业级单体应用
 * 提供二手物品交易、论坛交流、即时通讯等功能
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class MarketplaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApplication.class, args);
    }
}
