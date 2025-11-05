package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.Topic;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 话题控制器
 * 
 * 提供话题管理的API接口：CRUD、关注、热门推荐
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@Tag(name = "话题管理", description = "话题相关接口")
public class TopicController {

    private final TopicService topicService;
    private final UserRepository userRepository;

        @PostMapping
    @Operation(summary = "创建话题")
    public ApiResponse<Long> createTopic(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");
        log.info("创建话题: name={}", name);
        Long topicId = topicService.createTopic(name, description);
        return ApiResponse.success(topicId);
    }

        @PutMapping("/{topicId}")
    @Operation(summary = "更新话题")
    public ApiResponse<Void> updateTopic(
        @Parameter(description = "话题ID") @PathVariable Long topicId,
        @RequestBody Map<String, String> request
    ) {
        String name = request.get("name");
        String description = request.get("description");
        log.info("更新话题: topicId={}, name={}", topicId, name);
        topicService.updateTopic(topicId, name, description);
        return ApiResponse.success();
    }

        @DeleteMapping("/{topicId}")
    @Operation(summary = "删除话题")
    public ApiResponse<Void> deleteTopic(
        @Parameter(description = "话题ID") @PathVariable Long topicId
    ) {
        log.info("删除话题: topicId={}", topicId);
        topicService.deleteTopic(topicId);
        return ApiResponse.success();
    }

        @GetMapping("/{topicId}")
    @Operation(summary = "查询话题详情")
    public ApiResponse<Topic> getTopicById(
        @Parameter(description = "话题ID") @PathVariable Long topicId
    ) {
        Topic topic = topicService.getTopicById(topicId);
        return ApiResponse.success(topic);
    }

        @GetMapping
    @Operation(summary = "获取所有话题")
    public ApiResponse<List<Topic>> getAllTopics() {
        List<Topic> topics = topicService.getAllTopics();
        return ApiResponse.success(topics);
    }

        @GetMapping("/hot")
    @Operation(summary = "获取热门话题")
    public ApiResponse<List<Topic>> getHotTopics() {
        List<Topic> topics = topicService.getHotTopics();
        return ApiResponse.success(topics);
    }

        @PostMapping("/{topicId}/follow")
    @Operation(summary = "关注话题")
    public ApiResponse<Void> followTopic(
        @Parameter(description = "话题ID") @PathVariable Long topicId
    ) {
        Long userId = getCurrentUserId();
        log.info("关注话题: topicId={}, userId={}", topicId, userId);
        topicService.followTopic(topicId, userId);
        return ApiResponse.success();
    }

        @DeleteMapping("/{topicId}/follow")
    @Operation(summary = "取消关注话题")
    public ApiResponse<Void> unfollowTopic(
        @Parameter(description = "话题ID") @PathVariable Long topicId
    ) {
        Long userId = getCurrentUserId();
        log.info("取消关注话题: topicId={}, userId={}", topicId, userId);
        topicService.unfollowTopic(topicId, userId);
        return ApiResponse.success();
    }

        @GetMapping("/followed")
    @Operation(summary = "获取我关注的话题")
    public ApiResponse<List<Topic>> getUserFollowedTopics() {
        Long userId = getCurrentUserId();
        List<Topic> topics = topicService.getUserFollowedTopics(userId);
        return ApiResponse.success(topics);
    }

        @GetMapping("/{topicId}/followed")
    @Operation(summary = "检查是否已关注")
    public ApiResponse<Boolean> checkTopicFollowed(
        @Parameter(description = "话题ID") @PathVariable Long topicId
    ) {
        Long userId = getCurrentUserId();
        boolean followed = topicService.isTopicFollowedByUser(topicId, userId);
        return ApiResponse.success(followed);
    }

        @GetMapping("/{topicId}/followers/count")
    @Operation(summary = "获取话题关注人数")
    public ApiResponse<Long> getTopicFollowerCount(
        @Parameter(description = "话题ID") @PathVariable Long topicId
    ) {
        long count = topicService.getTopicFollowerCount(topicId);
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
