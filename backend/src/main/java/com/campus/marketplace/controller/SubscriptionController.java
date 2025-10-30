package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateSubscriptionRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.SubscriptionResponse;
import com.campus.marketplace.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 关键词订阅控制器
 *
 * 提供关键词订阅/取消与订阅列表接口
 *
 * @author BaSui
 * @date 2025-10-27
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "关键词订阅", description = "关键词订阅与取消接口")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "新增订阅", description = "订阅关键词，符合条件的商品上架会提醒")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateSubscriptionRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"keyword\": \"耳机\",
                                      \"campusId\": 1
                                    }
                                    """
                    )
            )
    )
    public ApiResponse<Long> subscribe(@Valid @RequestBody CreateSubscriptionRequest request) {
        return ApiResponse.success(subscriptionService.subscribe(request));
    }

    @DeleteMapping("/subscribe/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "取消订阅", description = "取消关键词订阅")
    public ApiResponse<Void> unsubscribe(@Parameter(description = "订阅ID", example = "1001") @PathVariable Long id) {
        subscriptionService.unsubscribe(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/subscribe")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "订阅列表", description = "查看我订阅的关键词")
    public ApiResponse<List<SubscriptionResponse>> listSubscriptions() {
        return ApiResponse.success(subscriptionService.listMySubscriptions());
    }
}
