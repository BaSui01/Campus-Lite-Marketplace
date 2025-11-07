package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 🔥 BaSui 的热重载测试控制器 - 测试 DevTools 是否生效！😎
 *
 * @author BaSui
 * @date 2025-11-07
 */
@RestController
@RequestMapping("/test")
@Tag(name = "热重载测试", description = "测试 Spring Boot DevTools 热重载功能")
public class TestHotReloadController {

    @GetMapping("/hello")
    @Operation(summary = "Hello World", description = "测试热重载 - 版本 1")
    public ApiResponse<String> hello() {
        return ApiResponse.success("Hello World! 🚀 版本 1 - 初始版本");
    }
}
