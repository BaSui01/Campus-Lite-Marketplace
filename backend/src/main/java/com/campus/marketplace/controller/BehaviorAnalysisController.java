package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.UserBehaviorLogDTO;
import com.campus.marketplace.common.dto.UserPersonaDTO;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.enums.BehaviorType;
import com.campus.marketplace.service.BehaviorAnalysisService;
import com.campus.marketplace.service.UserPersonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 行为分析Controller
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@RestController
@RequestMapping("/behavior-analysis")
@RequiredArgsConstructor
@Tag(name = "行为分析", description = "用户行为追踪和画像分析")
public class BehaviorAnalysisController {

    private final BehaviorAnalysisService behaviorAnalysisService;
    private final UserPersonaService personaService;

    @PostMapping("/record")
    @Operation(summary = "记录用户行为")
    public ApiResponse<UserBehaviorLogDTO> recordBehavior(
            @RequestParam Long userId,
            @RequestParam BehaviorType behaviorType,
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer duration,
            @RequestBody(required = false) Map<String, Object> extraData
    ) {
        UserBehaviorLogDTO result = behaviorAnalysisService.recordBehavior(
                userId, behaviorType, targetType, targetId, source, duration, extraData
        );
        return ApiResponse.success(result);
    }

    @GetMapping("/behaviors/{userId}")
    @Operation(summary = "获取用户行为列表")
    public ApiResponse<List<UserBehaviorLogDTO>> getUserBehaviors(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "50") Integer limit
    ) {
        List<UserBehaviorLogDTO> behaviors = behaviorAnalysisService.getUserBehaviors(userId, limit);
        return ApiResponse.success(behaviors);
    }

    @GetMapping("/persona/{userId}")
    @Operation(summary = "获取用户画像")
    public ApiResponse<UserPersonaDTO> getUserPersona(@PathVariable Long userId) {
        UserPersonaDTO persona = behaviorAnalysisService.getUserPersona(userId);
        return ApiResponse.success(persona);
    }

    @PostMapping("/persona/{userId}/build")
    @Operation(summary = "构建/更新用户画像")
    @PreAuthorize("hasAuthority('system:statistics:view')")
    public ApiResponse<UserPersonaDTO> buildUserPersona(@PathVariable Long userId) {
        UserPersonaDTO persona = behaviorAnalysisService.buildUserPersona(userId);
        return ApiResponse.success(persona);
    }

    @GetMapping("/persona/statistics")
    @Operation(summary = "用户分群统计")
    @PreAuthorize("hasAuthority('system:statistics:view')")
    public ApiResponse<Map<String, Long>> getUserSegmentStatistics() {
        Map<String, Long> statistics = personaService.getUserSegmentStatistics();
        return ApiResponse.success(statistics);
    }

    @GetMapping("/heatmap")
    @Operation(summary = "行为热力图")
    @PreAuthorize("hasAuthority('system:statistics:view')")
    public ApiResponse<Map<String, Object>> getBehaviorHeatmap() {
        Map<String, Object> heatmap = behaviorAnalysisService.getBehaviorHeatmap();
        return ApiResponse.success(heatmap);
    }
}
