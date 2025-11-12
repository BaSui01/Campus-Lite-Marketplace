package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.*;
import com.campus.marketplace.common.dto.UserFeedDTO;
import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.common.enums.TargetType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.*;
import com.campus.marketplace.service.CommunityService;
import com.campus.marketplace.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 社区服务实现类
 * 
 * 实现社区广场的核心功能：话题管理、动态流、互动功能
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final TopicRepository topicRepository;
    private final TopicTagRepository topicTagRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final UserFeedRepository userFeedRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Override
    public List<Topic> getHotTopics() {
        log.info("获取热门话题");
        return topicRepository.findTop10ByOrderByHotnessDesc();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTopicTagsToPost(Long postId, List<Long> topicIds) {
        log.info("为帖子添加话题标签: postId={}, topicIds={}", postId, topicIds);

        if (topicIds == null || topicIds.isEmpty()) {
            return;
        }

        if (topicIds.size() > 3) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "话题标签最多3个");
        }

        if (!postRepository.existsById(postId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 先删除现有标签
        topicTagRepository.deleteByPostId(postId);

        // 添加新标签
        for (Long topicId : topicIds) {
            if (!topicTagRepository.existsByPostIdAndTopicId(postId, topicId)) {
                TopicTag tag = TopicTag.builder()
                    .postId(postId)
                    .topicId(topicId)
                    .build();
                topicTagRepository.save(tag);

                // 更新话题统计
                topicRepository.findById(topicId).ifPresent(topic -> {
                    topic.incrementPostCount();
                    topic.updateHotness();
                    topicRepository.save(topic);
                });
            }
        }

        log.info("话题标签添加成功: postId={}", postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeTopicTagsFromPost(Long postId) {
        log.info("移除帖子的话题标签: postId={}", postId);

        List<TopicTag> tags = topicTagRepository.findByPostId(postId);
        
        for (TopicTag tag : tags) {
            // 更新话题统计
            topicRepository.findById(tag.getTopicId()).ifPresent(topic -> {
                topic.decrementPostCount();
                topic.updateHotness();
                topicRepository.save(topic);
            });
        }

        topicTagRepository.deleteByPostId(postId);
        log.info("话题标签移除成功: postId={}", postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likePost(Long postId, Long userId) {
        log.info("点赞帖子: postId={}, userId={}", postId, userId);

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "已经点赞过了");
        }

        // 创建点赞记录
        PostLike postLike = PostLike.builder()
            .postId(postId)
            .userId(userId)
            .build();
        postLikeRepository.save(postLike);

        // 🔥 新增：更新帖子点赞数（2025-11-09 - BaSui 😎）
        post.incrementLikeCount();
        postRepository.save(post);
        log.info("帖子点赞数已更新: postId={}, likeCount={}", postId, post.getLikeCount());

        // 发送通知给帖子作者（不是自己的帖子）
        if (!post.getAuthorId().equals(userId)) {
            try {
                notificationService.sendNotification(
                    post.getAuthorId(),
                    NotificationType.POST_REPLIED,
                    "有人点赞了你的帖子",
                    "你的帖子《" + post.getTitle() + "》获得了新的点赞",
                    postId,
                    "POST",
                    "/posts/" + postId
                );
            } catch (Exception e) {
                log.error("发送点赞通知失败: postId={}, userId={}", postId, userId, e);
            }
        }

        log.info("帖子点赞成功: postId={}, userId={}", postId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikePost(Long postId, Long userId) {
        log.info("取消点赞: postId={}, userId={}", postId, userId);

        PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "点赞记录不存在"));

        postLikeRepository.delete(postLike);

        // 🔥 新增：更新帖子点赞数（2025-11-09 - BaSui 😎）
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        post.decrementLikeCount();
        postRepository.save(post);
        log.info("帖子点赞数已更新: postId={}, likeCount={}", postId, post.getLikeCount());

        log.info("取消点赞成功: postId={}, userId={}", postId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectPost(Long postId, Long userId) {
        log.info("收藏帖子: postId={}, userId={}", postId, userId);

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (postCollectRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "已经收藏过了");
        }

        // 创建收藏记录
        PostCollect postCollect = PostCollect.builder()
            .postId(postId)
            .userId(userId)
            .build();
        postCollectRepository.save(postCollect);

        // 🔥 新增：更新帖子收藏数（2025-11-09 - BaSui 😎）
        post.incrementCollectCount();
        postRepository.save(post);
        log.info("帖子收藏数已更新: postId={}, collectCount={}", postId, post.getCollectCount());

        log.info("帖子收藏成功: postId={}, userId={}", postId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollectPost(Long postId, Long userId) {
        log.info("取消收藏: postId={}, userId={}", postId, userId);

        PostCollect postCollect = postCollectRepository.findByPostIdAndUserId(postId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "收藏记录不存在"));

        postCollectRepository.delete(postCollect);

        // 🔥 新增：更新帖子收藏数（2025-11-09 - BaSui 😎）
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        post.decrementCollectCount();
        postRepository.save(post);
        log.info("帖子收藏数已更新: postId={}, collectCount={}", postId, post.getCollectCount());

        log.info("取消收藏成功: postId={}, userId={}", postId, userId);
    }

    @Override
    public List<UserFeed> getUserFeed(Long userId) {
        log.info("获取用户动态流: userId={}", userId);
        // 关键修复：联查加载 actor，确保前端能拿到头像与昵称（nickname 为空时由前端做用户名兜底）
        return userFeedRepository.findByUserIdOrderByCreatedAtDescWithActor(userId);
    }

    @Override
    public List<UserFeedDTO> getUserFeedV2(Long userId) {
        log.info("获取用户动态流(v2 DTO): userId={}", userId);
        List<UserFeed> feeds = userFeedRepository.findByUserIdOrderByCreatedAtDescWithActor(userId);
        return feeds.stream().map(f -> {
            User actor = f.getActor();
            String displayName = null;
            String avatarUrl = null;
            if (actor != null) {
                avatarUrl = actor.getAvatar();
                String nickname = actor.getNickname();
                displayName = (nickname != null && !nickname.trim().isEmpty()) ? nickname : actor.getUsername();
            }
            TargetType targetType = f.getTargetType() != null ? f.getTargetType() : TargetType.POST;
            return UserFeedDTO.builder()
                .id(f.getId())
                .actorId(f.getActorId())
                .displayName(displayName)
                .avatarUrl(avatarUrl)
                .feedType(f.getFeedType())
                .targetType(targetType)
                .targetId(f.getTargetId())
                .createdAt(f.getCreatedAt())
                .build();
        }).toList();
    }

    @Override
    public List<Long> getPostIdsByTopicId(Long topicId) {
        log.info("获取话题下的帖子ID列表: topicId={}", topicId);
        List<TopicTag> tags = topicTagRepository.findByTopicId(topicId);
        return tags.stream()
            .map(TopicTag::getPostId)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isPostLikedByUser(Long postId, Long userId) {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Override
    public boolean isPostCollectedByUser(Long postId, Long userId) {
        return postCollectRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Override
    public long getPostLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Override
    public long getPostCollectCount(Long postId) {
        return postCollectRepository.countByPostId(postId);
    }
}
