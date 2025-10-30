package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateReplyRequest;
import com.campus.marketplace.common.entity.Post;
import com.campus.marketplace.common.entity.Reply;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.common.utils.SensitiveWordFilter;
import com.campus.marketplace.repository.PostRepository;
import com.campus.marketplace.repository.ReplyRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.ReplyServiceImpl;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("回复服务测试")
class ReplyServiceTest {

    @Mock
    private ReplyRepository replyRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SensitiveWordFilter sensitiveWordFilter;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ReplyServiceImpl replyService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User testUser;
    private Post testPost;
    private CreateReplyRequest validRequest;

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
        testPost = Post.builder()
                .title("测试帖子")
                .content("测试内容")
                .authorId(2L)
                .status(GoodsStatus.APPROVED)
                .viewCount(0)
                .replyCount(0)
                .build();
        testPost.setId(100L);
        validRequest = new CreateReplyRequest(100L, "这是一个测试回复", null, null);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    @DisplayName("回复成功")
    void createReply_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(100L)).thenReturn(Optional.of(testPost));
        when(sensitiveWordFilter.filter(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(valueOperations.increment(anyString(), eq(0L))).thenReturn(0L);
        when(replyRepository.save(any(Reply.class))).thenAnswer(invocation -> {
            Reply reply = invocation.getArgument(0);
            reply.setId(200L);
            return reply;
        });
        Long replyId = replyService.createReply(validRequest);
        assertThat(replyId).isNotNull().isEqualTo(200L);
        ArgumentCaptor<Reply> replyCaptor = ArgumentCaptor.forClass(Reply.class);
        verify(replyRepository).save(replyCaptor.capture());
        Reply savedReply = replyCaptor.getValue();
        assertThat(savedReply.getContent()).isEqualTo(validRequest.content());
        assertThat(savedReply.getPostId()).isEqualTo(100L);
        assertThat(savedReply.getAuthorId()).isEqualTo(testUser.getId());
        verify(postRepository).save(testPost);
        assertThat(testPost.getReplyCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("帖子不存在")
    void createReply_PostNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> replyService.createReply(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
        verify(replyRepository, never()).save(any(Reply.class));
    }

    @Test
    @DisplayName("帖子未审核")
    void createReply_PostNotApproved() {
        testPost.setStatus(GoodsStatus.PENDING);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(100L)).thenReturn(Optional.of(testPost));
        assertThatThrownBy(() -> replyService.createReply(validRequest))
                .isInstanceOf(BusinessException.class);
        verify(replyRepository, never()).save(any(Reply.class));
    }

    @Test
    @DisplayName("敏感词过滤")
    void createReply_SensitiveWordFiltered() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(100L)).thenReturn(Optional.of(testPost));
        when(sensitiveWordFilter.filter(eq("包含傻逼的回复"))).thenReturn("包含**的回复");
        when(valueOperations.increment(anyString(), eq(0L))).thenReturn(0L);
        when(replyRepository.save(any(Reply.class))).thenAnswer(invocation -> {
            Reply reply = invocation.getArgument(0);
            reply.setId(200L);
            return reply;
        });
        CreateReplyRequest requestWithBadWord = new CreateReplyRequest(100L, "包含傻逼的回复", null, null);
        Long replyId = replyService.createReply(requestWithBadWord);
        assertThat(replyId).isNotNull();
        ArgumentCaptor<Reply> replyCaptor = ArgumentCaptor.forClass(Reply.class);
        verify(replyRepository).save(replyCaptor.capture());
        assertThat(replyCaptor.getValue().getContent()).isEqualTo("包含**的回复");
    }

    @Test
    @DisplayName("超过频率限制")
    void createReply_ExceedsRateLimit() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(100L)).thenReturn(Optional.of(testPost));
        when(valueOperations.increment(anyString(), eq(0L))).thenReturn(5L);
        assertThatThrownBy(() -> replyService.createReply(validRequest))
                .isInstanceOf(BusinessException.class);
        verify(replyRepository, never()).save(any(Reply.class));
    }

    @Test
    @DisplayName("楼中楼回复")
    void createReply_SubReply() {
        CreateReplyRequest subReplyRequest = new CreateReplyRequest(100L, "楼中楼回复", 50L, 2L);
        Reply parentReply = Reply.builder().postId(100L).content("父回复").authorId(2L).build();
        parentReply.setId(50L);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(100L)).thenReturn(Optional.of(testPost));
        when(replyRepository.findById(50L)).thenReturn(Optional.of(parentReply));
        when(sensitiveWordFilter.filter(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(valueOperations.increment(anyString(), eq(0L))).thenReturn(0L);
        when(replyRepository.save(any(Reply.class))).thenAnswer(invocation -> {
            Reply reply = invocation.getArgument(0);
            reply.setId(200L);
            return reply;
        });
        Long replyId = replyService.createReply(subReplyRequest);
        assertThat(replyId).isNotNull();
        ArgumentCaptor<Reply> replyCaptor = ArgumentCaptor.forClass(Reply.class);
        verify(replyRepository).save(replyCaptor.capture());
        Reply savedReply = replyCaptor.getValue();
        assertThat(savedReply.getParentId()).isEqualTo(50L);
        assertThat(savedReply.getToUserId()).isEqualTo(2L);
    }
}