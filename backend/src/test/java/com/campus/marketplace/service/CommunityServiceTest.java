package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.*;
import com.campus.marketplace.common.enums.FeedType;
import com.campus.marketplace.repository.*;
import com.campus.marketplace.service.impl.CommunityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 社区服务测试
 * 
 * 测试社区广场的核心功能：话题管理、动态流、互动功能
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("社区服务测试")
class CommunityServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private TopicTagRepository topicTagRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostCollectRepository postCollectRepository;

    @Mock
    private UserFeedRepository userFeedRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private NotificationService notificationService;

    private CommunityService communityService;

    @BeforeEach
    void setUp() {
        communityService = new CommunityServiceImpl(
            topicRepository,
            topicTagRepository,
            postLikeRepository,
            postCollectRepository,
            userFeedRepository,
            postRepository,
            notificationService
        );
    }

    @Test
    @DisplayName("获取热门话题 - 应该按热度倒序返回前10个话题")
    void getHotTopics_ShouldReturnTop10TopicsByHotness() {
        // Arrange
        Topic topic1 = Topic.builder()
            .name("数码评测")
            .description("分享数码产品使用体验")
            .hotness(1000)
            .build();
        topic1.setId(1L);
        
        Topic topic2 = Topic.builder()
            .name("好物分享")
            .description("推荐好用的二手商品")
            .hotness(800)
            .build();
        topic2.setId(2L);

        when(topicRepository.findTop10ByOrderByHotnessDesc())
            .thenReturn(Arrays.asList(topic1, topic2));

        // Act
        List<Topic> hotTopics = communityService.getHotTopics();

        // Assert
        assertThat(hotTopics).hasSize(2);
        assertThat(hotTopics.get(0).getHotness()).isEqualTo(1000);
        assertThat(hotTopics.get(1).getHotness()).isEqualTo(800);
        verify(topicRepository, times(1)).findTop10ByOrderByHotnessDesc();
    }

    @Test
    @DisplayName("发布帖子并添加话题标签 - 应该创建帖子和话题关联")
    void publishPostWithTopics_ShouldCreatePostAndTopicTags() {
        // Arrange
        Long postId = 1L;
        List<Long> topicIds = Arrays.asList(1L, 2L);

        Post post = new Post();
        post.setId(postId);

        // Mock existsById instead of findById
        when(postRepository.existsById(postId)).thenReturn(true);

        // Act
        communityService.addTopicTagsToPost(postId, topicIds);

        // Assert
        verify(topicTagRepository, times(2)).save(any(TopicTag.class));
    }

    @Test
    @DisplayName("点赞帖子 - 应该创建点赞记录并增加帖子点赞数")
    void likePost_ShouldCreateLikeRecordAndIncrementLikeCount() {
        // Arrange
        Long postId = 1L;
        Long userId = 100L;

        Post post = new Post();
        post.setId(postId);
        post.setAuthorId(200L);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsByPostIdAndUserId(postId, userId))
            .thenReturn(false);

        // Act
        communityService.likePost(postId, userId);

        // Assert
        verify(postLikeRepository, times(1)).save(any(PostLike.class));
        verify(notificationService, times(1)).sendNotification(
            eq(200L),
            any(),
            anyString(),
            anyString(),
            anyLong(),
            anyString(),
            anyString()
        );
    }

    @Test
    @DisplayName("取消点赞 - 应该删除点赞记录并减少帖子点赞数")
    void unlikePost_ShouldRemoveLikeRecordAndDecrementLikeCount() {
        // Arrange
        Long postId = 1L;
        Long userId = 100L;

        PostLike postLike = PostLike.builder()
            .postId(postId)
            .userId(userId)
            .build();
        postLike.setId(1L);

        when(postLikeRepository.findByPostIdAndUserId(postId, userId))
            .thenReturn(Optional.of(postLike));

        // Act
        communityService.unlikePost(postId, userId);

        // Assert
        verify(postLikeRepository, times(1)).delete(postLike);
    }

    @Test
    @DisplayName("收藏帖子 - 应该创建收藏记录")
    void collectPost_ShouldCreateCollectRecord() {
        // Arrange
        Long postId = 1L;
        Long userId = 100L;

        Post post = new Post();
        post.setId(postId);

        // Mock existsById instead of findById
        when(postRepository.existsById(postId)).thenReturn(true);
        when(postCollectRepository.existsByPostIdAndUserId(postId, userId))
            .thenReturn(false);

        // Act
        communityService.collectPost(postId, userId);

        // Assert
        verify(postCollectRepository, times(1)).save(any(PostCollect.class));
    }

    @Test
    @DisplayName("获取用户动态流 - 应该返回关注用户的动态")
    void getUserFeed_ShouldReturnFollowedUsersActivities() {
        // Arrange
        Long userId = 100L;

        UserFeed feed1 = UserFeed.builder()
            .userId(userId)
            .feedType(FeedType.POST)
            .targetId(1L)
            .build();

        UserFeed feed2 = UserFeed.builder()
            .userId(userId)
            .feedType(FeedType.REVIEW)
            .targetId(2L)
            .build();

        when(userFeedRepository.findByUserIdOrderByCreatedAtDesc(userId))
            .thenReturn(Arrays.asList(feed1, feed2));

        // Act
        List<UserFeed> feeds = communityService.getUserFeed(userId);

        // Assert
        assertThat(feeds).hasSize(2);
        assertThat(feeds.get(0).getFeedType()).isEqualTo(FeedType.POST);
        assertThat(feeds.get(1).getFeedType()).isEqualTo(FeedType.REVIEW);
    }

    @Test
    @DisplayName("获取话题下的帖子 - 应该返回所有关联的帖子")
    void getPostsByTopic_ShouldReturnAllAssociatedPosts() {
        // Arrange
        Long topicId = 1L;

        TopicTag tag1 = TopicTag.builder()
            .topicId(topicId)
            .postId(1L)
            .build();

        TopicTag tag2 = TopicTag.builder()
            .topicId(topicId)
            .postId(2L)
            .build();

        when(topicTagRepository.findByTopicId(topicId))
            .thenReturn(Arrays.asList(tag1, tag2));

        // Act
        List<Long> postIds = communityService.getPostIdsByTopicId(topicId);

        // Assert
        assertThat(postIds).hasSize(2);
        assertThat(postIds).containsExactly(1L, 2L);
    }
}
