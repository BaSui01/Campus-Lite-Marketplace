package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.FeatureFlag;
import com.campus.marketplace.repository.FeatureFlagRepository;
import com.campus.marketplace.service.FeatureFlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Feature Flag Controller
 *
 * @author BaSui
 * @date 2025-10-29
 */

@RestController
@RequestMapping("/feature-flags")
@RequiredArgsConstructor
@Tag(name = "功能开关", description = "特性开关的新增、更新、删除与刷新")
public class FeatureFlagController {

    private final FeatureFlagRepository repository;
    private final FeatureFlagService featureFlagService;

    @GetMapping
    @Operation(summary = "开关列表", description = "查询所有功能开关")
    public ApiResponse<List<FeatureFlag>> list() {
        return ApiResponse.success(repository.findAll());
    }

    @PostMapping
    @Operation(summary = "新增或更新开关")
    public ApiResponse<FeatureFlag> upsert(@RequestBody FeatureFlag body) {
        FeatureFlag toSave = repository.findByKey(body.getKey())
                .map(existing -> {
                    existing.setEnabled(body.isEnabled());
                    existing.setRulesJson(body.getRulesJson());
                    existing.setDescription(body.getDescription());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return existing;
                }).orElseGet(() -> {
                    body.setUpdatedAt(LocalDateTime.now());
                    return body;
                });
        FeatureFlag saved = repository.save(toSave);
        featureFlagService.refresh(saved.getKey());
        return ApiResponse.success(saved);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新全部开关缓存")
    public ApiResponse<Void> refreshAll() {
        featureFlagService.refreshAll();
        return ApiResponse.success();
    }

    @DeleteMapping("/{key}")
    @Operation(summary = "删除开关")
    public ApiResponse<Void> delete(@PathVariable("key") @NotBlank String key) {
        repository.findByKey(key).ifPresent(repository::delete);
        featureFlagService.refresh(key);
        return ApiResponse.success();
    }
}
