package com.campus.marketplace.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA 配置类
 * 
 * 配置 JPA 审计、事务管理和 Repository 扫描
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.campus.marketplace.repository",
        repositoryBaseClass = com.campus.marketplace.repository.base.SoftDeleteJpaRepository.class
)
public class JpaConfig {
    // JPA 配置主要在 application.yml 中完成
    // 这里只做注解启用
}
