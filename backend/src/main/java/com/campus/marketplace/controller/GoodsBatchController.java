package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateBatchTaskRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.campus.marketplace.common.dto.request.GoodsBatchRequest;
import com.campus.marketplace.common.dto.request.InventoryBatchRequest;
import com.campus.marketplace.common.dto.request.PriceBatchRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.enums.BatchType;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.BatchLimitService;
import com.campus.marketplace.service.BatchOperationService;
import com.campus.marketplace.service.BatchTaskPrepareService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 商品批量操作控制器
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Tag(name = "商品批量操作")
@RestController
@RequestMapping("/api/goods/batch")
@RequiredArgsConstructor
public class GoodsBatchController {

    private final BatchOperationService batchOperationService;
    private final BatchTaskPrepareService batchTaskPrepareService;
    private final BatchLimitService batchLimitService;
    private final ObjectMapper objectMapper;

        @Operation(summary = "批量上架商品")

    @PostMapping("/online")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).BATCH_GOODS_ONLINE)")
    public ApiResponse<Long> batchOnline(@Valid @RequestBody GoodsBatchRequest request) throws Exception {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("批量上架商品: userId={}, count={}", userId, request.getTargetIds().size());

        // 验证批量数量限制
        batchLimitService.validateBatchLimit(request.getTargetIds().size());

        // 创建批量任务
        CreateBatchTaskRequest taskRequest = CreateBatchTaskRequest.builder()
                .batchType(BatchType.GOODS_BATCH)
                .requestData(objectMapper.writeValueAsString(request))
                .estimatedDuration(request.getTargetIds().size() * 2) // 预计每个2秒
                .build();

        Long taskId = batchOperationService.createBatchTask(taskRequest, userId);

        // 准备任务项
        batchTaskPrepareService.prepareTaskItems(
                taskId,
                request.getTargetIds(),
                objectMapper.writeValueAsString(request)
        );

        // 执行任务
        batchOperationService.executeBatchTask(taskId);

        return ApiResponse.success(taskId);
    }

        @Operation(summary = "批量下架商品")

    @PostMapping("/offline")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).BATCH_GOODS_OFFLINE)")
    public ApiResponse<Long> batchOffline(@Valid @RequestBody GoodsBatchRequest request) throws Exception {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("批量下架商品: userId={}, count={}", userId, request.getTargetIds().size());

        // 验证批量数量限制
        batchLimitService.validateBatchLimit(request.getTargetIds().size());

        CreateBatchTaskRequest taskRequest = CreateBatchTaskRequest.builder()
                .batchType(BatchType.GOODS_BATCH)
                .requestData(objectMapper.writeValueAsString(request))
                .estimatedDuration(request.getTargetIds().size() * 2)
                .build();

        Long taskId = batchOperationService.createBatchTask(taskRequest, userId);

        batchTaskPrepareService.prepareTaskItems(
                taskId,
                request.getTargetIds(),
                objectMapper.writeValueAsString(request)
        );

        batchOperationService.executeBatchTask(taskId);

        return ApiResponse.success(taskId);
    }

        @Operation(summary = "批量删除商品")

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).BATCH_GOODS_DELETE)")
    public ApiResponse<Long> batchDelete(@Valid @RequestBody GoodsBatchRequest request) throws Exception {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("批量删除商品: userId={}, count={}", userId, request.getTargetIds().size());

        // 验证批量数量限制
        batchLimitService.validateBatchLimit(request.getTargetIds().size());

        CreateBatchTaskRequest taskRequest = CreateBatchTaskRequest.builder()
                .batchType(BatchType.GOODS_BATCH)
                .requestData(objectMapper.writeValueAsString(request))
                .estimatedDuration(request.getTargetIds().size() * 2)
                .build();

        Long taskId = batchOperationService.createBatchTask(taskRequest, userId);

        batchTaskPrepareService.prepareTaskItems(
                taskId,
                request.getTargetIds(),
                objectMapper.writeValueAsString(request)
        );

        batchOperationService.executeBatchTask(taskId);

        return ApiResponse.success(taskId);
    }

        @Operation(summary = "批量调整价格")

    @PostMapping("/price")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).BATCH_GOODS_PRICE)")
    public ApiResponse<Long> batchUpdatePrice(@Valid @RequestBody PriceBatchRequest request) throws Exception {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("批量调整价格: userId={}, count={}", userId, request.getTargetIds().size());

        // 验证批量数量限制
        batchLimitService.validateBatchLimit(request.getTargetIds().size());

        CreateBatchTaskRequest taskRequest = CreateBatchTaskRequest.builder()
                .batchType(BatchType.PRICE_BATCH)
                .requestData(objectMapper.writeValueAsString(request))
                .estimatedDuration(request.getTargetIds().size() * 3)
                .build();

        Long taskId = batchOperationService.createBatchTask(taskRequest, userId);

        batchTaskPrepareService.prepareTaskItems(
                taskId,
                request.getTargetIds(),
                objectMapper.writeValueAsString(request)
        );

        batchOperationService.executeBatchTask(taskId);

        return ApiResponse.success(taskId);
    }

        @Operation(summary = "批量更新库存")

    @PostMapping("/inventory")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).BATCH_GOODS_INVENTORY)")
    public ApiResponse<Long> batchUpdateInventory(@Valid @RequestBody InventoryBatchRequest request) throws Exception {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("批量更新库存: userId={}, count={}", userId, request.getInventoryData().size());

        // 验证批量数量限制
        batchLimitService.validateBatchLimit(request.getInventoryData().size());

        CreateBatchTaskRequest taskRequest = CreateBatchTaskRequest.builder()
                .batchType(BatchType.INVENTORY_BATCH)
                .requestData(objectMapper.writeValueAsString(request))
                .estimatedDuration(request.getInventoryData().size() * 2)
                .build();

        Long taskId = batchOperationService.createBatchTask(taskRequest, userId);

        // 准备任务项（使用inventoryData的keys）
        batchTaskPrepareService.prepareTaskItems(
                taskId,
                request.getInventoryData().keySet().stream().toList(),
                objectMapper.writeValueAsString(request)
        );

        batchOperationService.executeBatchTask(taskId);

        return ApiResponse.success(taskId);
    }
}
