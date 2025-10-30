package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.SendMessageRequest;
import com.campus.marketplace.common.dto.response.ConversationResponse;
import com.campus.marketplace.common.dto.response.MessageResponse;
import com.campus.marketplace.common.entity.Conversation;
import com.campus.marketplace.common.entity.Message;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.ComplianceAction;
import com.campus.marketplace.common.enums.MessageStatus;
import com.campus.marketplace.common.enums.MessageType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.BlacklistRepository;
import com.campus.marketplace.repository.ConversationRepository;
import com.campus.marketplace.repository.MessageRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.ComplianceService.TextResult;
import com.campus.marketplace.service.impl.MessageServiceImpl;
import com.campus.marketplace.websocket.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("消息服务实现测试")
class MessageServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private BlacklistRepository blacklistRepository;
    @Mock private com.campus.marketplace.common.utils.SensitiveWordFilter sensitiveWordFilter;
    @Mock private ComplianceService complianceService;
    @Mock private WebSocketSessionManager sessionManager;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
        securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUsername).thenReturn("alice");
        securityUtilMock.when(() -> SecurityUtil.hasAuthority(anyString())).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @AfterEach
    void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("发送消息成功：补齐会话更新、Redis 自增与 WebSocket 推送")
    void sendMessage_success() throws Exception {
        User sender = buildUser(1L, "alice", 1L);
        User receiver = buildUser(2L, "bob", 1L);
        Conversation conversation = Conversation.builder()
                .user1Id(1L)
                .user2Id(2L)
                .lastMessageId(99L)
                .lastMessageTime(LocalDateTime.now().minusMinutes(5))
                .build();
        conversation.setId(10L);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(blacklistRepository.existsByUserIdAndBlockedUserId(2L, 1L)).thenReturn(false);
        when(complianceService.moderateText("你好", "MESSAGE_CONTENT"))
                .thenReturn(new TextResult(false, ComplianceAction.PASS, "你好", Set.of()));
        when(conversationRepository.findByTwoUsers(1L, 2L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message saved = invocation.getArgument(0);
            saved.setId(200L);
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        WebSocketSession session = mock(WebSocketSession.class);
        when(sessionManager.getSession(2L)).thenReturn(session);
        when(session.isOpen()).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        Long messageId = messageService.sendMessage(new SendMessageRequest(2L, MessageType.TEXT, "你好"));

        assertThat(messageId).isEqualTo(200L);
        verify(valueOperations).increment("msg:unread:2");
        verify(redisTemplate).expire(eq("msg:unread:2"), any());
        verify(session).sendMessage(isA(TextMessage.class));

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("你好");
        assertThat(messageCaptor.getValue().getStatus()).isEqualTo(MessageStatus.UNREAD);
    }

    @Test
    @DisplayName("发送消息跨校区受限会抛出业务异常")
    void sendMessage_crossCampusForbidden() {
        securityUtilMock.when(() -> SecurityUtil.hasAuthority("system:campus:cross")).thenReturn(false);

        User sender = buildUser(1L, "alice", 1L);
        User receiver = buildUser(2L, "bob", 2L);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));

        assertThatThrownBy(() -> messageService.sendMessage(new SendMessageRequest(2L, MessageType.TEXT, "hi")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.FORBIDDEN.getCode());
    }

    @Test
    @DisplayName("发送消息至自己立即抛出无效参数异常")
    void sendMessage_selfNotAllowed() {
        User sender = buildUser(1L, "alice", 1L);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(sender));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        assertThatThrownBy(() -> messageService.sendMessage(new SendMessageRequest(1L, MessageType.TEXT, "hi")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_PARAMETER.getCode());
    }

    @Test
    @DisplayName("黑名单拦截会阻止消息发送")
    void sendMessage_blockedByReceiver() {
        User sender = buildUser(1L, "alice", 1L);
        User receiver = buildUser(2L, "bob", 1L);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(blacklistRepository.existsByUserIdAndBlockedUserId(2L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> messageService.sendMessage(new SendMessageRequest(2L, MessageType.TEXT, "hi")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode());
    }

    @Test
    @DisplayName("未读消息数命中缓存直接返回")
    void getUnreadCount_cacheHit() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(buildUser(1L, "alice", 1L)));
        when(valueOperations.get("msg:unread:1")).thenReturn(5L);

        int count = messageService.getUnreadCount();

        assertThat(count).isEqualTo(5);
        verify(messageRepository, never()).countByReceiverIdAndStatus(anyLong(), any());
    }

    @Test
    @DisplayName("未读消息数缓存未命中则查询数据库并回填")
    void getUnreadCount_cacheMiss() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(buildUser(1L, "alice", 1L)));
        when(valueOperations.get("msg:unread:1")).thenReturn(null);
        when(messageRepository.countByReceiverIdAndStatus(1L, MessageStatus.UNREAD)).thenReturn(3L);

        int count = messageService.getUnreadCount();

        assertThat(count).isEqualTo(3);
        verify(valueOperations).set(eq("msg:unread:1"), eq(3L), any(java.time.Duration.class));
    }

    @Test
    @DisplayName("会话列表包含未读数与对方信息")
    void listConversations_success() {
        User current = buildUser(1L, "alice", 1L);
        User other = buildUser(2L, "bob", 1L);

        Conversation conversation = Conversation.builder()
                .user1Id(1L)
                .user2Id(2L)
                .lastMessageId(88L)
                .lastMessageTime(LocalDateTime.now())
                .build();
        conversation.setId(50L);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(current));
        when(conversationRepository.findByUserIdPaginated(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(conversation)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(other));
        when(messageRepository.countByConversationIdAndReceiverIdAndStatus(50L, 1L, MessageStatus.UNREAD))
                .thenReturn(7L);

        Page<ConversationResponse> page = messageService.listConversations(0, 10);

        assertThat(page.getContent()).hasSize(1);
        ConversationResponse response = page.getContent().getFirst();
        assertThat(response.getOtherUsername()).isEqualTo("bob");
        assertThat(response.getUnreadCount()).isEqualTo(7);
    }

    @Test
    @DisplayName("查看非本人的会话会触发无权限异常")
    void listMessages_forbidden() {
        User current = buildUser(1L, "alice", 1L);
        Conversation conversation = Conversation.builder()
                .user1Id(2L)
                .user2Id(3L)
                .build();
        conversation.setId(66L);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(current));
        when(conversationRepository.findById(66L)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> messageService.listMessages(66L, 0, 10))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.FORBIDDEN.getCode());
    }

    @Test
    @DisplayName("查询聊天记录成功映射 DTO")
    void listMessages_success() {
        User current = buildUser(1L, "alice", 1L);
        Conversation conversation = Conversation.builder()
                .user1Id(1L)
                .user2Id(2L)
                .build();
        conversation.setId(66L);

        Message message = Message.builder()
                .conversationId(66L)
                .senderId(1L)
                .receiverId(2L)
                .messageType(MessageType.TEXT)
                .content("hello")
                .status(MessageStatus.UNREAD)
                .isRecalled(false)
                .build();
        message.setId(300L);
        message.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(current));
        when(conversationRepository.findById(66L)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(eq(66L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(message)));

        Page<MessageResponse> page = messageService.listMessages(66L, 0, 10);

        assertThat(page.getTotalElements()).isOne();
        MessageResponse response = page.getContent().getFirst();
        assertThat(response.getMessageId()).isEqualTo(300L);
        assertThat(response.getContent()).isEqualTo("hello");
    }

    private static User buildUser(Long id, String username, Long campusId) {
        User user = User.builder()
                .username(username)
                .campusId(campusId)
                .build();
        user.setId(id);
        return user;
    }
}
