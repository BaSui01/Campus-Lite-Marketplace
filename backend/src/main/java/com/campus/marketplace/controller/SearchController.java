package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.SearchResultItem;
import com.campus.marketplace.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Validated
@Tag(name = "搜索", description = "全文检索接口")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(summary = "全文检索", description = "支持 goods/post，两者默认 goods；关键词必填，返回高亮片段与排序")
    public ApiResponse<Page<SearchResultItem>> search(
            @Parameter(description = "搜索类型：goods/post，默认 goods")
            @RequestParam(required = false) String type,
            @Parameter(description = "搜索关键词（必填）")
            @RequestParam(name = "q") String q,
            @Parameter(description = "页码，从0开始")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "标签 ID 列表（仅 goods 生效）")
            @RequestParam(name = "tags", required = false) java.util.List<Long> tagIds
    ) {
        if (q == null || q.isBlank()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        Page<SearchResultItem> result = searchService.search(type, q, page, size, tagIds);
        return ApiResponse.success(result);
    }
}
