package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreatePostRequest;
import com.campus.marketplace.common.dto.request.SendMessageRequest;
import com.campus.marketplace.common.dto.request.UpdatePostRequest;
import com.campus.marketplace.common.entity.Post;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.ComplianceAction;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.PostRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.PostServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Post Service Impl Test
 *
 * @author BaSui
 * @date 2025-10-29
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("帖子服务测试")
class PostServiceImplTest {

    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;
    @Mock private MessageService messageService;
    @Mock private com.campus.marketplace.common.utils.SensitiveWordFilter sensitiveWordFilter;
    @Mock private com.campus.marketplace.service.ComplianceService complianceService;

    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        postService = new PostServiceImpl(postRepository, userRepository, sensitiveWordFilter, complianceService, redisTemplate, messageService);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("帖子审核通过应发送通知")
    void approvePost_shouldSendApproveNotification() {
        Post post = Post.builder().title("测试帖子").authorId(10L).status(GoodsStatus.PENDING).build();
        when(postRepository.findById(1L)).thenReturn(java.util.Optional.of(post));

        postService.approvePost(1L, true, null);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(messageService, times(1)).sendMessage(captor.capture());
        SendMessageRequest req = captor.getValue();
        assertThat(req.receiverId()).isEqualTo(10L);
        assertThat(req.content()).contains("已审核通过");
        assertThat(post.getStatus()).isEqualTo(GoodsStatus.APPROVED);
    }

    @Test
    @DisplayName("帖子审核拒绝应发送通知")
    void approvePost_shouldSendRejectNotification() {
        Post post = Post.builder().title("测试帖子2").authorId(11L).status(GoodsStatus.PENDING).build();
        when(postRepository.findById(2L)).thenReturn(java.util.Optional.of(post));

        postService.approvePost(2L, false, "不符合规范");

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(messageService, times(1)).sendMessage(captor.capture());
        SendMessageRequest req = captor.getValue();
        assertThat(req.receiverId()).isEqualTo(11L);
        assertThat(req.content()).contains("未通过").contains("不符合规范");
        assertThat(post.getStatus()).isEqualTo(GoodsStatus.REJECTED);
    }

