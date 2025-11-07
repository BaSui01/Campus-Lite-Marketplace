package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.BatchUnblockRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.BlacklistStatsResponse;
import com.campus.marketplace.common.entity.Blacklist;
import com.campus.marketplace.repository.BlacklistRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 黑名单管理员控制器
 *
 * 提供管理员级别的黑名单批量查询、统计、批量解除等功能
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/admin/blacklist")
@RequiredArgsConstructor
@Tag(name = "黑名单管理（管理员）", description = "管理员批量查询、统计、解除黑名单")
public class BlacklistAdminController {

    private final BlacklistRepository blacklistRepository;

    /**
     * 管理员查询所有黑名单记录（分页）
     *
     * @param userId 筛选用户ID（可选）
     * @param blockedUserId 筛选被拉黑用户ID（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 黑名单列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)")
    @Operation(summary = "查询所有黑名单记录（分页）", description = "管理员查询系统中的所有黑名单记录")
    public ApiResponse<Page<Blacklist>> listAllBlacklist(
            @Parameter(description = "筛选用户ID（拉黑者）", example = "1001")
            @RequestParam(required = false) Long userId,

            @Parameter(description = "筛选被拉黑用户ID", example = "2002")
            @RequestParam(required = false) Long blockedUserId,

            @Parameter(description = "页码（从0开始）", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        log.info("管理员查询黑名单列表：userId={}, blockedUserId={}, page={}, size={}",
                userId, blockedUserId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Blacklist> blacklistPage;

        // 根据筛选条件查询
        if (userId != null && blockedUserId != null) {
            blacklistPage = blacklistRepository.findByUserIdAndBlockedUserId(userId, blockedUserId, pageable);
        } else if (userId != null) {
            blacklistPage = blacklistRepository.findByUserId(userId, pageable);
        } else if (blockedUserId != null) {
            blacklistPage = blacklistRepository.findByBlockedUserId(blockedUserId, pageable);
        } else {
            blacklistPage = blacklistRepository.findAll(pageable);
        }

        log.info("黑名单列表查询成功：total={}", blacklistPage.getTotalElements());
        return ApiResponse.success(blacklistPage);
    }

    /**
     * 查询指定用户拉黑了哪些人
     *
     * @param userId 用户ID
     * @return 被拉黑的用户ID列表
     */
    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_VIEW)")
    @Operation(summary = "查询指定用户的黑名单", description = "查询指定用户拉黑了哪些人")
    public ApiResponse<List<Long>> getBlacklistByUser(
            @Parameter(description = "用户ID", example = "1001")
            @PathVariable Long userId) {

        log.info("查询用户黑名单：userId={}", userId);

        List<Long> blockedUserIds = blacklistRepository.findBlockedUserIdsByUserId(userId);

        log.info("用户黑名单查询成功：userId={}, count={}", userId, blockedUserIds.size());
        return ApiResponse.success(blockedUserIds);
    }

    /**
     * 查询哪些人拉黑了指定用户
     *
     * @param blockedUserId 被拉黑的用户ID
     * @return 拉黑者的用户ID列表
     */
    @GetMapping("/blocked-by/{blockedUserId}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_VIEW)")
    @Operation(summary = "查询拉黑了指定用户的人", description = "查询哪些用户拉黑了指定用户")
    public ApiResponse<List<Long>> getBlockedByUsers(
            @Parameter(description = "被拉黑的用户ID", example = "2002")
            @PathVariable Long blockedUserId) {

        log.info("查询谁拉黑了用户：blockedUserId={}", blockedUserId);

        List<Long> userIds = blacklistRepository.findUserIdsByBlockedUserId(blockedUserId);

        log.info("拉黑者查询成功：blockedUserId={}, count={}", blockedUserId, userIds.size());
        return ApiResponse.success(userIds);
    }

    /**
     * 批量解除黑名单
     *
     * @param request 批量解除请求
     * @return 操作成功
     */
    @PostMapping("/batch-unblock")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)")
    @Operation(summary = "批量解除黑名单", description = "管理员批量解除黑名单关系")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BatchUnblockRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      "blacklistIds": [101, 102, 103]
                                    }
                                    """
                    )
            )
    )
    public ApiResponse<Integer> batchUnblock(@Valid @RequestBody BatchUnblockRequest request) {

        log.info("批量解除黑名单：ids={}", request.getBlacklistIds());

        int deletedCount = 0;
        for (Long id : request.getBlacklistIds()) {
            if (blacklistRepository.existsById(id)) {
                blacklistRepository.deleteById(id);
                deletedCount++;
            }
        }

        log.info("批量解除黑名单成功：deletedCount={}", deletedCount);
        return ApiResponse.success(deletedCount);
    }

    /**
     * 统计黑名单数量
     *
     * @return 黑名单统计数据
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_MONITOR_VIEW)")
    @Operation(summary = "统计黑名单数量", description = "统计系统中的黑名单总数、活跃拉黑者、被拉黑最多的用户等")
    public ApiResponse<BlacklistStatsResponse> getStatistics() {

        log.info("查询黑名单统计数据");

        long totalBlacklists = blacklistRepository.count();
        long activeBlockers = blacklistRepository.countDistinctUserId();
        long mostBlockedUserId = blacklistRepository.findMostBlockedUserId().orElse(0L);
        long mostBlockedCount = mostBlockedUserId > 0
            ? blacklistRepository.countByBlockedUserId(mostBlockedUserId)
            : 0;

        BlacklistStatsResponse stats = BlacklistStatsResponse.builder()
                .totalBlacklists(totalBlacklists)
                .activeBlockers(activeBlockers)
                .mostBlockedUserId(mostBlockedUserId)
                .mostBlockedCount(mostBlockedCount)
                .build();

        log.info("黑名单统计查询成功：total={}", totalBlacklists);
        return ApiResponse.success(stats);
    }

    /**
     * 检查两个用户之间的黑名单关系
     *
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @return 拉黑关系（双向检查）
     */
    @GetMapping("/check-relation")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_VIEW)")
    @Operation(summary = "检查用户黑名单关系", description = "检查两个用户之间的黑名单关系（双向）")
    public ApiResponse<CheckBlacklistRelationResponse> checkRelation(
            @Parameter(description = "用户ID", example = "1001")
            @RequestParam Long userId,

            @Parameter(description = "目标用户ID", example = "2002")
            @RequestParam Long targetUserId) {

        log.info("检查黑名单关系：userId={}, targetUserId={}", userId, targetUserId);

        boolean userBlockedTarget = blacklistRepository.existsByUserIdAndBlockedUserId(userId, targetUserId);
        boolean targetBlockedUser = blacklistRepository.existsByUserIdAndBlockedUserId(targetUserId, userId);

        CheckBlacklistRelationResponse response = CheckBlacklistRelationResponse.builder()
                .userId(userId)
                .targetUserId(targetUserId)
                .userBlockedTarget(userBlockedTarget)
                .targetBlockedUser(targetBlockedUser)
                .hasBlacklistRelation(userBlockedTarget || targetBlockedUser)
                .build();

        log.info("黑名单关系检查完成：userBlockedTarget={}, targetBlockedUser={}",
                userBlockedTarget, targetBlockedUser);
        return ApiResponse.success(response);
    }

    /**
     * 黑名单关系检查响应（内部类）
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CheckBlacklistRelationResponse {
        private Long userId;
        private Long targetUserId;
        private boolean userBlockedTarget;  // userId 是否拉黑了 targetUserId
        private boolean targetBlockedUser;  // targetUserId 是否拉黑了 userId
        private boolean hasBlacklistRelation; // 是否存在黑名单关系（双向任意一个）
    }
}
