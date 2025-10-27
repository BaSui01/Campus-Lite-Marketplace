package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.SendMessageRequest;
import com.campus.marketplace.common.entity.Post;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.repository.PostRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private MessageService messageService;
    @Mock private com.campus.marketplace.common.utils.SensitiveWordFilter sensitiveWordFilter;
    @Mock private com.campus.marketplace.service.ComplianceService complianceService;

    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        postService = new PostServiceImpl(postRepository, userRepository, sensitiveWordFilter, complianceService, redisTemplate, messageService);
    }

    @Test
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
}
