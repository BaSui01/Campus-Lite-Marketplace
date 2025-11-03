package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.*;
import com.campus.marketplace.common.enums.NotificationType;
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

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

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

        log.info("帖子收藏成功: postId={}, userId={}", postId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollectPost(Long postId, Long userId) {
        log.info("取消收藏: postId={}, userId={}", postId, userId);

        PostCollect postCollect = postCollectRepository.findByPostIdAndUserId(postId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "收藏记录不存在"));

        postCollectRepository.delete(postCollect);
        log.info("取消收藏成功: postId={}, userId={}", postId, userId);
    }

    @Override
    public List<UserFeed> getUserFeed(Long userId) {
        log.info("获取用户动态流: userId={}", userId);
        return userFeedRepository.findByUserIdOrderByCreatedAtDesc(userId);
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
