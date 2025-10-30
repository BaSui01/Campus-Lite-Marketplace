package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreatePostRequest;
import com.campus.marketplace.common.entity.Post;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.common.utils.SensitiveWordFilter;
import com.campus.marketplace.repository.PostRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.PostServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("帖子服务测试")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SensitiveWordFilter sensitiveWordFilter;

    @Mock
    private ComplianceService complianceService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PostServiceImpl postService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User testUser;
    private CreatePostRequest validRequest;

    @BeforeEach
    void setUp() {
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .status(UserStatus.ACTIVE)
                .build();
        validRequest = new CreatePostRequest(
                "这是一个测试帖子标题",
                "这是测试帖子的内容，包含一些详细信息用于测试",
                List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg")
        );
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 默认合规通过
        lenient().when(complianceService.moderateText(anyString(), anyString()))
                .thenAnswer(inv -> new ComplianceService.TextResult(false, com.campus.marketplace.common.enums.ComplianceAction.PASS, (String) inv.getArgument(0), java.util.Set.of()));
        lenient().when(complianceService.scanImages(anyList(), anyString()))
                .thenReturn(new ComplianceService.ImageResult(ComplianceService.ImageAction.PASS, null));
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    @DisplayName("发帖成功")
    void createPost_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(complianceService.moderateText(anyString(), anyString()))
                .thenAnswer(inv -> new ComplianceService.TextResult(false, com.campus.marketplace.common.enums.ComplianceAction.PASS, (String) inv.getArgument(0), java.util.Set.of()));
        when(complianceService.scanImages(anyList(), anyString()))
                .thenReturn(new ComplianceService.ImageResult(ComplianceService.ImageAction.PASS, null));
        when(valueOperations.increment(anyString(), eq(0L))).thenReturn(0L);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(100L);
            return post;
        });
        Long postId = postService.createPost(validRequest);
        assertThat(postId).isNotNull().isEqualTo(100L);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getTitle()).isEqualTo(validRequest.title());
        assertThat(savedPost.getStatus()).isEqualTo(GoodsStatus.PENDING);
    }

    @Test
    @DisplayName("用户不存在")
    void createPost_UserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> postService.createPost(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("敏感词过滤")
    void createPost_SensitiveWordFiltered() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(complianceService.moderateText(eq("包含傻逼的标题"), anyString()))
                .thenReturn(new ComplianceService.TextResult(true, com.campus.marketplace.common.enums.ComplianceAction.REVIEW, "包含**的标题", java.util.Set.of("傻逼")));
        when(complianceService.moderateText(eq("正常内容"), anyString()))
                .thenReturn(new ComplianceService.TextResult(false, com.campus.marketplace.common.enums.ComplianceAction.PASS, "正常内容", java.util.Set.of()));
        when(valueOperations.increment(anyString(), eq(0L))).thenReturn(0L);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(100L);
            return post;
        });
        CreatePostRequest requestWithBadWord = new CreatePostRequest("包含傻逼的标题", "正常内容", List.of());
        Long postId = postService.createPost(requestWithBadWord);
        assertThat(postId).isNotNull();
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getTitle()).isEqualTo("包含**的标题");
    }

    @Test
    @DisplayName("超过每日限制")
    void createPost_ExceedsDailyLimit() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(valueOperations.increment(anyString(), eq(0L))).thenReturn(10L);
        assertThatThrownBy(() -> postService.createPost(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_LIMIT_EXCEEDED);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("无图片")
    void createPost_WithoutImages() {
        CreatePostRequest requestWithoutImages = new CreatePostRequest("无图片的帖子", "这是没有图片的帖子内容", null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(complianceService.moderateText(anyString(), anyString()))
                .thenAnswer(inv -> new ComplianceService.TextResult(false, com.campus.marketplace.common.enums.ComplianceAction.PASS, (String) inv.getArgument(0), java.util.Set.of()));
        when(valueOperations.increment(anyString(), eq(0L))).thenReturn(0L);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(100L);
            return post;
        });
        Long postId = postService.createPost(requestWithoutImages);
        assertThat(postId).isNotNull();
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getImages()).isNull();
    }

    @Test
    @DisplayName("当日首次发帖")
    void createPost_FirstPostOfDay() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(complianceService.moderateText(anyString(), anyString()))
                .thenAnswer(inv -> new ComplianceService.TextResult(false, com.campus.marketplace.common.enums.ComplianceAction.PASS, (String) inv.getArgument(0), java.util.Set.of()));
        when(valueOperations.increment(anyString(), eq(0L))).thenReturn(null);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(100L);
            return post;
        });
        Long postId = postService.createPost(validRequest);
        assertThat(postId).isNotNull();
        verify(valueOperations).increment(contains("post:limit:1"), eq(1L));
    }
}