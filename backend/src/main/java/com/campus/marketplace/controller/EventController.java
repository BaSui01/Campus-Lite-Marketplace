package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.Event;
import com.campus.marketplace.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校园活动控制器
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "校园活动", description = "校园活动查询、报名相关接口")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "获取活动列表", description = "分页查询校园活动列表，支持按状态和校区筛选")
    public ApiResponse<Page<Event>> listEvents(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "活动状态(UPCOMING/ONGOING/ENDED/CANCELLED)") @RequestParam(required = false) String status,
            @Parameter(description = "校区ID") @RequestParam(required = false) Long campusId
    ) {
        Page<Event> events = eventService.listEvents(page, size, status, campusId);
        return ApiResponse.success(events);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取活动详情", description = "根据活动ID查询活动详细信息")
    public ApiResponse<Event> getEventDetail(
            @Parameter(description = "活动ID", required = true) @PathVariable Long id
    ) {
        Event event = eventService.getEventDetail(id);
        return ApiResponse.success(event);
    }

    @PostMapping("/{id}/register")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "报名活动", description = "用户报名参加活动")
    public ApiResponse<Void> registerEvent(
            @Parameter(description = "活动ID", required = true) @PathVariable Long id
    ) {
        eventService.registerEvent(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}/register")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "取消报名", description = "用户取消活动报名")
    public ApiResponse<Void> cancelRegistration(
            @Parameter(description = "活动ID", required = true) @PathVariable Long id
    ) {
        eventService.cancelRegistration(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/is-registered")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "检查是否已报名", description = "查询当前用户是否已报名该活动")
    public ApiResponse<Boolean> isRegistered(
            @Parameter(description = "活动ID", required = true) @PathVariable Long id
    ) {
        boolean registered = eventService.isRegistered(id);
        return ApiResponse.success(registered);
    }

    @GetMapping("/my-registrations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "我的报名活动", description = "查询当前用户报名的所有活动")
    public ApiResponse<List<Event>> getMyRegisteredEvents() {
        List<Event> events = eventService.getMyRegisteredEvents();
        return ApiResponse.success(events);
    }
}
