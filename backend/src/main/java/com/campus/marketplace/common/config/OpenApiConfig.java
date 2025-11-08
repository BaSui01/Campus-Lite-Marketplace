package com.campus.marketplace.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 配置
 * 
 * 访问地址：http://localhost:8080/api/swagger-ui.html
 * API 文档：http://localhost:8080/api/v3/api-docs
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API 基本信息
                .info(new Info()
                        .title("校园轻享集市系统 API")
                        .version("1.0.0")
                        .description("""
                                ## 校园轻享集市系统 RESTful API 文档
                                
                                基于 Java 21 + Spring Boot 3.x 的企业级单体应用
                                
                                ### 主要功能
                                - 用户认证授权（JWT + RBAC）
                                - 二手物品交易
                                - 订单管理与支付
                                - 即时通讯（WebSocket）
                                - 论坛社区
                                - 积分与优惠券系统
                                
                                ### 技术栈
                                - Java 21 (Virtual Threads)
                                - Spring Boot 3.2.0
                                - PostgreSQL 16
                                - Redis 7.x
                                - JWT 认证
                                
                                ### 认证说明
                                大部分接口需要在请求头中携带 JWT Token：
                                ```
                                Authorization: Bearer <your-jwt-token>
                                ```
                                
                                ### 获取 Token
                                1. 调用 `/auth/register` 注册账号
                                2. 调用 `/auth/login` 登录获取 Token
                                3. 在后续请求中使用该 Token
                                """)
                        .contact(new Contact()
                                .name("BaSui")
                                .email("basui@campus.edu")
                                .url("https://github.com/basui"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                
                // 服务器配置（✅ BaSui: 包含 /api 前缀，与 context-path 保持一致）
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8200/api")
                                .description("本地开发环境"),
                        new Server()
                                .url("https://api.campus-marketplace.com/api")
                                .description("生产环境")
                ))
                
                // JWT 认证配置
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("请输入 JWT Token（无需添加 'Bearer ' 前缀）")))
                
                // 全局应用 JWT 认证
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
