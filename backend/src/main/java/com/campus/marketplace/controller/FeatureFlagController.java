package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.FeatureFlag;
import com.campus.marketplace.repository.FeatureFlagRepository;
import com.campus.marketplace.service.FeatureFlagService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/feature-flags")
@RequiredArgsConstructor
public class FeatureFlagController {

    private final FeatureFlagRepository repository;
    private final FeatureFlagService featureFlagService;

    @GetMapping
    public ApiResponse<List<FeatureFlag>> list() {
        return ApiResponse.success(repository.findAll());
    }

    @PostMapping
    public ApiResponse<FeatureFlag> upsert(@RequestBody FeatureFlag body) {
        FeatureFlag toSave = repository.findByKey(body.getKey())
                .map(existing -> {
                    existing.setEnabled(body.isEnabled());
                    existing.setRulesJson(body.getRulesJson());
                    existing.setDescription(body.getDescription());
                    existing.setUpdatedAt(Instant.now());
                    return existing;
                }).orElseGet(() -> {
                    body.setUpdatedAt(Instant.now());
                    return body;
                });
        FeatureFlag saved = repository.save(toSave);
        featureFlagService.refresh(saved.getKey());
        return ApiResponse.success(saved);
    }

    @PostMapping("/refresh")
    public ApiResponse<Void> refreshAll() {
        featureFlagService.refreshAll();
        return ApiResponse.success();
    }

    @DeleteMapping("/{key}")
    public ApiResponse<Void> delete(@PathVariable("key") @NotBlank String key) {
        repository.findByKey(key).ifPresent(repository::delete);
        featureFlagService.refresh(key);
        return ApiResponse.success();
    }
}
