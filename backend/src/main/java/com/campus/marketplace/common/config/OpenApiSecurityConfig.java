package com.campus.marketplace.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 安全配置
 *
 * 定义全局 Bearer Token 安全方案，便于前端在 Swagger UI 中一键携带 JWT 调试。
 */
@Configuration
@OpenAPIDefinition(security = {@SecurityRequirement(name = OpenApiSecurityConfig.BEARER_SCHEME_NAME)})
@SecurityScheme(
        name = OpenApiSecurityConfig.BEARER_SCHEME_NAME,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiSecurityConfig {

    /**
     * Swagger UI Bearer Token 安全方案名称
     */
    public static final String BEARER_SCHEME_NAME = "BearerAuth";
}
