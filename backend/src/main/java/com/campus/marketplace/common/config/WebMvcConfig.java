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
     * 映射规则（统一使用根目录 uploads/）：
     * - /uploads/** → file:./uploads/
     *
     * 使用场景：
     * - 商品图片展示：/uploads/goods/123.jpg
     * - 用户头像访问：/uploads/avatars/user_123.jpg
     * - 帖子图片访问：/uploads/posts/post_456_1.jpg
     * - 聊天文件访问：/uploads/messages/msg_789.jpg
     *
     * ⚠️ 重要：
     * - 已删除 backend/src/main/resources/static/ 目录
     * - 统一使用根目录 uploads/ 存储所有静态文件
     * - 种子数据图片已移动到 uploads/goods/
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 统一静态资源映射 📁
        // 映射根目录的 uploads/ 文件夹到 /uploads/** URL
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(86400); // 缓存 24 小时 ⏰
    }
}