    @Test
    @DisplayName("创建帖子成功应过滤内容并更新限流计数")
    void createPost_shouldFilterAndIncrementLimit() {
        setAuthentication("laowang", List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
        User user = User.builder().username("laowang").campusId(200L).build();
        user.setId(5L);
        when(userRepository.findByUsername("laowang")).thenReturn(java.util.Optional.of(user));
        when(valueOperations.increment("post:limit:" + user.getId(), 0L)).thenReturn(3L);
        when(valueOperations.increment("post:limit:" + user.getId(), 1L)).thenReturn(4L);
        when(complianceService.moderateText("原始标题", "POST_TITLE"))
                .thenReturn(new com.campus.marketplace.service.ComplianceService.TextResult(true, ComplianceAction.REVIEW, "干净标题", Set.of()));
        when(complianceService.moderateText("原始内容", "POST_CONTENT"))
                .thenReturn(new com.campus.marketplace.service.ComplianceService.TextResult(false, ComplianceAction.PASS, "干净内容", Set.of()));
        when(complianceService.scanImages(List.of("img1"), "POST_IMAGES"))
                .thenReturn(new com.campus.marketplace.service.ComplianceService.ImageResult(
                        com.campus.marketplace.service.ComplianceService.ImageAction.PASS, null));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        Long postId = postService.createPost(new CreatePostRequest("原始标题", "原始内容", List.of("img1")));

        assertThat(postId).isEqualTo(99L);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository, atLeastOnce()).save(postCaptor.capture());
        Post saved = postCaptor.getValue();
        assertThat(saved.getTitle()).isEqualTo("干净标题");
        assertThat(saved.getContent()).isEqualTo("干净内容");
        assertThat(saved.getStatus()).isEqualTo(GoodsStatus.PENDING);
        verify(valueOperations).increment("post:limit:" + user.getId(), 0L);
        verify(valueOperations).increment("post:limit:" + user.getId(), 1L);
        verify(redisTemplate).expire("post:limit:" + user.getId(), 1, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("超过每日发帖限额应抛出异常")
    void createPost_shouldFailWhenLimitExceeded() {
        setAuthentication("user1", List.of());
        User user = User.builder().username("user1").build();
        user.setId(8L);
        when(userRepository.findByUsername("user1")).thenReturn(java.util.Optional.of(user));
        when(valueOperations.increment("post:limit:" + user.getId(), 0L)).thenReturn(10L);

        assertThatThrownBy(() -> postService.createPost(new CreatePostRequest("标题", "内容", List.of())))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.POST_LIMIT_EXCEEDED.getCode());

        verify(postRepository, never()).save(any());
        verify(valueOperations, never()).increment("post:limit:" + user.getId(), 1L);
    }

    @Test
    @DisplayName("非作者且非管理员删除帖子应被拒绝")
    void deletePost_shouldRejectWhenNotOwnerOrAdmin() {
        setAuthentication("intruder", List.of());
        Post post = Post.builder().authorId(1L).status(GoodsStatus.APPROVED).build();
        User user = User.builder().username("intruder").build();
        user.setId(2L);
        when(postRepository.findById(3L)).thenReturn(java.util.Optional.of(post));
        when(userRepository.findByUsername("intruder")).thenReturn(java.util.Optional.of(user));

        assertThatThrownBy(() -> postService.deletePost(3L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.FORBIDDEN.getCode());

        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("作者修改帖子内容应重置为待审核")
    void updatePost_shouldResetStatusWhenContentChanged() {
        setAuthentication("author", List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
        Post post = Post.builder()
                .authorId(6L)
                .title("旧标题")
                .content("旧内容")
                .status(GoodsStatus.APPROVED)
                .build();
        post.setId(12L);
        User user = User.builder().username("author").build();
        user.setId(6L);
        when(postRepository.findById(12L)).thenReturn(java.util.Optional.of(post));
        when(userRepository.findByUsername("author")).thenReturn(java.util.Optional.of(user));
        when(complianceService.moderateText("新标题", "POST_TITLE"))
                .thenReturn(new com.campus.marketplace.service.ComplianceService.TextResult(false, ComplianceAction.PASS, "新标题", Set.of()));
        when(complianceService.moderateText("新内容", "POST_CONTENT"))
                .thenReturn(new com.campus.marketplace.service.ComplianceService.TextResult(false, ComplianceAction.PASS, "新内容", Set.of()));

        postService.updatePost(12L, new UpdatePostRequest("新标题", "新内容", List.of("img")));

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository, atLeastOnce()).save(captor.capture());
        Post saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(GoodsStatus.PENDING);
        assertThat(saved.getTitle()).isEqualTo("新标题");
        assertThat(saved.getImages()).containsExactly("img");
    }

    @Test
    @DisplayName("跨校访问帖子详情应拒绝")
    void getPostDetail_shouldRejectCrossCampusAccess() {
        setAuthentication("userx", List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
        Post post = Post.builder().authorId(9L).campusId(100L).content("c").title("t").status(GoodsStatus.APPROVED).build();
        User user = User.builder().username("userx").campusId(101L).build();
        user.setId(9L);
        when(postRepository.findByIdWithAuthor(15L)).thenReturn(java.util.Optional.of(post));
        when(userRepository.findByUsername("userx")).thenReturn(java.util.Optional.of(user));

        assertThatThrownBy(() -> postService.getPostDetail(15L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.FORBIDDEN.getCode());

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("查询帖子列表应根据校区过滤")
    void listPosts_shouldFilterByCampus() {
        setAuthentication("campusUser", List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
        User user = User.builder().username("campusUser").campusId(300L).build();
        user.setId(20L);
        when(userRepository.findByUsername("campusUser")).thenReturn(java.util.Optional.of(user));
        Page<Post> page = new PageImpl<>(List.of());
        when(postRepository.findByStatusAndCampusId(eq(GoodsStatus.APPROVED), eq(300L), any(PageRequest.class)))
                .thenReturn(page);

        Page<?> result = postService.listPosts(0, 10, "createdAt", "DESC");

        assertThat(result.getContent()).isEmpty();
        verify(postRepository).findByStatusAndCampusId(eq(GoodsStatus.APPROVED), eq(300L), any(PageRequest.class));
        verify(postRepository, never()).findByStatus(eq(GoodsStatus.APPROVED), any(PageRequest.class));
    }

    private void setAuthentication(String username, List<? extends GrantedAuthority> authorities) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, "pwd", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
