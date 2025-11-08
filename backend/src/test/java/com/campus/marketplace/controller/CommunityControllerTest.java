package com.campus.marketplace.controller;

import com.campus.marketplace.common.entity.Topic;
import com.campus.marketplace.common.entity.UserFeed;
import com.campus.marketplace.common.enums.FeedType;
import com.campus.marketplace.service.CommunityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 社区控制器测试
 * 
 * 测试社区广场的API接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@WebMvcTest(CommunityController.class)
@Import(TestSecurityConfig.class)
@DisplayName("社区控制器测试")
class CommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommunityService communityService;

    @MockBean
    private com.campus.marketplace.repository.UserRepository userRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // Mock User for getCurrentUserId()
        com.campus.marketplace.common.entity.User mockUser = new com.campus.marketplace.common.entity.User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(java.util.Optional.of(mockUser));
    }

    @Test
    @DisplayName("获取热门话题 - 应该返回前10个热门话题")
    @WithMockUser
    void getHotTopics_ShouldReturnTop10Topics() throws Exception {
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

        when(communityService.getHotTopics())
            .thenReturn(Arrays.asList(topic1, topic2));

        // Act & Assert
        mockMvc.perform(get("/community/topics/hot"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].name").value("数码评测"))
            .andExpect(jsonPath("$.data[1].name").value("好物分享"));

        verify(communityService, times(1)).getHotTopics();
    }

    @Test
    @DisplayName("为帖子添加话题标签 - 应该成功添加")
    @WithMockUser
    void addTopicsToPost_ShouldSucceed() throws Exception {
        // Arrange
        Long postId = 1L;
        String requestBody = "{\"topicIds\": [1, 2, 3]}";

        doNothing().when(communityService).addTopicTagsToPost(eq(postId), anyList());

        // Act & Assert
        mockMvc.perform(post("/community/posts/{postId}/topics", postId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("操作成功"));

        verify(communityService, times(1)).addTopicTagsToPost(eq(postId), anyList());
    }

    @Test
    @DisplayName("点赞帖子 - 应该成功点赞")
    @WithMockUser(username = "testuser")
    void likePost_ShouldSucceed() throws Exception {
        // Arrange
        Long postId = 1L;

        doNothing().when(communityService).likePost(eq(postId), anyLong());

        // Act & Assert
        mockMvc.perform(post("/community/posts/{postId}/like", postId)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("操作成功"));

        verify(communityService, times(1)).likePost(eq(postId), anyLong());
    }

    @Test
    @DisplayName("取消点赞 - 应该成功取消")
    @WithMockUser(username = "testuser")
    void unlikePost_ShouldSucceed() throws Exception {
        // Arrange
        Long postId = 1L;

        doNothing().when(communityService).unlikePost(eq(postId), anyLong());

        // Act & Assert
        mockMvc.perform(delete("/community/posts/{postId}/like", postId)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("操作成功"));

        verify(communityService, times(1)).unlikePost(eq(postId), anyLong());
    }

    @Test
    @DisplayName("收藏帖子 - 应该成功收藏")
    @WithMockUser(username = "testuser")
    void collectPost_ShouldSucceed() throws Exception {
        // Arrange
        Long postId = 1L;

        doNothing().when(communityService).collectPost(eq(postId), anyLong());

        // Act & Assert
        mockMvc.perform(post("/community/posts/{postId}/collect", postId)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("操作成功"));

        verify(communityService, times(1)).collectPost(eq(postId), anyLong());
    }

    @Test
    @DisplayName("获取用户动态流 - 应该返回动态列表")
    @WithMockUser(username = "testuser")
    void getUserFeed_ShouldReturnFeedList() throws Exception {
        // Arrange
        UserFeed feed1 = UserFeed.builder()
            .userId(1L)
            .feedType(FeedType.POST)
            .targetId(1L)
            .actorId(2L)
            .build();
        feed1.setId(1L);

        UserFeed feed2 = UserFeed.builder()
            .userId(1L)
            .feedType(FeedType.REVIEW)
            .targetId(2L)
            .actorId(3L)
            .build();
        feed2.setId(2L);

        when(communityService.getUserFeed(anyLong()))
            .thenReturn(Arrays.asList(feed1, feed2));

        // Act & Assert
        mockMvc.perform(get("/community/feed"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].feedType").value("POST"))
            .andExpect(jsonPath("$.data[1].feedType").value("REVIEW"));

        verify(communityService, times(1)).getUserFeed(anyLong());
    }

    @Test
    @DisplayName("获取话题下的帖子 - 应该返回帖子ID列表")
    @WithMockUser
    void getPostsByTopic_ShouldReturnPostIds() throws Exception {
        // Arrange
        Long topicId = 1L;
        List<Long> postIds = Arrays.asList(1L, 2L, 3L);

        when(communityService.getPostIdsByTopicId(topicId))
            .thenReturn(postIds);

        // Act & Assert
        mockMvc.perform(get("/community/topics/{topicId}/posts", topicId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0]").value(1))
            .andExpect(jsonPath("$.data[1]").value(2))
            .andExpect(jsonPath("$.data[2]").value(3));

        verify(communityService, times(1)).getPostIdsByTopicId(topicId);
    }

    @Test
    @DisplayName("获取帖子点赞数 - 应该返回点赞数")
    @WithMockUser
    void getPostLikeCount_ShouldReturnCount() throws Exception {
        // Arrange
        Long postId = 1L;
        when(communityService.getPostLikeCount(postId)).thenReturn(100L);

        // Act & Assert
        mockMvc.perform(get("/community/posts/{postId}/likes/count", postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(100));

        verify(communityService, times(1)).getPostLikeCount(postId);
    }

    @Test
    @DisplayName("检查用户是否已点赞 - 应该返回点赞状态")
    @WithMockUser(username = "testuser")
    void checkPostLiked_ShouldReturnLikedStatus() throws Exception {
        // Arrange
        Long postId = 1L;
        when(communityService.isPostLikedByUser(eq(postId), anyLong())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/community/posts/{postId}/liked", postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(true));

        verify(communityService, times(1)).isPostLikedByUser(eq(postId), anyLong());
    }
}
