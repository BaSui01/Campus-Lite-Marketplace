package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.SendMessageRequest;
import com.campus.marketplace.common.entity.Conversation;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.BlacklistRepository;
import com.campus.marketplace.repository.ConversationRepository;
import com.campus.marketplace.repository.MessageRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.MessageServiceImpl;
import com.campus.marketplace.websocket.WebSocketSessionManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("消息-校区隔离测试")
class MessageServiceCampusTest {

    @Mock private UserRepository userRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private BlacklistRepository blacklistRepository;
    @Mock private com.campus.marketplace.common.utils.SensitiveWordFilter sensitiveWordFilter;
    @Mock private WebSocketSessionManager sessionManager;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private MessageServiceImpl messageService;

    private MockedStatic<com.campus.marketplace.common.utils.SecurityUtil> securityUtilMock;

    @BeforeEach
    void init() {
        securityUtilMock = mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class);
        securityUtilMock.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                .thenReturn("u1");
        securityUtilMock.when(() -> com.campus.marketplace.common.utils.SecurityUtil.hasAuthority(anyString()))
                .thenReturn(false);

        // RedisTemplate 深度桩，避免 ValueOperations 为空导致的 NPE
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.increment(anyString())).thenReturn(1L);
    }

    @AfterEach
    void close() {
        if (securityUtilMock != null) securityUtilMock.close();
    }

    @Test
    @DisplayName("跨校发送消息-无跨校权限被拒绝")
    void sendMessage_forbidden_whenCrossCampusWithoutAuthority() {
        User sender = User.builder().username("u1").email("u1@campus.edu").status(UserStatus.ACTIVE).points(0).creditScore(100).build();
        sender.setId(1L);
        sender.setCampusId(10L);

        User receiver = User.builder().username("u2").email("u2@campus.edu").status(UserStatus.ACTIVE).points(0).creditScore(100).build();
        receiver.setId(2L);
        receiver.setCampusId(20L);

        when(userRepository.findByUsername("u1")).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));

        assertThatThrownBy(() -> messageService.sendMessage(new SendMessageRequest(2L, com.campus.marketplace.common.enums.MessageType.TEXT, "hi")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.FORBIDDEN.getCode())
                .hasMessageContaining("跨校区通信被禁止");
    }

    @Test
    @DisplayName("跨校发送消息-有跨校权限放行")
    void sendMessage_success_whenCrossCampusWithAuthority() {
        User sender = User.builder().username("u1").email("u1@campus.edu").status(UserStatus.ACTIVE).points(0).creditScore(100).build();
        sender.setId(1L);
        sender.setCampusId(10L);
        User receiver = User.builder().username("u2").email("u2@campus.edu").status(UserStatus.ACTIVE).points(0).creditScore(100).build();
        receiver.setId(2L);
        receiver.setCampusId(20L);

        when(userRepository.findByUsername("u1")).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(blacklistRepository.existsByUserIdAndBlockedUserId(2L, 1L)).thenReturn(false);
        when(conversationRepository.findByTwoUsers(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> {
            Conversation c = invocation.getArgument(0);
            c.setId(100L);
            return c;
        });
        when(messageRepository.save(any())).thenAnswer(invocation -> {
            com.campus.marketplace.common.entity.Message m = invocation.getArgument(0);
            m.setId(200L);
            return m;
        });

        securityUtilMock.when(() -> com.campus.marketplace.common.utils.SecurityUtil.hasAuthority("system:campus:cross"))
                .thenReturn(true);

        Long mid = messageService.sendMessage(new SendMessageRequest(2L, com.campus.marketplace.common.enums.MessageType.TEXT, "hi"));
        assertThat(mid).isNotNull();
        verify(messageRepository).save(any());
    }
}
