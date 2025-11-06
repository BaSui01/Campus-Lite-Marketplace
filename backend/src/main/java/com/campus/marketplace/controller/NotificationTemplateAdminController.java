package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.NotificationTemplate;
import com.campus.marketplace.common.enums.NotificationChannel;
import com.campus.marketplace.repository.NotificationTemplateRepository;
import com.campus.marketplace.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Notification Template Admin Controller
 *
 * @author BaSui
 * @date 2025-10-29
 */

@RestController
@RequestMapping("/admin/notification-templates")
@RequiredArgsConstructor
@Tag(name = "通知模板管理", description = "模板CRUD与渲染调试")
public class NotificationTemplateAdminController {

    private final NotificationTemplateRepository repository;
    private final NotificationTemplateService templateService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)")
    @Operation(summary = "列表")
    public ApiResponse<List<NotificationTemplate>> list() {
        return ApiResponse.success(repository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)")
    @Operation(summary = "创建或更新")
    public ApiResponse<NotificationTemplate> save(@RequestBody NotificationTemplate tpl) {
        return ApiResponse.success(repository.save(tpl));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)")
    @Operation(summary = "删除")
    public ApiResponse<Void> delete(@Parameter(description = "模板ID", example = "3001") @PathVariable Long id) {
        repository.deleteById(id);
        return ApiResponse.success();
    }

    @PostMapping("/render/{code}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_RATE_LIMIT_MANAGE)")
    @Operation(summary = "渲染预览")
    public ApiResponse<Map<String, Object>> render(@Parameter(description = "模板编码", example = "ORDER_PAID") @PathVariable String code,
                                                   @RequestBody(required = false) Map<String, Object> params) {
        var locale = LocaleContextHolder.getLocale();
        var rendered = templateService.render(code, locale, params == null ? Map.of() : params);
        return ApiResponse.success(Map.of(
                "title", rendered.title(),
                "content", rendered.content(),
                "channels", rendered.channels().stream().map(NotificationChannel::name).toList()
        ));
    }
}
