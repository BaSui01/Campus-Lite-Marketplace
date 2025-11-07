package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.TagResponse;
import com.campus.marketplace.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签查询控制器（前台公开接口）
 *
 * 提供标签的查询接口（管理接口请使用 TagAdminController）
 *
 * @author BaSui
 * @date 2025-10-29
 */

@RestController
@RequiredArgsConstructor
@Tag(name = "标签查询", description = "标签查询接口（公开）")
public class TagController {

    private final TagService tagService;

    @GetMapping("/tags")
    @Operation(summary = "获取标签列表", description = "返回系统中全部标签")
    public ApiResponse<List<TagResponse>> listTags() {
        return ApiResponse.success(tagService.listAllTags());
    }
}
