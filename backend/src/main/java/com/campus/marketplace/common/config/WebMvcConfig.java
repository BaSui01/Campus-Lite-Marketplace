package com.campus.marketplace.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置
 *
 * 专门配置静态资源映射 📁
 *
 * ⚠️ CORS 跨域配置已统一到 SecurityConfig,避免冲突!
 *
 * @author BaSui
 * @date 2025-11-01
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源映射 📁
     *
     * 映射规则：
     * - /static/goods/** → classpath:/static/goods/
     * - /static/uploads/** → classpath:/static/uploads/
     *
     * 使用场景：
     * - 商品图片展示
     * - 用户上传文件访问
     * - 开发/测试环境占位图
     *
     * ⚠️ 重要：静态资源路径必须加 /static/ 前缀,避免和 API 路由冲突!
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 商品图片静态资源映射 📸
        // ⚠️ 使用 /static/goods/** 而不是 /goods/**,避免和 API 路由 /api/goods 冲突!
        registry.addResourceHandler("/static/goods/**")
                .addResourceLocations("classpath:/static/goods/")
                .setCachePeriod(3600); // 缓存 1 小时 ⏰

        // 用户上传文件静态资源映射 📤
        // ⚠️ 使用 /static/uploads/** 而不是 /uploads/**,避免路由冲突!
        registry.addResourceHandler("/static/uploads/**")
                .addResourceLocations("classpath:/static/uploads/", "file:./uploads/")
                .setCachePeriod(86400); // 缓存 24 小时 ⏰
    }
}
