package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.Topic;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.entity.UserFeed;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 社区控制器
 * 
 * 提供社区广场的API接口：话题管理、动态流、互动功能
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Tag(name = "社区广场", description = "社区广场相关接口")
public class CommunityController {

    private final CommunityService communityService;
    private final UserRepository userRepository;

    /**
     * 获取热门话题（前10个）
     */
    @GetMapping("/topics/hot")
    @Operation(summary = "获取热门话题", description = "获取当前最热门的10个话题")
    public ApiResponse<List<Topic>> getHotTopics() {
        log.info("获取热门话题");
        List<Topic> hotTopics = communityService.getHotTopics();
        return ApiResponse.success(hotTopics);
    }

    /**
     * 为帖子添加话题标签
     */
    @PostMapping("/posts/{postId}/topics")
    @Operation(summary = "为帖子添加话题标签", description = "最多添加3个话题标签")
    public ApiResponse<Void> addTopicsToPost(
        @Parameter(description = "帖子ID") @PathVariable Long postId,
        @RequestBody Map<String, List<Long>> request
    ) {
        log.info("为帖子添加话题标签: postId={}", postId);
        List<Long> topicIds = request.get("topicIds");
        communityService.addTopicTagsToPost(postId, topicIds);
        return ApiResponse.success();
    }

    /**
     * 移除帖子的话题标签
     */
    @DeleteMapping("/posts/{postId}/topics")
    @Operation(summary = "移除帖子的话题标签")
    public ApiResponse<Void> removeTopicsFromPost(
        @Parameter(description = "帖子ID") @PathVariable Long postId
    ) {
        log.info("移除帖子的话题标签: postId={}", postId);
        communityService.removeTopicTagsFromPost(postId);
        return ApiResponse.success();
    }

    /**
     * 点赞帖子
     */
    @PostMapping("/posts/{postId}/like")
    @Operation(summary = "点赞帖子")
    public ApiResponse<Void> likePost(
        @Parameter(description = "帖子ID") @PathVariable Long postId
    ) {
        Long userId = getCurrentUserId();
        log.info("点赞帖子: postId={}, userId={}", postId, userId);
        communityService.likePost(postId, userId);
        return ApiResponse.success();
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/posts/{postId}/like")
    @Operation(summary = "取消点赞")
    public ApiResponse<Void> unlikePost(
        @Parameter(description = "帖子ID") @PathVariable Long postId
    ) {
        Long userId = getCurrentUserId();
        log.info("取消点赞: postId={}, userId={}", postId, userId);
        communityService.unlikePost(postId, userId);
        return ApiResponse.success();
    }

    /**
     * 收藏帖子
     */
    @PostMapping("/posts/{postId}/collect")
    @Operation(summary = "收藏帖子")
    public ApiResponse<Void> collectPost(
        @Parameter(description = "帖子ID") @PathVariable Long postId
    ) {
        Long userId = getCurrentUserId();
        log.info("收藏帖子: postId={}, userId={}", postId, userId);
        communityService.collectPost(postId, userId);
        return ApiResponse.success();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/posts/{postId}/collect")
    @Operation(summary = "取消收藏")
    public ApiResponse<Void> uncollectPost(
        @Parameter(description = "帖子ID") @PathVariable Long postId
    ) {
        Long userId = getCurrentUserId();
        log.info("取消收藏: postId={}, userId={}", postId, userId);
        communityService.uncollectPost(postId, userId);
        return ApiResponse.success();
    }

    /**
     * 获取用户动态流
     */
    @GetMapping("/feed")
    @Operation(summary = "获取用户动态流", description = "获取关注用户的动态")
    public ApiResponse<List<UserFeed>> getUserFeed() {
        Long userId = getCurrentUserId();
        log.info("获取用户动态流: userId={}", userId);
        List<UserFeed> feeds = communityService.getUserFeed(userId);
        return ApiResponse.success(feeds);
    }

    /**
     * 获取话题下的帖子ID列表
     */
    @GetMapping("/topics/{topicId}/posts")
    @Operation(summary = "获取话题下的帖子", description = "获取指定话题下的所有帖子ID")
    public ApiResponse<List<Long>> getPostsByTopic(
        @Parameter(description = "话题ID") @PathVariable Long topicId
    ) {
        log.info("获取话题下的帖子: topicId={}", topicId);
        List<Long> postIds = communityService.getPostIdsByTopicId(topicId);
        return ApiResponse.success(postIds);
    }

    /**
     * 检查用户是否已点赞
     */
    @GetMapping("/posts/{postId}/liked")
    @Operation(summary = "检查是否已点赞")
    public ApiResponse<Boolean> checkPostLiked(
        @Parameter(description = "帖子ID") @PathVariable Long postId
    ) {
        Long userId = getCurrentUserId();
        boolean liked = communityService.isPostLikedByUser(postId, userId);
        return ApiResponse.success(liked);
    }

    /**
     * 检查用户是否已收藏
     */
    @GetMapping("/posts/{postId}/collected")
    @Operation(summary = "检查是否已收藏")
    public ApiResponse<Boolean> checkPostCollected(
        @Parameter(description = "帖子ID") @PathVariable Long postId
    ) {
        Long userId = getCurrentUserId();
        boolean collected = communityService.isPostCollectedByUser(postId, userId);
        return ApiResponse.success(collected);
    }

    /**
     * 获取帖子点赞数
     */
    @GetMapping("/posts/{postId}/likes/count")
    @Operation(summary = "获取帖子点赞数")
    public ApiResponse<Long> getPostLikeCount(
        @Parameter(description = "帖子ID") @PathVariable Long postId
    ) {
        long count = communityService.getPostLikeCount(postId);
        return ApiResponse.success(count);
    }

    /**
     * 获取帖子收藏数
     */
    @GetMapping("/posts/{postId}/collects/count")
    @Operation(summary = "获取帖子收藏数")
    public ApiResponse<Long> getPostCollectCount(
        @Parameter(description = "帖子ID") @PathVariable Long postId
    ) {
        long count = communityService.getPostCollectCount(postId);
        return ApiResponse.success(count);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
